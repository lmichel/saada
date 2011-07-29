package saadadb.sqltable;

import java.util.Iterator;

import saadadb.configuration.RelationConf;
import saadadb.database.Database;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.SaadaException;
import saadadb.util.Messenger;

public abstract class Table_Saada_Qualifiers extends SQLTable {

	public Table_Saada_Qualifiers() {
		super();
	}

	/** * @version $Id$

	 * @throws AbortException
	 */
	public static  void createTable() throws SaadaException {
		SQLTable.createTable("saada_qualifier", "name " + Database.getWrapper().getIndexableTextType() + " , qualifier " 
				+ Database.getWrapper().getIndexableTextType() + ", type text"
				, "name,qualifier"
				, false);
	}

	/**
	 * @param relation_config
	 * @throws AbortException
	 */
	public static void addRelation(RelationConf relation_config) throws AbortException {
		Iterator<String> it = relation_config.getQualifier().keySet().iterator();
		String rn = relation_config.getNameRelation();
		while( it.hasNext()) {
			SQLTable.addQueryToTransaction("insert into saada_qualifier values ("
					+ "'" + rn + "'," 
					+ "'" + it.next() + "'," 
					+ "'double')"
					, "saada_qualifier");	
		}
	}

	/**
	 * @param nameRelation
	 * @throws AbortException
	 */
	public static void addRelation(String nameRelation) {
		/*
		 * Exception must not  stop the porcess of removing the relation
		 */
		try {
			SQLTable.addQueryToTransaction("delete from  saada_qualifier where name = '" + nameRelation + "'"
					, "saada_qualifier");
		} catch (AbortException e) {
			Messenger.printMsg(Messenger.ERROR, "Deleting qualifiers: " + e.getMessage());
		}
	}
}
