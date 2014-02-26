package saadadb.query.matchpattern;

import static saadadb.query.parser.SaadaQLRegex.ASS_CLAUSE_LD;
import static saadadb.query.parser.SaadaQLRegex.ASS_CLAUSE_RD;
import static saadadb.query.parser.SaadaQLRegex.ASS_OBJ_ATT_CLASS;
import static saadadb.query.parser.SaadaQLRegex.FacWS;

import java.util.regex.Pattern;

import saadadb.exceptions.QueryException;
import saadadb.query.parser.ClauseSQL;

/** * @version $Id$

 * @author F.X. Pineau
 */
public final class AssObjAttClass extends ClauseSQL {
	private static final String syntax = ASS_OBJ_ATT_CLASS+" { SqlLikeStatement }"; 
	private static final String inStat  = "[^\\{\\}]+";  // Inner Statement
	private static final Pattern pattern = Pattern.compile(ASS_OBJ_ATT_CLASS + FacWS + ASS_CLAUSE_LD + FacWS +"("+ inStat +")"+ FacWS + ASS_CLAUSE_RD);

	public AssObjAttClass(String strQuery) throws QueryException {
		super(strQuery);
	}
	
	public static boolean isIn(String strQ){return pattern.matcher(strQ).find();}
	@Override
	protected final Pattern getPattern()  {return pattern;}
	@Override
	protected final String getClauseName(){return ASS_OBJ_ATT_CLASS;}
	@Override
	protected final String getSyntax()    {return syntax;}
}