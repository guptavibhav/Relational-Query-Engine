package edu.buffalo.cse562.reloperators;

public abstract class SingleSourceOperator extends RelationalOperator 
{
	protected RelationalOperator mSource;
	
	public SingleSourceOperator(RelationalOperator source)
	{
		mSource = source;
	}
	
	@Override
	public void run() 
	{
		new Thread(mSource).start();
	}

	@Override
	protected abstract void setSchema(); 
	
	@Override
	protected void reset() 
	{
		mBuffer.reset();
		mSource.reset();
		mStop = false;
	}
	
	@Override
	protected void stop() 
	{
		super.stop();
		mSource.stop();
		mSource.mBuffer.markDone();
	}

}
