package saadadb.query.parser;

import static saadadb.query.parser.SaadaQLRegex.LIMIT;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;

/** * @version $Id$

 * @author F.X. Pineau
 */
public final class Limit{
	private static final String syntax = "LIMIT positive_int"; 
	private static final Pattern pattern = Pattern.compile(LIMIT + "\\s+\\+?([1-9]\\d*)");
	
	private final String strMatch;
	private final int limit;
	
	public Limit(String strQuery) throws QueryException {
		String[] sTab = parse(strQuery);
		this.strMatch = sTab[0];
		this.limit = Integer.parseInt(sTab[1]);
	}
	
	/**
	 * Added by G.Mantelet
	 * @param str
	 * @param top
	 */
	public Limit(String str, int top){
		strMatch = str;
		limit = top;
	}
	
	public final String getStrMatch     (){return this.strMatch     ;}
	public final int    getLimit        (){return this.limit        ;}
	
	public static final boolean isIn(String strQ){return pattern.matcher(strQ).find();}
	
	private static final String[] parse(String strQ) throws QueryException {
		Matcher m = pattern.matcher(strQ);
		if(m.find()){
			return new String[]{m.group(0),m.group(1)};
		}else{
			QueryException.throwNewException(SaadaException.SYNTAX_ERROR,"Error parsing the Limit clause!\nCheck the syntax \""+syntax+"\"");
			return null;
		}
	}
}
