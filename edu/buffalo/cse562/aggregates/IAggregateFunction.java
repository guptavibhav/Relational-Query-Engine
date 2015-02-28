package edu.buffalo.cse562.aggregates;

import edu.buffalo.cse562.Factors.Factor;
import edu.buffalo.cse562.Factors.IFactor;

public interface IAggregateFunction extends IFactor 
{
	public void update(Factor f);
	public void reset();
}
