package edu.buffalo.cse562.logicaloperators;

import edu.buffalo.cse562.reloperators.RelationalOperator;
import edu.buffalo.cse562.util.SortDefinitionList;

public abstract class DualSourceLogicalOperator extends LogicalOperator 
{
	public LogicalOperator Source1;
    public LogicalOperator Source2;
    
    public DualSourceLogicalOperator(LogicalOperator s1, LogicalOperator s2)
    {
        this.Source1 = s1;
        this.Source2 = s2;
        this.Source1.Parent = this;
        this.Source2.Parent = this;
    }

	@Override
	public void recomputeSchema() 
	{
		this.Source1.recomputeSchema();
        this.Source2.recomputeSchema();
	}

	@Override
	public void replaceChild(LogicalOperator oldChild, LogicalOperator newChild) 
	{
		if (this.Source1 == oldChild)
            this.Source1 = newChild;
        else if (this.Source2 == oldChild)
            this.Source2 = newChild;
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
		SortDefinitionList lhs = Source1.getSortOrder();
		if (lhs == null)
			return null;
		
		SortDefinitionList rhs = Source2.getSortOrder();
		if (rhs == null)
			return null;
		
		if (lhs.isEqualTo(rhs))
			return lhs;
		else
			return null;
		
	}
}
