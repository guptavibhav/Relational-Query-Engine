package edu.buffalo.cse562.aggregates;

import net.sf.jsqlparser.expression.Expression;
import edu.buffalo.cse562.Factors.Factor;
import edu.buffalo.cse562.Factors.LongFactor;

public class SumAggregate extends AggregateFunction 
{
	public static final String FUNCTION_NAME = "SUM";
	
	private Factor mAccumulator;
	
	public SumAggregate(Expression exprToAgg, String thisVarName) 
	{
		super(FUNCTION_NAME, exprToAgg, thisVarName);
		reset();
	}

	@Override
	public Factor ToFactor() 
	{
		return mAccumulator;
	}

	@Override
	public void update(Factor f) 
	{
		mAccumulator = Factor.Add(mAccumulator, f);
	}

	@Override
	public void reset() 
	{
		//Start with long and change to double if that's what gets added
		mAccumulator = new LongFactor(0);
	}

}
