package edu.buffalo.cse562.reloperators;

import java.util.ArrayList;
import java.util.List;

import edu.buffalo.cse562.relations.*;

/*
 * We're assuming Inner Join here
 */
public class EquiJoinOperator extends DualSourceOperator 
{
	protected List<EquiJoinMapping> mMapping;
    
	public EquiJoinOperator(RelationalOperator ra1, RelationalOperator ra2, List<EquiJoinMapping> mapping) 
	{
		super(ra1, ra2);
		mMapping = mapping;
	}

	@Override
	protected void setSchema() 
	{
		//Our schema as the join of the 2 schemas

        mSchema = new Schema("Join");

        for (SchemaColumn sc : mSource1.getSchema())
        {
            mSchema.add(new SchemaColumn(sc));
        }

        for (SchemaColumn sc : mSource2.getSchema())
        {
            mSchema.add(new SchemaColumn(sc));
        }
	}

}
