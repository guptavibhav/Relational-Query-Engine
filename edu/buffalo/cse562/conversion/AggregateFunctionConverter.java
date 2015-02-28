package edu.buffalo.cse562.conversion;

import java.util.List;

import edu.buffalo.cse562.Exceptions.SQLTypeMismatchException;
import edu.buffalo.cse562.aggregates.*;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.schema.Column;

public class AggregateFunctionConverter 
{
	private static Expression convertFunctionCall(AggregateFunctionList functions, Function node) throws SQLTypeMismatchException
	{
		String fName = node.getName();
		String newVarName = "aggregate" + String.valueOf(functions.size()).trim();
		String actualName = newVarName;
		
		Expression e = null;
		
		if (!node.isAllColumns())
		{
			if (node.getParameters() != null)
			{
				List fnParams = node.getParameters().getExpressions();
				if (fnParams.size() != 1)
					throw new SQLTypeMismatchException("Expected single expression in aggregate function");
				
				e = (Expression)fnParams.get(0);
			}
		}
		else
		{
			fName += "(*)";
		}
		
		if (fName.equalsIgnoreCase(AverageAggregate.FUNCTION_NAME))
		{
			AggregateFunction f = functions.findFunction(AverageAggregate.FUNCTION_NAME, e);
			if (f == null)
				functions.add(new AverageAggregate(e, newVarName));
			else
				actualName = f.getThisVarName();
		}
		else if (fName.equalsIgnoreCase(CountAllAggregate.FUNCTION_NAME))
		{
			AggregateFunction f = functions.findFunction(CountAllAggregate.FUNCTION_NAME, e);
			if (f == null)
				functions.add(new CountAllAggregate(newVarName));
			else
				actualName = f.getThisVarName();
		}
		else if (fName.equalsIgnoreCase(CountAggregate.FUNCTION_NAME))
		{
			AggregateFunction f = functions.findFunction(CountAggregate.FUNCTION_NAME, e);
			if (f == null)
				functions.add(new CountAggregate(e, newVarName, node.isDistinct()));
			else
				actualName = f.getThisVarName();
		}
		else if (fName.equalsIgnoreCase(SumAggregate.FUNCTION_NAME))
		{
			AggregateFunction f = functions.findFunction(SumAggregate.FUNCTION_NAME, e);
			if (f == null)
				functions.add(new SumAggregate(e, newVarName));
			else
				actualName = f.getThisVarName();
		}
		else if (fName.equalsIgnoreCase(MaximumAggregate.FUNCTION_NAME))
		{
			AggregateFunction f = functions.findFunction(MaximumAggregate.FUNCTION_NAME, e);
			if (f == null)
				functions.add(new MaximumAggregate(e, newVarName));
			else
				actualName = f.getThisVarName();
		}
		else if (fName.equalsIgnoreCase(MinimumAggregate.FUNCTION_NAME))
		{
			AggregateFunction f = functions.findFunction(MinimumAggregate.FUNCTION_NAME, e);
			if (f == null)
				functions.add(new MinimumAggregate(e, newVarName));
			else
				actualName = f.getThisVarName();
		}
		else
		{
			//Not an aggregate function
			return node;
		}
		
		return new Column(null, actualName);
		
	}
	
	public static Expression convertExpression(AggregateFunctionList functions, Expression root)
	{
		if (root instanceof BinaryExpression)
		{
			BinaryExpression binexpr = (BinaryExpression)root;
			binexpr.setLeftExpression(convertExpression(functions, binexpr.getLeftExpression()));
			binexpr.setRightExpression(convertExpression(functions, binexpr.getRightExpression()));
			return root;
		}
		else if (root instanceof Parenthesis)
		{
			Parenthesis p = (Parenthesis)root;
			p.setExpression(convertExpression(functions, p.getExpression()));
			return root;
		}
		else if (root instanceof Function)
		{
			return convertFunctionCall(functions, (Function)root);
		}
		else
		{
			return root;
		}
	}

}
