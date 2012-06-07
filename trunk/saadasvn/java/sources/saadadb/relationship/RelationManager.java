package saadadb.relationship;

import java.io.File;
import java.util.ArrayList;

import saadadb.collection.SaadaOID;
import saadadb.command.ArgsParser;
import saadadb.command.EntityManager;
import saadadb.configuration.RelationConf;
import saadadb.database.Database;
import saadadb.database.Repository;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.generationclass.ClassRemover;
import saadadb.sqltable.SQLTable;
import saadadb.sqltable.Table_Saada_Relation;
import saadadb.util.Messenger;

/**
 * @author michel
 *
 */
/** * @version $Id$

 * @author michel
 * 01/2010: suppression de la generation de code correlateur
 */
public class RelationManager extends  EntityManager {

	private RelationConf relation_conf;
	public static String separ = System.getProperty("file.separator");


	/***********************************
	 * Inherited abstract methods
	 */
	public RelationManager(String name) {
		super(name); 

		try {
			this.relation_conf = new RelationConf(name);
		} catch (Exception e) {
			Messenger.printMsg(Messenger.ERROR, e.getLocalizedMessage());
		}
	}
	/**
	 * 
	 */
	public RelationManager() {
		super();
	}

	/**
	 * @param config
	 */
	public RelationManager(RelationConf config) {
		super(config.getNameRelation());
		this.relation_conf = config;
	}

	@Override
	public void create(ArgsParser ap) throws SaadaException {
		if( ap != null ) {
			this.buildConfig(ap);
		}
		this.create();
	}

	@Override
	public void empty(ArgsParser ap) throws SaadaException {
		try {
			this.empty();
		} catch (Exception e) {
			FatalException.throwNewException(SaadaException.DB_ERROR, e);
		}

	}

	@Override
	public void index(ArgsParser ap) throws SaadaException {
		try {
			IndexBuilder ib = new IndexBuilder(Repository.getIndexrelationsPath() + Database.getSepar(), name);

			ib.createIndexRelation();
		} catch (Exception e) {
			FatalException.throwNewException(SaadaException.DB_ERROR, e);
		}
	}

	@Override
	public void populate(ArgsParser ap) throws SaadaException {
		try {
			String q;			
			if(this.relation_conf == null  ) {
				this.relation_conf = new RelationConf(ap.getPopulate());
			}
			if( ap != null && (q = ap.getQuery()) != null ) {
				this.populateWithQuery(q);
			}
			else {
				this.populateWithQuery();
			}
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			FatalException.throwNewException(SaadaException.DB_ERROR, e);
		}
	}

	@Override
	public void remove(ArgsParser ap) throws SaadaException {
		try {
			this.remove();
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			FatalException.throwNewException(SaadaException.DB_ERROR, e);
		}
	} 

	@Override
	public void comment(ArgsParser ap) throws SaadaException {
		try {
			Messenger.printMsg(Messenger.TRACE, "Save description of relation " + this.relation_conf.getNameRelation());
			this.relation_conf.setDescription(ap.getComment());
			Table_Saada_Relation.saveDescription(this.relation_conf);
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			FatalException.throwNewException(SaadaException.DB_ERROR, e);
		}
	} 

	/**************************************
	 * Internal business
	 */

	/**
	 * @param ap
	 * @throws FatalException
	 */
	protected void buildConfig(ArgsParser ap) throws FatalException {
		String prim = ap.getFrom();
		int p_prim  = prim.lastIndexOf("_");
		String sec  = ap.getTo();
		int p_sec   = sec.lastIndexOf("_");
		this.relation_conf = new RelationConf(name
				, prim.substring(0, p_prim), prim.substring(p_prim+1) 
				, sec.substring(0, p_sec) , sec.substring(p_sec+1)
				, ap.getQuery()
				, ap.getQualifiers(), ap.getComment());
	}


	/**
	 * @return Returns the relation_conf.
	 */
	public RelationConf getRelation_conf() {
		return relation_conf;
	}

	/**
	 * Create the join table of the relation
	 * @param config
	 * @throws FatalException 
	 */
	protected void createTableRelation() throws FatalException {
		try {
			Database.getWrapper().createRelationshipTable(this.relation_conf);
		} catch (Exception e) {
			FatalException.throwNewException(SaadaException.DB_ERROR, e);
		}
	}


	/**
	 * Create a new relation
	 * @param name
	 * @param comment
	 * @throws AbortException 
	 * @throws AbortException 
	 */
	public void create() throws AbortException {
		if( Database.getCachemeta().getRelation(this.relation_conf.getNameRelation()) != null ) {
			AbortException.throwNewException(SaadaException.METADATA_ERROR, "Relation " + this.relation_conf.getNameRelation() + " already exists");
		}
		else {
			try {
				Messenger.setMaxProgress(3);
				this.processUserRequest();
				Messenger.incrementeProgress();
				this.createTableRelation();
				this.processUserRequest();
				Messenger.incrementeProgress();
				Table_Saada_Relation.addRelation(this.relation_conf);
				//GenerateClassAlgoRelationBySQL();
				this.processUserRequest();
				Messenger.incrementeProgress();
				Messenger.printMsg(Messenger.TRACE, "Empty relation < " + this.relation_conf.getNameRelation() + "> created");
				this.relation_conf.save();
			} catch(Exception e) {
				Messenger.printStackTrace(e);
				AbortException.throwNewException(SaadaException.DB_ERROR, e);
			}
		}
	}


	/**
	 * @throws Exception
	 */
	public void populateWithQuery() throws Exception {
		populateWithQuery(this.relation_conf.getQuery());
	}

	/**
	 * @throws Exception
	 */
	protected void populateWithQuery(String query) throws Exception {
		Messenger.printMsg(Messenger.TRACE, "Populate relation <" + name + ">");
		Messenger.setMaxProgress(3);
		String tempo_tbl;
		Database.getWrapper().suspendRelationTriggger(name);

		if( query != null && query.length() != 0 ) {
			/*
			 * Query in SaadaQL: must be translated in SQL before to be executed
			 */
			if( query.trim().startsWith("PrimaryFrom") )  {
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Translate SaadaQL correlator");
				tempo_tbl = "tempo_" + name;
				CorrQueryTranslator cqt = new CorrQueryTranslator(this, "PopulateRelation " + name + "\n" + query,tempo_tbl);
				cqt.parse();
				//query = cqt.buildQuery();
				String[] queries = cqt.buildQueries();

				SQLTable.createTemporaryTable(cqt.tempo_tbl, cqt.tempo_tbl_format, null, true);
				this.processUserRequest();
				Messenger.incrementeProgress();
				/*
				 * Build a temporary join table if there are constraints on attributes
				 * This table will be copied in the relation table if no neighbor constraint
				 * Otherwise it will be joined with that constraint result
				 */
				tempo_tbl = cqt.tempo_tbl;
				cqt.tempo_tbl = Database.getWrapper().getTempoTableName(cqt.tempo_tbl);
				if( queries != null ) {
					for( String q: queries ) {
						SQLTable.addQueryToTransaction(q, cqt.tempo_tbl, cqt.tables_to_lock.toArray(new String[0]));
					}
					/*
					 * Index can dramatically speed up the build-in of the final relationship table
					 * Index creation must be set into the current transaction otherwise the table is not visible 
					 * (tempo table created in the transaction)
					 */
					SQLTable.addQueryToTransaction("CREATE INDEX " + tempo_tbl.toLowerCase() + "_oidprimary ON " + cqt.tempo_tbl + "(oidprimary)");
					SQLTable.addQueryToTransaction("CREATE INDEX " + tempo_tbl.toLowerCase() + "_oidsecondary ON " + cqt.tempo_tbl + "(oidsecondary)");
				}
				this.processUserRequest();
				Messenger.incrementeProgress();
				/*
				 * A simple SQL query to compute correlations
				 */
				if( cqt.getNeighbour_detector() == null ) {
					Messenger.printMsg(Messenger.TRACE, "Populate the relationship with an SQL query");
					String iq = "INSERT INTO " + name + " (oidprimary, oidsecondary";
					for( String q: this.relation_conf.getQualifier().keySet()) {
						iq += ", " + q;
					}
					SQLTable.addQueryToTransaction(iq + ") SELECT * FROM " + cqt.tempo_tbl
							, name, new String[]{cqt.tempo_tbl});
					Messenger.printMsg(Messenger.TRACE, "Done");
				}
				/*
				 * we have neighbourhood constraints
				 */
				else {
					if (Messenger.debug_mode)
						Messenger.printMsg(Messenger.DEBUG, "Correlator using KDTree");
					/*
					 * Primary objects are stored within the KDTree
					 */
					this.processUserRequest();
					Messenger.incrementeProgress();
					Messenger.printMsg(Messenger.TRACE, "Build the KDTree");

					cqt.initNeighbour_detector();
					/*
					 * Build a temporary table with KDTree matches
					 */

					Messenger.printMsg(Messenger.TRACE, "Compute neighbourhood links");
					String table_name = cqt.computeNeighbourhoodLinks(name);

					String jquery = null;
					/*
					 * Join KDTree and SQL join if there a re constr on keyword or qualifier other than distance
					 */
					String[] alias_to_lock;
					if( queries != null ) {
						Messenger.printMsg(Messenger.TRACE, "Join KDTree and SaadaQL correlator");
						ArrayList<String> quals = Database.getCachemeta().getRelation(name).getQualifier_names();
						jquery = "";
						this.processUserRequest();
						Messenger.incrementeProgress();
						String target_cols = "(oidprimary, oidsecondary";
						for( String q: quals) {
							/*
							 * Distance qualifier is always taken from KDTree results
							 */
							if( q.equals("distance")) {
								jquery += ", k." + q ;
							}
							else {
								jquery += ", q." + q ;
							}
							target_cols += "," + q;
						}
						target_cols += ")";

						jquery = "INSERT INTO " + name + target_cols + " SELECT q.oidprimary,q.oidsecondary " + jquery;
						jquery += "\nFROM " +  cqt.tempo_tbl + " AS q, "
						+ table_name + " AS k\n"
						+ "WHERE q.oidprimary = k.oidprimary AND q.oidsecondary = k.oidsecondary";
						alias_to_lock = new String[]{tempo_tbl + " AS q", table_name + " AS k"};
						SQLTable.indexColumnOfTable(tempo_tbl, "oidprimary", this);
						SQLTable.indexColumnOfTable(tempo_tbl, "oidsecondary", this);
						Messenger.printMsg(Messenger.TRACE, "Done");
					}
					/*
					 * Otherwise, just copy the KDtree
					 */
					else  {
						Messenger.printMsg(Messenger.TRACE, "Copy KDTree into the DB");
						ArrayList<String> quals = Database.getCachemeta().getRelation(name).getQualifier_names();
						jquery = "";
						this.processUserRequest();
						Messenger.incrementeProgress();
						String target_cols = "(oidprimary, oidsecondary";
						for( String q: quals) {
							/*
							 * Distance qualifier is always taken from KDTree results
							 */
							if( q.equals("distance")) {
								jquery += ", k." + q ;
							}
							/*
							 * No query but position: not qualifer set but distance
							 */
							else {
								jquery += ",null" ;
							}
							target_cols += "," + q;
						}
						target_cols += ")";
						jquery = "INSERT INTO " + name + target_cols  + " SELECT k.oidprimary,k.oidsecondary " + jquery;
						jquery += "\nFROM " + table_name + " AS k\n";
						alias_to_lock = new String[]{table_name + " AS k"};
						Messenger.printMsg(Messenger.TRACE, "Done");
					}

					this.processUserRequest();
					Messenger.incrementeProgress();
					Messenger.printMsg(Messenger.TRACE, "Build the final relationship table");
					SQLTable.addQueryToTransaction(jquery,name, alias_to_lock);
					Messenger.printMsg(Messenger.TRACE, "Done");
				}
				this.processUserRequest();
				Messenger.incrementeProgress();
				Table_Saada_Relation.setIndexed(name, false);
			}
			/*
			 * SQL query
			 */
			else {
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Process SQL correlator");
				Messenger.incrementeProgress();
				SQLTable.addQueryToTransaction(query, name);
				Messenger.incrementeProgress();
				Table_Saada_Relation.setIndexed(name, false);
				Messenger.incrementeProgress();
			}		
			SQLTable.dropTmpTables();
		}
		Database.getWrapper().setClassColumns(name);
	}




	/**
	 * @param name
	 * @param class_id
	 * @throws Exception
	 */
	public final void removePrimaryClass(int class_id) throws Exception {
		SQLTable.dropTableIndex(name, null);
		SQLTable.addQueryToTransaction("DELETE FROM " + name + " WHERE " + SaadaOID.getSQLClassFilter("oidprimary") + " = " + class_id, name);	
		(new IndexBuilder(Repository.getIndexrelationsPath() + Database.getSepar(), name)).createIndexRelation();
	}


	/**
	 * @param sql_oidlist
	 * @throws Exception
	 */
	public final void removePrimaryKeys(String sql_oidlist) throws Exception {
		SQLTable.dropTableIndex(name, null);
		SQLTable.addQueryToTransaction("DELETE FROM " + name + " WHERE oidprimary IN  " + sql_oidlist, name);	
		(new IndexBuilder(Repository.getIndexrelationsPath() + Database.getSepar(), name)).createIndexRelation();
	}


	/**
	 * @param name
	 * @param class_id
	 * @throws Exception
	 */
	public final void removeSecondaryClass(int class_id) throws Exception {
		SQLTable.dropTableIndex(name, null);
		SQLTable.addQueryToTransaction("DELETE FROM " + name + " WHERE " + SaadaOID.getSQLClassFilter("oidsecondary") + " = " + class_id, name);	
		(new IndexBuilder(Repository.getIndexrelationsPath() + Database.getSepar(), name)).createIndexRelation();
	}


	/**
	 * @param sql_oidlist
	 * @throws Exception
	 */
	public final void removeSecondaryKeys(String sql_oidlist) throws Exception {
		SQLTable.dropTableIndex(name, null);
		SQLTable.addQueryToTransaction("DELETE FROM " + name + " WHERE oidsecondary IN  " + sql_oidlist, name);	
		(new IndexBuilder(Repository.getIndexrelationsPath() + Database.getSepar(), name)).createIndexRelation();
	}


	/**
	 * @throws Exception
	 */
	public  void empty() throws Exception {
		Messenger.printMsg(Messenger.TRACE, "Empty Relationship <" + name + ">");
		SQLTable.dropTableIndex(name, null);
		SQLTable.addQueryToTransaction("DELETE FROM " + name, name);
		Table_Saada_Relation.setIndexed(name, false);
		IndexBuilder ib = new IndexBuilder(Repository.getIndexrelationsPath() + Database.getSepar(), name);
		ib.createIndexRelation();		
	}

	/**
	 * Removee the relation
	 * @param name
	 * @param comment
	 * @throws Exception 
	 * @throws AbortException 
	 * @throws AbortException 
	 */
	public void remove() throws Exception {
		Messenger.setMaxProgress(12);
		//		/*
		//		 * Exception must not  stop the process of removing the relation
		//		 */
		//		try {
		//			this.empty();
		//		}
		//		catch(Exception e) {
		//			Messenger.printMsg(Messenger.ERROR, e.getMessage());
		//		}
		SQLTable.addQueryToTransaction("drop table  if exists " + name , name);
		ClassRemover.remove(name + "_correlator");
		this.processUserRequest();
		Messenger.incrementeProgress();
		this.processUserRequest();
		Messenger.incrementeProgress();
		ClassRemover.remove(name);
		Table_Saada_Relation.removeRelation(name);
		this.processUserRequest();
		Messenger.incrementeProgress();
		String index_path = Repository.getIndexrelationsPath();
		File index_dir = new File(index_path);
		String[] indfiles = index_dir.list();
		for( int i=0 ; i<indfiles.length ; i++ ) {
			if( indfiles[i].startsWith(name + ".") ) {
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Remove index file <" +indfiles[i] + ">" );
				(new File(index_path + separ + indfiles[i])).delete();
			}
			this.processUserRequest();
			Messenger.incrementeProgress();
		}
		Messenger.printMsg(Messenger.TRACE, "Relation <" + name + "> removed");
	}





}
