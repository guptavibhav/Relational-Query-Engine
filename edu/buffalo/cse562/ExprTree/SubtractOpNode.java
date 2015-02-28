package edu.buffalo.cse562.ExprTree;

import edu.buffalo.cse562.Factors.Factor;

public class SubtractOpNode extends BinaryOperationNode 
{
	public SubtractOpNode(ExpressionNode lhs, ExpressionNode rhs) 
	{
		super(lhs, rhs);
	}

	@Override
	public Factor ToFactor() 
	{
		return Factor.Subtract(this.LHS.ToFactor(), this.RHS.ToFactor());
	}
}
