package edu.buffalo.cse562.reloperators;

import edu.buffalo.cse562.Main;
import edu.buffalo.cse562.relations.FactorTuple;
import edu.buffalo.cse562.relations.Schema;

public class PrintOperator extends SingleSourceOperator 
{
	public PrintOperator(RelationalOperator source) 
	{
		super(source);
		setSchema();
	}

	public void startQuery()
	{
		Thread t = new Thread(this);
		t.start();
		
		try 
		{
			t.join();
		} 
		catch (InterruptedException e) 
		{
			e.printStackTrace();
			Main.onError();
		}
	}
	
	private void printTuple(FactorTuple t)
	{
		String out = "";
		for (int i = 0; i < t.size(); i++)
		{
			if (i > 0)
				out += "|";
			out += t.get(i).toString();
		}
		
		System.out.println(out);
	}
	
	@Override
	public void run() 
	{
		super.run();
		
		FactorTuple t = null;
		
		while (!mStop && (t = this.mSource.mBuffer.dequeueTuple()) != null)
		{
			printTuple(t);
		}
		
		/*
		 * F$%k it, if mStop is set (LIMIT), just kill the app.
		 * TODO: if there's time, fix this hack.  This came up because of adding
		 * blocking to the buffers to increase performance.
		 */
		if (mStop)
			System.exit(0);
	}
	
	@Override
	protected void setSchema() 
	{
		mSchema = new Schema(mSource.mSchema);
	}
	
}
