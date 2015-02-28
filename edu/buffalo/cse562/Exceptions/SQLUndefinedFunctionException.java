package edu.buffalo.cse562.Exceptions;

public class SQLUndefinedFunctionException extends RuntimeException 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public SQLUndefinedFunctionException()
	{
	}
	
	public SQLUndefinedFunctionException(String message)
	{
		super(message);
	}
}
