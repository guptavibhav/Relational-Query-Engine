package edu.buffalo.cse562.reloperators;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.management.Query;

import net.sf.jsqlparser.schema.Column;
import edu.buffalo.cse562.ExprTree.ExpressionTree;
import edu.buffalo.cse562.ExprTree.VariableNode;
import edu.buffalo.cse562.Factors.BooleanFactor;
import edu.buffalo.cse562.Factors.Factor;
import edu.buffalo.cse562.Factors.IFactor;
import edu.buffalo.cse562.Factors.NullFactor;
import edu.buffalo.cse562.aggregates.AggregateFunction;
import edu.buffalo.cse562.aggregates.AggregateFunctionList;
import edu.buffalo.cse562.aggregates.CountAggregate;
import edu.buffalo.cse562.aggregates.CountAllAggregate;
import edu.buffalo.cse562.relations.ColumnDataTypes;
import edu.buffalo.cse562.relations.FactorTuple;
import edu.buffalo.cse562.relations.Schema;
import edu.buffalo.cse562.relations.SchemaColumn;
import edu.buffalo.cse562.util.ExpressionTypeVisitor;

public class SortedAggregateOperator extends SingleSourceOperator 
{
	/*
	 * Placeholder for count all which does not take an argument.
	 */
	private class CountPlaceHolder implements IFactor
	{
		private NullFactor mNullFactor = new NullFactor();

		@Override
		public Factor ToFactor() 
		{
			return mNullFactor;
		}
	}
	
	/*
	 * List of aggregate functions to calculate.
	 * 
	 * This is passed during construction.
	 */
	private AggregateFunctionList mFunctions;
	
	private List mGroupByList;
	
	private QueryContext mContext;
	
	
	/*
	 * Group-by variable mapping 
	 */
	private ArrayList<IFactor> mGroupMap;
	
	/*
	 * Map used to find the variable being aggregated 
	 */
	private ArrayList<IFactor> mAggVarMap;
	
	private boolean mFirst = true; 
	private boolean mDoGroupBy = false;
	
	/*
	 * Last set of factors
	 */
	private ArrayList<Factor> mPreviousFactors;
	
	public SortedAggregateOperator(RelationalOperator source, AggregateFunctionList functions, List groupBy, VariableContext outerContext) 
	{
		super(source);
		mFunctions = functions;
		mGroupByList = groupBy;
		mGroupMap = new ArrayList<IFactor>();
		mAggVarMap = new ArrayList<IFactor>();
		mPreviousFactors = new ArrayList<Factor>();
		mContext = new QueryContext();
		mContext.CurrentContext = new VariableContext(this.mSource.mSchema);
		mContext.OuterQueryContext = outerContext;
		setSchema();
	}

	public SortedAggregateOperator(RelationalOperator source, AggregateFunctionList functions, VariableContext outerContext) 
	{
		this(source, functions, new ArrayList<SchemaColumn>(), outerContext);
	}
	
	private void sendAndClearAggreate()
	{
		FactorTuple tuple = new FactorTuple();
		
		//Use the previous values since the group map was updated to
		//compare against!  We always push data up AFTER the grouping 
		//criteria changes.
		for (int i = 0; i < mPreviousFactors.size(); i++)
		{
			tuple.add(mPreviousFactors.get(i));
		}
		
		for (int i = 0; i < mFunctions.size(); i++)
		{
			tuple.add(mFunctions.get(i).ToFactor());
			mFunctions.get(i).reset();
		}
		
		mBuffer.enqueueTuple(tuple);
	}
	
	private void checkGroupBy()
	{
		//Check if any of the group by criteria has changed.
		//If it has, send the PREVIOUS aggregate
		
		for (int i = 0; i < mPreviousFactors.size(); i++)
		{
			BooleanFactor res = (BooleanFactor) Factor.EqualOp(mPreviousFactors.get(i), mGroupMap.get(i).ToFactor());
			if (!res.getValue())
			{
				sendAndClearAggreate();
				//Store the previous values
				mPreviousFactors.clear();
				for (int j = 0; j < mGroupMap.size(); j++)
				{
					mPreviousFactors.add(mGroupMap.get(j).ToFactor());
				}				
				return;
			}
		}
	}
	
	private void processFullTuple(FactorTuple tuple)
	{
		//Update values
		mContext.CurrentContext.CurrentValues = tuple;
		
		//Check group by conditions
		if (mFirst)
		{
			mFirst = false;
			
			for (int i= 0; i < mGroupMap.size(); i++)
			{
				mPreviousFactors.add(mGroupMap.get(i).ToFactor());
			}
		}
		else if (mDoGroupBy)
		{
			checkGroupBy();
		}
		
		//Update Aggregates
		
		for (int i = 0; i < mFunctions.size(); i++)
		{
			mFunctions.get(i).update(mAggVarMap.get(i).ToFactor());
		}
	}
	
	@Override
	public void run() 
	{
		super.run();
		mDoGroupBy = mGroupMap.size() > 0;
		
		FactorTuple t = null;
		while ((t = mSource.mBuffer.dequeueTuple()) != null)
		{
			processFullTuple(t);
		}
		
		sendAndClearAggreate();
		mBuffer.markDone();
	}

	/*
	 * Our schema is whatever is in our group by clause plus our aggregate function
	 * variables.
	 */
	@Override
	protected void setSchema() 
	{
		mSchema = new Schema();
		
		if (mGroupByList != null)
			addGroupByToSchema();
		
		addFunctionsToSchema();
	}
	
	private void addGroupByToSchema()
	{
		for (int i = 0; i < mGroupByList.size(); i++)
		{
			Column gbCol = (Column)mGroupByList.get(i);
			
			//group by needs to be in the source list, otherwise we can't group!
			
			String tableName = "";
			if (gbCol.getTable() != null && gbCol.getTable().getName() != null)
				tableName = gbCol.getTable().getName();
			
			int srcCol = mSource.mSchema.findColumnIndex(tableName, gbCol.getColumnName());
			mGroupMap.add(new VariableNode(mContext.CurrentContext, srcCol));
			mSchema.add(new SchemaColumn(mSource.mSchema.get(srcCol)));
		}
	}
	
	private void addFunctionsToSchema()
	{
		for (AggregateFunction f : mFunctions)
		{
			ColumnDataTypes type = GetFunctionType(f);
			mSchema.add(new SchemaColumn("", f.getThisVarName(), type));
			
			if (f instanceof CountAllAggregate)
			{
				//Count all doesn't take any arguments, but we're making each function
				//take a factor on update().  So, we'll pass it a null factor.
				mAggVarMap.add(new CountPlaceHolder());
			}
			else 	
			{
				ExpressionTree expr = new ExpressionTree(f.getAggregatingExpression(), mContext);
				mAggVarMap.add(expr);
			}
		}
	}
	
	private ColumnDataTypes GetFunctionType(AggregateFunction f)
    {
        if (f.getAggFunctionName() == CountAggregate.FUNCTION_NAME || f.getAggFunctionName() == CountAllAggregate.FUNCTION_NAME)
            return ColumnDataTypes.tpLong;
        else
            return new ExpressionTypeVisitor(mSource.getSchema(), f.getAggregatingExpression()).getExpressionType();
    }

}
