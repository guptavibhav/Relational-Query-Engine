package edu.buffalo.cse562.ExprTree;

import edu.buffalo.cse562.Factors.Factor;

public class EqualOpNode extends BinaryOperationNode 
{
	public EqualOpNode(ExpressionNode lhs, ExpressionNode rhs) 
	{
		super(lhs, rhs);
	}

	@Override
	public Factor ToFactor() 
	{
		return Factor.EqualOp(this.LHS.ToFactor(), this.RHS.ToFactor());
	}
}
