package edu.buffalo.cse562.ExprTree;

import edu.buffalo.cse562.Exceptions.SQLTypeMismatchException;
import edu.buffalo.cse562.Factors.Factor;
import edu.buffalo.cse562.Factors.StringFactor;

public class ConcatOpNode extends BinaryOperationNode  
{
	public ConcatOpNode(ExpressionNode lhs, ExpressionNode rhs) 
	{
		super(lhs, rhs);
	}

	@Override
	public Factor ToFactor() 
	{
		Factor f1 = this.LHS.ToFactor();
		Factor f2 = this.LHS.ToFactor();
		
		if (f1 instanceof StringFactor && f2 instanceof StringFactor)
		{
			String s1 = f1.toString();
			String s2 = f2.toString();
			return new StringFactor(s1 + s2);
		}
		else
		{
			throw new SQLTypeMismatchException("You can only concatenate strings");
		}
	}

}
