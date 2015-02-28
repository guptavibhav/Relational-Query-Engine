package edu.buffalo.cse562.reloperators;

import edu.buffalo.cse562.buffers.ITupleBuffer;
import edu.buffalo.cse562.buffers.SynchedQueueBuffer;
import edu.buffalo.cse562.buffers.SynchedQueueBuffer2;
import edu.buffalo.cse562.relations.Schema;

public abstract class RelationalOperator implements IRelationOperator, Runnable 
{	
	/*
	 * Local copy of the schema for this relation
	 */
	protected Schema mSchema;
	
	/*
	 * The buffer that we put our tuples.  These get consumed by our parent.
	 */
	protected ITupleBuffer mBuffer;
	
	/*
	 * Synch for LIMIT
	 */
	protected volatile boolean mStop;
	
	public RelationalOperator() 
	{
		//TODO: Change as necessary
		mBuffer = new SynchedQueueBuffer2();
	}
	
	/*
	 * State transitions for all relational algebra operators.
	 * The states go as follows:
	 * 
	 * 	new()		->	instantiated
	 * 
	 * 	setSchema() ->	schema_set
	 *  
	 *  begin()		->	running
	 */
	
	protected abstract void setSchema();
	
	@Override
	public Schema getSchema() 
	{
		return mSchema;
	}
	
	protected abstract void reset();
	
	protected void stop()
	{
		mStop = true;
		
		//We may be hanging waiting free space (LIMIT).  This should
		//clear us.
		this.mBuffer.markDone();
	}
}
