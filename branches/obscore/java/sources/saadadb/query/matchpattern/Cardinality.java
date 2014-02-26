package saadadb.query.matchpattern;

import static saadadb.query.parser.SaadaQLRegex.ArgSep;
import static saadadb.query.parser.SaadaQLRegex.CARDINALITY;
import static saadadb.query.parser.SaadaQLRegex.FacWS;
import static saadadb.query.parser.SaadaQLRegex.NumericValue;
import static saadadb.query.parser.SaadaQLRegex.OpCompatiblesValue;
import static saadadb.query.parser.SaadaQLRegex.SAADA_OPs;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;

/** * @version $Id: Cardinality.java 118 2012-01-06 14:33:51Z laurent.mistahl $

 * @author F.X. Pineau
 */
public final class Cardinality{
	private static final String syntax = "Cardinality op number|(number,number)";

	private static final String inStat = "(" + SAADA_OPs + ")" + FacWS + "(" + OpCompatiblesValue + ")";
	private static final String regex = CARDINALITY + FacWS + inStat + FacWS ;
	private static final Pattern pattern = Pattern.compile(regex);

	private final String strMatch;
	private final Card card;

	/**
	 * @param strQuery
	 * @throws SaadaException
	 */
	public Cardinality(String strQuery) throws SaadaException {
		String[] sTab = parse(strQuery);
		this.strMatch = sTab[0];
		this.card=new Card(sTab[1],sTab[2],sTab[3]);
	}

	/**
	 * @return
	 */
	public final String   getStrMatch(){return this.strMatch;}
	/**
	 * @param strQ
	 * @return
	 */
	public static final boolean isIn(String strQ){return pattern.matcher(strQ).find();}

	/**
	 * @return
	 */
	public final Card getCard  (){return this.card;}

	/**
	 * @param strQ
	 * @return
	 * @throws SaadaException
	 * @throws ParsingException
	 */
	private static final String[] parse(String strQ) throws SaadaException{
		Matcher m = pattern.matcher(strQ);
		if(m.find()){
			if(m.group(2).matches(NumericValue)){
				return new String[]{m.group(0),m.group(1),m.group(2),"0"};
			}else{
				String[] strTab = m.group(2).replace("(","").replace(")","").trim().split(FacWS+ArgSep+FacWS);
				return new String[]{m.group(0),m.group(1),strTab[0],strTab[1]};
			}	
		}
		QueryException.throwNewException(SaadaException.SYNTAX_ERROR,"Errror parsing the Cardinality clause!\nCheck the syntax \""+syntax+"\"");
		return null;
	}
}
 
