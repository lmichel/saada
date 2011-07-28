package saadadb.query.matchpattern;

import static saadadb.query.parser.SaadaQLRegex.ASS_CLAUSE_LD;
import static saadadb.query.parser.SaadaQLRegex.ASS_CLAUSE_RD;
import static saadadb.query.parser.SaadaQLRegex.ArgSep;
import static saadadb.query.parser.SaadaQLRegex.FacWS;
import static saadadb.query.parser.SaadaQLRegex.NumericValue;
import static saadadb.query.parser.SaadaQLRegex.OpCompatiblesValue;
import static saadadb.query.parser.SaadaQLRegex.QUAL;
import static saadadb.query.parser.SaadaQLRegex.QUALIFIER;
import static saadadb.query.parser.SaadaQLRegex.SAADA_OPs;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;

/**
 * @author F.X. Pineau
 */
public final class Qualifier{
	private static final String syntax = "Qualifier { qual op val | (val1,val2) }";

	private static final String inStat = "("+ QUAL + ")" + FacWS + "(" + SAADA_OPs + ")" + FacWS + "(" +OpCompatiblesValue + ")" ;
	private static final String regex = QUALIFIER + FacWS + ASS_CLAUSE_LD + FacWS + inStat + FacWS + ASS_CLAUSE_RD ;
	private static final Pattern pattern = Pattern.compile(regex);

	private final String strMatch;
	private final Qualif qual;
	
	/**
	 * @param strQuery
	 * @throws SaadaException
	 */
	public Qualifier(String strQuery) throws SaadaException {
		String[] sTab = parse(strQuery);
		this.strMatch = sTab[0];
		this.qual = new Qualif(sTab[1],sTab[2],sTab[3],sTab[4]);
	}

	public final String getStrMatch(){return this.strMatch;}
	public final Qualif getQual(){return this.qual;}

	public static final boolean isIn(String strQ){return pattern.matcher(strQ).find();}
	
	/**
	 * @param strQ
	 * @return
	 * @throws QueryException
	 */
	private static final String[] parse(String strQ) throws QueryException {
		Matcher m = pattern.matcher(strQ);
		if(m.find()){
			if(m.group(3).matches(NumericValue)){
				return new String[]{m.group(0),m.group(1),m.group(2),m.group(3),"0.0"};
			}else{
				String[] strTab = m.group(3).replace("(","").replace(")","").trim().split(FacWS+ArgSep+FacWS);
				return new String[]{m.group(0),m.group(1),m.group(2),strTab[0],strTab[1]};
			}	
		}
		QueryException.throwNewException(SaadaException.SYNTAX_ERROR,"Errror parsing the Qualifier clause!\nCheck the syntax \""+syntax+"\"");
		return null;
	}
}

