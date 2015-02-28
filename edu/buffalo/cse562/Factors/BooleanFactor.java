package edu.buffalo.cse562.Factors;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import edu.buffalo.cse562.Exceptions.SQLTypeMismatchException;
import edu.buffalo.cse562.relations.Schema;

public class BooleanFactor extends Factor implements Comparable<Factor>
{
	private boolean mValue;
	
	public BooleanFactor(boolean value)
	{
		this.mValue = value;
	}
	
	public BooleanFactor(DataInputStream ds) throws IOException
	{
		this.mValue = ds.readBoolean();
	}
	
	public boolean getValue()
	{
		return mValue;
	}
	
	public void setValue(boolean value)
	{
		this.mValue = value;
	}
	
	@Override
	public String toString() 
	{
		return String.valueOf(mValue);
	}

	@Override
	public void toFile(DataOutputStream ds) throws IOException 
	{
		ds.writeInt(Schema.TYPE_BOOLEAN);
		ds.writeBoolean(this.mValue);
	}
	
	@Override
	public boolean equals(Object obj) 
	{
		if (obj instanceof BooleanFactor)
		{
			return this.mValue == ((BooleanFactor)obj).mValue;
		}
		else
		{
			return false;
		}
	}
	
	@Override
	public int hashCode() 
	{
		return new Boolean(mValue).hashCode();
	}
	
	@Override
	public int compareTo(Factor o) 
	{
		if (o instanceof BooleanFactor)
		{
			boolean otherval = ((BooleanFactor) o).getValue();
			
			if (otherval == mValue)
				return 0;
			else if (mValue && !otherval)
				return 1;
			else 
				return -1;
		}
		else
		{
			throw new SQLTypeMismatchException("BooleanFactors can only be compared to other BooleanFactors");
		}
	}
	
}
