package saadadb.query.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.query.constbuilders.NativeSQLConstraint;
import saadadb.query.constbuilders.SaadaQLConstraint;

/**
 * @author F.X. Pineau
 */
public abstract class ClauseSQL {
	private final String   strMatch     ;
	private final String   theStatement ;
	private final String[] attributeList;
	
	public final String   getStrMatch     (){return this.strMatch     ;}
	protected final String   getTheStatement (){return this.theStatement ;}
	public final String[] getAttributeList(){return this.attributeList;}
	
	protected abstract Pattern getPattern();
	protected abstract String  getSyntax();
	protected abstract String  getClauseName();
	
	/**
	 * @param strQuery
	 * @throws QueryException
	 */
	protected ClauseSQL(String strQuery) throws QueryException {
		String[] sT = this.parse(strQuery);
		this.strMatch = sT[0];
		this.theStatement = sT[1];
		this.attributeList = rmDoublons(SqlParser.parseAttributes(this.theStatement));		
	}
	
	/**
	 * @param strQ
	 * @return
	 * @throws QueryException
	 */
	protected final String[] parse(String strQ) throws QueryException {
		Matcher m = this.getPattern().matcher(strQ);
		if(m.find()){
			return new String[]{m.group(0),m.group(1)};
		}else{
			QueryException.throwNewException(SaadaException.SYNTAX_ERROR,"Error parsing the clause "+this.getClauseName()+"!\nCheck the syntax \""+this.getSyntax()+"\"");
			return null;
		}
	}
	
	/**
	 * Remove doublons from a list of String
	 * @param sTab
	 * @return a list of String without doublons
	 */
	private static final String[] rmDoublons(String[] sTab){
		int rm=0;
		for(int i=0;i<sTab.length;i++) {
			/*
			 * oidsaada is always queried: it must be discarded from constrained attribute to avoid column duplication 
			 */
			if( sTab[i].equals("oidsaada") ) {
				sTab[i]=null;
				rm++;
			}
			else {
				for(int j=i+1;j<sTab.length;j++) {
					if(sTab[i].equals(sTab[j])){
						sTab[j]=null;
						rm++;
					}
				}
			}
		}
		if(rm==0) return sTab;
		String[] new_sTab = new String[sTab.length-rm];
		int pos=-1;for(String s:sTab)if(s!=null)new_sTab[++pos]=s;
		return new_sTab;
	} 
	
	/**
	 * Returns the constraint in an input object for the merger.
	 * @return
	 * @throws QueryException
	 */
	public SaadaQLConstraint getSaadaQLConstraint() throws QueryException {
		return new NativeSQLConstraint(this.theStatement, attributeList);
	}
}
