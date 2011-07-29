package saadadb.query.constbuilders;
 
import saadadb.exceptions.QueryException;

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

	public static void main(String[] args) {
		 * @version $Id$
		
	}
}
