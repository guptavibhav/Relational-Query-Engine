package edu.buffalo.cse562.util;

import java.util.ArrayList;

public class SortDefinitionList extends ArrayList<SortDefinition> 
{
	public boolean isEqualTo(SortDefinitionList other)
	{
		if (this.size() != other.size())
			return false;
		
		for (int i = 0; i < this.size(); i++)
		{
			SortDefinition lhs = this.get(i);
			SortDefinition rhs = other.get(i);
			
			if (lhs.IsAscending != rhs.IsAscending)
				return false;
			
			if (!lhs.mExpression.toString().equalsIgnoreCase(rhs.mExpression.toString()))
				return false;
		}
		
		return true;
	}

}
