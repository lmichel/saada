package saadadb.query.result;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Iterator;

import saadadb.meta.AttributeHandler;

/**
 * @author laurentmichel
 * * @version $Id$

 */
public class ADQLResultSet extends SaadaQLResultSet {

	public ADQLResultSet(ResultSet rs, HashMap<String,AttributeHandler> columnMeta) throws Exception {
		this(rs, -1, columnMeta);
	}

	public ADQLResultSet(ResultSet rs, int maxRowsToManage, HashMap<String,AttributeHandler> columnMeta) throws Exception {
		super(rs);
		limit = (maxRowsToManage<=0)?Integer.MAX_VALUE:maxRowsToManage;
		computeSize();		
		
		// Build the meta set:
		col_names = new SaadaQLMetaSet(null, null);
		Iterator<AttributeHandler> it = oidSQL.getMeta().iterator();
		while(it.hasNext()){
			AttributeHandler tmp = it.next();
			String name = tmp.getNameattr();
			// If the attribute handlers of a selected column is in the given map, consider this one rather than the one of the SQLLikeResultSet:
			if (columnMeta.containsKey(name))
				tmp = columnMeta.get(name);
			col_names.add(tmp);
		}
	}

}
