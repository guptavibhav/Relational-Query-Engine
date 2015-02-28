package edu.buffalo.cse562.logicaloperators;

import java.util.List;

import net.sf.jsqlparser.schema.Column;
import edu.buffalo.cse562.aggregates.AggregateFunction;
import edu.buffalo.cse562.aggregates.AggregateFunctionList;
import edu.buffalo.cse562.aggregates.CountAggregate;
import edu.buffalo.cse562.aggregates.CountAllAggregate;
import edu.buffalo.cse562.relations.ColumnDataTypes;
import edu.buffalo.cse562.relations.Schema;
import edu.buffalo.cse562.relations.SchemaColumn;
import edu.buffalo.cse562.reloperators.InMemorySortOperator;
import edu.buffalo.cse562.reloperators.OutOfCoreSortOperator;
import edu.buffalo.cse562.reloperators.RelationalOperator;
import edu.buffalo.cse562.reloperators.SortedAggregateOperator;
import edu.buffalo.cse562.reloperators.VariableContext;
import edu.buffalo.cse562.util.ExpressionTypeVisitor;
import edu.buffalo.cse562.util.SortDefinition;
import edu.buffalo.cse562.util.SortDefinitionList;
import edu.buffalo.cse562.util.SwapManager;

public class LogicalGroupByAgg extends SingleSourceLogicalOperator 
{
	public AggregateFunctionList AggregatesFunctions;
    public List GroupBy;
    public VariableContext OuterContext;

    public LogicalGroupByAgg(LogicalOperator source, AggregateFunctionList aggs, List groupBy, VariableContext outer)
    {
    	super(source);
        this.AggregatesFunctions = aggs;
        this.GroupBy = groupBy;
        OuterContext = outer;
    }

    @Override
    public void recomputeSchema() 
    {
    	super.recomputeSchema();
    	
        //Aggregate Functions + Group By

        this.mCurSchema = new Schema("Unknown");

        for (Object o : this.GroupBy)
        {
        	Column c = (Column)o;
        	String table = "";
        	if (c.getTable() != null && c.getTable().getName() != null)
        		table = c.getTable().getName();
        	
            SchemaColumn srcCol = this.Source.getSchema().findColumn(table, c.getColumnName());
            this.mCurSchema.add(new SchemaColumn(srcCol));
        }

        for (AggregateFunction f : AggregatesFunctions)
        {
            ColumnDataTypes type = ColumnDataTypes.tpLong;

            if (!f.getAggFunctionName().equals(CountAggregate.FUNCTION_NAME) && !f.getAggFunctionName().equals(CountAllAggregate.FUNCTION_NAME))
                type = new ExpressionTypeVisitor(this.Source.getSchema(), f.getAggregatingExpression()).getExpressionType();

            mCurSchema.add(new SchemaColumn("", f.getThisVarName(), type));
        }

    }

    @Override
	public LogicalOperator copy() 
    {
        return new LogicalGroupByAgg(this.Source.copy(), this.AggregatesFunctions, this.GroupBy, OuterContext);
    }
    
    private SortDefinitionList convertGroupByToSortList()
    {
    	SortDefinitionList sdl = new SortDefinitionList();

        for (Object o : this.GroupBy)
        {
        	Column c = (Column)o;
            sdl.add(new SortDefinition(c, true));
        }
        
        return sdl;
    }
    
    @Override
	public RelationalOperator toPhysicalOperator()
    {
    	RelationalOperator source;
    	
    	if (SwapManager.SwapLocation.equals(""))
    		source = new InMemorySortOperator(this.Source.toPhysicalOperator(), convertGroupByToSortList());
    	else
    		source = new OutOfCoreSortOperator(this.Source.toPhysicalOperator(), convertGroupByToSortList());
        
    	return new SortedAggregateOperator(source, this.AggregatesFunctions, this.GroupBy, OuterContext);
    }

    @Override
    public SortDefinitionList getSortOrder() 
    {
    	/*
    	 * This needs to change if we implement a hash aggregate
    	 */
    	return convertGroupByToSortList();
    }

}
