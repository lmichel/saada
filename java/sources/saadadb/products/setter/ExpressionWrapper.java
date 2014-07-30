package saadadb.products.setter;

import java.util.List;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
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
	private String expression;
	private Expression expressionBuilder;
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
	public ExpressionWrapper(String expression,List<AttributeHandler> attributeHandlers) throws Exception
	{
		if(expression==null)
			IgnoreException.throwNewException(SaadaException.WRONG_PARAMETER, "expression parameter missing");
		try {
			//		if(variables==null)
			//			FatalException.throwNewException(SaadaException.WRONG_PARAMETER, "variables parameter missing");
			this.attributeHandlers = attributeHandlers;
			this.expKey = expression.replaceAll("\\s", "");
			this.expression=expression;
			this.evaluate(attributeHandlers);
		} catch(Exception e ){
			Messenger.printStackTrace(e);
			IgnoreException.throwNewException(SaadaException.WRONG_PARAMETER, e);
		} 
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
	 * @param attributeHandlers
	 * @throws FatalException
	 */
	private void evaluate(List<AttributeHandler> attributeHandlers) throws Exception{
		if(this.isEvaluated ) {
			return;
		}
		try {
			this.attributeHandlers = attributeHandlers;
			if( this.expressionBuilder == null ) {
				this.expressionBuilder = new ExpressionBuilder(this.expression).build();
			}
			if(this.attributeHandlers!=null)
			{	
				for(AttributeHandler ah:this.attributeHandlers)
				{
					//We check if the variable exist in the expression before any link
					if(this.expression.contains(ah.getNameattr()))
						this.expressionBuilder.variable(ah.getNameattr(), Double.valueOf(ah.getValue()));
				}
			}
			this.value = this.expressionBuilder.evaluate();	
		} catch(Exception e ){
			Messenger.printStackTrace(e);
			IgnoreException.throwNewException(SaadaException.WRONG_PARAMETER, e);
		} 

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
	public double getValue(List<AttributeHandler> attributeHandlers) throws Exception {
		this.isEvaluated = false;
		this.evaluate(attributeHandlers);
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
	public String getStringValue(List<AttributeHandler> attributeHandlers) throws Exception {
		return String.valueOf(this.getValue(attributeHandlers));
	}

}
