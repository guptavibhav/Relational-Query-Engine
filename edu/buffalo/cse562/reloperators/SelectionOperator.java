package edu.buffalo.cse562.reloperators;

import net.sf.jsqlparser.expression.Expression;
import edu.buffalo.cse562.ExprTree.ExpressionTree;
import edu.buffalo.cse562.Factors.BooleanFactor;
import edu.buffalo.cse562.buffers.ITupleBuffer;
import edu.buffalo.cse562.relations.FactorTuple;
import edu.buffalo.cse562.relations.Schema;

/*
 * Selection: Select a subset of the input rows
 *
 * This is a WHERE or HAVING clause.
 * 
 * 
 * This operator works as follows:
 * 		Retrieve a tuple from the source
 * 		Populate the current value in the schema
 * 		
 */
public class SelectionOperator extends SingleSourceOperator 
{
	/*
	 * Original JSQL expression
	 */
	protected Expression mJsqlExpr;
	
	/*
	 * Converted, executable expression (set during init)
	 */
	protected ExpressionTree mExprTree;
	
	protected QueryContext mContext;

	public SelectionOperator(RelationalOperator source, Expression boolExpr, VariableContext outerContext) 
	{
		super(source);
		mJsqlExpr = boolExpr;
		setSchema();
		
		mContext = new QueryContext();
		mContext.CurrentContext = new VariableContext(this.mSchema);
		mContext.OuterQueryContext = outerContext;
	}
	
	@Override
	protected void setSchema() 
	{
		//Our schema is the same as our source's schema since all we do is filter tuples
		mSchema = new Schema(mSource.getSchema());
	}

	private void init() 
	{
		/*
		 * For our initialization, we need to convert JSQL's expression tree
		 * to one that can be evaluated.  This can only be done after the schema
		 * is set.
		 * 
		 * This links against our schema
		 */
		mExprTree = new ExpressionTree(mJsqlExpr, mContext);
	}
	
	/*
	 * Process a tuple that corresponds to our schema.  Our job is to prune tuples
	 * that don't pass the boolean test.
	 */
	private void processFullTuple(FactorTuple tuple)
	{
		//Populate the variables in the schema that the expression
		//is linked against
		mContext.CurrentContext.CurrentValues = tuple;
		
		BooleanFactor f = (BooleanFactor) mExprTree.ToFactor();
		
		if (f.getValue())
		{
			mBuffer.enqueueTuple(tuple);
		}
	}
	
	@Override
	public void run() 
	{
		super.run();
		
		init();
		ITupleBuffer childBuffer = this.mSource.mBuffer;
		
		FactorTuple t;
		
		while (!mStop && (t = childBuffer.dequeueTuple()) != null)
		{
	 		processFullTuple(t);
 		}
		
		//Tell OUR parent that we're done
		mBuffer.markDone();
	}
}
