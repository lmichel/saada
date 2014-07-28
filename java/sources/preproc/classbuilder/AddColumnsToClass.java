package preproc.classbuilder;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import saadadb.collection.Category;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.generationclass.GenerationClassProduct;
import saadadb.meta.AttributeHandler;
import saadadb.meta.MetaClass;
import saadadb.sqltable.SQLTable;
import saadadb.sqltable.Table_Saada_Metacat;
import saadadb.util.ChangeKey;
import saadadb.util.Messenger;

public class AddColumnsToClass {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ArgsParser arg;
		try {
			arg = new ArgsParser(args);
			Database.init(arg.getDBName());
			/*
			 * Check parameters
			 */
			String classeToBeModified = arg.getClassName();
			MetaClass  mc = Database.getCachemeta().getClass(classeToBeModified);

			String[] colDef = arg.getColdef();
			if(colDef == null || colDef.length != 2) {
				Messenger.printMsg(Messenger.ERROR, "Wrong column definition");	
				System.exit(1);
			}
			String ahName = colDef[0];
			String ahType = colDef[1];
			/*
			 * Rise an exception if wrong type
			 */
			Database.getWrapper().getSQLTypeFromJava(ahType);
			/*
			 * Build the handler of the new attribute
			 */
			AttributeHandler nah = new AttributeHandler();
			nah.setNameorg(ahName);
			nah.setNameattr(ChangeKey.changeKey(ahName));
			nah.setType(ahType);
			nah.setComment("Added by a Saada utility");
			boolean modified = false;
			AttributeHandler oah = mc.getAttributes_handlers().get(nah.getNameattr());
			/*
			 * new attribute: insert a new column
			 */
			if( oah == null ) {
				SQLTable.beginTransaction();
				modified = true;
				Messenger.printMsg(Messenger.TRACE, "Column <" + nah.getNameattr() + "> of type \"" + nah.getType() + "\" added to table/class <" + mc.getName() + "> ");
//				SQLTable.addQueryToTransaction(Database.getWrapper().changeColumnType(mc.getName(), nah.getNameattr(), Database.getWrapper().getSQLTypeFromJava(nah.getType()))
//						, mc.getName());
			}
			/*
			 * Attribute exist but type has been changed
			 */
			else if( oah.getType().equals(nah.getType()) == false ) {
				SQLTable.beginTransaction();
				modified = true;
				Messenger.printMsg(Messenger.TRACE, "Column <" + nah.getNameattr() + "> of table/class <" + mc.getName() + "> converted from \"" + oah.getType() + "\" to \"" + nah.getType() + "\"");
//				SQLTable.addQueryToTransaction(Database.getWrapper().changeColumnType(mc.getName(), nah.getNameattr(), Database.getWrapper().getSQLTypeFromJava(nah.getType()))
//						, mc.getName());

			}
			if( modified ) {
				Messenger.printMsg(Messenger.TRACE, "Update meta data tables");
				LinkedHashMap<String, AttributeHandler> ahmap = new LinkedHashMap<String, AttributeHandler>();
				ahmap.putAll(mc.getAttributes_handlers());
				ahmap.put(nah.getNameattr(), nah);
				(new Table_Saada_Metacat(mc.getName()
						, mc.getCollection_name()
						, mc.getCategory()
						, new ArrayList<AttributeHandler>(ahmap.values()))).updateUCDTable(mc.getId());
				GenerationClassProduct.buildJavaClass(ahmap
						,  Database.getRoot_dir() + Database.getSepar()+ "class_mapping"
						, mc.getName()
						, Category.explain(mc.getCategory()) + "UserColl");
				SQLTable.commitTransaction();
			}


		} catch (Exception e) {
			SQLTable.abortTransaction();
			Messenger.printStackTrace(e);
		}
		Database.close();

	}

}
