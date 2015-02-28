package edu.buffalo.cse562.Factors;

import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.Time;

import edu.buffalo.cse562.Exceptions.SQLTypeMismatchException;

public class TimeFactor extends Factor 
{
	private Time mValue;
	
	public TimeFactor(Time value)
	{
		this.mValue = value;
	}
	
	public Time getValue()
	{
		return mValue;
	}
	
	public void setValue(Time value)
	{
		this.mValue = value;
	}
	
	@Override
	public void toFile(DataOutputStream ds) throws IOException 
	{
		throw new RuntimeException("Unsupported data type: TimeFactor");
	}
	
	@Override
	public String toString() 
	{
		return mValue.toString();
	}

	@Override
	public boolean equals(Object obj) 
	{
		if (obj instanceof TimeFactor)
			return this.mValue.equals(((TimeFactor)obj).mValue);
		else
			return false;
	}
	
	@Override
	public int hashCode() 
	{
		return mValue.hashCode();
	}
	
	@Override
	public int compareTo(Factor o) 
	{
		if (o instanceof TimeFactor)
		{
			return mValue.compareTo(((TimeFactor) o).getValue());
		}
		else if (o instanceof NullFactor)
		{
			return 1;
		}
		else
		{
			throw new SQLTypeMismatchException("Times must be compared to Times");
		}
	}
}
