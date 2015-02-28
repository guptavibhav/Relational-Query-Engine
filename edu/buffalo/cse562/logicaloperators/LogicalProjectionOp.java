package edu.buffalo.cse562.logicaloperators;

import java.util.List;

import net.sf.jsqlparser.statement.select.SelectItem;
import edu.buffalo.cse562.relations.QuerySchemaVisitor;
import edu.buffalo.cse562.reloperators.ProjectionOperator;
import edu.buffalo.cse562.reloperators.RelationalOperator;
import edu.buffalo.cse562.reloperators.VariableContext;

public class LogicalProjectionOp extends SingleSourceLogicalOperator 
{
	public List<SelectItem> SelectItems;
	public VariableContext OuterContext;
	
	public LogicalProjectionOp(LogicalOperator source, List<SelectItem> selectItems, VariableContext outer) 
	{
		super(source);
		
		SelectItems = selectItems;
		OuterContext = outer;
	}

	@Override
	public void recomputeSchema() 
	{
		super.recomputeSchema();
		
		mCurSchema = QuerySchemaVisitor.getSchema(this.SelectItems, this.Source.getSchema());
	}
	
	@Override
	public LogicalOperator copy() 
	{
		/*
		 * As long as the items in this list don't get changed, it's OK if they appear in
		 * multiple copies/projections.
		 * 
		 * If a subset is needed, add the existing items to a new list and leave this one alone.
		 * 
		 * If a superset is needed, start with an empty list and copy the references.
		 */
		return new LogicalProjectionOp(this.Source.copy(), SelectItems, OuterContext);
	}

	@Override
	public RelationalOperator toPhysicalOperator() 
	{
		
		return new ProjectionOperator(this.Source.toPhysicalOperator(), this.SelectItems, OuterContext);
	}

}
