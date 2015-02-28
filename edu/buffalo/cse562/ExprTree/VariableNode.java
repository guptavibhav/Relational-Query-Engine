package edu.buffalo.cse562.ExprTree;

import edu.buffalo.cse562.Factors.Factor;
import edu.buffalo.cse562.reloperators.VariableContext;

public class VariableNode extends ExpressionNode 
{
	private int mColIndex;
	private VariableContext mContext;
	
	public VariableNode(VariableContext context, int col) 
	{
		mContext = context;
		mColIndex = col;
	}

	@Override
	public Factor ToFactor() 
	{
		return mContext.CurrentValues.get(mColIndex);
	}

}
