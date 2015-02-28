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
import edu.buffalo.cse562.relations.SchemaColumn;
import edu.buffalo.cse562.relations.TableManager;
import edu.buffalo.cse562.reloperators.RelationalOperator;

public class TestSchema implements StatementVisitor
{
	public void Test()
	{
		try 
		{
			CCJSqlParserManager mgr = new CCJSqlParserManager();
			
			TableManager.TableLocation = ""; 	//Set based on args
			
			String create = "CREATE TABLE personInfo(id int, first_name string, last_name string, Gender string, age int, State string)";
			String[] selects = 
				{
					"SELECT id, first_name, last_name, age from personInfo where age>=30",
					"SELECT id, first_name as FIRST, last_name AS LAST, age from personInfo P where age>=30",
					"SELECT avg(age) as AVG, last_name from personInfo P where age>=30 group by last_name",
					"SELECT * from personInfo"
				};
			
			Statement stmt;
			try 
			{
				stmt = mgr.parse(new StringReader(create));
				stmt.accept(this);
				
				for (int i = 0; i < selects.length; i++)
				{
					stmt = mgr.parse(new StringReader(selects[i]));
					stmt.accept(this);
				}
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
		
		System.out.println("SCHEMA: ");
		for (SchemaColumn c : op.getSchema())
		{
			System.out.println("\t" + c.toString());
		}
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
