package saadadb.products.setter;

import java.util.ArrayList;
import java.util.TreeMap;

import net.objecthunter.exp4j.function.Function;
import saadadb.products.setter.numericFunctions.NumPowerByTwo;
import saadadb.products.setter.numericFunctions.NumSqrt;
import saadadb.products.setter.numericFunctions.ToRadian;

public class DictionaryNumericFunction {
	public static final String TORADIAN="toRadian";
	public static final String SQRT="sqrt";
	public static final String POW="pow";
	public static final String CONVERT="convert";
	/**
	 * List of known numeric users function
	 */
	public static TreeMap<String,String> index;
	static{
		index = new TreeMap<String,String>();
		index.put(TORADIAN, "desc");
		index.put(SQRT, "desc");
		index.put(POW,"desc");
		index.put(CONVERT, "desc");
	}
	
	/**
	 * This method instantiate an object "Function" and add it in the list given in parameters.
	 * @param functionName
	 * @param functions
	 * @throws Exception
	 */
	public static final void addToFunctionList(String functionName,ArrayList<Function> functions) throws Exception
	{
		switch(functionName)
		{
		case TORADIAN:
			functions.add(new ToRadian(TORADIAN));
			break;
		case SQRT:
			functions.add(new NumSqrt(SQRT));
			break;
		case POW:
			functions.add(new NumPowerByTwo(POW));
			break;
		default :
			break;
		}
		return;
		
	}
	
	
	public static double convert(String value, String oldUnit, String newUnit)
	{
		return 42;
	}

}
