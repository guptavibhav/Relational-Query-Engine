package edu.buffalo.cse562.ExprTree;

import edu.buffalo.cse562.Factors.BooleanFactor;
import edu.buffalo.cse562.Factors.Factor;
import edu.buffalo.cse562.Factors.NullFactor;

public class IsNullOp extends ExpressionNode 
{
	private ExpressionNode mInnerExpr;
	
	public IsNullOp(ExpressionNode innerExpr) 
	{
		mInnerExpr = innerExpr;
	}

	@Override
	public Factor ToFactor() 
	{
		Factor f = mInnerExpr.ToFactor();
		if (f instanceof NullFactor)
			return new BooleanFactor(true);
		else
			return new BooleanFactor(false);
	}

}
