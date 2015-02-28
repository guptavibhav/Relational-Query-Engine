package edu.buffalo.cse562.optimizer;

import java.util.List;

import edu.buffalo.cse562.logicaloperators.*;

public class CommonOptimizer 
{
	public static void getSelectionQueue(LogicalOperator root, List<LogicalSelectionOperator> queue)
    {
        if (root instanceof LogicalQuery)
        {
            //We do not want to consider sub-selects
            return;
        }
        else if (root instanceof LogicalSelectionOperator)
        {
            queue.add((LogicalSelectionOperator)root);
            getSelectionQueue(((LogicalSelectionOperator)root).Source, queue);
        }
        else if (root instanceof SingleSourceLogicalOperator)
        {
            getSelectionQueue(((SingleSourceLogicalOperator)root).Source, queue);
        }
        else if (root instanceof DualSourceLogicalOperator)
        {
            getSelectionQueue(((DualSourceLogicalOperator)root).Source1, queue);
            getSelectionQueue(((DualSourceLogicalOperator)root).Source2, queue);
        }

        //Else base case - table
    }
	
	public static void SwapOperators(LogicalOperator root, LogicalOperator child, LogicalOperator childSource)
    {
        /*
         * The tree looks like this:
         * 
         *      ? -> root -> child -> childSource
         * 
         * and we want it to look like:
         * 
         *      ? -> child -> root -> childSource
         */
		
		if (root.Parent != null)
			root.Parent.replaceChild(root, child);
        child.Parent = root.Parent;

        child.replaceChild(childSource, root);
        root.Parent = child;

        root.replaceChild(child, (childSource));
        childSource.Parent = root;            
    }
}
