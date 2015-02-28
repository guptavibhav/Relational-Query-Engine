package edu.buffalo.cse562.reloperators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.buffalo.cse562.Factors.Factor;
import edu.buffalo.cse562.buffers.ITupleBuffer;
import edu.buffalo.cse562.relations.FactorTuple;

public class InMemoryHashJoin extends EquiJoinOperator {

	public InMemoryHashJoin(RelationalOperator ra1, RelationalOperator ra2, List<EquiJoinMapping> mapping) 
	{
		super(ra1, ra2, mapping);
		setSchema();
	}
	
	private String GetHashKey(FactorTuple t, boolean lhs)
    {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < mMapping.size(); i++)
        {
            if (i > 0)
                sb.append('|');

            if (lhs)
            	sb.append(t.get(mMapping.get(i).LHS).toString());
            else
            	sb.append(t.get(mMapping.get(i).RHS).toString());
        }

        return sb.toString();
    }
	
	private HashMap<String, List<FactorTuple>> Materialize(ITupleBuffer buffer)
	{
		HashMap<String, List<FactorTuple>> hash = new HashMap<String, List<FactorTuple>>();
		
		FactorTuple t = null;
		
		while ((t = buffer.dequeueTuple()) != null && !mStop)
		{
			String key = GetHashKey(t, false);
			List<FactorTuple> values = hash.get(key);
			if (values == null)
			{
				values = new ArrayList<FactorTuple>();
				values.add(t);
				hash.put(key, values);
			}
			else
			{
				values.add(t);
			}
		}
		
		return hash;
	}
	
	private FactorTuple MergeTuples(FactorTuple lhs, FactorTuple rhs)
    {
        FactorTuple outTuple = new FactorTuple();
        
        for (Factor f : lhs)
        {
            outTuple.add(f);
        }

        for (Factor f : rhs)
        {
            outTuple.add(f);
        }

        return outTuple;
    }
	
	@Override
	public void run() 
	{
		super.run();
		
		//Materialize and hash the RHS
		HashMap<String, List<FactorTuple>> hash = Materialize(this.mSource2.mBuffer);
		
		//Process LHS, matching with RHS
        FactorTuple t = null;

        while (!mStop && (t = this.mSource1.mBuffer.dequeueTuple()) != null)
        {
        	List<FactorTuple> values = hash.get(GetHashKey(t, true));
        	
        	if (values != null)
        	{
        		for (FactorTuple rhs : values)
        		{
        			this.mBuffer.enqueueTuple(MergeTuples(t, rhs));
        		}
        	}
        }
        
        this.mBuffer.markDone();
		
	}

}
