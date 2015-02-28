package edu.buffalo.cse562.ExprTree;

import edu.buffalo.cse562.Factors.Factor;

public class DivideOpNode extends BinaryOperationNode 
{
	public DivideOpNode(ExpressionNode lhs, ExpressionNode rhs) 
	{
		super(lhs, rhs);
	}

	@Override
	public Factor ToFactor() 
	{
		return Factor.Divide(this.LHS.ToFactor(), this.RHS.ToFactor());
	}
}
