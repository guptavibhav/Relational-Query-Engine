package edu.buffalo.cse562.buffers;

import edu.buffalo.cse562.relations.FactorTuple;

public interface ITupleBuffer 
{
	public FactorTuple dequeueTuple();
	public void enqueueTuple(FactorTuple tuple);
	public void markDone();
	public int size();
	public void reset();
}
