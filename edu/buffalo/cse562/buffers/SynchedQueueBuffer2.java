package edu.buffalo.cse562.buffers;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import edu.buffalo.cse562.Main;
import edu.buffalo.cse562.relations.FactorTuple;

public class SynchedQueueBuffer2 implements ITupleBuffer 
{
	private static final int POLL_TIMEOUT = 5;	//Milliseconds
	
	private Queue<FactorTuple> mQueue = new LinkedList<FactorTuple>();
    private boolean mIsDone = false;
    private ReentrantLock mQueueLock;
    private Condition mCondition;
    private Condition mCapCondition;
	private int mCount = 0;
	private static final int MAX_CAP = 7000;
    
	public SynchedQueueBuffer2() 
	{
		mQueueLock = new ReentrantLock();
		mCondition = mQueueLock.newCondition();
		mCapCondition = mQueueLock.newCondition();
	}

	@Override
	public FactorTuple dequeueTuple() 
	{
		FactorTuple tuple = null;
		
		mQueueLock.lock();
		
		while (mCount == 0 && !mIsDone)
		{
			try 
			{
				mCondition.await();
			} 
			catch (InterruptedException e) 
			{
				// TODO Auto-generated catch block
				mQueueLock.unlock();
				e.printStackTrace();
				Main.onError();
			}
		}
		
		//At this point the size is non-empty or there won't be any more items.
		//Also, we hold the lock
		
		if (mCount > 0)
		{
			tuple = mQueue.remove();
			--mCount;
			mCapCondition.signalAll();
		}
		
		mQueueLock.unlock();
		return tuple;
	}

	@Override
	public void enqueueTuple(FactorTuple tuple) 
	{
		mQueueLock.lock();
		
		if (mQueue.size() == MAX_CAP)
		{
			try 
			{
				mCapCondition.await();
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
				Main.onError();
			}
		}
		
		if (!mIsDone)
		{
			mQueue.add(tuple);
			mCount++;	
		}
		
		mCondition.signalAll();
		mQueueLock.unlock();
	}

	@Override
	public void markDone() 
	{
		mQueueLock.lock();
		mIsDone = true;
		mCondition.signalAll();
		mCapCondition.signalAll();
		mQueueLock.unlock();
	}

	@Override
	public int size() 
	{
		return mCount;
	}

	@Override
	public void reset() 
	{
		this.mQueue.clear();
		mIsDone = false;
	}

}
