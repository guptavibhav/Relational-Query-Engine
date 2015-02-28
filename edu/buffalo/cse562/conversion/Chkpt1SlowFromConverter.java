package edu.buffalo.cse562.conversion;

import java.util.List;

import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.FromItemVisitor;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SubJoin;
import net.sf.jsqlparser.statement.select.SubSelect;
import edu.buffalo.cse562.Main;
import edu.buffalo.cse562.relations.Schema;
import edu.buffalo.cse562.relations.TableManager;
import edu.buffalo.cse562.reloperators.AliasOperator;
import edu.buffalo.cse562.reloperators.RelationalOperator;
import edu.buffalo.cse562.reloperators.SelectionOperator;
import edu.buffalo.cse562.reloperators.NestedLoopsJoin;
import edu.buffalo.cse562.reloperators.SimpleTableOperator;
import edu.buffalo.cse562.reloperators.VariableContext;

/*
 * Convert all FROM items into a relational operator.  This might end up being an entire
 * tree depending on what's in the FROM clause.  
 */
public class Chkpt1SlowFromConverter implements FromItemVisitor
{
	private RelationalOperator mFromRoot;
	private VariableContext mOuterContext;
	
	public RelationalOperator convert(PlainSelect select, VariableContext outerContext)
	{
		mOuterContext = outerContext;
		
		select.getFromItem().accept(this);
		
		List joins = select.getJoins();
		
		if (joins != null)
		{
			for (Object o : joins)
			{				
				RelationalOperator lhs = mFromRoot;
				Join j = (Join)o;
				j.getRightItem().accept(this);
				mFromRoot = new NestedLoopsJoin(lhs, mFromRoot);
				if (j.getOnExpression() != null)
				{
					mFromRoot = new SelectionOperator(mFromRoot, j.getOnExpression(), null);
				}
			}	
		}
		
		return mFromRoot;
	}
	
	@Override
	public void visit(Table arg0) 
	{
		Schema s = TableManager.getInstance().getTable(arg0.getName());
		SimpleTableOperator op = new SimpleTableOperator(s);
		
		if (arg0.getAlias() != null && !arg0.getAlias().equals(""))
		{
			AliasOperator aliasOp = new AliasOperator(op, arg0.getAlias());
			mFromRoot = aliasOp;
		}
		else
		{
			mFromRoot = op;
		}
	}

	@Override
	public void visit(SubSelect arg0) 
	{
		Checkpoint1Conversion subSelConverter = new Checkpoint1Conversion();
		mFromRoot = subSelConverter.convert(arg0.getSelectBody(), mOuterContext);
		if (arg0.getAlias() != null)
		{
			mFromRoot = new AliasOperator(mFromRoot, arg0.getAlias());
		}
	}

	@Override
	public void visit(SubJoin arg0) 
	{
		//TODO: Handle sub-joins (checkpoint 2?)
		System.err.println("SubJoins are not implemented yet");
		Main.onError();
	}
}
