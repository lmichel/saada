package saadadb.products.setter.testsetter;

import java.util.ArrayList;

import net.objecthunter.exp4j.function.Function;
import saadadb.products.setter.DictionaryNumericFunction;
import saadadb.products.setter.ExpressionWrapper;

/**
 * Testing class for the numerics function
 * @author pertuy
 * @version $Id$
 */
public class NumericFunctionTest {
	private String expression;
	private ExpressionWrapper wrapper;
	private String log;
	private ArrayList<Function> functionList;
	public NumericFunctionTest(String expression) throws Exception
	{
		this.expression=expression;
		wrapper = new ExpressionWrapper(expression);
		log="";
		functionList=new ArrayList<Function>();
	}
	
	public NumericFunctionTest() throws Exception
	{
		log="";
		functionList=new ArrayList<Function>();

	}
	
	/**
	 * Test one valid function
	 * @throws Exception
	 */
	public void onlyOneValidFunction() throws Exception
	{
		wrapper = new ExpressionWrapper("sqrt(4)");

		try {
			DictionaryNumericFunction.addToFunctionList(DictionaryNumericFunction.SQRT, functionList);
		} catch (Exception e) {
			log+="onlyOneValidFunction test failed (Dictionary Exception)\n";
		}
		try {
			wrapper.evaluate(null, functionList);
		} catch (Exception e) {
			log+="onlyOneValidFunction test failed (wrapper Exception)\n";
		}
		if(wrapper.getValue()==2)
		{
			log+="onlyOneValidFunction test succeed \n";
		}
		else
		{
			log+="onlyOneValidFunction test failed (Wrong result)\n";
		}
		
	}
	
	
	/**
	 * Test a function with too much arguments
	 * @throws Exception
	 */
	public void tooMuchArgs() throws Exception
	{
		wrapper = new ExpressionWrapper("toRadian(5,5)");
		try {
			DictionaryNumericFunction.addToFunctionList(DictionaryNumericFunction.TORADIAN, functionList);
	
		} catch (Exception e) {
			log+="tooMuchArgs test failed (Dictionary Exception)\n";
		}
		try {
			wrapper.evaluate(null, functionList);
		} catch (Exception e) {
			log+="tooMuchArgs test succeed (wrapper Exception)\n";
		}

		if(!Double.isNaN(wrapper.getValue()))
			log+="invalidFunction test failed (Should raise an Exception)\n";
	}
	
	
	/**
	 * Test an expression with several valid functions
	 * @throws Exception
	 */
	public void severalValidFunction() throws Exception
	{
		wrapper = new ExpressionWrapper("(sqrt(4)+10)/pow(2)");
		try {
			DictionaryNumericFunction.addToFunctionList(DictionaryNumericFunction.SQRT, functionList);
			DictionaryNumericFunction.addToFunctionList(DictionaryNumericFunction.POW, functionList);
		} catch (Exception e) {
			log+="severalValidFunction test failed (Dictionary Exception)\n";
		}
		try {
			wrapper.evaluate(null, functionList);
		} catch (Exception e) {
			log+="severalValidFunction test failed (wrapper Exception)\n";
		}
		System.out.println(wrapper.getValue());

		if(wrapper.getValue()==3)
		{
			log+="severalValidFunction test succeed \n";
		}
		else
		{
			log+="onlyOneValidFunction test failed (Wrong result)\n";
		}
		
	}
	
	/**
	 * Test an expression with an invalid function
	 * @throws Exception
	 */
	public void invalidFunction() throws Exception
	{
		wrapper = new ExpressionWrapper("(sqru(4)+10)/poe(2)");
		try {
			DictionaryNumericFunction.addToFunctionList(DictionaryNumericFunction.SQRT, functionList);
			DictionaryNumericFunction.addToFunctionList(DictionaryNumericFunction.POW, functionList);
		} catch (Exception e) {
			log+="invalidFunction test failed (Dictionary Exception)\n";
		}
		try {
			wrapper.evaluate(null, functionList);
		} catch (Exception e) {
			log+="invalidFunction test succeed (wrapper Exception)\n";
		}
		if(!Double.isNaN(wrapper.getValue()))
			log+="invalidFunction test failed (Should raise an Exception)\n";

	}
	
//	public void convertFunction()
//	{
//		if(DictionaryNumericFunction.convert("5", "plop", "plop")!=42)
//		{
//			log+="Convertion Function test failed \n";
//		}
//		else
//			log+="Convertion Function test succeed\n";
//	}
	
	/**
	 * Test all the functions
	 * @throws Exception
	 */
	public void testAll() throws Exception
	{
		try {
		onlyOneValidFunction();
		} catch(Exception e){e.printStackTrace();}
		try {
		severalValidFunction();
		} catch(Exception e){e.printStackTrace();}
		try {
//		convertFunction();
		tooMuchArgs();
		} catch(Exception e){e.printStackTrace();}
		try {
			invalidFunction();
			} catch(Exception e){e.printStackTrace();}
			System.out.println(log);
	}
	
	public static void main(String args[])
	{
		NumericFunctionTest test = null;
		try {
			test = new NumericFunctionTest();
			test.testAll();

		} catch (Exception e) {
			// TODO Auto-generated catch block
		}
	}
}