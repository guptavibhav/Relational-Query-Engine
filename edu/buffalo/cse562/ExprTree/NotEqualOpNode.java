package edu.buffalo.cse562.ExprTree;

import edu.buffalo.cse562.Factors.Factor;

public class NotEqualOpNode extends BinaryOperationNode 
{
	public NotEqualOpNode(ExpressionNode lhs, ExpressionNode rhs) 
	{
		super(lhs, rhs);
	}

	@Override
	public Factor ToFactor() 
	{
		return Factor.NotEqualOp(this.LHS.ToFactor(), this.RHS.ToFactor());
	}
}
