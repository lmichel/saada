package saadadb.configuration;

import saadadb.collection.Category;
import saadadb.command.ArgsParser;
import saadadb.command.EntityManager;
import saadadb.database.Database;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.generationclass.GenerationClassCollection;
import saadadb.generationclass.SaadaClassReloader;
import saadadb.meta.AttributeHandler;
import saadadb.meta.MetaCollection;
import saadadb.sqltable.SQLTable;
import saadadb.sqltable.Table_Saada_Metacoll;
import saadadb.util.JavaTypeUtility;
import saadadb.util.Messenger;
import saadadb.util.RegExp;

/**
 * Manager adding a new extended attribute to a given category
 * @author michel
 * @version $Id$
 *
 */
public class ExtendAttributeManager extends EntityManager {

	@Override
	public void create(ArgsParser ap) throws SaadaException {
		String category = ap.getCategory();
		int catnum = Category.getCategory(category);
		String name = ap.getCreate();
		String type = ap.getType();
		String description = ap.getComment();
		if ( name == null || !name.matches( RegExp.EXTATTRIBUTE)) {
			FatalException.throwNewException(SaadaException.WRONG_PARAMETER, name + ": Value not allowed for attribute name, must match " + RegExp.EXTATTRIBUTE );			
		}
		if( !JavaTypeUtility.isSupportedForExtAtt(type)) {
			FatalException.throwNewException(SaadaException.WRONG_PARAMETER, "Type " + type + " not suported for exented attributes");						
		}
		if( MetaCollection.attributeExistIn(name, catnum) ) {
			FatalException.throwNewException(SaadaException.WRONG_PARAMETER, "Attribute " + name + " already exists in category " + category);			
		}
		/*
		 * Attribute handler of the new column
		 */
		AttributeHandler ah = new AttributeHandler();
		ah.setNameattr(name);
		ah.setNameorg(name);
		ah.setType(type);
		ah.setComment(description);
		Messenger.printMsg(Messenger.TRACE, "Add extended attribute " + ah + " to category " + category);

		/*
		 * Start to update the category schema:
		 */
		try {
			/*
			 * Added to the collection attribute handlers to the xml file
			 */
			CollectionAttributeExtend cae = new CollectionAttributeExtend(Database.getRoot_dir());
			cae.addAttributeExtend(category, ah);
			cae.save();
			// compile the new class
			SaadaClassReloader.reloadGeneratedClass(GenerationClassCollection.Generation(Database.getConnector(), catnum));
			// update the metacolll table
			Table_Saada_Metacoll.addAttributeForCategory(catnum, ah);
		} catch(SaadaException ce) {
			FatalException.throwNewException(SaadaException.INTERNAL_ERROR, ce);
		} catch(Exception ce) {
			Messenger.printStackTrace(ce);
			FatalException.throwNewException(SaadaException.INTERNAL_ERROR, ce);
		}
		// update data tables
		for( String cn: Database.getCachemeta().getCollection_names()) {
			String ct = Database.getWrapper().getCollectionTableName(cn, catnum);
			try {
				SQLTable.addQueryToTransaction(Database.getWrapper().addColumn(ct, name, Database.getWrapper().getSQLTypeFromJava(type)), ct);
			} catch (Exception e) {
				Messenger.printStackTrace(e);
				SQLTable.abortTransaction();
				AbortException.throwNewException(SaadaException.DB_ERROR, e);
			}
		}
		Messenger.printMsg(Messenger.TRACE, "Attribute " + ah + " added to category " + category);
		/*
		 * At this point, the column should have been added.
		 */
	}

	
	@Override
	public void rename(ArgsParser ap) throws SaadaException {
		//TODO change also the type
		if( !Database.getWrapper().supportAlterColumn() ){
			QueryException.throwNewException(SaadaException.UNSUPPORTED_OPERATION, Database.getWrapper().getDBMS() + " does not support to alter table columns");
		}
		String category = ap.getCategory();
		int catnum = Category.getCategory(category);
		String oldName = ap.getRename();
		String newName = ap.getNewname();
		if ( oldName == null || !oldName.matches( RegExp.EXTATTRIBUTE)) {
			FatalException.throwNewException(SaadaException.WRONG_PARAMETER, oldName + ": Value not allowed for attribute name, must match " + RegExp.EXTATTRIBUTE );			
		}
		if ( newName == null || !newName.matches( RegExp.EXTATTRIBUTE)) {
			FatalException.throwNewException(SaadaException.WRONG_PARAMETER, newName + ": Value not allowed for attribute name, must match " + RegExp.EXTATTRIBUTE );			
		}
		if( !MetaCollection.attributeExistIn(oldName, catnum) ) {
			FatalException.throwNewException(SaadaException.WRONG_PARAMETER, "Attribute " + oldName + " does not exists in category " + category);			
		}
		
		/*
		 * Attribute handler of the old/new column
		 */
		AttributeHandler oldah = new AttributeHandler();
		oldah.setNameattr(oldName);
		oldah.setNameorg(oldName);
		AttributeHandler newah = new AttributeHandler();
		newah.setNameattr(newName);
		newah.setNameorg(newName);
		newah.setType(MetaCollection.getAttribute_handlers_flatfile().get(oldName).getType());
		
		Messenger.printMsg(Messenger.TRACE, "Rename extended attribute " + oldah + " of category " + category + " to " + newah);

		/*
		 * Start to update the category schema:
		 */
		try {
			/*
			 * Remove to the collection attribute handlers to the xml file
			 */
			CollectionAttributeExtend cae = new CollectionAttributeExtend(Database.getRoot_dir());
			cae.removeAttributeExtend(category, oldah);
			cae.save();
			// compile the new class
			SaadaClassReloader.reloadGeneratedClass(GenerationClassCollection.Generation(Database.getConnector(), catnum));
			cae = new CollectionAttributeExtend(Database.getRoot_dir());
			cae.addAttributeExtend(category, newah);
			cae.save();
			// compile the new class
			SaadaClassReloader.reloadGeneratedClass(GenerationClassCollection.Generation(Database.getConnector(), catnum));
			// update the metacolll table
			Table_Saada_Metacoll.renameAttributeForCategory(catnum, oldah, newah);			
		} catch(SaadaException ce) {
			FatalException.throwNewException(SaadaException.INTERNAL_ERROR, ce);
		} catch(Exception ce) {
			Messenger.printStackTrace(ce);
			FatalException.throwNewException(SaadaException.INTERNAL_ERROR, ce);
		}
		// update data tables
		for( String cn: Database.getCachemeta().getCollection_names()) {
			String ct = Database.getWrapper().getCollectionTableName(cn, catnum);
			try {
				SQLTable.addQueryToTransaction(Database.getWrapper().renameColumn(ct, oldName, newName), ct);
			} catch (Exception e) {
				Messenger.printStackTrace(e);
				SQLTable.abortTransaction();
				AbortException.throwNewException(SaadaException.DB_ERROR, e);
			}
		}
		Messenger.printMsg(Messenger.TRACE, "Attribute " + oldah + " from category " + category + " renamed to " + newah.getNameattr());
	}

	@Override
	public void empty(ArgsParser ap) throws SaadaException {
		Messenger.printMsg(Messenger.ERROR, "Not implemented for extended attributes");
	}

	@Override
	public void remove(ArgsParser ap) throws SaadaException {
		if( !Database.getWrapper().supportAlterColumn() ){
			QueryException.throwNewException(SaadaException.UNSUPPORTED_OPERATION, Database.getWrapper().getDBMS() + " does not support to alter table columns");
		}
		String category = ap.getCategory();
		int catnum = Category.getCategory(category);
		String name = ap.getRemove();
		if( !MetaCollection.attributeExistIn(name, catnum) ) {
			FatalException.throwNewException(SaadaException.WRONG_PARAMETER, "Attribute " + name + " does not exists in category " + category);			
		}
		/*
		 * Attribute handler of the new column
		 */
		AttributeHandler ah = new AttributeHandler();
		ah.setNameattr(name);
		ah.setNameorg(name);
		Messenger.printMsg(Messenger.TRACE, "Add extended attribute " + ah + " to category " + category);
		/*
		 * Start to update the category schema:
		 */
		try {
			/*
			 * Remove to the collection attribute handlers to the xml file
			 */
			CollectionAttributeExtend cae = new CollectionAttributeExtend(Database.getRoot_dir());
			cae.removeAttributeExtend(category, ah);
			cae.save();
			// compile the new class
			SaadaClassReloader.reloadGeneratedClass(GenerationClassCollection.Generation(Database.getConnector(), catnum));
			// update the metacolll table
			Table_Saada_Metacoll.removeAttributeForCategory(catnum, ah);
		} catch(SaadaException ce) {
			FatalException.throwNewException(SaadaException.INTERNAL_ERROR, ce);
		} catch(Exception ce) {
			Messenger.printStackTrace(ce);
			FatalException.throwNewException(SaadaException.INTERNAL_ERROR, ce);
		}
		// update data tables
		for( String cn: Database.getCachemeta().getCollection_names()) {
			String ct = Database.getWrapper().getCollectionTableName(cn, catnum);
			try {
				SQLTable.addQueryToTransaction(Database.getWrapper().dropColumn(ct, name), ct);
			} catch (Exception e) {
				Messenger.printStackTrace(e);
				SQLTable.abortTransaction();
				AbortException.throwNewException(SaadaException.DB_ERROR, e);
			}
		}
		Messenger.printMsg(Messenger.TRACE, "Attribute " + ah + " removed from category " + category);
		/*
		 * At this point, the column should have been added.
		 */	}

	@Override
	public void populate(ArgsParser ap) throws SaadaException {
		Messenger.printMsg(Messenger.ERROR, "Not implemented for extended attributes");
	}

	@Override
	public void index(ArgsParser ap) throws SaadaException {
		Messenger.printMsg(Messenger.ERROR, "Not implemented for extended attributes");
	}

	@Override
	public void comment(ArgsParser ap) throws SaadaException {
		Messenger.printMsg(Messenger.ERROR, "Not implemented for extended attributes");
	}
}
