package preproc.xid;

import java.util.Date;

import saadadb.collection.obscoremin.EntrySaada;
import saadadb.database.Database;
import saadadb.query.executor.Query;
import saadadb.query.result.SaadaQLResultSet;
import saadadb.sqltable.SQLTable;
import saadadb.util.Messenger;
import cds.astro.Astrocoo;

public class RenameWFI {

	public static void main(String[] args) throws Exception{
		Database.init("XIDResult");
		Database.setAdminMode("");
		Messenger.debug_mode= false;
//		SQLTable.beginTransaction();
//		SQLTable.addQueryToTransaction("UPDATE AIPWFI_Entry SET namesaada = 'NotSet'");
//		SQLTable.commitTransaction();
		SaadaQLResultSet srs = (new Query()).runQuery("Select ENTRY From * In WideFieldData", false);
		int cpt = 0;
//		PreparedStatement updatColl = Database.get_connection().prepareStatement(
//				"update WideFieldData_Entry set namesaada = ? where oidsaada = ? ");
//		PreparedStatement updatClass = Database.get_connection().prepareStatement(
//		"update AIP_WFIEntry set namesaada = ? where oidsaada = ? ");

		SQLTable.beginTransaction();
		int majs = 0;
		while( srs.next() ) {
			long oid = srs.getOid();
			EntrySaada es = (EntrySaada) Database.getCache().getObject(oid);
    		Astrocoo coo =new Astrocoo(Database.getAstroframe(), es.s_ra, es.s_dec);
    		coo.setPrecision(5);
//			updatColl.setLong(2, oid); 
//			updatClass.setLong(2, oid); 
			String n = es.getFieldString("XIDPGM")  + coo.toString("s").replaceAll(" ", "");
//			updatColl.setString(1, n); 
//			updatClass.setString(1, n); 
//			majs += updatColl.executeUpdate();
//			majs += updatClass.executeUpdate();

			if( (cpt%50000) == 0 ) {
//    			SQLTable.commitTransaction();
//    			SQLTable.beginTransaction();
    			System.out.println((new Date()).toString() + " " + cpt + " " + n + " " + majs + " majs");
				SQLTable.commitTransaction();
				SQLTable.beginTransaction();
   		}
    		//bw.write("WFI" + coo.toString("s") + "\t" + oid + "\n");
//    		String sql = "UPDATE WideFieldData_Entry SET namesaada = 'WFI" + coo.toString("s") + "' WHERE oidsaada = " + oid;
//    		SQLTable.runQueryUpdateSQL(sql, false);
//			sql = "UPDATE iap_wfientry SET namesaada = 'WFI" + coo.toString("s") + "' WHERE oidsaada = " + oid;
//    		SQLTable.runQueryUpdateSQL(sql, false);
    		cpt++;
		}
		SQLTable.commitTransaction();
		Database.close();

//		SQLTable.commitTransaction();
	}
}
