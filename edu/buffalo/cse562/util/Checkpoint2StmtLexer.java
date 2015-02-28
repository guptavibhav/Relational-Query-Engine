package edu.buffalo.cse562.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import edu.buffalo.cse562.Main;

public class Checkpoint2StmtLexer 
{
	private ArrayList<String> lines = new ArrayList<String>();
	private ArrayList<String> statements = new ArrayList<String>();
	
	private int mCurIndex = 0;
	
	public Checkpoint2StmtLexer(String everything) 
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
		
		for (int i = 0; i < lines.size() - 1; i++)
		{
			String s = lines.get(i).trim();
			if (s.endsWith(";"))
			{
				stmtStarts[i + 1] = true;
			}
			else
			{
				stmtStarts[i + 1] = false;
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
