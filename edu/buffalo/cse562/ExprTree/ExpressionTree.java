package edu.buffalo.cse562.ExprTree;

import edu.buffalo.cse562.Factors.*;
import edu.buffalo.cse562.aggregates.AggregateFunctionList;
import edu.buffalo.cse562.reloperators.QueryContext;
import net.sf.jsqlparser.expression.Expression;

/**
 * Class ExpressionTree - an Executable Expression Tree
 * 
 *  JSQL's trees cannot be executed directly without processing with a visitor or using the instanceof operator.  
 *  We'll do the conversion once so that higher levels can just call ToFactor() to retrieve the value.
 *  
 *  For the most part, this is just a parallel version of JSQL's expression tree.
 *  
 * @author scott
 *
 */

public class ExpressionTree implements IFactor 
{
	private ExpressionNode mRootExpression;
	
	/**
	 * Constructor for an executable ExpressionTree.
	 * 
	 * @param expr The JSQL expression to use to create our executable tree
	 * @param context The WorkingTuple object that contains the variable references we need
	 */
	public ExpressionTree(Expression expr, QueryContext context)
	{
		ExprNodeBuilder builder = new ExprNodeBuilder(expr, context);
		mRootExpression = builder.convert();
	}
	
	/**
	 * Constructor for an executable ExpressionTree.
	 * 
	 * @param expr The JSQL expression to use to create our executable tree
	 * @param schema The Schema object that contains the variable references we need
	 */
	public ExpressionTree(Expression expr, QueryContext context, AggregateFunctionList aggs)
	{
		ExprNodeBuilder builder = new ExprNodeBuilder(expr, context, aggs);
		mRootExpression = builder.convert();
	}
	
	@Override
	public Factor ToFactor() 
	{
		return mRootExpression.ToFactor();
	}

}
