package edu.buffalo.cse562;

import edu.buffalo.cse562.conversion.Checkpoint1Conversion;
import edu.buffalo.cse562.conversion.QueryToLogicalTree;
import edu.buffalo.cse562.logicaloperators.LogicalOperator;
import edu.buffalo.cse562.relations.TableManager;
import edu.buffalo.cse562.reloperators.PrintOperator;
import edu.buffalo.cse562.reloperators.RelationalOperator;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.replace.Replace;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;

public class MainLoopStatementVisitor implements StatementVisitor 
{

	@Override
	public void visit(Select arg0) 
	{
		RelationalOperator op;
		
		LogicalOperator root = new QueryToLogicalTree().convert(arg0.getSelectBody(), null);
		op = root.toPhysicalOperator();
		
		//Checkpoint1Conversion converter = new Checkpoint1Conversion();
		//op = converter.convert(arg0.getSelectBody(), null); 
		
		new PrintOperator(op).startQuery();
	}

	@Override
	public void visit(Delete arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Update arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Insert arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Replace arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Drop arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Truncate arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(CreateTable arg0) 
	{
		TableManager tm = TableManager.getInstance();
		tm.addTable(arg0);
	}

}
