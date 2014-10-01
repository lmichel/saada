package saadadb.products.setter;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.function.Function;
import saadadb.collection.Category;
import saadadb.collection.SaadaOID;
import saadadb.collection.obscoremin.SaadaInstance;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.util.Messenger;
import saadadb.util.SaadaConstant;
import saadadb.vo.request.formator.votable.OidsVotableFormator;

/**
 * This class calculates an expression using the exp4j package
 * 
 * @author hahn
 * @version $Id$
 */

public class TestLabWrapper {
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
	// private List<AttributeHandler> attributeHandlers;
	private SaadaInstance saadaInstance;
	private double value = SaadaConstant.DOUBLE;
	boolean isEvaluated = false;
	/**
	 * Evaluate an expression which can contain several keywords and return its
	 * result. The expression must not be null
	 * 
	 * @param expression
	 * @param attributeHandlers
	 * @return
	 * @throws FatalException
	 */
	public TestLabWrapper(String expression, SaadaInstance sadInstance,
			List<Function> numericFuncList) throws Exception {
		if (expression == null)
			IgnoreException.throwNewException(SaadaException.WRONG_PARAMETER,
					"expression parameter missing");
		
		
		// try {
		// if(variables==null)
		// FatalException.throwNewException(SaadaException.WRONG_PARAMETER,
		// "variables parameter missing");
		// this.attributeHandlers = attributeHandlers;
		this.expKey = expression.replaceAll("\\s", "");
		this.expression = expression;
		saadaInstance = sadInstance;
		this.evaluate(numericFuncList);
		// } catch(Exception e ){
		// Messenger.printStackTrace(e);
		// IgnoreException.throwNewException(SaadaException.WRONG_PARAMETER, e);
		// }
	}

	/**
	 * @param expression
	 * @throws SaadaException
	 */
	public TestLabWrapper(String expression) throws Exception {

		if (expression == null)
			IgnoreException.throwNewException(SaadaException.WRONG_PARAMETER,
					"expression parameter missing");
		
	
		try {
			// if(variables==null)
			// FatalException.throwNewException(SaadaException.WRONG_PARAMETER,
			// "variables parameter missing");
			this.expKey = expression.replaceAll("\\s", "");
			this.expression = expression;
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			IgnoreException
					.throwNewException(SaadaException.WRONG_PARAMETER, e);
		}
	}

	/**
	 * Evaluate an expression with the Attributehandler provided Can be use on a
	 * wrapper to recalculate the same expression with differents values
	 * 
	 * @param numericFuncList
	 *            : List of custom function to evaluate
	 * @param attributeHandlers
	 *            list of variables
	 * @throws FatalException
	 */
	public void evaluate(List<Function> numericFuncList) throws Exception {
		if (this.isEvaluated) {
			return;
		}
		// try {
		// this.attributeHandlers = attributeHandlers;

		if (this.exp4jExpression == null) {
			expressionBuilder = new ExpressionBuilder(this.expression);
			if (numericFuncList != null && !numericFuncList.isEmpty()) {
				for (Function f : numericFuncList) {
					expressionBuilder.function(f);
				}
			}
			this.exp4jExpression = expressionBuilder.build();
		}

		/*
		 * if(this.attributeHandlers!=null) { for(AttributeHandler
		 * ah:this.attributeHandlers) { //We check if the variable exist in the
		 * expression before any link
		 * //if(this.expression.contains(ah.getNameattr()))
		 * this.exp4jExpression.variable(ah.getNameattr(),
		 * Double.valueOf(ah.getValue())); } }
		 */
				
		LinkedHashMap<String, AttributeHandler> ahList = getCollectionAttr();

	
		
		Iterator i = ahList.keySet().iterator();
		while(i.hasNext())
		{
			
			// We check if the variable exist in the expression before any link
			
				// TODO check if working
				this.exp4jExpression.variable((ahList.get((String)i.next()).getNameattr()),
						Double.valueOf(saadaInstance.getFieldValue(ahList.get((String)i.next()).getNameattr()).toString()));
		}
		this.value = this.exp4jExpression.evaluate();
		// } catch(Exception e ){
		// Messenger.printStackTrace(e);
		// IgnoreException.throwNewException(SaadaException.WRONG_PARAMETER, e);
		// }

	}

	/**
	 * @return
	 */
	public String getExpKey() {
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
	public double getValue(List<Function> numericFuncList) throws Exception {
		this.isEvaluated = false;
		this.evaluate(numericFuncList);
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
	public String getStringValue(List<Function> numericFuncList)
			throws Exception {
		return String.valueOf(this.getValue(numericFuncList));
	}
	/**
	 * Returns an LinkedHashMap of AttributeHandler of the category for the collection given by collectionName
	 * @return
	 * @throws FatalException 
	 */
		@SuppressWarnings("static-access")
		protected  LinkedHashMap<String, AttributeHandler> getCollectionAttr() throws FatalException{
			String collectionName = saadaInstance.getCollection().getName();
			switch(SaadaOID.getCategoryNum(saadaInstance.oidsaada)) {
			case Category.SPECTRUM : return Database.getCachemeta().getCollection(collectionName).getAttribute_handlers_spectrum();
			case Category.IMAGE : return Database.getCachemeta().getCollection(collectionName).getAttribute_handlers_image();
			}
			//TODO should throws an exception if null?
			return null;
		}
}
