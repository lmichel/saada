package saadadb.query.matchpattern;

import static saadadb.query.parser.SaadaQLRegex.ASS_CLAUSE_LD;
import static saadadb.query.parser.SaadaQLRegex.ASS_CLAUSE_RD;
import static saadadb.query.parser.SaadaQLRegex.ASS_OBJ_CLASS;
import static saadadb.query.parser.SaadaQLRegex.ArgSep;
import static saadadb.query.parser.SaadaQLRegex.CLASS;
import static saadadb.query.parser.SaadaQLRegex.FacWS;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
/** * @version $Id$

 * @author F.X. Pineau
 */
public final class AssObjClass{
	private static final String syntax = "AssObjClass { class_name [, ...] }"; 
	private static final String inStat = "(?:" + CLASS + FacWS + ArgSep + FacWS + ")*(?:" + CLASS + ")";
	private static final String regex = ASS_OBJ_CLASS + FacWS + ASS_CLAUSE_LD + FacWS + "(" + inStat + ")" + FacWS + ASS_CLAUSE_RD ;
	private static final Pattern pattern = Pattern.compile(regex);

	private final String strMatch;
	private final String[] listClass;

	public AssObjClass(String strQuery) throws QueryException{
		String[][] sM = parse(strQuery);
		this.strMatch  = sM[0][0];
		this.listClass = sM[1]; 
	}

	public final String   getstrMatch (){return this.strMatch ;}
	public final String[] getlistClass(){return this.listClass;}

	public static final boolean isIn(String strQ){return pattern.matcher(strQ).find();}

	private static final String[][] parse(String strQ) throws QueryException {
		Matcher m = pattern.matcher(strQ);
		if(m.find()){
			return new String[][]{new String[]{m.group(0)},rmDoublons(m.group(1).split("\\s*"+ArgSep+"\\s*"))};
		}
		else{
			QueryException.throwNewException(SaadaException.SYNTAX_ERROR, "Error parsing the AssObjClass regex!\nCheck the syntax \""+syntax+"\"");
			return null;
		}
	}
	
	/**
	 * Remove doublons from a list of String
	 * @param sTab
	 * @return a list of String without doublons
	 */
	private static final String[] rmDoublons(String[] sTab){
		int rm=0;
		for(int i=0;i<sTab.length;i++)
			for(int j=i+1;j<sTab.length;j++)
				if(sTab[i].equals(sTab[j])){
					sTab[j]=null;
					rm++;
				}
		if(rm==0) return sTab;
		String[] new_sTab = new String[sTab.length-rm];
		int pos=-1;for(String s:sTab)if(s!=null)new_sTab[++pos]=s;
		return new_sTab;
	} 
}
