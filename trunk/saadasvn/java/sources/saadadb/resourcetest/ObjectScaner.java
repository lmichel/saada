package saadadb.resourcetest;

import java.sql.ResultSet;

import saadadb.collection.Category;
import saadadb.collection.SaadaInstance;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.sqltable.SQLQuery;
import saadadb.util.Messenger;

/**
 * @author michel
 * * @version $Id$

 */
public class ObjectScaner {
	static private boolean verbose = false;


	/**
	 * 
	 */
	private static void usage() {
		System.out.println("USAGE: java ObjectScaner [-v] [db_name]");
		System.out.println("    -v: verbose mode");
		System.exit(1);
	}
	/**
	 * @param args
	 * @throws FatalException 
	 */
	public static void main (String[] args) throws Exception {
		ArgsParser ap;
		try {
			ap = new ArgsParser(args);
			Database.init(ap.getDBName());
			Database.getConnector().setAdminMode(ap.getPassword());
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		SQLQuery squery = new SQLQuery();
		ResultSet crs = squery.run("Select name, id from saada_collection" );
		int nb_coll=0;
		int nb_class=0;
		int nb_obj=0;
		
		try {
			while( crs.next() ) {
				String coll = crs.getString(1);
				int coll_id = crs.getInt(2);
				nb_coll ++;
				printout("* Collection <" + coll + ">");
				String categories[] = Category.NAMES;
				for( int cat=0  ; cat<categories.length ; cat++) {
					if( categories[cat].equals("ROOT_PRODUCT") ) {
						continue;
					}
					String category = Category.explain(categories[cat]);
					printout("  Category <" + category + ">");
					if( categories[cat].equals("FLATFILE") ) {
						ResultSet ors = squery.run("Select oidsaada from " + coll + "_flatfile" ); 
						while( ors.next() ) {
							long oid = ors.getLong(1);
							nb_obj++;
							SaadaInstance si = Database.getCache().getObject(oid);
							printout("        " + oid + "<" + si.getNameSaada() + ">");
						}
						
					}
					else {
						ResultSet clrs = squery.run("Select name from saada_class where collection_id = '" 
								+ coll_id + "' and category = '" + category + "'" );
						while( clrs.next() ) {
							String saadaclass = clrs.getString(1);
							nb_class++;
							printout("  Class <" + saadaclass + ">");					
							ResultSet ors = squery.run("Select oidsaada from " + saadaclass ); 
							while( ors.next() ) {
								long oid = ors.getLong(1);
								nb_obj++;
								SaadaInstance si = Database.getCache().getObject(oid);
								printout("        " + oid + "<" + si.getNameSaada() + ">");
							}
						}
					}
					
				}
			}
			System.out.println("Scanned " + nb_coll + " collection, " + nb_class + " classes and " + nb_obj + " objects");
		}  catch(Exception e) {
			squery.close();
			FatalException.throwNewException(SaadaException.METADATA_ERROR, e);
		}
		squery.close();
	}
	
	/**
	 * @param string
	 */
	private static void printout(String msg) {
		if( Messenger.debug_mode  ) {
			System.out.println(msg);
		}		
	}
}
