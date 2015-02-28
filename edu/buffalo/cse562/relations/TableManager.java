package edu.buffalo.cse562.relations;

import java.util.ArrayList;
import java.util.Hashtable;

import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;

public class TableManager 
{
	private static TableManager mInstance = null;
	public static String TableLocation = "";
	
	public static TableManager getInstance()
	{
		if (mInstance == null)
			mInstance = new TableManager();
		
		return mInstance;
	}
	
	/*
	 * For the amount of accesses, an ArrayList is fine for this.
	 */
	private ArrayList<Schema> mTables;
	
	private Hashtable<String, ColumnDataTypes> mTypeMap;
	
	private TableManager() 
	{
		buildMap();
		mTables = new ArrayList<Schema>();
	}
	
	private void buildMap()
	{
		mTypeMap = new Hashtable<String, ColumnDataTypes>();
		
		/*
		 * For now, we map everything to JSQL factor types.  For future checkpoints,
		 * we may need to change this.
		 */
		
		//Integers
		mTypeMap.put("int", ColumnDataTypes.tpLong);
		mTypeMap.put("integer", ColumnDataTypes.tpLong);
		mTypeMap.put("smallint", ColumnDataTypes.tpLong);
		mTypeMap.put("tinyint", ColumnDataTypes.tpLong);
		mTypeMap.put("mediumint", ColumnDataTypes.tpLong);
		mTypeMap.put("bigint", ColumnDataTypes.tpLong);
		
		//Floats
		mTypeMap.put("double", ColumnDataTypes.tpDouble);
		mTypeMap.put("float", ColumnDataTypes.tpDouble);
		mTypeMap.put("decimal", ColumnDataTypes.tpDouble);
		
		//String
		mTypeMap.put("string", ColumnDataTypes.tpString);
		mTypeMap.put("char", ColumnDataTypes.tpString);
		mTypeMap.put("varchar", ColumnDataTypes.tpString);
		mTypeMap.put("string", ColumnDataTypes.tpString);
		
		//Date/Time
		mTypeMap.put("date", ColumnDataTypes.tpDate);
		mTypeMap.put("time", ColumnDataTypes.tpTime);
		mTypeMap.put("timestamp", ColumnDataTypes.tpTimeStamp);
	}
	
	public void addTable(CreateTable tableDef)
	{
		String tableName = tableDef.getTable().getName();
		
		//Ignore consecutive create table statements
		//TODO: Is this OK?  I'm doing this because we don't need to do this twice for multiple schemas
		
		if (getTable(tableName) != null)
			return;
		
		Schema s = new Schema(tableName);
		
		for (Object o : tableDef.getColumnDefinitions())
		{
			ColumnDefinition c = (ColumnDefinition)o;
			
			String dataType = c.getColDataType().getDataType().toLowerCase();
			ColumnDataTypes type = ColumnDataTypes.tpString;
			
			if (mTypeMap.containsKey(dataType))
			{
				type = mTypeMap.get(dataType);
			}
			else
			{
				throw new RuntimeException("Cannot find data type!! : " + dataType);
			}
			
			SchemaColumn col = new SchemaColumn(tableName, c.getColumnName(), type);
			s.add(col);
		}
		
		mTables.add(s);
	}

	public void clearAllTables()
	{
		mTables.clear();
	}
	
	public Schema getTable(String name)
	{
		for (int i = 0; i < mTables.size(); i++)
		{
			if (mTables.get(i).getName().equalsIgnoreCase(name))
				return mTables.get(i);
		}
		
		return null;
	}
	
}
