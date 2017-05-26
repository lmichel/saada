
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
         * @param regexp
         * @param expectedMatches
         */
        public RegExpMatcher(String regexp, int expectedMatches){
                pattern = Pattern.compile(regexp);
                this.expectedMatches = expectedMatches;         
        }
        
        /**
         * @param string
         * @return the list of substrings matching the capturing groups 
         *         if their number equals to expectedMatches. return null otherwise
         */
        public  List<String> getMatches(String string){
                Matcher matcher = pattern.matcher(string);
                if ( matcher.matches() && matcher.groupCount() == this.expectedMatches) {
                        List<String>    retour = new ArrayList<String>();
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
                if ( matcher.matches() && matcher.groupCount() >= this.expectedMatches) {
                        List<String>    retour = new ArrayList<String>();
                        for( int i=1 ; i<=matcher.groupCount() ; i++) {
                                retour.add(matcher.group(i));                           
                        }
                        return retour;
                }
                return null;            
        }

}