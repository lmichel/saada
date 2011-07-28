package saadadb.query.parser;

import static saadadb.query.parser.SaadaQLRegex.FacWS;
import static saadadb.query.parser.SaadaQLRegex.WHERE_ATTR_SAADA;
import static saadadb.query.parser.SaadaQLRegex.WHERE_CLAUSE_LD;
import static saadadb.query.parser.SaadaQLRegex.WHERE_CLAUSE_RD;

import java.util.regex.Pattern;

import saadadb.exceptions.QueryException;

/**
 * @author F.X. Pineau
 */
public final class WhereAttributeSaada extends ClauseSQL {
	private static final String syntax = WHERE_ATTR_SAADA+" { SqlLikeStatement }"; 
	private static final String inStat  = "[^\\{\\}]+";  // Inner Statement
	private static final Pattern pattern = Pattern.compile(WHERE_ATTR_SAADA + FacWS + WHERE_CLAUSE_LD + FacWS +"("+ inStat +")"+ FacWS + WHERE_CLAUSE_RD);
	
	public WhereAttributeSaada(String strQuery) throws QueryException{
		super(strQuery);
	}
	
	public static boolean isIn(String strQ){return pattern.matcher(strQ).find();}
	
	@Override
	protected final Pattern getPattern()  {return pattern;}
	@Override
	protected final String getClauseName(){return WHERE_ATTR_SAADA;}
	@Override
	protected final String getSyntax()    {return syntax;}
}
