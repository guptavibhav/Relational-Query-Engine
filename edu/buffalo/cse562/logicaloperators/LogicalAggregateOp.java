package edu.buffalo.cse562.logicaloperators;

import edu.buffalo.cse562.aggregates.*;
import edu.buffalo.cse562.relations.ColumnDataTypes;
import edu.buffalo.cse562.relations.Schema;
import edu.buffalo.cse562.relations.SchemaColumn;
import edu.buffalo.cse562.reloperators.RelationalOperator;
import edu.buffalo.cse562.reloperators.SortedAggregateOperator;
import edu.buffalo.cse562.reloperators.VariableContext;
import edu.buffalo.cse562.util.ExpressionTypeVisitor;

public class LogicalAggregateOp extends SingleSourceLogicalOperator 
{
	public AggregateFunctionList AggregateFunctions;
	public VariableContext OuterContext;
	
	public LogicalAggregateOp(LogicalOperator source, AggregateFunctionList aggs, VariableContext context) 
	{
		super(source);
		AggregateFunctions = aggs;
		OuterContext = context;
	}

	@Override
	public void recomputeSchema() 
	{
		super.recomputeSchema();
		
		//Only what is in our aggregate function list!

        this.mCurSchema = new Schema("");

        for (AggregateFunction f : AggregateFunctions)
        {
            ColumnDataTypes type = ColumnDataTypes.tpLong;

            if (f.getAggFunctionName() != CountAggregate.FUNCTION_NAME && f.getAggFunctionName() != CountAllAggregate.FUNCTION_NAME)
                type = new ExpressionTypeVisitor(this.Source.getSchema(), f.getAggregatingExpression()).getExpressionType();
            
            mCurSchema.add(new SchemaColumn("", f.getThisVarName(), type));
        }
	}
	
	@Override
	public LogicalOperator copy() 
	{
		/*
         * It's OK to just copy the aggregate functions since we never change them...
         */ 
        return new LogicalAggregateOp(this.Source.copy(), this.AggregateFunctions, OuterContext);
	}

	@Override
	public RelationalOperator toPhysicalOperator() 
	{
		return new SortedAggregateOperator(this.Source.toPhysicalOperator(), this.AggregateFunctions, OuterContext);
	}

}
