package saadadb.query.parser;

import static saadadb.query.parser.SaadaQLRegex.ConstraintSyntax;
import static saadadb.query.parser.SaadaQLRegex.FacWS;
import static saadadb.query.parser.SaadaQLRegex.OpLogSyntax;
import static saadadb.query.parser.SaadaQLRegex.SAADA_OPs;
import static saadadb.query.parser.SaadaQLRegex.UcdSyntaxCaptG;
import static saadadb.query.parser.SaadaQLRegex.UnitSyntaxCaptG;
import static saadadb.query.parser.SaadaQLRegex.ValueSyntax;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.query.constbuilders.UCDField;


/**
 * @author F.X. Pineau
 */
public abstract class ClauseUCD {
	private final String strMatch;
	private final ArrayList<UCDField> constTab = new ArrayList<UCDField>();
	
	public final String          getStrMatch()     { return this.strMatch;}
	public final ArrayList<UCDField> getSaadaQLConstraints(){ return this.constTab;}
	
	protected abstract Pattern getPattern();
	protected abstract String  getSyntax();
	protected abstract String  getClauseName();
	
	/**
	 * @param strQuery
	 * @throws SaadaException getTranslatedUcdConstraint
	 */
	protected ClauseUCD(String strQuery) throws SaadaException {
		strMatch = strQuery;
		this.parse(strQuery);
		check(this.constTab);
	}
	
	/**
	 * @param strQ
	 * @return
	 * @throws SaadaException 
	 */
	private final void parse(String strQ) throws SaadaException  {
		Matcher m = this.getPattern().matcher(strQ);
		if(m.find()){
			Matcher m2 = Pattern.compile("("+ConstraintSyntax+")(?:\\s+("+OpLogSyntax+"?)\\s+)?").matcher(m.group(1));
			while(m2.find()){
				Matcher m3 = Pattern.compile(UcdSyntaxCaptG + FacWS + "("+SAADA_OPs+"\\*?)" + FacWS + "("+ValueSyntax+")" + FacWS +UnitSyntaxCaptG).matcher(m2.group(1));
				if(m3.find()){
					String unit = (m3.group(4)==null || m3.group(4).trim().equals(""))?"none":m3.group(4);
					if( !UnitHandler.isValid(unit)) {
						QueryException.throwNewException(SaadaException.WRONG_PARAMETER,"In "+this.getClauseName()+": Unit \"" + unit + "\" not understood by the converter!");
					}
					if(m3.group(3).contains("'") && !m3.group(2).matches("!?=\\*?")){
						QueryException.throwNewException(SaadaException.WRONG_PARAMETER,"In "+this.getClauseName()+": Can't use a string value 'xxx' with operators different from \"=\" or \"!=\"");
					}
					if(m3.group(3).contains("(") && !m3.group(2).contains("[")){
						QueryException.throwNewException(SaadaException.WRONG_PARAMETER,"In "+this.getClauseName()+": Can't use 2 numeric values (v1,v2) with operators different from \"[]\",\"[=]\",\"][\" or \"]=[\"");
					}
					this.constTab.add(new UCDField(m3.group(1),m3.group(2),m3.group(3),unit));
				}else{QueryException.throwNewException(SaadaException.WRONG_PARAMETER,"In "+this.getClauseName()+"... Humm... I don't understand! This is an regex error not supposed to happen!");}
			}
		}else{
			QueryException.throwNewException(SaadaException.SYNTAX_ERROR,"Error parsing the "+this.getClauseName()+" clause!\nCheck the syntax \""+this.getSyntax()+"\"");
		}
	}
	
	/**
	 * @param constTab2
	 * @throws QueryException
	 */
	private static final void check(ArrayList<UCDField> constTab2) throws QueryException {
		for(int i=0;i<constTab2.size()-1;i++)
			for(int j=i+1;j<constTab2.size();j++)
				if(constTab2.get(i).getMetacolname().equals(constTab2.get(j).getMetacolname()))
					QueryException.throwNewException(SaadaException.SYNTAX_ERROR,"Error on UCD \""+constTab2.get(i).getMetacolname()+"\": You can put only one constraint on a same UCD in a the same clause!");
	}

	/**
	 * @return
	 */
	public AttributeHandler[] getUCDColumns() {
		AttributeHandler[] retour = new AttributeHandler[constTab.size()];
		for( int i=0 ; i<constTab.size() ; i++ ) {
			retour[i] = constTab.get(i).getConstraintHandler();
		}
		return retour;
	}
}
