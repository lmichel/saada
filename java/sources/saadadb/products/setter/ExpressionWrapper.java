package saadadb.products.setter;

import java.util.List;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;

/**
 * This class calculate an expression using the exp4j package
 * @author pertuy
 * @version $Id$
 */

public class ExpressionWrapper {

	/**
	 * Evaluate an expression which can contain several keywords and return its result.
	 * The expression must not be null
	 * @param expression
	 * @param variables
	 * @return
	 * @throws FatalException
	 */
	public String evaluate(String expression,List<AttributeHandler> variables) throws Exception
	{
		
		if(expression==null)
			FatalException.throwNewException(SaadaException.WRONG_PARAMETER, "expression parameter missing");
//		if(variables==null)
//			FatalException.throwNewException(SaadaException.WRONG_PARAMETER, "variables parameter missing");
		Expression expr = new ExpressionBuilder(expression)
		.build();
		if(variables!=null)
		{	
			for(AttributeHandler ah:variables)
			{
				expr.variable(ah.getNameattr(), Double.valueOf(ah.getValue()));
			}
		}
		
		return String.valueOf(expr.evaluate());
		
	}
	
	/**
	 * Trivial test function
	 * @throws Exception
	 */
	public void trivialTest() throws Exception
	{
		System.out.println(evaluate("1+2+3+4+5*2",null));
	}
}
