package edu.buffalo.cse562.ExprTree;

import edu.buffalo.cse562.Factors.Factor;

public class MultiplyOpNode extends BinaryOperationNode 
{
	public MultiplyOpNode(ExpressionNode lhs, ExpressionNode rhs) 
	{
		super(lhs, rhs);
	}

	@Override
	public Factor ToFactor() 
	{
		return Factor.Multiply(this.LHS.ToFactor(), this.RHS.ToFactor());
	}
}
