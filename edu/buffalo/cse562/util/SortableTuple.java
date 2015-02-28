package edu.buffalo.cse562.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import edu.buffalo.cse562.Factors.Factor;
import edu.buffalo.cse562.relations.ColumnDataTypes;
import edu.buffalo.cse562.relations.FactorTuple;
import edu.buffalo.cse562.relations.Schema;

public class SortableTuple implements Comparable<SortableTuple>, Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private List<SortFactor> mSortKeys;
	
	//Public to avoid a function call
	public FactorTuple mData;
	
	public SortableTuple(List<SortFactor> sortKeys, FactorTuple data)
	{
		mSortKeys = sortKeys;
		mData = data;
	}
	
	public void toFile(DataOutputStream ds) throws IOException
	{
		for (SortFactor sf : mSortKeys)
		{
			sf.toFile(ds);
		}
		
		mData.toFile(ds);
	}
	
	public SortableTuple(DataInputStream ds, int dataCount, int keyCount) throws IOException
	{
		mSortKeys = new ArrayList<SortFactor>();
		
		for (int i = 0; i < keyCount; i++)
		{
			mSortKeys.add(new SortFactor(ds));
		}
		
		mData = new FactorTuple();
		
		for (int i = 0; i < dataCount; i++)
		{
			mData.add(Factor.createFromFile(ds));
		}
	}
	
	@Override
	public int compareTo(SortableTuple o) 
	{
		for (int i = 0; i < mSortKeys.size(); i++)
		{
			int res = mSortKeys.get(i).Value.compareTo(o.mSortKeys.get(i).Value);
			
			if (res < 0)
			{
				//This < other
				
				if (mSortKeys.get(i).Asc)
					return -1;
				else
					return 1;
			}
			else if (res > 0)
			{
				//This > other
				if (mSortKeys.get(i).Asc)
					return 1;
				else
					return -1;
			}
		}
		
		return 0;
	}
}
