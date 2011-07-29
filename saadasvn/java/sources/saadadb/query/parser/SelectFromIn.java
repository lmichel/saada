package saadadb.query.parser;
//package saada.query;

import static saadadb.collection.Category.NAMES;
import static saadadb.query.parser.SaadaQLRegex.ArgSep;
import static saadadb.query.parser.SaadaQLRegex.CATEGORIES;
import static saadadb.query.parser.SaadaQLRegex.CLASS;
import static saadadb.query.parser.SaadaQLRegex.COLL;
import static saadadb.query.parser.SaadaQLRegex.FROM;
import static saadadb.query.parser.SaadaQLRegex.FacWS;
import static saadadb.query.parser.SaadaQLRegex.IN;
import static saadadb.query.parser.SaadaQLRegex.ReqWS;
import static saadadb.query.parser.SaadaQLRegex.SELECT;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import saadadb.collection.Category;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;

/** * @version $Id$

 * This class parse the SaadaQL clause:
 * SELECT category FROM * | class1 [, ...] IN * | coll [, ...]
 * 
 * It stores the category, a list of class and a list of collection 
 * 
 * @author F.X. Pineau
 */
public class SelectFromIn{
	public static final int MULT_COL           = 0;
	public static final int ONE_COL_ONE_CLASS  = 1;
	public static final int ONE_COL_MULT_CLASS = 2;
	public static final int ONE_COL_ANY_CLASS = 3;
	
	private static final String syntax = "SELECT category FROM * | class1 [, ...] IN * | coll [, ...]";
	//Class that parse the clause: 
	private static final String ListClas = "(?:(?:(?:" + CLASS + FacWS + ArgSep + FacWS + ")*(?:" + CLASS + "))|ALL)";
	private static final String ListColl = "(?:(?:(?:" + COLL  + FacWS + ArgSep + FacWS + ")*(?:" + COLL + "))|ALL)" ;
	private static final String regex = SELECT + ReqWS + "(" + CATEGORIES + ")" +  ReqWS 
								+ FROM + ReqWS + "(" + ListClas + ")" + ReqWS 
								+ IN + ReqWS + "(" + ListColl + ")" + "(?:\\s|$)";
	private static final Pattern pattern = Pattern.compile(regex);
	
	private final String strMatch;

	private final int catego ;
	private final String[] listClass;
	private final String[] listColl ;
	private final int mode;
	
	public SelectFromIn(String strQuery) throws SaadaException{
		String[][] sM  = parse(strQuery);
		this.strMatch  = sM[0][0];
		this.catego    = Category.getCategory(sM[0][1]);
		this.listClass = sM[1];
		this.listColl  = sM[2];
		Arrays.toString(listColl);
		if((this.listColl.length>1 || this.listColl[0].equals("*")) && (this.listClass.length>1 || !this.listClass[0].equals("*")))
			QueryException.throwNewException(SaadaException.SYNTAX_ERROR,"If several collections, list of classes must be \"*\"!");
		if(this.listColl.length>1 || this.listColl[0].equals("*")) this.mode = MULT_COL;
		else if(this.listClass.length>1 ) this.mode = ONE_COL_MULT_CLASS;
		else if(this.listClass[0].equals("*")) this.mode = ONE_COL_ANY_CLASS;
		else this.mode = ONE_COL_ONE_CLASS;
		this.checkClassAndColl() ;
	}

	public static final String    getSyntax (){return syntax   ;}
	public final String   getStrMatch (){return this.strMatch  ;}
	public final int      getCatego   (){return this.catego    ;}
	public final String[] getListClass(){return this.listClass ;}
	public final String[] getListColl (){return this.listColl  ;}
	public final int      getMode     (){return this.mode      ;}

	protected static final boolean isIn(String strQ){return pattern.matcher(strQ).find();}
	
	/**
	 * Check that classes and collections exist
	 * @throws FatalException
	 */
	private final void checkClassAndColl() throws FatalException {
		for( String coll: this.listColl) {
			if( coll.equals("*")) {
				return ;
			}
			else {
				for( String classe: this.listClass) {
					if( classe.equals("*")) {
						return ;
					}
					Database.getCachemeta().getClassesOfCollection(coll, catego);
				}
				
			}
			Database.getCachemeta().getCollection(coll);
		}
	}
	/**
	 * @param strQ
	 * @return
	 * @throws QueryException
	 */
	private static final String[][] parse(String strQ) throws QueryException{
		Matcher m = pattern.matcher(strQ);
		if(m.find()){
			return new String[][]{new String[]{m.group(0),m.group(1)},m.group(2).split(FacWS+ArgSep+FacWS),m.group(3).split(FacWS+ArgSep+FacWS)};
		}else{
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER,"Error while parsing the SelectFromIn clause!\n"
					+  "Check the syntax \""+syntax+"\".\n"
					+  "WARNING: category is case sensitive, its possible values are:\n"
					+  Arrays.toString(NAMES));
		}
		return null;
	}
	
	/**
	 * Return a From/In clause compatible with the VO
	 * @return
	 */
	public final String getVOResourceName() { 
		String retour = "[";
		if( this.listColl.length == 1 ) {
			retour = "[" + this.listColl[0];
			if( this.listClass.length > 0 ) {
				retour += "(";
				for( int i=0 ; i<this.listClass.length ; i++ ) {
					if( i > 0 ){
						retour += ",";
					}
					retour += this.listClass[i];
				}
				retour += ")";
			}
		}
		else {
			for( int i=0 ; i<this.listColl.length ; i++ ) {
				if( i > 0 ){
					retour += ",";
				}
				retour += this.listColl[i];
			}
		}
		retour += "]";
		return retour;			    	
	}

} 
