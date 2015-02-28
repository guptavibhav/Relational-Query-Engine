package edu.buffalo.cse562.ExprTree;

import edu.buffalo.cse562.Exceptions.SQLTypeMismatchException;
import edu.buffalo.cse562.Factors.BooleanFactor;
import edu.buffalo.cse562.Factors.Factor;

public class InverseOpNode extends ExpressionNode 
{
	private ExpressionNode mInnerExpr;
	
	public InverseOpNode(ExpressionNode innerExpr) 
	{
		mInnerExpr = innerExpr;
	}

	@Override
	public Factor ToFactor() 
	{
		Factor f = mInnerExpr.ToFactor();
		if (f instanceof BooleanFactor)
			return new BooleanFactor(!(((BooleanFactor) f).getValue()));
		else
			throw new SQLTypeMismatchException("You can only invert boolean factors");
	}

}
