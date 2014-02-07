/**
 * 
 */
package upgrade.collection;
import saadadb.collection.Category;
import saadadb.database.Database;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.generationclass.GenerationClassCollection;
import saadadb.generationclass.SaadaClassReloader;
import saadadb.meta.AttributeHandler;
import saadadb.sqltable.SQLTable;
import saadadb.sqltable.Table_Saada_Class;
import saadadb.sqltable.Table_Saada_Collection;
import saadadb.sqltable.Table_Saada_Metacoll;
import saadadb.sqltable.Table_Saada_Relation;
import saadadb.util.Messenger;

/**
 * @author michel
 * @version $Id$
 *
 */
public class Upgrade {

	/**
	 * @param catnum
	 * @param ah
	 * @throws Exception
	 */
	public static void addColumnToCollection(int catnum, AttributeHandler ah) throws Exception{
		String category = Category.explain(catnum);
		// compile the new class
		SaadaClassReloader.reloadGeneratedClass(GenerationClassCollection.Generation(Database.getConnector(), catnum));
		// update the metacolll table
		try {
			Table_Saada_Metacoll.addAttributeForCategory(catnum, ah);
		} catch( QueryException e){};
		// update data tables
		for( String cn: Database.getCachemeta().getCollection_names()) {
			String ct = Database.getWrapper().getCollectionTableName(cn, catnum);
			try {
				if( !SQLTable.hasColumn(ct, ah.getNameattr())) {
					SQLTable.addQueryToTransaction(Database.getWrapper().addColumn(ct, ah.getNameattr(), Database.getWrapper().getSQLTypeFromJava(ah.getType())), ct);
				}
			} catch (Exception e) {
				Messenger.printStackTrace(e);
				SQLTable.abortTransaction();
				AbortException.throwNewException(SaadaException.DB_ERROR, e);
			}
		}
		Messenger.printMsg(Messenger.TRACE, "Attribute " + ah + " added to category " + category);

	}

	public static void upgrade() throws Exception {
		Table_Saada_Relation.addStatColumn();
		Table_Saada_Class.addStatColumn();
		Table_Saada_Collection.addStatColumn();		
		AttributeHandler ah = new AttributeHandler();
		ah.setNameattr("healpix_csa");
		ah.setNameorg("healpix_csa");
		ah.setType("long");
		ah.setUcd("pos.healpix");
		for( int cat: new int[]{Category.ENTRY, Category.SPECTRUM, Category.IMAGE}){
			Messenger.printMsg(Messenger.TRACE, "Add column " + ah + " to category " +  Category.explain(cat));
			SQLTable.beginTransaction();
			addColumnToCollection(cat, ah);
			SQLTable.commitTransaction();
		}
		for( int cat: new int[]{Category.ENTRY, Category.SPECTRUM, Category.IMAGE}){
			for( String cn: Database.getCachemeta().getCollection_names()) {
				HealpixSetter healpixSetter = new HealpixSetter(Database.getWrapper().getCollectionTableName(cn, cat));
				healpixSetter.set();
			}
		}
		Messenger.printMsg(Messenger.TRACE, "Update complete");
	}

}
