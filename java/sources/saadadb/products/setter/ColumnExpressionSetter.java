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
		if( expression == null ){
			FatalException.throwNewException(SaadaException.INTERNAL_ERROR, "Column setter cannot be set with a null expression");
		}
		expression=expr;
		wrapper = new ExpressionWrapper();

		if(attributes!=null)
		{
			/*
			 * We build the list of AH present in the expression and we format their names (in the expression)
			 */
			exprAttributes = new ArrayList<AttributeHandler>();
			buildAttributeList(attributes);
		}
		/*
		 * We treat the String functions
		 */
		execStringFunction();
		
		/*
		 *\/!\ WARNING : We must handle the numerics functions here
		 */
		
		
		/*
		 * If an exception is risen in the Wrapper (Wrong value in ah, missins value), the result is set to "NULL"
		 */
		try {
			result=calculateExpression();
		} catch (Exception e) {
			result=SaadaConstant.STRING;
		}
	}
	
	/**
	 * Look for String functions and stock them. the function also modify the value of "expression"
	 * @return true if a String function have been found
	 */
	private boolean checkStringFunction()
	{
		boolean functionFound=false;
		Matcher matcher;
		Pattern pattern;
		stringFunctionList=new TreeMap<String,String>();
		String key="SF";
		int i=0;
		/*
		 * For all our Dictionary functions
		 */
		for(Entry<String,String> e:DictionaryString.index.entrySet())
		{
			/*
			 * We check in the expression if there's a string like "... Function(something) ... "
			 */
			pattern=Pattern.compile(e.getKey()+"\\([^()]*\\)");
			matcher=pattern.matcher(expression);
			//if we find a match, we're stocking it in the stringFunctionList
			while(matcher.find())
			{
				i++;
				functionFound=true;
				expression=expression.replace(matcher.group(), key+i);
				stringFunctionList.put(key+i, matcher.group());
			}
		}
		return functionFound;
		
	}
	
	/**
	 * Execute the String functions stocked in the map StringFunctionList and give the correct value to Expression
	 * @throws Exception 
	 * 
	 */
	private void execStringFunction() throws Exception
	{
		//We do something only if a String function have been found

		if(checkStringFunction())
		{
			//This regex allow us to get the params from the functions
			Pattern paramPattern=Pattern.compile("\\(([^)]+)\\)");
			//This regex allow us to obtain the fuction name
			Pattern functionNamePattern=Pattern.compile("([^()]*)");
			Matcher matcher;
			String tempArgs="";
			String functionName="";
			String[] functionArgs=null;
			
			//first step : we separate the function name from the parameters.
			
			for(Entry<String,String> e : stringFunctionList.entrySet())
			{
				//We get the arguments from the function
				matcher=paramPattern.matcher(e.getValue());
				if(matcher.find())
					tempArgs=matcher.group(1);
				
				//We delete the quotes from the arguments
				tempArgs=tempArgs.replace("\"","");
				
				//Separate the differents paramater from a same function
				functionArgs =tempArgs.split("\\s*,\\s*");
				
				//We get the function name
				matcher=functionNamePattern.matcher(e.getValue());
				if(matcher.find())
					 functionName = matcher.group();
				
				/*
				 * second step, we're replacing the ah name by their value
				 */
				for(int i=0;i<functionArgs.length;i++)
				{
					for(AttributeHandler ah : exprAttributes)
					{
						if(functionArgs[i].contains(ah.getNameattr()))
						{
							functionArgs[i]=ah.getValue();
						}
					}
				}
				/*
				 * third and last step : We replacing the function by its result in the expression
				 */
				expression=expression.replace(e.getKey(), DictionaryString.exec(functionName, functionArgs));

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
			if(expression.contains(temp.getNameattr()) || expression.contains(temp.getNameorg()))
			{
				exprAttributes.add(temp);
				//if the variable in the expression don't have the good format, we replace it
				expression=expression.replace(temp.getNameorg(), temp.getNameattr());
			}
			
		}
	}
	
	/**
	 * Call the Wrapper to calculate the expression
	 * @return
	 * @throws Exception
	 */
	private String calculateExpression() throws Exception
	{
		return wrapper.evaluate(expression, exprAttributes);
	}
	
	
	public String getResult()
	{
		return result;
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
		Map<String,String> test = new TreeMap<String,String>();
		Matcher matcher;
		String expr="MJD(_tmin)+MJD(\"06/07/1990\")+toLower(_facility)";
		Pattern pattern;
		int i=0;
		/*
		 * For all our Dictionary functions
		 */
		for(Entry<String,String> e:DictionaryString.index.entrySet())
		{

			pattern=Pattern.compile(e.getKey()+"\\([^()]*\\)");
			matcher=pattern.matcher(expr);
			while (matcher.find())
			{

					i++;
					//System.out.println(matcher.group());

					expr=expr.replace(matcher.group(), "Test"+i);
					test.put("Test"+i, matcher.group());

			}
			
		}
	//	System.out.println(expr);
		
		
		
		//Exec String Function test
		
		String temp = "";
		List<AttributeHandler> attributes = new ArrayList<AttributeHandler>();
		AttributeHandler ah1=new AttributeHandler();
		ah1.setNameattr("_tmin");
		ah1.setValue("12/07/2014");
		attributes.add(ah1);
		AttributeHandler ah2=new AttributeHandler();
		ah2.setNameattr("_facility");
		ah2.setValue("HELLOWORLD");
		attributes.add(ah2);
		String funcName ="";
		pattern=Pattern.compile("\\(([^)]+)\\)");
		Pattern pattern2=Pattern.compile("([^()]*)");
		for(Entry<String,String>e:test.entrySet())
		{
			matcher=pattern.matcher(e.getValue());
			if(matcher.find())
				temp=matcher.group(1);
			//Permet de récupérer tous les params)
			
			temp=temp.replace("\"","");
			String[] arg =temp.split("\\s*,\\s*");
			
//			for(String s:arg)
//			{
//				System.out.println("ARGS ="+s);
//			}
			matcher=pattern2.matcher(e.getValue());
			if(matcher.find())
				 funcName = matcher.group();
			
			for(int j=0;j<arg.length;j++)
			{
				for(AttributeHandler ah : attributes)
				{
					//System.out.println("ARGS ="+s);
					//System.out.println("AH ="+ah.getNameattr());
					if(arg[j].contains(ah.getNameattr()))
					{
						arg[j]=ah.getValue();
						//System.out.println("AHAHA:"+s);
					}
				}

			}
			System.out.println("HORS BOUCLE:"+arg[0]);
			
			
			
			
			
			try {
				expr=expr.replace(e.getKey(), DictionaryString.exec(funcName, arg));
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			System.out.println(expr);
		}
	}

}
