package saadadb.relationship;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import saadadb.command.SaadaProcess;
import saadadb.database.Database;
import saadadb.database.Repository;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.MetaRelation;
import saadadb.sqltable.SQLLargeQuery;
import saadadb.sqltable.SQLTable;
import saadadb.sqltable.Table_Saada_Relation;
import saadadb.util.Messenger;
import saadadb.util.TimeSaada;

/**
 * @author michel
 * * @version $Id$
 * 05/2012:  Make sure index are empty for relation without counterparts. (a NULL OIDS were set right now)
 */
public class IndexBuilder extends SaadaProcess { 

	private String path;
	private String relationName;
	private boolean isEmpty;


	/**
	 * @param ripath
	 * @param relationName
	 * @throws QueryException 
	 */
	public IndexBuilder(String ripath, String relationName) throws Exception{
		super(15);
		this.path = ripath;
		this.relationName = relationName;
	}


	/**
	 * @throws Exception 
	 */
	private final void createCardinalityIndex() throws Exception{
		Messenger.printMsg(Messenger.TRACE, "Build cardinality index");
		MetaRelation mr = Database.getCachemeta().getRelation(relationName);
		if( mr == null ) return;
		String colPrimary   = Database.getCachemeta().getCollectionTableName(mr.getPrimary_coll(), mr.getPrimary_category());

		SQLLargeQuery squery= new SQLLargeQuery();
		ResultSet rse = squery.run("SELECT oidprimary FROM "+this.relationName + " LIMIT 1");
		isEmpty = true;
		while(rse.next()){
			Messenger.printMsg(Messenger.TRACE, "relationship not empty");
			isEmpty = false;
			break;
		}
		if( isEmpty ) {
			Messenger.printMsg(Messenger.TRACE, "relationship  empty");			
		}
		rse.close();
		
		TimeSaada time = new TimeSaada();
		time.start();
		LinkedHashMap<Long, ArrayList<Object>> cardI = new LinkedHashMap<Long,ArrayList<Object>>();
		ArrayList<Object> al = new ArrayList<Object>();
		if( isEmpty) {
			ResultSet rs = squery.run("SELECT oidsaada FROM "+colPrimary);
			while(rs.next()){
				al.add(rs.getLong(1));
			}
			squery.close();
			rs = null;
			squery = null;
			this.processUserRequest();
			Messenger.incrementeProgress();
			cardI.put(0L,al);			
		}
		else {
			/*
			 * Select NULL cardinality
			 */
			ResultSet rs = squery.run(Database.getWrapper().getSelectWithExcept("SELECT oidsaada FROM "+colPrimary
					, "oidsaada"
					, "SELECT distinct oidprimary FROM "+relationName));
			while(rs.next()){
				al.add(rs.getLong(1));
			}
			squery.close();
			rs = null;
			squery = null;
			this.processUserRequest();
			Messenger.incrementeProgress();
			cardI.put(0L,al);
			/*
			 * Add non NULL cardinality
			 */
			squery= new SQLLargeQuery();
			rs = squery.run("SELECT count(oidsecondary),oidprimary FROM "+relationName+" GROUP BY oidprimary ORDER BY count(oidsecondary)");
			long cardprevious = 0;
			while(rs.next()){
				long  card = rs.getLong(1);
				long oidp = rs.getLong(2);
				if( card!=cardprevious ){
					al = new ArrayList<Object>();
					cardI.put(card,al);
				}
				al.add(oidp);
				cardprevious = card;
			}
			squery.close();
			rs = null;
			squery = null;
		}
		this.processUserRequest();
		Messenger.incrementeProgress();
		LongCPIndex lci = new LongCPIndex(relationName, this.path, "card");
		lci.buildIndex(cardI);
		lci.save();

		this.processUserRequest();
		Messenger.incrementeProgress();

		time.stop();
		Messenger.printMsg(Messenger.DEBUG, "Index " + lci.getName() + " built in " +time.check()+" ms");
		al = null;
		cardI = null;
		lci = null;
		System.gc();
	}

	/**
	 * @throws Exception 
	 */
	private final void createCollCorrIndex() throws Exception{
		Messenger.printMsg(Messenger.TRACE, "Build correlation index (collection level)");
		MetaRelation mr = Database.getCachemeta().getRelation(relationName);
		if( mr == null ) return;

		TimeSaada time = new TimeSaada();
		time.start();

		SQLLargeQuery squery = new SQLLargeQuery();
		ResultSet rs = squery.run("SELECT oidprimary,oidsecondary FROM "+relationName+" ORDER BY oidprimary,oidsecondary");

		this.processUserRequest();
		Messenger.incrementeProgress();
		HashMap<Long,ArrayList<Object>> corrI = new LinkedHashMap<Long,ArrayList<Object>>();
		ArrayList<Object> al = new ArrayList<Object>();
		long oidprevious = 0;
		while(rs.next()){
			long oidp = rs.getLong(1);
			long oids = rs.getLong(2);
			if(oidprevious!=0 && oidp!=oidprevious){
				corrI.put(oidprevious,al);
				al = new ArrayList<Object>();
			}
			al.add(oids);
			oidprevious = oidp;
		}
		squery.close();
		rs = null;
		//Add nothing if there is no counterparts
		if( oidprevious != 0 ) {
			corrI.put(oidprevious,al);
		}

		LongCPIndex lci = new LongCPIndex(relationName, this.path,"corr");
		lci.buildIndex(corrI);
		lci.save();

		time.stop();
		this.processUserRequest();
		Messenger.incrementeProgress();
		Messenger.printMsg(Messenger.DEBUG, lci.getName() + ": " + lci.getSize() + "entries  built in " +time.check()+" ms");

		al = null;
		corrI = null;
		lci = null;
		System.gc();
	}

	/**
	 * @param qualifier
	 * @throws SaadaException
	 * @throws SQLException
	 * @throws IOException
	 */
	private final void createCollQualIndex(String qualifier) throws Exception {
		Messenger.printMsg(Messenger.TRACE, "Build index for qualifier \"" + qualifier + "\"(collection level)");
		TimeSaada time = new TimeSaada();
		time.start();

		SQLLargeQuery squery= new SQLLargeQuery();
		ResultSet rs = squery.run("SELECT oidprimary,oidsecondary,"+qualifier+" FROM "+relationName+" WHERE " + qualifier + " IS NOT NULL ORDER BY oidprimary,oidsecondary");

		this.processUserRequest();
		Messenger.incrementeProgress();

		HashMap<Long,ArrayList<Object>> qualI = new LinkedHashMap<Long,ArrayList<Object>>();
		ArrayList<Object> al = new ArrayList<Object>();

		long oidprevious = 0;
		while(rs.next()){
			long oidp = rs.getLong(1);
			double qual = rs.getDouble(qualifier);
			if( oidprevious != 0 &&  oidp != oidprevious){
				qualI.put(oidprevious,al);
				al = new ArrayList<Object>();
			}
			al.add(qual);
			oidprevious = oidp;
		}
		squery.close();
		rs = null;
		//Add nothing if there is no counterparts
		if( oidprevious != 0 ) {
			qualI.put(oidprevious,al);
		}

		DoubleCPIndex ri = new DoubleCPIndex(relationName, this.path, "qual." + qualifier);
		ri.buildIndex(qualI);
		ri.save();

		time.stop();
		this.processUserRequest();
		Messenger.incrementeProgress();
		Messenger.printMsg(Messenger.DEBUG, ri.getName() + ": " + ri.getSize() + "entries  built in " +time.check()+" ms");

		al = null;
		qualI = null;
		ri = null;
		System.gc();
	}


	/**
	 * @param className
	 * @throws SaadaException
	 * @throws SQLException
	 * @throws IOException
	 */
	private final void createClassCorrIndex(String className) throws Exception {
		Messenger.printMsg(Messenger.TRACE, "Build correlation indexes (class \"" + className + "\"level)");
		TimeSaada time = new TimeSaada();
		time.start();

		SQLLargeQuery squery= new SQLLargeQuery();
		ResultSet rs = squery.run("SELECT oidprimary,oidsecondary"
				+" FROM " + relationName
				+" WHERE " + Database.getWrapper().getSecondaryClassColumn() + " = " + Database.getCachemeta().getClass(className).getId()
				+" ORDER BY oidprimary,oidsecondary");

		LinkedHashMap<Long,ArrayList<Object>> corrI = new LinkedHashMap<Long,ArrayList<Object>>();
		ArrayList<Object> al = new ArrayList<Object>();

		long oidprevious = 0;
		while(rs.next()){
			long oidp = rs.getLong(1);
			long oids = rs.getLong(2);
			if(oidprevious!=0 && oidp!=oidprevious){
				corrI.put(oidprevious,al);
				al = new ArrayList<Object>();
			}
			al.add(oids);
			oidprevious = oidp;
		}
		squery.close();
		rs = null;
		//Add nothing if there is no counterparts
		if( oidprevious != 0 ) {
			corrI.put(oidprevious,al);
		}

		LongCPIndex lci = new LongCPIndex(relationName, this.path,"corrclass." + className);
		lci.buildIndex(corrI);
		lci.save();
		this.processUserRequest();
		Messenger.incrementeProgress();
		time.stop();
		Messenger.printMsg(Messenger.DEBUG, lci.getName() + ": " + lci.getSize() + "entries  built in " +time.check()+" ms");

		al = null;
		corrI = null;
		lci = null;
		System.gc();
	}

	/**
	 * @param className
	 * @param qualifier
	 * @throws SaadaException
	 * @throws SQLException
	 * @throws IOException
	 */
	private final void createClassQualIndex(String className,String qualifier) throws Exception {
		Messenger.printMsg(Messenger.TRACE, "Build index for qualifier \"" + qualifier + "\"(class \"" + className + "\" level)");
		TimeSaada time = new TimeSaada();
		time.start();

		SQLLargeQuery squery= new SQLLargeQuery();
		ResultSet rs = squery.run("SELECT oidprimary,oidsecondary," + qualifier
				+" FROM " + relationName
				+" WHERE " + qualifier + " IS NOT NULL AND " + Database.getWrapper().getSecondaryClassColumn() + " = " + Database.getCachemeta().getClass(className).getId()
				+" ORDER BY oidprimary,oidsecondary");

		LinkedHashMap<Long, ArrayList<Object>> qualI = new LinkedHashMap<Long,ArrayList<Object>>();
		ArrayList<Object> al = new ArrayList<Object>();
		long oidprevious = 0;
		while(rs.next()){
			long oidp = rs.getLong(1);
			double qual = rs.getDouble(3);
			if(oidprevious!=0 && oidp!=oidprevious){
				qualI.put(oidprevious,al);
				al = new ArrayList<Object>();
			}
			al.add(qual);
			oidprevious = oidp;
		}
		squery.close();
		rs = null;
		//Add nothing if there is no counterparts
		if( oidprevious != 0 ) {
			qualI.put(oidprevious,al);
		}

		this.processUserRequest();
		Messenger.incrementeProgress();

		DoubleCPIndex ri = new DoubleCPIndex(relationName, this.path,"classqual." + className + "." + qualifier);
		ri.buildIndex(qualI);
		ri.save();
		this.processUserRequest();
		Messenger.incrementeProgress();
		time.stop();
		Messenger.printMsg(Messenger.DEBUG, ri.getName() + ": " + ri.getSize() + "entries  built in " +time.check()+" ms");

		al = null;
		ri = null;
		qualI = null;
		System.gc();
	}

	/**
	 * @throws SaadaException
	 * @throws SQLException
	 * @throws IOException
	 */
	private final void createCollQualIndexes() throws Exception{
		for(String qual:Database.getCachemeta().getRelation(relationName).getQualifier_names()){
			createCollQualIndex(qual);
		}
	}

	/**
	 * @throws SaadaException
	 * @throws SQLException
	 * @throws IOException
	 */
	private final void createClassQualIndexes() throws Exception{
		MetaRelation mr = Database.getCachemeta().getRelation(relationName);
		for(String className:Database.getCachemeta().getClassesOfCollection(mr.getSecondary_coll(),mr.getSecondary_category())){
			createClassCorrIndex(className);
			for(String qual:Database.getCachemeta().getRelation(relationName).getQualifier_names()){
				createClassQualIndex(className,qual);
			}
		}
	}

	/**
	 * @throws Exception
	 */
	public void createIndexRelation() throws Exception{
		if( Database.getCachemeta().getRelation(relationName) == null ) return;
		Messenger.printMsg(Messenger.TRACE, "Build Saada indexes for relationship " + relationName);
		indexSecondaryOidColumn();
		indexPrimaryOidColumn();
		/*
		 * Grant access to tables (we can be with a transaction)
		 */
		SQLTable.commitTransaction();
		createCardinalityIndex();
		createCollCorrIndex();
		createCollQualIndexes();
		createClassQualIndexes();
		/*
		 * Marks the relationship as indexed
		 */
		SQLTable.beginTransaction();
		Table_Saada_Relation.setIndexed(relationName, true);
		/*
		 * We suppose to be within a transaction
		 */
		//SQLTable.commitTransaction();
	}


	/**
	 * Index the join  table on the class field of the secondary oid
	 * @throws SQLException 
	 * @throws AbortException 
	 * 
	 */
	public void indexSecondaryOidColumn() throws Exception {
		String index_name = relationName + "_secoid_class";
		Messenger.printMsg(Messenger.TRACE, "Index Secondary Classids " + index_name);
		Map<String, String> existing_index = Database.getWrapper().getExistingIndex(relationName);
		if( existing_index != null && existing_index.get(index_name.toLowerCase()) == null && existing_index.get(index_name) == null) {
			SQLTable.addQueryToTransaction(Database.getWrapper().getSecondaryRelationshipIndex(relationName), relationName);	
			return;
		}
		return;
	}

	/**
	 * Index the join  table on the class field of the secondary oid
	 * @throws SQLException 
	 * @throws AbortException 
	 * 
	 */
	public void indexPrimaryOidColumn() throws Exception {
		String index_name = relationName + "_primoid_class";
		Messenger.printMsg(Messenger.TRACE, "Index Primary Classids " + index_name);
		Map<String, String> existing_index = Database.getWrapper().getExistingIndex(relationName);
		if( existing_index != null && existing_index.get(index_name.toLowerCase()) == null&& existing_index.get(index_name) == null ) {
			SQLTable.addQueryToTransaction(Database.getWrapper().getPrimaryRelationshipIndex(relationName), relationName);	
			return;
		}
		return;
	}

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {		
		Messenger.debug_mode = false;
		String dir = Repository.getIndexrelationsPath() + Database.getSepar();
		String[] relations = Database.getCachemeta().getRelation_names();

		if( args.length == 0 || args[0].toLowerCase().equals("all") || args[0].toLowerCase().equals("-all") ) {
			Messenger.printMsg(Messenger.TRACE, "Indexing all relations");
			for( int i=0 ; i<relations.length ; i++ ) {
				IndexBuilder ib = new IndexBuilder(dir, relations[i]);
				Messenger.printMsg(Messenger.TRACE, "Indexing relation " + relations[i]);
				try {
					ib.createIndexRelation();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}
		else {
			for( int i=0 ; i<relations.length ; i++ ) {
				IndexBuilder ib = new IndexBuilder(dir, relations[i]);
				if( args[0].equals(relations[i])) {
					Messenger.printMsg(Messenger.TRACE, "Indexing relation " + relations[i]);
					try {
						ib.createIndexRelation();
					} catch (Exception e) {
						e.printStackTrace();
					} 
					System.exit(0);
				}
			}
			Messenger.printMsg(Messenger.ERROR, "Relation " + args[0] + " doesn't exist");
			System.exit(1);
		}
	}	
}
