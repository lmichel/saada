package saadadb.query.merger;

import saadadb.exceptions.QueryException;

public class DevelopConstraintBuilder extends SQLConstraintBuilder {

	/** * @version $Id$

	 * @param SQL_col
	 * @param where
	 * @throws QueryException 
	 */
	public DevelopConstraintBuilder(String SQL_col, String where) throws QueryException {
		this.sql_colname = SQL_col;
		this.where = where;
	}
	
}
