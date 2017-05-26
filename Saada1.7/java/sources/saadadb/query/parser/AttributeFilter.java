package saadadb.query.parser;

import static saadadb.query.parser.SaadaQLRegex.ATTRIBUTE;
import static saadadb.query.parser.SaadaQLRegex.ATTR_FILTER;
import static saadadb.query.parser.SaadaQLRegex.FacWS;
import static saadadb.query.parser.SaadaQLRegex.WHERE_CLAUSE_LD;
import static saadadb.query.parser.SaadaQLRegex.WHERE_CLAUSE_RD;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;

/**
 * @author laurent
 * @version $Id$
 *
 */
public final class AttributeFilter {
	private static final String syntax =  ATTR_FILTER+" { attr list }"; 
	private static final String inStat  = "(" + ATTRIBUTE + "(," + ATTRIBUTE + ")*)?";  // Inner Statement" +
	private static final Pattern pattern = Pattern.compile(ATTR_FILTER + FacWS + WHERE_CLAUSE_LD + FacWS +"("+ inStat +")"+ FacWS + WHERE_CLAUSE_RD);
	private final String   strMatch     ;
	private final String   theStatement ;
	private String[] columns;
	
	public AttributeFilter(String strQuery) throws QueryException{
		String[] sT = this.parse(strQuery);
		this.strMatch = sT[0];
		this.theStatement = sT[1];
		this.columns = theStatement.split("[\\s,]+");
	}
	
	public static boolean isIn(String strQ){
		return pattern.matcher(strQ).find();
	}
	
	public String[] getGolumns() {
		return this.columns;
	}
	
	/**
	 * @param strQ
	 * @return
	 * @throws QueryException
	 */
	protected final String[] parse(String strQ) throws QueryException {
		Matcher m = this.getPattern().matcher(strQ);
		if(m.find()){
			return new String[] {m.group(0), m.group(1)};
		}else{
			QueryException.throwNewException(SaadaException.SYNTAX_ERROR,"Error parsing the clause "+this.getClauseName()+"!\nCheck the syntax \""+this.getSyntax()+"\"");
			return null;
		}
	}

	public final String   getStrMatch     (){return this.strMatch     ;}
	/**
	 * @return
	 */
	protected final Pattern getPattern()  {return pattern;}

	/**
	 * @return
	 */
	protected final String getClauseName(){return ATTR_FILTER;}
	/**
	 * @return
	 */
	protected final String getSyntax()    {return syntax;}
}
