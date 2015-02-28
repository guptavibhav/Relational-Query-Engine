package edu.buffalo.cse562.reloperators;

import edu.buffalo.cse562.Factors.Factor;
import edu.buffalo.cse562.Factors.IFactor;
import edu.buffalo.cse562.Factors.ListFactor;
import edu.buffalo.cse562.relations.FactorTuple;
import edu.buffalo.cse562.relations.Schema;

/*
 * This class is a both an operator and an IFactor.  Its job is to execute
 */
public class SubSelectOperator extends SingleSourceOperator implements IFactor 
{
	private ListFactor mResults;
	private boolean mRunOnce = false;
	private boolean mHasRun = false;
	
	public SubSelectOperator(RelationalOperator source, boolean runOnce) 
	{
		super(source);
		mRunOnce = runOnce;
		setSchema();
	}

	@Override
	public void run() 
	{
		super.run();
		
		FactorTuple t = null;
		
		while (!mStop && (t = mSource.mBuffer.dequeueTuple()) != null)
		{
			mResults.add(t.get(0));
		}
		
		this.mBuffer.markDone();
		mHasRun = true;
	}

	@Override
	protected void setSchema() 
	{
		this.mSchema = new Schema(mSource.mSchema);
	}

	@Override
	public Factor ToFactor() 
	{
		/*
		 * If we don't need to run this every time, then just return the previous
		 * results if we've already run the query.
		 */
		if (mRunOnce && mHasRun)
			return mResults;
		else if (mHasRun)
			this.reset();
		
		mResults = new ListFactor();
		Thread t = new Thread(this);
		t.start();
		
		try 
		{
			//Wait for the query to run
			t.join();
		}
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
		
		return mResults;
	}

}
