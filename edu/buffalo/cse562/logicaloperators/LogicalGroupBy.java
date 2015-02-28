package edu.buffalo.cse562.logicaloperators;

import java.util.List;

import net.sf.jsqlparser.schema.Column;

import edu.buffalo.cse562.relations.Schema;
import edu.buffalo.cse562.relations.SchemaColumn;
import edu.buffalo.cse562.reloperators.RelationalOperator;

public class LogicalGroupBy extends SingleSourceLogicalOperator 
{
	private List mGroupBy;
	
	public LogicalGroupBy(LogicalOperator source, List groupBy) 
	{
		super(source);
		mGroupBy = groupBy;
	}

	@Override
	public LogicalOperator copy() 
	{
		//We never actually modify group by criteria, so it's OK to copy the reference
		return new LogicalGroupBy(this.Source.copy(), this.mGroupBy);
	}

	@Override
	public RelationalOperator toPhysicalOperator() 
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void recomputeSchema() 
	{
		super.recomputeSchema();
		
		this.mCurSchema = new Schema("Unknown");

        for (Object o : mGroupBy)
        {
        	Column c = (Column)o;
        	
        	String table = "";
        	if (c.getTable() != null && c.getTable().getName() == null)
        	{
        		table = c.getTable().getName();
        	}
        	
            SchemaColumn sc = this.Source.getSchema().findColumn(table, c.getColumnName());
            mCurSchema.add(new SchemaColumn(sc));
        }
	}

}
