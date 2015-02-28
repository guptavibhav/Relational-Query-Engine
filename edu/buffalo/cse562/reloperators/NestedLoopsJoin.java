package edu.buffalo.cse562.reloperators;

import java.util.ArrayList;

import edu.buffalo.cse562.Main;
import edu.buffalo.cse562.Factors.Factor;
import edu.buffalo.cse562.buffers.ITupleBuffer;
import edu.buffalo.cse562.relations.FactorTuple;
import edu.buffalo.cse562.relations.Schema;
import edu.buffalo.cse562.relations.SchemaColumn;

public class NestedLoopsJoin extends DualSourceOperator 
{
	private ArrayList<FactorTuple> mBuffer1;
	private ArrayList<FactorTuple> mBuffer2;
	
	public NestedLoopsJoin(RelationalOperator ra1, RelationalOperator ra2) 
	{
		super(ra1, ra2);
		
		mBuffer1 = new ArrayList<FactorTuple>();
		mBuffer2 = new ArrayList<FactorTuple>();
		
		setSchema();
	}
	
	private class BufferDrainer implements Runnable
	{ 
		private ITupleBuffer mSource;
		private ArrayList<FactorTuple> mSink;
		
		public BufferDrainer(ITupleBuffer source, ArrayList<FactorTuple> sink)
		{
			mSource = source;
			mSink = sink;
		}
		
		@Override
		public void run() 
		{
			FactorTuple t = null;
			
			while ((t = mSource.dequeueTuple()) != null)
			{
				mSink.add(t);
			}
		}
	}
	
	private void drainBuffer(ITupleBuffer source, ArrayList<FactorTuple> sink)
	{
		FactorTuple t = null;
		
		while ((t = source.dequeueTuple()) != null)
		{
			sink.add(t);
		}
	}
	
	@Override
	public void run() 
	{
		super.run();
		
		Thread t1 = new Thread(new BufferDrainer(mSource1.mBuffer, mBuffer1));
		
		if (mStop)
		{
			this.mBuffer.markDone();
			return;
		}
		
		Thread t2 = new Thread(new BufferDrainer(mSource2.mBuffer, mBuffer2));
		
		//drainBuffer(mSource1.mBuffer, mBuffer1);
		//drainBuffer(mSource2.mBuffer, mBuffer2);
		
		t1.start();
		t2.start();
		
		try {
			t1.join();
			t2.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Main.onError();
		}
		
		//Bad nested loop join
		
		for (int i = 0; i < mBuffer1.size(); i++)
		{
			if (mStop)
				break;
			
			for (int j = 0; j < mBuffer2.size(); j++)
			{
				FactorTuple t = new FactorTuple();
				for (Factor f : mBuffer1.get(i))
				{
					t.add(f);
				}
				
				for (Factor f : mBuffer2.get(j))
				{
					t.add(f);
				}
				
				mBuffer.enqueueTuple(t);
			}
		}
		
		mBuffer.markDone();
		
	}
	
	@Override
	protected void setSchema() 
	{
		//Our schema as the join of the 2 schemas
		
		mSchema = new Schema();
		
		for (SchemaColumn sc : mSource1.mSchema)
		{
			mSchema.add(new SchemaColumn(sc));
		}
		
		for (SchemaColumn sc : mSource2.mSchema)
		{
			mSchema.add(new SchemaColumn(sc));
		}
	}

}
