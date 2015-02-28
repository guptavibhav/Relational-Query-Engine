package edu.buffalo.cse562.logicaloperators;

import edu.buffalo.cse562.reloperators.RelationalOperator;
import edu.buffalo.cse562.util.SortDefinitionList;

public abstract class SingleSourceLogicalOperator extends LogicalOperator 
{
	public LogicalOperator Source;
	
	public SingleSourceLogicalOperator(LogicalOperator source) 
	{
		this.Source = source;
		this.Source.Parent = this;
	}

	@Override
	public void recomputeSchema() 
	{
		this.Source.recomputeSchema();
	}

	@Override
	public void replaceChild(LogicalOperator oldChild, LogicalOperator newChild) 
	{
		if (this.Source == oldChild)
			this.Source = newChild;
		else
			throw new RuntimeException("Invalid child pointer in replace child");
	}

	@Override
	public abstract LogicalOperator copy();

	@Override
	public abstract RelationalOperator toPhysicalOperator();
	
	@Override
	public SortDefinitionList getSortOrder() 
	{
		return this.Source.getSortOrder();
	}

}
