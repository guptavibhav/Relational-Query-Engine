package edu.buffalo.cse562.aggregates;

import net.sf.jsqlparser.expression.Expression;
import edu.buffalo.cse562.Factors.BooleanFactor;
import edu.buffalo.cse562.Factors.Factor;
import edu.buffalo.cse562.Factors.NullFactor;
import edu.buffalo.cse562.relations.SchemaColumn;

public class MaximumAggregate extends AggregateFunction 
{
	public static final String FUNCTION_NAME = "MAX";
	
	private Factor mMaxValue;
	
	public MaximumAggregate(Expression exprToAgg, String thisVarName) 
	{
		super(FUNCTION_NAME, exprToAgg, thisVarName);
		reset();
	}

	@Override
	public Factor ToFactor() 
	{
		return mMaxValue;
	}

	@Override
	public void update(Factor f) 
	{
		if (mMaxValue instanceof NullFactor)
		{
			mMaxValue = f;
		}
		else
		{
			BooleanFactor res = (BooleanFactor) Factor.GreaterThan(f, mMaxValue);
			if (res.getValue())
			{
				mMaxValue = f;
			}
		}
			
	}

	@Override
	public void reset() 
	{
		mMaxValue = new NullFactor();
	}

}
