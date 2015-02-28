package edu.buffalo.cse562.ExprTree;

import edu.buffalo.cse562.Factors.Factor;

public class GreaterThanOpNode extends BinaryOperationNode 
{
	public GreaterThanOpNode(ExpressionNode lhs, ExpressionNode rhs) 
	{
		super(lhs, rhs);
	}

	@Override
	public Factor ToFactor() 
	{
		return Factor.GreaterThan(this.LHS.ToFactor(), this.RHS.ToFactor());
	}
}
