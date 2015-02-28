package edu.buffalo.cse562.ExprTree;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.buffalo.cse562.Exceptions.SQLTypeMismatchException;
import edu.buffalo.cse562.Factors.BooleanFactor;
import edu.buffalo.cse562.Factors.Factor;
import edu.buffalo.cse562.Factors.StringFactor;

public class LikeOpNode extends ExpressionNode
{
	private ExpressionNode mLHS;
	private Pattern mPattern;
	
	public LikeOpNode(ExpressionNode lhs, String like) 
	{
		mLHS = lhs;
		like = like.toUpperCase();
		mPattern = Pattern.compile(convertToRegEx(like));
	}

	@Override
	public Factor ToFactor() 
	{
		Factor f = mLHS.ToFactor();
		if (!(f instanceof StringFactor))
			throw new SQLTypeMismatchException("LHS Expression in LIKE not a String");
		
		String lhs = ((StringFactor)f).getValue();
		lhs = lhs.toUpperCase();
		
		Matcher m = mPattern.matcher(lhs);
		return new BooleanFactor(m.find());
	}
	
	private String convertToRegEx(String s)
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append('^');	//Beginning of line
		
		boolean quoteStarted = false;
		
		for (int i = 0; i < s.length(); i++)
		{
			char c = s.charAt(i);
			
			switch (c)
			{
			case '%':
			case '_':
				if (quoteStarted)
				{
					quoteStarted = false;
					sb.append("\\E");
				}
				if (c == '%')
					sb.append(".*");
				else
					sb.append('.');
				break;
			
			default:
				if (!quoteStarted)
				{
					quoteStarted = true;
					sb.append("\\Q");
				}
				sb.append(c);
				break;
			}
		}
		
		if (quoteStarted)
			sb.append("\\E");
		
		sb.append('$');		//End of line
		
		return sb.toString();
	}

}
