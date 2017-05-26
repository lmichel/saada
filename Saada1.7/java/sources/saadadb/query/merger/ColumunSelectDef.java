package saadadb.query.merger;

import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;

/**
 * @author michel
 * Definition of a column in select clause. As queries often cover multiple tables, alias are needed
 * STANDARD mode: SELECT a.expsession AS alias
 * NOMAPPING mode: SELECT expression AS alias
 * EXPRESSION mode: SELECT (2*class._x - collection.y) AS alias
 */
class ColumunSelectDef {
	private String sqlcolname;
	private String resultcolname;
	private int mode;
	static final int STANDARD = 1;
	static final int NOMAPPING = 2;
	static final int EXPRESSION = 3;
	
	ColumunSelectDef(String sqlcolname, String resultcolname, int mode) throws QueryException {
		if( sqlcolname == null || sqlcolname.length() == 0 || resultcolname == null || resultcolname.length() == 0 
				|| mode < 1 || mode > 3) {
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "On ColumunSelectDef init");
		}
		this.sqlcolname = sqlcolname;
		this.resultcolname = resultcolname;
		this.mode = mode;
	}

	public boolean isInStandardMode() {
		return ( mode == STANDARD)? true: false;
	}
	public boolean isInNomappingMode() {
		return ( mode == NOMAPPING)? true: false;
	}
	public boolean isInExpressionMode() {
		return ( mode == EXPRESSION)? true: false;
	}
	/**
	 * @return the expression
	 */
	public String getSqlcolname() {
		return sqlcolname;
	}

	/**
	 * @return the alias
	 */
	public String getResultcolname() {
		return resultcolname;
	}
	
	
	/**
	 * @param table_name
	 * @return
	 */
	public String getSQLColumnDef(String table_name) {
		if( this.mode == STANDARD ) {
			if( "coord".equals(sqlcolname)) {
				return table_name + ".pos_ra_csa  as ascension, " + table_name  + ".pos_dec_csa as declination";					
			}
			else {
				return table_name + "." + sqlcolname + " as " + resultcolname;
			}
		}
		else if( this.mode == EXPRESSION ) {
				return "(" + sqlcolname + ") as " + resultcolname;
		}
		else {
			return null;
		}
	}
}
