package saadadb.query.constbuilders;

import static saadadb.query.parser.SaadaQLRegex.COLL;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.VOResource;
import saadadb.query.matchpattern.AssDM;
import saadadb.query.matchpattern.AssObjAttClass;
import saadadb.query.matchpattern.AssObjAttSaada;
import saadadb.query.matchpattern.AssObjClass;
import saadadb.query.matchpattern.AssUCD;
import saadadb.query.matchpattern.AssUType;
import saadadb.query.matchpattern.Cardinality;
import saadadb.query.matchpattern.Qualifier;

/**
 * @author F.X. Pineau
 *  * @version $Id$

 * Syntaxe: matchPattern{ "relation"
 *	 1 Cardinality("<=",1,0)
 *	 n Qualifier("pixel","][",1,3)
 *	 1 AssObjAttSaada( "SQL" )
 *	 1 AssObjClass(cl1,cl2)
 *	 1 AssObjAttClass("SQL")
 *	 1 AssUCD()
 *   1 AssUType()
 */
public final class MatchPattern{
	private final String inPattern;
	private String         relation  ;
	private Cardinality    card      ;
	private Qualifier[]    qualTab   ;
	private AssObjAttSaada aoasClause;
	private AssObjClass    aocClause ;
	private AssObjAttClass aoacClause;
	private AssUCD         auClause  ;
	private AssUType       autClause ;
	private AssDM          dmClause ;
	private VOResource     vor;

	public MatchPattern(String str, VOResource vor)throws SaadaException {
		this.inPattern = str;
		this.vor = vor;
		this.parse();
		if(this.getAoacClause()!=null && (this.getAocClause()==null || this.getAocClause().getlistClass().length>1 || this.getAocClause().getlistClass()[0].equals("*")))
			QueryException.throwNewException(SaadaException.SYNTAX_ERROR, "In matchPattern, if there is a clause \"AssObjAttClass\", the class conserned, and only that one, must be precise in a \"AssObjClass\" clause");
	}

	public final String         getRelation  (){return this.relation  ;}
	public final Cardinality    getCard      (){return this.card      ;}
	public final Qualifier[]    getQualTab   (){return this.qualTab   ;}
	public final AssObjAttSaada getAoasClause(){return this.aoasClause;}
	public final AssObjClass    getAocClause (){return this.aocClause ;}
	public final AssObjAttClass getAoacClause(){return this.aoacClause;}
	public final AssUCD         getAuClause  (){return this.auClause  ;}
	public final AssUType       getAutClause (){return this.autClause ;}
	public final AssDM          getDmClause  (){return this.dmClause ;}

	public final void parse()throws SaadaException {
		String strQtemp = this.inPattern;
		Pattern p = Pattern.compile("^\\s*"+COLL);
		Matcher m = p.matcher(strQtemp);
		if(m.find()){
			strQtemp=strQtemp.replace(m.group(0),"");
			this.relation = m.group(0).replaceAll("\"","").trim();
			if(Cardinality.isIn(strQtemp)){
				this.card = new Cardinality(strQtemp);
				strQtemp=strQtemp.replace(this.card.getStrMatch(),"");	
			}
			List<Qualifier> al = new ArrayList<Qualifier>();
			while(Qualifier.isIn(strQtemp)){
				Qualifier qua = new Qualifier(strQtemp);
				al.add(qua);
				strQtemp=strQtemp.replace(qua.getStrMatch(),"");
			}
			if(al.size()>0){
				this.qualTab = al.toArray(new Qualifier[0]);
			}
			if(AssObjAttSaada.isIn(strQtemp)){
				this.aoasClause = new AssObjAttSaada(strQtemp);
				strQtemp=strQtemp.replace(this.aoasClause.getStrMatch(),"");
			}
			if(AssObjClass.isIn(strQtemp)){
				this.aocClause = new AssObjClass(strQtemp);
				strQtemp=strQtemp.replace(this.aocClause.getstrMatch(),"");
			}
			if(AssObjAttClass.isIn(strQtemp)){
				this.aoacClause = new AssObjAttClass(strQtemp);
				strQtemp=strQtemp.replace(this.aoacClause.getStrMatch(),"");
			}
			if(AssUCD.isIn(strQtemp)){
				this.auClause = new AssUCD(strQtemp);
				strQtemp=strQtemp.replace(this.auClause.getStrMatch(),"");
			}
			if(AssUType.isIn(strQtemp)){
				if(this.auClause!=null) QueryException.throwNewException(SaadaException.UNSUPPORTED_OPERATION, "AssUCD and AssUType in the same MatchPattern not yet implemented!");
				this.autClause = new AssUType(strQtemp, this.vor);
				strQtemp=strQtemp.replace(this.autClause.getStrMatch(),"");
			}
			// Check Errors
			if(!strQtemp.replaceAll(",","").trim().equals("")) {
				QueryException.throwNewException(SaadaException.SYNTAX_ERROR, "No all the matchPattern has been parsed!! There is still: \""+strQtemp.replaceAll(",","").trim()+"\". Check it in \""+this.inPattern+"\" to find the error");
			}
		}
		else{
			QueryException.throwNewException(SaadaException.SYNTAX_ERROR, "Relation not found in the matchPattern inside: \"" + inPattern + "\"!");
		}
	}		
}
 
