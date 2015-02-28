package edu.buffalo.cse562.ExprTree;

import edu.buffalo.cse562.Factors.Factor;

public class GreaterThanEqualOpNode extends BinaryOperationNode 
{
	public GreaterThanEqualOpNode(ExpressionNode lhs, ExpressionNode rhs) 
	{
		super(lhs, rhs);
	}

	@Override
	public Factor ToFactor() 
	{
		return Factor.GreaterThanEqual(this.LHS.ToFactor(), this.RHS.ToFactor());
	}
}
