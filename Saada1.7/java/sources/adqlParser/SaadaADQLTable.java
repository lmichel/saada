package adqlParser;

import saadadb.collection.Category;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.meta.MetaClass;
import saadadb.meta.MetaCollection;
import adqlParser.parser.ParseException;
import adqlParser.query.ADQLJoin;
import adqlParser.query.ADQLObject;
import adqlParser.query.ADQLQuery;
import adqlParser.query.ADQLTable;


public class SaadaADQLTable extends ADQLTable {
	
	protected MetaClass metaClass = null;
	protected MetaCollection metaColl = null;

	public SaadaADQLTable(ADQLQuery query, String alias) {
		super(query);
		if (alias != null && alias.trim().length() > 0)
			this.alias = alias;
	}

	public SaadaADQLTable(String table) throws ParseException  {
		this(table, table);
	}

	public SaadaADQLTable(String table, String alias) throws ParseException  {
		super(table);
		if (alias != null && alias.trim().length() > 0)
			this.alias = alias;
		
		metaClass = SaadaDBConsistency.getMetaClass(table);
		if (metaClass == null){
			metaColl = SaadaDBConsistency.getMetaCollection(table);
			if (metaColl != null)
				/*
				 * Added by LM to handle case sensitive DBMS
				 */
				try {
					tableRef = Database.getCachemeta().getCollectionTableName(metaColl.getName(), Category.getCategory(tableRef));
				} catch (FatalException e) {
					throw new ParseException(e.getMessage());
				}
		}else{
			tableRef = metaClass.getName();
			metaColl = SaadaDBConsistency.getMetaCollection(metaClass.getCollection_name());
		}
	}

	protected SaadaADQLTable(String table, String alias, MetaClass classMeta, MetaCollection collMeta) {
		super(table);
		if (alias != null && alias.trim().length() > 0)
			this.alias = alias;
		
		metaClass = classMeta;
		metaColl = collMeta;
	}
	
	public final boolean hasMeta(){
		return metaColl != null;
	}
	
	public final void setMeta(MetaClass classMeta, MetaCollection collMeta){
		metaClass = classMeta;
		metaColl = collMeta;
	}
	
	public final void setMeta(MetaCollection collMeta){
		metaClass = null;
		metaColl = collMeta;
	}

	/**
	 * @return The isClass.
	 */
	public final boolean isClass() {
		return metaColl != null && metaClass != null;
	}

	/**
	 * @return The metaClass.
	 */
	public final MetaClass getMetaClass() {
		return metaClass;
	}
	
	/**
	 * Gets the name of the collection table for this class.<br />
	 * <b><u>Warning:</u> it works only if the table is not a sub-query and corresponds to an existing table !</b>
	 * 
	 * @return	The table name of the collection, <i>null</i> otherwise.
	 * @throws FatalException 
	 */
	public final String getCollectionTable() throws FatalException{
		if (isClass() && metaClass != null) {
			return Database.getCachemeta().getCollectionTableName(metaClass.getCollection_name(), metaClass.getCategory());
		}
		else
			return null;
	}

	/**
	 * @return The metaColl.
	 */
	public final MetaCollection getMetaCollection() {
		return metaColl;
	}

	public ADQLObject getCopy() throws ParseException {
		SaadaADQLTable copy = (isSubQuery())?(new SaadaADQLTable((ADQLQuery)subQuery.getCopy(), alias)):(new SaadaADQLTable(tableRef, alias, metaClass, metaColl));
		if (join != null)
			copy.setJoin((ADQLJoin)join.getCopy());
		return copy;
	}

}
