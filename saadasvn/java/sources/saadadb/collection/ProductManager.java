package saadadb.collection;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import saadadb.api.SaadaLink;
import saadadb.command.ArgsParser;
import saadadb.command.EntityManager;
import saadadb.database.Database;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.query.executor.Query;
import saadadb.query.result.OidsaadaResultSet;
import saadadb.relationship.RelationManager;
import saadadb.sqltable.SQLLargeQuery;
import saadadb.sqltable.SQLQuery;
import saadadb.sqltable.SQLTable;
import saadadb.util.Messenger;

/**
 * Handle database operation at product level
 * @author michel
 *
 */
public class ProductManager extends EntityManager {
	private  boolean followLinks = false;
	private boolean noIndex = false;
	private ArrayList<Long> oids;
	private Set<String> tablesToIndex = new HashSet<String>();
	private String coll, ecoll;
	private String[] start_rel;
	private String[] estart_rel;
	private String[] end_rel;
	private String[] eend_rel;
	private String coll_table, ecoll_table;		
	private int category;
	private String classe, eclasse;
	private Map<String, ArrayList<Long>>linkTargetsToRemove = new HashMap<String, ArrayList<Long>>();

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
		try {
			noIndex = ap.isNoindex();
			if( noIndex ) Messenger.printMsg(Messenger.TRACE, "SQL indexes won't be rebuilt");
			followLinks = (ap.getLinks() != null && ap.getLinks().equalsIgnoreCase("follow"))? true: false;
			if( followLinks ) Messenger.printMsg(Messenger.TRACE, "Link targets will be removed");
			String query = ap.getRemove().trim();
			if( query.startsWith("Select")) {
				removeProducts(query);
			} else {
				String[] soids =  ap.getRemove().split("[,;]");
				long oids[] = new long[soids.length];
				for( int j=0 ; j<soids.length ; j++ ) {
					oids[j] = Long.parseLong(soids[j]);
				}
				removeProducts();
			}
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			AbortException.throwNewException(SaadaException.DB_ERROR, e);
		}
	}

	/**************************************
	 * Internal business
	 */

	/**
	 * Remove all data matching the query
	 * @param query
	 * @throws SaadaException
	 */
	public final void removeProducts(String query) throws SaadaException {
		try {
			Query q = new Query();
			OidsaadaResultSet rs = q.runBasicQuery(query);
			Messenger.printMsg(Messenger.TRACE, "Remove data matching the query " +query);
			this.oids = new ArrayList<Long>();
			while(rs.next()) {
				this.oids.add(rs.getOId());
			}
			removeProducts();			
		} catch(AbortException e ) {
			AbortException.throwNewException(SaadaException.DB_ERROR, e);
		} catch(Exception e ) {
			Messenger.printStackTrace(e);
			AbortException.throwNewException(SaadaException.DB_ERROR, e);
		}

	}

	/**
	 * @param oids
	 * @throws SaadaException
	 */
	public final void removeProducts(long[] oids) throws SaadaException {
		this.oids = new ArrayList<Long>();
		for( long oid: oids) {
			this.oids.add(oid);

		}
		Messenger.printMsg(Messenger.DEBUG, "Test : " + this.oids.get(0));
		removeProducts();				
	}

	/**
	 * @param oids
	 * @throws FatalException
	 */
	private final void removeProducts() throws SaadaException {
		/*
		 * Nothing to do: return
		 */
		if( this.oids == null || this.oids.size() == 0 ) {
			Messenger.printMsg(Messenger.WARNING, "The list of oids to remove is empty" );
			return;
		}
		/*
		 * This method can only work if all oids are from the same collection/category/class.
		 */
		this.checkOidList();


		Messenger.printMsg(Messenger.TRACE, "Remove " + this.oids.size() + " data from " + SaadaOID.getTreePath(this.oids.get(0)));
		SQLQuery squery = new SQLQuery();
		try {
			this.setDataTreeLocation();
			this.processUserRequest();
			this.storeLinkTargetsToRemove();

			/*
			 * Build the where statement testing that an oid belongs to the list of oids to remove
			 */
			String in_stm = getInStatement();
			/*
			 * If products to remove are tables: process first entries
			 */
			this.processUserRequest();
			if( category == Category.TABLE && eclasse != null && eclasse.length() > 0 ) {
				this.processUserRequest();
				String join = "(SELECT e.oidsaada FROM " + ecoll_table + " as e, " + coll_table + " as t WHERE e.oidtable = t.oidsaada AND t.oidsaada IN " + in_stm + ")";
				/*
				 * Remove first entries from relationships. Relationships index must be re-build, that can be long.....
				 */
				Messenger.printMsg(Messenger.TRACE, "Update relationships involving table entries");
				for( String rel: estart_rel) {
					this.processUserRequest();
					Messenger.printMsg(Messenger.TRACE, "Relationships <" + rel + ">");
					(new RelationManager(rel)).removePrimaryKeys(join);
				}
				for( String rel: eend_rel) {
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
				//				/*
				//				 * Flatfiles have no class
				//				 */
				//				if( !eclasse.endsWith("UserColl") ) {
				//					this.processUserRequest();
				//					SQLTable.dropTableIndex(eclasse, null);
				//					SQLTable.addQueryToTransaction("DELETE FROM " + eclasse + " WHERE oidsaada IN " + join, coll_table);
				//					SQLTable.indexTable(eclasse, null);
				//				}
				/*
				 * Remove collection level data
				 */
				this.processUserRequest();
				SQLTable.dropTableIndex(ecoll_table, null);
				SQLTable.addQueryToTransaction("DELETE FROM " + ecoll_table + " WHERE oidtable IN " + in_stm, coll_table);
				squery = new SQLQuery();
				ResultSet rs = squery.run("SELECT count(oidsaada) FROM " + ecoll_table);
				while( rs.next() ) {
					if( rs.getInt(1) == 0 ) {
						SQLTable.addQueryToTransaction("DELETE FROM " + eclasse );						
					} else {
						SQLTable.addQueryToTransaction(Database.getWrapper().getNullLeftJoinDelete(eclasse, "oidsaada", ecoll_table, "oidsaada"));						
					}
					break;
				}
				rs.close();
				//SQLTable.indexTable(ecoll_table, null);
				tablesToIndex.add(ecoll_table);
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
			if( !classe.equals("FLATFILE")) {
				this.processUserRequest();
				SQLTable.dropTableIndex(classe, null);
				SQLTable.addQueryToTransaction("DELETE FROM  " + classe + " WHERE oidsaada IN " +in_stm, classe);
				//SQLTable.indexTable(classe, null);
				tablesToIndex.add(classe);

			}
			/*
			 * and the collection level data
			 */
			this.processUserRequest();
			SQLTable.dropTableIndex(coll_table, null);
			SQLTable.addQueryToTransaction("DELETE FROM " + coll_table + " WHERE oidsaada IN  " + in_stm, coll_table);
			//SQLTable.indexTable(coll_table, null);
			tablesToIndex.add(coll_table);

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
			this.removeLinkTargets();
			this.reIndexTable();
		} catch(AbortException e ) {
			AbortException.throwNewException(SaadaException.DB_ERROR, e);
		} catch(Exception e ) {
			Messenger.printStackTrace(e);
			AbortException.throwNewException(SaadaException.DB_ERROR, e);
		}

	}

	/**
	 * Store in a map all oids targeted by the relationships starting from the data
	 * collection we are working on
	 * @throws Exception 
	 * @throws SaadaException 
	 * @throws FatalException 
	 */
	private void storeLinkTargetsToRemove() throws Exception {
		if( !followLinks ) {
			return;
		}
		for( long oid: this.oids) {
			for( String rel: start_rel) {
				/*
				 * Link target of type entry mustn't be removed beacuse they are no data porduct
				 */
				if( Database.getCachemeta().getRelation(rel).getSecondary_category() != Category.ENTRY) {
					SaadaLink[] sls = Database.getCache().getObject(oid).getStartingLinks(rel);
					for( SaadaLink sl: sls) {
						long soid= sl.getEndindOID();
						String path = SaadaOID.getTreePath(soid);
						ArrayList<Long> al;
						if( (al = linkTargetsToRemove.get(path)) == null ){
							al = new ArrayList<Long>();
							linkTargetsToRemove.put(path, al);
						}
						al.add(soid);
					}
				}
			}
			/*
			 * IN case of table, we must remove data linked to the entries
			 */
			if( category == Category.TABLE  ) {
				SQLLargeQuery squery= new SQLLargeQuery();
				ResultSet rs = squery.run("(SELECT e.oidsaada FROM " + ecoll_table + " as e, " + coll_table + " as t WHERE e.oidtable = t.oidsaada AND t.oidsaada IN " + getInStatement() + ")");
				while( rs.next() ) {
					long eoid = rs.getLong(1);
					for( String rel: start_rel) {
						SaadaLink[] sls = Database.getCache().getObject(eoid).getStartingLinks(rel);
						for( SaadaLink sl: sls) {
							long soid= sl.getEndindOID();
							/*
							 * Link target of type entry mustn't be removed beacuse they are no data porduct
							 */
							if( SaadaOID.getCategoryNum(soid) != Category.ENTRY){
								String path = SaadaOID.getTreePath(soid);
								ArrayList<Long> al;
								if( (al = linkTargetsToRemove.get(path)) == null ){
									al = new ArrayList<Long>();
									linkTargetsToRemove.put(path, al);
								}
								al.add(soid);
							}
						}
					}
				}
			}
		}
		//		for( Entry<String, ArrayList<Long>> e: linkTargetsToRemove.entrySet()) {
		//			System.out.println(e.getKey() + " " + e.getValue());
		//			
		//		}
	}

	private void removeLinkTargets() throws SaadaException {
		if( !followLinks ) {
			return;
		}
		this.followLinks = false;
		for( Entry<String, ArrayList<Long>> e: linkTargetsToRemove.entrySet()) {
			Messenger.printMsg(Messenger.TRACE, "Remove " + e.getValue().size() + " link target from " + e.getKey());
			this.oids = e.getValue();
			this.removeProducts();
		}

	}

	/**
	 * Build the where statement testing that an oid belongs to the list of oids to remove
	 */
	private String getInStatement(){
		String in_stm = "(" + this.oids.get(0);
		for(int i=1 ; i<this.oids.size() ; i++ ) {
			in_stm += ", " + this.oids.get(i);
		}
		in_stm += ")";
		return in_stm;
	}


	/**
	 * Set all attribute locating the oids to remove with the data tree
	 * @throws FatalException
	 */
	private void setDataTreeLocation() throws FatalException{
		category            = SaadaOID.getCategoryNum(this.oids.get(0));
		if( category == Category.FLATFILE ) {
			classe = "FLATFILE";
			eclasse = null;
		} else {
			classe = Database.getCachemeta().getClass(SaadaOID.getClassNum(this.oids.get(0))).getName();
			coll             = Database.getCachemeta().getCollection(SaadaOID.getCollectionNum(this.oids.get(0))).getName();
			start_rel      = Database.getCachemeta().getRelationNamesStartingFromColl(coll, category);
			end_rel        = Database.getCachemeta().getRelationNamesEndingOnColl(coll, category);
			coll_table       = Database.getCachemeta().getCollectionTableName(coll, category);		
			if( category == Category.TABLE ) {
				eclasse = Database.getCachemeta().getClass(classe).getAssociate_class();
				ecoll   = Database.getCachemeta().getCollection(SaadaOID.getCollectionNum(this.oids.get(0))).getName();
				ecoll_table       = Database.getCachemeta().getCollectionTableName(coll, Category.ENTRY);		
				estart_rel      = Database.getCachemeta().getRelationNamesStartingFromColl(ecoll, Category.ENTRY);
				eend_rel = Database.getCachemeta().getRelationNamesEndingOnColl(Database.getCachemeta().getClass(eclasse).getCollection_name()
						, Database.getCachemeta().getClass(eclasse).getCategory());	
			}
		}
	}

	/**
	 * Rebuild dataindex if requested
	 * @throws AbortException 
	 */
	private void reIndexTable() throws AbortException{
		if( !noIndex) {
			Messenger.printMsg(Messenger.TRACE, "Start to re-index " + tablesToIndex.size() + "tables.");
			for( String tbl: tablesToIndex ) {
				SQLTable.indexTable(tbl, null);
			}
		}
	}

	/**
	 * Cjack that all oids are from the same collection/category/class.
	 * @throws AbortException
	 */
	private void checkOidList() throws AbortException {
		for( long oid: oids) {
			try {
				Database.getCache().getObject(oid);
			} catch (Exception e) {
				AbortException.throwNewException(SaadaException.WRONG_PARAMETER, "No object with oid = " + oid + " found");
			}
			if( (oid >> 32) != (this.oids.get(0) >> 32) ) {
				AbortException.throwNewException(SaadaException.WRONG_PARAMETER, "OIDs to be removed are not all from the same collection/category/class");
			}
		}
	}
}
