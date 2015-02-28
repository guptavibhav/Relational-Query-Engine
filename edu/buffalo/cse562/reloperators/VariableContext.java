package edu.buffalo.cse562.reloperators;

import edu.buffalo.cse562.relations.FactorTuple;
import edu.buffalo.cse562.relations.Schema;

public class VariableContext 
{
	public Schema ContextSchema;
	public FactorTuple CurrentValues = null;
	
	public VariableContext(Schema s) 
	{
		ContextSchema = s;
	}

}
