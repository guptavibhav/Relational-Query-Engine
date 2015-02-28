package edu.buffalo.cse562.util;

import net.sf.jsqlparser.expression.Expression;

public class SortDefinition 
{
	public Expression mExpression;
	public boolean IsAscending;
	
	public SortDefinition(Expression e, boolean isAsc) 
	{
		mExpression = e;
		IsAscending = isAsc;
	}

}
