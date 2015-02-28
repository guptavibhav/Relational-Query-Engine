package edu.buffalo.cse562.tests;

import java.io.StringReader;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParser;
import edu.buffalo.cse562.aggregates.AggregateFunction;
import edu.buffalo.cse562.aggregates.AggregateFunctionList;
import edu.buffalo.cse562.conversion.AggregateFunctionConverter;

public class TestFunctionBuilder 
{
	public void Test()
	{
		try 
		{
			CCJSqlParser parser = new CCJSqlParser(new StringReader("2 + avg(A.B) * max(cost) + sum(1 + 6 * COST) + sum(1 + 6 * cost)"));
			Expression e = parser.SimpleExpression();
			
			AggregateFunctionList functions = new AggregateFunctionList();
			AggregateFunctionConverter.convertExpression(functions, e);
			
			for (AggregateFunction f : functions)
			{
				System.out.println(f.getAggFunctionName());
				System.out.println(f.getThisVarName());
				System.out.println(f.getAggregatingExpression().toString());
			}
			
		} 
		catch (Exception e)
		{
			System.err.println("Error! Message: " + e.getMessage());
		}
	}
}
