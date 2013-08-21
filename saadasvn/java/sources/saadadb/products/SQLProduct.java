package saadadb.products;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import saadadb.exceptions.IgnoreException;
import saadadb.meta.AttributeHandler;

public class SQLProduct  extends File implements ProductFile 
{
	public SQLProduct(File parent, String child) 
	{
		super(parent, child);
		// TODO Auto-generated constructor stub
	}

	private static final long serialVersionUID = 1L;

	@Override
	public boolean hasMoreElements() 
	{
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public Object nextElement() 
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String getKWValueQuickly(String key) 
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void setKWEntry(LinkedHashMap<String, AttributeHandler> tah) throws IgnoreException 
			{
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public double[] getExtrema(String key) throws Exception 
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public int getNRows() throws IgnoreException 
	{
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public int getNCols() throws IgnoreException 
	{
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public void initEnumeration() throws IgnoreException 
	{
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public Map<String, ArrayList<AttributeHandler>> getMap(String category) throws IgnoreException 
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public SpaceFrame getSpaceFrame() 
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void setSpaceFrameForTable() throws IgnoreException 
	{
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void setSpaceFrame() 
	{
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void closeStream() 
	{
		// TODO Auto-generated method stub
		
	}
}
