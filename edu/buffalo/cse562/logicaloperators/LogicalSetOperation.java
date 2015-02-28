package edu.buffalo.cse562.logicaloperators;

import edu.buffalo.cse562.Main;
import edu.buffalo.cse562.relations.Schema;
import edu.buffalo.cse562.reloperators.RelationalOperator;
import edu.buffalo.cse562.reloperators.UnionOperator;

public class LogicalSetOperation extends DualSourceLogicalOperator 
{
	public enum SQLSetOperations
    {
        Union,
        Intersection,
        Except
    }
	
	public SQLSetOperations SetOperation;
	
	public LogicalSetOperation(LogicalOperator s1, LogicalOperator s2, SQLSetOperations type) 
	{
		super(s1, s2);
		
		this.SetOperation = type;
	}

	@Override
	public void recomputeSchema() 
	{
		super.recomputeSchema();
		
		//We always follow the first source's schema
		mCurSchema = new Schema(this.Source1.getSchema());
	}
	
	@Override
	public LogicalOperator copy() 
	{
		return new LogicalSetOperation(this.Source1, this.Source2, this.SetOperation);
	}

	@Override
	public RelationalOperator toPhysicalOperator() 
	{
		switch (this.SetOperation)
		{
		case Union:
			return new UnionOperator(this.Source1.toPhysicalOperator(), this.Source2.toPhysicalOperator());
		
		default:
			System.err.println("Unsupported set operation encountered");
			Main.onError();
			return null;
		}
	}

}
