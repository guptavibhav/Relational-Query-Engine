package edu.buffalo.cse562.util;

public class SwapManager 
{
	public static String SwapLocation = "";
	
	private static int mNextIndex = 0;
	
	public static synchronized int getNextSwapIndex()
	{
		int temp = mNextIndex++;
		return temp;
	}
	
	private SwapManager() 
	{
		
	}

}
