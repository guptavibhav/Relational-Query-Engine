package edu.buffalo.cse562.optimizer;

import java.util.ArrayList;
import java.util.List;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.schema.Column;
import edu.buffalo.cse562.conversion.VariableFinder;
import edu.buffalo.cse562.logicaloperators.DualSourceLogicalOperator;
import edu.buffalo.cse562.logicaloperators.LogicalAliasOp;
import edu.buffalo.cse562.logicaloperators.LogicalJoinOp;
import edu.buffalo.cse562.logicaloperators.LogicalOperator;
import edu.buffalo.cse562.logicaloperators.LogicalQuery;
import edu.buffalo.cse562.logicaloperators.LogicalScanOp;
import edu.buffalo.cse562.logicaloperators.LogicalSelectionOperator;
import edu.buffalo.cse562.logicaloperators.SingleSourceLogicalOperator;
import edu.buffalo.cse562.relations.Schema;
import edu.buffalo.cse562.reloperators.VariableContext;
import edu.buffalo.cse562.util.UtilityFunctions;

/*
 * Instead of taking in an entire query, we'll just work on optimizing the
 * sources (joins) and selections.  This can be done independently of the
 * sorting and projection optimization.
 */
public class PreOptimizer 
{
	private LogicalOperator mRoot;
	private Expression mCondition;
	private VariableContext mOuterContext;
	
	public PreOptimizer(LogicalOperator fromRoot, Expression condition, VariableContext outerContext) 
	{
		mRoot = fromRoot;
		mCondition = condition;
		mOuterContext = outerContext;
	}
	
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
	
	private void RecSplitSelections(LogicalOperator root, List<Expression> conditions)
    {
		if (root instanceof LogicalQuery || root instanceof LogicalAliasOp || root instanceof LogicalScanOp)
        {
			//These either indicate a new query or end of the line, so we want to stop here.
            return;
        }
		else if (root instanceof LogicalSelectionOperator)
        {
            LogicalSelectionOperator selection = (LogicalSelectionOperator)root;
            SplitConditions(selection.Condition, conditions);
            RecSplitSelections(selection.Source, conditions);
        }
        else if (root instanceof SingleSourceLogicalOperator)
        {
            //The Selections we care about always start above the from conditions, so
            //don't worry about dual source operators.  If they are above from items,
        	//then they were already part of a join and we'll get them later
            RecSplitSelections(((SingleSourceLogicalOperator)root).Source, conditions);
        }
        else if (root instanceof DualSourceLogicalOperator)
        {
        	//We split the join condition to so we can apply it
        	//where we want to
        	if (root instanceof LogicalJoinOp)
        	{
        		LogicalJoinOp join = (LogicalJoinOp)root;
        		SplitConditions(join.Condition, conditions);
        	}
        	
        	RecSplitSelections(((DualSourceLogicalOperator)root).Source1, conditions);
        	RecSplitSelections(((DualSourceLogicalOperator)root).Source2, conditions);
        }
    }
	
	private void getSources(LogicalOperator root, List<LogicalOperator> sources)
    {
    	if (root instanceof LogicalScanOp || root instanceof LogicalQuery || root instanceof LogicalAliasOp)
    	{
    		sources.add(root);
    	}
    	else if (root instanceof SingleSourceLogicalOperator)
    	{
    		getSources(((SingleSourceLogicalOperator)root).Source, sources);
    	}
    	else if (root instanceof DualSourceLogicalOperator)
    	{
    		getSources(((DualSourceLogicalOperator)root).Source1, sources);
    		getSources(((DualSourceLogicalOperator)root).Source2, sources);
    	}
    }
	
	private void optimizeSimpleSelects(List<Expression> conditions, List<LogicalOperator> sources)
    {
    	Schema fullSchema = this.mRoot.getSchema();
    	
    	/*
    	 * For each condition, get the list of variables.
    	 * If they all belong to the same table, apply the condition to that source
    	 */
    	
    	for (int i = conditions.size() - 1; i >= 0; i--)
    	{
    		VariableFinder vf = new VariableFinder();
    		List<Column> variables = vf.getColumnReferences(conditions.get(i), fullSchema);
    		
    		for (int j = 0; j < sources.size(); j++)
    		{
    			LogicalOperator source = sources.get(j);
    			Schema schema = source.getSchema();
    			boolean ok = true;
    			
    			for (Column c : variables)
        		{
    				if (mOuterContext != null && 
    						mOuterContext.ContextSchema.contains(UtilityFunctions.getTableSafely(c), c.getColumnName()))
					{
    					//Sub-select
    					//Don't worry about this, it's essentially a constant to us		
					}
    				else if (!schema.contains(UtilityFunctions.getTableSafely(c), c.getColumnName()))
        			{
        				ok = false;
        				break;
        			}
        		}
    			
    			if (ok)
    			{
    				//We found a match
    				sources.set(j, new LogicalSelectionOperator(source, conditions.get(i), this.mOuterContext));
    				conditions.remove(i);
    			}
    		}
    	}
    }
	
	private List<Expression> getJoinConditions(List<Expression> allConditions)
    {
    	ArrayList<Expression> joinConditions = new ArrayList<Expression>();
    	
    	for (int i = allConditions.size() - 1; i >= 0; i--)
    	{
    		Expression expr = allConditions.get(i);
    		
    		if (expr instanceof EqualsTo)
    		{
    			EqualsTo eqOp = (EqualsTo)expr;
    			if (eqOp.getLeftExpression() instanceof Column &&
    				eqOp.getRightExpression() instanceof Column)
				{
    				//This must be a join condition since we've already handled
    				//var = var for the same table
    				
    				/*
    				 * We need to be careful if this is in the outer schema.  It should not
    				 * be considered for joins.
    				 */
    				
    				boolean skip = false;
    				
    				if (mOuterContext != null)
    				{
    					Column c1 = (Column)eqOp.getLeftExpression();
    					Column c2 = (Column)eqOp.getRightExpression();
    					
    					if (mOuterContext.ContextSchema.contains(UtilityFunctions.getTableSafely(c1), c1.getColumnName()) ||
    							mOuterContext.ContextSchema.contains(UtilityFunctions.getTableSafely(c2), c2.getColumnName()))
    					{
    						skip = true;
    					}
    				}
    				
    				if (!skip)
    				{
    					joinConditions.add(expr);
    					allConditions.remove(i);
    				}
				}
    		}
    	}
    	
    	return joinConditions;
    }
	
	public LogicalOperator optimize()
	{
		/*
		 * Create a complete logical operator chain to process
		 */
		LogicalOperator query = null;
		
		if (mCondition == null)
			query = mRoot;
		else
			query = new LogicalSelectionOperator(mRoot, mCondition, mOuterContext);
		
		/*
		 * First, split the condition into conjuncts
		 */
        List<Expression> conditions = new ArrayList<Expression>();
    	RecSplitSelections(query, conditions);
		
    	/*
    	 * Next, get the list of sources
    	 */
    	List<LogicalOperator> sources = new ArrayList<LogicalOperator>();
    	getSources(mRoot, sources);
    	
    	/*
    	 * Next, optimize SOME selections by pushing expressions that involve
    	 * only one relation.
    	 */
    	optimizeSimpleSelects(conditions, sources);

    	/*
    	 * Now, find any join conditions (A = B) and remove them from the remaining conditions
    	 */
    	List<Expression> joinConditions = getJoinConditions(conditions);
    	
    	/*
    	 * Next, optimize the join order.  This could really be anything.
    	 */
    	LogicalOperator combinedSource = new JoinOrderOptimizer(joinConditions, sources).Optimize();
    	
    	/*
    	 * Finally, if there are any remaining conditions, patch them together and wrap
    	 * the joins in it.
    	 */
    	if (conditions.size() > 0)
    	{
    		for (int i = 0; i < conditions.size(); i++)
    		{
    			combinedSource = new LogicalSelectionOperator(combinedSource, conditions.get(i), this.mOuterContext);
    		}
    	}
    	
		return combinedSource;
	}

}
