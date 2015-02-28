package edu.buffalo.cse562.logicaloperators;

import java.util.ArrayList;
import java.util.List;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import edu.buffalo.cse562.Exceptions.SQLSymbolNotFoundException;
import edu.buffalo.cse562.relations.Schema;
import edu.buffalo.cse562.relations.SchemaColumn;
import edu.buffalo.cse562.reloperators.EquiJoinMapping;
import edu.buffalo.cse562.reloperators.InMemoryHashJoin;
import edu.buffalo.cse562.reloperators.InMemorySortOperator;
import edu.buffalo.cse562.reloperators.OutOfCoreSortOperator;
import edu.buffalo.cse562.reloperators.RelationalOperator;
import edu.buffalo.cse562.reloperators.SortMergeJoin;
import edu.buffalo.cse562.util.SortDefinition;
import edu.buffalo.cse562.util.SortDefinitionList;
import edu.buffalo.cse562.util.SwapManager;
import edu.buffalo.cse562.util.UtilityFunctions;

public class LogicalJoinOp extends DualSourceLogicalOperator 
{
	public Expression Condition;
	
	public LogicalJoinOp(LogicalOperator s1, LogicalOperator s2, Expression condition) 
	{
		super(s1, s2);
		Condition = condition;
	}
	
	@Override
	public void recomputeSchema() 
	{
		super.recomputeSchema();
		
		this.mCurSchema = new Schema("");

        for (SchemaColumn sc : this.Source1.getSchema())
        {
            this.mCurSchema.add(new SchemaColumn(sc));
        }

        for (SchemaColumn sc : this.Source2.getSchema())
        {
            this.mCurSchema.add(new SchemaColumn(sc));
        }
	}
	
	private void SplitExpressions(Expression root, List<Expression> expressions)
    {
        if (root instanceof AndExpression)
        {
            SplitExpressions(((AndExpression)root).getLeftExpression(), expressions);
            SplitExpressions(((AndExpression)root).getRightExpression(), expressions);
        }
        else
        {
            expressions.add(root);
        }
    }

    private EquiJoinMapping CreateSingleMap(EqualsTo expr)
    {
        Column lhs = (Column)expr.getLeftExpression();
        Column rhs = (Column)expr.getRightExpression();

        String ltable = UtilityFunctions.getTableSafely(lhs);
        String rtable = UtilityFunctions.getTableSafely(rhs);
        
        int col1 = -1;
        int col2 = -1;
        
        try
        {
        	col1 = this.Source1.getSchema().findColumnIndex(ltable, lhs.getColumnName());
        	col2 = this.Source2.getSchema().findColumnIndex(rtable, rhs.getColumnName());
        }
        catch (SQLSymbolNotFoundException e)
        {
        	col1 = this.Source1.getSchema().findColumnIndex(rtable, rhs.getColumnName());
            col2 = this.Source2.getSchema().findColumnIndex(ltable, lhs.getColumnName());
        }

        return new EquiJoinMapping(col1, col2);
    }

    private List<EquiJoinMapping> createJoinMapping()
    {
        List<EquiJoinMapping> mapping = new ArrayList<EquiJoinMapping>();
        List<Expression> conditions = new ArrayList<Expression>();
        SplitExpressions(this.Condition, conditions);

        for (Expression e : conditions)
        {
            mapping.add(CreateSingleMap((EqualsTo)e));
        }

        return mapping;
    }
	
	@Override
	public LogicalOperator copy() 
	{
		return new LogicalJoinOp(this.Source1.copy(), this.Source2.copy(), this.Condition);
	}
	
	private SortDefinitionList getJoinSortList(List<EquiJoinMapping> mapping, boolean isLHS)
	{
		SortDefinitionList sdl = new SortDefinitionList();
		Schema schema = isLHS ? Source1.getSchema() : Source2.getSchema();
		
		for (int i = 0; i < mapping.size(); i++)
		{
			EquiJoinMapping m = mapping.get(i);
			
			SchemaColumn sc = isLHS ? schema.get(m.LHS) : schema.get(m.RHS);
			
			Column c = new Column();
			c.setColumnName(sc.getName());
			
			if (sc.getTableName() != "")
			{
				Table t = new Table();
				t.setName(sc.getTableName());
				c.setTable(t);
			}
			else
			{
				throw new RuntimeException("Ahhhh!");
			}
			
			sdl.add(new SortDefinition(c, true));
		}
		
		return sdl;
	}
	
	@Override
	public RelationalOperator toPhysicalOperator() 
	{
		List<EquiJoinMapping> mapping = createJoinMapping();
		
		if (SwapManager.SwapLocation.equals(""))
		{
			return new InMemoryHashJoin(this.Source1.toPhysicalOperator(), 
					this.Source2.toPhysicalOperator(), createJoinMapping());			
		}
		else
		{
			SortDefinitionList lhs = getJoinSortList(mapping, true);
			SortDefinitionList rhs = getJoinSortList(mapping, false);
			
			OutOfCoreSortOperator lhsOp = new OutOfCoreSortOperator(this.Source1.toPhysicalOperator(), lhs);
			OutOfCoreSortOperator rhsOp = new OutOfCoreSortOperator(this.Source2.toPhysicalOperator(), rhs);
			
			return new SortMergeJoin(lhsOp, rhsOp, mapping);
		}
		
	}

}
