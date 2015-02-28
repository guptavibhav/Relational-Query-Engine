package edu.buffalo.cse562.ExprTree;

import java.util.List;

import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.Union;
import edu.buffalo.cse562.Factors.Factor;
import edu.buffalo.cse562.conversion.Checkpoint1Conversion;
import edu.buffalo.cse562.conversion.OuterSchemaVariableFinder;
import edu.buffalo.cse562.conversion.QueryToLogicalTree;
import edu.buffalo.cse562.logicaloperators.LogicalOperator;
import edu.buffalo.cse562.reloperators.RelationalOperator;
import edu.buffalo.cse562.reloperators.SubSelectOperator;
import edu.buffalo.cse562.reloperators.VariableContext;

public class SubSelectOpNode extends ExpressionNode implements SelectVisitor
{
	private SubSelectOperator mOperator;
	private boolean mCurVal = false;
	private VariableContext mOuterContext;
	
	public SubSelectOpNode(VariableContext curContext, SelectBody select) 
	{
		//Create the outer context which is the current inner context
		//mOuterContext = new VariableContext(curContext.ContextSchema);
		mOuterContext = curContext;
		
		//Look for variable references to the outer context 
		select.accept(this);
		
		LogicalOperator root = new QueryToLogicalTree().convert(select, mOuterContext);
		RelationalOperator source = root.toPhysicalOperator();
		mOperator = new SubSelectOperator(source, !mCurVal);
	}
	
	@Override
	public Factor ToFactor() 
	{
		return mOperator.ToFactor();
	}

	@Override
	public void visit(PlainSelect arg0) 
	{
		if (mCurVal)
			return;
		
		OuterSchemaVariableFinder osv = new OuterSchemaVariableFinder(mOuterContext);
		
		for (int i = 0; i < arg0.getSelectItems().size(); i++)
		{
			SelectItem si = (SelectItem)arg0.getSelectItems().get(0);
			if (si instanceof SelectExpressionItem)
			{
				mCurVal = osv.HasOuterVariable(((SelectExpressionItem) si).getExpression(), false);
				
				if (mCurVal)
					return;
			}
		}
		
		if (arg0.getWhere() != null)
		{
			mCurVal = osv.HasOuterVariable(arg0.getWhere(), false);
			
			if (mCurVal)
				return;
		}
		
		if (arg0.getHaving() != null)
		{
			mCurVal = osv.HasOuterVariable(arg0.getHaving(), false);
			
			if (mCurVal)
				return;
		}
		
	}

	@Override
	public void visit(Union arg0) 
	{
		if (mCurVal)
			return;
		
		List l = arg0.getPlainSelects();
		
		for (int i = 0; i < l.size(); i++)
		{
			visit((PlainSelect)l.get(i));
			
			if (mCurVal)
				return;
		}
	}

}
