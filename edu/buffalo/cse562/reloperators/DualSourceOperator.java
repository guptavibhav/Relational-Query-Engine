package edu.buffalo.cse562.reloperators;

public abstract class DualSourceOperator extends RelationalOperator 
{

	protected RelationalOperator mSource1;
	protected RelationalOperator mSource2;
	
	public DualSourceOperator(RelationalOperator ra1, RelationalOperator ra2)
	{
		mSource1 = ra1;
		mSource2 = ra2;
	}
	
	@Override
	public void run() 
	{
		new Thread(mSource1).start();
		new Thread(mSource2).start();
	}

	@Override
	protected abstract void setSchema();
	
	@Override
	protected void reset() 
	{
		mBuffer.reset();
		mSource1.reset();
		mSource2.reset();
		mStop = false;
	}
	
	@Override
	protected void stop() 
	{
		super.stop();
		
		mSource1.stop();
		mSource2.stop();
		
		mSource1.mBuffer.markDone();
		mSource2.mBuffer.markDone();
	}

}
