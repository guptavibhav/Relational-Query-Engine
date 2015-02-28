package edu.buffalo.cse562.reloperators;

import edu.buffalo.cse562.relations.FactorTuple;
import edu.buffalo.cse562.relations.Schema;

public class UnionOperator extends DualSourceOperator 
{
	public UnionOperator(RelationalOperator ra1, RelationalOperator ra2) 
	{
		super(ra1, ra2);
		
		setSchema();
	}

	@Override
	protected void setSchema() 
	{
		//per SQL spec, our schema is the same as the LHS of the union
		mSchema = new Schema(mSource1.mSchema);
	}
	
	private void drain(RelationalOperator op)
	{
		FactorTuple t = null;
		
		while (!mStop && (t = op.mBuffer.dequeueTuple()) != null)
		{
			this.mBuffer.enqueueTuple(t);
		}
	}
	
	@Override
	public void run() 
	{
		super.run();
		
		drain(mSource1);
		drain(mSource2);
		
		mBuffer.markDone();
	}
}
