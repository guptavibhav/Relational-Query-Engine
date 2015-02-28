package edu.buffalo.cse562.conversion;

import java.util.ArrayList;
import java.util.List;

import edu.buffalo.cse562.Main;
import edu.buffalo.cse562.aggregates.AggregateFunctionList;
import edu.buffalo.cse562.logicaloperators.*;
import edu.buffalo.cse562.logicaloperators.LogicalSetOperation.SQLSetOperations;
import edu.buffalo.cse562.optimizer.PostOptimizer;
import edu.buffalo.cse562.optimizer.PreOptimizer;
import edu.buffalo.cse562.optimizer.ProjectionOptimizer;
import edu.buffalo.cse562.relations.Schema;
import edu.buffalo.cse562.relations.SchemaColumn;
import edu.buffalo.cse562.reloperators.VariableContext;
import edu.buffalo.cse562.util.SortDefinition;
import edu.buffalo.cse562.util.SortDefinitionList;
import edu.buffalo.cse562.util.UtilityFunctions;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.FromItemVisitor;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SubJoin;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.select.Union;

public class QueryToLogicalTree implements SelectVisitor
{

	/*
	 * Convert the list of joins to a single source
	 */
	private class FromListConverter implements FromItemVisitor
	{
        private PlainSelect mSelect;
        private LogicalOperator mCurTable;
        
        public FromListConverter(PlainSelect select)
        {
            mSelect = select;
        }

        public LogicalOperator convert()
        {
        	//Convert the first item
            mSelect.getFromItem().accept(this);

            //Every other item gets joined as a cross product.  We do this in case
            //we want a different join order.
			if (mSelect.getJoins() != null)
			{
				for (Object o : mSelect.getJoins())
	            {
	            	Join join = (Join)o;
	            	LogicalOperator lhs = mCurTable;
	            	join.getRightItem().accept(this);
	            	
	            	if (join.isFull() || join.isLeft() || join.isNatural() || join.isRight() || join.isOuter())
	            	{
	            		System.err.println("Encountered a join we can't handle: " + join.toString());
	            		Main.onError();
	            	}
	            	
	                join.getRightItem().accept(this);
	                mCurTable = new LogicalCrossProductOp(lhs, mCurTable);
	            }
			}

            return mCurTable;
        }
        
		@Override
		public void visit(Table arg0) 
		{
			mCurTable = new LogicalScanOp(arg0.getName());
			if (arg0.getAlias() != null && !arg0.getAlias().equals(""))
				mCurTable = new LogicalAliasOp(mCurTable, arg0.getAlias());
		}
		@Override
		public void visit(SubSelect arg0) 
		{
			//The outer context does not apply here as it's only for expressions
			mCurTable = new QueryToLogicalTree().convert(arg0.getSelectBody(), null);
            mCurTable = new LogicalAliasOp(mCurTable, arg0.getAlias());
		}
		
		@Override
		public void visit(SubJoin arg0) 
		{
			arg0.getLeft().accept(this);
            LogicalOperator lhs = mCurTable;
            
            Join join = arg0.getJoin();
            
            if (join.isFull() || join.isLeft() || join.isNatural() || join.isRight() || join.isOuter())
        	{
        		System.err.println("Encountered a sub-join we can't handle: " + join.toString());
        		Main.onError();
        	}
            
            join.getRightItem().accept(this);
            
            mCurTable = new LogicalJoinOp(lhs, mCurTable, join.getOnExpression());
		}
	}
	
	private LogicalOperator mRoot;
	private VariableContext mOuterContext;

    public LogicalOperator convert(SelectBody query, VariableContext outerContext)
    {
    	mOuterContext = outerContext;
        
    	query.accept(this);
        
        return mRoot;
    }
	
    /*
     * Convert the Order By clause to a SortDefinitionList
     */
    private SortDefinitionList getOrderByList(List orderByList)
	{
		SortDefinitionList list = new SortDefinitionList();
		
		for (Object o : orderByList)
		{
			OrderByElement elem = (OrderByElement)o;
			list.add(new SortDefinition(elem.getExpression(), elem.isAsc()));
		}
		
		return list;
	}
    
    private List<Column> getAllSortVariables(SortDefinitionList sortList)
    {
        List<Column> sortVariables = new ArrayList<Column>();

        for (SortDefinition def : sortList)
        {
            List<Column> temp = new VariableFinder().getColumnReferences(def.mExpression, null);
            
            if (temp.size() > 0)
                sortVariables.addAll(temp);
        }

        return sortVariables;
    }
    
    private LogicalOperator CreateSortedProjection(LogicalOperator source, PlainSelect select)
    {
        SortDefinitionList sortList = getOrderByList(select.getOrderByElements());
        List<Column> sortVariables = getAllSortVariables(sortList);

        //Temporary operator so we can get the schema
        LogicalProjectionOp projection = new LogicalProjectionOp(source, (List<SelectItem>)select.getSelectItems(), mOuterContext);
        Schema schema = projection.getSchema();

        //Make a copy for testing
        Schema tempSchema = new Schema(schema);

        boolean found = false;

        /*
         * For each sort variable, if it's not in the output schema, add it from the projection's input schema
         */
        for (Column v : sortVariables)
        {
        	String table = UtilityFunctions.getTableSafely(v);
        	
        	if (!tempSchema.contains(table, v.getColumnName()))
			{
        		found = true;
                tempSchema.add(new SchemaColumn(source.getSchema().findColumn(table, v.getColumnName())));
			}
        }

        if (!found)
        {
            //We can safely put the sort after the projection
        	LogicalOperator newRoot = new ProjectionOptimizer(projection, mOuterContext).Optimize();
            return new LogicalSortOperator(newRoot, sortList);

        	//return new LogicalSortOperator(projection, sortList);
        }
        else
        {
            //We need to do a projection->sort->projection
            //We'll build a new select statement for the new projection operator
            List<SelectItem> selectList = new ArrayList<SelectItem>();

            for (SchemaColumn sc : tempSchema)
            {
            	SelectExpressionItem expr = new SelectExpressionItem();
            	Column c = new Column();
            	Table t = new Table();
            	t.setName(sc.getTableName());
            	c.setTable(t);
            	c.setColumnName(sc.getName());
            	expr.setExpression(c);
            	
                selectList.add(expr);
            }

            //Inner Projection
            projection = new LogicalProjectionOp(source, selectList, mOuterContext);
            LogicalOperator newRoot = new ProjectionOptimizer(projection, mOuterContext).Optimize();
            
            //Sort
            LogicalSortOperator sortOp = new LogicalSortOperator(newRoot, sortList);
            //LogicalSortOperator sortOp = new LogicalSortOperator(projection, sortList);
            
            //Outer Projection (using original select)
            return new LogicalProjectionOp(sortOp, select.getSelectItems(), mOuterContext);
        }
    }
    
    /*
     * Get the list of aggregate functions used in the query.
     * Note: This has a major side effect:
     *  - Select expressions and the Having Clause will be rewritten to reflect the agg variable substitution
     */
    private AggregateFunctionList InitAggFunctionList(PlainSelect select)
    { 
        AggregateFunctionList aggFunctions = new AggregateFunctionList();

        for (int i = 0; i < select.getSelectItems().size(); i++)
        {
            if (select.getSelectItems().get(i) instanceof SelectExpressionItem)
            {
                SelectExpressionItem item = (SelectExpressionItem)select.getSelectItems().get(i);
                item.setExpression(AggregateFunctionConverter.convertExpression(aggFunctions, item.getExpression()));
            }
        }

        if (select.getHaving() != null)
		{
			select.setHaving(AggregateFunctionConverter.convertExpression(aggFunctions, select.getHaving()));
		}

        return aggFunctions;
    }
    
	@Override
	public void visit(PlainSelect select) 
	{
		//Let's take care of the sources first
        mRoot = new FromListConverter(select).convert();
        
        // *This rewrites the select node!
        AggregateFunctionList aggs = InitAggFunctionList(select);
        
        /*
         * We'll take care of pre-optimization here.  We could do it after we
         * have the whole tree constructed, but then we'd just have to pluck off
         * projection and aggregation anyways.
         */
        
        if (mRoot instanceof DualSourceLogicalOperator)
        {
        	mRoot = new PreOptimizer(mRoot, select.getWhere(), mOuterContext).optimize();
        }
        else if (select.getWhere() != null)
        {
        	/*
             * If it's single source, then just wrap it in the filter because that's the
             * best we can do.
             */
        	mRoot = new LogicalSelectionOperator(mRoot, select.getWhere(), mOuterContext);
        }

        /*
         * Aggregates
         */ 
        if (aggs.size() > 0)
        {
        	if (select.getGroupByColumnReferences() != null)
            {
                mRoot = new LogicalGroupByAgg(mRoot, aggs, select.getGroupByColumnReferences(), mOuterContext);
            }
            else
            {
                mRoot = new LogicalAggregateOp(mRoot, aggs, mOuterContext);
            }
        }
        else if (select.getGroupByColumnReferences() != null)
        {
            mRoot = new LogicalGroupBy(mRoot, select.getGroupByColumnReferences());
        }

        /*
         * Having Clause
         */ 
        if (select.getGroupByColumnReferences() != null && select.getHaving() != null)
        {
            mRoot = new LogicalSelectionOperator(mRoot, select.getHaving(), mOuterContext);
        }

        /*
         * Sorting and projection are a bit intertwinded b/c of SQL semantics.
         * We need might need to treat them separately.
         */

        if (select.getSelectItems().size() > 1 || !(select.getSelectItems().get(0) instanceof AllColumns))
        {
            if (select.getOrderByElements() == null)
            {
                mRoot = new LogicalProjectionOp(mRoot, select.getSelectItems(), mOuterContext);
                mRoot = new ProjectionOptimizer((LogicalProjectionOp)mRoot, mOuterContext).Optimize();
            }
            else
            {
                mRoot = CreateSortedProjection(mRoot, select);
            }
        }
        else if (select.getOrderByElements() != null)
        {
            //No actual projection is taking place, but we have sorting
            mRoot = new LogicalSortOperator(mRoot, getOrderByList(select.getOrderByElements()));
        }
        
        if (select.getLimit() != null)
        {
        	Limit l = (Limit)select.getLimit();
        	mRoot = new LogicalLimitOperator(mRoot, (int)l.getRowCount());
        }
        
        /*
         * Wrap this in LogicalQuery node so the optimizer can identify the start
         */ 
        mRoot = new LogicalQuery(mRoot);
        mRoot.recomputeSchema();
        new PostOptimizer((LogicalQuery)mRoot, mOuterContext).optimize();
	}

	@Override
	public void visit(Union arg0) 
	{
		mRoot = new QueryToLogicalTree().convert((PlainSelect)arg0.getPlainSelects().get(0), mOuterContext);
		
		for (int i = 1; i < arg0.getPlainSelects().size(); i++)
		{
			LogicalOperator temp = new QueryToLogicalTree().convert((PlainSelect)arg0.getPlainSelects().get(i), mOuterContext);
			mRoot = new LogicalSetOperation(mRoot, temp, SQLSetOperations.Union);
		}
		
		if (arg0.getOrderByElements() != null)
        {
			mRoot = new LogicalSortOperator(mRoot, getOrderByList(arg0.getOrderByElements()));
        }
		
		if (arg0.getLimit() != null)
        {
        	Limit l = (Limit)arg0.getLimit();
        	mRoot = new LogicalLimitOperator(mRoot, (int)l.getRowCount());
        }
		
		mRoot.recomputeSchema();
	}

}
