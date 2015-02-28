package edu.buffalo.cse562.logicaloperators;

import edu.buffalo.cse562.relations.Schema;
import edu.buffalo.cse562.reloperators.InMemorySortOperator;
import edu.buffalo.cse562.reloperators.OutOfCoreSortOperator;
import edu.buffalo.cse562.reloperators.RelationalOperator;
import edu.buffalo.cse562.util.SortDefinitionList;
import edu.buffalo.cse562.util.SwapManager;

public class LogicalSortOperator extends SingleSourceLogicalOperator 
{
	public SortDefinitionList SortList;
	
	public LogicalSortOperator(LogicalOperator source, SortDefinitionList sortList) 
	{
		super(source);
		
		SortList = sortList;
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
		return new LogicalSortOperator(this.Source.copy(), this.SortList);
	}

	@Override
	public RelationalOperator toPhysicalOperator() 
	{
		if (SwapManager.SwapLocation.equals(""))
		{
			return new InMemorySortOperator(this.Source.toPhysicalOperator(), this.SortList);
		}
		else
		{
			return new OutOfCoreSortOperator(this.Source.toPhysicalOperator(), this.SortList);	
		}
	}
	
	@Override
	public SortDefinitionList getSortOrder() 
	{
		/*
		 * Who cares what the sort order was, we're changing it
		 * to our SortList
		 */
		return this.SortList;
	}

}
