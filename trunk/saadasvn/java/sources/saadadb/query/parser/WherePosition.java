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

import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.query.constbuilders.NativeSQLConstraint;
import saadadb.query.constbuilders.PositionConstraint;
import saadadb.query.constbuilders.SaadaQLConstraint;

/**
 * @author F.X. Pineau
 */
public final class WherePosition{
	private static final String syntax = "WherePosition { isInBox | isInCircle ( \" coord \" , size_arcmin , J1950 | J2000 | -, FK4 | FK5 | ICRS | GALACTIC | ECLPITIC) [, ...] }";

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
	public WherePosition(String strQuery) throws SaadaException{
		this.parse(strQuery);
	}

	public final String getStrMatch()    {return this.strMatch;}
	protected final String getTheStatement(){return this.theStatement;}

	/**
	 * @param strColl
	 * @return
	 * @throws SaadaException
	 */
	public final String getSqlConstraint(String strColl) throws SaadaException{
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
	public final String getSqlConstraint() throws SaadaException{
		String str = "";
		for(int i=0;i<posConstraintTab.length-1;i++){
			str += posConstraintTab[i].getSqlConstraint() + "\n      OR ";
		}
		str += posConstraintTab[posConstraintTab.length-1].getSqlConstraint();
		return str;
	}

	public static final boolean isIn(String strQ){return pattern.matcher(strQ).find();}

	private final void parse(String strQ) throws SaadaException{
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
		}
		else{
			QueryException.throwNewException(SaadaException.SYNTAX_ERROR,"Error parsing the WherePosition clause!\nCheck the syntax \""+syntax+"\"");
		}
	}

	/**
	 * @return
	 * @throws QueryException
	 * @throws SaadaException
	 */
	public SaadaQLConstraint getSaadaQLConstraint() throws QueryException, SaadaException {
		return new NativeSQLConstraint(this.getSqlConstraint(),new String[]{"pos_ra_csa", "pos_dec_csa"});
	}

}
