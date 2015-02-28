package edu.buffalo.cse562.optimizer;

import java.util.ArrayList;
import java.util.List;

import net.sf.jsqlparser.schema.Column;

import edu.buffalo.cse562.conversion.VariableFinder;
import edu.buffalo.cse562.logicaloperators.*;
import edu.buffalo.cse562.relations.Schema;
import edu.buffalo.cse562.util.UtilityFunctions;

public class SelectionOptimizer 
{
	private LogicalOperator mRoot;

    public SelectionOptimizer(LogicalQuery root)
    {
        mRoot = root.Source;
    }

    private boolean TryPushIntoJoin(LogicalSelectionOperator selection, LogicalOperator join, LogicalOperator joinSource)
    {
        Schema schema = joinSource.getSchema();

        VariableFinder vf = new VariableFinder();
        List<Column> variables = vf.getColumnReferences(selection.Condition, join.getSchema());

        for (Column c : variables)
        {
        	if (!schema.contains(UtilityFunctions.getTableSafely(c), c.getColumnName()))
                return false;
        }

        //They all matched so we can push to just above the source.
        CommonOptimizer.SwapOperators(selection, join, joinSource);
        return true;
    }

    /*
     * Return the new root
     */ 
    private void PushCondition(LogicalSelectionOperator root)
    {
        /*
         * Selection commuting properties:
         * 
         *  1) Selection commutes with selection - Always
         * 
         *  2) Selection commutes with cross product - Only if all variables from the 
         *     selection are in one side of the relation.
         */

        LogicalOperator source = root.Source;

        if (source instanceof LogicalSelectionOperator)
        {
            //Case 1: We swap these two nodes
            LogicalOperator childSource = ((LogicalSelectionOperator)source).Source;
            CommonOptimizer.SwapOperators(root, source, childSource);
            root.recomputeSchema();
            PushCondition(root);
        }
        else if (source instanceof LogicalCrossProductOp || source instanceof LogicalJoinOp)
        {
            DualSourceLogicalOperator dualSource = (DualSourceLogicalOperator)source;

            if (TryPushIntoJoin(root, source, dualSource.Source1))
            {
                root.recomputeSchema();
                PushCondition(root);    //Keep going
            }
            else if (TryPushIntoJoin(root, source, dualSource.Source2))
            {
                root.recomputeSchema();
                PushCondition(root);    //Keep going
            }
        }
    }

    /*
     * The strategy is to first process the tree to find all the selections.
     * We then push each one as far as it can go, even if it doesn't get pushed above a table.
     * We use a queue so we don't infinitely try to push selections.  To see why, consider the
     * following:
     *      Sel(A) -> Sel(B) -> Table
     * Pushing the first selection would yield
     *      Sel(B) -> Sel(A) -> Table
     * Pushing the new root would yield
     *      Sel(A) -> Sel(B) -> Table
     * This would continue forever...
     */
    public void Optimize()
    {
        List<LogicalSelectionOperator> selections = new ArrayList<LogicalSelectionOperator>();
        CommonOptimizer.getSelectionQueue(mRoot, selections);

        for (int i = 0; i < selections.size(); i++)
        {
            PushCondition(selections.get(i));
        }
    }
}
