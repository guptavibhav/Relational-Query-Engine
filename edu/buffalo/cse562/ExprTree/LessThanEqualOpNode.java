package edu.buffalo.cse562.ExprTree;

import edu.buffalo.cse562.Factors.Factor;

public class LessThanEqualOpNode extends BinaryOperationNode 
{
	public LessThanEqualOpNode(ExpressionNode lhs, ExpressionNode rhs) 
	{
		super(lhs, rhs);
	}

	@Override
	public Factor ToFactor() 
	{
		return Factor.LessThanEqual(this.LHS.ToFactor(), this.RHS.ToFactor());
	}
}
