package saadadb.products.setter.testsetter;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.products.datafile.JsonDataFile;
import saadadb.products.setter.ExpressionWrapper;
/**
 * Test the ExpressionWrapper
 * @author pertuy
 * @version $Id$
 */
public class ExpressionShaker {
	private JSONObject jsonAhs;
	private JsonDataFile fooProduct;
	ExpressionWrapper wrapper;
	ArrayList<String> report;
	
	/**
	 * Create a fooProduct from a Json file to test the ExpressionWrapper with its AttributesHandler
	 * @param path
	 * @throws Exception
	 */
	public ExpressionShaker(String path) throws Exception
	{
		report = new ArrayList<String>();
		JSONParser parser = new JSONParser();  
		JSONObject jsonObject = (JSONObject)parser.parse(new FileReader(path));  
		this.jsonAhs = (JSONObject) jsonObject.get("fields");  
		this.fooProduct = new JsonDataFile(this.jsonAhs, 0);

	}
	
	/**
	 * Basic test to see if the Parsing work with no keywords
	 * @param expr
	 * @return
	 * @throws FatalException
	 */
	public void noKeywordTest(String expr) throws Exception
	{
		wrapper=new ExpressionWrapper(expr,null,null);
		report.add("-NoKeywordTest result : "+wrapper.getStringValue());
	}
	
	/**
	 * Call the Expression Wrapper with the Attributes in "value"
	 * @param expr
	 * @param values
	 * @param expectedResult
	 * @return
	 * @throws FatalException
	 */
	public String keywordsTest(String expr,List<AttributeHandler> values) throws Exception
	{
		wrapper=new ExpressionWrapper(expr,values,null);
		return wrapper.getStringValue();
	}
	
	/**
	 * Test the result of the wrapper when you give the accurates keywords contained by the expression
	 * @throws SaadaException
	 */
	public void validKeywordsTest() throws Exception
	{
		List<AttributeHandler> values = new ArrayList<AttributeHandler>();
		Map<String,AttributeHandler> mah;
		mah=fooProduct.getAttributeHandlerCopy();
		values.add(mah.get("_emin"));
		values.add(mah.get("_emax"));
		report.add("-ValidKeywordTest result : "+keywordsTest("(_emin+_emax)/2+10",values));
	}
	
	/**
	 * Display the results of our tests
	 */
	public void displayReport()
	{
		for(String s : report)
		{
			System.out.println(s);
		}
	}
	
	/**
	 * Launch all the test we set
	 * @throws SaadaException
	 */
	public void processAll()
	{
	
		try {
			this.noKeywordTest("(5+5)*2*5/20");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("noKeyWordTest failed");
			e.printStackTrace();
		}
		try {
			this.validKeywordsTest();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("validKeyWordsTest failed");
			e.printStackTrace();
		}
		try {
			missingKeywordsTest();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("-missingKeywordsTest normal Exception");
			e.printStackTrace();
		}
		
		try {
			tooMuchAttributesTest();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("-tooMuchAttribute Exception");
			e.printStackTrace();
		}
		try {
			emptyExpressionTest();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("-emptyExpressionTest Exception");
			e.printStackTrace();
		}
		
		try {
			nullExpressionTest();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("-nullExpressionTest Exception");
			e.printStackTrace();
		}
		
		try{
			wrongAttributeValueTest();
		} catch (Exception e) {
			System.out.println("-wrongExpressionTest Exception");
			e.printStackTrace();
		}
	}
	
	/**
	 * Test the reaction of the Expression Wrapper when one of the Keywords in the expression have no value.
	 * We expected and exception
	 * @throws SaadaException 
	 * 
	 */
	public void missingKeywordsTest() throws Exception
	{
		List<AttributeHandler> values = new ArrayList<AttributeHandler>();
		Map<String,AttributeHandler> mah;
		mah=fooProduct.getAttributeHandlerCopy();
		values.add(mah.get("_emin"));
		//values.add(mah.get("_emax"));
		report.add("-MissingKeywordsTest result : "+keywordsTest("(_emin+_emax)/2+10",values));
	}
	
	/**
	 * We test the reaction of the ExpressionWrapper when we give it a value which is not present in the expression
	 * @throws Exception
	 */
	public void tooMuchAttributesTest() throws Exception
	{
		List<AttributeHandler> values = new ArrayList<AttributeHandler>();
		Map<String,AttributeHandler> mah;
		mah=fooProduct.getAttributeHandlerCopy();
		values.add(mah.get("_emin"));
		values.add(mah.get("_emax"));
		report.add("-TooMuchAttributesTest result : "+keywordsTest("_emax/2+10",values));	
	}
	
	/**
	 * Test the reaction of the ExpressionWrapper when the Exception String is empty
	 * @throws Exception
	 */
	public void emptyExpressionTest() throws Exception
	{
		report.add("-emptyExpressionTest result : "+keywordsTest("",null));
	}
	
	public void nullExpressionTest() throws Exception
	{
		report.add("-nullExpressionTest result : "+keywordsTest(null,null));
	}
	
	/**
	 * Test the reaction of the ExpressionWrapper when an attribute is set with a wrong value (Numeric replaced by
	 * String for example)
	 * @throws Exception
	 */
	public void wrongAttributeValueTest() throws Exception
	{
		List<AttributeHandler> values = new ArrayList<AttributeHandler>();
		Map<String,AttributeHandler> mah;
		mah=fooProduct.getAttributeHandlerCopy();
		values.add(mah.get("_ra"));
		report.add("-TooMuchAttributesTest result : "+keywordsTest("_ra/2+10",values));	
	}
	public static void main(String args[])
	{

		try {
			ExpressionShaker es = new ExpressionShaker("/home/pertuy/WorkSpace/SaadaObscore/datatest/"
					+ "simple_obscsore.json");
			es.processAll();
			es.displayReport();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
