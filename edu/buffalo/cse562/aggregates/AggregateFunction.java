package edu.buffalo.cse562.aggregates;

import net.sf.jsqlparser.expression.Expression;

public abstract class AggregateFunction implements IAggregateFunction 
{
	private String mThisVarName = "";
	
	private Expression mExprToAggregate;
	
	private String mAggFunctionName = "";
	
	protected boolean mIsDistinct = false;
	
	/**
	 * 
	 * @param varToAgg What we are aggregating
	 * @param aggVarName The name of the variable to put this data
	 */
	public AggregateFunction(String fnName, Expression exprToAgg, String thisVarName) 
	{
		this.mAggFunctionName = fnName;
		mThisVarName = thisVarName;
		mExprToAggregate = exprToAgg;
	}
	
	public Expression getAggregatingExpression()
	{
		return mExprToAggregate;
	}
	
	public String getThisVarName()
	{
		return mThisVarName;
	}
	
	public String getAggFunctionName()
	{
		return mAggFunctionName;
	}
	
	public void setDistinct(boolean value)
	{
		mIsDistinct = value;
	}
	
	public boolean isDistinct()
	{
		return mIsDistinct;
	}

}
