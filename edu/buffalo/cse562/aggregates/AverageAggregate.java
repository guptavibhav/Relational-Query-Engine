package edu.buffalo.cse562.aggregates;

import net.sf.jsqlparser.expression.Expression;
import edu.buffalo.cse562.Factors.Factor;
import edu.buffalo.cse562.Factors.LongFactor;
import edu.buffalo.cse562.relations.SchemaColumn;

public class AverageAggregate extends AggregateFunction
{
	public static final String FUNCTION_NAME = "AVG";
	
	private Factor mAccumulator;
	private long mCount;
	
	public AverageAggregate(Expression exprToAgg, String thisVarName) 
	{
		super(FUNCTION_NAME, exprToAgg, thisVarName);
		reset();
	}

	@Override
	public Factor ToFactor() 
	{
		if (mCount == 0)
			return new LongFactor(0);
		else
			return Factor.Divide(mAccumulator, new LongFactor(mCount));
	}

	@Override
	public void update(Factor f) 
	{
		mAccumulator = Factor.Add(mAccumulator, f);
		mCount++;
	}

	@Override
	public void reset() 
	{
		//Start with long and change to double if that's what gets added
		mAccumulator = new LongFactor(0);
		mCount = 0;
	}

}
