package saadadb.products.setter;

/**
 * @author michel
 * @version $Id$
 */
/**
 * @author michel
 * @version $Id$
 */
public class StringFunctionDescriptor {

	public String functionName;
	public String[] functionArguments;
	
	/**
	 * @param name
	 * @param args
	 */
	public StringFunctionDescriptor(String name, String[] args)
	{
		functionName=name;
		functionArguments = args;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String retour = functionName + "(" ;
		for( String s: functionArguments){
			retour += s + " ";
		}
		return retour + ")";
	}
	
	/**
	 * Returns true if at least one param is not a constant string
	 * @return
	 */
	public boolean useKeywords() {
		for( String s: functionArguments) {
			if( !(s.startsWith("\"") || s.startsWith("'")) ) {
				return true;
			}
		}
		return false;
	}
	
	public String[] getUnquotedArguments() {
		String[] retour = new String[this.functionArguments.length];
		for( int i=0 ; i<retour.length ; i++) {
			retour[i] = this.functionArguments[i].replaceAll("['\"]", "");
		}
		return retour;
	}
} 
