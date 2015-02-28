package edu.buffalo.cse562.optimizer;

import edu.buffalo.cse562.logicaloperators.DualSourceLogicalOperator;
import edu.buffalo.cse562.logicaloperators.LogicalOperator;
import edu.buffalo.cse562.logicaloperators.LogicalSortOperator;
import edu.buffalo.cse562.logicaloperators.SingleSourceLogicalOperator;
import edu.buffalo.cse562.reloperators.SingleSourceOperator;
import edu.buffalo.cse562.util.SortDefinitionList;

/*
 * Our job is to get rid of redundant sort operations
 */
public class SortOptimizer 
{
	private LogicalOperator mRoot;
	
	public SortOptimizer(LogicalOperator root) 
	{
		mRoot = root;
	}
	
	private void depthFirstOptimize(LogicalOperator root)
	{
		if (root instanceof SingleSourceLogicalOperator)
		{
			SingleSourceLogicalOperator sso = (SingleSourceLogicalOperator)root;
			depthFirstOptimize(sso.Source);
		}
		else if (root instanceof DualSourceLogicalOperator)
		{
			DualSourceLogicalOperator dso = (DualSourceLogicalOperator)root;
			depthFirstOptimize(dso.Source1);
			depthFirstOptimize(dso.Source2);
		}
		
		if (root instanceof LogicalSortOperator)
		{
			LogicalSortOperator sortOp = (LogicalSortOperator)root;
			SortDefinitionList prevOrder = sortOp.Source.getSortOrder();
			
			if (prevOrder != null)
			{
				if (sortOp.SortList.isEqualTo(prevOrder))
				{
					//This is redundant!
					
					//remove node
					sortOp.Source.Parent = sortOp.Parent;
					sortOp.Parent.replaceChild(sortOp, sortOp.Source);
				}
			}
		}
	}
	
	public void Optimize()
	{
		depthFirstOptimize(mRoot);
	}

}
