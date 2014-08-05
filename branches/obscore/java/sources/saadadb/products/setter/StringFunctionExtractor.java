package saadadb.products.setter;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.objecthunter.exp4j.function.Function;
import saadadb.util.RegExp;

/**
 * Extract String functions from an expression
 * @author pertuy
 * @version $Id$
 */
public class StringFunctionExtractor {

	protected Map<String,String> stringFunctionList;
	protected String expression;
	protected boolean functionFound =false;
	protected Map<String,StringFunctionDescriptor> splitedFunctionsMap;

	public StringFunctionExtractor(String expr)
	{
		expression=expr;
	}

	/**
	 * Build a map of functions and replace the functions by a flag in the expression
	 * @return
	 */
	private void extractStringFunction()
	{
		Matcher matcher;
		Pattern pattern;
		stringFunctionList=new TreeMap<String,String>();
		String key="@SF";
		int i=0;
		/*
		 * For all our Dictionary functions
		 */
		for(Entry<String,String> e:DictionaryStringFunction.index.entrySet())
		{
			/*
			 * We check in the expression if there's a string like "... Function(something) ... "
			 */
			pattern=Pattern.compile(e.getKey()+"\\([^()]*\\)");
			matcher=pattern.matcher(expression);
			//if we find a match, we're stocking it in the stringFunctionList
			while(matcher.find())
			{
				functionFound=true;
				i++;
				expression=expression.replace(matcher.group(), key+i+"@");
				stringFunctionList.put(key+i+"@", matcher.group());
			}
		}
	}
	
	

	/**
	 * Split the functions contained by the StringFunctionMap and stock them in a new Map
	 * @return a boolean which indicate if the method has been executed or not
	 */
	private void splitFunction()
	{
		//if no function have been found, we do nothing
		if(this.functionFound)
		{
			splitedFunctionsMap=new TreeMap<String,StringFunctionDescriptor>();
			//This regex allow us to get the params from the functions
			Pattern paramPattern=Pattern.compile(RegExp.FUNCTION_ARGS);
			//This regex allow us to obtain the fuction name
			Pattern functionNamePattern=Pattern.compile(RegExp.FUNCTION_NAME);
			Matcher matcher;
			String tempArgs="";
			String functionName="";
			String[] functionArgs=null;

			for(Entry<String,String> e : stringFunctionList.entrySet())
			{
				//We get the arguments from the function
				matcher=paramPattern.matcher(e.getValue());
				if(matcher.find())
					tempArgs=matcher.group(1);

				//We delete the quotes from the arguments
				//tempArgs=tempArgs.replace("\"","");

				//Separate the differents paramater from a same function
				functionArgs =tempArgs.split("\\s*,\\s*");

				//We get the function name
				matcher=functionNamePattern.matcher(e.getValue());
				if(matcher.find())
					functionName = matcher.group();

				//We keep the name and the arguments in a new map
				splitedFunctionsMap.put(e.getKey(), new StringFunctionDescriptor(functionName,functionArgs));

			}
		}
	}
	
	/**
	 * Extract and split the String functions from the expression. 
	 * @return a boolean which indicate if a String function was found
	 */
	public boolean extractAndSplit()
	{
		extractStringFunction();
		splitFunction();
		return functionFound;
	}

}
