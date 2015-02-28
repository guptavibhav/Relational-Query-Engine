package edu.buffalo.cse562.logicaloperators;

import edu.buffalo.cse562.relations.Schema;
import edu.buffalo.cse562.reloperators.RelationalOperator;

/*
 * Outer wrapper for a query.  This indicates that this is the beginning of an 
 * SQL SELECT statement which is the basic unit of optimization.
 */ 
public class LogicalQuery extends SingleSourceLogicalOperator {

	public LogicalQuery(LogicalOperator source) 
	{
		super(source);
	}

	@Override
	public void recomputeSchema() 
	{
		super.recomputeSchema();
		
		mCurSchema = new Schema(this.Source.getSchema());
	}
	
	@Override
	public LogicalOperator copy() 
	{
		return new LogicalQuery(this.Source.copy());
	}

	@Override
	public RelationalOperator toPhysicalOperator() 
	{
		return this.Source.toPhysicalOperator();
	}

}
