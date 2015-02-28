package edu.buffalo.cse562.conversion;

import java.util.List;

import edu.buffalo.cse562.aggregates.AggregateFunctionList;
import edu.buffalo.cse562.reloperators.*;
import edu.buffalo.cse562.util.SortDefinition;
import edu.buffalo.cse562.util.SortDefinitionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.Union;

public class Checkpoint1Conversion implements SelectVisitor 
{
	private RelationalOperator mRoot;
	private VariableContext mOuterContext;
	
	public RelationalOperator convert(SelectBody select, VariableContext outerContext)
	{
		mOuterContext = outerContext;
		select.accept(this);
		return mRoot;
	}
	
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
	
	private AggregateFunctionList InitAggFunctionList(PlainSelect select)
	{
		AggregateFunctionList aggs = new AggregateFunctionList();
		
		for (int i = 0; i < select.getSelectItems().size(); i++)
		{
			SelectItem si = (SelectItem)select.getSelectItems().get(i);
			
			if (si instanceof SelectExpressionItem)
			{
				SelectExpressionItem item = (SelectExpressionItem)si;
				item.setExpression(AggregateFunctionConverter.convertExpression(aggs, item.getExpression()));
			}
		}
		
		if (select.getHaving() != null)
		{
			select.setHaving(AggregateFunctionConverter.convertExpression(aggs, select.getHaving()));
		}
		
		return aggs;
	}
	
	private SortDefinitionList getGroupByList(PlainSelect arg0)
	{
		SortDefinitionList list = new SortDefinitionList();
		
		for (Object o : arg0.getGroupByColumnReferences())
		{
			list.add(new SortDefinition((Column)o, true));
		}
		
		return list;
		
	}
	
	@Override
	public void visit(PlainSelect arg0) 
	{
		/*
		 * The order from top down is as follows:
		 * 
		 * 		(Union)
		 * 
		 * 		Limit			(LIMIT)
		 * 		Distinct?
		 * 		Projection 		(SELECT ... FROM)
		 * 		Sort			(ORDER BY)
		 * 		Selection   	(HAVING)
		 * 		Aggregation*	(Presence of agg function)		
		 * 		Selection		(WHERE)
		 *		Source			(FROM) - could be table, join, etc.
		 */
		
		//Let's see if there are any aggregates.
		//NOTE: This actually rewrites select expressions!
		AggregateFunctionList aggs = InitAggFunctionList(arg0);
		
		//Let's build this thing bottom-up...
		
		//Source node for this select
		RelationalOperator curSource = new Chkpt1SlowFromConverter().convert(arg0, mOuterContext);
		//RelationalOperator curSource = new Chkpt1FromConverter().convert(arg0);
		
		//Optional WHERE clause
		if (arg0.getWhere() != null)
		{
			curSource = new SelectionOperator(curSource, arg0.getWhere(), mOuterContext);
		}
		
		//If we have aggs, deal with them now
		if (aggs.size() > 0)
		{
			if (arg0.getGroupByColumnReferences() != null)
			{
				curSource = new InMemorySortOperator(curSource, getGroupByList(arg0));
				curSource = new SortedAggregateOperator(curSource, aggs, arg0.getGroupByColumnReferences(), mOuterContext);
			}
			else
			{
				curSource = new SortedAggregateOperator(curSource, aggs, null);
			}
			
			if (arg0.getHaving() != null)
			{
				curSource = new SelectionOperator(curSource, arg0.getHaving(), mOuterContext);
			}
		}
		
		List selItems = arg0.getSelectItems();
		
		//If there is only one select item and it's *, then we don't even need this operator
		if (selItems.size() > 1 || !(selItems.get(0) instanceof AllColumns))
		{
			curSource = new ProjectionOperator(curSource, selItems, null);
		}

		if (arg0.getOrderByElements() != null)
		{
			curSource = new InMemorySortOperator(curSource, getOrderByList(arg0.getOrderByElements()));
		}
		
		mRoot = curSource;

	}

	@Override
	public void visit(Union arg0) 
	{
		//TODO: Handle union all vs. distinct
		
		//Handle the first select specially
		PlainSelect select = (PlainSelect) arg0.getPlainSelects().get(0);
		visit(select);
		RelationalOperator curUnion = mRoot;
		
		for (int i = 1; i < arg0.getPlainSelects().size(); i++)
		{
			select = (PlainSelect) arg0.getPlainSelects().get(0);
			curUnion = new UnionOperator(curUnion, mRoot);
		}
		
		if (arg0.getOrderByElements() != null)
		{
			curUnion = new InMemorySortOperator(curUnion, getOrderByList(arg0.getOrderByElements()));
		}
		
		//TODO: Limit operator?
		
		mRoot = curUnion;
	}
}
