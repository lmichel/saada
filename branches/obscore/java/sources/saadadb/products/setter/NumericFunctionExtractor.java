package saadadb.products.setter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.objecthunter.exp4j.function.Function;
import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.vocabulary.RegExp;

/**
 * This class is used to get the numeric Functions used by exp4j
 * @author pertuy
 * @version $Id$
 */
public class NumericFunctionExtractor {
	protected ArrayList<Function> numericFunctions;
	String expression;
	
	public NumericFunctionExtractor(String expr)
	{
		expression=expr;
	}
	
	/**
	 * Extract the numeric functions from an expression
	 * @return a list of numeric functions
	 * @throws Exception
	 */
	public ArrayList<Function> extractFunctions() throws Exception
	{
		Matcher matcher;
		Pattern pattern;
		numericFunctions=new ArrayList<Function>();
		/*
		 * For all our Dictionary functions
		 */
		for(Entry<String,String> e:DictionaryNumericFunction.index.entrySet())
		{
			/*
			 * We check in the expression if there's a string like "... Function(something) ... "
			 */
			pattern=Pattern.compile(e.getKey());
			matcher=pattern.matcher(expression);
			//if we find a match, we're stocking it in the stringFunctionList
			while(matcher.find())
			{
				DictionaryNumericFunction.addToFunctionList(matcher.group().trim(), numericFunctions);
			}
		
		}
		return numericFunctions;
	}
	
	/**
	 * Treat the conversion Function case
	 * @param exprAttributes 
	 * @param convert function with its arguments
	 * @return result of the conversion
	 * @throws IgnoreException 
	 */
	public String treatConvertFunction(List<AttributeHandler> exprAttributes) throws IgnoreException
	{
		AttributeHandler ahToConvert = null;
		String function = this.checkConvertFunction();
		if(function!=null)
		{
			//This regex allow us to get the params from the functions
			Pattern paramPattern=Pattern.compile(RegExp.FUNCTION_ARGS);
			Matcher matcher;
			String tempArgs="";
			String[] functionArgs=null;

			//We get the arguments from the function
			matcher=paramPattern.matcher(function);
			if(matcher.find())
				tempArgs=matcher.group(1);

			//We delete the quotes from the arguments
			tempArgs=tempArgs.replace("\"","");

			//Separate the differents paramater from a same function
			functionArgs =tempArgs.split("\\s*,\\s*");


			if(functionArgs.length>3 && functionArgs.length<2)
			{
				IgnoreException.throwNewException(SaadaException.INTERNAL_ERROR, "Convert function must have 2 or 3 arguments");
			}
			
			//We check and replace the Ah presents in the function
			if(exprAttributes!=null && !exprAttributes.isEmpty())
				for (int i=0;i<functionArgs.length;i++)
				{
					for(AttributeHandler ah :exprAttributes)
					{
						if(functionArgs[i].trim().equals(ah.getDbName()) || functionArgs[i].trim().equals(ah.getName()))
						{
							functionArgs[i]=ah.getDbName();//getValue();
							ahToConvert=ah;
							break;
						}
					}
					//There is only one Ah to detect in the convert function
					if(ahToConvert!=null)
						break;
				}
			String result="";
			if(functionArgs.length==3)
				 result=DictionaryNumericFunction.convert(functionArgs[0], functionArgs[1], functionArgs[2]);//Double.toString(DictionaryNumericFunction.convert(functionArgs[0], functionArgs[1], functionArgs[2]));
			else
			{
				if((ahToConvert.getUnit()!=null) && (!ahToConvert.getUnit().isEmpty()))
					 result=DictionaryNumericFunction.convert(functionArgs[0], ahToConvert.getUnit(), functionArgs[1]);
				else
					IgnoreException.throwNewException(SaadaException.INTERNAL_ERROR, "The keyword ("+ ahToConvert.getDbName()+") to convert have no defined unit");
			}
			expression=expression.replace(function, result);
		}
		return expression;
	}

	
	private String checkConvertFunction()
	{
		Pattern pattern = Pattern.compile(DictionaryNumericFunction.CONVERT+"\\([^()]*\\)");
		Matcher matcher=pattern.matcher(expression);

		if(matcher.find())
			return matcher.group();
		else
			return null;
		}
}
