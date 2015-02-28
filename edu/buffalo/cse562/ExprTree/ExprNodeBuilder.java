package edu.buffalo.cse562.ExprTree;

import java.util.List;

import edu.buffalo.cse562.Exceptions.SQLSymbolNotFoundException;
import edu.buffalo.cse562.Exceptions.SQLTypeMismatchException;
import edu.buffalo.cse562.Factors.*;
import edu.buffalo.cse562.aggregates.AggregateFunction;
import edu.buffalo.cse562.aggregates.AggregateFunctionList;
import edu.buffalo.cse562.reloperators.QueryContext;
import edu.buffalo.cse562.reloperators.VariableContext;
import edu.buffalo.cse562.util.UtilityFunctions;
import net.sf.jsqlparser.expression.*;
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
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
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

public class ExprNodeBuilder implements ExpressionVisitor 
{
	private Expression mExpression;
	private ExpressionNode mResult;
	private QueryContext mContext;
	private AggregateFunctionList mAggLookup;
	
	/**
	 * 
	 * @param expr The expression that the gets converted to an ExpressionNode.
	 * @param schema The schema to use for variable look-ups.
	 */
	public ExprNodeBuilder(Expression expr, QueryContext context) 
	{
		this(expr, context, new AggregateFunctionList());
	}

	/**
	 * 
	 * @param expr The expression that the gets converted to an ExpressionNode.
	 * @param schema The schema to use for variable look-ups.
	 * @param aggs Aggregate lookup for functions
	 */
	public ExprNodeBuilder(Expression expr, QueryContext context, AggregateFunctionList aggs) 
	{
		mExpression = expr;
		mContext = context;
		mAggLookup = aggs;
	}

	public ExpressionNode convert()
	{
		mExpression.accept(this);
		return mResult;
	}
	
	@Override
	public void visit(NullValue arg0) 
	{
		mResult = new ConstFactorNode(new NullFactor());
	}

	private void handleDateFunction(Function arg0)
	{
		List args = arg0.getParameters().getExpressions();
		if (args.size() != 1)
		{
			throw new SQLTypeMismatchException("Expected single expression in DATE fuction");
		}
		
		mResult = new ConstFactorNode(new DateFactor(UtilityFunctions.stringToDate(args.get(0).toString())));
	}
	
	@Override
	public void visit(Function arg0) 
	{
		//These arguments kind of suck to deal with...
		
		//We're trying to find the variable name to substitute for this function call
		
		String fName = arg0.getName();
		
		if (fName.equalsIgnoreCase("DATE"))
		{
			handleDateFunction(arg0);
			return;
		}
		
		ExpressionList list = arg0.getParameters();
		
		Expression e = null;
		
		//Aggregating variables can be any expression.  We do require that they 
		if (!arg0.isAllColumns())
		{
			if (list.getExpressions().size() != 1)
				throw new SQLTypeMismatchException("Expected single expression in aggregate function");
			
			e = (Expression) list.getExpressions().get(0);
		}
		else
		{
			fName += "(*)";
		}
		
		AggregateFunction f = mAggLookup.findFunction(fName, e);
		
		if (f == null)
			throw new SQLSymbolNotFoundException("Aggregate function call: " + fName);
		
		int colIndex = mContext.CurrentContext.ContextSchema.findColumnIndex(f.getThisVarName());
		mResult = new VariableNode(mContext.CurrentContext, colIndex);
	}

	@Override
	public void visit(InverseExpression arg0)
	{
		ExpressionNode inner = new ExprNodeBuilder(arg0.getExpression(), mContext, mAggLookup).convert();
		mResult = new InverseOpNode(inner);
	}

	@Override
	public void visit(JdbcParameter arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(DoubleValue arg0) 
	{
		mResult = new ConstFactorNode(new DoubleFactor(arg0.getValue()));
	}

	@Override
	public void visit(LongValue arg0) 
	{
		mResult = new ConstFactorNode(new LongFactor(arg0.getValue()));	
	}

	@Override
	public void visit(DateValue arg0) 
	{
		mResult = new ConstFactorNode(new DateFactor(arg0.getValue()));	
	}

	@Override
	public void visit(TimeValue arg0) 
	{
		mResult = new ConstFactorNode(new TimeFactor(arg0.getValue()));	
	}

	@Override
	public void visit(TimestampValue arg0) 
	{
		mResult = new ConstFactorNode(new TimestampFactor(arg0.getValue()));
	}

	@Override
	public void visit(Parenthesis arg0) 
	{
		mResult = new ExprNodeBuilder(arg0.getExpression(), mContext, mAggLookup).convert();
	}

	@Override
	public void visit(StringValue arg0) 
	{
		mResult = new ConstFactorNode(new StringFactor(arg0.getValue()));
	}

	@Override
	public void visit(Addition arg0) 
	{
		ExpressionNode lhs = new ExprNodeBuilder(arg0.getLeftExpression(), mContext, mAggLookup).convert();
		ExpressionNode rhs = new ExprNodeBuilder(arg0.getRightExpression(), mContext, mAggLookup).convert();
		mResult = new AddOpNode(lhs, rhs);
	}

	@Override
	public void visit(Division arg0) 
	{
		ExpressionNode lhs = new ExprNodeBuilder(arg0.getLeftExpression(), mContext, mAggLookup).convert();
		ExpressionNode rhs = new ExprNodeBuilder(arg0.getRightExpression(), mContext, mAggLookup).convert();
		mResult = new DivideOpNode(lhs, rhs);	
	}

	@Override
	public void visit(Multiplication arg0) 
	{
		ExpressionNode lhs = new ExprNodeBuilder(arg0.getLeftExpression(), mContext, mAggLookup).convert();
		ExpressionNode rhs = new ExprNodeBuilder(arg0.getRightExpression(), mContext, mAggLookup).convert();
		mResult = new MultiplyOpNode(lhs, rhs);	
	}

	@Override
	public void visit(Subtraction arg0) 
	{
		ExpressionNode lhs = new ExprNodeBuilder(arg0.getLeftExpression(), mContext, mAggLookup).convert();
		ExpressionNode rhs = new ExprNodeBuilder(arg0.getRightExpression(), mContext, mAggLookup).convert();
		mResult = new SubtractOpNode(lhs, rhs);	
	}

	@Override
	public void visit(AndExpression arg0) 
	{
		ExpressionNode lhs = new ExprNodeBuilder(arg0.getLeftExpression(), mContext, mAggLookup).convert();
		ExpressionNode rhs = new ExprNodeBuilder(arg0.getRightExpression(), mContext, mAggLookup).convert();
		mResult = new AndOpNode(lhs, rhs);	
	}

	@Override
	public void visit(OrExpression arg0) 
	{
		ExpressionNode lhs = new ExprNodeBuilder(arg0.getLeftExpression(), mContext, mAggLookup).convert();
		ExpressionNode rhs = new ExprNodeBuilder(arg0.getRightExpression(), mContext, mAggLookup).convert();
		mResult = new OrOpNode(lhs, rhs);	
	}

	@Override
	public void visit(Between arg0) 
	{
		
		ExpressionNode lhs = new ExprNodeBuilder(arg0.getLeftExpression(), mContext, mAggLookup).convert();
		ExpressionNode fromExpr = new ExprNodeBuilder(arg0.getBetweenExpressionStart(), mContext, mAggLookup).convert();
		ExpressionNode toExpr = new ExprNodeBuilder(arg0.getBetweenExpressionEnd(), mContext, mAggLookup).convert();
		
		//This is just syntactic sugar for fromExpr <= lhs <= toExpr
		ExpressionNode relOp1 = new LessThanOpNode(fromExpr, lhs); 
		ExpressionNode relOp2 = new LessThanOpNode(lhs, toExpr);
		
		mResult = new AndOpNode(relOp1, relOp2);
	}

	@Override
	public void visit(EqualsTo arg0) 
	{
		ExpressionNode lhs = new ExprNodeBuilder(arg0.getLeftExpression(), mContext, mAggLookup).convert();
		ExpressionNode rhs = new ExprNodeBuilder(arg0.getRightExpression(), mContext, mAggLookup).convert();
		mResult = new EqualOpNode(lhs, rhs);
	}

	@Override
	public void visit(GreaterThan arg0) 
	{
		ExpressionNode lhs = new ExprNodeBuilder(arg0.getLeftExpression(), mContext, mAggLookup).convert();
		ExpressionNode rhs = new ExprNodeBuilder(arg0.getRightExpression(), mContext, mAggLookup).convert();
		mResult = new GreaterThanOpNode(lhs, rhs);	
	}

	@Override
	public void visit(GreaterThanEquals arg0) 
	{
		ExpressionNode lhs = new ExprNodeBuilder(arg0.getLeftExpression(), mContext, mAggLookup).convert();
		ExpressionNode rhs = new ExprNodeBuilder(arg0.getRightExpression(), mContext, mAggLookup).convert();
		mResult = new GreaterThanEqualOpNode(lhs, rhs);	
	}

	@Override
	public void visit(InExpression arg0) 
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(IsNullExpression arg0) 
	{
		ExpressionNode inner = new ExprNodeBuilder(arg0.getLeftExpression(), mContext, mAggLookup).convert();
		mResult = new IsNullOp(inner);
	}

	@Override
	public void visit(LikeExpression arg0) 
	{
		ExpressionNode lhs = new ExprNodeBuilder(arg0.getLeftExpression(), mContext, mAggLookup).convert();
		ExpressionNode rhs = new ExprNodeBuilder(arg0.getRightExpression(), mContext, mAggLookup).convert();
		
		if (!(rhs instanceof ConstFactorNode))
			throw new SQLTypeMismatchException("RHS of LIKE must be a string constant");
		
		Factor f = rhs.ToFactor();
		if (!(f instanceof StringFactor))
			throw new SQLTypeMismatchException("RHS of LIKE must be a string constant");
		
		String s = ((StringFactor)f).getValue();
		
		mResult = new LikeOpNode(lhs, s);
	}

	@Override
	public void visit(MinorThan arg0) 
	{
		ExpressionNode lhs = new ExprNodeBuilder(arg0.getLeftExpression(), mContext, mAggLookup).convert();
		ExpressionNode rhs = new ExprNodeBuilder(arg0.getRightExpression(), mContext, mAggLookup).convert();
		mResult = new LessThanOpNode(lhs, rhs);	
	}

	@Override
	public void visit(MinorThanEquals arg0) 
	{
		ExpressionNode lhs = new ExprNodeBuilder(arg0.getLeftExpression(), mContext, mAggLookup).convert();
		ExpressionNode rhs = new ExprNodeBuilder(arg0.getRightExpression(), mContext, mAggLookup).convert();
		mResult = new LessThanEqualOpNode(lhs, rhs);
	}

	@Override
	public void visit(NotEqualsTo arg0) 
	{
		ExpressionNode lhs = new ExprNodeBuilder(arg0.getLeftExpression(), mContext, mAggLookup).convert();
		ExpressionNode rhs = new ExprNodeBuilder(arg0.getRightExpression(), mContext, mAggLookup).convert();
		mResult = new NotEqualOpNode(lhs, rhs);
	}

	@Override
	public void visit(Column arg0) 
	{
		String table = UtilityFunctions.getTableSafely(arg0);		
		String name = arg0.getColumnName();
		
		int colIndex = -1;
		
		try
		{
			colIndex = mContext.CurrentContext.ContextSchema.findColumnIndex(table, name);
			mResult = new VariableNode(mContext.CurrentContext, colIndex);
		}
		catch (SQLSymbolNotFoundException e)
		{
			if (mContext.OuterQueryContext != null)
			{
				colIndex = mContext.OuterQueryContext.ContextSchema.findColumnIndex(table, name);
				mResult = new VariableNode(mContext.OuterQueryContext, colIndex);
			}
			else
			{
				throw new SQLSymbolNotFoundException("name");
			}
		}
	}

	@Override
	public void visit(SubSelect arg0) 
	{
		mResult = new SubSelectOpNode(mContext.CurrentContext, arg0.getSelectBody());
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
		ExpressionNode lhs = new ExprNodeBuilder(arg0.getLeftExpression(), mContext, mAggLookup).convert();
		ExpressionNode rhs = new ExprNodeBuilder(arg0.getRightExpression(), mContext, mAggLookup).convert();
		mResult = new ConcatOpNode(lhs, rhs);
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
