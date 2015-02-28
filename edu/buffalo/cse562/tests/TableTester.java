package edu.buffalo.cse562.tests;

import java.io.StringReader;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.replace.Replace;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;
import edu.buffalo.cse562.conversion.Checkpoint1Conversion;
import edu.buffalo.cse562.relations.FactorTuple;
import edu.buffalo.cse562.relations.Schema;
import edu.buffalo.cse562.relations.SchemaColumn;
import edu.buffalo.cse562.relations.TableManager;
import edu.buffalo.cse562.reloperators.PrintOperator;
import edu.buffalo.cse562.reloperators.RelationalOperator;
import edu.buffalo.cse562.reloperators.SimpleTableOperator;

public class TableTester implements StatementVisitor
{	
	public void runTests()
	{
		try 
		{
			CCJSqlParserManager mgr = new CCJSqlParserManager();
			
			//TableManager.TableLocation = "/Users/scott/Projects/Database/cse562-project/team18/data/personInfo";
			TableManager.TableLocation = "/home/scott/projects/cse562/team18/data/personInfo";
			
			String create = "CREATE TABLE personInfo(id int, first_name string, last_name string, Gender string, age int, State string)";
			
			Statement stmt;
			try 
			{
				stmt = mgr.parse(new StringReader(create));
				stmt.accept(this);
				
				//Schema s = TableManager.getInstance().getTable("personInfo");
				//SimpleTableOperator op = new SimpleTableOperator(s);
				//new PrintOperator(op).startQuery();
				
				//String select = "SELECT id, first_name, last_name, age from personInfo where age>=30";
				//String select = "SELECT id, age from personInfo where age>=30 AND id < 50";
				//String select = "SELECT count(age), avg(age) from personInfo where age>=30";
				//String select = "SELECT id, first_name, last_name, age from personInfo where age>=30 order by age";
				//String select = "SELECT age, count(age) from personInfo where age>=30 group by age";
				//String select = "SELECT age from personInfo where age>=30 order by age";
				//String select = "SELECT age, count(age) from personInfo where age>=30 group by age having count(age) > 7";
				
				//String select = "SELECT age, count(age) from personInfo where age >= 30 group by age";
				
				//String select = "SELECT 1 + avg(age) from personInfo where age >= 30";
				
				//String select = "SELECT id, first_name, last_name, age from personInfo where age>=30 order by age";
				
				//String select = "SELECT age, count(age) from personInfo where age >= 30 AND count(age) > 7";
				
				//String select = "SELECT age, count(age) from personInfo where age>=30 group by age having count(age) > 7";
				
				//String select = "SELECT * from personInfo, personInfo";
				
				String select = "SELECT P1.*, P2.* from personInfo P1 JOIN personInfo P2 ON P1.id = P2.id";
				//String select = "SELECT age, count(*) from personInfo where age>=30 group by age having count(*) > 7";	
				
				stmt = mgr.parse(new StringReader(select));
				stmt.accept(this);
				
			} 
			catch (JSQLParserException e) 
			{
				System.err.println("Syntax Error! Message: " + e.getMessage());
			}
			catch (Exception e)
			{
				System.err.println("Error! Message: " + e.getMessage());
			}
			
		} 
		catch (Exception e)
		{
			System.err.println("Error! Message: " + e.getMessage());
		}
	}

	@Override
	public void visit(Select arg0) 
	{
		Checkpoint1Conversion converter = new Checkpoint1Conversion();
		RelationalOperator op = converter.convert(arg0.getSelectBody(), null); 
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
