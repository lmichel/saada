package saadadb.query.merger;



import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;

public abstract class SQLConstraintBuilder {
	private boolean taken_by_class= false;
	private boolean taken_by_collection= false;
	public String where;
	public String sql_colname;
	/** * @version $Id$

	 * @return the debug_where
	 */
	public String getWhere() {
		return where;
	}

	/**
	 * @return the debug_colname
	 */
	public String getSQLcolname() {
		return sql_colname;
	}

	/**
	 * @param alias
	 * @return
	 */
	public  String getSQL(String alias){
		if( alias != null && alias.length() > 0 ) {
			return alias + "." + sql_colname + " " + where;
		}
		else {
			return sql_colname + " " + where;
		}
	}
	
	/**
	 * @throws QueryException
	 */
	public void takeByClass() throws QueryException {
		if( taken_by_collection ) {
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "SQLConstraintBuilder already taken by a collection: can not be taken by a class");
		}
		taken_by_class = true;
	}
	/**
	 * @throws QueryException
	 */
	public void takeByCollection() throws QueryException {
		if( taken_by_class ) {
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "SQLConstraintBuilder already taken by a class: can not be taken by a collection");
		}
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
	
}
