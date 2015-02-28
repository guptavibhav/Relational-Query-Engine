package edu.buffalo.cse562.aggregates;

import java.util.Hashtable;

import net.sf.jsqlparser.expression.Expression;
import edu.buffalo.cse562.Factors.Factor;
import edu.buffalo.cse562.Factors.LongFactor;
import edu.buffalo.cse562.Factors.NullFactor;
import edu.buffalo.cse562.relations.SchemaColumn;

public class CountAggregate extends AggregateFunction
{
	public static final String FUNCTION_NAME = "COUNT";
	
	private long mCount;
	
	private Hashtable<Factor, Factor> mSeenValues;
	
	public CountAggregate(Expression exprToAgg, String thisVarName) 
	{
		this(exprToAgg, thisVarName, false);
	}

	public CountAggregate(Expression exprToAgg, String thisVarName, boolean isDistinct) 
	{
		super(FUNCTION_NAME, exprToAgg, thisVarName);
		reset();
		this.mIsDistinct = isDistinct;
	}
	
	@Override
	public Factor ToFactor() 
	{
		return new LongFactor(mCount);
	}

	@Override
	public void update(Factor f) 
	{
		if (!(f instanceof NullFactor))
		{
			if (!this.mIsDistinct || !mSeenValues.containsKey(f))
			{
				mCount++;
				if (mIsDistinct)
					mSeenValues.put(f, f);
			}
		}
	}

	@Override
	public void reset() 
	{
		mCount = 0;
		mSeenValues = new Hashtable<Factor, Factor>();
	}

}
