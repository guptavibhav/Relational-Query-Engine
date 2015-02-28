package edu.buffalo.cse562.optimizer;

import java.util.ArrayList;
import java.util.List;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import edu.buffalo.cse562.logicaloperators.DualSourceLogicalOperator;
import edu.buffalo.cse562.logicaloperators.LogicalAliasOp;
import edu.buffalo.cse562.logicaloperators.LogicalOperator;
import edu.buffalo.cse562.logicaloperators.LogicalQuery;
import edu.buffalo.cse562.logicaloperators.LogicalScanOp;
import edu.buffalo.cse562.logicaloperators.LogicalSelectionOperator;
import edu.buffalo.cse562.logicaloperators.SingleSourceLogicalOperator;
import edu.buffalo.cse562.reloperators.VariableContext;

public class PostOptimizer 
{
	private LogicalQuery mRoot;
    private VariableContext mOuterContext;
    
    private void SplitConditions(Expression condition, List<Expression> conditions)
    {
        if (condition instanceof AndExpression)
        {
            AndExpression andOp = (AndExpression)condition;
            SplitConditions(andOp.getLeftExpression(), conditions);
            SplitConditions(andOp.getRightExpression(), conditions);
        }
        else
        {
            conditions.add(condition);
        }
    }

    private void RecSplitSelections(LogicalOperator root)
    {
    	if (root instanceof LogicalQuery || root instanceof LogicalAliasOp || root instanceof LogicalScanOp)
        {
			//These either indicate a new query or end of the line, so we want to stop here.
            return;
        }
    	else if (root instanceof LogicalSelectionOperator)
        {
            LogicalSelectionOperator selection = (LogicalSelectionOperator)root;
            List<Expression> conditions = new ArrayList<Expression>();
            SplitConditions(selection.Condition, conditions);

            LogicalOperator source = selection.Source;

            if (conditions.size() > 1)
            {
                LogicalOperator op = source;

                for (int i = 0; i < conditions.size(); i++)
                {
                    op = new LogicalSelectionOperator(op, conditions.get(i), mOuterContext);
                }

                op.Parent = selection.Parent;
                op.Parent.replaceChild(root, op);

                selection.Parent = null;
                selection.Condition = null;
                selection.Source = null;
            }

            RecSplitSelections(source);
        }
        else if (root instanceof SingleSourceLogicalOperator)
        {
            //The Selections we care about always start above the from conditions, so
            //don't worry about dual source operators.  If they are above from items,
        	//then they were already part of a join and we'll get them later
            RecSplitSelections(((SingleSourceLogicalOperator)root).Source);
        }
    }

    private void RecJoinConditions(LogicalOperator root)
    {
        if (root instanceof LogicalSelectionOperator)
        {
            LogicalSelectionOperator selection = (LogicalSelectionOperator)root;

            if (selection.Source instanceof LogicalSelectionOperator)
            {
                LogicalSelectionOperator selection2 = (LogicalSelectionOperator)selection.Source;
                selection.Condition = new AndExpression(selection.Condition, selection2.Condition);

                //Clean up
                selection.Source = selection2.Source;
                selection2.Source.Parent = selection;

                selection2.Source = null;
                selection2.Parent = null;

                RecJoinConditions(selection);   //Let's keep going on this node
            }
            else
            {
                RecJoinConditions(selection.Source);
            }

        }
        else if (root instanceof SingleSourceLogicalOperator)
        {
            SingleSourceLogicalOperator sso = (SingleSourceLogicalOperator)root;
            RecJoinConditions(sso.Source);
        }
        else if (root instanceof DualSourceLogicalOperator)
        {
            DualSourceLogicalOperator dso = (DualSourceLogicalOperator)root;
            RecJoinConditions(dso.Source1);
            RecJoinConditions(dso.Source2);
        }
    }
    
	public PostOptimizer(LogicalQuery root, VariableContext outerContext) 
	{
		mRoot = root;
        mOuterContext = outerContext;	
	}
	
	public void optimize()
	{
		//Our subselects are already optimized.  We just need to process this
        //individual query
		
        //First, break apart all selections
        RecSplitSelections(mRoot);
        mRoot.recomputeSchema();

        //Next, optimize selection
        new SelectionOptimizer(mRoot).Optimize();

        //Next, optimize projection
        //TODO: Projection
        
        //Next, optimize sorting.  Remove any redundant sorts
        new SortOptimizer(mRoot).Optimize();
        
        //Finally, merge selections back together
        RecJoinConditions(mRoot);
	}

}
