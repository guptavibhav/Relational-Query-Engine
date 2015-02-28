package edu.buffalo.cse562.logicaloperators;

import edu.buffalo.cse562.relations.Schema;
import edu.buffalo.cse562.reloperators.LimitOperator;
import edu.buffalo.cse562.reloperators.RelationalOperator;

public class LogicalLimitOperator extends SingleSourceLogicalOperator 
{
	public int Limit;
	
	public LogicalLimitOperator(LogicalOperator source, int limit) 
	{
		super(source);
		Limit = limit;
	}

	@Override
	public void recomputeSchema() 
	{
		super.recomputeSchema();
		
		this.mCurSchema = new Schema(this.Source.getSchema());
	}
	
	@Override
	public LogicalOperator copy() 
	{
		return new LogicalLimitOperator(this.Source.copy(), this.Limit);
	}

	@Override
	public RelationalOperator toPhysicalOperator() 
	{
		return new LimitOperator(this.Source.toPhysicalOperator(), this.Limit);
	}

}
