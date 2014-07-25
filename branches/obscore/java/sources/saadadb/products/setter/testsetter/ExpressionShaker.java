package saadadb.products.setter.testsetter;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.products.FooProduct;
import saadadb.products.setter.ExpressionWrapper;
/**
 * Test the ExpressionWrapper
 * @author pertuy
 * @version $Id$
 */
public class ExpressionShaker {
	private JSONObject jsonAhs;
	private FooProduct fooProduct;
	
	/**
	 * Create a fooProduct from a Json file to test the ExpressionWrapper with its AttributesHandler
	 * @param path
	 * @throws Exception
	 */
	public ExpressionShaker(String path) throws Exception
	{
		JSONParser parser = new JSONParser();  
		JSONObject jsonObject = (JSONObject)parser.parse(new FileReader(path));  
		this.jsonAhs = (JSONObject) jsonObject.get("fields");  
		this.fooProduct = new FooProduct(this.jsonAhs, 0);

	}
	
	public double noKeywordTest(String expr) throws FatalException
	{
		String res=ExpressionWrapper.evaluate(expr,null);
		return Double.valueOf(res);
	}
	
	public double keywordsTest(String expr,List<AttributeHandler> values) throws FatalException
	{
		String res=ExpressionWrapper.evaluate(expr,values);
		return Double.valueOf(res);
	}
	
	public void validKeywords() throws SaadaException
	{
		List<AttributeHandler> values = new ArrayList<AttributeHandler>();
		Map<String,AttributeHandler> mah;
		mah=fooProduct.getAttributeHandler();
		values.add(mah.get("_emin"));
		values.add(mah.get("_emax"));
		System.out.println(keywordsTest("_emin+_emax",values));
	}
	
	
	public static void main(String args[])
	{
		Map<String,AttributeHandler> mah;
		List<AttributeHandler> values = new ArrayList<AttributeHandler>();
		try {
			ExpressionShaker es = new ExpressionShaker("/home/pertuy/WorkSpace/SaadaObscore/datatest/"
					+ "simple_obscsore.json");
			System.out.println(es.noKeywordTest("(5+5)*2")==20);
			es.validKeywords();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
