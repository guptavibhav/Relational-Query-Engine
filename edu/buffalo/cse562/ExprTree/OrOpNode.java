package edu.buffalo.cse562.ExprTree;

import edu.buffalo.cse562.Factors.BooleanFactor;
import edu.buffalo.cse562.Factors.Factor;

public class OrOpNode extends BinaryOperationNode 
{
	public OrOpNode(ExpressionNode lhs, ExpressionNode rhs) 
	{
		super(lhs, rhs);
	}

	@Override
	public Factor ToFactor() 
	{
		//Short-circuit ORs
		Factor lhsFactor = this.LHS.ToFactor();
		
		if (lhsFactor instanceof BooleanFactor)
			if (((BooleanFactor) lhsFactor).getValue())
					return lhsFactor; 
		
		return Factor.Or(lhsFactor, this.RHS.ToFactor());
	}
}
