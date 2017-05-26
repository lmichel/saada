package saadadb.query.constbuilders;

import saadadb.database.Database;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.query.parser.SelectFromIn;

public class OrderByConstraint extends SaadaQLConstraint{
	private SelectFromIn  sfi;
	/**
	 * @param strQuery
	 * @throws QueryException
	 */
	public OrderByConstraint(String[] attributes, boolean desc, SelectFromIn sfi) throws SaadaException {
		super(SaadaQLConstraint.GLOBAL);
		this.sfi = sfi;
		this.sqlcolnames = attributes;
		if( attributes != null && attributes.length > 0) {
			this.where = "Order By " + applyScope(attributes[0]) + ((desc)? " desc": "");
		} else {
			this.where = "";
		}
	}	/**
	 * Collect the scope of the query to prepend the ordering parameter with the right table name
	 * @param sfi
	 * @throws Exception
	 */
	public String applyScope(String attr) throws SaadaException{
		String [] listClass = this.sfi.getListClass();
		String [] listColl = this.sfi.getListColl();
		if( attr.startsWith("_") ){
			if( listClass == null || listClass.length == 0 ) {
				QueryException.throwNewException(SaadaException.WRONG_PARAMETER
						, "Order by: No class given althouth the ordering parameter " + attr + "  is at class level");
			}
			this.takeByClass();
			return listClass[0] + "." + attr;
		} else {
			if( listColl == null || listColl.length == 0 ) {
				QueryException.throwNewException(SaadaException.WRONG_PARAMETER
						, "Order by: No collection given althouth the ordering parameter " + attr + "  is at collection level");
			}
			if( attr.equals("oidsaada") || attr.equals("namesaada")) {
				if( listClass.length == 1 && !listClass[0].equals("*")){
					this.takeByClass();
					return listClass[0] + "." + attr;
				} else {
					this.takeByCollection();
					return  Database.getCachemeta().getCollectionTableName(listColl[0], sfi.getCatego()) + "." + attr;	
				}				
			} else {
				this.takeByCollection();
				return  Database.getCachemeta().getCollectionTableName(listColl[0], sfi.getCatego()) + "." + attr;	
			}
		}
	}
}

