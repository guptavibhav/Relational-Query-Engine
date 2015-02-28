package edu.buffalo.cse562.ExprTree;

import edu.buffalo.cse562.Factors.BooleanFactor;
import edu.buffalo.cse562.Factors.Factor;

public class AndOpNode extends BinaryOperationNode 
{
	public AndOpNode(ExpressionNode lhs, ExpressionNode rhs) 
	{
		super(lhs, rhs);
	}

	@Override
	public Factor ToFactor() 
	{
		Factor lhsFactor = this.LHS.ToFactor();
		
		//Short-circuit ANDs
		if (lhsFactor instanceof BooleanFactor)
			if (!(((BooleanFactor) lhsFactor).getValue()))
					return lhsFactor; 
		
		return Factor.And(lhsFactor, this.RHS.ToFactor());
	}
}
