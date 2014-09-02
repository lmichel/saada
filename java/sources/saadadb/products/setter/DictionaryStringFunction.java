package saadadb.products.setter;

import java.util.TreeMap;

import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.SaadaException;
import saadadb.util.DateUtils;
import saadadb.util.Merger;

/**
 * Repertory and handle the execution of every String based function which can be found in an expression 
 * @author pertuy
 * @version $Id$
 */
public class DictionaryStringFunction {
	
	public static final String MJD = "MJD";
	public static final String LOWERCASE="toLower";
	public static final String UPPERCASE="toUpper";
	public static final String SUBSTRING = "substring";
	public static final String STRCAT = "strcat";
	
	/**
	 * This Map represent the dictionary "Index", in other words it lists every function contain by this class 
	 * (the key) with a description for each one (the value).
	 */
	public static TreeMap<String,String> index;
	static{
		index = new TreeMap<String,String>();
		index.put(MJD,"Convert a date (MM-DD-YYYY or MM/DD/YYYY or MM DD YYYY) in Modified Julien Days" );
		index.put(LOWERCASE, "Convert the String into lowercase characters");
		index.put(UPPERCASE, "Convert the String into uppercase characters");
		index.put(SUBSTRING, "Returns a new string that is a substring of this string. "
				+ "The substring begins with the character at the specified index and extends to "
				+ "the end of this string or up to endIndex");
		index.put(STRCAT, "Concatenates all parameters in one string");
	}
	/**
	 * Execute the function corresponding to the String parameter with the specified arguments.
	 * The arguments from functionArgs must be in the right order to properly execute the function.
	 * @param function
	 * @param ah
	 * @return
	 * @throws Exception 
	 */
	public static final String exec(String function,String[] functionArgs) throws Exception
	{
		/*
		 * There is no function without any argument
		 */
		if(functionArgs==null)
			IgnoreException.throwNewException(SaadaException.WRONG_PARAMETER, "No arguments found");
		String result=null;

		/*
		 * We try to identify the function for its execution
		 * For each function we check the number of arguments
		 */
		switch(function){
		case MJD:
			if(functionArgs.length!=1)
				IgnoreException.throwNewException(SaadaException.WRONG_PARAMETER, "Must have 1 parameter");
			result=MJD(functionArgs[0]);
			break;
		case SUBSTRING:
			if(functionArgs.length!=3)
				IgnoreException.throwNewException(SaadaException.WRONG_PARAMETER, "Must have 3 parameter");
			result = SUBSTRING(functionArgs[0],Integer.parseInt(functionArgs[1]), Integer.parseInt(functionArgs[2]));
			break;
		case LOWERCASE:
			if(functionArgs.length!=1)
				IgnoreException.throwNewException(SaadaException.WRONG_PARAMETER, "Must have 1 parameter");
			result=functionArgs[0].toLowerCase();
			break;
		case UPPERCASE:
			if(functionArgs.length!=1)
				IgnoreException.throwNewException(SaadaException.WRONG_PARAMETER, "Must have 1 parameter");
			result=functionArgs[0].toUpperCase();
			break;
		case STRCAT:
			result = STRCAT(functionArgs);
			break;
		default:
			//If we reach this point, we didn't find any corresponding function
			IgnoreException.throwNewException(SaadaException.WRONG_PARAMETER, function + ": unknown function");
		}

		return result;

	}
	
	public static final String SUBSTRING(String string, int start, int stop) throws Exception
	{
		return string.substring(start, stop) ;
	}
	public static final String STRCAT(String[] strings) throws Exception
	{
		return Merger.getMergedArray("", strings);
	}

	public static final String MJD(String args) throws Exception
	{
		return DateUtils.getMJD(args);
	}
	
	public static void main(String[] args)
	{
		try {
			System.out.println(exec("toLower",new String[]{"HELLO WORLD"}));
			System.out.println(exec("MJD",new String[]{"12-06-2014"}));
			System.out.println(exec("MJD",new String[]{"plop"}));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
