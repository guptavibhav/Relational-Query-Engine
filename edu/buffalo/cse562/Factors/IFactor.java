package edu.buffalo.cse562.Factors;

/**
 * Anything implementing this interface can return a Factor representation of itself.  
 * For example, Expression trees, variables, and some sub-queries can be converted to factors.
 * 
 * It's a bit of a misnomer, but we mean this to be something that can be converted to
 * a value
 * 
 * @author scott
 *
 */
public interface IFactor 
{
	public Factor ToFactor();
}
