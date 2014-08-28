package saadadb.products.setter.testsetter;

import java.util.Map;
import java.util.TreeMap;

import saadadb.enums.ColumnSetMode;
import saadadb.meta.AttributeHandler;
import saadadb.products.setter.ColumnExpressionSetter;

/**
 * Test class for the ColumnExpressionSetter
 * @author pertuy
 * @version $Id$
 */
public class ColumnExpressionTest {
	private Map<String,AttributeHandler> mapTest;
	private String log;
	private ColumnExpressionSetter ces;
	
	/**
	 * Init the test values. The expression and the map will take different values for the different tests
	 */
	public ColumnExpressionTest()
	{
		ces = new ColumnExpressionSetter();
		mapTest = new TreeMap<String,AttributeHandler>();
		AttributeHandler ah1 = new AttributeHandler();
		AttributeHandler ah2 = new AttributeHandler();
		AttributeHandler ah3 = new AttributeHandler();
		AttributeHandler ah4 = new AttributeHandler();
		AttributeHandler ah5 = new AttributeHandler();
		ah1.setNameattr("_tmin");
		ah1.setValue("11/18/1858"); //=1
		
		ah2.setNameattr("_emax");
		ah2.setNameorg("EMAX");
		ah2.setValue("2");
		
		ah3.setNameattr("_emin");
		ah3.setValue("3");
		
		ah4.setNameattr("_tmax");
		ah4.setValue("11/21/1858");//=5
		
		ah5.setNameattr("_emin59");
		ah5.setValue("5");

	}
	
	/**
	 * Test the ColumnSetter with the expression and the attributes given in parameter.
	 * @param expr
	 * @param attributes
	 * @throws Exception
	 */
	public void testColumnSetterWith(String expr,Map<String,AttributeHandler> attributes) throws Exception
	{

		if (attributes==null || attributes.isEmpty())
		{
			ces.calculateExpression(expr);
		}
		else
		{
			ces.calculateExpression(expr, attributes);
		}
	}
	
	/**
	 * We test the ColumnExpression with a constant argument
	 * We should switch in By_Value mode
	 */
	private void constant()
	{
		log+="\nTest : Expression with a CONSTANT argument \n";
		boolean success=true;
		try {
			testColumnSetterWith("42",null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log+="ERROR : Exception\n";
		}
		if(ces.getNumExpressionResult()!=42)
		{
			log+="ERROR : Wrong Result\n";
			success=false;
		}
	
		if(ces.getSettingMode()!=ColumnSetMode.BY_VALUE)
		{
			log+="ERROR : Wrong ColumnMode. The ColumnMode should be By_Value and is"+ces.getSettingMode()+"\n";
			success=false;
		}
		
		if(!ces.getExpressionResult().equals(ces.getValue()))
		{
			log+="ERROR : The ColumnsingleSetter should have value="+ces.getValue()+"and expr result ="+ces.getExpressionResult()+"\n";
			success=false;
		}
		if(success)
			log+="Test succeed\n";
	}
	
	/**
	 * We test the ColumnExpression with a operation of constant arguments;
	 * We should switch in By_Value mode
	 */
	private void constantOperation()
	{
		log+="\nTest : Expression with CONSTANT Operation\n";
		boolean success=true;
		try {
			testColumnSetterWith("'6'*'7'",null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log+="ERROR : Exception\n";
		}
		if(ces.getNumExpressionResult()!=42)
		{
			log+="ERROR : Wrong Result\n";
			success=false;
		}
	
		if(ces.getSettingMode()!=ColumnSetMode.BY_VALUE)
		{
			log+="ERROR : Wrong ColumnMode. The ColumnMode should be By_Value and is"+ces.getSettingMode()+"\n";
			success=false;
		}
		
		if(!ces.getExpressionResult().equals(ces.getValue()))
		{
			log+="ERROR : The ColumnsingleSetter should have value="+ces.getValue()+"and expr result ="+ces.getExpressionResult()+"\n";
			success=false;
		}
		if(success)
			log+="Test succeed\n";
	}
	
	/**
	 * We test the ColumnExpression with a operation of variables
	 */
	private void variablesOperation()
	{
		log+="\nTest : Expression with VARIABLE operation\n";
		AttributeHandler ah2 = new AttributeHandler();
		AttributeHandler ah3 = new AttributeHandler();
		AttributeHandler ah5 = new AttributeHandler();

		ah2.setNameattr("_emax");
		ah2.setNameorg("EMAX");
		ah2.setValue("2");
		
		ah3.setNameattr("_emin");
		ah3.setValue("3");

		ah5.setNameattr("_emin59");
		ah5.setValue("5");
		mapTest.put("_emax", ah2);
		mapTest.put("_emin", ah3);
		mapTest.put("_emin59", ah5);
		boolean success=true;
		try {
			testColumnSetterWith("EMAX*_emin+_emin59",mapTest);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log+="ERROR : Exception\n";
		}
		if(ces.getNumExpressionResult()!=11)
		{
			log+="ERROR : Wrong Result\n";
			success=false;
		}
		if(ces.getSettingMode()!=ColumnSetMode.BY_EXPRESSION)
		{
			log+="ERROR : Wrong ColumnMode. The ColumnMode should be By_Expression and is"+ces.getSettingMode()+"\n";
			success=false;
		}
		
		if(success)
			log+="Test succeed\n";
	}
	
	/**
	 * Test an expression empty
	 */
	private void expressionEmpty()
	{
		log+="\nTest : Expression empty\n";
		boolean success=false;
		try {
			testColumnSetterWith("",null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			success=true;
			log+="Test succeed\n";	
		}
		if(!success)
			log+="Test failed : No exception\n";
	}
	
	/**
	 * Test an expression null
	 */
	private void expressionNull()
	{
		log+="\nTest : Expression null\n";
		boolean success=false;
		try {
			testColumnSetterWith(null,null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			success=true;
			log+="Test succeed "+e.getMessage()+"\n";	
			
		}
		if(!success)
			log+="Test failed : No exception\n";
	}
	
	private void invalidVariables()
	{
		mapTest.clear();
		log+="\nTest : Expression with invalid arguments\n";
		boolean success=false;
		try {
			testColumnSetterWith("_EMAX+_emin",mapTest);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			success=true;
			log+="Test succeed\n";	
		}
		if(!success)
			log+="Test failed : No exception\n";
	}
	
	/**
	 * Test an expression with String Functions using constant args
	 */
	private void stringFunctionWithConstantArgs()
	{
		log+="\nTest : Expression with String functions using CONSTANT arguments\n";
		boolean success=true;
		try {
			testColumnSetterWith("MJD(\"11/22/1858\") - MJD(\"11/19/1858\")",null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log+="ERROR : Exception\n";
		}
		if(ces.getNumExpressionResult()!=3)
		{
			log+="ERROR : Wrong Result\n";
			success=false;
		}
	
		if(ces.getSettingMode()!=ColumnSetMode.BY_VALUE)
		{
			log+="ERROR : Wrong ColumnMode. The ColumnMode should be By_Value and is"+ces.getSettingMode()+"\n";
			success=false;
		}
		
		if(!ces.getExpressionResult().equals(ces.getValue()))
		{
			log+="ERROR : The ColumnsingleSetter should have value="+ces.getValue()+"and expr result ="+ces.getExpressionResult()+"\n";
			success=false;
		}
		if(success)
			log+="Test succeed\n";
	}
	
	/**
	 * Test an expression using a String function with variables arguments
	 */
	private void stringFunctionWithVariablesArgs()
	{
		log+="\nTest : Expression with String functions using VARIABLES arguments\n";

		AttributeHandler ah1 = new AttributeHandler();
		AttributeHandler ah4 = new AttributeHandler();
		ah1.setNameattr("_tmin");
		ah1.setValue("11/18/1858"); //=1
		ah4.setNameattr("_tmax");
		ah4.setValue("11/21/1858");//=4
		
		mapTest.clear();
		mapTest.put("_tmin", ah1);
		mapTest.put("_tmax", ah4);
		
		boolean success=true;
		try {
			testColumnSetterWith("MJD(_tmin) + MJD(_tmax)",mapTest);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log+="ERROR : Exception\n";
		}
		if(ces.getNumExpressionResult()!=5)
		{
			log+="ERROR : Wrong Result\n";
			success=false;
		}
		if(ces.getSettingMode()!=ColumnSetMode.BY_EXPRESSION)
		{
			log+="ERROR : Wrong ColumnMode. The ColumnMode should be By_Expression and is"+ces.getSettingMode()+"\n";
			success=false;
		}
		
		if(success)
			log+="Test succeed\n";
		
	}
	
	/**
	 * Test an expression using a numeric function with constant arguments
	 */
	private void numericFunctionWithConstantArgs()
	{
		log+="\nTest : Expression with Numeric functions using CONSTANT arguments\n";
		boolean success=true;
		try {
			testColumnSetterWith("pow(2)",null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log+="ERROR : Exception\n";
		}
		if(ces.getNumExpressionResult()!=4)
		{
			log+="ERROR : Wrong Result\n";
			success=false;
		}
	
		if(ces.getSettingMode()!=ColumnSetMode.BY_VALUE)
		{
			log+="ERROR : Wrong ColumnMode. The ColumnMode should be By_Value and is"+ces.getSettingMode()+"\n";
			success=false;
		}
		
		if(!ces.getExpressionResult().equals(ces.getValue()))
		{
			log+="ERROR : The ColumnsingleSetter should have value="+ces.getValue()+"and expr result ="+ces.getExpressionResult()+"\n";
			success=false;
		}
		if(success)
			log+="Test succeed\n";
	}
	
	/**
	 * Test an expression using a numeric function with variables arguments
	 */
	private void numericFunctionWithVariableArgs()
	{
		log+="\nTest : Expression with Numeric functions using VARIABLES arguments\n";


		AttributeHandler ah5 = new AttributeHandler();

		ah5.setNameattr("_emin59");
		ah5.setValue("5");
	
		mapTest.put("_emin59", ah5);
		
		boolean success=true;
		try {
			testColumnSetterWith("pow(_emin59)",mapTest);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log+="ERROR : Exception\n";
		}
		if(ces.getNumExpressionResult()!=25)
		{
			log+="ERROR : Wrong Result\n";
			success=false;
		}
		if(ces.getSettingMode()!=ColumnSetMode.BY_EXPRESSION)
		{
			log+="ERROR : Wrong ColumnMode. The ColumnMode should be By_Expression and is"+ces.getSettingMode()+"\n";
			success=false;
		}
		
		if(success)
			log+="Test succeed\n";
	}
	
	/**
	 * Test the convert function
	 */
	private void convertFunction()
	{

		log+="\nTest : Expression using the convert function\n";
		AttributeHandler ah5 = new AttributeHandler();
		ah5.setNameattr("_emin59");
		ah5.setValue("5");
		ah5.setUnit("km");
		mapTest.clear();
		mapTest.put("_emin59", ah5);

		boolean success=true;
		try {
			testColumnSetterWith("convert(_emin59,mm)",mapTest);
			//System.out.println(ces);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log+="ERROR : Exception\n";
		}
		if(ces.getNumExpressionResult()!=50)
		{
			log+="ERROR : Wrong Result\n";
			success=false;
		}
	
		if(ces.getSettingMode()!=ColumnSetMode.BY_EXPRESSION)
		{
			log+="ERROR : Wrong ColumnMode. The ColumnMode should be By_Value and is"+ces.getSettingMode()+"\n";
			success=false;
		}
		
		if(!ces.getExpressionResult().equals(ces.getValue()))
		{
			log+="ERROR : The ColumnsingleSetter should have value="+ces.getValue()+"and expr result ="+ces.getExpressionResult()+"\n";
			success=false;
		}
		if(success)
			log+="Test succeed\n";
	}
	
	
	/**
	 * Run all the test
	 */
	public void processAllTest()
	{
		constant();
		constantOperation();
		variablesOperation();
		expressionEmpty();
		expressionNull();
		invalidVariables();
		stringFunctionWithConstantArgs();
		stringFunctionWithVariablesArgs();
		numericFunctionWithVariableArgs();
		numericFunctionWithConstantArgs();
		convertFunction();
		System.out.println(log);
		
	}
	
	public static void main(String Args[])
	{
		ColumnExpressionTest test = new ColumnExpressionTest();
		test.processAllTest();
	}

}
