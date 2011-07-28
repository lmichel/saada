package saadadb.collection;

import java.sql.ResultSet;

import saadadb.command.ArgsParser;
import saadadb.command.EntityManager;
import saadadb.database.Database;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.relationship.RelationManager;
import saadadb.sqltable.SQLQuery;
import saadadb.sqltable.SQLTable;
import saadadb.util.Messenger;

/**
 * Handle database operation at product level
 * @author michel
 *
 */
public class ProductManager extends EntityManager {
	/**
	 * Although all method could be static, we need to make an instance to monitor the progress from 
	 * the GUI
	 */
	public ProductManager() {
		super();
	}

	/***********************************
	 * Inherited abstract methods
	 */
	@Override
	public void comment(ArgsParser ap) throws SaadaException {
		
	}


	@Override
	public void create(ArgsParser ap) throws SaadaException {
		
	}


	@Override
	public void empty(ArgsParser ap) throws SaadaException {
		
	}


	@Override
	public void index(ArgsParser ap) throws SaadaException {
		
	}


	@Override
	public void populate(ArgsParser ap) throws SaadaException {
		
	}


	@Override
	public void remove(ArgsParser ap) throws SaadaException {
		
	}

	/**************************************
	 * Internal business
	 */
	/**
	 * @param product_filename
	 * @throws SaadaException
	 */
	public final void removeProduct(String product_filename) throws SaadaException{
		SQLQuery squery = new SQLQuery();

		try {
			ResultSet rs = squery.run("SELECT oidsaada FROM saada_loaded_file WHERE filename = '" + product_filename + "'");
			boolean found = false;
			while( rs.next() ) {
				found = true;
				long oid = rs.getLong(1);
				this.processUserRequest();
				Messenger.printMsg(Messenger.TRACE, "Remove product file <" + product_filename + "> " 
						+ SaadaOID.getCategoryName(oid) + " instance of class <" 
						+ SaadaOID.getClassName(oid) + "> in collection " 
						+ SaadaOID.getCollectionName(oid));
				removeProducts(new long[]{oid});
			}
			if( !found ) {
				Messenger.printMsg(Messenger.WARNING, "Product file <" + product_filename + "> not found");
			}
			squery.close();
		} catch (Exception e) {			
			Messenger.printStackTrace(e);
			AbortException.throwNewException(SaadaException.DB_ERROR, e);
		}
		
	}
	/**
	 * @param oids
	 * @throws FatalException
	 */
	public final void removeProducts(long[] oids) throws SaadaException {
		/*
		 * Nothing to do: return
		 */
		if( oids.length == 0 ) {
			return;
		}
		/*
		 * This method can only work if all oids are from the same collection/category/class.
		 */
		for( long oid: oids) {
			if( (oid >> 32) != (oids[0] >> 32) ) {
				AbortException.throwNewException(SaadaException.WRONG_PARAMETER, "OIDs to be removed are not all from the same collection/category/class");
			}
		}
		SQLQuery squery = new SQLQuery();
		try {
			/*
			 * Build some metadata names used afters
			 */
			int category            = SaadaOID.getCategoryNum(oids[0]);
			String classe           = Database.getCachemeta().getClass(SaadaOID.getClassNum(oids[0])).getName();
			String coll             = Database.getCachemeta().getCollection(SaadaOID.getCollectionNum(oids[0])).getName();
			String[] start_rel      = Database.getCachemeta().getRelationNamesStartingFromColl(coll, category);
			String[] end_rel        = Database.getCachemeta().getRelationNamesEndingOnColl(coll, category);
			String coll_table       = Database.getCachemeta().getCollectionTableName(coll, category);		
			this.processUserRequest();
			/*
			 * Build the where statement testing that an oid belongs to the list of oids to remove
			 */
			String in_stm = "(" + oids[0];
			for(int i=1 ; i<oids.length ; i++ ) {
				in_stm += ", " + oids[i];
			}
			in_stm += ")";
			/*
			 * Begins a possibly heavy transaction
			 */
			SQLTable.beginTransaction();
			/*
			 * If products to remove are tables: process first entries
			 */
			this.processUserRequest();
			String eclasse = Database.getCachemeta().getClass(classe).getAssociate_class();
			if( eclasse != null && eclasse.length() > 0 ) {
				this.processUserRequest();
				String ecoll_table = Database.getCachemeta().getCollectionTableName(coll, Database.getCachemeta().getClass(eclasse).getCategory());
				String join = "(SELECT e.oidsaada FROM " + ecoll_table + " as e, " + coll_table + " as t WHERE e.oidtable = t.oidsaada AND t.oidsaada IN " + in_stm + ")";
				String[] entr_start_rel = Database.getCachemeta().getRelationNamesStartingFromColl(Database.getCachemeta().getClass(eclasse).getCollection_name()
						, Database.getCachemeta().getClass(eclasse).getCategory());
				String[] entr_end_rel = Database.getCachemeta().getRelationNamesEndingOnColl(Database.getCachemeta().getClass(eclasse).getCollection_name()
						, Database.getCachemeta().getClass(eclasse).getCategory());			
				/*
				 * Remove first entries from relationships. Relationships index must be re-build, that can be long.....
				 */
				Messenger.printMsg(Messenger.TRACE, "Update relationships involving table entries");
				for( String rel: entr_start_rel) {
					this.processUserRequest();
					Messenger.printMsg(Messenger.TRACE, "Relationships <" + rel + ">");
					(new RelationManager(rel)).removePrimaryKeys(join);
				}
				for( String rel: entr_end_rel) {
					this.processUserRequest();
					Messenger.printMsg(Messenger.TRACE, "Relationships <" + rel + ">");
					(new RelationManager(rel)).removeSecondaryKeys(join);
				}
				/*
				 * Remove entries rows from the DB
				 */
				Messenger.printMsg(Messenger.TRACE, "Remove entries of all tables");
				/*
				 * Update the class level first in order to keep the join with table which uses the collection table
				 */
				/*
				 * Flatfiles have no class
				 */
				if( !eclasse.endsWith("UserColl") ) {
					this.processUserRequest();
					SQLTable.dropTableIndex(eclasse, null);
					SQLTable.addQueryToTransaction("DELETE FROM " + eclasse + " WHERE oidsaada IN " + join, coll_table);
					SQLTable.indexTable(eclasse, null);
				}
				/*
				 * Remove collection level data
				 */
				this.processUserRequest();
				SQLTable.dropTableIndex(ecoll_table, null);
				SQLTable.addQueryToTransaction("DELETE FROM " + ecoll_table + " WHERE oidsaada IN " + join, coll_table);
				SQLTable.indexTable(ecoll_table, null);
			}

			Messenger.printMsg(Messenger.TRACE, "Remove data products");
			/*
			 * Update relationship first
			 */
			Messenger.printMsg(Messenger.TRACE, "Update relationships");

			for( String rel: start_rel) {
				this.processUserRequest();
				Messenger.printMsg(Messenger.TRACE, "Relationships <" + rel + ">");
				(new RelationManager(rel)).removePrimaryKeys(in_stm);
			}
			for( String rel: end_rel) {
				this.processUserRequest();
				Messenger.printMsg(Messenger.TRACE, "Relationships <" + rel + ">");
				(new RelationManager(rel)).removeSecondaryKeys(in_stm);
			}
			/*
			 * remove class level data first
			 */
			/*
			 * Flatfiles have no class
			 */
			if( !classe.endsWith("UserColl") ) {
				this.processUserRequest();
				SQLTable.dropTableIndex(classe, null);
				SQLTable.addQueryToTransaction("DELETE FROM  " + classe + " WHERE oidsaada IN " +in_stm, classe);
				SQLTable.indexTable(classe, null);
			}
			/*
			 * and the collection level data
			 */
			this.processUserRequest();
			SQLTable.dropTableIndex(coll_table, null);
			SQLTable.addQueryToTransaction("DELETE FROM " + coll_table.toLowerCase() + " WHERE oidsaada IN  " + in_stm, coll_table);
			SQLTable.indexTable(coll_table, null);

			/*
			 * Remove data files from the repository
			 */
			if( category != Category.ENTRY ) {
				this.processUserRequest();
				Messenger.printMsg(Messenger.TRACE, "Remove data products from the repository");
				/*
				 * Files with a full path have been loaded with the "keep" repository name. They musn't be removed
				 */
				squery = new SQLQuery();
				ResultSet rs = squery.run("SELECT repositoryname FROM saada_loaded_file WHERE oidsaada IN "  + in_stm );
				while( rs.next() ) {
					this.processUserRequest();
					String fn = rs.getString(1);
					Messenger.printMsg(Messenger.TRACE, "Deleting <" + fn + "> from the repository");
					RepositoryManager.removeFile(fn, coll, category);
				}
				squery.close();
				SQLTable.addQueryToTransaction("DELETE FROM saada_loaded_file WHERE oidsaada IN " + in_stm, "saada_loaded_file");
			}
			SQLTable.commitTransaction();
		} catch(AbortException e ) {
			AbortException.throwNewException(SaadaException.DB_ERROR, e);
		} catch(Exception e ) {
			Messenger.printStackTrace(e);
			AbortException.throwNewException(SaadaException.DB_ERROR, e);
		}

	}


}
