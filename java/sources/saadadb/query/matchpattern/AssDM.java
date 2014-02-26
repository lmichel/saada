package saadadb.query.matchpattern;

import static saadadb.query.parser.SaadaQLRegex.ASS_CLAUSE_LD;
import static saadadb.query.parser.SaadaQLRegex.ASS_CLAUSE_RD;
import static saadadb.query.parser.SaadaQLRegex.ASS_DM;
import static saadadb.query.parser.SaadaQLRegex.FacWS;
import static saadadb.query.parser.SaadaQLRegex.InUCDStat;

import java.util.regex.Pattern;

import saadadb.exceptions.SaadaException;
import saadadb.meta.VOResource;
import saadadb.query.parser.ClauseDM;
/**
 * @author laurentmichel
 * * @version $Id: AssDM.java 118 2012-01-06 14:33:51Z laurent.mistahl $

 */
public class AssDM extends ClauseDM {
	
	private static final String syntax = "AssDM { \\[ucd\\] op 'text'|number|(number,number) [\\[unit\\]] [ and|or ...] }";
	private static final String regex  = ASS_DM + FacWS + ASS_CLAUSE_LD + FacWS + "(" + InUCDStat+ ")" + FacWS + ASS_CLAUSE_RD;
	private static final Pattern pattern = Pattern.compile(regex);

	protected AssDM(String strQuery, VOResource vor) throws SaadaException {
		super(strQuery,vor);
	}
	protected static final boolean isIn(String strQ){return pattern.matcher(strQ).find();}
	@Override
	protected final String  getClauseName() { return ASS_DM; }
	@Override
	protected final Pattern getPattern()   { return pattern;}
	@Override
	protected final String  getSyntax()     { return syntax;}

}
