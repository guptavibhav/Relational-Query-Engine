package edu.buffalo.cse562.logicaloperators;

import edu.buffalo.cse562.relations.Schema;
import edu.buffalo.cse562.reloperators.RelationalOperator;
import edu.buffalo.cse562.util.SortDefinitionList;

public abstract class LogicalOperator 
{
	/*
	 * We track the parent node so we can easily swap nodes
	 */
	public LogicalOperator Parent;
	
	/*
	 * Current schema of the operator.  This can change and will get updated
	 * when recomputeSchema() is invoked
	 */
	protected Schema mCurSchema;
	
	/* ABSTRACT FUNCTIONS */
	
	public abstract void recomputeSchema();
	public abstract void replaceChild(LogicalOperator oldChild, LogicalOperator newChild);
	public abstract LogicalOperator copy();
    public abstract RelationalOperator toPhysicalOperator();
    public abstract SortDefinitionList getSortOrder();
    
    public Schema getSchema()
    {
    	if (mCurSchema == null)
    		recomputeSchema();
    	
    	return this.mCurSchema;
    }
}
