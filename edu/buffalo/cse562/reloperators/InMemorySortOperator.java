package edu.buffalo.cse562.reloperators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.buffalo.cse562.ExprTree.ExpressionTree;
import edu.buffalo.cse562.relations.FactorTuple;
import edu.buffalo.cse562.relations.Schema;
import edu.buffalo.cse562.util.SortDefinitionList;
import edu.buffalo.cse562.util.SortFactor;
import edu.buffalo.cse562.util.SortableTuple;

public class InMemorySortOperator extends SingleSourceOperator 
{	
	private SortDefinitionList mSortList;
	private ArrayList<ExpressionTree> mSortFactorMap;
	
	/*
	 * For now, this will fit in memory.  Eventually,
	 * this may need to be flushed to disk
	 */
	private ArrayList<SortableTuple> mSortBuffer;
	
	private QueryContext mContext;
	
	public InMemorySortOperator(RelationalOperator source, SortDefinitionList list) 
	{
		super(source);
		mSortList = list;
		mSortBuffer = new ArrayList<SortableTuple>();
		setSchema();
		
		mContext = new QueryContext();
		mContext.CurrentContext = new VariableContext(this.mSchema);
		mContext.OuterQueryContext = null;
		buildSortList();
	}
	
	private void processFullTuple(FactorTuple t)
	{
		//Populate the current values (we're linked against schema)
		mContext.CurrentContext.CurrentValues = t;
		
		//Next, evaluate each sort expression
		List<SortFactor> factors = new ArrayList<SortFactor>(mSortFactorMap.size());
		
		for (int i = 0; i < mSortFactorMap.size(); i++)
		{
			factors.add(new SortFactor(mSortFactorMap.get(i).ToFactor(), mSortList.get(i).IsAscending));
		}
		
		//Finally, add this to the sortable list
		mSortBuffer.add(new SortableTuple(factors, t));
	}
	
	@Override
	public void run() 
	{
		super.run();
		
		FactorTuple t = null;
		
		while (!mStop && (t = mSource.mBuffer.dequeueTuple()) != null)
		{
			processFullTuple(t);
		}
		
		if (mStop)
		{
			mBuffer.markDone();
			return;
		}
		
		Collections.sort(mSortBuffer);
		
		for (int i = 0; i < mSortBuffer.size(); i++)
		{
			mBuffer.enqueueTuple(mSortBuffer.get(i).mData);
		}
		
		mBuffer.markDone();
		
	}
	
	private void buildSortList()
	{
		mSortFactorMap = new ArrayList<ExpressionTree>();
		
		for (int i = 0; i < mSortList.size(); i++)
		{
			mSortFactorMap.add(new ExpressionTree(mSortList.get(i).mExpression, mContext));
		}
	}
	
	@Override
	protected void setSchema() 
	{
		mSchema = new Schema(mSource.mSchema);
	}

}
