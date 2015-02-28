package edu.buffalo.cse562.Factors;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import edu.buffalo.cse562.Exceptions.SQLTypeMismatchException;
import edu.buffalo.cse562.relations.Schema;

public class StringFactor extends Factor 
{
	private String mValue;
	
	public StringFactor(String value)
	{
		this.mValue = value;
	}
	
	public StringFactor(DataInputStream ds) throws IOException
	{
		this.mValue = ds.readUTF();
	}
	
	@Override
	public void toFile(DataOutputStream ds) throws IOException 
	{
		ds.writeInt(Schema.TYPE_STRING);
		ds.writeUTF(this.mValue);
	}
	
	public String getValue()
	{
		return mValue;
	}
	
	public void setValue(String value)
	{
		this.mValue = value;
	}
	
	@Override
	public String toString() 
	{
		return mValue;
	}

	@Override
	public boolean equals(Object obj) 
	{
		if (obj instanceof StringFactor)
			return this.mValue.equals(((StringFactor)obj).mValue);
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
		if (o instanceof StringFactor)
		{
			return mValue.compareTo(((StringFactor) o).getValue());
		}
		else if (o instanceof NullFactor)
		{
			return 1;
		}
		else
		{
			throw new SQLTypeMismatchException("Strings must be compared to Strings");
		}
		
	}
}
