package saadadb.query.matchpattern;

import static saadadb.query.parser.SaadaQLRegex.ASS_CLAUSE_LD;
import static saadadb.query.parser.SaadaQLRegex.ASS_CLAUSE_RD;
import static saadadb.query.parser.SaadaQLRegex.ASS_OBJ_ATT_SAADA;
import static saadadb.query.parser.SaadaQLRegex.FacWS;

import java.util.regex.Pattern;

import saadadb.exceptions.QueryException;
import saadadb.query.parser.ClauseSQL;


/**
 * @author F.X. Pineau
 */
public final class AssObjAttSaada extends ClauseSQL {
	private static final String syntax = ASS_OBJ_ATT_SAADA+" { SqlLikeStatement }"; 
	private static final String inStat  = "[^\\{\\}]+";  // Inner Statement
	private static final Pattern pattern = Pattern.compile(ASS_OBJ_ATT_SAADA + FacWS + ASS_CLAUSE_LD + FacWS +"("+ inStat +")"+ FacWS + ASS_CLAUSE_RD);

	public AssObjAttSaada(String strQuery) throws QueryException {
		super(strQuery);
	}
	
	public static boolean isIn(String strQ){return pattern.matcher(strQ).find();}
	@Override
	protected final Pattern getPattern()  {return pattern;}
	@Override
	protected final String getClauseName(){return ASS_OBJ_ATT_SAADA;}
	@Override
	protected final String getSyntax()    {return syntax;}
}
