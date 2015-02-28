package edu.buffalo.cse562.Factors;

import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.Timestamp;

import edu.buffalo.cse562.Exceptions.SQLTypeMismatchException;

public class TimestampFactor extends Factor 
{
	private Timestamp mValue;
	
	public TimestampFactor(Timestamp value)
	{
		this.mValue = value;
	}
	
	public Timestamp getValue()
	{
		return mValue;
	}
	
	public void setValue(Timestamp value)
	{
		this.mValue = value;
	}
	
	@Override
	public boolean equals(Object obj) 
	{
		if (obj instanceof TimestampFactor)
			return this.mValue.equals(((TimestampFactor)obj).mValue);
		else
			return false;
	}
	
	@Override
	public void toFile(DataOutputStream ds) throws IOException 
	{
		throw new RuntimeException("Unsupported data type: TimeStampFactor");
	}
	
	@Override
	public int hashCode() 
	{
		return mValue.hashCode();
	}
	
	@Override
	public String toString() 
	{
		return mValue.toString();
	}

	@Override
	public int compareTo(Factor o) 
	{
		if (o instanceof TimestampFactor)
		{
			return mValue.compareTo(((TimestampFactor) o).getValue());
		}
		else if (o instanceof NullFactor)
		{
			return 1;
		}
		else
		{
			throw new SQLTypeMismatchException("Timestamps must be compared to Timestamps");
		}
	}
}
