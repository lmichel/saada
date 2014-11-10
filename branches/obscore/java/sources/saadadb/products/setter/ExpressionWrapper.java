package saadadb.products.setter;

import java.util.List;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.function.Function;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.util.Messenger;
import saadadb.util.SaadaConstant;

/**
 * This class calculate an expression using the exp4j package
 * @author pertuy
 * @version $Id$
 */

public class ExpressionWrapper {
	private String expKey;
	/**
	 * The String expression from the ColumnExpressionSetter
	 */
	private String expression;
	/**
	 * The object creating and exp4j expression
	 */
	private ExpressionBuilder expressionBuilder;
	/**
	 * The expj4 object representing an expression
	 */
	private Expression exp4jExpression;
	/**
	 * The keyword list coming from the ColumnExpressionSetter
	 */
	private List<AttributeHandler> attributeHandlers;
	private double value = SaadaConstant.DOUBLE;
	boolean isEvaluated = false;

	/**
	 * Evaluate an expression which can contain several keywords and return its result.
	 * The expression must not be null
	 * @param expression
	 * @param attributeHandlers
	 * @return
	 * @throws FatalException
	 */
	public ExpressionWrapper(String expression,List<AttributeHandler> attributeHandlers,List<Function> numericFuncList) throws Exception
	{
		if(expression==null)
			IgnoreException.throwNewException(SaadaException.WRONG_PARAMETER, "expression parameter missing");
//		try {
			//		if(variables==null)
			//			FatalException.throwNewException(SaadaException.WRONG_PARAMETER, "variables parameter missing");
			this.attributeHandlers = attributeHandlers;
			this.expKey = expression.replaceAll("\\s", "");
			this.expression=expression;
			this.evaluate(attributeHandlers,numericFuncList);
//		} catch(Exception e ){
//			Messenger.printStackTrace(e);
//			IgnoreException.throwNewException(SaadaException.WRONG_PARAMETER, e);
//		} 
	}
	/**
	 * @param expression
	 * @throws SaadaException
	 */
	public ExpressionWrapper(String expression) throws Exception
	{

		if(expression==null)
			IgnoreException.throwNewException(SaadaException.WRONG_PARAMETER, "expression parameter missing");
		try {
			//		if(variables==null)
			//			FatalException.throwNewException(SaadaException.WRONG_PARAMETER, "variables parameter missing");
			this.expKey = expression.replaceAll("\\s", "");
			this.expression=expression;
		} catch(Exception e ){
			Messenger.printStackTrace(e);
			IgnoreException.throwNewException(SaadaException.WRONG_PARAMETER, e);
		} 
	}
	/**
	 * Evaluate an expression with the Attributehandler provided
	 * Can be use on a wrapper to recalculate the same expression with differents values 
	 * @param numericFuncList : List of custom function to evaluate
	 * @param attributeHandlers list of variables
	 * @throws FatalException
	 */
	public void evaluate(List<AttributeHandler> attributeHandlers,List<Function> numericFuncList) throws Exception{
		if(this.isEvaluated ) {
			return;
		}
//		try {
			this.attributeHandlers = attributeHandlers;

		
			
			if( this.exp4jExpression == null) {
				expressionBuilder = new ExpressionBuilder(this.expression);	
				if(numericFuncList!=null && !numericFuncList.isEmpty())
				{
					for(Function f:numericFuncList)
					{
						expressionBuilder.function(f);
					}
				}
				this.exp4jExpression = expressionBuilder.build();
			}
			
			if(this.attributeHandlers!=null)
			{	
				for(AttributeHandler ah:this.attributeHandlers)
				{
					//We check if the variable exist in the expression before any link
					//if(this.expression.contains(ah.getDbName()))
					this.exp4jExpression.variable(ah.getDbName(), Double.valueOf(ah.getValue()));
				}
			}
			
			this.value = this.exp4jExpression.evaluate();	
//		} catch(Exception e ){
//			Messenger.printStackTrace(e);
//			IgnoreException.throwNewException(SaadaException.WRONG_PARAMETER, e);
//		} 

	}
	
	

	/**
	 * @return
	 */
	public String getExpKey(){
		return this.expKey;
	}
	/**
	 * @return
	 */
	public double getValue() {
		return this.value;
	}
	/**
	 * @param attributeHandlers
	 * @return
	 * @throws FatalException
	 */
	public double getValue(List<AttributeHandler> attributeHandlers,List<Function> numericFuncList) throws Exception {
		this.isEvaluated = false;
		this.evaluate(attributeHandlers,numericFuncList);
		return this.value;
	}
	/**
	 * @return
	 */
	public String getStringValue() {
		return String.valueOf(this.getValue());
	}
	/**
	 * @param attributeHandlers
	 * @return
	 * @throws FatalException
	 */
	public String getStringValue(List<AttributeHandler> attributeHandlers,List<Function> numericFuncList) throws Exception {
		return String.valueOf(this.getValue(attributeHandlers,numericFuncList));
	}

}
