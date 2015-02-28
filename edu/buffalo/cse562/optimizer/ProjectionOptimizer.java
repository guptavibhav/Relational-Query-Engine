package edu.buffalo.cse562.optimizer;

import java.util.ArrayList;
import java.util.List;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import edu.buffalo.cse562.Main;
import edu.buffalo.cse562.aggregates.AggregateFunction;
import edu.buffalo.cse562.aggregates.AggregateFunctionList;
import edu.buffalo.cse562.conversion.VariableFinder;
import edu.buffalo.cse562.logicaloperators.DualSourceLogicalOperator;
import edu.buffalo.cse562.logicaloperators.LogicalAggregateOp;
import edu.buffalo.cse562.logicaloperators.LogicalAliasOp;
import edu.buffalo.cse562.logicaloperators.LogicalCrossProductOp;
import edu.buffalo.cse562.logicaloperators.LogicalGroupByAgg;
import edu.buffalo.cse562.logicaloperators.LogicalJoinOp;
import edu.buffalo.cse562.logicaloperators.LogicalOperator;
import edu.buffalo.cse562.logicaloperators.LogicalProjectionOp;
import edu.buffalo.cse562.logicaloperators.LogicalScanOp;
import edu.buffalo.cse562.logicaloperators.LogicalSelectionOperator;
import edu.buffalo.cse562.logicaloperators.SingleSourceLogicalOperator;
import edu.buffalo.cse562.relations.ColumnDataTypes;
import edu.buffalo.cse562.relations.Schema;
import edu.buffalo.cse562.relations.SchemaColumn;
import edu.buffalo.cse562.reloperators.VariableContext;
import edu.buffalo.cse562.util.UtilityFunctions;

public class ProjectionOptimizer 
{
	private LogicalProjectionOp mRoot;
    private LogicalOperator mNewRoot;
    private VariableContext mOuterContext;

    public ProjectionOptimizer(LogicalProjectionOp root, VariableContext outerContext)
    {
        mRoot = root;
        mOuterContext = outerContext;
    }

    private boolean CheckForNonVariableExpressions(LogicalProjectionOp projection)
    {
        for (SelectItem item : projection.SelectItems)
        {
            if (item instanceof SelectExpressionItem)
            {
                SelectExpressionItem expr = (SelectExpressionItem)item;
                if (!(expr.getExpression() instanceof Column) || (expr.getAlias() != null && !expr.getAlias().equals("")))
                {
                    return true;
                }
            }
            else
            {
                return true;
            }
        }

        return false;
    }

    /*
     * Convert the list of select items into a list of variables
     */ 
    private LogicalProjectionOp ConvertComplexProjection(LogicalProjectionOp projection)
    {
        boolean foundComplexExpr = false;
        List<Column> variables = new ArrayList<Column>();
        Schema schema = projection.getSchema();

        for (SelectItem item : projection.SelectItems)
        {
            if (item instanceof SelectExpressionItem)
            {
                SelectExpressionItem expr = (SelectExpressionItem)item;

                if (!(expr.getExpression() instanceof Column) || (expr.getAlias() != null && !expr.getAlias().equals("")))
                {
                    foundComplexExpr = true;
                }

                if (expr.getExpression() instanceof Column)
                {
                    variables.add((Column)expr.getExpression());
                }
                else
                {
                    List<Column> tempVars = new VariableFinder().getColumnReferences(expr.getExpression(), schema);

                    if (tempVars.size() > 0)
                        variables.addAll(tempVars);
                }

            }
            else if (item instanceof AllColumns)
            {
                //Add all columns to item list
                
                for (SchemaColumn sc : schema)
                {
                	Column c = new Column();
                	c.setColumnName(sc.getName());
                	if (!sc.getTableName().equals(""))
                	{
                		c.setTable(new Table());
                		c.getTable().setName(sc.getTableName());
                	}
                    variables.add(c);
                }
            }
            else if (item instanceof AllTableColumns)
            {
                String table = ((AllTableColumns)item).getTable().getName();
                //Add all columns from a table to item list
                for (SchemaColumn sc : schema)
                {
                    if (sc.getTableName().equalsIgnoreCase(table))
                    {
                    	Column c = new Column();
                    	c.setColumnName(sc.getName());
                    	if (!sc.getTableName().equals(""))
                    	{
                    		c.setTable(new Table());
                    		c.getTable().setName(sc.getTableName());
                    	}
                        variables.add(c);
                    }
                }
            }
        }

        /*
         * Create new select item list
         */ 
        List<SelectItem> selectItems = new ArrayList<SelectItem>();

        for (Column node : variables)
        {
        	SelectExpressionItem selectItem = new SelectExpressionItem();
        	selectItem.setExpression(node);
            selectItems.add(selectItem);
        }

        if (foundComplexExpr)
        {
            LogicalProjectionOp p2 = new LogicalProjectionOp(projection.Source, selectItems, mOuterContext);
            projection.replaceChild(projection.Source, p2);
            p2.Parent = projection;
            projection.recomputeSchema();
            return p2;
        }
        else
        {
            //Some comibination of *, Table.*, and variables without aliases
            //That means this schema is equivalent
            projection.SelectItems = selectItems;
            projection.recomputeSchema();
            return projection;
        }
    }

    private List<SelectItem> GetIntersection(Schema schema, List<SelectItem> variableNodes)
    {
        List<SelectItem> intersection = new ArrayList<SelectItem>();

        for (SelectItem node : variableNodes)
        {
            SelectExpressionItem selectItem = (SelectExpressionItem)node;
            Column varNode = (Column)selectItem.getExpression();
            if (schema.contains(UtilityFunctions.getTableSafely(varNode), varNode.getColumnName()))
            {
                intersection.add(selectItem);
            }
        }

        return intersection;
    }

    private LogicalOperator SwapProjectionAndSelection(LogicalProjectionOp root, LogicalSelectionOperator source)
    {
        LogicalProjectionOp rootCopy = (LogicalProjectionOp)root.copy();
        boolean needsCopy = false;

        VariableFinder vf = new VariableFinder();
        List<Column> vars = vf.getColumnReferences(source.Condition, source.getSchema());

        for (Column v : vars)
        {
            if (!rootCopy.getSchema().contains(UtilityFunctions.getTableSafely(v), v.getColumnName()))
            {
                needsCopy = true;
                rootCopy.getSchema().add(new SchemaColumn(UtilityFunctions.getTableSafely(v), 
                		v.getColumnName(), ColumnDataTypes.tpLong));   //Type doesn't matter;
                
                SelectExpressionItem si = new SelectExpressionItem();
                si.setExpression(v);
                
                rootCopy.SelectItems.add(si);
                rootCopy.recomputeSchema();
            }
        }

        if (needsCopy)
        {
            //In this case, we've added to the schema so we need to project away the added
            //column(s) after selection.  After insering the new projection, the tree branch will change like this:
            //  Orig:   proj->selection->sel_source
            //  New:    proj->selection->proj2->sel_source
            
            source.Source.Parent = rootCopy;
            rootCopy.Source = source.Source;

            source.Source = rootCopy;
            rootCopy.Parent = source;

            rootCopy.recomputeSchema();
            
            return root;
        }
        else
        {
            CommonOptimizer.SwapOperators(root, source, source.Source);
            
            if (root == mNewRoot)
                mNewRoot = source;
            
            //We want the projection to be pushed farther
            return source;
        }
    }

    private void SwapJoinAndProjection(LogicalProjectionOp projection, DualSourceLogicalOperator join, Expression condition)
    {
        Schema lhsSchema = join.Source1.getSchema();
        Schema rhsSchema = join.Source2.getSchema();

        List<SelectItem> lhsSelects = GetIntersection(lhsSchema, projection.SelectItems);
        List<SelectItem> rhsSelects = GetIntersection(rhsSchema, projection.SelectItems);

        if (lhsSelects.size() + rhsSelects.size() != projection.SelectItems.size())
        {
            //Something is really wrong here!
            System.err.println("Illogical intersection between projection and join sources");
            Main.onError();
        }

        LogicalProjectionOp lhsProj = new LogicalProjectionOp(join.Source1, lhsSelects, mOuterContext);
        LogicalProjectionOp rhsProj = new LogicalProjectionOp(join.Source2, rhsSelects, mOuterContext);

        if (condition != null)
        {
            VariableFinder vf = new VariableFinder();
            List<Column> variables = vf.getColumnReferences(condition, join.getSchema());

            for (Column v : variables)
            {
                if (lhsSchema.contains(UtilityFunctions.getTableSafely(v), v.getColumnName()))
                {
                    if (!lhsProj.getSchema().contains(UtilityFunctions.getTableSafely(v), v.getColumnName()))
                    {
                    	SelectExpressionItem si = new SelectExpressionItem();
                    	si.setExpression(v);
                        lhsSelects.add(si);
                        lhsProj.recomputeSchema();
                    }
                }
                else
                {
                	if (!rhsProj.getSchema().contains(UtilityFunctions.getTableSafely(v), v.getColumnName()))
                    {
                    	SelectExpressionItem si = new SelectExpressionItem();
                    	si.setExpression(v);
                        rhsSelects.add(si);
                        rhsProj.recomputeSchema();
                    }
                }
            }
        }

        join.replaceChild(join.Source1, lhsProj);
        join.replaceChild(join.Source2, rhsProj);

        lhsProj.Parent = join;
        rhsProj.Parent = join;

        lhsProj.Source.Parent = lhsProj;
        rhsProj.Source.Parent = rhsProj;

        join.recomputeSchema();

        /*
         * NOTE: We leave the original projection where it is because
         * it might change the order of the elements.
         */ 
    }
    
    private List<Integer> getSchemaIndexes(LogicalProjectionOp projection)
    {
    	Schema schema = projection.getSchema();
    	List<Integer> indexes = new ArrayList<Integer>();
    	
    	for (SchemaColumn sc : schema)
    	{
    		int c = projection.Source.getSchema().findColumnIndex(sc.getTableName(), sc.getName());
    		indexes.add(c);
    	}
    	
    	return indexes;
    }
    
    private void SwapWithAggregate(LogicalProjectionOp projection)
    {
    	LogicalOperator aggregate = projection.Source;
    	List<SelectItem> tempList = new ArrayList<SelectItem>();
    	
    	/*
    	 * we don't need aggregate variables
    	 */
    	for (SelectItem s : projection.SelectItems)
    	{
    		Column c = (Column)(((SelectExpressionItem)s).getExpression());
    		
    		if (!c.getColumnName().startsWith("aggregate"))
    		{
    			tempList.add(s);
    		}
    	}
    	
    	/*
    	 * We need everything in the aggregate function expressions and the group by list
    	 */
    	AggregateFunctionList functions = null;
    	List<Column> groupBy = null;
    	LogicalOperator aggSrc = ((SingleSourceLogicalOperator)aggregate).Source;
    	LogicalProjectionOp newProj = new LogicalProjectionOp(aggSrc, tempList, mOuterContext);
    	
    	
    	if (aggregate instanceof LogicalGroupByAgg)
    	{
    		LogicalGroupByAgg gba = (LogicalGroupByAgg)aggregate;
    		functions = gba.AggregatesFunctions;
    		groupBy = gba.GroupBy;
    		aggSrc = gba.Source;
    	}
    	else
    	{
    		LogicalAggregateOp aggOp = (LogicalAggregateOp)aggregate;
    		functions = aggOp.AggregateFunctions;
    		aggSrc = aggOp.Source;
    	}
    	
    	for (AggregateFunction f : functions)
    	{
    		if (f.getAggregatingExpression() != null)
    		{
    			VariableFinder vf = new VariableFinder();
    			List<Column> cols = vf.getColumnReferences(f.getAggregatingExpression(), aggregate.getSchema());
    			
    			for (Column c : cols)
    			{
    				String table = UtilityFunctions.getTableSafely(c);
    				
    				if (!newProj.getSchema().contains(table, c.getColumnName()))
    				{
    					SelectExpressionItem item = new SelectExpressionItem();
        				item.setExpression(c);
        				tempList.add(item);	
        				newProj.recomputeSchema();
    				}
    			}
    		}
    	}
    	
    	if (groupBy != null)
    	{
    		for (Column c : groupBy)
			{
    			String table = UtilityFunctions.getTableSafely(c);
				
				if (!newProj.getSchema().contains(table, c.getColumnName()))
				{
					SelectExpressionItem item = new SelectExpressionItem();
					item.setExpression(c);
					tempList.add(item);
					newProj.recomputeSchema();
				}
			}
    	}
    	
    	aggSrc.Parent = newProj;
    	
    	aggregate.replaceChild(aggSrc, newProj);
    	newProj.Parent = aggregate;
    	
    	projection.recomputeSchema();
    	
    }

    /*
     * It is a assumed that the projection ONLY contains variable references
     */
    private void TryPushProjections(LogicalOperator root)
    {
        if (root instanceof LogicalProjectionOp)
        {
            LogicalProjectionOp proj = (LogicalProjectionOp)root;

            if (proj.Source instanceof LogicalSelectionOperator)
            {
                root = SwapProjectionAndSelection(proj, (LogicalSelectionOperator)proj.Source);
            }
            else if (proj.Source instanceof LogicalJoinOp)
            {
                SwapJoinAndProjection(proj, (LogicalJoinOp)proj.Source, ((LogicalJoinOp)proj.Source).Condition);
            }
            else if (proj.Source instanceof LogicalCrossProductOp)
            {
                SwapJoinAndProjection(proj, (LogicalCrossProductOp)proj.Source, null);
            }
            else if (proj.Source instanceof LogicalAggregateOp || proj.Source instanceof LogicalGroupByAgg)
            {
            	SwapWithAggregate(proj);
            }
            else if (proj.Source instanceof LogicalAliasOp)
            {
            	LogicalAliasOp alias = (LogicalAliasOp)proj.Source;
            	if (alias.Source instanceof LogicalScanOp)
            	{
            		//LogicalScanOp op = (LogicalScanOp)alias.Source;
            		//op.Indexes = getSchemaIndexes(proj);
            		//root.recomputeSchema();
            		return;
            	}
            }
            else if (proj.Source instanceof LogicalScanOp)
            {
            	//LogicalScanOp op = (LogicalScanOp)proj.Source;
        		//op.Indexes = getSchemaIndexes(proj);
        		//root.recomputeSchema();
        		return;
            }
        }
        
        if (root instanceof SingleSourceLogicalOperator)
        {
            TryPushProjections(((SingleSourceLogicalOperator)root).Source);
        }
        else if (root instanceof DualSourceLogicalOperator)
        {
            TryPushProjections(((DualSourceLogicalOperator)root).Source1);
            TryPushProjections(((DualSourceLogicalOperator)root).Source2);
        }
    }

    public LogicalOperator Optimize()
    {
        mNewRoot = mRoot;
        LogicalProjectionOp projStart = mRoot;

        if (CheckForNonVariableExpressions(mRoot))
        {
            //We need to convert the selection to something manageable
            projStart = ConvertComplexProjection(mRoot);
        }

        TryPushProjections(projStart);

        return mNewRoot;
    }
}
