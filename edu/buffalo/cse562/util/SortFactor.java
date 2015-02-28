package edu.buffalo.cse562.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;

import edu.buffalo.cse562.Factors.Factor;

public class SortFactor implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public Factor Value;
	public boolean Asc = true;
	
	public SortFactor(Factor f, boolean isAsc)
	{
		Value = f;
		Asc = isAsc;
	}
	
	public SortFactor(DataInputStream ds) throws IOException
	{
		this.Asc = ds.readBoolean();
		this.Value = Factor.createFromFile(ds);
	}
	
	public void toFile(DataOutputStream ds) throws IOException
	{
		ds.writeBoolean(this.Asc);
		this.Value.toFile(ds);
	}
}
