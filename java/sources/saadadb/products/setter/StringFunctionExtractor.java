package saadadb.products.setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import saadadb.vocabulary.RegExp;

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
	/**
	 * true if the expression only contains one string expression
	 */
	protected boolean singleStringExpression = false;

	public StringFunctionExtractor(String expr) throws Exception
	{
		expression=expr;
		stringFunctionList=new TreeMap<String,String>();
		splitedFunctionsMap=new TreeMap<String,StringFunctionDescriptor>();
		this.extractStringFunction();
		this.splitFunction();
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
				expression=expression.replace(matcher.group(), key+i+"@").trim();;
				stringFunctionList.put(key+i+"@", matcher.group());
			}
		}
	}



	/**
	 * Split the functions contained by the StringFunctionMap and stock them in a new Map
	 * @return a boolean which indicate if the method has been executed or not
	 * @throws Exception 
	 */
	private void splitFunction() throws Exception {	
		//if no function have been found, we do nothing
		if(this.functionFound)	{
			splitedFunctionsMap=new TreeMap<String,StringFunctionDescriptor>();
			//This regex allow us to get the params from the functions
			Pattern paramPattern=Pattern.compile(RegExp.FUNCTION_ARGS);
			//This regex allow us to obtain the fuction name
			Pattern functionNamePattern=Pattern.compile(RegExp.FUNCTION_NAME);
			Matcher matcher;
			String tempArgs="";
			String functionName="";
			String[] functionArgs=null;

			for(Entry<String,String> e : stringFunctionList.entrySet())	{
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
			this.computeFunctionsWithConstParams();
			if( splitedFunctionsMap.size() == 1 && this.expression.startsWith("@")  && this.expression.endsWith("@")) {
				this.singleStringExpression = true;
			}
		}
	}

	/**
	 * @throws Exception
	 */
	private void computeFunctionsWithConstParams() throws Exception {
		List<String> toBeRemoved = new ArrayList<String>();
		for( Entry<String, StringFunctionDescriptor> e: this.splitedFunctionsMap.entrySet()){
			StringFunctionDescriptor sfd = e.getValue();
			if( !sfd.useKeywords()){
				this.expression = this.expression.replace(e.getKey(), sfd.execute());
				toBeRemoved.add(e.getKey());
			}
		}
		for( String s: toBeRemoved) {
			splitedFunctionsMap.remove(s);
		}
	}

	/**
	 * Extract and split the String functions from the expression. 
	 * @return a boolean which indicate if a String function was found
	 * @throws Exception 
	 */
	//	public boolean extractAndSplit() throws Exception
	//	{
	//		extractStringFunction();
	//		splitFunction();
	//		return functionFound;
	//	}

	/**
	 * @return
	 */
	public boolean isSingleStringExpression() {
		return this.singleStringExpression;
	}

	public boolean hasStringFunctions() {
		return (this.splitedFunctionsMap.size() != 0);
	}

	/**
	 * @return true if 	at least one function uses at least on keyword
	 */
	public boolean useKeywords() {
		for( StringFunctionDescriptor s: this.splitedFunctionsMap.values() ){
			if( s.useKeywords()) {
				return true;
			}
		}
		return  false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String retour = "expr=" + this.expression + " Fcts:[";
		for( Entry<String, StringFunctionDescriptor> e: this.splitedFunctionsMap.entrySet()){
			retour += e.getKey() + ":" + e.getValue() + " ";
		}
		retour += "]";
		return retour;
	}
	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		StringFunctionExtractor s = new StringFunctionExtractor("strcat(_tmin, 'eeee', _tmax)");
		System.out.println(s + "\n");

		s = new StringFunctionExtractor("strcat(_tmin, 'eeee', _tmax) +strcat('_tmin', 'eeee', '_tmax')");
		System.out.println(s + "\n");	

		s = new StringFunctionExtractor("strcat(_tmin, 'eeee', _tmax) +XXX('_tmin', 'eeee', '_tmax')");
		System.out.println(s+ "\n");	
		
		s = new StringFunctionExtractor("MJD('11/20/1858') - MJD('11/22/1858')");
		System.out.println(s);	
		
		s = new StringFunctionExtractor("strcat(A, B, C)");
		System.out.println(s);	
		for(Entry<String,StringFunctionDescriptor> e : s.splitedFunctionsMap.entrySet()){
			System.out.println(" - " + e);
			String[] ags = e.getValue().functionArguments;
			for( String sr: ags){
				System.out.println("   ." + sr);
			}
		}
		s = new StringFunctionExtractor("strcat(_tmin, 'eeee://sqdsqd/', _tmax) +XXX('_tmin', 'eeee', '_tmax')");
		System.out.println(s+ "\n");	
		s = new StringFunctionExtractor("split(_tmin, '_', 2)");
		System.out.println(s+ "\n");	
	}
}
