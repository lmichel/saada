package saadadb.products.setter.testsetter;

import java.util.Map;
import java.util.TreeMap;

import saadadb.database.Database;
import saadadb.enums.ColumnSetMode;
import saadadb.meta.AttributeHandler;
import saadadb.products.setter.ColumnExpressionSetter;

public class StringConcatTest {
	private Map<String,AttributeHandler> mapTest;
	private String log  = "";
	private ColumnExpressionSetter ces;

	/**
	 * Init the test values. The expression and the map will take different values for the different tests
	 */
	public StringConcatTest()
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
			ces.setExpression(expr);
		}
		else
		{
			ces.setExpression(expr);
			ces.calculateExpression(attributes);
		}
	}

	/**
	 * Test an expression using a String function with variables arguments
	 */
	private void stringFunctionWithVariablesArgs()
	{
		log+="Test : Expression with String functions using VARIABLES arguments\n";

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
			testColumnSetterWith("strcat(_tmin, 'azzz',  _tmax)",mapTest);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log+="ERROR : Exception\n";
		}
		System.out.println(ces);
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

	public static void main(String[] args) {
		StringConcatTest sct = new StringConcatTest();
		sct.stringFunctionWithVariablesArgs();;
		System.out.println(sct.log);
		Database.close();
	}

}
