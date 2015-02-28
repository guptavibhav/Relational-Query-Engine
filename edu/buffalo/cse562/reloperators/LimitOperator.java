package edu.buffalo.cse562.reloperators;

import edu.buffalo.cse562.relations.FactorTuple;
import edu.buffalo.cse562.relations.Schema;

public class LimitOperator extends SingleSourceOperator 
{
	int mLimit = 0;
	
	public LimitOperator(RelationalOperator source, int limit) 
	{
		super(source);
		
		mLimit = limit;
		setSchema();
	}

	@Override
	protected void setSchema() 
	{
		mSchema = new Schema(this.mSource.mSchema);
	}
	
	@Override
	public void run() 
	{
		super.run();
		
		FactorTuple t = null;
		int count = 0;
		
		while (!mStop && ((t = mSource.mBuffer.dequeueTuple()) != null))
		{
			this.mBuffer.enqueueTuple(t);
			
			++count;
			
			if (count == mLimit)
			{
				stop();
				break;
			}
		}
		
		mBuffer.markDone();
	}

}
