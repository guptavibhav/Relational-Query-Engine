package edu.buffalo.cse562.reloperators;

import java.util.ArrayList;
import java.util.List;

import edu.buffalo.cse562.Factors.Factor;
import edu.buffalo.cse562.buffers.ITupleBuffer;
import edu.buffalo.cse562.relations.FactorTuple;

public class SortMergeJoin extends EquiJoinOperator 
{
	private FactorTuple mLHSOverflow;
    private FactorTuple mRHSOverflow;
    
	public SortMergeJoin(RelationalOperator ra1, RelationalOperator ra2, List<EquiJoinMapping> mapping) 
	{
		super(ra1, ra2, mapping);
		setSchema();
	}

	private interface GetOverflowFunction
	{
		public FactorTuple getOverflow();
	}
	
	private interface SetOverlowFunction
	{
		public void SetOverflow(FactorTuple t);
	}
	
	private interface CompareToPrevTuple
	{
		public boolean CompareTuples(FactorTuple cur, FactorTuple prev);
	}
	
	private GetOverflowFunction mGetLHSOverflowFunction = new GetOverflowFunction() 
	{
		@Override
		public FactorTuple getOverflow() 
		{
			return mLHSOverflow;
		}
	};
	
	private GetOverflowFunction mGetRHSOverflowFunction = new GetOverflowFunction() 
	{
		@Override
		public FactorTuple getOverflow() 
		{
			return mRHSOverflow;
		}
	};
	
	private SetOverlowFunction mSetLHSOverflowFunction = new SetOverlowFunction() 
	{
		@Override
		public void SetOverflow(FactorTuple t) 
		{
			mLHSOverflow = t;
		}
	};
	
	private SetOverlowFunction mSetRHSOverflowFunction = new SetOverlowFunction() 
	{
		@Override
		public void SetOverflow(FactorTuple t) 
		{
			mRHSOverflow = t;
		}
	};
	
	private CompareToPrevTuple mComparePrevLHS = new CompareToPrevTuple() 
	{
		@Override
		public boolean CompareTuples(FactorTuple cur, FactorTuple prev) 
		{
			for (int i = 0; i < mMapping.size(); i++)
			{
				if (cur.get(mMapping.get(i).LHS).compareTo(prev.get(mMapping.get(i).LHS)) != 0)
					return false;
			}
			return true;
		}
	};
	
	private CompareToPrevTuple mComparePrevRHS = new CompareToPrevTuple() 
	{
		@Override
		public boolean CompareTuples(FactorTuple cur, FactorTuple prev) 
		{
			for (int i = 0; i < mMapping.size(); i++)
			{
				if (cur.get(mMapping.get(i).RHS).compareTo(prev.get(mMapping.get(i).RHS)) != 0)
					return false;
			}
			return true;
		}
	};
	
	private int CompareTuples(FactorTuple lhs, FactorTuple rhs)
    {
        for (int i = 0; i < mMapping.size(); i++)
        {
        	int c = lhs.get(mMapping.get(i).LHS).compareTo(rhs.get(mMapping.get(i).RHS));

            if (c != 0)
                return c;
        }
        return 0;
    }
	
	/*
     * Get a block from the left or right hand source 
     */ 
    private List<FactorTuple> GetFactorBlock(ITupleBuffer buffer, SetOverlowFunction fSetOverflow, GetOverflowFunction fGetOverflow, CompareToPrevTuple fComparer)
    {
        FactorTuple cur = null;
        FactorTuple overFlow = fGetOverflow.getOverflow();

        if (overFlow == null)
            cur = buffer.dequeueTuple();
        else
            cur = overFlow;

        if (cur == null)
            return null;

        List<FactorTuple> tuples = new ArrayList<FactorTuple>();
        tuples.add(cur);

        FactorTuple prev = cur;

        while ((cur = buffer.dequeueTuple()) != null)
        {
        	if (fComparer.CompareTuples(cur, prev))
            {
                tuples.add(cur);
                prev = cur;
            }
            else
            {
                break;
            }
        }

        fSetOverflow.SetOverflow(cur);
        return tuples;
    }
	
    private FactorTuple MergeTuples(FactorTuple lhs, FactorTuple rhs)
    {
        FactorTuple t = new FactorTuple();

        for (Factor f : lhs)
        {
            t.add(f);
        }

        for (Factor f : rhs)
        {
            t.add(f);
        }

        return t;
    }
    
    private void MergeAndSend(List<FactorTuple> lhs, List<FactorTuple> rhs)
    {
        for (FactorTuple ft1 : lhs)
        {
            for (FactorTuple ft2 : rhs)
            {
                this.mBuffer.enqueueTuple(MergeTuples(ft1, ft2));
            }
        }
    }
    
    @Override
    public void run() 
    {
    	super.run();
    	
    	//Current tuples

        boolean getLHS = true;
        boolean getRHS = true;

        List<FactorTuple> lhs = null;
        List<FactorTuple> rhs = null;
        
        while (!mStop)
        {
            if (getLHS)
                lhs = GetFactorBlock(mSource1.mBuffer, mSetLHSOverflowFunction, mGetLHSOverflowFunction, mComparePrevLHS);

            if (getRHS)
                rhs = GetFactorBlock(mSource2.mBuffer, mSetRHSOverflowFunction, mGetRHSOverflowFunction, mComparePrevRHS);

            //TODO: Outer joins?!?!
            
            if (lhs == null || rhs == null)
            {
                this.mBuffer.markDone();
                return;
            }

            int comp = CompareTuples(lhs.get(0), rhs.get(0));

            if (comp == 0)
            {
                getLHS = true;
                getRHS = true;
                MergeAndSend(lhs, rhs);
            }
            else if (comp < 0)
            {
                getLHS = true;
                getRHS = false;
            }
            else
            {
                getLHS = false;
                getRHS = true;
            }
        }
        
        this.mBuffer.markDone();
    }
    
}
