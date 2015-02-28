package edu.buffalo.cse562.ExprTree;

public abstract class BinaryOperationNode extends ExpressionNode 
{	
	protected ExpressionNode LHS;
	protected ExpressionNode RHS;
	
	public BinaryOperationNode(ExpressionNode lhs, ExpressionNode rhs)
	{
		LHS = lhs;
		RHS = rhs;
	}
}
