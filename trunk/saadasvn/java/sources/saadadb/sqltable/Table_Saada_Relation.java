package saadadb.sqltable;

import java.sql.ResultSet;

import saadadb.collection.Category;
import saadadb.configuration.RelationConf;
import saadadb.database.Database;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.util.Messenger;

/**
 * @author laurent
 * @version $Id$
 */
public abstract class Table_Saada_Relation extends SQLTable{

	public static  void createTable() throws SaadaException {
		SQLTable.createTable("saada_relation", "id " + Database.getWrapper().getSerialToken() + ", name " + Database.getWrapper().getIndexableTextType() + ", primary_coll text, primary_cat text, secondary_coll text, secondary_cat text, correlator text NULL, indexed boolean default false, description text NULL"
				, "name"
				, false);
	}

	/**
	 * @param relation_config
	 * @throws SaadaException 
	 */
	public static final void addRelation(RelationConf relation_config) throws SaadaException {
		SQLTable.addQueryToTransaction("insert into saada_relation (name, primary_coll,primary_cat,secondary_coll,secondary_cat, correlator, indexed, description  )values ("
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
		SQLTable.addQueryToTransaction("update saada_relation set correlator = "
				+ "'" + Database.getWrapper().getEscapeQuote(relation_config.getQuery()) + "'" 
				+ " WHERE name = '"
				+ relation_config.getNameRelation() + "'"
				, "saada_relation");

	}


	/**
	 * @param relation_config
	 * @throws AbortException
	 */
	public static final void saveDescription(RelationConf relation_config) throws SaadaException {
		SQLTable.beginTransaction();
		SQLTable.addQueryToTransaction("update saada_relation set description = "
				+ "'" + Database.getWrapper().getEscapeQuote(relation_config.getDescription()) + "'" 
				+ " WHERE name = '"
				+ relation_config.getNameRelation() + "'"
				, "saada_relation");
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
			ResultSet rs = squery.run("select indexed from saada_relation where name = '" + rel_name + "'");
			while( rs.next()) {
				squery.close();
				return rs.getBoolean(1);
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
	public static void setIndexed(String rel_name, boolean indexed) throws Exception {
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Set relation <" + rel_name + ">  indexed as " + indexed);
		SQLTable.addQueryToTransaction("UPDATE saada_relation SET indexed = " + Database.getWrapper().getBooleanAsString(indexed) + " WHERE name = '" + rel_name + "'", "saada_relation");
	}
	/**
	 * @param nameRelation
	 */
	public static void removeRelation(String nameRelation){
		Table_Saada_Qualifiers.addRelation(nameRelation);
		try {
			SQLTable.addQueryToTransaction("delete from saada_relation where name = '" + nameRelation + "'"
					, "saada_relation");
		} catch (AbortException e) {
			Messenger.printMsg(Messenger.ERROR, "Deleting relation: " + e.getMessage());
		}
	}
}
