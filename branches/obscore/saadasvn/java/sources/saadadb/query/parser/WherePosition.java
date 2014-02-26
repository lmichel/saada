package saadadb.query.parser;

import static saadadb.query.parser.SaadaQLRegex.ArgSep;
import static saadadb.query.parser.SaadaQLRegex.COORD_EQU;
import static saadadb.query.parser.SaadaQLRegex.COORD_SYS;
import static saadadb.query.parser.SaadaQLRegex.FacWS;
import static saadadb.query.parser.SaadaQLRegex.NumericValue;
import static saadadb.query.parser.SaadaQLRegex.POS_QUERY_KEYS;
import static saadadb.query.parser.SaadaQLRegex.WHERE_CLAUSE_LD;
import static saadadb.query.parser.SaadaQLRegex.WHERE_CLAUSE_RD;
import static saadadb.query.parser.SaadaQLRegex.WHERE_POSITION;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import saadadb.database.Database;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.query.constbuilders.NativeSQLConstraint;
import saadadb.query.constbuilders.PositionConstraint;
import saadadb.query.constbuilders.SaadaQLConstraint;
import saadadb.util.Messenger;

/**
 * @author F.X. Pineau
 * 01/2014: Region processing.
 */
public final class WherePosition{
	private static final String syntax = "WherePosition { isInBox | isInCircle| isInRegion ( \" coord \" , size_arcmin , J1950 | J2000 | -, FK4 | FK5 | ICRS | GALACTIC | ECLPITIC) [, ...] }";

	private static final String coord      = "[/\\s\\w\\-\\+\\.:,;]+";         // A AFFINER!!
	private static final String inSecKey   = "\"" + coord + "\"" + FacWS + ArgSep + FacWS +NumericValue + FacWS + ArgSep + FacWS +COORD_EQU + FacWS + ArgSep + FacWS +COORD_SYS;
	private static final String inSecKeyCapturingG = "\"("+coord+")\"" + FacWS + ArgSep + FacWS + "(" + NumericValue + ")" + FacWS + ArgSep + FacWS + "(" + COORD_EQU + ")" + FacWS + ArgSep + FacWS + "(" + COORD_SYS + ")";
	private static final String inStatSep = "(?:,|\\s)";
	private static final String inStatSimple = POS_QUERY_KEYS + FacWS + "\\(" + FacWS + inSecKey + FacWS + "\\)";
	private static final String inStatSimpleCapturingG =  "(" + POS_QUERY_KEYS + ")" + FacWS + "\\(" + FacWS + "(" + inSecKey + ")" + FacWS + "\\)" ;
	private static final String inStat = "(?:" + inStatSimple + FacWS + inStatSep + FacWS + ")*(?:" + inStatSimple + ")";

	private static final Pattern pattern = Pattern.compile(WHERE_POSITION + FacWS + WHERE_CLAUSE_LD + FacWS + "(" +  inStat + ")" + FacWS + WHERE_CLAUSE_RD);

	private String strMatch;
	private String theStatement;

	private PositionConstraint[] posConstraintTab;

	/**
	 * @param strQuery
	 * @throws SaadaException 
	 */
	public WherePosition(String strQuery) throws Exception{
		this.parse(strQuery);
	}

	public final String getStrMatch()    {return this.strMatch;}
	protected final String getTheStatement(){return this.theStatement;}

	/**
	 * @param strColl
	 * @return
	 * @throws SaadaException
	 */
	public final String getSqlConstraint(String strColl) throws Exception{
		String str = "";
		for(int i=0;i<posConstraintTab.length-1;i++){
			str += posConstraintTab[i].getSqlConstraint(strColl) + "\n      OR ";
		}
		str += posConstraintTab[posConstraintTab.length-1].getSqlConstraint(strColl);
		return str;
	}
	/**
	 * @return
	 * @throws SaadaException
	 */
	public final String getSqlConstraint() throws Exception{
		String str = "";
		for(int i=0;i<posConstraintTab.length-1;i++){
			str += posConstraintTab[i].getSqlConstraint() + "\n      OR ";
		}
		str += posConstraintTab[posConstraintTab.length-1].getSqlConstraint();
		return str;
	}

	public static final boolean isIn(String strQ){return pattern.matcher(strQ).find();}

	private final void parse(String strQ) throws Exception{
		Matcher m = pattern.matcher(strQ);
		if(m.find()){
			this.strMatch = new String(m.group(0));
			Pattern pIn = Pattern.compile(inStatSimpleCapturingG);
			Matcher mIn = pIn.matcher(m.group(1));
			ArrayList<PositionConstraint> al = new ArrayList<PositionConstraint>();//arraylist de wherePisInositionConstraint
			while(mIn.find()){
				Pattern pDet = Pattern.compile(inSecKeyCapturingG);
				Matcher mDet = pDet.matcher(mIn.group(2));
				if(mDet.find()){
					al.add(new PositionConstraint(mIn.group(1),mDet.group(1).trim(),mDet.group(2),mDet.group(3).toUpperCase(),mDet.group(4).toUpperCase()));
				}else{QueryException.throwNewException(SaadaException.SYNTAX_ERROR,"In WherePosition... Humm... I don't understand! This is an regex error not supposed to happen!");}
			}
			this.posConstraintTab = al.toArray(new PositionConstraint[0]);
		} else{
			QueryException.throwNewException(SaadaException.SYNTAX_ERROR,"Error parsing the WherePosition clause!\nCheck the syntax \""+syntax+"\"");
		}
	}

	/**
	 * @return
	 * @throws QueryException
	 * @throws SaadaException
	 */
	public SaadaQLConstraint getSaadaQLConstraint() throws QueryException, Exception {
		return new NativeSQLConstraint(this.getSqlConstraint(),new String[]{"pos_ra_csa", "pos_dec_csa"});
	}
	
	public static void main(String[] args) throws Exception {
		Database.init("ThreeXMM");
		Messenger.debug_mode = true;
		//WherePosition wp = new WherePosition("WherePosition {isInCircle(\"M33\", 0.001, -, ICRS) }");
		WherePosition wp = new WherePosition("WherePosition {isInRegion(\"202.45837,+47.29028, " +
				"202.52390,+47.28777, 202.55595,+47.19132, 202.53863,+47.13598, " +
				"202.43629,+47.15609, 202.38558,+47.22314, 202.41519,+47.23658, " +
				"202.44488,+47.18796, 202.46833,+47.18378, 202.49177,+47.19301, " +
				"202.49177,+47.21481, 202.46336,+47.22738, 202.45590,+47.28860, " +
				"\", 0 , -, ICRS) } "); 
					
		
		
		System.out.println(wp.getSqlConstraint().replaceAll("OR", "\nOR"));
		Database.close();
	}

}
