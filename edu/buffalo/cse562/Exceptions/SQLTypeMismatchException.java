package edu.buffalo.cse562.Exceptions;

public class SQLTypeMismatchException extends RuntimeException 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public SQLTypeMismatchException()
	{
	}
	
	public SQLTypeMismatchException(String message)
	{
		super(message);
	}
}
