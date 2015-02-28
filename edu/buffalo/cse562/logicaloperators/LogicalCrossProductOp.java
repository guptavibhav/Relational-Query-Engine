package edu.buffalo.cse562.logicaloperators;

import edu.buffalo.cse562.relations.Schema;
import edu.buffalo.cse562.relations.SchemaColumn;
import edu.buffalo.cse562.reloperators.NestedLoopsJoin;
import edu.buffalo.cse562.reloperators.RelationalOperator;

public class LogicalCrossProductOp extends DualSourceLogicalOperator 
{
	
	public LogicalCrossProductOp(LogicalOperator s1, LogicalOperator s2) 
	{
		super(s1, s2);
	}

	@Override
	public LogicalOperator copy() 
	{
		return new LogicalCrossProductOp(this.Source1.copy(), this.Source2.copy());
	}

	@Override
	public RelationalOperator toPhysicalOperator() 
	{
		return new NestedLoopsJoin(this.Source1.toPhysicalOperator(), this.Source2.toPhysicalOperator());
	}

	@Override
	public void recomputeSchema() 
	{
		super.recomputeSchema();
		
		this.mCurSchema = new Schema("");

        for (SchemaColumn sc : this.Source1.getSchema())
        {
            this.mCurSchema.add(new SchemaColumn(sc));
        }

        for (SchemaColumn sc : this.Source2.getSchema())
        {
            this.mCurSchema.add(new SchemaColumn(sc));
        }
	}
}
