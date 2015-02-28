package edu.buffalo.cse562.tests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.buffalo.cse562.Factors.DoubleFactor;
import edu.buffalo.cse562.Factors.Factor;
import edu.buffalo.cse562.relations.FactorTuple;
import edu.buffalo.cse562.util.SortFactor;
import edu.buffalo.cse562.util.SortableTuple;

public class TestSorterClasses 
{
	public static void runTest()
	{
		ArrayList<SortableTuple> factors = new ArrayList<SortableTuple>();
		
		for (int i = 0; i < 100; i++)
		{
			FactorTuple t = new FactorTuple();
			
			Factor f1 = new DoubleFactor(Math.random() * 10000);
			Factor f2 = new DoubleFactor(Math.random() * 10000);
			
			t.add(f1);
			t.add(f2);
			
			List<SortFactor> keys = new ArrayList<SortFactor>();
			keys.add(new SortFactor(f1, true));
			factors.add(new SortableTuple(keys, t));
		}
		
		Collections.sort(factors);
		
		for (int i = 0; i < 100; i++)
		{
			String s = factors.get(i).mData.get(0).toString() + ", " + 
					factors.get(i).mData.get(1).toString();
			
			System.out.println(s);
		}
		
	}
}
