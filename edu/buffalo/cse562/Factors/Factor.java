package edu.buffalo.cse562.Factors;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;

import edu.buffalo.cse562.Exceptions.SQLTypeMismatchException;
import edu.buffalo.cse562.relations.Schema;

/**
 * As a class, this is mostly a marker class for our base data types.
 * It's a bit of misuse of the word factor, but close enough.
 * 
 * This also implements the standard mathematical operations for factors
 * 
 * @author scott
 *
 */
public abstract class Factor implements Comparable<Factor>, Serializable
{
	public static Factor createFromFile(DataInputStream ds) throws IOException
	{
		int type = ds.readInt();
		
		switch (type)
		{
		case Schema.TYPE_BOOLEAN:
			return new BooleanFactor(ds);

		case Schema.TYPE_DATE:
			return new DateFactor(ds);

		case Schema.TYPE_DOUBLE:
			return new DoubleFactor(ds);

		case Schema.TYPE_LONG:
			return new LongFactor(ds);

		case Schema.TYPE_NULL:
			return new NullFactor(ds);

		case Schema.TYPE_STRING:
			return new StringFactor(ds);
			
		default:
			return null;
		}
		
	}
	
	private static enum MathOps
	{
		Add,
		Subtract,
		Multiply,
		Divide
	}
	
	private static enum BoolOps
	{
		And,
		Or
	}
	
	private static enum EqualityOps
	{
		LessThan,
		LessThanEqual,
		GreaterThan,
		GreaterThanEqual,
		Equal,
		NotEqual
	}
	
	public abstract  void toFile(DataOutputStream ds) throws IOException;
	
	/************    Factor Operations    ********/
	
	private static Factor commonArithmetic(Factor f1, Factor f2, MathOps op)
	{
		/*
		 * Allowed:
		 * 		long op long
		 * 		double op double
		 * 		long op double		=> cast to double
		 * 		double op long		=> cast to double
		 */
		
		if (f2 instanceof ListFactor)
		{
			ListFactor list = (ListFactor)f2;
			if (list.size() != 1)
			{
				throw new SQLTypeMismatchException("Cannot apply math operations to lists of size <> 1");
			}
			f2 = list.get(0);
		}
		
		if (f1 instanceof LongFactor && f2 instanceof LongFactor)
		{
			//This is the only case that returns a long
			
			long lv1 = ((LongFactor)f1).getValue();
			long lv2 = ((LongFactor)f2).getValue();
			long res = 0;
			
			switch (op)
			{
			case Add:
				res = lv1 + lv2;
				break;
			
			case Subtract:
				res = lv1 - lv2;
				break;
			
			case Multiply:
				res = lv1 * lv2;
				break;
			
			case Divide:
				res = lv1 / lv2;
				break;
			}
			
			return new LongFactor(res);
		}
		else 
		{
			//All other cases will return a double
			
			double dv1 = 0;
			double dv2 = 0;
			
			if (f1 instanceof LongFactor && f2 instanceof DoubleFactor)
			{
				dv1 = (double)((LongFactor)f1).getValue();
				dv2 = ((DoubleFactor)f2).getValue();	
			}
			else if (f1 instanceof DoubleFactor && f2 instanceof LongFactor)
			{
				dv1 = ((DoubleFactor)f1).getValue();
				dv2 = (double)((LongFactor)f2).getValue();
			}
			else if (f1 instanceof DoubleFactor && f2 instanceof DoubleFactor)
			{
				dv1 = ((DoubleFactor)f1).getValue();
				dv2 = ((DoubleFactor)f2).getValue();
			}
			else
			{
				throw new SQLTypeMismatchException();
			}
			
			double res = 0;
			BigDecimal bdv1 = new BigDecimal(dv1);
			BigDecimal bdv2 = new BigDecimal(dv2);
			
			switch (op)
			{
			case Add:
				res = dv1 + dv2;
				break;
			
			case Subtract:
				res = dv1 - dv2;
				break;
			
			case Multiply:
				res = dv1 * dv2;
				break;
			
			case Divide:
				res = dv1 / dv2;
				break;
			}
			
			res = Math.round(res * 1000000) / 1000000.0;
			
			return new DoubleFactor(res);
		}
	}
	
	private static Factor commonBooleanOp(Factor f1, Factor f2, BoolOps op)
	{
		if (!(f1 instanceof BooleanFactor) || !(f2 instanceof BooleanFactor))
		{
			throw new SQLTypeMismatchException();
		}
		
		boolean a = ((BooleanFactor)f1).getValue();
		boolean b = ((BooleanFactor)f2).getValue();
		
		switch (op)
		{
		case And:
			return new BooleanFactor(a && b);
		case Or:
			return new BooleanFactor(a || b);
		default:
			return new BooleanFactor(false);
		}
	}
	
	private static Factor commonEqualityTests(Factor f1, Factor f2, EqualityOps op)
	{
		if (f2 instanceof ListFactor)
		{
			ListFactor list = (ListFactor)f2;
			if (list.size() != 1)
			{
				throw new SQLTypeMismatchException("Cannot apply equality tests to lists of size <> 1");
			}
			f2 = list.get(0);
		}
		
		int compVal = f1.compareTo(f2);
		boolean result = false;
		
		if (compVal > 0)
		{
			if (op == EqualityOps.GreaterThan || op == EqualityOps.GreaterThanEqual ||
					op == EqualityOps.NotEqual)
				result = true;
		}
		else if (compVal < 0)
		{
			if (op == EqualityOps.LessThan || op == EqualityOps.LessThanEqual ||
					op == EqualityOps.NotEqual)
				result = true;
		}
		else
		{
			if (op == EqualityOps.LessThanEqual || op == EqualityOps.GreaterThanEqual ||
					op == EqualityOps.Equal)
				result = true;
		}
		
		return new BooleanFactor(result);
		
	}
	
	public static Factor Add(Factor f1, Factor f2)
	{	
		return commonArithmetic(f1, f2, MathOps.Add);
	}
	
	public static Factor Subtract(Factor f1, Factor f2)
	{
		return commonArithmetic(f1, f2, MathOps.Subtract);
	}
	
	public static Factor Divide(Factor f1, Factor f2)
	{
		return commonArithmetic(f1, f2, MathOps.Divide);
	}
	
	public static Factor Multiply(Factor f1, Factor f2)
	{
		return commonArithmetic(f1, f2, MathOps.Multiply);
	}
	
	public static Factor And(Factor f1, Factor f2)
	{
		return commonBooleanOp(f1, f2, BoolOps.And);
	}
	
	public static Factor Or(Factor f1, Factor f2)
	{
		return commonBooleanOp(f1, f2, BoolOps.Or);
	}
	
	public static Factor LessThan(Factor f1, Factor f2)
	{
		return commonEqualityTests(f1, f2, EqualityOps.LessThan);
	}
	
	public static Factor LessThanEqual(Factor f1, Factor f2)
	{
		return commonEqualityTests(f1, f2, EqualityOps.LessThanEqual);
	}
	
	public static Factor GreaterThan(Factor f1, Factor f2)
	{
		return commonEqualityTests(f1, f2, EqualityOps.GreaterThan);
	}
	
	public static Factor GreaterThanEqual(Factor f1, Factor f2)
	{
		return commonEqualityTests(f1, f2, EqualityOps.GreaterThanEqual);
	}
	
	public static Factor EqualOp(Factor f1, Factor f2)
	{
		return commonEqualityTests(f1, f2, EqualityOps.Equal);
	}
	
	public static Factor NotEqualOp(Factor f1, Factor f2)
	{
		return commonEqualityTests(f1, f2, EqualityOps.NotEqual);
	}
}
