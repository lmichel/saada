package saadadb.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Class processing a simple capturing group.
 *
 * @author michel
 * @version $Id$
 * 
 * 09/2015: add method extractVariables
 */
public class RegExpMatcher {
	/**
	 * Regular expression to be matched
	 */
	private Pattern pattern ;
	/**
	 * Minimal number of matches expected for a given String
	 */
	private int expectedMatches;
	
	/**
	 * @param regexp : regular expression
	 * @param expectedMatches : Minimal number of matches expected for a given String
	 */
	public RegExpMatcher(String regexp, int expectedMatches){
		pattern = Pattern.compile(regexp);
		this.expectedMatches = expectedMatches;		
	}
	/**
	 * param-less constructor used for {@link RegExpMatcher#extractVariables(String)}
	 */
	public RegExpMatcher(){
	}
	
	/**
	 * @param string
	 * @return the list of substrings matching the capturing groups 
	 *         if their number equals to expectedMatches. return null otherwise
	 */
	public  List<String> getMatches(String string){
		Matcher matcher = pattern.matcher(string);
		if ( matcher.matches() && matcher.groupCount() == this.expectedMatches) {
			List<String>	retour = new ArrayList<String>();
			for( int i=1 ; i<=this.expectedMatches ; i++) {
				retour.add(matcher.group(i));				
			}
			
			return retour;
		}
		return null;		
	}

	/**
	 * @param string
	 * @return the list of substrings matching the capturing groups 
	 *         if their number is greater or equals to expectedMatches. return null otherwise
	 */
	public  List<String> getMatchesAndMore(String string){
		if( string == null ){
			return null;
		}
		Matcher matcher = pattern.matcher(string);
		System.out.println( matcher.matches());
		System.out.println( matcher.groupCount());
		if ( matcher.matches() && matcher.groupCount() >= this.expectedMatches) {
			List<String>	retour = new ArrayList<String>();
			for( int i=1 ; i<=matcher.groupCount() ; i++) {
				retour.add(matcher.group(i));				
			}
			return retour;
		}
		return null;		
	}
	
	/**
	 * Extract from string a list of variable prefixed by $
	 * The regexp given to the constructor has no effect
	 * @param string
	 * @return
	 */
	public List<String> extractVariables(String string){
		pattern = Pattern.compile("(\\$[a-zA-Z_][a-zA-Z_0-9]+)");
		this.expectedMatches = 0;	
		Matcher matcher = pattern.matcher(string);
		System.out.println( matcher.matches());
		System.out.println( matcher.groupCount());
		List<String>	retour = new ArrayList<String>();
		while (matcher.find()) {
			retour.add(matcher.group(1));				
		}
		return retour;
	}
	
	
	public static void main(String[] args){
		RegExpMatcher rem = new RegExpMatcher();
		List<String> le = rem.extractVariables("toto$filename-$target_name");
		for( String s: le){
			System.out.println(s);
		}		
	}
}