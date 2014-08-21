package saadadb.products.setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import net.objecthunter.exp4j.function.Function;
import saadadb.enums.ColumnSetMode;
import saadadb.exceptions.IgnoreException;
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
	 * The last expression evaluated
	 */
	 
	private String lastExpressionEvaluated;
	
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
	private String result="Not set";
	
	
	/**
	 * A ColumnSingleSetter used for the abstract methods from ColumnSetter and when mode=ByValue
	 */
	private ColumnSingleSetter singleSetter;
	
	/**
	 * The List of all numeric function used in the expression
	 */
	private ArrayList<Function> numericFunctionList;

	
	/**
	 * Constructor without keyword calculating the expression immediatly
	 * @param expr
	 * @throws Exception 
	 */
	public ColumnExpressionSetter(String expr) throws Exception
	{
		super();
		this.settingMode = ColumnSetMode.NOT_SET;
		if( expr == null ){
			IgnoreException.throwNewException(SaadaException.INTERNAL_ERROR, "Column setter cannot be set with a null expression");
		}
		//expression=expr;
		singleSetter = new ColumnSingleSetter();
		this.calculateExpression(expr);
	}
	
	/**
	 * Constructor with only one ah calculating the expression immediatly
	 * @param attr
	 * @throws Exception
	 */
	public ColumnExpressionSetter(AttributeHandler attr) throws Exception
	{
		super();
		this.settingMode = ColumnSetMode.NOT_SET;
		if( attr == null ){
			IgnoreException.throwNewException(SaadaException.INTERNAL_ERROR, "Column setter cannot be set with a null expression");
		}
		//expression=expr;
		singleSetter = new ColumnSingleSetter();
		exprAttributes= new ArrayList<AttributeHandler>();
		this.calculateExpression(attr);
	}
	
	/**
	 * Constructor with expression and list of ah calculating the expression immediatly
	 * @param expression
	 * @param attr
	 * @throws Exception
	 */
	public ColumnExpressionSetter(String expression,Map<String,AttributeHandler> attr) throws Exception
	{
		super();
		this.settingMode = ColumnSetMode.NOT_SET;
		if( attr == null ){
			IgnoreException.throwNewException(SaadaException.INTERNAL_ERROR, "Column setter cannot be set with a null expression");
		}
		//expression=expr;
		singleSetter = new ColumnSingleSetter();
		this.calculateExpression(expression,attr);
	} 
	
	/**
	 * Main constructor
	 */
	public ColumnExpressionSetter()
	{
		super();
		this.settingMode = ColumnSetMode.NOT_SET;
		singleSetter = new ColumnSingleSetter();
	}
	
	/**
	 * Set the expression to the specified value
	 * @param expr
	 */
	public void setExpression(String expr)
	{
		expression=expr;
	}
	


	
	/**
	 * Calculate the Expression using its stocked value
	 * @throws Exception 
	 */
	public void calculateExpression(Map<String,AttributeHandler> attributes) throws Exception
	{
		boolean isNewExpression = true;
		if( expression == null ){
			IgnoreException.throwNewException(SaadaException.INTERNAL_ERROR, "Column setter cannot be set with a null expression");
		}
		//We check if we're in the value or Expression case
		if(attributes==null || attributes.isEmpty())
		{
			this.settingMode=ColumnSetMode.BY_VALUE;
		}
		else
		{
			this.settingMode=ColumnSetMode.BY_EXPRESSION;
		}
		
		/*
		 * We treat the String functions
		 */
		this.checkAndExecStringFunction(attributes);

		if(settingMode==ColumnSetMode.BY_EXPRESSION)
		{
			/*
			 * We build the list of AH present in the expression and we format their names (in the expression)
			 * The String functions MUST have been treated at this point
			 */
			AttributeHandlerExtractor ahExtractor = new AttributeHandlerExtractor(expression, attributes);
			exprAttributes = ahExtractor.extractAH();
			expression=ahExtractor.expression;
		}	
		//We check if we already evaluated the same expression
		if(lastExpressionEvaluated!=null && expression.trim().equals(lastExpressionEvaluated.trim()))
		{
			isNewExpression=false;
		}
		
		
		/*
		 *\/!\ WARNING : We must handle the numerics functions here
		 */	
		ExtractNumericFunction();
		/*
		 * If an exception is risen in the Wrapper (Wrong value in ah, missins value), the result is set to "NULL"
		 */
		try {
			if(isNewExpression)
			{
				wrapper = new ExpressionWrapper(expression, exprAttributes,numericFunctionList);
				lastExpressionEvaluated=expression;
			}
			else
			{
				//If we did evaluate the same expression, we have no need to rebuild the expressionBuilder in the ExpressionWrapper
				wrapper.evaluate(exprAttributes,numericFunctionList);
			}
		} catch (Exception e) {
			e.printStackTrace();
			result=SaadaConstant.STRING;
		}
		//Check THIS
		if(settingMode==ColumnSetMode.BY_VALUE)
			singleSetter.setValue(result);
	}
	
	
	/**
	 * Calculate the Expression 
	 * @param expr the expression to calculate
	 * @param attributes the AttributHandlers which can be in the expression
	 * @throws Exception 
	 */
	public void calculateExpression(String expr,Map<String,AttributeHandler> attributes) throws Exception
	{
		boolean isNewExpression = true;
		if( expr == null ){
			IgnoreException.throwNewException(SaadaException.INTERNAL_ERROR, "Column setter cannot be set with a null expression");
		}
		expression=expr;
		/*
		 * We treat the String functions
		 */
		if(attributes==null || attributes.isEmpty())
		{
			this.settingMode=ColumnSetMode.BY_VALUE;
		}
		else
		{
			this.settingMode=ColumnSetMode.BY_EXPRESSION;
		}	
		this.checkAndExecStringFunction(attributes);
		if(settingMode==ColumnSetMode.BY_EXPRESSION)
		{
			/*
			 * We build the list of AH present in the expression and we format their names (in the expression)
			 * The String functions MUST have been treated at this point
			 */
			AttributeHandlerExtractor ahExtractor = new AttributeHandlerExtractor(expression, attributes);
			exprAttributes = ahExtractor.extractAH();
			expression=ahExtractor.expression;
		}
		//We check if we already evaluated the same expression
		if(lastExpressionEvaluated!=null && expression.trim().equals(lastExpressionEvaluated.trim()))
		{
			isNewExpression=false;
		}
		ExtractNumericFunction();
		/*
		 * If an exception is risen in the Wrapper (Wrong value in ah, missins value), the result is set to "NULL"
		 */
		try {
			if(isNewExpression)
			{
				wrapper = new ExpressionWrapper(expression, exprAttributes,numericFunctionList);
				lastExpressionEvaluated=expression;
			}
			else
			{
				//If we did evaluate the same expression, we have no need to rebuild the expressionBuilder in the ExpressionWrapper
				wrapper.evaluate(exprAttributes,numericFunctionList);
			}
			result=wrapper.getStringValue();
		} catch (Exception e) {
			e.printStackTrace();
			result=SaadaConstant.STRING;
		}
		if(settingMode==ColumnSetMode.BY_VALUE)
			singleSetter.setValue(result);
	}
	

	
	/**
	 * Calculate an Expression with no Keywords (ex : "(5+6+9)/20")
	 * @param expr The expression to calculate
	 * @throws Exception 
	 */
	public void calculateExpression(String expr) throws Exception
	{
		if( expr == null ){
			IgnoreException.throwNewException(SaadaException.INTERNAL_ERROR, "Column setter cannot be set with a null expression");
		}
		this.settingMode=ColumnSetMode.BY_VALUE;
		expression=expr;
		/*
		 * We treat the String functions
		 */
		this.checkAndExecStringFunction(null);

		/*
		 *\/!\ WARNING : We must handle the numerics functions here
		 */
		ExtractNumericFunction();
		/*
		 * If an exception is risen in the Wrapper (Wrong value in ah, missins value), the result is set to "NULL"
		 */
		try {
			wrapper = new ExpressionWrapper(expression, null,numericFunctionList);
			result=wrapper.getStringValue();
			singleSetter.setValue(result);
		} catch (Exception e) {
		//	e.printStackTrace();

			result=SaadaConstant.STRING;
		}
	}
	
	
	/**
	 * Calculate an Expression composed of one ah only
	 * @param expr The expression to calculate
	 * @throws Exception 
	 */
	public void calculateExpression(AttributeHandler ah) throws Exception
	{
		if( ah == null ){
			IgnoreException.throwNewException(SaadaException.INTERNAL_ERROR, "Column setter cannot be set with a null expression");
		}
		this.settingMode=ColumnSetMode.BY_EXPRESSION;
		expression=ah.getNameattr().toString();
		//If we only have on AH, we just get his value
		//System.out.println(ah.getValue());
		result=ah.getValue();
//		this.exprAttributes.add(ah);
//		/*
//		 * We treat the String functions
//		 */
//		this.checkAndExecStringFunction(null);
//
//		/*
//		 *\/!\ WARNING : We must handle the numerics functions here
//		 */
//		ExtractNumericFunction();
//		/*
//		 * If an exception is risen in the Wrapper (Wrong value in ah, missins value), the result is set to "NULL"
//		 */
//		try {
//			wrapper = new ExpressionWrapper(expression, exprAttributes,numericFunctionList);
//			result=wrapper.getStringValue();
//			singleSetter.setValue(result);
//		} catch (Exception e) {
//			System.out.println(expression);
//			e.printStackTrace();
//
//			//result=SaadaConstant.STRING;
//			//Dans le cas d'un seul Ah considéré comme une expression, si l'expressionWrapper ne peut pas le calculer on suppose qu'il
//			//s'agit d'un string et on lui donne la valeur de l'expression.
//			result=expression;
//		}
		
	}
	
	
	/**
	 * Add all numeric function to the ArrayList and treat the special case of the convert function
	 * @throws Exception
	 */
	private void ExtractNumericFunction() throws Exception
	{
		NumericFunctionExtractor extractor = new NumericFunctionExtractor(expression);
		numericFunctionList=extractor.extractFunction();
		expression=extractor.treatConvertFunction(exprAttributes);
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
				if(attributes!=null && !attributes.isEmpty())
				{
					//We replace the AH name by their values for each argument
					for(int i=0;i<args.length;i++)
					{
						args[i]=args[i].trim();
						//If between quote, it's not an AH
						if(!args[i].contains("\""))
						{
							for(Entry<String,AttributeHandler> ah : attributes.entrySet())
							{
								if((args[i].equals(ah.getValue().getNameattr())) || (args[i].equals(ah.getValue().getNameorg())))
								{
									args[i]=ah.getValue().getValue();
									stringFunctionArgumentsList.add(ah.getValue());
								}
							}
						}
						else
						{
							//we delete the quotes do use the function
							args[i]=args[i].replace("\"", "");
						}
					}
				}
				//We execute the String function
				expression=expression.replace(e.getKey(), DictionaryStringFunction.exec(e.getValue().functionName, args));	
			}		
		}
	}
	
	public String toString()
	{
		return "---- Expression calculée = "+expression+" //// Résultat obtenu : "+result;
	}
	
	
	public String getExpressionResult()
	{
		return result;
	}
	
	public double getNumExpressionResult()
	{
		return Double.valueOf(result);
	}
	
	public String getExpression() {
		return expression;
	}
	
	@Override
	protected void setInitMessage() {
		if(expression==null || expression.isEmpty())
			return;
		switch(this.settingMode){
		case BY_EXPRESSION:
			this.completeMessage("expression <"+this.singleSetter.getAssociateAtttribute().getValue()+">");
		case BY_VALUE:
			this.completeMessage("value <" +this.result+">");
			break;

		default:
			this.completeMessage("Nothing found");
			break;
		}
		
	}

	@Override
	public void setByValue(String value, boolean fromMapping) {
		this.singleSetter.setByValue(value, fromMapping);
			
	}

	@Override
	public void setByValue(double value, boolean fromMapping) {
		this.singleSetter.setByValue(value, fromMapping);		
	}

	@Override
	public void setByKeyword(boolean fromMapping) {
		this.singleSetter.setByKeyword(fromMapping);
	}

	@Override
	public void setByKeyword(String value, boolean fromMapping) {
		this.singleSetter.setByKeyword(value, fromMapping);
		
	}

	@Override
	public void setByKeyword(double value, boolean fromMapping) {
		this.singleSetter.setByKeyword(value, fromMapping);
		
	}

	@Override
	public void setByWCS(String value, boolean fromMapping) {
		this.singleSetter.setByWCS(value, fromMapping);
		
	}

	@Override
	public void setByWCS(double value, boolean fromMapping) {
		this.singleSetter.setByWCS(value, fromMapping);		
	}

	@Override
	public void setByPixels(String value, boolean fromMapping) {
		this.singleSetter.setByPixels(value, fromMapping);
		
	}

	@Override
	public void setByPixels(double value, boolean fromMapping) {
		this.singleSetter.setByPixels(value, fromMapping);
		
	}

	@Override
	public void setByTableColumn(String value, boolean fromMapping) {
		this.singleSetter.setByTableColumn(value, fromMapping);
		
	}

	@Override
	public void setByTabeColumn(double value, boolean fromMapping) {
		this.singleSetter.setByTabeColumn(value, fromMapping);
		
	}

	@Override
	public void setBySaada() {
		this.singleSetter.setBySaada();
		
	}

	@Override
	public void setValue(double value, String unit) {
		this.singleSetter.setValue(value, unit);
		
	}

	@Override
	public void setNotSet() {
		this.singleSetter.setNotSet();
		
	}

	@Override
	public void setValue(String value) {
		this.singleSetter.setValue(value);
		this.result=value;
		
	}

	@Override
	public void setValue(double value) {
		this.singleSetter.setValue(value);
		
	}

	@Override
	public void setUnit(String unit) {
		this.singleSetter.setUnit(unit);
		
	}

	@Override
	public String getValue() {
		return this.result;
	}

	@Override
	public double getNumValue() {
		return Double.valueOf(this.result);
	}

	@Override
	public String getComment() {
		return this.singleSetter.getComment();
	}

	@Override
	public String getAttNameOrg() {
		return this.singleSetter.getAttNameOrg();
	}

	@Override
	public String getAttNameAtt() {
		return this.singleSetter.getAttNameAtt();
	}

	@Override
	public String getUnit() {
		return this.singleSetter.getUnit();
	}

	@Override
	public String getUcd() {
		return this.singleSetter.getUcd();
	}
	
	@Override
	public AttributeHandler getAssociateAtttribute() {
		return this.singleSetter.getAssociateAtttribute();
	}
	public static void main(String args[])
	{
		//Tests
		
		Map<String,AttributeHandler> mapTest = new TreeMap<String,AttributeHandler>();
		AttributeHandler ah1 = new AttributeHandler();
		AttributeHandler ah2 = new AttributeHandler();
		AttributeHandler ah3 = new AttributeHandler();
		AttributeHandler ah4 = new AttributeHandler();
		AttributeHandler ah5 = new AttributeHandler();
		ah1.setNameattr("_tmin");
		ah1.setValue("11/20/1858"); //=3
		
		ah2.setNameattr("_emax");
		ah2.setNameorg("EMAX");
		ah2.setValue("10");
		
		ah3.setNameattr("_emin");
		ah3.setValue("2");
		
		ah4.setNameattr("_tmax");
		ah4.setValue("11/22/1858");//=5
		
		ah5.setNameattr("_emin59");
		ah5.setValue("1");
		
		//Test calcul constante
		String expression = "42";
		ColumnExpressionSetter ces = new ColumnExpressionSetter();
		System.out.println("Test de calcul d'une valeur constante seule");
		System.out.println("---- Résultat souhaité : "+expression);
		try {
			ces.calculateExpression(expression);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		System.out.println(ces);
		System.out.println();

		
		//Test calcul d'une opération de constante
		expression = "6*7";
		ces = new ColumnExpressionSetter();
		System.out.println("Test de calcul d'une opération de constante");
		System.out.println("---- Résultat souhaité : 42");
		try {
			ces.calculateExpression(expression);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		System.out.println(ces);
		System.out.println();
		
		
		//Test opération de keywords
		mapTest.put("_emax", ah2);
		mapTest.put("_emin", ah3);
		mapTest.put("_emin59", ah5);
		expression = "EMAX*_emin+_emin59";
		ces = new ColumnExpressionSetter();
		System.out.println("Test de calcul d'une opération de keywords");
		System.out.println("---- Résultat souhaité : 21");
		try {
			ces.calculateExpression(expression,mapTest);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		System.out.println(ces);
		System.out.println();

	
		//Test opération de fonctions avec arguments constants
		expression = "MJD(11/22/1858) - MJD(11/19/1858)";
		ces = new ColumnExpressionSetter();
		System.out.println("Test de calcul de fonctions avec arguments contants");
		System.out.println("---- Résultat souhaité : 3");
		try {
			ces.calculateExpression(expression);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		System.out.println(ces);
		System.out.println();

		//Test opération de fonctions avec arguments variables
		mapTest.clear();
		mapTest.put("_tmin", ah1);
		mapTest.put("_tmax", ah4);
		expression = "80/ (MJD(_tmin) + MJD(_tmax))";
		ces = new ColumnExpressionSetter();
		System.out.println("Test de calcul de fonctions avec arguments variables");
		System.out.println("---- Résultat souhaité : 10");
		try {
			ces.calculateExpression(expression,mapTest);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		System.out.println(ces);
		System.out.println();

		
		//Test Appel fonction expression vide
		expression = "";
		ces = new ColumnExpressionSetter();
		System.out.println("Test de calcul expression vide");
		System.out.println("---- Résultat souhaité : Exception");
		try {
			ces.calculateExpression(expression);
		} catch (Exception e1) {

			e1.printStackTrace();
		}
		System.out.println(ces);		
		System.out.println();

		
		//Test Appel fonction expression vide
		expression = null;
		ces = new ColumnExpressionSetter();
		System.out.println("Test de calcul expression null");
		System.out.println("---- Résultat souhaité : Exception");
		try {
			ces.calculateExpression(expression);
		} catch (Exception e1) {

			e1.printStackTrace();
		}
		System.out.println(ces);
		System.out.println();
		
		//Test calcul expression de constante avec map remplie
		expression = "(5+2)*6";
		ces = new ColumnExpressionSetter();
		System.out.println("Test de calcul expression de constante avec map remplie");
		System.out.println("---- Résultat souhaité : 42");
		try {
			ces.calculateExpression(expression,mapTest);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		System.out.println(ces);
		System.out.println();
		
		//Test calcul expression de keywords avec map vide
		mapTest.clear();
		expression = "_emin+_emax";
		ces = new ColumnExpressionSetter();
		System.out.println("Test de calcul expression de keyword avec map vide");
		System.out.println("---- Résultat souhaité : Exception");
		try {
			ces.calculateExpression(expression,mapTest);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		System.out.println(ces);
		
		//Test calcul expression de keywords avec map null
		mapTest=null;
		expression = "_emin+_emax";
		ces = new ColumnExpressionSetter();
		System.out.println("Test de calcul expression de keyword avec map null");
		System.out.println("---- Résultat souhaité : Exception");
		try {
			ces.calculateExpression(expression,mapTest);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		System.out.println(ces);


		System.out.println();
		mapTest = new TreeMap<String,AttributeHandler>();

		mapTest.put("_emax", ah2);
		mapTest.put("_emin", ah3);
		expression = "EMAX*_emin";
		ces = new ColumnExpressionSetter();
		System.out.println("Test Passage expression Wrapper");
		System.out.println("---- Résultat souhaité : 20");
		try {
			ces.calculateExpression(expression,mapTest);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		System.out.println(ces);
		System.out.println();

		

		System.out.println();
		mapTest = new TreeMap<String,AttributeHandler>();

		mapTest.put("_emax", ah2);
		mapTest.put("_emin", ah3);
		expression = "EMAX*_emin";
		System.out.println("Test Passage expression Wrapper");
		System.out.println("---- Résultat should be the same");
		try {
			ces.calculateExpression(expression,mapTest);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		System.out.println(ces);
		System.out.println();
		
		
		
		mapTest.clear();
		mapTest.put("_emax", ah2);
		mapTest.put("_emin59", ah5);
		expression = "EMAX+_emin59";
		System.out.println("Result should be different");
		try {
			ces.calculateExpression(expression,mapTest);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		System.out.println(ces);
		System.out.println();
		
		
		expression="toRadian(180)+toRadian(90)";
		System.out.println("Result =3.14...");
		try {
			ces.calculateExpression(expression,null);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		System.out.println(ces);
		System.out.println();
		
		mapTest.put("_emax", ah2);
		mapTest.put("_emin", ah3);
		expression="toRadian(10) + _emax + convert(_emin,mm,m)";
		System.out.println("Result =12.17...");
		try {
			ces.calculateExpression(expression,mapTest);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		System.out.println(ces);
		System.out.println();
	}
}
