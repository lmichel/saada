package saadadb.query.constbuilders;
 
import saadadb.exceptions.QueryException;

/**
 * @author laurent
 * @version $Id$
 */
public class NativeSQLConstraint extends SaadaQLConstraint{
	
	
	/** * @version $Id$

	 * @param strQuery
	 * @throws QueryException
	 */
	public NativeSQLConstraint( String   where, String[] attributes) throws QueryException {
		super(SaadaQLConstraint.NATIVE);
		this.where = where;
		this.sqlcolnames = attributes;
	}

}
