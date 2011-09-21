package saadadb.collection;

import saadadb.command.ArgsParser;
import saadadb.command.EntityManager;
import saadadb.database.Database;
import saadadb.database.Repository;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.relationship.RelationManager;
import saadadb.sqltable.SQLTable;
import saadadb.sqltable.Table_Saada_Class;
import saadadb.sqltable.Table_Saada_Collection;
import saadadb.sqltable.Table_Saada_Loaded_File;
import saadadb.util.Messenger;

public class CollectionManager extends EntityManager {

	/***********************************
	 * Inherited abstract methods
	 */

	public CollectionManager() {
		super();
	}
	public CollectionManager(String name) {
		super(name);
	}

	@Override
	public void create(ArgsParser ap) throws FatalException {
		this.create(ap.getComment());
	}


	@Override
	public void empty(ArgsParser ap) throws SaadaException {
		try {
			String[] classes;
			this.processUserRequest();
			

			for( int cat=1 ; cat<Category.NB_CAT ; cat++ ) {
				if(  ap == null || ap.getCategory() == null 
						|| (Category.getCategory(ap.getCategory()) == cat && cat != Category.ENTRY)
						|| (Category.getCategory(ap.getCategory()) == Category.TABLE && cat == Category.ENTRY) ) {
					Messenger.printMsg(Messenger.TRACE, "Empty category " + Category.explain(cat));
					/*
					 * Empty relationships
					 */
					this.emptyRelations(cat);
					/*
					 * remove products
					 */
					Table_Saada_Loaded_File.removeLoadedFiles(this.name, cat);
					RepositoryManager.emptyCategory(this.name, cat);
					/*
					 * Remove collection level data
					 */
					this.removeCollectionLevelData(cat);
					/*
					 * Remove classes
					 */
					classes = Table_Saada_Class.getClassNamesForCollection(name, cat);
					for( String classe: classes) {
						ClassManager cm = new ClassManager(classe);
						cm.remove();
					}
				}
			}
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			FatalException.throwNewException(SaadaException.DB_ERROR, e);
		}
	}


	@Override
	public void index(ArgsParser ap) throws SaadaException {
		Messenger.printMsg(Messenger.ERROR, "Not implemented for collections");
	}


	@Override
	public void populate(ArgsParser ap)  {
		Messenger.printMsg(Messenger.ERROR, "Not implemented for collections");
	}


	@Override
	public void remove(ArgsParser ap) throws SaadaException {
		try {
			Messenger.printMsg(Messenger.TRACE, "Remove collection " + this.name);
			Table_Saada_Collection.dropCollection(this.name);
			this.removeRelations();	
			for( int cat=1 ; cat<Category.NB_CAT ; cat++ ) {
				/*
				 * remove products
				 */
				Table_Saada_Loaded_File.removeLoadedFiles(this.name, cat);
				RepositoryManager.emptyCategory(this.name, cat);
				/*
				 * Remove collection level data
				 */
				this.removeCollectionLevelData(cat);
				/*
				 * Remove classes
				 */
				for( String classe: Table_Saada_Class.getClassNamesForCollection(name, cat)) {
					ClassManager cm = new ClassManager(classe);
					cm.remove();
				}
				Repository.removeCollectionDir(this.name);
			}
			this.removeCategoryTables();

		} catch (Exception e) {
			Messenger.printStackTrace(e);
			FatalException.throwNewException(SaadaException.DB_ERROR, e);
		}

	}

	@Override
	public void comment(ArgsParser ap) throws SaadaException {
		this.comment(ap.getComment());

	}

	/**************************************
	 * Internal business
	 */


	/**
	 * @param category
	 * @throws Exception
	 */
	protected void emptyRelations(int category) throws Exception {
		Messenger.printMsg(Messenger.TRACE, "Start to empty relationships connected with category" + Category.explain(category) + " in collecton <" + this.name + ">");
		String[] end_rel        = Database.getCachemeta().getRelationNamesEndingOnColl(this.name, category);
		String[] start_rel      = Database.getCachemeta().getRelationNamesStartingFromColl(this.name, category);
		/*
		 * Flush first the relationships otherwise  they will be re-indexed for each removed class
		 */
		for( String rel: start_rel) {
			this.processUserRequest();
			Messenger.printMsg(Messenger.TRACE, "Empty relationship <" + rel + ">");
			(new RelationManager(rel)).empty();
		}
		for( String rel: end_rel) {
			this.processUserRequest();
			Messenger.printMsg(Messenger.TRACE, "Empty relationship <" + rel + ">");
			(new RelationManager(rel)).empty();
		}
	}

	/**
	 * @param category
	 * @throws AbortException
	 * @throws FatalException
	 */
	protected void removeCollectionLevelData(int category) throws AbortException, FatalException {
		Messenger.printMsg(Messenger.TRACE, "Remove data at collection level for category " + Category.explain(category));
		String table =  Database.getCachemeta().getCollectionTableName(this.name, category);
		SQLTable.dropTableIndex(table, this);
		SQLTable.addQueryToTransaction("DELETE FROM " + table);			
	}


	/**
	 * @param comment
	 * @throws FatalException
	 */
	protected void comment(String comment) throws FatalException {
		if( !Database.getCachemeta().collectionExists(this.name)  ) {
			FatalException.throwNewException(SaadaException.METADATA_ERROR, "this.name " + this.name + " does not exist");
		}
		else {
			try {
				SQLTable.addQueryToTransaction("UPDATE saada_this.name SET description = '" + comment + "' WHERE name = '" + this.name + "'");
			} catch (AbortException e) {
				e.printStackTrace();
				FatalException.throwNewException(SaadaException.DB_ERROR, e);
			}
		}
	}

	/**
	 * @param comment
	 * @throws FatalException
	 */
	protected void create(String comment) throws FatalException  {
		if( Database.getCachemeta().collectionExists(this.name)  ) {
			FatalException.throwNewException(SaadaException.WRONG_PARAMETER, "Collection " + this.name + " already exists");
		}
		else if( !this.name.matches("[_a-zA-Z0-9]+") ) {
			FatalException.throwNewException(SaadaException.WRONG_PARAMETER, "Collection  name con only contain letters, digitd or '_'");
		}
		else {
			Messenger.setMaxProgress(2 + Category.NB_CAT);
			Messenger.printMsg(Messenger.TRACE, "Create " + this.name);
			String cc;
			if( comment == null ) {
				cc = "";
			}
			else {
				cc = comment.replaceAll("['\"]", " ");
			}
			Table_Saada_Collection.addCollection(this.name, cc);
			Messenger.incrementeProgress();
			Repository.createSubdirsForCollection(this.name);
			Messenger.incrementeProgress();
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Collection " + this.name + " is created ");				
		}
	}


	/**
	 * @throws Exception
	 */
	protected void removeRelations() throws Exception {
		for( int i=1 ; i<Category.NB_CAT ; i++ ) {
			String category = Category.explain(i).toLowerCase();
			String[] end_rel        = Database.getCachemeta().getRelationNamesEndingOnColl(this.name, i);
			String[] start_rel      = Database.getCachemeta().getRelationNamesStartingFromColl(this.name, i);
			for( String rel: end_rel) {
				this.processUserRequest();
				Messenger.printMsg(Messenger.TRACE, "Remove relationship <" + rel + ">");
				try {
					(new RelationManager(rel)).remove();
				} catch(Exception e) {Messenger.printMsg(Messenger.ERROR, e.toString());}
			}
			for( String rel: start_rel) {
				this.processUserRequest();
				Messenger.printMsg(Messenger.TRACE, "Remove relationship <" + rel + ">");
				try {
					(new RelationManager(rel)).remove();
				} catch(Exception e) {Messenger.printMsg(Messenger.ERROR, e.toString());}
			}
			this.processUserRequest();
//			Messenger.printMsg(Messenger.TRACE, "Remove " + category + " super class");
//			String table_name = Database.getCachemeta().getCollectionTableName(this.name, i);
//			SQLTable.addQueryToTransaction("DROP TABLE " + table_name, table_name);	
		}
	}
	/**
	 * @throws Exception
	 */
	protected void removeCategoryTables() throws Exception {
		for( int i=1 ; i<Category.NB_CAT ; i++ ) {
			String category = Category.explain(i).toLowerCase();
			this.processUserRequest();
			Messenger.printMsg(Messenger.TRACE, "Remove " + category + " super class");
			String table_name = Database.getCachemeta().getCollectionTableName(this.name, i);
			if( Database.getWrapper().tableExist(table_name)) {
				SQLTable.addQueryToTransaction("DROP TABLE " + table_name);	
			}
			table_name = "saada_metaclass_" + category;
			SQLTable.addQueryToTransaction("DELETE FROM " + table_name + " WHERE name_coll = '" + this.name + "'", table_name);					
		}
	}
}



