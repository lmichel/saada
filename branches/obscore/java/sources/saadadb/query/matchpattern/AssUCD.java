package saadadb.query.matchpattern;

import static saadadb.query.parser.SaadaQLRegex.ASS_CLAUSE_LD;
import static saadadb.query.parser.SaadaQLRegex.ASS_CLAUSE_RD;
import static saadadb.query.parser.SaadaQLRegex.ASS_UCD;
import static saadadb.query.parser.SaadaQLRegex.FacWS;
import static saadadb.query.parser.SaadaQLRegex.InUCDStat;

import java.util.regex.Pattern;

import saadadb.exceptions.SaadaException;
import saadadb.query.parser.ClauseUCD;

/** * @version $Id: AssUCD.java 118 2012-01-06 14:33:51Z laurent.mistahl $

 * @author F.X. Pineau
 */
public final class AssUCD extends ClauseUCD{
	private static final String syntax = "AssUCD { \\[ucd\\] op 'text'|number|(number,number) [\\[unit\\]] [ and|or ...] }";
	private static final String regex  = ASS_UCD + FacWS + ASS_CLAUSE_LD + FacWS + "(" + InUCDStat+ ")" + FacWS + ASS_CLAUSE_RD;
	private static final Pattern pattern = Pattern.compile(regex);

	public AssUCD(String strQuery) throws SaadaException {
		super(strQuery);
	}
	public static final boolean isIn(String strQ){return pattern.matcher(strQ).find();}
	@Override
	protected final String  getClauseName() { return ASS_UCD; }
	@Override
	protected final Pattern getPattern()   { return pattern;}
	@Override
	protected final String  getSyntax()     { return syntax;}
}