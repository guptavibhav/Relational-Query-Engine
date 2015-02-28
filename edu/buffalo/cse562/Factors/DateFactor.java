package edu.buffalo.cse562.Factors;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.Date;

import edu.buffalo.cse562.Exceptions.SQLTypeMismatchException;
import edu.buffalo.cse562.relations.Schema;

public class DateFactor extends Factor implements Comparable<Factor>
{
	private Date mValue;
	
	public DateFactor(Date value)
	{
		this.mValue = value;
	}
	
	public DateFactor(DataInputStream ds) throws IOException
	{
		this.mValue = new Date(ds.readLong());
	}
	
	public Date getValue()
	{
		return mValue;
	}
	
	public void setValue(Date value)
	{
		this.mValue = value;
	}
	
	@Override
	public String toString() 
	{
		return mValue.toString();
	}
	
	@Override
	public void toFile(DataOutputStream ds) throws IOException 
	{
		ds.writeInt(Schema.TYPE_DATE);
		ds.writeLong(this.mValue.getTime());
	}
	
	@Override
	public boolean equals(Object obj) 
	{
		if (obj instanceof DateFactor)
			return this.mValue.equals((DateFactor)obj);
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
		if (o instanceof DateFactor)
			return mValue.compareTo(((DateFactor) o).getValue());
		else if (o instanceof NullFactor)
			return 1;
		else
			throw new SQLTypeMismatchException("Dates can only be compared to other dates");
	}
}
