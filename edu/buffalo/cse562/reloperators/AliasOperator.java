package edu.buffalo.cse562.reloperators;

import edu.buffalo.cse562.relations.FactorTuple;
import edu.buffalo.cse562.relations.SchemaColumn;
import edu.buffalo.cse562.relations.Schema;

/**
 * 
 * In trying to keep a little of the relational logic separate from
 * SQL additions, this operator wraps any relation that can be aliased.
 * For example, tables, sub-selects, and certain joins come to mind.
 *
 */
public class AliasOperator extends SingleSourceOperator
{
	private String mAlias = "";
	
	public AliasOperator(RelationalOperator source, String alias) 
	{
		super(source);
		mAlias = alias;
		
		//Link our buffer directly to our source's buffer BEFORE we start running.
		this.mBuffer = mSource.mBuffer;
		
		setSchema();
	}

	@Override
	public void run() 
	{
		super.run();
	}

	@Override
	protected void setSchema() 
	{
		//Our schema is the same as our source, but with a different table name
		//applied to each column
		
		mSchema = new Schema(mSource.mSchema);		//Copy source schema
		mSchema.setName(mAlias);					//This handles columns too
	}

}
