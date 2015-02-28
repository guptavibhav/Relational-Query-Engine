package edu.buffalo.cse562.ExprTree;

import edu.buffalo.cse562.Factors.Factor;

public class AddOpNode extends BinaryOperationNode 
{
	public AddOpNode(ExpressionNode lhs, ExpressionNode rhs) 
	{
		super(lhs, rhs);
	}

	@Override
	public Factor ToFactor() 
	{
		return Factor.Add(this.LHS.ToFactor(), this.RHS.ToFactor());
	}
}
