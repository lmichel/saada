package saadadb.products.setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.util.SaadaConstant;

/**
 * The columnSetter used when a field must be evaluate by an expression
 * @author pertuy
 * @version $Id$
 */
public class ColumnExpressionSetter extends ColumnSetter implements Cloneable{

	
	/**
	 * The expression to evaluate 
	 */
	private String expression;
	 
	/**
	 * The wrapper used to evaluate the expression
	 */
	private ExpressionWrapper wrapper;
	
	/**
	 * The list of attributes used in the ExpressionWrapper
	 */
	private List<AttributeHandler> exprAttributes;
	
	
	/**
	 * The list of attribute being String functions arguments (names of AH)
	 */
	private List<AttributeHandler> stringFunctionArgumentsList;
	
	/**
	 * The result of the expression
	 */
	private String result;
	
	/**
	 * List of String functions found in the expression
	 */
	private Map<String,String> stringFunctionList;
	
	/**
	 * Initialize the wrapper, the expression and if not null, the list of AttributeHandler then calculate the result
	 * @param expr
	 * @param attributes
	 * @throws FatalException
	 */
	public ColumnExpressionSetter(String expr, Map<String,AttributeHandler> attributes) throws Exception
	{
		super();
		if( expr == null ){
			FatalException.throwNewException(SaadaException.INTERNAL_ERROR, "Column setter cannot be set with a null expression");
		}
		expression=expr;
		/*
		 * We treat the String functions
		 */
		checkAndExecStringFunction(attributes);

		if(attributes!=null)
		{

			/*
			 * We build the list of AH present in the expression and we format their names (in the expression)
			 */
			AttributeHandlerExtractor ahExtractor = new AttributeHandlerExtractor(expression, attributes);
			exprAttributes = ahExtractor.extractAH();//new ArrayList<AttributeHandler>();
			expression=ahExtractor.expression;
			//buildAttributeList(attributes);
		}

		
		/*
		 *\/!\ WARNING : We must handle the numerics functions here
		 */
		
		
		/*
		 * If an exception is risen in the Wrapper (Wrong value in ah, missins value), the result is set to "NULL"
		 */
		try {
			wrapper = new ExpressionWrapper(expression, exprAttributes);
			result=wrapper.getStringValue();
		} catch (Exception e) {
			e.printStackTrace();
			result=SaadaConstant.STRING;
		}
	}
	

	
	/**
	 * Execute the String functions in the expression and replaced them by their values
	 * @throws Exception 
	 * 
	 */
	private void checkAndExecStringFunction(Map<String,AttributeHandler> attributes) throws Exception
	{
		StringFunctionExtractor extractor = new StringFunctionExtractor(this.expression);
		//We only do something if a String function have been found
		if(extractor.extractAndSplit())
		{
			stringFunctionArgumentsList= new ArrayList<AttributeHandler>();
			//We're getting back the new expression (with flags)
			this.expression=extractor.expression;
			//for each function
			for(Entry<String,StringFunctionDescriptor> e : extractor.splitedFunctionsMap.entrySet())
			{
				String[] args = e.getValue().functionArguments;
				//We replace the AH name by their values for each argument
				for(int i=0;i<args.length;i++)
				{
					for(Entry<String,AttributeHandler> ah : attributes.entrySet())
					{
						if(args[i].contains(ah.getValue().getNameattr()) || (args[i].contains(ah.getValue().getNameorg()) &&
								ah.getValue().getNameorg()!=""))
						{
							args[i]=ah.getValue().getValue();
							stringFunctionArgumentsList.add(ah.getValue());
						}
					}
				}
				//We execute the String function
				expression=expression.replace(e.getKey(), DictionaryString.exec(e.getValue().functionName, args));

				
			}
				
		}
		

	}
	
	
	/**
	 * Search the expression for corresponding attributeHandler
	 * @param attributes
	 * @return
	 */
	private void buildAttributeList(Map<String,AttributeHandler> attributes)
	{
		/*
		 * We must check the NameAttr and the NameOrg for each AttributeHandler
		 */
		for(Entry<String,AttributeHandler> e : attributes.entrySet())
		{
			AttributeHandler temp = e.getValue();
			if(expression.contains(temp.getNameattr()) || (expression.contains(temp.getNameorg()) 
					&& (temp.getNameorg()!="")))
			{
				exprAttributes.add(temp);
				//if the variable in the expression don't have the good format, we replace it
				if(temp.getNameorg()!="")
					expression=expression.replace(temp.getNameorg(), temp.getNameattr());
			}
			
		}
	}
	
//	/**
//	 * Call the Wrapper to calculate the expression
//	 * @return
//	 * @throws Exception
//	 */
//	private String calculateExpression() throws Exception
//	{
//		return wrapper.evaluate(expression, exprAttributes);
//	}
	
	
	public String getResult()
	{
		return result;
	}
	
	public String getExpression() {
		return expression;
	}
	
	@Override
	protected void setInitMessage() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setByValue(String value, boolean fromMapping) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setByValue(double value, boolean fromMapping) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setByKeyword(boolean fromMapping) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setByKeyword(String value, boolean fromMapping) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setByKeyword(double value, boolean fromMapping) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setByWCS(String value, boolean fromMapping) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setByWCS(double value, boolean fromMapping) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setByPixels(String value, boolean fromMapping) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setByPixels(double value, boolean fromMapping) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setByTableColumn(String value, boolean fromMapping) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setByTabeColumn(double value, boolean fromMapping) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setBySaada() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setValue(double value, String unit) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setNotSet() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setValue(String value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setValue(double value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setUnit(String unit) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getValue() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getNumValue() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getComment() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAttNameOrg() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAttNameAtt() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getUnit() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getUcd() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public static void main(String args[])
	{

		Map<String,AttributeHandler> mapTest = new TreeMap<String,AttributeHandler>();
		AttributeHandler ah1 = new AttributeHandler();
		AttributeHandler ah2 = new AttributeHandler();
		AttributeHandler ah3 = new AttributeHandler();
		AttributeHandler ah4 = new AttributeHandler();

		
		ah1.setNameattr("_tmin");
		ah1.setValue("11/06/2042");
		
		ah2.setNameattr("_emax");
		ah2.setNameorg("EMAX");
		ah2.setValue("42");
		
		ah3.setNameattr("_emin");
		ah3.setValue("6");
		
		ah4.setNameattr("_tmax");
		ah4.setValue("02/02/1995");
		mapTest.put(ah1.getNameattr(), ah1);
		mapTest.put(ah2.getNameattr(), ah2);
		mapTest.put(ah3.getNameattr(), ah3);
		mapTest.put(ah4.getNameattr(), ah4);
		String expr = "MJD(_tmin)+6-EMAX*_emin +MJD(_tmax)";
		String expr2="6+5+3+2";
		ColumnExpressionSetter test=null,test2=null;
		try {
			test = new ColumnExpressionSetter(expr, mapTest);
			test2 = new ColumnExpressionSetter(expr2, null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(test.getExpression());
		System.out.println(test.getResult());
		System.out.println(test2.getExpression());
		System.out.println(test2.getResult());
		
	}



	@Override
	public AttributeHandler getAssociateAtttribute() {
		// TODO Auto-generated method stub
		return null;
	}



}
