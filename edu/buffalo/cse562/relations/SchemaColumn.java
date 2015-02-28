package edu.buffalo.cse562.relations;

public class SchemaColumn
{
	protected String mName = "";
	protected String mTableName = "";
	protected ColumnDataTypes mDataType;
	
	public SchemaColumn(String tableName, String colName, ColumnDataTypes type) 
	{
		this.mName = colName;
		this.mTableName = tableName == null ? "" : tableName;
		this.mDataType = type;
	}
	
	public SchemaColumn(SchemaColumn copyThis)
	{
		this.mName = copyThis.mName;
		this.mTableName = copyThis.mTableName == null ? "" : copyThis.mTableName;
		
		if (this.mTableName == null)
			mTableName = "";
		
		this.mDataType = copyThis.mDataType;
	}
	
	public String getName()
	{
		return mName;
	}
	
	public void setName(String name)
	{
		mName = name;
	}
	
	public String getTableName()
	{
		return mTableName;
	}
	
	public void setTableName(String name)
	{
		if (name == null)
			name = "";
		else
			mTableName = name;
	}

	public ColumnDataTypes getDataType()
	{
		return mDataType;
	}
	
	public void SetDataType(ColumnDataTypes type)
	{
		mDataType = type;
	}
	
	@Override
	
	public boolean equals(Object obj) 
	{
		if (obj instanceof SchemaColumn)
		{
			SchemaColumn other = (SchemaColumn)obj;
			if (this.mTableName.equals("") || other.mTableName.equals(""))
			{
				return mTableName.equals(other.mTableName);
			}
			else
			{
				return (mTableName.equals(other.mTableName) && mName.equals(other.mName));
			}
		}
		else
		{
			return false;
		}
	}
	
	@Override
	public String toString() 
	{
		if (mTableName != null && !mTableName.equals(""))
			return mTableName + "." + mName;
		else
			return mName;
	}

}
