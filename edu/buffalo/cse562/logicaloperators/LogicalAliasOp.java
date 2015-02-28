package edu.buffalo.cse562.logicaloperators;

import edu.buffalo.cse562.relations.Schema;
import edu.buffalo.cse562.reloperators.AliasOperator;
import edu.buffalo.cse562.reloperators.RelationalOperator;

public class LogicalAliasOp extends SingleSourceLogicalOperator 
{

	public String Alias;
	
	public LogicalAliasOp(LogicalOperator source, String alias) 
	{
		super(source);
		Alias = alias;
	}

	@Override
	public LogicalOperator copy() 
	{
		return new LogicalAliasOp(this.Source.copy(), this.Alias);
	}

	@Override
	public RelationalOperator toPhysicalOperator() 
	{
		return new AliasOperator(this.Source.toPhysicalOperator(), this.Alias);
	}
	
	@Override
	public void recomputeSchema() 
	{
		super.recomputeSchema();
		
		mCurSchema = new Schema(this.Source.getSchema());
		mCurSchema.setName(this.Alias);
	}

}
