package edu.buffalo.cse562.Factors;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import edu.buffalo.cse562.Exceptions.SQLTypeMismatchException;
import edu.buffalo.cse562.relations.Schema;

public class DoubleFactor extends Factor 
{
	private double mValue;
	
	public DoubleFactor(double value)
	{
		this.mValue = value;
	}
	
	public DoubleFactor(DataInputStream ds) throws IOException
	{
		this.mValue = ds.readDouble();
	}
	
	@Override
	public void toFile(DataOutputStream ds) throws IOException 
	{
		ds.writeInt(Schema.TYPE_DOUBLE);
		ds.writeDouble(this.mValue);
	}
	
	public double getValue()
	{
		return mValue;
	}
	
	public void setValue(double value)
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
			return this.mValue == ((DoubleFactor)obj).mValue;
		else if (obj instanceof LongFactor)
			return this.mValue == ((LongFactor)obj).getValue();
		else
			return false;
	}
	
	@Override
	public int hashCode() 
	{
		return new Double(this.mValue).hashCode();
	}
	
	@Override
	public int compareTo(Factor o) 
	{
		double otherVal = 0;
		
		if (o instanceof DoubleFactor)
		{
			otherVal = ((DoubleFactor) o).getValue();
		}
		else if (o instanceof LongFactor)
		{
			otherVal = ((LongFactor) o).getValue();
		}
		else if (o instanceof NullFactor)
		{
			return 1;
		}
		else
		{
			throw new SQLTypeMismatchException("Doubles can only be compared to Doubles and Longs");
		}
		
		if (mValue > otherVal)
			return 1;
		else if (mValue < otherVal)
			return -1;
		else
			return 0;
	}
}
