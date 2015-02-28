package edu.buffalo.cse562.Factors;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import edu.buffalo.cse562.relations.Schema;

public class NullFactor extends Factor 
{
	private static final String NULL_STRING = "NULL";
	
	public NullFactor()
	{
	}
		
	@Override
	public String toString() 
	{
		return NULL_STRING;
	}
	
	public NullFactor(DataInputStream ds) throws IOException
	{
		ds.readInt();
	}
	
	@Override
	public void toFile(DataOutputStream ds) throws IOException 
	{
		//Place holder
		ds.writeInt(Schema.TYPE_NULL);
		ds.writeInt(0);
	}
	
	@Override
	public boolean equals(Object obj) 
	{
		if (obj instanceof NullFactor)
			return true;
		else
			return false;
	}
	
	@Override
	public int hashCode() 
	{
		//Somewhat small prime number
		return 7193;
	}

	@Override
	public int compareTo(Factor o) 
	{
		//TODO: Is this the behavior we want???
		return -1;
	}
}
