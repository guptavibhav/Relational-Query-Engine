package edu.buffalo.cse562.util;

import java.io.File;
import java.sql.Date;

import net.sf.jsqlparser.schema.Column;

public class UtilityFunctions 
{
	public static File findFileIgnoreCase(String filename, String path)
	{
		File parent = new File(path);
		String[] contents = parent.list();
		for (int i = 0; i < contents.length; i++)
		{
			if (contents[i].equalsIgnoreCase(filename))
			{
				return new File(path + "/" + contents[i]);
			}
		}
		
		return null;
	}
	
	public static Date stringToDate(String s)
	{
		String temp = s;
		if (temp.startsWith("'"))
		{
			temp = temp.substring(1);
		}
		
		if (temp.endsWith("'"))
		{
			temp = temp.substring(0, temp.length() - 1);
		}
		
		String[] values = temp.split("\\-");
		assert(values.length == 3);
		
		int year = Integer.parseInt(values[0]) - 1900;
		int month = Integer.parseInt(values[1]) - 1;
		int day = Integer.parseInt(values[2]);
		
		return new Date(year, month, day);
	}
	
	public static String getTableSafely(Column c)
	{
		String table = "";
		if (c.getTable() != null && c.getTable().getName() != null)
			table = c.getTable().getName();
		
		return table;
	}

}
