package saadadb.query.parser;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.query.constbuilders.OrderByConstraint;
import saadadb.query.constbuilders.SaadaQLConstraint;

/**
* This class parse the SaadaQL clause:
* ORDER BY attr [ ASC | DESC ]
* 
* It stores the attribut, the option and the kind of attribute (of class or of collection) 
* 
* @author F.X. Pineau
*/
public final class OrderBy{
	private static final String syntax = "Order By attr [ asc | desc ]";

	public static final int ON_CLASS = 1;
	public static final int ON_COLL  = 2;
	public static final int BOTH     = 3;//for oidsaada, md5keysaada, ..;
	
    public  static final String[] specialKey = {"oidsaada","obs_id"};
	private static final String regex = SaadaQLRegex.ORDER_BY + SaadaQLRegex.ReqWS + "(" + SaadaQLRegex.ATTRIBUTE + ")(" + SaadaQLRegex.ORDER_BY_OPTION + ")";//"(\\s+"+option+")?" ;
	private static final Pattern pattern = Pattern.compile(regex);
	
	private String strMatch;
	private String attr;
	
	private String theStatement; //= constraint
	private boolean isDesc = false;
	private SelectFromIn sfi;
	private int  type = -1;
	
	public OrderBy(String strQuery, SelectFromIn sfi) throws QueryException {
		this.parse(strQuery);
		this.sfi = sfi;
	}
	
	/**
	 * Default order by (ordr by oidsaada desc)
	 * @param sfi
	 * @throws QueryException
	 */
	public OrderBy( SelectFromIn sfi) throws QueryException {
		this.strMatch = "Order By oidsaada desc";
		this.theStatement = "oidsaada";
		this.attr = "oidsaada";
		this.sfi = sfi;
		this.isDesc = true;
	}

	public static final String   getSyntax (){return syntax       ;}
	public final String getStrMatch     (){return this.strMatch    ;}
	public final String getTheStatement (){return this.theStatement;}
	public final int    getType         (){return (this.haveSpecialKey())?BOTH:this.type;}
    protected final String getOrder        (){return (isDesc)?"DESC":"";}
	
    public static final boolean isIn(String strQ){return pattern.matcher(strQ).find();}
	
	/**
	 * @param strQ
	 * @throws QueryException
	 */
	private final void parse(String strQ) throws QueryException  {
		Matcher m = pattern.matcher(strQ);
		if(m.find()){
			this.strMatch = m.group(0);
			this.theStatement = m.group(1);
			this.attr = m.group(1);
			if(m.group(2)!=null && m.group(2).matches("\\s+(?i)(desc)")){
				this.isDesc = true;
			}
		}
		else{
			String str_e = "The String "+strQ+" doesn't have a part matching the ORDER BY regex!\nCheck the syntax \""+syntax+"\".\n";
			QueryException.throwNewException(SaadaException.SYNTAX_ERROR, "SaadaQL Syntax ERROR: " + str_e);
		}
	}
	
	/**
	 * @return
	 */
	public final boolean haveSpecialKey(){
		for(String spK:specialKey){
			if(this.theStatement.equals(spK)){return true;}
		}
		return false;
	}

	/**
	 * @param val
	 * @throws QueryException
	 */
	public final void setType(int val) throws QueryException {
			this.type = typeCode(val);
	}
	/**
	 * @param val
	 * @return
	 * @throws QueryException
	 */
	private static final int typeCode(int val) throws QueryException {
		switch(val){
		case ON_CLASS:
			return ON_CLASS;
		case ON_COLL:
			return ON_COLL;
		case BOTH:
			return BOTH;
		}
		QueryException.throwNewException(SaadaException.UNSUPPORTED_OPERATION, "Order By type code \""+val+"\" unknown!");
		return 0;
	}
	
	/**
	 * @return
	 * @throws QueryException
	 * @throws SaadaException
	 */
	public SaadaQLConstraint getSaadaQLConstraint() throws QueryException, SaadaException {
		return new OrderByConstraint(new String[]{this.attr}, this.isDesc, this.sfi);
	}
	
	public  String getSQL(String alias){
		return this.strMatch;
	}
	public  String getAttr(){
		return this.attr;
	}


}
