package saadadb.collection;

import java.io.File;

import saadadb.command.ArgsParser;
import saadadb.command.EntityManager;
import saadadb.database.Database;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.generationclass.GenerationClassProduct;
import saadadb.meta.VocabularyValidator;
import saadadb.relationship.RelationManager;
import saadadb.sqltable.SQLTable;
import saadadb.sqltable.Table_Saada_Class;
import saadadb.sqltable.Table_Saada_Loaded_File;
import saadadb.sqltable.Table_Saada_Metaclass;
import saadadb.util.Messenger;

public class ClassManager extends EntityManager{

	/**
	 * Although all method could be static, we need to make an instance to monitor the progress from 
	 * the GUI
	 */
	public ClassManager(String classe) {
		super(classe);
	}
	public ClassManager() {
		super();
	}


	@Override
	public void comment(ArgsParser ap) throws SaadaException {
		this.comment(ap.getComment());
	}


	@Override
	public void create(ArgsParser ap) throws SaadaException {
		Messenger.printMsg(Messenger.ERROR, "Not implemented for classes");
	}
	
	@Override
	public void rename(ArgsParser ap) throws SaadaException {
		/*
		 * New name is given be -name=.. parameter which returns a string array
		 */
		String newName = ap.getNameComponents()[0];	
		StringBuffer report = new StringBuffer();
		if( !Database.getCachemeta().classExists(name) ) {
			AbortException.throwNewException(SaadaException.WRONG_PARAMETER, "Class " + name + " does not exist");
		}
		if( !VocabularyValidator.checkClassOrRelationName(newName, report)) {
			AbortException.throwNewException(SaadaException.WRONG_PARAMETER, report.toString());
		}
		//SQLTable.beginTransaction();
		Table_Saada_Class.renameClass(name, newName);
		Table_Saada_Loaded_File.renameClass(name, newName);
		Table_Saada_Metaclass.renameClass(name, newName);
		if( !Database.getWrapper().supportDropTableInTransaction() ) {
			SQLTable.commitTransaction();
			SQLTable.beginTransaction();
		}
		Database.getWrapper().renameTable(name, newName);
		try {
			GenerationClassProduct.renameJavaClass(Database.getRoot_dir() + "/class_mapping" , name, newName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//SQLTable.commitTransaction();
	}	

	@Override
	public void empty(ArgsParser ap) throws SaadaException {	
		try { 
			String eclasse = Database.getCachemeta().getClass(name).getAssociate_class();
			if( eclasse != null && eclasse.length() > 0 ) {
				(new ClassManager(eclasse)).empty(null);
			}
			this.empty();
		} catch (Exception e) {
			FatalException.throwNewException(SaadaException.DB_ERROR, e);
		}

	}


	@Override
	public void index(ArgsParser ap) throws SaadaException {
		Messenger.printMsg(Messenger.ERROR, "Not implemented for classes");
	}


	@Override
	public void populate(ArgsParser ap) throws SaadaException {
		Messenger.printMsg(Messenger.ERROR, "Not implemented for classes");
	}


	@Override
	public void remove(ArgsParser ap) throws SaadaException {
		try { 
			String eclasse = Database.getCachemeta().getClass(name).getAssociate_class();
			if( eclasse != null && eclasse.length() > 0 ) {
				ClassManager cm = new ClassManager(eclasse);
				cm.empty();
				cm.remove();
			}
			this.empty();
			this.remove();
		} catch (Exception e) {
			FatalException.throwNewException(SaadaException.DB_ERROR, e);
		}

	}
	/**
	 * @throws Exception
	 */
	protected final void remove()throws Exception {
		Messenger.printMsg(Messenger.TRACE, "Remove class <" + name + ">");
		String mapping_dir = Database.getRoot_dir() + Database.getSepar() + "class_mapping" + Database.getSepar();
		this.processUserRequest();
		/*
		 * drop class table
		 */
		this.processUserRequest();
		if( Database.getWrapper().tableExist(name)) {
			SQLTable.addQueryToTransaction("DROP TABLE " + name, name);
		}
		/*
		 * remove meta-data
		 */
		this.processUserRequest();
		Table_Saada_Class.removeClass(name);
		SaadaOID.resetCounter(name);
		try {
			Table_Saada_Metaclass.removeClass(name);
		}catch(Exception e ) {
			Messenger.printMsg(Messenger.ERROR, e.getLocalizedMessage());
		}
		this.processUserRequest();
		(new File(mapping_dir + name + ".java")).delete();
		(new File(mapping_dir + "generated" 
				+ Database.getSepar() + Database.getName() 
				+ Database.getSepar() + name + ".class")).delete();
	}
	
	/**
	 * @param comment
	 * @throws FatalException
	 */
	protected void comment(String comment) throws FatalException {
		if( !Database.getCachemeta().classExists(this.name)) 
		{
			FatalException.throwNewException(SaadaException.METADATA_ERROR, "this.name " + this.name + " does not exist");
		}
		else 
		{
			try 
			{
				SQLTable.addQueryToTransaction("UPDATE saada_class SET description = '" + comment.replaceAll("'", " ") + "' WHERE name = '" + this.name + "'");
			} 
			catch (AbortException e) 
			{
				e.printStackTrace();
				FatalException.throwNewException(SaadaException.DB_ERROR, e);
			}
		}
	}

	/**
	 * @param in_transaction
	 * @param reload_cache
	 * @throws Exception
	 */
	protected final void empty()throws Exception {
		try {
			int class_id = Database.getCachemeta().getClass(name).getId();
			int category            = Database.getCachemeta().getClass(name).getCategory();
			String coll             = Database.getCachemeta().getClass(name).getCollection_name();
			String coll_table       = Database.getCachemeta().getCollectionTableName(coll, category);
			String[] end_rel        = Database.getCachemeta().getRelationNamesEndingOnColl(coll, category);
			String[] start_rel      = Database.getCachemeta().getRelationNamesStartingFromColl(coll, category);
			Messenger.printMsg(Messenger.TRACE, "Empty class <" + name + ">");
			this.processUserRequest();

			/*
			 * Update relationship first
			 */
			Messenger.printMsg(Messenger.TRACE, "Update relationships");
			for( String rel: start_rel) {
				this.processUserRequest();
				Messenger.printMsg(Messenger.TRACE, "Relationships <" + rel + ">");
				(new RelationManager(rel)).removePrimaryClass(class_id);
			}
			for( String rel: end_rel) {
				this.processUserRequest();
				Messenger.printMsg(Messenger.TRACE, "Relationships <" + rel + ">");
				(new RelationManager(rel)).removeSecondaryClass(class_id);
			}
			/*
			 * Below tests on table existence make the empty function working even on an altered DB
			 * with the hope it is no longer altered after...
			 */
			/*
			 * and the collection level data
			 */
			Messenger.printMsg(Messenger.TRACE, "clean up collection table");
			if( Database.getWrapper().tableExist(name)) {
				this.processUserRequest();
				SQLTable.dropTableIndex(coll_table, null);
				SQLTable.addQueryToTransaction("DELETE FROM " + coll_table +  " WHERE " + SaadaOID.getSQLClassFilter("oidsaada") + " = " + class_id, coll_table);
			}
		}catch (Exception e) {
			Messenger.printMsg(Messenger.ERROR, e.getLocalizedMessage());
		}
		/*
		 * remove class level data first
		 */
		Messenger.printMsg(Messenger.TRACE, "clean up class table");
		if( Database.getWrapper().tableExist(name)) {
			this.processUserRequest();
			SQLTable.dropTableIndex(name, null);
			SQLTable.addQueryToTransaction("DELETE FROM  " + name , name);
		}
		/*
		 * Remove data files from the repository
		 */
		//		if( category != Category.ENTRY ) {
		this.processUserRequest();
		Messenger.printMsg(Messenger.TRACE, "Remove data products from the repository");
		RepositoryManager.emptyClass(name);
		SQLTable.addQueryToTransaction("DELETE FROM saada_loaded_file WHERE classname = '" + name + "'", "saada_loaded_file");

		//		}
	}




}
