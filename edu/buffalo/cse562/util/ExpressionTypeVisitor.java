package edu.buffalo.cse562.util;

import edu.buffalo.cse562.Exceptions.SQLTypeMismatchException;
import edu.buffalo.cse562.aggregates.*;
import edu.buffalo.cse562.relations.ColumnDataTypes;
import edu.buffalo.cse562.relations.Schema;
import edu.buffalo.cse562.relations.SchemaColumn;
import net.sf.jsqlparser.expression.AllComparisonExpression;
import net.sf.jsqlparser.expression.AnyComparisonExpression;
import net.sf.jsqlparser.expression.CaseExpression;
import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.InverseExpression;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.TimeValue;
import net.sf.jsqlparser.expression.TimestampValue;
import net.sf.jsqlparser.expression.WhenClause;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseAnd;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseOr;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseXor;
import net.sf.jsqlparser.expression.operators.arithmetic.Concat;
import net.sf.jsqlparser.expression.operators.arithmetic.Division;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.Matches;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SubSelect;

public class ExpressionTypeVisitor implements ExpressionVisitor 
{
	private ColumnDataTypes mCurType = ColumnDataTypes.tpLong;
	private Expression mExpression;
	private Schema mSchema;
	private AggregateFunctionList mAggregates;
	
	public ExpressionTypeVisitor(Schema sourceSchema, Expression expr)
	{
		this(sourceSchema, expr, new AggregateFunctionList());
	}
	
	public ExpressionTypeVisitor(Schema sourceSchema, Expression expr, AggregateFunctionList aggs) 
	{
        mExpression = expr;
        mSchema = sourceSchema;
        mAggregates = aggs;
	}

	public ColumnDataTypes getExpressionType()
	{
		mExpression.accept(this);
		return mCurType;
	}
	
	@Override
	public void visit(NullValue arg0) 
	{
        throw new SQLTypeMismatchException("Can we have null values in expressions?");
	}

	@Override
	public void visit(Function arg0) 
	{
		String name = arg0.getName();
		
        if (name.equalsIgnoreCase(CountAggregate.FUNCTION_NAME) || name == CountAllAggregate.FUNCTION_NAME)
        {
            mCurType = ColumnDataTypes.tpLong;
        }
        else if (name == "DATE")
        {
            mCurType = ColumnDataTypes.tpDate;
        }
        else if (name == "STR")
        {
            mCurType = ColumnDataTypes.tpString;
        }
        else if (arg0.getParameters() != null && arg0.getParameters().getExpressions().size() == 1)
        {
        	Expression e = (Expression)arg0.getParameters().getExpressions().get(0);
            AggregateFunction f = mAggregates.findFunction(name, e);
            if (f != null)
            {
                SchemaColumn col = mSchema.findColumn(f.getThisVarName());
                mCurType = col.getDataType();
            }

            /*
            //The type becomes the type of what we're aggregating
            if (n.ParameterList.Count == 1)
                n.ParameterList[0].Accept(this);
             */
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
	public void visit(DoubleValue arg0) 
	{
		mCurType = ColumnDataTypes.tpDouble;
	}

	@Override
	public void visit(LongValue arg0) 
	{
		mCurType = ColumnDataTypes.tpLong;
	}

	@Override
	public void visit(DateValue arg0) 
	{
		mCurType = ColumnDataTypes.tpDate;
	}

	@Override
	public void visit(TimeValue arg0) 
	{
		mCurType = ColumnDataTypes.tpTime;
	}

	@Override
	public void visit(TimestampValue arg0) 
	{
		mCurType = ColumnDataTypes.tpTimeStamp;
	}

	@Override
	public void visit(Parenthesis arg0) 
	{
		arg0.getExpression().accept(this);
	}

	@Override
	public void visit(StringValue arg0) 
	{
		mCurType = ColumnDataTypes.tpString;
	}

	public ColumnDataTypes CheckMathOp(Expression lhs, Expression rhs)
    {
        lhs.accept(this);
        ColumnDataTypes type1 = mCurType;
        rhs.accept(this);
        ColumnDataTypes type2 = mCurType;

        if (type1 == type2)
            return type1;

        //Check promotion to double
        if (type1 == ColumnDataTypes.tpLong && type2 == ColumnDataTypes.tpDouble)
        {
            return ColumnDataTypes.tpDouble;
        }
        else if (type1 == ColumnDataTypes.tpDouble && type2 == ColumnDataTypes.tpLong)
        {
            return ColumnDataTypes.tpDouble;
        }
        else
        {
            throw new SQLTypeMismatchException("Invalid mathematical operand types");
        }
    }
	
	@Override
	public void visit(Addition arg0) 
	{
		mCurType = CheckMathOp(arg0.getLeftExpression(), arg0.getRightExpression());
	}

	@Override
	public void visit(Division arg0) 
	{
		mCurType = CheckMathOp(arg0.getLeftExpression(), arg0.getRightExpression());
	}

	@Override
	public void visit(Multiplication arg0) 
	{
		mCurType = CheckMathOp(arg0.getLeftExpression(), arg0.getRightExpression());	
	}

	@Override
	public void visit(Subtraction arg0) 
	{
		mCurType = CheckMathOp(arg0.getLeftExpression(), arg0.getRightExpression());	
	}

	private ColumnDataTypes CheckBooleanCompare(Expression lhs, Expression rhs)
    {
        lhs.accept(this);
        ColumnDataTypes type1 = mCurType;
        rhs.accept(this);
        ColumnDataTypes type2 = mCurType;

        if (type1 == type2)
            return ColumnDataTypes.tpBoolean;

        //Check promotion to double
        if (type1 == ColumnDataTypes.tpLong && type2 == ColumnDataTypes.tpDouble)
        {
            return ColumnDataTypes.tpBoolean;
        }
        else if (type1 == ColumnDataTypes.tpDouble && type2 == ColumnDataTypes.tpLong)
        {
            return ColumnDataTypes.tpBoolean;
        }
        else
        {
            throw new SQLTypeMismatchException("Invalid mathematical operand types");
        }
    }
	
	@Override
	public void visit(AndExpression arg0) 
	{
        //Just type check here.  Why?  because both will set mCurType to boolean if successful.
        arg0.getLeftExpression().accept(this);
        arg0.getRightExpression().accept(this);	
	}

	@Override
	public void visit(OrExpression arg0) 
	{
        //Just type check here.  Why?  because both will set mCurType to boolean if successful.
        arg0.getLeftExpression().accept(this);
        arg0.getRightExpression().accept(this);	
	}

	@Override
	public void visit(Between arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(EqualsTo arg0) 
	{
		mCurType = CheckBooleanCompare(arg0.getLeftExpression(), arg0.getRightExpression());
	}

	@Override
	public void visit(GreaterThan arg0) 
	{
		mCurType = CheckBooleanCompare(arg0.getLeftExpression(), arg0.getRightExpression());	
	}

	@Override
	public void visit(GreaterThanEquals arg0) 
	{
		mCurType = CheckBooleanCompare(arg0.getLeftExpression(), arg0.getRightExpression());	
	}

	@Override
	public void visit(InExpression arg0) 
	{
		mCurType = ColumnDataTypes.tpBoolean;
	}

	@Override
	public void visit(IsNullExpression arg0) 
	{
		mCurType = ColumnDataTypes.tpBoolean;
	}

	@Override
	public void visit(LikeExpression arg0) 
	{
		mCurType = ColumnDataTypes.tpBoolean;
	}

	@Override
	public void visit(MinorThan arg0) 
	{
		mCurType = CheckBooleanCompare(arg0.getLeftExpression(), arg0.getRightExpression());	
	}

	@Override
	public void visit(MinorThanEquals arg0) 
	{
		mCurType = CheckBooleanCompare(arg0.getLeftExpression(), arg0.getRightExpression());
	}

	@Override
	public void visit(NotEqualsTo arg0) 
	{
		mCurType = CheckBooleanCompare(arg0.getLeftExpression(), arg0.getRightExpression());
	}

	@Override
	public void visit(Column arg0) 
	{
		String table = "";
		if (arg0.getTable() != null && arg0.getTable().getName() != null)
			table = arg0.getTable().getName();
		
		SchemaColumn col = mSchema.findColumn(table, arg0.getColumnName());
        mCurType = col.getDataType();
	}

	@Override
	public void visit(SubSelect arg0) {
		// TODO Auto-generated method stub
		
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
		mCurType = ColumnDataTypes.tpString;
	}

	@Override
	public void visit(Matches arg0) 
	{
		mCurType = ColumnDataTypes.tpBoolean;
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
