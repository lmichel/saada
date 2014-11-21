package saadadb.query.merger;

import java.util.LinkedHashMap;
import java.util.Map.Entry;


/**
 * Used as message between ClassQnode and  COllectionQNode
 * @author michel
 *
 */
public class SetOfSelectedColumns {
	protected LinkedHashMap<String, ColumunSelectDef> select_class = new LinkedHashMap<String, ColumunSelectDef>();
	protected LinkedHashMap<String, ColumunSelectDef> select_collection = new LinkedHashMap<String, ColumunSelectDef>();
	protected LinkedHashMap<String, ColumunSelectDef> select_either = new LinkedHashMap<String, ColumunSelectDef>();
	/*
	 * Used when we can not see if column is class or collection (case of DM mappping)
	 */
	private boolean force_join = false;
	 
	
	/**
	 * 
	 */
	SetOfSelectedColumns() {
		select_class = new LinkedHashMap<String, ColumunSelectDef>();
		select_collection = new LinkedHashMap<String, ColumunSelectDef>();
		select_either = new LinkedHashMap<String, ColumunSelectDef>();
	}

	/**
	 * 
	 */
	public void forceJoin() {
		force_join = true;
	}
	/**
	 * @param result_column_name
	 * @param coldef
	 * @return
	 */
	public void addSelectedColumn(String result_column_name, ColumunSelectDef coldef) {
		String sqlcolname = coldef.getSqlcolname();
		if( sqlcolname.equals("oidsaada") ) {
			return;
		}
		else if( sqlcolname.startsWith("_")  ) {
			select_class.put(result_column_name, coldef);
		}
		else if( sqlcolname.equals("obs_id") ) {
			select_either.put(result_column_name, coldef);
		}
		else {
			select_collection.put(result_column_name, coldef);
		}
	}
	
	/**
	 * @param result_column_name
	 * @param coldef
	 */
	public void addSelectedClassColumn(String result_column_name, ColumunSelectDef coldef) {
		String sqlcolname = coldef.getSqlcolname();
		if( sqlcolname.equals("oidsaada") ) {
			return;
		}
		else  {
			select_class.put(result_column_name, coldef);
		}
	}
	/**
	 * @return
	 */
	public boolean hasOnlyClassColumns() {
		if( !force_join && select_collection.size() == 0 ) {
			return true;
		}
		else {
			return false;
		}
	}
	/**
	 * @return
	 */
	public boolean hasOnlyCollectionColumns() {
		if( !force_join && select_class.size() == 0 ) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public void setEitherColumns() {
		if( this.select_either == null || this.select_either.size() == 0 ) {
			return;
		}
		if(  select_collection.size() == 0 ) {
			select_class.putAll(select_either);
		}
		else {
			select_collection.putAll(select_either);
		}
		this.select_either = new LinkedHashMap<String, ColumunSelectDef>();
	}
	/**
	 * @param with_either
	 * @return
	 */
	public String getClassSelect(String table_name) {
		String retour = "";
		for( Entry<String, ColumunSelectDef> e: select_class.entrySet()) {
			String scn = e.getValue().getSQLColumnDef(table_name);
//			if( scn.indexOf('.') == -1 ) {
//				scn = table_name + "." + scn;
//			}
			if( retour.length() > 0 ) {
				retour += ", ";
			}
//			retour += scn + " AS " + e.getKey();
			retour += scn;
		}
		if( this.hasOnlyClassColumns() ) {
			for( Entry<String, ColumunSelectDef> e: select_either.entrySet()) {
				String scn = e.getValue().getSQLColumnDef(table_name);
//				if( scn.indexOf('.') == -1 ) {
//					scn = table_name + "." + scn;
//				}
				if( retour.length() > 0 ) {
					retour += ", ";
				}
//				retour += scn + " AS " + e.getKey();
				retour += scn;
			}
		}
		return retour;
	} 
	/**
	 * @param with_either
	 * @return
	 */
	public String getCollectionSelect(String table_name) {
		String retour = "";
		for( Entry<String, ColumunSelectDef> e: select_collection.entrySet()) {
			String scn = e.getValue().getSQLColumnDef(table_name);
//			if( scn.indexOf('.') == -1 ) {
//				scn = table_name + "." + scn;
//			}
			if( retour.length() > 0 ) {
				retour += ", ";
			}
//			retour += scn + " AS " + e.getKey();
			retour += scn;
		}
			for( Entry<String, ColumunSelectDef> e: select_either.entrySet()) {
				String scn = e.getValue().getSQLColumnDef(table_name);
//				if( scn.indexOf('.') == -1 ) {
//					scn = table_name + "." + scn;
//				}
				if( retour.length() > 0 ) {
					retour += ", ";
				}
//				retour += scn + " AS " + e.getKey();
				retour += scn;
			
		}
		return retour;
	} 
}
