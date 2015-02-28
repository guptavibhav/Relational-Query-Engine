package edu.buffalo.cse562.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import edu.buffalo.cse562.Main;

/*
 *  It is a severe limitation of the JSQL parser that it can only handle one statement at a time.
 *  We SHOULD be able to give it a string containing multiple statements for which it should return
 *  statements until it doesn't have any left.  Otherwise, we need to tokenize the input to determine
 *  when we have another statement.
 * 
 *  This is worse given that we're provided with statements that DON'T end with ';', some of which are
 *  multiple lines.
 * 
 *  We'll go very simple here.  We'll spilt the input on the new line character and assemble SQL statements
 *  based on the first word.  if the first word is a statement start word, we'll assume a new statement is starting.
 *  We'll also take care of comments in the form of -- 
 */
public class SQLStatementLexer 
{	
	private ArrayList<String> lines = new ArrayList<String>();
	private ArrayList<String> statements = new ArrayList<String>();
	
	private int mCurIndex = 0;
	
	public SQLStatementLexer(String everything) 
	{
		splitLines(everything);
		createStatements();
	}
	
	private void splitLines(String everything)
	{
		ArrayList<String> temp = new ArrayList<String>();
		
		BufferedReader br = new BufferedReader(new StringReader(everything));
		String line = null;
		try {
			while ((line = br.readLine()) != null)
			{
				temp.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
			Main.onError();
		}
		
		//For now, assume "--" does not appear in strings
		//TODO: Fix this for checkpoint 2
		
		for (int i = 0; i < temp.size(); i++)
		{
			String cur = temp.get(i);
			int where = cur.indexOf("--");
			if (where == 0)
			{
				cur = "";
			}
			else if (where > 0)
			{
				cur = cur.substring(0, where);
			}
			
			if (cur.length() > 0)
				lines.add(cur);
		}
		
	}
	
	private void createStatements()
	{		
		boolean[] stmtStarts = new boolean[lines.size()];
		
		for (int i = 0; i < lines.size(); i++)
		{
			String s = lines.get(i);
			int end = s.indexOf(' ');
			if (end == -1)
				end = s.length();
			
			String firstWord = s.substring(0, end);
			if (firstWord.equalsIgnoreCase("create") ||
					firstWord.equalsIgnoreCase("select"))
			{
				stmtStarts[i] = true;
			}
			else
			{
				stmtStarts[i] = false;
			}
		}
		
		String curStatement = lines.get(0);
		
		for (int i = 1; i < lines.size(); i++)
		{
			if (stmtStarts[i])
			{
				statements.add(curStatement);
				curStatement = lines.get(i);
			}
			else
			{
				curStatement = curStatement + " " + lines.get(i);
			}
		}
		
		statements.add(curStatement);
		
	}
	
	public String getNextStatement()
	{
		if (mCurIndex >= statements.size())
			return null;
		else
			return statements.get(mCurIndex++);
	}

}
