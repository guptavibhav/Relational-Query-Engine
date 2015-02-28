package edu.buffalo.cse562.logicaloperators;

import java.util.List;

import edu.buffalo.cse562.relations.Schema;
import edu.buffalo.cse562.relations.SchemaColumn;
import edu.buffalo.cse562.relations.TableManager;
import edu.buffalo.cse562.reloperators.RelationalOperator;
import edu.buffalo.cse562.reloperators.SimpleTableOperator;
import edu.buffalo.cse562.util.SortDefinitionList;

public class LogicalScanOp extends LogicalOperator 
{
	public String TableName;
	public List<Integer> Indexes;
	
	private Schema mOrigSchema = null;
	
	public LogicalScanOp(String table) 
	{
		this.TableName = table;
		Indexes = null;
	}

	@Override
	public void recomputeSchema() 
	{
		this.mCurSchema = TableManager.getInstance().getTable(this.TableName);
		this.mOrigSchema = mCurSchema;
		
		if (Indexes != null)
		{
			this.mCurSchema = new Schema();
			this.mCurSchema.setName(mOrigSchema.getName());
			
			for (int i = 0; i < Indexes.size(); i++)
			{
				mCurSchema.add(new SchemaColumn(mOrigSchema.get(Indexes.get(i))));
			}
		}
	}

	@Override
	public void replaceChild(LogicalOperator oldChild, LogicalOperator newChild) 
	{
		//We don't have children!
		throw new RuntimeException("Cannot replace child of a scan operator - no children!");
	}

	@Override
	public LogicalOperator copy() 
	{
		return new LogicalScanOp(this.TableName);
	}

	@Override
	public RelationalOperator toPhysicalOperator() 
	{
		return new SimpleTableOperator(this.getSchema());
	}

	@Override
	public SortDefinitionList getSortOrder() 
	{
		//We don't have a sort order
		return null;
	}

}
