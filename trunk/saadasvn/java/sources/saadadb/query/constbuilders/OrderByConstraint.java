package saadadb.query.constbuilders;

import saadadb.exceptions.QueryException;

public class OrderByConstraint extends SaadaQLConstraint{

	/**
	 * @param strQuery
	 * @throws QueryException
	 */
	public OrderByConstraint( String   where, String[] attributes) throws QueryException {
		super(SaadaQLConstraint.GLOBAL);
		this.where = where;
		this.sqlcolnames = attributes;
	}
}

