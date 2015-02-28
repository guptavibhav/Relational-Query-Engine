package edu.buffalo.cse562.Exceptions;

/*
 * These should really be normal exceptions, but we want them to easily
 * propagate back to the main execution loop so we'll just *remember* to catch
 * them instead of forcing it everywhere along the call path
 */
public class SQLSymbolNotFoundException extends RuntimeException 
{
	private static final long serialVersionUID = 1L;

	public SQLSymbolNotFoundException(String symbol) 
	{
		super("The symbol name "  + symbol + " cannot be resolved");
	}
}
