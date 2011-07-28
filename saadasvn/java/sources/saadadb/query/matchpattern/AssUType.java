package saadadb.query.matchpattern;

import static saadadb.query.parser.SaadaQLRegex.ASS_CLAUSE_LD;
import static saadadb.query.parser.SaadaQLRegex.ASS_CLAUSE_RD;
import static saadadb.query.parser.SaadaQLRegex.ASS_UType;
import static saadadb.query.parser.SaadaQLRegex.FacWS;
import static saadadb.query.parser.SaadaQLRegex.InUCDStat;

import java.util.regex.Pattern;

import saadadb.exceptions.SaadaException;
import saadadb.meta.VOResource;
import saadadb.query.parser.ClauseDM;

public class AssUType extends ClauseDM{
	private static final String syntax = "AssUType { \\[ucd\\] op 'text'|number|(number,number) [\\[unit\\]] [ and|or ...] }";
	private static final String regex  = ASS_UType + FacWS + ASS_CLAUSE_LD + FacWS + "(" + InUCDStat+ ")" + FacWS + ASS_CLAUSE_RD;
	private static final Pattern pattern = Pattern.compile(regex);

	public AssUType(String strQuery, VOResource vor) throws SaadaException {
		super(strQuery, vor);
	}
	public static final boolean isIn(String strQ){return pattern.matcher(strQ).find();}
	@Override
	protected final String  getClauseName() { return ASS_UType; }
	@Override
	protected final Pattern getPattern()   { return pattern;}
	@Override
	protected final String  getSyntax()     { return syntax;}
}
