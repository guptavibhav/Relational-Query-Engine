package edu.buffalo.cse562.reloperators;

import java.util.ArrayList;
import java.util.List;

import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SelectItemVisitor;
import edu.buffalo.cse562.ExprTree.ExpressionTree;
import edu.buffalo.cse562.ExprTree.VariableNode;
import edu.buffalo.cse562.Factors.IFactor;
import edu.buffalo.cse562.buffers.ITupleBuffer;
import edu.buffalo.cse562.relations.ColumnDataTypes;
import edu.buffalo.cse562.relations.QuerySchemaVisitor;
import edu.buffalo.cse562.relations.SchemaColumn;
import edu.buffalo.cse562.relations.FactorTuple;
import edu.buffalo.cse562.relations.Schema;
import edu.buffalo.cse562.util.ExpressionTypeVisitor;

public class ProjectionOperator extends SingleSourceOperator implements SelectItemVisitor
{	
	/*
	 * Each IFactor object represents the source of one column in our schema
	 * In other words, there is a 1-1, in-order mapping from mMapping to our schema.
	 */
	private ArrayList<IFactor> mMapping;
	
	//Current context for evaluating expressions
	private QueryContext mContext;
	
    /*
     * This has the definitions for both our schema and how to compute the values.
     */ 
    private List<SelectItem> mSelectItems;
	
	public ProjectionOperator(RelationalOperator source, List<SelectItem> selectItems, VariableContext outerContext) 
	{
		super(source);
		
		mSelectItems = selectItems;
		mMapping = new ArrayList<IFactor>();
		
		//We link against our source's schema
		mContext = new QueryContext();
		mContext.CurrentContext = new VariableContext(new Schema(this.mSource.mSchema));
		mContext.OuterQueryContext = outerContext;
		
		setSchema();
	}
	
	/*
	 * The idea here is to populate the variables in the copy of the
	 * source schema, which our mapping is statically linked against,
	 * and evaluate each factor/column value 
	 */
	private void processFullTuple(FactorTuple tuple)
	{
		//First, populate the variables from the source into the copy of the source schema 
		mContext.CurrentContext.CurrentValues = tuple;
		
		FactorTuple t = new FactorTuple();
		
		//Next, evaluate each expression/column reference in the mapping
		for (int i = 0; i < mMapping.size(); i++)
		{
			t.add(mMapping.get(i).ToFactor());
		}
		
		mBuffer.enqueueTuple(t);
	}
	
	@Override
	public void run() 
	{
		super.run();
		
		//Set up the mapping to fill the result tuple
        BuildMapping();
        
		ITupleBuffer childBuffer = this.mSource.mBuffer;
		
		FactorTuple t;
		
		while (!mStop && (t = childBuffer.dequeueTuple()) != null)
		{
	 		processFullTuple(t);
 		}
		
		//Tell OUR parent that we're done
		mBuffer.markDone();
	}

	/*
	 * We actually need to do quite a bit of work here because our columns could consist of any
	 * combination of 3 things:
	 * 		
	 * 		1) Select All (*).  In this case we add all columns from the sourceSchema to our schema
	 * 		   and set a reference to the original column in the mapping
	 * 
	 * 		2) Select All Table (.*).  Same as above but with a filer on the table name
	 * 
	 * 		3) Individual Expression.  This would be much easier if it was just a variable,
	 * 		   but it can be anything.  So, we convert the expression (and link against the copied source schema)
	 * 		  and stuff that in the mapping.
	 * 
	 *  	The main idea is that we create a mapping from our schema to something that can be converted
	 *  	to a factor (i.e. implements IFactor).  This is either a variable or an expression, though the 
	 *  	former is just an instance of the latter...
	 *  
	 *  	*Also, we assume that aggregates have been converted to variables.
	 * 	
	 */
	@Override
	protected void setSchema() 
	{
		mSchema = QuerySchemaVisitor.getSchema(mSelectItems, this.mSource.getSchema());
	}

	//Unfortunately, we need to duplicate some of what the schema calculator does to build the map
    private void BuildMapping()
    {
    	for (SelectItem item : mSelectItems)
		{
			item.accept(this);
		}
    }
	
	@Override
	public void visit(AllColumns arg0) 
	{
		Schema source = mSource.getSchema();
		
		for (int i = 0; i < source.size(); i++)
		{
			mMapping.add(new VariableNode(mContext.CurrentContext, i));
		}	
	}

	@Override
	public void visit(AllTableColumns arg0) 
	{
		String table = arg0.getTable().getName();
		Schema source = mSource.getSchema();
		
		for (int i = 0; i < source.size(); i++)
		{
			SchemaColumn c = source.get(i);
			
			if (c.getTableName().equalsIgnoreCase(table))
			{
				mMapping.add(new VariableNode(mContext.CurrentContext, i));
			}
		}	
	}

	@Override
	public void visit(SelectExpressionItem arg0) 
	{
		mMapping.add(new ExpressionTree(arg0.getExpression(), mContext));	
	}

}
