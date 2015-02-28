package edu.buffalo.cse562.buffers;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import edu.buffalo.cse562.Main;
import edu.buffalo.cse562.relations.FactorTuple;

public class SynchedQueueBuffer extends LinkedBlockingQueue<FactorTuple> implements ITupleBuffer 
{
	private boolean mIsDone = false;
	private static final int POLL_TIMEOUT = 5;	//Milliseconds
	
	public SynchedQueueBuffer() 
	{
		super();
	}

	public SynchedQueueBuffer(int capacity)
	{
		super(capacity);
	}

	@Override
	public FactorTuple dequeueTuple() 
	{
		while (true)
		{
			try 
			{
				FactorTuple t = this.poll(POLL_TIMEOUT, TimeUnit.MILLISECONDS);
				
				if (t != null)
				{
					return t;
				}
				else if (mIsDone && this.size() == 0)
				{
					return null;
				}
				
			} catch (InterruptedException e) 
			{
				e.printStackTrace();
				Main.onError();
			}
		}
	}

	@Override
	public void enqueueTuple(FactorTuple tuple) 
	{
		try 
		{
			this.put(tuple);
		} 
		catch (InterruptedException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void markDone() 
	{
		this.mIsDone = true;
	}
	
	@Override
	public int size() {
		// TODO Auto-generated method stub
		return super.size();
	}

	@Override
	public void reset() 
	{
		this.clear();
		mIsDone = false;
	}

}
