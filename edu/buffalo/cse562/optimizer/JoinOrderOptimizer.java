package edu.buffalo.cse562.optimizer;

import java.util.ArrayList;
import java.util.List;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.schema.Column;

import edu.buffalo.cse562.logicaloperators.*;
import edu.buffalo.cse562.relations.Schema;
import edu.buffalo.cse562.util.UtilityFunctions;

/*
 * This is an implementation of the left-deep dynamic programming algorithm
 * discussed in the text and other literature. 
 * 
 * We should be able to easily amend this to include better cost estimates.  For now,
 * a cross product has a cost of 1 and everything else has no cost.  The idea is to
 * minimize the number of cross products by selecting the best join conditions.
 */
public class JoinOrderOptimizer 
{
	private class PlanCosts
    {
        public int Cost = 0;
        public LogicalOperator Plan;
    }

    private List<Expression> mJoinConditions;
    private List<LogicalOperator> mJoinList;
    private PlanCosts[] mPlanCosts;

    public JoinOrderOptimizer(List<Expression> joinConditions, List<LogicalOperator> joinList)
    {
        mJoinConditions = joinConditions;
        mJoinList = joinList;
        mPlanCosts = new PlanCosts[(int)Math.pow(2, joinList.size())];
    }

    private LogicalOperator GetSingleJoinCost(LogicalOperator s1, LogicalOperator s2)
    {
        //First, find everything join condition for these two tables
        List<Expression> conditions = new ArrayList<Expression>();

        Schema lhs = s1.getSchema();
        Schema rhs = s2.getSchema();

        for (Expression expr : mJoinConditions)
        {
            //These were the conditions for adding the expression in the first place
            EqualsTo eqOp = (EqualsTo)expr;
            Column lvar = (Column)eqOp.getLeftExpression();
            Column rvar = (Column)eqOp.getRightExpression();

            
            if (lhs.contains((UtilityFunctions.getTableSafely(lvar)), lvar.getColumnName()))
            {
            	if ((rhs.contains((UtilityFunctions.getTableSafely(rvar)), rvar.getColumnName())))
            	{
            		conditions.add(expr);
            	}
            }
            else if (lhs.contains((UtilityFunctions.getTableSafely(rvar)), rvar.getColumnName()))
            {
            	if ((rhs.contains((UtilityFunctions.getTableSafely(lvar)), lvar.getColumnName())))
            	{
            		conditions.add(expr);
            	}
            }
        }

        LogicalOperator copy1 = s1.copy();
        LogicalOperator copy2 = s2.copy();

        if (conditions.size() == 0)
        {
            return new LogicalCrossProductOp(copy1, copy2);
        }
        else
        {
            Expression expr = conditions.get(0);
            
            for (int i = 1; i < conditions.size(); i++)
            {
            	expr = new AndExpression(expr, conditions.get(i));
            }
            
            return new LogicalJoinOp(copy1, copy2, expr);
        }
    }

    private PlanCosts FindBestPlan(int bitmap)
    {
        if (mPlanCosts[bitmap] != null)
            return mPlanCosts[bitmap];

        List<Integer> indexes = new ArrayList<Integer>();

        for (int i = 0; i < mJoinList.size(); i++)
        {
            int mask = 1 << i;

            if ((mask & bitmap) > 0)
            {
                indexes.add(i);
            }
        }

        PlanCosts best = new PlanCosts();
        best.Cost = Integer.MAX_VALUE;

        if (indexes.size() == 2)
        {
            best.Plan = GetSingleJoinCost(mJoinList.get(indexes.get(0)), mJoinList.get(indexes.get(1)));

            if (best.Plan instanceof LogicalCrossProductOp)
                best.Cost = 1;
            else
                best.Cost = 0;
        }
        else
        {
            for (int i = 0; i < indexes.size(); i++)
            {
                LogicalOperator left = mJoinList.get(indexes.get(i));
                int index = indexes.get(i);
                int mask = 1 << index;
                int remaining = (~mask) & bitmap;
                
                PlanCosts costRemaining = FindBestPlan(remaining);
                PlanCosts newCost = new PlanCosts();
                newCost.Plan = GetSingleJoinCost(left, costRemaining.Plan);
                newCost.Cost = costRemaining.Cost;

                if (newCost.Plan instanceof LogicalCrossProductOp)
                    newCost.Cost++;

                if (newCost.Cost < best.Cost)
                {
                    best = newCost;
                }
            }
        }

        //Store for future lookups
        mPlanCosts[bitmap] = best;
        
        return best;
    }

    public LogicalOperator Optimize()
    {
        int mask = 0;

        for (int i = 0; i < mJoinList.size(); i++)
        {
            mask |= (1 << i);   
        }

        PlanCosts cost = FindBestPlan(mask);
        return cost.Plan;
    }
}
