package edu.buffalo.cse562.relations;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import edu.buffalo.cse562.Factors.Factor;

public class FactorTuple extends ArrayList<Factor> 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public void toFile(DataOutputStream ds) throws IOException
	{
		for (int i = 0; i < this.size(); i++)
		{
			this.get(i).toFile(ds);
		}
	}
}
