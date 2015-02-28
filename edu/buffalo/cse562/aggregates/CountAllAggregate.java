package edu.buffalo.cse562.aggregates;

import net.sf.jsqlparser.expression.Expression;
import edu.buffalo.cse562.Factors.Factor;
import edu.buffalo.cse562.Factors.LongFactor;
import edu.buffalo.cse562.relations.SchemaColumn;

public class CountAllAggregate extends AggregateFunction
{
	public static final String FUNCTION_NAME = "COUNT(*)";
	
	private long mCount;
	
	public CountAllAggregate(String thisVarName) 
	{
		super(FUNCTION_NAME, null, thisVarName);
		reset();
	}

	@Override
	public Factor ToFactor() 
	{
		return new LongFactor(mCount);
	}

	@Override
	public void update(Factor f) 
	{
		mCount++;
	}

	@Override
	public void reset() 
	{
		mCount = 0;
	}

}
