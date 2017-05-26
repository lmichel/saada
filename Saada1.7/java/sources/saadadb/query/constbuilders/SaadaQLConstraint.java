package saadadb.query.constbuilders;



import java.util.ArrayList;

import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.meta.VOResource;

public abstract class SaadaQLConstraint {
	final static public int NATIVE=0;
	final static public int POSITION=1;
	final static public int DM_MAPPED=2;
	final static public int COL_MAPPED=3;
	final static public int GLOBAL=4;
	private boolean taken_by_class= false;
	private boolean taken_by_collection= false;
	public String where;
	public String[] sqlcolnames;
	private int mode = -1;
	
	
	/**
	 * @param mode
	 * @throws QueryException
	 */
	SaadaQLConstraint(int mode) throws QueryException {
		if( mode < NATIVE || mode > GLOBAL) {
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "SQLConstraint mode must be between 0 and 3");
		}
		this.mode = mode;
	}
	
	/**
	 * @param key
	 * @return
	 */
	public String[] getResultColNames(String key) {
		switch(this.mode) {
		case GLOBAL:
		case NATIVE: ArrayList<String> al= new ArrayList<String>();
		for( String s: sqlcolnames) {
			al.add(getNativeResultColName(key, s));
		}
		return al.toArray(new String[0]);
		case POSITION: return new String[]{"ascension", "declination"};
		case DM_MAPPED: 
		case COL_MAPPED:return new String[]{key};
		}
		return null;
	}
	/**
	 * @param colname
	 * @return
	 */
	public static  String getNativeResultColName( String key, String colname) {
		return key + "_" + colname;
	}
	/**
	 * @param colname
	 * @return
	 */
	public static  String getPositionResultColName(String colname) {
		if( colname.equals("pos_ra_csa")) {
			return "ascension";
		}
		else if( colname.equals("pos_dec_csa")) {
			return "declination";
		}
		return null;
	}
	/**
	 * @return
	 */
	public boolean isGlobal() {
		return ( mode == GLOBAL );
	}
	/**
	 * @return
	 */
	public boolean isNative() {
		return ( mode == NATIVE );
	}
	
	/**
	 * * @return
	 */
	public boolean isPosition() {
		return ( mode == POSITION );
	}
	/**
	 * @return
	 */
	public boolean isDMMapped() {
		return ( mode == DM_MAPPED );
	}
	/**
	 * @return
	 */
	public boolean isColMapped() {
		return ( mode == COL_MAPPED );
	}
	/**
	 * @return the debug_where
	 */
	public String getWhere() {
		return where;
	}

	/**
	 * @return the debug_colname
	 */
	public String[] getSqlcolnames() {
		return sqlcolnames;
	}

	/**
	 * @param alias
	 * @return
	 */
	public  String getSQL(String alias){
		if( alias != null && alias.length() > 0 ) {
			return alias + "." + sqlcolnames + " " + where;
		}
		else {
			return sqlcolnames + " " + where;
		}
	}
	
	/**
	 * @throws QueryException
	 */
	public void takeByClass() throws QueryException {
//		if( taken_by_collection ) {
//			QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "SQLConstraintBuilder already taken by a collection: can not be taken by a class");
//		}
		taken_by_class = true;
	}
	/**
	 * @throws QueryException
	 */
	public void takeByCollection() throws QueryException {
//		if( taken_by_class ) {
//			QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "SQLConstraintBuilder already taken by a class: can not be taken by a collection");
//		}
		taken_by_collection = true;
	}
	/**
	 * @return
	 */
	public boolean isTakenByClass() {
		return taken_by_class;
	}
	
	/**
	 * @return
	 */
	public boolean isTakenByCollection() {
		return taken_by_collection;
	}
	
	/**
	 * Makes the builder free again (taken neither by class nor by collection)
	 */
	public void reset(){
		taken_by_collection = false;
		taken_by_class = false;		
	}
	
	/**
	 * Relevant for UTYPE and UCD constraint only
	 * @return
	 */
	public String getMetacolname(){return null;}
	public String getUnit(){return null;}
	public VOResource getDM(){return null;}
	public String computeWhereStatement(AttributeHandler ah) throws SaadaException{return null;}
	public String computeWhereStatement(String computed_column) throws SaadaException{return null;}

} 
