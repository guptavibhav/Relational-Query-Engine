package edu.buffalo.cse562;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import edu.buffalo.cse562.relations.TableManager;
import edu.buffalo.cse562.tests.TableTester;
import edu.buffalo.cse562.tests.TestFunctionBuilder;
import edu.buffalo.cse562.tests.TestSchema;
import edu.buffalo.cse562.tests.TestSorterClasses;
import edu.buffalo.cse562.util.Checkpoint2StmtLexer;
import edu.buffalo.cse562.util.SQLStatementLexer;
import edu.buffalo.cse562.util.SwapManager;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

public class Main
{
	public static void onError()
	{
		System.err.println("An error has occurred, I think I'll just die...");
		System.exit(1);
	}
	
	private static void runTests()
	{
		new TestFunctionBuilder().Test();
    	//new TestSchema().Test();
    	
    	//TestSorterClasses.runTest();
    	
    	//new TableTester().runTests();
	}
	
	private static ArrayList<String> mSQLFiles;
	
	private static void mainLoop()
	{
		CCJSqlParserManager mgr = new CCJSqlParserManager();
		StatementVisitor visitor = new MainLoopStatementVisitor();
		
		//String root = TableManager.TableLocation + "/";
		
		for (String fileName : mSQLFiles)
		{
			try
			{
				BufferedReader b = new BufferedReader(new FileReader(fileName));
				String lineIn = null;
				String all = "";
				
				while ((lineIn = b.readLine()) != null)
				{
					all = all + lineIn + "\n";
				}
				
				Checkpoint2StmtLexer lexer = new Checkpoint2StmtLexer(all);
				
				String stmtString = null;
				while ((stmtString = lexer.getNextStatement()) != null)
				{
					Statement stmt;
					try 
					{
						stmt = mgr.parse(new StringReader(stmtString));
						stmt.accept(visitor);
					} 
					catch (JSQLParserException e) 
					{
						e.printStackTrace();
						Main.onError();
					}
					catch (Exception e)
					{
						e.printStackTrace();
						Main.onError();
					}
				}
				
				b.close();
			}
			catch (FileNotFoundException e) 
			{
				e.printStackTrace();
				Main.onError();
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
				Main.onError();
			}
		}
	}
	
	private static void printUsage()
	{
		System.out.println("Usage: ");
		System.out.println("\tjava cse.buffalo.cse562.main --swap <data_directory> [--swap <data_directory>] <sql_file> <sql_file> ...");
	}
	
    public static void main(String[] args) 
    {
    	if ((args.length < 3))
    	{
    		printUsage();
    		return;
    	}
    	
    	if (!args[0].equals("--data"))
    	{
    		printUsage();
    		return;
    	}
    	
    	TableManager.TableLocation = args[1];
    	
    	int curArg = 2;
    	if (args[curArg].equals("--swap"))
    	{
    		if (args.length < 4)
    		{
    			printUsage();
    			return;
    		}
    		
    		curArg++;
    		String swapDir = args[curArg++];
    		SwapManager.SwapLocation = swapDir;
    	}
    	
    	mSQLFiles = new ArrayList<String>();
    	
    	for (int i = curArg; i < args.length; i++)
    	{
    		mSQLFiles.add(args[i]);
    	}
    	
    	//Uncomment this if you want to run specific tests
    	//runTests();
    	
    	mainLoop();
	}
} 
