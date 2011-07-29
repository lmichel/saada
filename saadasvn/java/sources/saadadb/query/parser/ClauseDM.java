package saadadb.query.parser;

import static saadadb.query.parser.SaadaQLRegex.FacWS;
import static saadadb.query.parser.SaadaQLRegex.OpLogSyntax;
import static saadadb.query.parser.SaadaQLRegex.SAADA_OPs;
import static saadadb.query.parser.SaadaQLRegex.UTypeSyntaxCaptG;
import static saadadb.query.parser.SaadaQLRegex.UnitSyntaxCaptG;
import static saadadb.query.parser.SaadaQLRegex.UtypeConstraintSyntax;
import static saadadb.query.parser.SaadaQLRegex.ValueSyntax;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.VOResource;
import saadadb.query.constbuilders.DMField;


/** * @version $Id$

 * @author F.X. Pineau
 */
public abstract class ClauseDM {
	private final String strMatch;
	private  ArrayList<DMField> constTab = new ArrayList<DMField>();
	protected VOResource vor = null;
	public final String            getStrMatch()     { return this.strMatch;}
	public final ArrayList<DMField> getSaadaQLConstraints(){ return this.constTab;}

	protected abstract Pattern getPattern();
	protected abstract String  getSyntax();
	protected abstract String  getClauseName();

	/**
	 * @param strQuery
	 * @throws SaadaException 
	 */
	protected ClauseDM(String strQuery, VOResource vor) throws SaadaException {
		this.vor = vor;
		StrUTypeConst suc = this.parse(strQuery);
		this.strMatch = suc.s;
		this.constTab = suc.utypec;
		check(this.constTab);
	}

	/**
	 * @param strQ
	 * @return
	 * @throws SaadaException 
	 */
	private final StrUTypeConst parse(String strQ) throws SaadaException  {
		ArrayList<DMField> alC = new ArrayList<DMField>();
		Matcher m = this.getPattern().matcher(strQ);
		if(m.find()){
			Matcher m2 = Pattern.compile("("+UtypeConstraintSyntax+")(?:\\s+("+OpLogSyntax+"?)\\s+)?").matcher(m.group(1));
			while(m2.find()){
				Matcher m3 = Pattern.compile(UTypeSyntaxCaptG + FacWS + "("+SAADA_OPs+"\\*?)" + FacWS + "("+ValueSyntax+")" + FacWS +UnitSyntaxCaptG).matcher(m2.group(1));
				if(m3.find()){
					String unit = (m3.group(4)==null || m3.group(4).trim().equals(""))?"none":m3.group(4);
//					if( !UCDsUnitManager.isValid(unit)) {
//						QueryException.throwNewException(SaadaException.WRONG_PARAMETER,"In "+this.getClauseName()+": Unit \"" + unit + "\" not understood by the converter!");
//					}
					if(m3.group(3).contains("'") && !m3.group(2).matches("!?=\\*?")){
						QueryException.throwNewException(SaadaException.WRONG_PARAMETER,"In "+this.getClauseName()+": Can't use a string value 'xxx' with operators different from \"=\" or \"!=\"");
					}
					if(m3.group(3).contains("(") && !m3.group(2).contains("[")){
						QueryException.throwNewException(SaadaException.WRONG_PARAMETER,"In "+this.getClauseName()+": Can't use 2 numeric values (v1,v2) with operators different from \"[]\",\"[=]\",\"][\" or \"]=[\"");
					}
					alC.add(new DMField(vor, m3.group(1),m3.group(2),m3.group(3),unit));
				}else{QueryException.throwNewException(SaadaException.SYNTAX_ERROR,"In "+this.getClauseName()+"... Humm... I don't understand! This is an regex error not supposed to happen!");}
			}
		}else{
			QueryException.throwNewException(SaadaException.SYNTAX_ERROR,"Error parsing the "+this.getClauseName()+" clause!\nCheck the syntax \""+this.getSyntax()+"\"");
		}
		return new StrUTypeConst(m.group(0),alC);
	}

	/**
	 * @param uTypec
	 * @throws QueryException
	 */
	private static final void check(ArrayList<DMField> uTypec) throws QueryException {
		for(int i=0;i<uTypec.size()-1;i++)
			for(int j=i+1;j<uTypec.size();j++)
				if(uTypec.get(i).getMetacolname().equals(uTypec.get(j).getMetacolname()))
					QueryException.throwNewException(SaadaException.SYNTAX_ERROR,"Error on UType \""+uTypec.get(i).getMetacolname()+"\": You can put only one constraint on a same UType in a the same clause!");
	}

	/**
	 * @author michel
	 *
	 */
	private static final class StrUTypeConst{
		private final String             s;
		private final ArrayList<DMField> utypec;
		private StrUTypeConst(String s,ArrayList<DMField> utypec){
			this.s=s;
			this.utypec=utypec;
		}
	}

}
