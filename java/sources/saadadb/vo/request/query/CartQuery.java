/**
 * 
 */
package saadadb.vo.request.query;

import java.util.ArrayList;

import saadadb.exceptions.QueryException;
import saadadb.query.result.SaadaInstanceResultSet;

/**
 * This a dummy query. It just stores the cart parameter for the CartFormator
 * @author laurent
 * @version $Id: CartQuery.java 118 2012-01-06 14:33:51Z laurent.mistahl $
 */
public class CartQuery extends VOQuery{

	public CartQuery() {
		mandatoryDataParams = new String[]{"cart"};
		mandatoryMetaParams = new String[]{"format"};
	}

	/* (non-Javadoc)
	 * @see saadadb.vo.request.query.VOQuery#getOids()
	 */
	@Override
	public ArrayList<Long> getOids() throws Exception {
		return null;
	}

	/* (non-Javadoc)
	 * @see saadadb.vo.request.query.VOQuery#getSaadaInstanceResultSet()
	 */
	@Override
	public SaadaInstanceResultSet getSaadaInstanceResultSet() {
		return null;
	}

	/* (non-Javadoc)
	 * @see saadadb.vo.request.query.VOQuery#close()
	 */
	@Override
	public void close() throws QueryException {
	}

	/* (non-Javadoc)
	 * @see saadadb.vo.request.query.VOQuery#buildQuery()
	 */
	@Override
	public void buildQuery() throws Exception {
	}

	/* (non-Javadoc)
	 * @see saadadb.vo.request.query.VOQuery#runQuery()
	 */
	@Override
	public void runQuery() throws Exception {
	}

}
