package edu.buffalo.cse562.Factors;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import edu.buffalo.cse562.Exceptions.SQLTypeMismatchException;

public class ListFactor extends Factor 
{
	private ArrayList<Factor> mValues;
	
	public ListFactor() 
	{
		mValues = new ArrayList<Factor>();
	}
	
	public void add(Factor f)
	{
		mValues.add(f);
	}
	
	@Override
	public void toFile(DataOutputStream ds) throws IOException 
	{
		//This is only for intermediate results
		throw new RuntimeException("Cannot serialize lists");
	}
	
	public int size()
	{
		return mValues.size();
	}
	
	public Factor get(int index)
	{
		return mValues.get(index);
	}
	
	@Override
	public int compareTo(Factor o) 
	{
		throw new SQLTypeMismatchException("Cannot compare ListFactors");
	}
	
	@Override
	public String toString() 
	{
		if (mValues.size() == 1)
			return mValues.get(0).toString();
		else
			return "{LIST}";
	}

}
