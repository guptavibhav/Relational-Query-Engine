package edu.buffalo.cse562.ExprTree;

import edu.buffalo.cse562.Factors.Factor;

public class ParenOpNode extends ExpressionNode 
{

	private ExpressionNode mInnerExpr;
	
	public ParenOpNode(ExpressionNode innerExpr) 
	{
		mInnerExpr = innerExpr;
	}

	@Override
	public Factor ToFactor() 
	{
		return mInnerExpr.ToFactor();
	}

}
