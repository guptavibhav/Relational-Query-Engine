package edu.buffalo.cse562.logicaloperators;

import net.sf.jsqlparser.expression.Expression;
import edu.buffalo.cse562.relations.Schema;
import edu.buffalo.cse562.reloperators.RelationalOperator;
import edu.buffalo.cse562.reloperators.SelectionOperator;
import edu.buffalo.cse562.reloperators.VariableContext;

public class LogicalSelectionOperator extends SingleSourceLogicalOperator 
{
	public Expression Condition;
	public VariableContext OuterContext;
	
	
	public LogicalSelectionOperator(LogicalOperator source, Expression condition, VariableContext context) 
	{
		super(source);
		
		this.Condition = condition;
		this.OuterContext = context;
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
		return new LogicalSelectionOperator(this.Source.copy(), this.Condition, this.OuterContext);
	}

	@Override
	public RelationalOperator toPhysicalOperator() 
	{
		return new SelectionOperator(this.Source.toPhysicalOperator(), this.Condition, this.OuterContext);
	}

}
