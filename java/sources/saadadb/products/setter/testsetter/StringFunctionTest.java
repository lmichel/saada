package saadadb.products.setter.testsetter;

import saadadb.products.setter.DictionaryStringFunction;

/**
 * class used to test the DictionaryStringFunction
 * @author pertuy
 * @version $Id$
 */
public class StringFunctionTest {

	private String funcName;
	private String args[];
	private String log;
	
	public StringFunctionTest()
	{
		
	}
	
	/**
	 * Test the call of a String function with valid name and arguments
	 */
	private void validFunctionArgsTest()
	{
		String result=null;
		funcName=DictionaryStringFunction.MJD;
		args = new String[]{"11-18-1858"};
		try {
			result=DictionaryStringFunction.exec(funcName, args);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log+="validFunctionArgs test failed (Exception exec)\n";
		}
		if(Double.valueOf(result)==1)
		{
			log+="validFunctionArgs test succeed\n";
		}
	}
	
	/**
	 * Test the call of a String function with unvalid name
	 */
	private void invalidFunctionTest()
	{
		String result=null;

		funcName="imInvalid";
		args = new String[]{"11-18-1858"};
		try {
			result=DictionaryStringFunction.exec(funcName, args);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log+="invalidFunction test succeed (Exception exec)\n";
		}
		if(result!=null)
		{
			log+="validFunctionArgs test failed\n";
		}
	
	}
	
	/**
	 * Test the call of a function with null arguments
	 */
	private void invalidArgumentsTest()
	{
		String result=null;

		funcName="imInvalid";
		args =null;
		try {
			result=DictionaryStringFunction.exec(funcName, args);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log+="invalidArgumentsTest test succeed (Exception exec)\n";
		}
		if(result!=null)
		{
			log+="invalidArgumentsTest test failed\n";
		}
	
	}
	
	
	public void allTest()
	{
		validFunctionArgsTest();
		invalidFunctionTest();
		invalidArgumentsTest();
		System.out.println(log);
	}
	
	public static void main(String args[])
	{
		StringFunctionTest test = new StringFunctionTest();
		test.allTest();
	}
}
