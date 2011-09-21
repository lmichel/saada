package saadadb.resourcetest;

import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.relationship.RelationManager;
import saadadb.sqltable.SQLTable;
import saadadb.util.Messenger;

public class RelationTester extends GridBuilder {

	RelationTester(String pos, int size, String collection) throws Exception {
		super(pos, size, collection);
	}


	public static void main(String[] args) {
		ArgsParser ap;
		try {
			ap = new ArgsParser(args);
			Database.init(ap.getDBName());
			Database.getConnector().setAdminMode(ap.getPassword());
			String target = "12.345 -12.3456";
			int size = 10;

			String prim  = ap.getFrom();
			int p_prim   = prim.lastIndexOf("_");
			String pcoll = prim.substring(0, p_prim);
			String sec   = ap.getTo();
			int p_sec    = sec.lastIndexOf("_");
			String scoll = sec.substring(0, p_sec);
			new RelationTester(target, size, pcoll);
			Database.getCachemeta().reload(true);
			new RelationTester(target, size, scoll);

			RelationManager rm = new RelationManager(ap.getCreate());
			if( Database.getCachemeta().getRelation(ap.getCreate()) != null ) {
				SQLTable.beginTransaction();
				rm.remove(ap);	
				SQLTable.commitTransaction();
			}
			Database.getCachemeta().reload(true);
			SQLTable.beginTransaction();
			rm.create(ap);	
			SQLTable.commitTransaction();
			Database.getCachemeta().reload(true);
			rm = new RelationManager(ap.getCreate());
			SQLTable.beginTransaction();
			rm.populate(null);	
			rm.index(null);		
			SQLTable.commitTransaction();
//			Database.getCachemeta().reload(true);
//			System.out.println(Database.getCachemeta().getRelation(ap.getCreate()));
			
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			System.exit(1);
			
			System.out.println("USAGE java RelationTester -create=relationname -from=startingcoll/cat -to=endingcoll/cat -query=[correlator] -qualifires=[q1,q2...] -comment=[description] SAADADB_NAME");
		}
		System.exit(0);

	}

}