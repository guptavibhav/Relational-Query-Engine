package edu.buffalo.cse562.conversion;

import java.util.ArrayList;

import edu.buffalo.cse562.relations.Schema;
import edu.buffalo.cse562.reloperators.VariableContext;
import edu.buffalo.cse562.util.UtilityFunctions;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.conditional.*;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.select.Union;

public class VariableFinder implements ExpressionVisitor 
{
	private ArrayList<Column> mVariableRefs;
	private Schema mSchema;
	private boolean mCheckSchema = false;
	
	public ArrayList<Column> getColumnReferences(Expression e, Schema schema)
	{
		mVariableRefs = new ArrayList<Column>();
		mSchema = schema;
		mCheckSchema = false;
		e.accept(this);
		return mVariableRefs;
	}
	
	public ArrayList<Column> getColumnsFrom(Expression e, Schema schema)
	{
		mVariableRefs = new ArrayList<Column>();
		mCheckSchema = true;
		mSchema = schema;
		e.accept(this);
		return mVariableRefs;
	}
	
	@Override
	public void visit(NullValue arg0) { }

	@Override
	public void visit(Function arg0) 
	{ 
		for (Object o : arg0.getParameters().getExpressions())
		{
			Expression e = (Expression)o;
			e.accept(this);
		}
	}

	@Override
	public void visit(InverseExpression arg0) 
	{ 
		arg0.getExpression().accept(this);
	}

	@Override
	public void visit(JdbcParameter arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(DoubleValue arg0) { }

	@Override
	public void visit(LongValue arg0) { }

	@Override
	public void visit(DateValue arg0) { }

	@Override
	public void visit(TimeValue arg0) { }

	@Override
	public void visit(TimestampValue arg0) { }

	@Override
	public void visit(Parenthesis arg0) 
	{
		arg0.getExpression().accept(this);
	}

	@Override
	public void visit(StringValue arg0) { }

	@Override
	public void visit(Addition arg0) 
	{ 
		arg0.getLeftExpression().accept(this);
		arg0.getRightExpression().accept(this);
	}

	@Override
	public void visit(Division arg0) 
	{
		arg0.getLeftExpression().accept(this);
		arg0.getRightExpression().accept(this);	
	}

	@Override
	public void visit(Multiplication arg0) 
	{
		arg0.getLeftExpression().accept(this);
		arg0.getRightExpression().accept(this);
	}

	@Override
	public void visit(Subtraction arg0) 
	{
		arg0.getLeftExpression().accept(this);
		arg0.getRightExpression().accept(this);	
	}

	@Override
	public void visit(AndExpression arg0) 
	{
		arg0.getLeftExpression().accept(this);
		arg0.getRightExpression().accept(this);	
	}

	@Override
	public void visit(OrExpression arg0) 
	{
		arg0.getLeftExpression().accept(this);
		arg0.getRightExpression().accept(this);	
	}

	@Override
	public void visit(Between arg0) 
	{
		arg0.getLeftExpression().accept(this);
		arg0.getBetweenExpressionStart().accept(this);
		arg0.getBetweenExpressionEnd().accept(this);
	}

	@Override
	public void visit(EqualsTo arg0) 
	{
		arg0.getLeftExpression().accept(this);
		arg0.getRightExpression().accept(this);	
	}

	@Override
	public void visit(GreaterThan arg0) 
	{
		arg0.getLeftExpression().accept(this);
		arg0.getRightExpression().accept(this);
	}

	@Override
	public void visit(GreaterThanEquals arg0) 
	{
		arg0.getLeftExpression().accept(this);
		arg0.getRightExpression().accept(this);	
	}

	@Override
	public void visit(InExpression arg0) 
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(IsNullExpression arg0) 
	{
		arg0.getLeftExpression().accept(this);
	}

	@Override
	public void visit(LikeExpression arg0) 
	{
		arg0.getLeftExpression().accept(this);
		arg0.getRightExpression().accept(this);	
	}

	@Override
	public void visit(MinorThan arg0) 
	{
		arg0.getLeftExpression().accept(this);
		arg0.getRightExpression().accept(this);	
	}

	@Override
	public void visit(MinorThanEquals arg0) 
	{
		arg0.getLeftExpression().accept(this);
		arg0.getRightExpression().accept(this);	
	}

	@Override
	public void visit(NotEqualsTo arg0) 
	{
		arg0.getLeftExpression().accept(this);
		arg0.getRightExpression().accept(this);	
	}

	@Override
	public void visit(Column arg0) 
	{
		if (mCheckSchema)
		{
			if (mSchema != null)
			{
				if (mSchema.contains(UtilityFunctions.getTableSafely(arg0), arg0.getColumnName()))
				{
					mVariableRefs.add(arg0);	
				}
			}
		}
		else
		{
			mVariableRefs.add(arg0);
		}
	}

	private void checkSelectExpr(Expression expr)
	{
		if (mSchema == null)
			return;
		
		ArrayList<Column> vars = new VariableFinder().getColumnsFrom(expr, mSchema);
		if (vars.size() > 0)
		{
			this.mVariableRefs.addAll(vars);
		}
	}
	
	private void processSubSelect(PlainSelect select)
	{
		for (Object o : select.getSelectItems())
		{
			if (o instanceof SelectExpressionItem)
			{
				SelectExpressionItem item = (SelectExpressionItem)o;
				checkSelectExpr(item.getExpression());
			}
		}
		
		if (select.getWhere() != null)
			checkSelectExpr(select.getWhere());
		
		if (select.getHaving() != null)
			checkSelectExpr(select.getHaving());
	}
	
	@Override
	public void visit(SubSelect arg0) 
	{
		if (arg0.getSelectBody() instanceof PlainSelect)
		{
			processSubSelect((PlainSelect)arg0.getSelectBody());
		}
		else if (arg0.getSelectBody() instanceof Union)
		{
 			Union union = (Union)arg0.getSelectBody();
			for (Object o : union.getPlainSelects())
			{
				processSubSelect((PlainSelect)o);
			}
		}
	}

	@Override
	public void visit(CaseExpression arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(WhenClause arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ExistsExpression arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(AllComparisonExpression arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(AnyComparisonExpression arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Concat arg0) 
	{
		arg0.getLeftExpression().accept(this);
		arg0.getRightExpression().accept(this);
	}

	@Override
	public void visit(Matches arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(BitwiseAnd arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(BitwiseOr arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(BitwiseXor arg0) {
		// TODO Auto-generated method stub
		
	}

}
