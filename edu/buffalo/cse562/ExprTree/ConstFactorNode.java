package edu.buffalo.cse562.ExprTree;

import edu.buffalo.cse562.Factors.Factor;

/**
 * This class wraps a literal factor
 * 		e.g. 1, 555653.45, 'Hello', etc.
 * 
 * @author scott
 *
 */
public class ConstFactorNode extends ExpressionNode 
{
	private Factor mFactor;
	
	public ConstFactorNode(Factor f) 
	{
		mFactor = f;
	}

	@Override
	public Factor ToFactor() 
	{
		return mFactor;
	}

}
