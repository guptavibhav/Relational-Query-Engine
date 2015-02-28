package edu.buffalo.cse562.aggregates;

import java.util.ArrayList;

import net.sf.jsqlparser.expression.Expression;

public class AggregateFunctionList extends ArrayList<AggregateFunction> 
{
	public AggregateFunction findFunction(String fnName, Expression exprToAgg)
	{
		for (AggregateFunction f : this)
		{
			if (f.getAggFunctionName().equalsIgnoreCase(fnName))
			{
				//exprToAgg is null for COUNT
				if (f.getAggFunctionName() == CountAllAggregate.FUNCTION_NAME)
					return f;
				else if (exprToAgg.toString().equalsIgnoreCase(f.getAggregatingExpression().toString()))
					return f;
			}
		}
		
		return null;
	}
}
