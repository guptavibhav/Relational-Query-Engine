package edu.buffalo.cse562.reloperators;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.buffalo.cse562.Main;
import edu.buffalo.cse562.ExprTree.ExpressionTree;
import edu.buffalo.cse562.relations.FactorTuple;
import edu.buffalo.cse562.relations.Schema;
import edu.buffalo.cse562.util.SortDefinitionList;
import edu.buffalo.cse562.util.SortFactor;
import edu.buffalo.cse562.util.SortableTuple;
import edu.buffalo.cse562.util.SwapManager;

public class OutOfCoreSortOperator extends SingleSourceOperator 
{	
	private class StoredListState
	{
		public String fileName = "";
		public int listCount = 0;
	}
	
	private SortDefinitionList mSortList;
	private ArrayList<ExpressionTree> mSortFactorMap;
	
	private QueryContext mContext;
	
	private String mFileName = "";
	
	private File mSwapDir;
	
	/*
	 * Maximum number of tuples per initial file.
	 * We'll merge lists twice as big and output to a temp list, so we'll need
	 * 3 times this amount of memory.
	 * 
	 * At this point, we don't have a great way to do this...
	 */
	private static final int MAX_TUPLES = 1000000;
	
	private int mCurFileNum = 0;
	
	private static final boolean USE_SERIALIZATION = false;
	private static final boolean CLEAR_SWAP = false;
	
	public OutOfCoreSortOperator(RelationalOperator source, SortDefinitionList list) 
	{
		super(source);
		mSortList = list;
		setSchema();
		
		mContext = new QueryContext();
		mContext.CurrentContext = new VariableContext(this.mSchema);
		mContext.OuterQueryContext = null;
		buildSortList();
		
		initSwapDir();
	}
	
	private void initSwapDir()
	{
		String tempDirName = String.valueOf(SwapManager.SwapLocation + "/" + SwapManager.getNextSwapIndex());
		mSwapDir = new File(tempDirName);
		mSwapDir.mkdir();
	}
	
	private void clearSwap()
	{
		if (!CLEAR_SWAP)
			return;
		
		mCurFileNum = 0;
		
		for (File f : mSwapDir.listFiles())
		{
			f.delete();
		}
		
		mSwapDir.delete();
	}
	
	private String flushToDiskNative(List<SortableTuple> tuples)
	{
		String file =  mSwapDir.getAbsolutePath() + "/" + mFileName + String.valueOf(mCurFileNum++);
		
		try 
		{
			DataOutputStream ds = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
			
			ds.writeInt(tuples.size());
			for (SortableTuple t : tuples)
			{
				t.toFile(ds);
			}
			
			ds.close();
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
			Main.onError();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			Main.onError();
		}	
		
		return file;
	}
	
	private void flushAppend(List<SortableTuple> tuples, DataOutputStream ds)
	{
		try 
		{
			ds.writeInt(tuples.size());
			for (SortableTuple t : tuples)
			{
				t.toFile(ds);
			}
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
			Main.onError();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			Main.onError();
		}	
			
	}
	
	private String flushtoDisk(List<?> list)
	{
		String file =  mSwapDir.getAbsolutePath() + "/" + mFileName + String.valueOf(mCurFileNum++);
		ObjectOutputStream outputStream;
		
		try 
		{
			outputStream = new ObjectOutputStream(new FileOutputStream(file));
			outputStream.writeObject(list);
			outputStream.close();
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
			Main.onError();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			Main.onError();
		}	
		
		return file;
	}
	
	private List<StoredListState> runPhaseOne()
	{
		List<StoredListState> files = new ArrayList<StoredListState>();
		List<SortableTuple> sortBuffer = new ArrayList<SortableTuple>();
		
		FactorTuple tuple = null;
		
		while (!mStop && (tuple = mSource.mBuffer.dequeueTuple()) != null)
		{
			/*
			 * Flush to disk after seeing MAX_TUPLES
			 */
			if (sortBuffer.size() == MAX_TUPLES)
			{
				Collections.sort(sortBuffer);
				StoredListState state = new StoredListState();
				
				if (USE_SERIALIZATION)
					state.fileName = flushtoDisk(sortBuffer);
				else
					state.fileName = flushToDiskNative(sortBuffer);
				
				state.listCount = 1;
				files.add(state);
				sortBuffer = new ArrayList<SortableTuple>();
			}
			
			//Calculate the sort keys
			mContext.CurrentContext.CurrentValues = tuple;
			List<SortFactor> factors = new ArrayList<SortFactor>(mSortFactorMap.size());
			
			for (int i = 0; i < mSortFactorMap.size(); i++)
			{
				factors.add(new SortFactor(mSortFactorMap.get(i).ToFactor(), mSortList.get(i).IsAscending));
			}
			
			SortableTuple st = new SortableTuple(factors, tuple);
			sortBuffer.add(st);
		}
		
		if (files.size() == 0)
		{
			//Just flush to the buffer if the list is small
			Collections.sort(sortBuffer);
			
			for (int i = 0; i < sortBuffer.size(); i++)
			{
				mBuffer.enqueueTuple(sortBuffer.get(i).mData);
			}
		}
		else if (sortBuffer.size() > 0)
		{	
			//Flush the remainder to disk
			Collections.sort(sortBuffer);
			StoredListState state = new StoredListState();
			
			if (USE_SERIALIZATION)
				state.fileName = flushtoDisk(sortBuffer);
			else
				state.fileName = flushToDiskNative(sortBuffer);
			
			state.listCount = 1;
			files.add(state);
		}
		
		return files;
	}
	
	private ArrayList<SortableTuple> nativeReadFromFile(DataInputStream ds) throws IOException
	{
		ArrayList<SortableTuple> tuples = new ArrayList<SortableTuple>();
		
		int count = ds.readInt();
		
		int dataCount = this.mSchema.size();
		int keyCount = this.mSortList.size();
		
		for (int i = 0; i < count; i++)
		{
			tuples.add(new SortableTuple(ds, dataCount, keyCount));
		}
		
		return tuples;
	}
	
	/*
	 * Merge A and B into a new file C
	 * 
	 * A and B are sorted
	 */
	private StoredListState mergeFiles(StoredListState fileA, StoredListState fileB, boolean streamToBuffer)
	{
		StoredListState state = new StoredListState();
		state.listCount = 0;
		state.fileName = mSwapDir.getAbsolutePath() + "/" + mFileName + String.valueOf(mCurFileNum++);
		
		ObjectOutputStream outputStream = null;
		ObjectInputStream lhsInputStream;
		ObjectInputStream rhsInputStream;
		
		ArrayList<SortableTuple> lhs;
		ArrayList<SortableTuple> rhs;
		
		try 
		{
			lhsInputStream = new ObjectInputStream(new FileInputStream(fileA.fileName));
			rhsInputStream = new ObjectInputStream(new FileInputStream(fileB.fileName));
			
			if (!streamToBuffer)
				outputStream = new ObjectOutputStream(new FileOutputStream(state.fileName));
			
			//Initial lists
			lhs = (ArrayList<SortableTuple>)lhsInputStream.readObject();
			rhs = (ArrayList<SortableTuple>)rhsInputStream.readObject();
			
			int curLHS = 0;
			int curRHS = 0;
			
			int curLHSListNum = 1;
			int curRHSListNum = 1;
			
			List<SortableTuple> tempBuffer = new ArrayList<SortableTuple>();
			
			SortableTuple leftTuple = null;
			SortableTuple rightTuple = null;
			
			boolean lhsDone = false;
			boolean rhsDone = false;
			
			while (true)
			{
				if (curLHS == lhs.size())
				{
					curLHS = 0;
					curLHSListNum++;
					
					if (curLHSListNum > fileA.listCount)
						lhsDone = true;
					else
						lhs = (ArrayList<SortableTuple>)lhsInputStream.readObject();
				}
				
				if (curRHS == rhs.size())
				{
					curRHS = 0;
					curRHSListNum++;
					
					if (curRHSListNum > fileB.listCount)
						rhsDone = true;
					else
						rhs = (ArrayList<SortableTuple>)rhsInputStream.readObject();
					
				}
				
				if (lhsDone && rhsDone)
				{
					if (!streamToBuffer && tempBuffer.size() > 0)
					{
						outputStream.writeObject(tempBuffer);
						state.listCount++;
					}
					break;
				}
				
				//If we made it here, we have tuples to compare.
				if (!lhsDone)
					leftTuple = lhs.get(curLHS);
				
				if (!rhsDone)
					rightTuple =  rhs.get(curRHS);
				
				if (!lhsDone && rhsDone)
				{
					if (streamToBuffer)
					{
						this.mBuffer.enqueueTuple(leftTuple.mData);
					}
					else
					{
						tempBuffer.add(leftTuple);
					}
					curLHS++;
				}
				else if (lhsDone && !rhsDone)
				{
					if (streamToBuffer)
					{
						this.mBuffer.enqueueTuple(rightTuple.mData);
					}
					else
						tempBuffer.add(rightTuple);
					
					curRHS++;
				}
				else if (leftTuple.compareTo(rightTuple) < 0)
				{
					if (streamToBuffer)
					{
						this.mBuffer.enqueueTuple(leftTuple.mData);
					}
					else
						tempBuffer.add(leftTuple);
					
					curLHS++;
				}
				else
				{
					if (streamToBuffer)
					{
						this.mBuffer.enqueueTuple(rightTuple.mData);
					}
					else
						tempBuffer.add(rightTuple);
					
					curRHS++;
				}
				
				if (tempBuffer.size() == MAX_TUPLES && ! streamToBuffer)
				{
					outputStream.writeObject(tempBuffer);
					state.listCount++;
					tempBuffer = new ArrayList<SortableTuple>();
				}			
			}
			
			lhsInputStream.close();
			rhsInputStream.close();
			
			if (!streamToBuffer)
				outputStream.close();

		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			Main.onError();
		} 
		catch (ClassNotFoundException e) 
		{
			e.printStackTrace();
			Main.onError();
		}
		
		return state;
	}
	
	private StoredListState mergeFiles2(StoredListState fileA, StoredListState fileB, boolean streamToBuffer)
	{
		StoredListState state = new StoredListState();
		state.listCount = 0;
		state.fileName = mSwapDir.getAbsolutePath() + "/" + mFileName + String.valueOf(mCurFileNum++);
		
		DataOutputStream outputStream = null;
		DataInputStream lhsInputStream;
		DataInputStream rhsInputStream;
		
		ArrayList<SortableTuple> lhs;
		ArrayList<SortableTuple> rhs;
		
		try 
		{
			lhsInputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(fileA.fileName)));
			rhsInputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(fileB.fileName)));
			
			if (!streamToBuffer)
			{
				outputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(state.fileName)));
			}
			
			//Initial lists
			lhs = nativeReadFromFile(lhsInputStream);
			rhs = nativeReadFromFile(rhsInputStream);
			
			int curLHS = 0;
			int curRHS = 0;
			
			int curLHSListNum = 1;
			int curRHSListNum = 1;
			
			List<SortableTuple> tempBuffer = new ArrayList<SortableTuple>();
			
			SortableTuple leftTuple = null;
			SortableTuple rightTuple = null;
			
			boolean lhsDone = false;
			boolean rhsDone = false;
			
			while (true && !mStop)
			{
				if (curLHS == lhs.size())
				{
					curLHS = 0;
					curLHSListNum++;
					
					if (curLHSListNum > fileA.listCount)
						lhsDone = true;
					else
						lhs = nativeReadFromFile(lhsInputStream);
				}
				
				if (curRHS == rhs.size())
				{
					curRHS = 0;
					curRHSListNum++;
					
					if (curRHSListNum > fileB.listCount)
						rhsDone = true;
					else
						rhs = nativeReadFromFile(rhsInputStream);
					
				}
				
				if (lhsDone && rhsDone)
				{
					if (!streamToBuffer && tempBuffer.size() > 0)
					{
						flushAppend(tempBuffer, outputStream);
						state.listCount++;
					}
					break;
				}
				
				//If we made it here, we have tuples to compare.
				if (!lhsDone)
					leftTuple = lhs.get(curLHS);
				
				if (!rhsDone)
					rightTuple =  rhs.get(curRHS);
				
				if (!lhsDone && rhsDone)
				{
					if (streamToBuffer)
					{
						this.mBuffer.enqueueTuple(leftTuple.mData);
					}
					else
					{
						tempBuffer.add(leftTuple);
					}
					curLHS++;
				}
				else if (lhsDone && !rhsDone)
				{
					if (streamToBuffer)
					{
						this.mBuffer.enqueueTuple(rightTuple.mData);
					}
					else
						tempBuffer.add(rightTuple);
					
					curRHS++;
				}
				else if (leftTuple.compareTo(rightTuple) < 0)
				{
					if (streamToBuffer)
					{
						this.mBuffer.enqueueTuple(leftTuple.mData);
					}
					else
						tempBuffer.add(leftTuple);
					
					curLHS++;
				}
				else
				{
					if (streamToBuffer)
					{
						this.mBuffer.enqueueTuple(rightTuple.mData);
					}
					else
						tempBuffer.add(rightTuple);
					
					curRHS++;
				}
				
				if (tempBuffer.size() == MAX_TUPLES && !streamToBuffer)
				{
					flushAppend(tempBuffer, outputStream);
					state.listCount++;
					tempBuffer = new ArrayList<SortableTuple>();
				}			
			}
			
			lhsInputStream.close();
			rhsInputStream.close();
			
			if (!streamToBuffer)
				outputStream.close();

		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			Main.onError();
		} 
		
		return state;
	}
	
	private void runPhase2(List<StoredListState> fileData)
	{
		while (fileData.size() > 2 && !mStop)
		{
			List<StoredListState> tempData = new ArrayList<OutOfCoreSortOperator.StoredListState>();
			
			for (int i = 0; i < fileData.size(); i+= 2)
			{
				if (i+1 < fileData.size())
				{
					if (USE_SERIALIZATION)
						tempData.add(mergeFiles(fileData.get(i), fileData.get(i+1), false));
					else
						tempData.add(mergeFiles2(fileData.get(i), fileData.get(i+1), false));
				}
				else
				{
					tempData.add(fileData.get(i));
				}
				
			}
			
			fileData = tempData;
		}
		
		if (mStop)
			return;
		
		if (USE_SERIALIZATION)
			mergeFiles(fileData.get(0), fileData.get(1), true);
		else
			mergeFiles2(fileData.get(0), fileData.get(1), true);
		
	}
	
	@Override
	public void run() 
	{
		super.run();
		
		List<StoredListState> files = runPhaseOne();
		
		if (mStop)
		{
			mBuffer.markDone();
			clearSwap();
			return;
		}
		
		if (files.size() > 0)
			runPhase2(files);
		
		this.mBuffer.markDone();
		
		clearSwap();
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
