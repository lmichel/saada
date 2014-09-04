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
	private List<AttributeHandler> exprAttributes = new ArrayList<AttributeHandler>();;


	/**
	 * The list of attribute being String functions arguments (names of AH)
	 */
	private List<AttributeHandler> stringFunctionArgumentsList = new ArrayList<AttributeHandler>();

	/**
	 * The result of the expression
	 */
	private String result="NotSet";

	/**
	 * A ColumnSingleSetter used for the abstract methods from ColumnSetter and when mode=ByValue
	 */
	//private ColumnSingleSetter singleSetter;

	/**
	 * The List of all numeric function used in the expression
	 */
	private ArrayList<Function> numericFunctionList;

	//	/**
	//	 * Boolean indicating if we've override the automatic detection of settingmode
	//	 */
	//	private boolean columnModeForced=false;

	private StringFunctionExtractor stringFunctionExtractor;
	private NumericFunctionExtractor numericFunctionExtractor;
	private AttributeHandler singleAttributeHandler;
	private boolean singleStringExpression = false;
	private String unit="";
	private String ucd="";

	private INIT computingMode;
	enum INIT{
		CONSTANT_VALUE,
		CONSTANT_EXPRESSION,
		SINGLE_ATTRIBUTE,
		MULTI_ATTRIBUTE,
	};
	/**
	 * Constructor without keyword. The expression is considered as constant
	 * @param constantValue
	 * @throws Exception 
	 */
	public ColumnExpressionSetter(String constantValue) throws Exception	{
		super();
		if( constantValue == null ){
			IgnoreException.throwNewException(SaadaException.INTERNAL_ERROR, "Column setter cannot be set with a null expression");
		}
		//expression=expr;
		this.settingMode = ColumnSetMode.BY_VALUE;
		this.expression = this.result = constantValue;
		this.computingMode = INIT.CONSTANT_VALUE;
	}

	/**
	 * Constructor with only one ah.
	 * The expression is set with the name of the attribute handler/ Its value will be taker as setter value
	 * @param attr
	 * @throws Exception
	 */
	public ColumnExpressionSetter(AttributeHandler attr) throws Exception {
		super();
		this.settingMode = ColumnSetMode.NOT_SET;
		if( attr == null ){
			IgnoreException.throwNewException(SaadaException.INTERNAL_ERROR, "Column setter cannot be set with a null expression");
		}
		//expression=expr;
		this.exprAttributes= new ArrayList<AttributeHandler>();
		this.setExpression(attr.getNameorg());
		this.singleAttributeHandler = attr;
		this.settingMode = ColumnSetMode.BY_KEYWORD;
		this.computingMode = INIT.SINGLE_ATTRIBUTE;
		this.calculateExpression(null);
	}

	/**
	 * If the AH map is null, the expression is considered as a set of operations on constant values. It is computed once.
	 * @param expression arithmetic expression
	 * @param attributes map of the AH possibly used by the expression
	 * @throws Exception
	 */
	public ColumnExpressionSetter(String expression, Map<String,AttributeHandler> attributes) throws Exception{
		super();
		if( attributes == null ){
			this.computingMode = INIT.CONSTANT_EXPRESSION;
		} else {
			this.computingMode = INIT.MULTI_ATTRIBUTE;
		}
		this.setExpression(expression);
		this.calculateExpression(attributes);
	}

	/**
	 * Constructor with only one ah calculating the expression immediatly, the setting mode is manually given
	 * @param attr
	 * @throws Exception
	 */
	public ColumnExpressionSetter(AttributeHandler attr, ColumnSetMode mode) throws Exception {
		this(attr);
		this.settingMode = mode;
	}


	/**
	 * Override the auto-mode détection
	 */
	public ColumnExpressionSetter()	{
		super();
		this.settingMode = ColumnSetMode.NOT_SET;
	}

	/**
	 * @param expression
	 * @throws Exception
	 */
	private void setExpression(String expression) throws Exception{
		if( expression == null ){
			IgnoreException.throwNewException(SaadaException.INTERNAL_ERROR, "Column setter cannot be set with a null expression");
		}
		this.stringFunctionExtractor = new StringFunctionExtractor(expression);
		if( this.stringFunctionExtractor.isSingleStringExpression() ) {
			this.singleStringExpression = true;
			this.wrapper = null;
			this.expression = this.stringFunctionExtractor.expression;		
			if( this.useKeywords() ) {
				this.settingMode = ColumnSetMode.BY_EXPRESSION;
			} else {
				for(Entry<String,StringFunctionDescriptor> e : stringFunctionExtractor.splitedFunctionsMap.entrySet()) {
					this.expression = this.expression.replace(e.getKey()
							, DictionaryStringFunction.exec(e.getValue().functionName
									, e.getValue().getUnquotedArguments()));	
				}
				this.result = this.expression;
				this.settingMode = ColumnSetMode.BY_VALUE;
			}
		} else {
			this.expression = this.stringFunctionExtractor.expression;
			if( this.stringFunctionExtractor.splitedFunctionsMap.size() == 0 ){
				this.stringFunctionExtractor = null;
			} 
			System.out.println("expr " + this.expression );
			this.settingMode = ColumnSetMode.BY_EXPRESSION;
			if( this.stringFunctionExtractor  != null && !this.stringFunctionExtractor.useKeywords() ){
				for(Entry<String,StringFunctionDescriptor> e : stringFunctionExtractor.splitedFunctionsMap.entrySet()) {
					this.expression = this.expression.replace(e.getKey()
							, DictionaryStringFunction.exec(e.getValue().functionName
									, e.getValue().getUnquotedArguments()));	
				}
				this.stringFunctionExtractor = null;
			}
			this.extractNumericFunctions();
			this.expression=this.expression.replace("'", "");
		}
	}

	/**
	 * @param attributes
	 * @throws Exception
	 */
	public void calculateExpression() throws Exception{
		if( this.computingMode == INIT.CONSTANT_VALUE){

		}
		try {
			if( this.settingMode == ColumnSetMode.BY_KEYWORD){
				System.out.println("by kw");
				this.result=this.singleAttributeHandler.getValue();				
			} else 	if( this.settingMode == ColumnSetMode.BY_VALUE || this.settingMode == ColumnSetMode.NOT_SET){
				System.out.println("by value");
				return;
			} else if( this.settingMode == ColumnSetMode.BY_EXPRESSION ){
				if( this.stringFunctionExtractor!= null ){
					System.out.println("evl string");
					this.execStringFunction();
				}
				if( !this.singleStringExpression ){

					System.out.println("compute expression");
					//				AttributeHandlerExtractor ahExtractor = new AttributeHandlerExtractor(expression, attributes);
					//				this.exprAttributes = ahExtractor.extractAH();
					//				this.expression=ahExtractor.expression;
					if( !this.expression.trim().equals(this.lastExpressionEvaluated)) {
						System.out.println("new expression");
						this.lastExpressionEvaluated = this.expression.trim();
						this.wrapper = new ExpressionWrapper(this.expression, this.exprAttributes, this.numericFunctionList);
						this.wrapper.evaluate(this.exprAttributes, this.numericFunctionList);
						this.result=wrapper.getStringValue();
						if( this.exprAttributes.size() == 0 && !(this.stringFunctionExtractor != null && this.stringFunctionExtractor.useKeywords()) ){
							this.settingMode = ColumnSetMode.BY_VALUE;
						} 
					} else {
						System.out.println("same expression");
					}
				} else {
					this.result = this.expression;					
				}
			} else {
				this.result = this.expression;
				System.out.println("stringMode");

			}
		} catch (Exception e) {
			this.result = SaadaConstant.STRING;
			this.settingMode = ColumnSetMode.NOT_SET;
			this.completeMessage("exp failed: " + e.getMessage());
		}
	}

	/**
	 * @param attributes
	 * @throws Exception
	 */
	private void calculateExpression(Map<String,AttributeHandler> attributes) throws Exception{
		try {
			if( this.settingMode == ColumnSetMode.BY_KEYWORD){
				System.out.println("by kw");
				this.result=this.singleAttributeHandler.getValue();				
			} else 	if( this.settingMode == ColumnSetMode.BY_VALUE || this.settingMode == ColumnSetMode.NOT_SET){
				System.out.println("by value");
				return;
			} else if( this.settingMode == ColumnSetMode.BY_EXPRESSION ){
				if( this.stringFunctionExtractor!= null ){
					System.out.println("evl string");
					this.execStringFunction(attributes);
				} else if( !this.singleStringExpression ){
					System.out.println("compute expression");
					AttributeHandlerExtractor ahExtractor = new AttributeHandlerExtractor(expression, attributes);
					this.exprAttributes = ahExtractor.extractAH();
					this.expression=ahExtractor.expression;
					if( !this.expression.trim().equals(this.lastExpressionEvaluated)) {
						System.out.println("new expression");
						this.lastExpressionEvaluated = this.expression.trim();
						this.wrapper = new ExpressionWrapper(expression, exprAttributes,numericFunctionList);
						this.wrapper.evaluate(this.exprAttributes, this.numericFunctionList);
						this.result=wrapper.getStringValue();
						if( this.exprAttributes.size() == 0 && !(this.stringFunctionExtractor != null && this.stringFunctionExtractor.useKeywords()) ){
							this.settingMode = ColumnSetMode.BY_VALUE;
						} 
					} else {
						System.out.println("same expression");
					}
				}
			} else {
				this.result = this.expression;
				System.out.println("stringMode");

			}
		} catch (Exception e) {
			this.result = SaadaConstant.STRING;
			this.settingMode = ColumnSetMode.NOT_SET;
			this.completeMessage("exp failed: " + e.getMessage());
		}
	}

	/**
	 * Execute the String functions in the expression and replaced them by their values
	 * @throws Exception 
	 * 
	 */
	private void execStringFunction(Map<String,AttributeHandler> attributes) throws Exception {
		System.out.println("string expression");
		//We're getting back the new expression (with flags)
		this.expression=this.stringFunctionExtractor.expression;
		//for each function
		boolean attributeFound =false;

		for(Entry<String,StringFunctionDescriptor> e : this.stringFunctionExtractor.splitedFunctionsMap.entrySet()){
			String[] args = e.getValue().functionArguments;
			String[] values = new String[args.length];
			if(attributes!=null && !attributes.isEmpty()) {
				//We replace the AH name by their values for each argument
				for(int i=0;i<args.length;i++) {
					attributeFound = false;
					args[i]=args[i].trim();
					//If between quote, it's not an AH
					if(!(args[i].startsWith("\"") || args[i].startsWith("'"))) {
						for(AttributeHandler ah : attributes.values()) {
							if( ah.isNamedLike(args[i])) {
								attributeFound=true;
								values[i]= ah.getValue();
								this.stringFunctionArgumentsList.add(ah);
							}
						}
						if(attributeFound==false)
							IgnoreException.throwNewException(IgnoreException.WRONG_PARAMETER, "Attribute " + args[i] + " referenced by the expression not found in the product attributes");

					} else {
						//we delete the quotes do use the function
						values[i]=args[i].replaceAll("[\"']", "");
					}
				}
			} else {
				/* If there is no attributes
				 * For each argument, we delete the quotes, if there is no quotes, this is an invalid argument
				 */
				for(int i=0;i<args.length;i++) {
					if( (args[i].startsWith("\"") || args[i].startsWith("'"))) {
						values[i]=args[i].replaceAll("[\"']", "");
					} else
						IgnoreException.throwNewException(IgnoreException.WRONG_PARAMETER, "The String function arguments must be quoted strings or keywords");
				}
			}
			//We execute the String function
			this.expression=this.expression.replace(e.getKey(), DictionaryStringFunction.exec(e.getValue().functionName, values));	
		}		
	}

	/**
	 * Compute the string function after the the AH used by them have been extracted.
	 * Update the expression with the results of this computation
	 * @throws Exception
	 */
	private void execStringFunction() throws Exception {
		System.out.println("string expression");
		//We're getting back the new expression (with flags)
		this.expression=this.stringFunctionExtractor.expression;
		//for each function
		boolean attributeFound =false;

		for(Entry<String,StringFunctionDescriptor> e : this.stringFunctionExtractor.splitedFunctionsMap.entrySet()){
			String[] args = e.getValue().functionArguments;
			String[] values = new String[args.length];
			if(this.stringFunctionArgumentsList!=null && !this.stringFunctionArgumentsList.isEmpty()) {
				//We replace the AH name by their values for each argument
				for(int i=0;i<args.length;i++) {
					attributeFound = false;
					args[i]=args[i].trim();
					//If between quote, it's not an AH
					if(!(args[i].startsWith("\"") || args[i].startsWith("'"))) {
						for(AttributeHandler ah : this.stringFunctionArgumentsList) {
							if( ah.isNamedLike(args[i])) {
								attributeFound=true;
								values[i]= ah.getValue();
							}
						}
						if(attributeFound==false)
							IgnoreException.throwNewException(IgnoreException.WRONG_PARAMETER, "Attribute " + args[i] + " referenced by the expression not found in the product attributes");

					} else {
						//we delete the quotes do use the function
						values[i]=args[i].replaceAll("[\"']", "");
					}
				}
			} else {
				/* If there is no attributes
				 * For each argument, we delete the quotes, if there is no quotes, this is an invalid argument
				 */
				for(int i=0;i<args.length;i++) {
					if( (args[i].startsWith("\"") || args[i].startsWith("'"))) {
						values[i]=args[i].replaceAll("[\"']", "");
					} else
						IgnoreException.throwNewException(IgnoreException.WRONG_PARAMETER, "The String function arguments must be quoted strings or keywords");
				}
			}
			//We execute the String function
			this.expression=this.expression.replace(e.getKey(), DictionaryStringFunction.exec(e.getValue().functionName, values));	
		}		
	}

	/**
	 * Calculate the Expression 
	 * @param expr the expression to calculate
	 * @param attributes the AttributHandlers which can be in the expression
	 * @throws Exception 
	 */
	//	public void calculateExpression(String expr,Map<String,AttributeHandler> attributes) throws Exception
	//	{
	//		boolean isNewExpression = true;
	//		if( expr == null ){
	//			IgnoreException.throwNewException(SaadaException.INTERNAL_ERROR, "Column setter cannot be set with a null expression");
	//		}
	//		//if the attributes list is null, we call the calculateExpr without ah
	//		if(attributes!=null && !attributes.isEmpty())
	//		{
	//			expression=expr;
	//			this.completeMessage("expression <" + expr+ "> ");
	//
	//			/*
	//			 * We treat the String functions
	//			 */
	//			if(!columnModeForced)
	//			{//We check if we're in the value or Expression case
	//				if(attributes==null || attributes.isEmpty())
	//				{
	//					this.settingMode=ColumnSetMode.BY_VALUE;
	//				}
	//				else
	//				{
	//					this.settingMode=ColumnSetMode.BY_EXPRESSION;
	//				}
	//			}
	//			this.checkAndExecStringFunction(attributes);
	//			System.out.println("@@@@@@@@@@ " + this.expression);
	//			if(settingMode==ColumnSetMode.BY_EXPRESSION)
	//			{
	//				/*
	//				 * We build the list of AH present in the expression and we format their names (in the expression)
	//				 * The String functions MUST have been treated at this point
	//				 */
	//				AttributeHandlerExtractor ahExtractor = new AttributeHandlerExtractor(expression, attributes);
	//				exprAttributes = ahExtractor.extractAH();
	//				expression=ahExtractor.expression;
	//			}
	//			//We check if we already evaluated the same expression
	//			if(lastExpressionEvaluated!=null && expression.trim().equals(lastExpressionEvaluated.trim()))
	//			{
	//				isNewExpression=false;
	//			}
	//			ExtractNumericFunction();
	//			expression=expression.replace("'", "");
	//			/*
	//			 * If an exception is risen in the Wrapper (Wrong value in ah, missins value), the result is set to "NULL"
	//			 */
	//			try {
	//				if(isNewExpression)
	//				{
	//					wrapper = new ExpressionWrapper(expression, exprAttributes,numericFunctionList);
	//					lastExpressionEvaluated=expression;
	//				}
	//				else
	//				{
	//					//If we did evaluate the same expression, we have no need to rebuild the expressionBuilder in the ExpressionWrapper
	//					wrapper.evaluate(exprAttributes,numericFunctionList);
	//				}
	//				result=wrapper.getStringValue();
	//				singleSetter.setValue(result);
	//			} catch (Exception e) {
	//				//Messenger.printStackTrace(e);
	//				result=SaadaConstant.STRING;
	//				settingMode = ColumnSetMode.NOT_SET;
	//				//IgnoreException.throwNewException(SaadaException.SYNTAX_ERROR, e);
	//			}
	//			if(settingMode==ColumnSetMode.BY_VALUE)
	//				singleSetter.setValue(result);
	//		}
	//		else
	//		{
	//			this.calculateExpression(expr);
	//		}
	//	}
	/**
	 * Calculate an Expression with no Keywords (ex : "(5+6+9)/20")
	 * @param expr The expression to calculate
	 * @throws Exception 
	 */
	//	public void calculateExpression(String expr) throws Exception
	//	{
	//		if( expr == null ){
	//			IgnoreException.throwNewException(SaadaException.INTERNAL_ERROR, "Column setter cannot be set with a null expression");
	//		}
	//		if(!columnModeForced)
	//			this.settingMode=ColumnSetMode.BY_VALUE;
	//		expression=expr;
	//		this.completeMessage("value <" + expr+ "> ");
	//
	//		/*
	//		 * We treat the String functions
	//		 */
	//		this.checkAndExecStringFunction(null);
	//		/*
	//		 *\/!\ WARNING : We must handle the numerics functions here
	//		 */
	//		ExtractNumericFunction();
	//		expression=expression.replace("'", "");
	//		/*
	//		 * If an exception is risen in the Wrapper (Wrong value in ah, missins value), the result is set to "NULL"
	//		 */
	//		try {
	//			wrapper = new ExpressionWrapper(expression, null,numericFunctionList);
	//			result=wrapper.getStringValue();
	//			singleSetter.setValue(result);
	//		} catch (Exception e) {
	//		//	e.printStackTrace();
	//
	//			//Messenger.printStackTrace(e);
	//			result=expression;
	//			//IgnoreException.throwNewException(SaadaException.SYNTAX_ERROR, e);
	//		}
	//	}


	//	/**
	//	 * Calculate an Expression composed of one ah only
	//	 * @param expr The expression to calculate
	//	 * @throws Exception 
	//	 */
	//	public void calculateExpression(AttributeHandler ah) throws Exception
	//	{
	//		if( ah == null ){
	//			IgnoreException.throwNewException(SaadaException.INTERNAL_ERROR, "Column setter cannot be set with a null expression");
	//		}
	//		if(!columnModeForced)	
	//			this.settingMode=ColumnSetMode.BY_EXPRESSION;
	//		expression=ah.getNameattr().toString();
	//		this.completeMessage("Attribute <" + ah.getNameorg()+ ah.getUnit()+ "> ");
	//
	//		//If we only have on AH, we just get his value
	//		//System.out.println(ah.getValue());
	//		result=ah.getValue();
	//		singleSetter.setValue(result);
	//
	//	}

	/**
	 * @return
	 */
	private boolean useKeywords() {
		if( this.stringFunctionExtractor.useKeywords()){
			return true;
		} else if( exprAttributes.size() > 0 ){
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Add all numeric function to the ArrayList and treat the special case of the convert function
	 * @throws Exception
	 */
	private void extractNumericFunctions() throws Exception
	{
		this.numericFunctionExtractor = new NumericFunctionExtractor(expression);
		this.numericFunctionList=numericFunctionExtractor.extractFunctions();
		this.expression=numericFunctionExtractor.treatConvertFunction(exprAttributes);
	}

	//	/**
	//	 * Execute the String functions in the expression and replaced them by their values
	//	 * @throws Exception 
	//	 * 
	//	 */
	//	private void checkAndExecStringFunction(Map<String,AttributeHandler> attributes) throws Exception
	//	{
	//		StringFunctionExtractor extractor = new StringFunctionExtractor(this.expression);
	//		//We only do something if a String function have been found
	//		if(extractor.extractAndSplit()) {
	//
	//			//stringFunctionArgumentsList= new ArrayList<AttributeHandler>();
	//			//We're getting back the new expression (with flags)
	//			this.expression=extractor.expression;
	//			//for each function
	//			boolean attributeFound =false;
	//			for(Entry<String,StringFunctionDescriptor> e : extractor.splitedFunctionsMap.entrySet())
	//			{
	//				String[] args = e.getValue().functionArguments;
	//				if(attributes!=null && !attributes.isEmpty())
	//				{
	//					//We replace the AH name by their values for each argument
	//					for(int i=0;i<args.length;i++)
	//					{
	//						attributeFound = false;
	//						args[i]=args[i].trim();
	//						//If between quote, it's not an AH
	//						if(!(args[i].startsWith("\"") || args[i].startsWith("'")))
	//						{
	//							for(AttributeHandler ah : attributes.values())
	//							{
	//								if( ah.isNamedLike(args[i]))
	//								{
	//									attributeFound=true;
	//									//args[i]= ah.getValue();
	//									//stringFunctionArgumentsList.add(ah);
	//								}
	//							}
	//							if(attributeFound==false)
	//								IgnoreException.throwNewException(IgnoreException.WRONG_PARAMETER, "Attribute " + args[i] + " referenced by the expression not found in the product attributes");
	//
	//						}
	//						else
	//						{
	//							//we delete the quotes do use the function
	//							args[i]=args[i].replaceAll("[\"']", "");
	//						}
	//					}
	//				}
	//				else
	//				{
	//					/* If there is no attributes
	//					 * For each argument, we delete the quotes, if there is no quotes, this is an invalid argument
	//					 */
	//					for(int i=0;i<args.length;i++)
	//					{
	//						if( (args[i].startsWith("\"") || args[i].startsWith("'"))) {
	//							args[i]=args[i].replaceAll("[\"']", "");
	//						}
	//						else
	//							IgnoreException.throwNewException(IgnoreException.WRONG_PARAMETER, "The String function arguments must be quoted strings or keywords");
	//					}
	//				}
	//				//We execute the String function
	//				expression=expression.replace(e.getKey(), DictionaryStringFunction.exec(e.getValue().functionName, args));	
	//			}		
	//		}
	//	}
	//
	public String toString(){

		String retour =  "(" + this.expression + ")=" +result
				+ " [";
		for( AttributeHandler ah: this.exprAttributes) {
			retour += ah.getNameorg() + " ";
		}
		retour += "] " 
				+ this.getSettingMode() + " "
				+ this.message;
		if( this.storedValue != null )
			retour += " storedValue=" + this.storedValue;
		return retour;
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
			this.completeMessage("expression <"+this.expression+">");
		case BY_VALUE:
			this.completeMessage("value <" +this.result+">");
			break;

		default:
			this.completeMessage("Nothing found");
			break;
		}

	}

	/*
	 * For each "SetByXXX" method, we give the value to "result" and we set the mode. If we don't, the old methods will not be able
	 * to get the correct values or will believe the columnsetter is not set
	 * (non-Javadoc)
	 * @see saadadb.products.setter.ColumnSetter#setByValue(java.lang.String, boolean)
	 */
	@Override
	public void setByValue(String value, boolean fromMapping) {
		this.settingMode=ColumnSetMode.BY_VALUE;
		this.result = this.expression = value;	
		if( fromMapping  ) {
			this.completeMessage("user mapping");
		}
	}

	@Override
	public void setByValue(double value, boolean fromMapping) {
		this.settingMode=ColumnSetMode.BY_VALUE;
		this.result = this.expression = String.valueOf(value);;	
		if( fromMapping  ) {
			this.completeMessage("user mapping");
		}
	}

	@Override
	public void setByKeyword(boolean fromMapping) {
		this.settingMode = ColumnSetMode.BY_KEYWORD;
		this.completeMessage("keyword <" + this.singleAttributeHandler.getNameorg()+ ">");
		if( fromMapping  ) {
			this.completeMessage("user mapping");
		}
	}

	@Override
	public void setByKeyword(String value, boolean fromMapping) {
		this.settingMode = ColumnSetMode.BY_KEYWORD;
		this.result = this.expression = value;	
		this.completeMessage("keyword <" + this.singleAttributeHandler.getNameorg()+ ">");
		if( fromMapping  ) {
			this.completeMessage("user mapping");
		}
	}

	@Override
	public void setByKeyword(double value, boolean fromMapping) {
		this.settingMode = ColumnSetMode.BY_KEYWORD;
		this.result = this.expression = String.valueOf(value);;	
		this.completeMessage("keyword <" + this.singleAttributeHandler.getNameorg()+ ">");
		if( fromMapping  ) {
			this.completeMessage("user mapping");
		}
	}

	@Override
	public void setByWCS(String value, boolean fromMapping) {
		this.settingMode = ColumnSetMode.BY_WCS;
		this.result = this.expression = value;	
		this.completeMessage("WCS value <" + this.singleAttributeHandler.getValue()+ this.singleAttributeHandler.getUnit() +">");
		if( fromMapping  ) {
			this.completeMessage("user mapping");
		}
	}

	@Override
	public void setByWCS(double value, boolean fromMapping) {
		this.settingMode = ColumnSetMode.BY_WCS;
		this.result = this.expression = String.valueOf(value);;	
		this.completeMessage("WCS value <" + this.singleAttributeHandler.getValue()+ this.singleAttributeHandler.getUnit() +">");
		if( fromMapping  ) {
			this.completeMessage("user mapping");
		}
	}

	@Override
	public void setByPixels(String value, boolean fromMapping) {
		this.settingMode = ColumnSetMode.BY_PIXELS;
		this.result = this.expression = value;	
		this.completeMessage("pixel value <" + this.singleAttributeHandler.getValue()+ this.singleAttributeHandler.getUnit()+">");
		if( fromMapping  ) {
			this.completeMessage("user mapping");
		}
	}

	@Override
	public void setByPixels(double value, boolean fromMapping) {
		this.settingMode = ColumnSetMode.BY_PIXELS;
		this.result = this.expression = String.valueOf(value);;	
		this.completeMessage("pixel value <" + this.singleAttributeHandler.getValue()+ this.singleAttributeHandler.getUnit()+">");
		if( fromMapping  ) {
			this.completeMessage("user mapping");
		}
	}

	@Override
	public void setByTableColumn(String value, boolean fromMapping) {
		this.settingMode = ColumnSetMode.BY_TABLE_COLUMN;
		this.result = this.expression = value;	
		this.completeMessage("content of the column <" + this.singleAttributeHandler.getValue()+ this.singleAttributeHandler.getUnit()+ ">");
		if( fromMapping  ) {
			this.completeMessage("user mapping");
		}
	}

	@Override
	public void setByTabeColumn(double value, boolean fromMapping) {
		this.settingMode = ColumnSetMode.BY_TABLE_COLUMN;
		this.result = this.expression = String.valueOf(value);;	
		this.completeMessage("content of the column <" + this.singleAttributeHandler.getValue()+ this.singleAttributeHandler.getUnit()+ ">");
		if( fromMapping  ) {
			this.completeMessage("user mapping");
		}
	}

	@Override
	public void setBySaada() {
		this.settingMode = ColumnSetMode.BY_SAADA;
		this.result = this.expression = null;
	}

	@Override
	public void setValue(double value, String unit) {
		this.storedValue = this.result = this.expression = String.valueOf(value);;	
		this.setUnit(unit);
		this.storedValue = value;
	}

	@Override
	public void setNotSet() {
		this.settingMode = ColumnSetMode.NOT_SET;

	}

	@Override
	public void setValue(String value) {
		this.result=value;
	}

	@Override
	public void setValue(double value) {

		this.result=String.valueOf(value);	
	}

	@Override
	public void setUnit(String unit) {
		this.unit = unit;	
	}

	@Override
	public String getValue() {
		return result;
	}

	@Override
	public double getNumValue() {
		return Double.valueOf(this.result);
	}

	@Override
	public String getComment() {
		return this.message.toString();
	}

	@Override
	public String getAttNameOrg() {
		return this.singleAttributeHandler.getNameorg();
	}

	@Override
	public String getAttNameAtt() {
		return this.singleAttributeHandler.getNameattr();
	}

	@Override
	public String getUnit() {
		return this.unit;
	}

	@Override
	public String getUcd() {
		return this.ucd;
	}

	@Override
	public AttributeHandler getAssociateAtttribute() {
		return this.singleAttributeHandler.getAssociateAtttribute();
	}
	//	public static void main(String args[])
	//	{
	//		//Tests
	//		
	//		Map<String,AttributeHandler> mapTest = new TreeMap<String,AttributeHandler>();
	//		AttributeHandler ah1 = new AttributeHandler();
	//		AttributeHandler ah2 = new AttributeHandler();
	//		AttributeHandler ah3 = new AttributeHandler();
	//		AttributeHandler ah4 = new AttributeHandler();
	//		AttributeHandler ah5 = new AttributeHandler();
	//		ah1.setNameattr("_tmin");
	//		ah1.setValue("11/20/1858"); //=3
	//		
	//		ah2.setNameattr("_emax");
	//		ah2.setNameorg("EMAX");
	//		ah2.setValue("10");
	//		
	//		ah3.setNameattr("_emin");
	//		ah3.setValue("2");
	//		
	//		ah4.setNameattr("_tmax");
	//		ah4.setValue("11/22/1858");//=5
	//		
	//		ah5.setNameattr("_emin59");
	//		ah5.setValue("1");
	//		
	//		//Test calcul constante
	//		String expression = "42";
	//		ColumnExpressionSetter ces = new ColumnExpressionSetter();
	//		System.out.println("Test de calcul d'une valeur constante seule");
	//		System.out.println("---- Résultat souhaité : "+expression);
	//		try {
	//			ces.calculateExpression(expression);
	//		} catch (Exception e1) {
	//			e1.printStackTrace();
	//		}
	//		System.out.println(ces);
	//		System.out.println();
	//
	//		
	//		//Test calcul d'une opération de constante
	//		expression = "6*7";
	//		ces = new ColumnExpressionSetter();
	//		System.out.println("Test de calcul d'une opération de constante");
	//		System.out.println("---- Résultat souhaité : 42");
	//		try {
	//			ces.calculateExpression(expression);
	//		} catch (Exception e1) {
	//			e1.printStackTrace();
	//		}
	//		System.out.println(ces);
	//		System.out.println();
	//		
	//		
	//		//Test opération de keywords
	//		mapTest.put("_emax", ah2);
	//		mapTest.put("_emin", ah3);
	//		mapTest.put("_emin59", ah5);
	//		expression = "EMAX*_emin+_emin59";
	//		ces = new ColumnExpressionSetter();
	//		System.out.println("Test de calcul d'une opération de keywords");
	//		System.out.println("---- Résultat souhaité : 21");
	//		try {
	//			ces.calculateExpression(expression,mapTest);
	//		} catch (Exception e1) {
	//			e1.printStackTrace();
	//		}
	//		System.out.println(ces);
	//		System.out.println();
	//
	//	
	//		//Test opération de fonctions avec arguments constants
	//		expression = "MJD(11/22/1858) - MJD(11/19/1858)";
	//		ces = new ColumnExpressionSetter();
	//		System.out.println("Test de calcul de fonctions avec arguments contants");
	//		System.out.println("---- Résultat souhaité : 3");
	//		try {
	//			ces.calculateExpression(expression);
	//		} catch (Exception e1) {
	//			e1.printStackTrace();
	//		}
	//		System.out.println(ces);
	//		System.out.println();
	//
	//		//Test opération de fonctions avec arguments variables
	//		mapTest.clear();
	//		mapTest.put("_tmin", ah1);
	//		mapTest.put("_tmax", ah4);
	//		expression = "80/ (MJD(_tmin) + MJD(_tmax))";
	//		ces = new ColumnExpressionSetter();
	//		System.out.println("Test de calcul de fonctions avec arguments variables");
	//		System.out.println("---- Résultat souhaité : 10");
	//		try {
	//			ces.calculateExpression(expression,mapTest);
	//		} catch (Exception e1) {
	//			e1.printStackTrace();
	//		}
	//		System.out.println(ces);
	//		System.out.println();
	//
	//		
	//		//Test Appel fonction expression vide
	//		expression = "";
	//		ces = new ColumnExpressionSetter();
	//		System.out.println("Test de calcul expression vide");
	//		System.out.println("---- Résultat souhaité : Exception");
	//		try {
	//			ces.calculateExpression(expression);
	//		} catch (Exception e1) {
	//
	//			e1.printStackTrace();
	//		}
	//		System.out.println(ces);		
	//		System.out.println();
	//
	//		
	//		//Test Appel fonction expression vide
	//		expression = null;
	//		ces = new ColumnExpressionSetter();
	//		System.out.println("Test de calcul expression null");
	//		System.out.println("---- Résultat souhaité : Exception");
	//		try {
	//			ces.calculateExpression(expression);
	//		} catch (Exception e1) {
	//
	//			e1.printStackTrace();
	//		}
	//		System.out.println(ces);
	//		System.out.println();
	//		
	//		//Test calcul expression de constante avec map remplie
	//		expression = "(5+2)*6";
	//		ces = new ColumnExpressionSetter();
	//		System.out.println("Test de calcul expression de constante avec map remplie");
	//		System.out.println("---- Résultat souhaité : 42");
	//		try {
	//			ces.calculateExpression(expression,mapTest);
	//		} catch (Exception e1) {
	//			e1.printStackTrace();
	//		}
	//		System.out.println(ces);
	//		System.out.println();
	//		
	//		//Test calcul expression de keywords avec map vide
	//		mapTest.clear();
	//		expression = "_emin+_emax";
	//		ces = new ColumnExpressionSetter();
	//		System.out.println("Test de calcul expression de keyword avec map vide");
	//		System.out.println("---- Résultat souhaité : Exception");
	//		try {
	//			ces.calculateExpression(expression,mapTest);
	//		} catch (Exception e1) {
	//			e1.printStackTrace();
	//		}
	//		System.out.println(ces);
	//		
	//		//Test calcul expression de keywords avec map null
	//		mapTest=null;
	//		expression = "_emin+_emax";
	//		ces = new ColumnExpressionSetter();
	//		System.out.println("Test de calcul expression de keyword avec map null");
	//		System.out.println("---- Résultat souhaité : Exception");
	//		try {
	//			ces.calculateExpression(expression,mapTest);
	//		} catch (Exception e1) {
	//			e1.printStackTrace();
	//		}
	//		System.out.println(ces);
	//
	//
	//		System.out.println();
	//		mapTest = new TreeMap<String,AttributeHandler>();
	//
	//		mapTest.put("_emax", ah2);
	//		mapTest.put("_emin", ah3);
	//		expression = "EMAX*_emin";
	//		ces = new ColumnExpressionSetter();
	//		System.out.println("Test Passage expression Wrapper");
	//		System.out.println("---- Résultat souhaité : 20");
	//		try {
	//			ces.calculateExpression(expression,mapTest);
	//		} catch (Exception e1) {
	//			e1.printStackTrace();
	//		}
	//		System.out.println(ces);
	//		System.out.println();
	//
	//		
	//
	//		System.out.println();
	//		mapTest = new TreeMap<String,AttributeHandler>();
	//
	//		mapTest.put("_emax", ah2);
	//		mapTest.put("_emin", ah3);
	//		expression = "EMAX*_emin";
	//		System.out.println("Test Passage expression Wrapper");
	//		System.out.println("---- Résultat should be the same");
	//		try {
	//			ces.calculateExpression(expression,mapTest);
	//		} catch (Exception e1) {
	//			e1.printStackTrace();
	//		}
	//		System.out.println(ces);
	//		System.out.println();
	//		
	//		
	//		
	//		mapTest.clear();
	//		mapTest.put("_emax", ah2);
	//		mapTest.put("_emin59", ah5);
	//		expression = "EMAX+_emin59";
	//		System.out.println("Result should be different");
	//		try {
	//			ces.calculateExpression(expression,mapTest);
	//		} catch (Exception e1) {
	//			e1.printStackTrace();
	//		}
	//		System.out.println(ces);
	//		System.out.println();
	//		
	//		
	//		expression="toRadian(180)+toRadian(90)";
	//		System.out.println("Result =3.14...");
	//		try {
	//			ces.calculateExpression(expression,null);
	//		} catch (Exception e1) {
	//			e1.printStackTrace();
	//		}
	//		System.out.println(ces);
	//		System.out.println();
	//		
	//		mapTest.put("_emax", ah2);
	//		mapTest.put("_emin", ah3);
	//		expression="toRadian(10) + _emax + convert(_emin,mm,m)";
	//		System.out.println("Result =12.17...");
	//		try {
	//			ces.calculateExpression(expression,mapTest);
	//		} catch (Exception e1) {
	//			e1.printStackTrace();
	//		}
	//		System.out.println(ces);
	//		System.out.println();
	//	}

	public static void main(String[] args) throws Exception {
		AttributeHandler ah1 = new AttributeHandler();
		AttributeHandler ah2 = new AttributeHandler();
		AttributeHandler ah3 = new AttributeHandler();
		AttributeHandler ah4 = new AttributeHandler();
		AttributeHandler ah5 = new AttributeHandler();
		ah1.setNameorg("TMIN");
		ah1.setNameattr("_tmin");
		ah1.setValue("11/20/1858"); //=3

		ah2.setNameattr("_emax");
		ah2.setNameorg("EMAX");
		ah2.setValue("10");

		ah3.setNameorg("EMIN");
		ah3.setNameattr("_emin");
		ah3.setValue("2");

		ah4.setNameorg("TMAX");
		ah4.setNameattr("_tmax");
		ah4.setValue("11/22/1858");//=5

		ah5.setNameorg("EMIN59");
		ah5.setNameattr("_emin59");
		ah5.setValue("1");
		Map<String,AttributeHandler> mapTest = new TreeMap<String,AttributeHandler>();		
		mapTest.put(ah1.getNameorg(), ah1);
		mapTest.put(ah2.getNameorg(), ah2);
		mapTest.put(ah3.getNameorg(), ah3);
		mapTest.put(ah4.getNameorg(), ah4);
		mapTest.put(ah5.getNameorg(), ah5);

		ColumnExpressionSetter ces;
		/*
CONSTANT_VALUE,
CONSTANT_EXPRESSION,
SINGLE_ATTRIBUTE,
MULTI_ATTRIBUTE,
		 */		

		System.out.println("----------- case CONSTANT_VALUE -------------------");
		ces = new ColumnExpressionSetter("ah4");
		System.out.println(" compiled: " + ces);
		ces.calculateExpression();
		System.out.println(" comptued: " + ces);

		System.out.println("----------- case CONSTANT_EXPRESSION -------------------");
		ces = new ColumnExpressionSetter("strcat('1', '2')", null);
		System.out.println(" compiled: " + ces);
		ces.calculateExpression();
		System.out.println(" comptued: " + ces);

		ces = new ColumnExpressionSetter("12+ strcat('1', '2')", null);
		System.out.println(" compiled: " + ces);
		ces.calculateExpression();
		System.out.println(" comptued: " + ces);

		System.out.println("----------- case SINGLE_ATTRIBUTE -------------------");
		ces = new ColumnExpressionSetter(ah4);
		System.out.println(" compiled: " + ces);
		ces.calculateExpression();
		System.out.println(" comptued: " + ces);

		System.out.println("----------- case MULTI_ATTRIBUTE -------------------");
		for( String exp: new String[]{ 
				"strcat('1', '2')",
				"strcat(_emin59, '2')" ,
				"strcat(_emin59, _tmax)" ,
				"12" ,
				"2*12" ,
				"2*12 + EMIN59" ,
				"2*12 + MJD(_tmax)" ,
				"log(2*12) + MJD(_tmax)" ,
				"log(EMIN) + MJD(_tmax)" ,
				"log(EMIN) + MJD('11/22/1858')" ,
				"MJD(TMAX) - MJD(_tmin)" ,
				"MJD(TMAX) - MJD('11/22/1858')" ,
				"MJD('11/22/1858')" ,
				"MJD('11/20/1858') - MJD('11/22/1858')" ,
		}) {
			System.out.println("*****Expr: " + exp);
			ces = new ColumnExpressionSetter(exp, mapTest);
			System.out.println(" compiled: " + ces);
			ces.calculateExpression(mapTest);
			System.out.println(" computed: " + ces);
			if( ces.notSet() ) break;
			ces.calculateExpression(mapTest);
			System.out.println(" computed: " + ces);
			if( ces.notSet() ) break;
		}
	}



}
