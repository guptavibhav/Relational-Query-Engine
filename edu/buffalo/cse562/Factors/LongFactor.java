package edu.buffalo.cse562.Factors;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import edu.buffalo.cse562.Exceptions.SQLTypeMismatchException;
import edu.buffalo.cse562.relations.Schema;

public class LongFactor extends Factor 
{
	private long mValue;
	
	public LongFactor(long value)
	{
		this.mValue = value;
	}
	
	public LongFactor(DataInputStream ds) throws IOException
	{
		this.mValue = ds.readLong();
	}
	
	@Override
	public void toFile(DataOutputStream ds) throws IOException 
	{
		ds.writeInt(Schema.TYPE_LONG);
		ds.writeLong(this.mValue);
	}
	
	public long getValue()
	{
		return mValue;
	}
	
	public void setValue(long value)
	{
		this.mValue = value;
	}
	
	@Override
	public String toString() 
	{
		return String.valueOf(mValue);
	}

	@Override
	public boolean equals(Object obj) 
	{
		if (obj instanceof DoubleFactor)
			return this.mValue == ((DoubleFactor)obj).getValue();
		else if (obj instanceof LongFactor)
			return this.mValue == ((LongFactor)obj).getValue();
		else
			return false;
	}
	
	@Override
	public int hashCode() 
	{
		return new Long(mValue).hashCode();
	}
	
	@Override
	public int compareTo(Factor o) 
	{
		if (o instanceof LongFactor)
		{
			long otherVal = ((LongFactor) o).getValue();
			if (mValue > otherVal)
				return 1;
			else if (mValue < otherVal)
				return -1;
			else
				return 0;
		}
		else if (o instanceof DoubleFactor)
		{
			double otherVal = ((DoubleFactor) o).getValue();
			double thisVal = (double)mValue;
			
			if (thisVal > otherVal)
				return 1;
			else if (thisVal < otherVal)
				return -1;
			else
				return 0;
		}
		else if (o instanceof NullFactor)
		{
			return 1;
		}
		else
		{
			throw new SQLTypeMismatchException("Longs can only be compared to Doubles and Longs");
		}
	}
}
