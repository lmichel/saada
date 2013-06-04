package saadadb.sqltable;

import java.sql.ResultSet;
import java.util.Map;

import saadadb.collection.Category;
import saadadb.configuration.RelationConf;
import saadadb.database.Database;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.util.Messenger;

public abstract class Table_Saada_Relation extends SQLTable{
	public static final String tableName = "saada_relation";
	/**
	 * @throws AbortException
	 */
	public static  void createTable() throws SaadaException {
		SQLTable.createTable(tableName, "id " + Database.getWrapper().getSerialToken() + ", name " + Database.getWrapper().getIndexableTextType() + ", primary_coll text, primary_cat text, secondary_coll text, secondary_cat text, correlator text NULL, indexed boolean default false, description text NULL"
				, "name"
				, false);
	}
	
	/**
	 * @param relation_config
	 * @throws SaadaException 
	 */
	public static final void addRelation(RelationConf relation_config) throws SaadaException {
		SQLTable.addQueryToTransaction("insert into " + tableName + " (name, primary_coll,primary_cat,secondary_coll,secondary_cat, correlator, indexed, description  )values ("
				+ "'" + relation_config.getNameRelation() + "'," 
				+ "'" + relation_config.getColPrimary_name() + "',"
				+ "'" + Category.explain(relation_config.getColPrimary_type()) + "'," 
				+ "'" + relation_config.getColSecondary_name() + "',"
				+ "'" + Category.explain(relation_config.getColSecondary_type()) + "'," 
				+ "'" + Database.getWrapper().getEscapeQuote(relation_config.getQuery()) + "'," 
				+  Database.getWrapper().getBooleanAsString(false) + ", "
		        + "'" + Database.getWrapper().getEscapeQuote(relation_config.getDescription()) + "')"
		        , "saada_relation");
		Table_Saada_Qualifiers.addRelation(relation_config);
	}

	/**
	 * @param relation_config
	 * @throws AbortException
	 */
	public static final void saveCorrelator(RelationConf relation_config) throws SaadaException {
		SQLTable.addQueryToTransaction("update " + tableName + " set correlator = "
				+ "'" + Database.getWrapper().getEscapeQuote(relation_config.getQuery()) + "'" 
				+ " WHERE name = '"
				+ relation_config.getNameRelation() + "'"
		        , tableName);
		
	}

	
	/**
	 * @param relation_config
	 * @throws AbortException
	 */
	public static final void saveDescription(RelationConf relation_config) throws SaadaException {
		SQLTable.beginTransaction();
		SQLTable.addQueryToTransaction("update " + tableName + " set description = "
				+ "'" + Database.getWrapper().getEscapeQuote(relation_config.getDescription()) + "'" 
				+ " WHERE name = '"
				+ relation_config.getNameRelation() + "'"
		        , tableName);
		SQLTable.commitTransaction();
		
	}

	/**
	 * Return the flag indicating that relation and saada indexes are synchronized
	 * This operation is issued at DB level because the index status time life goes 
	 * over the cache life
	 * @param rel_name
	 * @return
	 * @throws FatalException 
	 */
	public static boolean isIndexed(String rel_name) throws FatalException {
		try {
			SQLQuery squery = new SQLQuery();
			ResultSet rs = squery.run("select indexed from " + tableName + " where name = '" + rel_name + "'");
			while( rs.next()) {
				boolean retour = rs.getBoolean(1);
				squery.close();
				return retour;
			}
			squery.close();
		} catch (Exception e) {
			FatalException.throwNewException(SaadaException.DB_ERROR, e);
			Messenger.printStackTrace(e);
		}
		return false;
	}
	
	/**
	 * @param rel_name
	 * @param indexed
	 * @throws AbortException
	 */
	public static void setIndexed(String rel_name, boolean indexed, int stat) throws Exception {
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Set relation <" + rel_name + ">  indexed as " + indexed);
		SQLTable.addQueryToTransaction("UPDATE " + tableName + " SET indexed = " + Database.getWrapper().getBooleanAsString(indexed) + ", stat = " + stat + " WHERE name = '" + rel_name + "'", "saada_relation");
	}
	/**
	 * @param nameRelation
	 */
	public static void removeRelation(String nameRelation){
		Table_Saada_Qualifiers.addRelation(nameRelation);
		try {
			SQLTable.addQueryToTransaction("delete from " + tableName + " where name = '" + nameRelation + "'"
			        , "saada_relation");
		} catch (AbortException e) {
			Messenger.printMsg(Messenger.ERROR, "Deleting relation: " + e.getMessage());
		}
	}
	/**
	 * Add a stat column the the table
	 * @throws Exception
	 */
	public static final void addStatColumn() throws Exception {
		SQLTable.addStatColumn(tableName);
	}

	
}
