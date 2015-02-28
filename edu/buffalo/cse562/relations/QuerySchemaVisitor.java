package edu.buffalo.cse562.relations;

import java.util.List;

import edu.buffalo.cse562.Exceptions.SQLSymbolNotFoundException;
import edu.buffalo.cse562.aggregates.AggregateFunction;
import edu.buffalo.cse562.aggregates.AggregateFunctionList;
import edu.buffalo.cse562.aggregates.CountAggregate;
import edu.buffalo.cse562.aggregates.CountAllAggregate;
import edu.buffalo.cse562.util.ExpressionTypeVisitor;

import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.FromItemVisitor;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SelectItemVisitor;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SubJoin;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.select.Union;

public class QuerySchemaVisitor implements SelectVisitor 
{
	private static class SelectListSchemaVisitor implements SelectItemVisitor
	{
		private Schema mSchema;
        private Schema mSource;
        private List<SelectItem> mNodes;
		
        public SelectListSchemaVisitor(List<SelectItem> nodes, Schema source)
        {
            mNodes = nodes;
            mSchema = new Schema("Unknown");
            mSource = source;
        }
        
        public Schema ConvertToSchema()
        {
        	for (SelectItem node : mNodes)
        	{
        		node.accept(this);
        	}
        	
        	return mSchema;
        }
        
		@Override
		public void visit(AllColumns arg0) 
		{
			for (SchemaColumn sc : mSource)
            {
                SchemaColumn copy = new SchemaColumn(sc);
                mSchema.add(copy);
            }
		}

		@Override
		public void visit(AllTableColumns arg0) 
		{
			for (SchemaColumn sc : mSource)
            {
                if (arg0.getTable().getName().equalsIgnoreCase(sc.getTableName()))
                {
                    SchemaColumn copy = new SchemaColumn(sc);
                    mSchema.add(copy);
                }
            }
		}

		@Override
		public void visit(SelectExpressionItem arg0) 
		{
			ColumnDataTypes type = new ExpressionTypeVisitor(mSource, arg0.getExpression()).getExpressionType();

            SchemaColumn sc = null;

            if (arg0.getExpression() instanceof Column)
            {
                Column c = (Column)arg0.getExpression();

                SchemaColumn sourceCol = mSource.findColumn(c.getTable() == null ? "" : c.getTable().getName(), c.getColumnName());

                sc = new SchemaColumn(sourceCol);
            }
            else
            {
                sc = new SchemaColumn("", "?Column?", type);
            }

            if (arg0.getAlias() != null && !arg0.getAlias().equals(""))
                sc.setName(arg0.getAlias());

            mSchema.add(sc);
		}
		
	}
	
	private static class SourceSchemaCalculator implements FromItemVisitor
	{
        private Schema mSchema;
        private AggregateFunctionList mAggregates;
        
        public Schema getSchema(FromItem node, AggregateFunctionList aggs)
        {
            mAggregates = aggs;
            node.accept(this);
            return mSchema;
        }
        
		@Override
		public void visit(Table arg0) 
		{
			Schema s = TableManager.getInstance().getTable(arg0.getName());

            if (s == null)
                throw new SQLSymbolNotFoundException("Unable to find table: " + arg0.getName());

            mSchema = new Schema(s);

            if (arg0.getAlias() != null && !arg0.equals(""))
            {
                mSchema.setName(arg0.getAlias());
            }
		}

		@Override
		public void visit(SubSelect arg0) 
		{
			mSchema = new QuerySchemaVisitor(arg0.getSelectBody(), mAggregates).getSchema();
			
			if (arg0.getAlias() != null && !(arg0.getAlias().equals("")))
                mSchema.setName(arg0.getAlias());
		}

		@Override
		public void visit(SubJoin arg0) 
		{
			 Schema l = new SourceSchemaCalculator().getSchema(arg0.getLeft(), mAggregates);
             Schema r = new SourceSchemaCalculator().getSchema(arg0.getJoin().getRightItem(), mAggregates);

             mSchema = new Schema(l);
             
             for (SchemaColumn sc : r)
             {
                 mSchema.add(new SchemaColumn(sc));
             }
		}
		
	}
	
	private SelectBody mSelect;
    private Schema mSchema;
    private AggregateFunctionList mAggregates;
    
    public static Schema getSchema(SelectBody s, AggregateFunctionList aggs)
    {
        return new QuerySchemaVisitor(s, aggs).getSchema();
    }

    public static Schema getSchema(List<SelectItem> selectItems, Schema source)
    {
        return new QuerySchemaVisitor.SelectListSchemaVisitor(selectItems, source).ConvertToSchema();
    }
    
	private QuerySchemaVisitor(SelectBody select, AggregateFunctionList aggs)
	{
		mSelect = select;
		mAggregates = aggs;
	}
	
	private Schema getJoinSchema(PlainSelect select)
    {
        //There must be at least one source
        FromItem cur = select.getFromItem();
        
        Schema temp = new SourceSchemaCalculator().getSchema(cur, mAggregates);
        Schema joinSchema = new Schema(temp);
        
        if (select.getJoins() != null)
        {
	        for (int i = 0; i < select.getJoins().size(); i++)
	        {
	        	Join j = (Join)select.getJoins().get(i);
	        	temp = new SourceSchemaCalculator().getSchema(j.getRightItem(), mAggregates);
	        	
	        	for (SchemaColumn sc : temp)
	        	{
	        		joinSchema.add(new SchemaColumn(sc));
	        	}
	        	
	        }
        }

        //Add the aggregates to the schema
        if (mAggregates != null && mAggregates.size() > 0)
        {
            for (AggregateFunction a : mAggregates)
            {
                ColumnDataTypes type = ColumnDataTypes.tpLong;
                if (!a.getAggFunctionName().equals(CountAggregate.FUNCTION_NAME) && 
                		!a.getAggFunctionName().equals(CountAllAggregate.FUNCTION_NAME))
                {
                    type = new ExpressionTypeVisitor(temp, a.getAggregatingExpression()).getExpressionType();
                }
                
                temp.add(new SchemaColumn("", a.getThisVarName(), type));
            }
        }

        return temp;
    }
	
	public Schema getSchema()
	{
		mSelect.accept(this);
        return mSchema;
	}
	
	@Override
	public void visit(PlainSelect arg0) 
	{
		Schema source = getJoinSchema(arg0);
        mSchema = new SelectListSchemaVisitor((List<SelectItem>)arg0.getSelectItems(), source).ConvertToSchema();
	}

	@Override
	public void visit(Union arg0) 
	{
		PlainSelect select = (PlainSelect)arg0.getPlainSelects().get(0);
		select.accept(this);
	}

}
