package edu.buffalo.cse562.ExprTree;

import edu.buffalo.cse562.Factors.Factor;

public class LessThanOpNode extends BinaryOperationNode 
{
	public LessThanOpNode(ExpressionNode lhs, ExpressionNode rhs) 
	{
		super(lhs, rhs);
	}

	@Override
	public Factor ToFactor() 
	{
		return Factor.LessThan(this.LHS.ToFactor(), this.RHS.ToFactor());
	}
}
