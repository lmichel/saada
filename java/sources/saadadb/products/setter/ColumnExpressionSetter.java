package saadadb.products.setter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import net.objecthunter.exp4j.function.Function;
import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.util.SaadaConstant;
import saadadb.vocabulary.enums.ColumnSetMode;

/**
 * The columnSetter used when a field must be evaluate by an expression
 * @author pertuy
 * @version $Id$
 */
public class ColumnExpressionSetter extends ColumnSetter implements Cloneable{


	/**
	 * The expression to evaluate 
	 */
	protected String expression;
	/**
	 * The last expression evaluated
	 */

	protected String lastExpressionEvaluated;
	/**
	 * The wrapper used to evaluate the expression
	 */
	protected ExpressionWrapper wrapper;

	/**
	 * The list of attributes used in the ExpressionWrapper
	 */
	protected List<AttributeHandler> exprAttributes = new ArrayList<AttributeHandler>();;
	/**
	 * The list of attribute being String functions arguments (names of AH)
	 */
	protected List<AttributeHandler> stringFunctionArgumentsList = new ArrayList<AttributeHandler>();
	/**
	 * The result of the expression
	 */
	protected String result="NotSet";
	/**
	 * A ColumnSingleSetter used for the abstract methods from ColumnSetter and when mode=ByValue
	 */
	//private ColumnSingleSetter singleSetter;

	/**
	 * The List of all numeric function used in the expression
	 */
	protected ArrayList<Function> numericFunctionList;

	//	/**
	//	 * Boolean indicating if we've override the automatic detection of settingmode
	//	 */
	//	private boolean columnModeForced=false;

	protected StringFunctionExtractor stringFunctionExtractor;
	protected NumericFunctionExtractor numericFunctionExtractor;
	protected AttributeHandler singleAttributeHandler;
	protected boolean singleStringExpression = false;
	/**
	 * If false, no call of exp4j 
	 */
	protected boolean arithmeticExpression = true; 
	protected String unit="";
	protected String ucd="";
	public final String fieldName;

	protected INIT computingMode;
	enum INIT{
		CONSTANT_VALUE,
		CONSTANT_EXPRESSION,
		SINGLE_ATTRIBUTE,
		MULTI_ATTRIBUTE,
		WSC_AXE
	};
	/**
	 * Constructor without keyword. The expression is considered as constant
	 * @param constantValue
	 * @throws Exception 
	 */
	public ColumnExpressionSetter(String fieldName, String constantValue) throws Exception	{
		super();
		if( constantValue == null ){
			IgnoreException.throwNewException(SaadaException.INTERNAL_ERROR, "Column setter cannot be set with a null expression");
		}
		this.arithmeticExpression = false;
		this.fieldName = fieldName;
		this.settingMode = ColumnSetMode.BY_VALUE;
		this.expression = this.result = constantValue;
		this.computingMode = INIT.CONSTANT_VALUE;
	}

	/**
	 * Constructor with only one ah.
	 * The expression is set with the name of the attribute handler/ Its value will be taker as setter value
	 * @param attr
	 * @param arithmeticExpression allows or not the expression to be computed
	 * @throws Exception
	 */
	public ColumnExpressionSetter(String fieldName, AttributeHandler attr, boolean arithmeticExpression) throws Exception {
		super();
		this.arithmeticExpression = arithmeticExpression;
		this.settingMode = ColumnSetMode.NOT_SET;
		if( attr == null ){
			IgnoreException.throwNewException(SaadaException.INTERNAL_ERROR, "Column setter cannot be set with a null expression");
		}
		this.fieldName = fieldName;
		this.exprAttributes= new ArrayList<AttributeHandler>();
		this.setExpression(attr.getNameorg());
		this.singleAttributeHandler = attr;
		this.settingMode = ColumnSetMode.BY_KEYWORD;
		this.computingMode = INIT.SINGLE_ATTRIBUTE;
		this.calculateExpressionFromAttributes(null);
	}
	/**
	 * Constructor with only one ah.
	 * The expression is set with the name of the attribute handler/ Its value will be taker as setter value.
	 * it s considered as an arithmetic expression
	 * @param attr
	 * @throws Exception
	 */
	public ColumnExpressionSetter(String fieldName, AttributeHandler attr) throws Exception {
		super();
		this.arithmeticExpression = true;
		this.settingMode = ColumnSetMode.NOT_SET;
		if( attr == null ){
			IgnoreException.throwNewException(SaadaException.INTERNAL_ERROR, "Column setter cannot be set with a null attributeHandler");
		}
		this.fieldName = fieldName;
		this.exprAttributes= new ArrayList<AttributeHandler>();
		this.setExpression(attr.getNameorg());
		this.singleAttributeHandler = attr;
		this.settingMode = ColumnSetMode.BY_KEYWORD;
		this.computingMode = INIT.SINGLE_ATTRIBUTE;
		this.calculateExpressionFromAttributes(null);
	}

	/**
	 * If the AH map is null, the expression is considered as a set of operations on constant values. It is computed once.
	 * @param expression arithmetic expression
	 * @param attributes map of the AH possibly used by the expression
	 * @throws Exception
	 */
	public ColumnExpressionSetter(String fieldName, String expression, Map<String,AttributeHandler> attributes, boolean arithmeticExpression) throws Exception{
		super();
		this.arithmeticExpression = arithmeticExpression;
		if( attributes == null ){
			this.computingMode = INIT.CONSTANT_EXPRESSION;
		} else {
			this.computingMode = INIT.MULTI_ATTRIBUTE;
		}
		this.fieldName = fieldName;
		this.setExpression(expression);
		this.calculateExpressionFromAttributes(attributes);
	}

	/**
	 * Constructor with only one ah calculating the expression immediatly, the setting mode is manually given
	 * @param attr
	 * @throws Exception
	 */
	public ColumnExpressionSetter(String fieldName, AttributeHandler attr, ColumnSetMode mode, boolean arithmeticExpression) throws Exception {
		this(fieldName, attr, arithmeticExpression);
		this.settingMode = mode;
	}


	/**
	 * Override the auto-mode détection
	 */
	public ColumnExpressionSetter(String fieldName )	{
		super();
		this.fieldName = fieldName;
		this.settingMode = ColumnSetMode.NOT_SET;			
		this.computingMode = INIT.MULTI_ATTRIBUTE;
	}

	/**
	 * Switches the column setter in expression mode
	 * @param expression: Setter's expression
	 * @throws Exception
	 */
	public void switchToExpression(String expression) throws Exception {
		this.computingMode = INIT.MULTI_ATTRIBUTE;
		this.settingMode = ColumnSetMode.BY_EXPRESSION;
		Map<String, AttributeHandler> lah= new LinkedHashMap<String, AttributeHandler>();
		lah.put(this.singleAttributeHandler.getNameattr(), this.singleAttributeHandler);
		this.setExpression(expression);
		this.calculateExpressionFromAttributes(lah);
	}

	/**
	 * @param expression
	 * @throws Exception
	 */
	protected void setExpression(String expression) throws Exception{
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

	/* (non-Javadoc)
	 * @see saadadb.products.setter.ColumnSetter#calculateExpression()
	 */
	@Override
	public void calculateExpression() throws Exception{
		if( this.computingMode == INIT.CONSTANT_VALUE){

		}
		try {
			if( this.settingMode == ColumnSetMode.BY_KEYWORD){
				this.result=this.singleAttributeHandler.getValue();				
			} else 	if( this.settingMode == ColumnSetMode.BY_VALUE || this.settingMode == ColumnSetMode.NOT_SET){
				return;
			} else if( this.settingMode == ColumnSetMode.BY_EXPRESSION ){
				if( this.stringFunctionExtractor!= null ){
					this.execStringFunction();
				}
				if( !this.singleStringExpression ){
					if( this.arithmeticExpression ) {
						if( this.arithmeticExpression /** && !this.expression.trim().equals(this.lastExpressionEvaluated) **/) {
							this.lastExpressionEvaluated = this.expression.trim();
							this.wrapper = new ExpressionWrapper(this.expression, this.exprAttributes, this.numericFunctionList);
							this.wrapper.evaluate(this.exprAttributes, this.numericFunctionList);
							this.result=wrapper.getStringValue();
							if( this.exprAttributes.size() == 0 && !(this.stringFunctionExtractor != null && this.stringFunctionExtractor.useKeywords()) ){
								this.settingMode = ColumnSetMode.BY_VALUE;
							} 
						} else {
						}
					} else {
						this.result = this.expression;					
					}
				} else {
					this.result = this.expression;					
				}
			} else {
				this.result = this.expression;
			}
			if( this.result == null ){
				//this.settingMode = ColumnSetMode.NOT_SET;
				this.completeConversionMsg("Expression returns null");				
			}
		} catch (Exception e) {
			this.result = SaadaConstant.STRING;
			//this.settingMode = ColumnSetMode.NOT_SET;
			this.completeConversionMsg("Exp failed: " + e.getMessage());
		}
	}

	/**
	 * @param attributes
	 * @throws Exception
	 */
	private void calculateExpressionFromAttributes(Map<String,AttributeHandler> attributes) throws Exception{
		try {
			if( this.settingMode == ColumnSetMode.BY_KEYWORD){
				this.result=this.singleAttributeHandler.getValue();				
			} else 	if( this.settingMode == ColumnSetMode.BY_VALUE || this.settingMode == ColumnSetMode.NOT_SET){
				return;
			} else if( this.settingMode == ColumnSetMode.BY_EXPRESSION ){
				if( this.stringFunctionExtractor!= null ){
					this.execStringFunction(attributes);
				} 
				if( this.singleStringExpression ){
					this.result = this.expression;
				} else {
					AttributeHandlerExtractor ahExtractor = new AttributeHandlerExtractor(expression, attributes);
					this.exprAttributes = ahExtractor.extractAH();
					this.expression=ahExtractor.expression;
					if( this.arithmeticExpression ) {
						if(  !this.expression.trim().equals(this.lastExpressionEvaluated)) {
							this.lastExpressionEvaluated = this.expression.trim();
							this.wrapper = new ExpressionWrapper(expression, exprAttributes,numericFunctionList);
							this.wrapper.evaluate(this.exprAttributes, this.numericFunctionList);
							this.result=wrapper.getStringValue();
							if( this.exprAttributes.size() == 0 && !(this.stringFunctionExtractor != null && this.stringFunctionExtractor.useKeywords()) ){
								this.settingMode = ColumnSetMode.BY_VALUE;
							} 
						} else {
						}
					} else {
						this.result = this.expression;
					}
				}
			} else {
				this.result = this.expression;
			}
		} catch (Exception e) {
			this.result = SaadaConstant.NOTSET;
			//this.settingMode = ColumnSetMode.NOT_SET;
			this.completeConversionMsg("exp failed: " + e.getMessage());
		}
	}

	/**
	 * Execute the String functions in the expression and replaced them by their values
	 * @throws Exception 
	 * 
	 */
	private void execStringFunction(Map<String,AttributeHandler> attributes) throws Exception {
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


	/**
	 * @return
	 */
	public String toString(){
		String retour =  this.fieldName + " ";
		if( this.expression != null ){
			retour +=  "(" + this.expression + ")=" + this.result + ((this.unit != null)? this.unit: "")
					+ " [";
			for( AttributeHandler ah: this.exprAttributes) {
				retour += ah.getNameorg() + " ";
			}
			retour += "] ";
		}
		retour += super.toString();
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

	public AttributeHandler getSingleAttributeHandler() {
		return this.singleAttributeHandler;
	}
	@Override
	protected void setInitMessage() {
		if(expression==null || expression.isEmpty())
			return;
		switch(this.settingMode){
		case BY_EXPRESSION:
			this.completeUserMappingMsg("expression <"+this.expression+">");
		case BY_VALUE:
			this.completeUserMappingMsg("value <" +this.result+">");
			break;

		default:
			this.completeUserMappingMsg("Nothing found");
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
			this.completeUserMappingMsg("user mapping");
		}
	}

	@Override
	public void setByValue(double value, boolean fromMapping) {
		this.settingMode=ColumnSetMode.BY_VALUE;
		this.result = this.expression = String.valueOf(value);;	
		if( fromMapping  ) {
			this.completeUserMappingMsg("user mapping");
		}
	}

	@Override
	public void setByKeyword(boolean fromMapping) {
		this.settingMode = ColumnSetMode.BY_KEYWORD;
		String msg = (this.singleAttributeHandler == null)? "": "<" + this.singleAttributeHandler.getNameorg()+ ">";
		this.completeUserMappingMsg("keyword " + msg);
		if( fromMapping  ) {
			this.completeUserMappingMsg("user mapping");
		}
	}

	@Override
	public void setByKeyword(String value, boolean fromMapping) {
		this.settingMode = ColumnSetMode.BY_KEYWORD;
		this.result = this.expression = value;	
		String msg = (this.singleAttributeHandler == null)? "": "<" + this.singleAttributeHandler.getNameorg()+ ">";
		this.completeUserMappingMsg("keyword " + msg);
		if( fromMapping  ) {
			this.completeUserMappingMsg("user mapping");
		}
	}

	@Override
	public void setByKeyword(double value, boolean fromMapping) {
		this.settingMode = ColumnSetMode.BY_KEYWORD;
		this.result = this.expression = String.valueOf(value);;	
		String msg = (this.singleAttributeHandler == null)? "": "<" + this.singleAttributeHandler.getNameorg()+ ">";
		this.completeUserMappingMsg("keyword " + msg);
		if( fromMapping  ) {
			this.completeUserMappingMsg("user mapping");
		}
	}

	@Override
	public void setByWCS(String value, boolean fromMapping) {
		this.settingMode = ColumnSetMode.BY_WCS;
		this.result = this.expression = value;	
		String msg = (this.singleAttributeHandler == null)? ""
				+ "": "<" + this.singleAttributeHandler.getValue()+ this.singleAttributeHandler.getUnit()+ ">";
		this.completeUserMappingMsg("keyword " + msg);
		if( fromMapping  ) {
			this.completeUserMappingMsg("user mapping");
		}
	}

	@Override
	public void setByWCS(double value, boolean fromMapping) {
		this.settingMode = ColumnSetMode.BY_WCS;
		this.result = this.expression = String.valueOf(value);;	
		String msg = (this.singleAttributeHandler == null)? ""
				+ "": "<" + this.singleAttributeHandler.getValue()+ this.singleAttributeHandler.getUnit()+ ">";
		this.completeUserMappingMsg("keyword " + msg);
		if( fromMapping  ) {
			this.completeUserMappingMsg("user mapping");
		}
	}

	@Override
	public void setByPixels(String value, boolean fromMapping) {
		this.settingMode = ColumnSetMode.BY_PIXELS;
		this.result = this.expression = value;	
		String msg = (this.singleAttributeHandler == null)? ""
				+ "": "<" + this.singleAttributeHandler.getValue()+ this.singleAttributeHandler.getUnit()+ ">";
		this.completeUserMappingMsg("keyword " + msg);
		if( fromMapping  ) {
			this.completeUserMappingMsg("user mapping");
		}
	}

	@Override
	public void setByPixels(double value, boolean fromMapping) {
		this.settingMode = ColumnSetMode.BY_PIXELS;
		this.result = this.expression = String.valueOf(value);;	
		String msg = (this.singleAttributeHandler == null)? ""
				+ "": "<" + this.singleAttributeHandler.getValue()+ this.singleAttributeHandler.getUnit()+ ">";
		this.completeUserMappingMsg("keyword " + msg);
		if( fromMapping  ) {
			this.completeUserMappingMsg("user mapping");
		}
	}

	@Override
	public void setByTableColumn(String value, boolean fromMapping) {
		this.settingMode = ColumnSetMode.BY_TABLE_COLUMN;
		this.result = this.expression = value;	
		String msg = (this.singleAttributeHandler == null)? ""
				+ "": "<" + this.singleAttributeHandler.getValue()+ this.singleAttributeHandler.getUnit()+ ">";
		this.completeUserMappingMsg("keyword " + msg);
		if( fromMapping  ) {
			this.completeUserMappingMsg("user mapping");
		}
	}

	@Override
	public void setByTabeColumn(double value, boolean fromMapping) {
		this.settingMode = ColumnSetMode.BY_TABLE_COLUMN;
		this.result = this.expression = String.valueOf(value);;	
		String msg = (this.singleAttributeHandler == null)? ""
				+ "": "<" + this.singleAttributeHandler.getValue()+ this.singleAttributeHandler.getUnit()+ ">";
		this.completeUserMappingMsg("keyword " + msg);
		if( fromMapping  ) {
			this.completeUserMappingMsg("user mapping");
		}
	}

	@Override
	public void setBySaada() {
		this.settingMode = ColumnSetMode.BY_SAADA;
		this.result = this.expression = null;
	}

	@Override
	public void setValue(double value, String unit) {
		this.storedValue = this.result = String.valueOf(value);;	
		//	this.setUnit(unit);
		this.storedValue = value;
	}

	@Override
	public void setFailed(String conversionMessage){
		this.result =SaadaConstant.NOTSET;
		this.completeUserMappingMsg(conversionMessage);				
	}
	@Override
	public void setFailed(String message, Exception e){
		this.result =SaadaConstant.NOTSET;
		this.completeUserMappingMsg(message + ":" + e.getMessage());				
	}
//
//	@Override
//	public void setNotSet() {
//		this.settingMode = ColumnSetMode.NOT_SET;
//	}
//
//	@Override
//	public void setNotSet(String message, Exception e){
//		this.settingMode = ColumnSetMode.NOT_SET;
//		this.completeUserMappingMsg(message + ":" + e.getMessage());		
//	}
//
//	@Override
//	public void setNotSet(String message) {
//		this.settingMode = ColumnSetMode.NOT_SET;
//		this.completeUserMappingMsg(message);
//	}
//
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
		return this.getFullMappingReport();
	}

	@Override
	public String getAttNameOrg() {
		return this.fieldName;
	}

	@Override
	public String getAttNameAtt() {
		return this.fieldName;
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

	@Override
	public List<AttributeHandler> getExprAttributes() {
		return this.exprAttributes;
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		AttributeHandler ah1 = new AttributeHandler();
		AttributeHandler ah2 = new AttributeHandler();
		AttributeHandler ah3 = new AttributeHandler();
		AttributeHandler ah4 = new AttributeHandler();
		AttributeHandler ah5 = new AttributeHandler();
		AttributeHandler ah6 = new AttributeHandler();
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

		ah6.setNameorg("Temps");
		ah6.setNameattr("_temps");
		ah6.setValue("2014-02-13");//=5

		ah5.setNameorg("EMIN59");
		ah5.setNameattr("_emin59");
		ah5.setValue("1");
		Map<String,AttributeHandler> mapTest = new TreeMap<String,AttributeHandler>();		
		mapTest.put(ah1.getNameorg(), ah1);
		mapTest.put(ah2.getNameorg(), ah2);
		mapTest.put(ah3.getNameorg(), ah3);
		mapTest.put(ah4.getNameorg(), ah4);
		mapTest.put(ah5.getNameorg(), ah5);
		mapTest.put(ah6.getNameorg(), ah6);

		ColumnExpressionSetter ces;
		/*
CONSTANT_VALUE,
CONSTANT_EXPRESSION,
SINGLE_ATTRIBUTE,
MULTI_ATTRIBUTE,
		 */		

		System.out.println("----------- case CONSTANT_VALUE -------------------");
		ces = new ColumnExpressionSetter("name","2014-02-13");
		System.out.println(" compiled: " + ces);
		ces.calculateExpression();
		System.out.println(" comptued: " + ces);
		ces = new ColumnExpressionSetter("name","ah4");
		System.out.println(" compiled: " + ces);
		ces.calculateExpression();
		System.out.println(" comptued: " + ces);

		System.out.println("----------- case CONSTANT_EXPRESSION -------------------");
		ces = new ColumnExpressionSetter("name", "strcat('1', '2')", null, true);
		System.out.println(" compiled: " + ces);
		ces.calculateExpression();
		System.out.println(" comptued: " + ces);

		ces = new ColumnExpressionSetter("name","12+ strcat('1', '2')", null, true);
		System.out.println(" compiled: " + ces);
		ces.calculateExpression();
		System.out.println(" comptued: " + ces);

		System.out.println("----------- case SINGLE_ATTRIBUTE -------------------");
		ces = new ColumnExpressionSetter("name", ah4, true);
		System.out.println(" compiled: " + ces);
		ces.calculateExpression();
		System.out.println(" comptued: " + ces);
		ces = new ColumnExpressionSetter("name", ah6, true);
		System.out.println(" compiled: " + ces);
		ces.calculateExpression();
		System.out.println(" comptued: " + ces);

		System.out.println("----------- case MULTI_ATTRIBUTE -------------------");
		for( String exp: new String[]{ 
				"strcat('1', ' ' , '2')",
				//				"strcat(_emin59, '2')" ,
				//				"strcat(_emin59, _tmax)" ,
				//				"12" ,
				//				"2*12" ,
				//				"2*12 + EMIN59" ,
				//				"2*12 + MJD(_tmax)" ,
				//				"log(2*12) + MJD(_tmax)" ,
				//				"log(EMIN) + MJD(_tmax)" ,
				//				"log(EMIN) + MJD('11/22/1858')" ,
				//				"MJD(TMAX) - MJD(_tmin)" ,
				//				"MJD(TMAX) - MJD('11/22/1858')" ,
				//				"MJD('11/22/1858')" ,
				//				"MJD('11/20/1858') - MJD('11/22/1858')" ,
		}) {
			System.out.println("*****Expr: " + exp);
			ces = new ColumnExpressionSetter("name",exp, mapTest, false);
			System.out.println(" compiled: " + ces);
			ces.calculateExpressionFromAttributes(mapTest);
			System.out.println(" computed: " + ces);
			if( ces.isNotSet() ) break;
			ces.calculateExpressionFromAttributes(mapTest);
			if( ces.isNotSet() ) break;
		}
	}



}
