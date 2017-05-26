package saadadb.query.parser;

import static saadadb.query.parser.SaadaQLRegex.FacWS;
import static saadadb.query.parser.SaadaQLRegex.InUCDStat;
import static saadadb.query.parser.SaadaQLRegex.WHERE_CLAUSE_LD;
import static saadadb.query.parser.SaadaQLRegex.WHERE_CLAUSE_RD;
import static saadadb.query.parser.SaadaQLRegex.WHERE_UCD;

import java.util.regex.Pattern;

import saadadb.exceptions.SaadaException;


/**
 * @author F.X. Pineau
 */
public final class WhereUCD extends ClauseUCD{
	private static final String syntax = "WhereUCD { \\[ucd\\] op 'text'|number|(number,number) [\\[unit\\]] [ and|or ...] }";
	private static final String regex  = WHERE_UCD + FacWS + WHERE_CLAUSE_LD + FacWS + "(" + InUCDStat+ ")" + FacWS + WHERE_CLAUSE_RD;
	private static final Pattern pattern = Pattern.compile(regex);

	public WhereUCD(String strQuery) throws SaadaException {
		super(strQuery);
	}
	public static final boolean isIn(String strQ){return pattern.matcher(strQ).find();}
	@Override
	protected final String getClauseName() { return WHERE_UCD; }
	@Override
	protected final Pattern getPattern()   { return pattern;}
	@Override
	protected final String getSyntax()     { return syntax;}
}
