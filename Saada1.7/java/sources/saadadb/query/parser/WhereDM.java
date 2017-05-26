package saadadb.query.parser;

import static saadadb.query.parser.SaadaQLRegex.FacWS;
import static saadadb.query.parser.SaadaQLRegex.InTYPEStat;
import static saadadb.query.parser.SaadaQLRegex.WHERE_CLAUSE_LD;
import static saadadb.query.parser.SaadaQLRegex.WHERE_CLAUSE_RD;
import static saadadb.query.parser.SaadaQLRegex.WHERE_DM;

import java.util.regex.Pattern;

import saadadb.exceptions.SaadaException;
import saadadb.meta.VOResource;

public class WhereDM extends ClauseDM {
	private static final String syntax = "WhereDM { \\[ucd\\] op 'text'|number|(number,number) [\\[unit\\]] [ and|or ...] }";
	private static final String regex  = WHERE_DM + FacWS + WHERE_CLAUSE_LD + FacWS + "(" + InTYPEStat+ ")" + FacWS + WHERE_CLAUSE_RD;
	private static final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);

	public WhereDM(String strQuery, VOResource vor) throws SaadaException {
		super(strQuery, vor);
	}
	public static final boolean isIn(String strQ){return pattern.matcher(strQ).find();}
	@Override
	protected final String getClauseName() { return WHERE_DM; }
	@Override
	protected final Pattern getPattern()   { return pattern;}
	@Override
	protected final String getSyntax()     { return syntax;}

}
