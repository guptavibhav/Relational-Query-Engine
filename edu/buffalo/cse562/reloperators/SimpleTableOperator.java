package edu.buffalo.cse562.reloperators;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Date;
import java.util.List;

import edu.buffalo.cse562.Main;
import edu.buffalo.cse562.Factors.*;
import edu.buffalo.cse562.relations.FactorTuple;
import edu.buffalo.cse562.relations.Schema;
import edu.buffalo.cse562.relations.TableManager;
import edu.buffalo.cse562.relations.SchemaColumn;
import edu.buffalo.cse562.util.UtilityFunctions;

public class SimpleTableOperator extends RelationalOperator 
{
	/*
	 * A list of indexes to retrieve.  Set this if you know you only
	 * need certain fields
	 */
	public List<Integer> Indexes;
	
	private Schema mOrigSchema;
	
	public SimpleTableOperator(Schema s) 
	{
		this(s, null);
	}
	
	public SimpleTableOperator(Schema s, List<Integer> indexes)
	{
		mSchema = s;
		mOrigSchema = s;
		this.Indexes = indexes;
		setSchema();
	}

	@Override
	protected void setSchema() 
	{
		//There is nothing to do here.  The schema was passed in the constructor.
		
		if (Indexes != null && Indexes.size() > 0)
		{
			mSchema = new Schema();
			mSchema.setName(mOrigSchema.getName());
			
			for (int i = 0; i < Indexes.size(); i++)
			{
				mSchema.add(new SchemaColumn(mOrigSchema.get(Indexes.get(i))));
			}
		}
	}
	
	private String[] safeSplit(int size, String line)
	{
		
		if (line.charAt(line.length() - 1) != '|')
		{
			line += "|";
		}
		
		String[] arr = new String[size];
		
		for (int i = 0; i < size; i++)
			arr[i] = "";
		
		StringBuilder sb = new StringBuilder();
		int cur = 0;
		
		for (int i = 0; i < line.length(); i++)
		{
			if (line.charAt(i) == '|')
			{
				arr[cur++] = sb.toString();
				sb.delete(0, sb.length());
			}
			else
			{
				sb.append(line.charAt(i));
			}
		}
		
		return arr;
	}
	
	private Factor stringToFactor(String value, SchemaColumn c)
	{
		switch (c.getDataType())
		{
		case tpLong:
			return new LongFactor(Long.parseLong(value));
			
		case tpDouble:
			return new DoubleFactor(Double.parseDouble(value));
			
		case tpString:
			return new StringFactor(value);
			
		case tpDate:
			return new DateFactor(UtilityFunctions.stringToDate(value));
			
		case tpTime:
			System.err.println("Time values were not specified in the spec");
			Main.onError();
			
		case tpTimeStamp:
			System.err.println("Timestamp values were not specified in the spec");
			Main.onError();
			
		default:
			System.err.println("Invalid data type");
			Main.onError();
		}
		
		return null;
	}
	
	private FactorTuple stringToTuple(String line)
	{
		/*
		 * The conversion from String to Factor is very straight forward.
		 * We split the String at | and use the schema to determine which
		 * type of Factor it is. 
		 */
		
		FactorTuple t = new FactorTuple();
		
		//String[] values = line.split("\\|");
		//assert(values.length == mSchema.size());
		
		String[] values = safeSplit(mSchema.size(), line);
		
		for (int i = 0; i < mSchema.size(); i++)
		{
			SchemaColumn c = mSchema.get(i);
			t.add(stringToFactor(values[i], c));
		}
		
		return t;
	}
	
	private FactorTuple getTupleFromIndexes(String line)
	{
		FactorTuple t = new FactorTuple();
		
		String[] values = safeSplit(mSchema.size(), line);
		
		for (int i = 0; i < Indexes.size(); i++)
		{
			SchemaColumn c = mSchema.get(i);
			String value = values[Indexes.get(i)];
			t.add(stringToFactor(value, c));
		}
		
		return t;
	}
	
	@Override
	public void run() 
	{
		/*
		 * This is the only operator that doesn't read from a child operator.
		 * Here, we get our data from disk and fill the buffer with tuple until
		 * there are no more records on disk.
		 */
		
		File file = UtilityFunctions.findFileIgnoreCase(mSchema.getName() + ".dat", 
				TableManager.TableLocation);
		
		BufferedReader b;
		
		boolean doIndexes = (Indexes != null && Indexes.size() > 0);
		
		try 
		{
			b = new BufferedReader(new FileReader(file));
			String lineIn = null;
			while (!mStop && (lineIn = b.readLine()) != null)
			{
				if (doIndexes)
					this.mBuffer.enqueueTuple(getTupleFromIndexes(lineIn));
				else
					this.mBuffer.enqueueTuple(stringToTuple(lineIn));
			}
			
			this.mBuffer.markDone();
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

	@Override
	protected void reset() 
	{
		mBuffer.reset();
		mStop = false;
	}
}
