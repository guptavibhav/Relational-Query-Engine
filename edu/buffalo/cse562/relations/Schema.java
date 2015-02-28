package edu.buffalo.cse562.relations;

import java.util.ArrayList;
import java.util.HashMap;

import edu.buffalo.cse562.Exceptions.SQLAmbiguousNameException;
import edu.buffalo.cse562.Exceptions.SQLSymbolNotFoundException;

public class Schema extends ArrayList<SchemaColumn> 
{	
	//This is reaaaaally ugly since these are duped in ColumnDataTypes,
	//but we need this for speed and can't change everything right now...
	
	public static final int TYPE_LONG = 0;
	public static final int TYPE_DOUBLE = 1;
	public static final int TYPE_STRING = 2;
	public static final int TYPE_BOOLEAN = 3;
	public static final int TYPE_NULL = 4;
	public static final int TYPE_DATE = 5;
	
	private static final long serialVersionUID = 1L;

	//This needs to be set for built-in tables or temp tables, and (most?) derived tables
	private String mName = "";
	
	private class VariableLookup
	{
		public int Count = 0;
		public int Position = 0;
		
		public VariableLookup(int pos)
		{
			Position = pos; 
			Count = 1;
		}
	}
	
	private HashMap<String, VariableLookup> mFullNames = new HashMap<String, Schema.VariableLookup>();
	private HashMap<String, VariableLookup> mColumnNames = new HashMap<String, Schema.VariableLookup>();
	
	public Schema() 
	{
	}
	
	public Schema(String name)
	{
		mName = name;
	}
	
	public Schema(Schema copyThis)
	{
		this.mName = copyThis.mName;
		
		for (SchemaColumn c : copyThis)
		{
			this.add(new SchemaColumn(c));
		}
	}
	
	@Override
	public boolean add(SchemaColumn sc) 
	{
		super.add(sc);
		
		String colName = sc.mName;
        String fullName = sc.mTableName != null && !sc.mTableName.equals("") ? sc.mTableName + "." + sc.mName : sc.mName;

        colName = colName.toUpperCase();
        fullName = fullName.toUpperCase();
        VariableLookup vl;

        vl = mFullNames.get(fullName);
        
        if (vl != null)
        {
            vl.Count++;
        }
        else
        {
            mFullNames.put(fullName, new VariableLookup(this.size() - 1));
        }

        vl = mColumnNames.get(colName);
        
        if (vl != null)
        {
            vl.Count++;
        }
        else
        {
            mColumnNames.put(colName, new VariableLookup(this.size() - 1));
        }
        
        return true;
	}
	
	public String getName()
	{
		return mName;
	}
	
	public void setName(String name)
	{
		mName = name;
		
		ArrayList<SchemaColumn> copy = new ArrayList<SchemaColumn>();
        copy.addAll(this);
        this.clear();
        mFullNames.clear();
        mColumnNames.clear();

        for (SchemaColumn sc : copy)
        {
        	sc.mTableName = mName;
            this.add(sc);
        }
	}
	
	public SchemaColumn findColumn(SchemaColumn c) throws SQLSymbolNotFoundException, SQLAmbiguousNameException
	{
		return findColumn(c.getTableName(), c.getName());
	}
	
	public SchemaColumn findColumn(String colName) throws SQLSymbolNotFoundException, SQLAmbiguousNameException
	{
		int index = findColumnIndex("", colName);
		return this.get(index);
	}

	public SchemaColumn findColumn(String tableName, String colName) throws SQLSymbolNotFoundException, SQLAmbiguousNameException
	{
		int index = findColumnIndex(tableName, colName);
		return this.get(index);
	}
	
	public int findColumnIndex(String colName) throws SQLSymbolNotFoundException, SQLAmbiguousNameException
	{
		return findColumnIndex("", colName);
	}
	
	public int findColumnIndex(String tableName, String colName) throws SQLSymbolNotFoundException, SQLAmbiguousNameException
	{
		boolean checkTable = false;
		String fullName = colName;
		
		if (tableName != null && !tableName.equals(""))
		{
			checkTable = true;
			fullName = tableName + "." + colName;
		}
		
		fullName = fullName.toUpperCase(); 
		
		VariableLookup vl;
		
		if (checkTable)
			vl = mFullNames.get(fullName);
        else
            vl = mColumnNames.get(fullName);
		
		if (vl == null)
			throw new SQLSymbolNotFoundException(fullName);
		else if (vl.Count > 1)
			throw new SQLSymbolNotFoundException(fullName);
		else
			return vl.Position;
	}
	
	public boolean contains(String tableName, String colName)
	{
		boolean checkTable = false;
		String fullName = colName;
		
		if (tableName != null && !tableName.equals(""))
		{
			checkTable = true;
			fullName = tableName + "." + colName;
		}
		
		fullName = fullName.toUpperCase(); 
		
		VariableLookup vl;
		
		if (checkTable)
			vl = mFullNames.get(fullName);
        else
            vl = mColumnNames.get(fullName);
		
		if (vl == null)
			return false;
		else 
			return true;
	}
}
