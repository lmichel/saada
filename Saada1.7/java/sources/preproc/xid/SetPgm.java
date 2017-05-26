package preproc.xid;

import saadadb.database.Database;
import saadadb.sqltable.SQLTable;

public class SetPgm {
	public static void main(String[] args) throws Exception {
		Database.init("Napoli");
		Database.setAdminMode("");
		SQLTable.beginTransaction();

		SQLTable.addQueryToTransaction("UPDATE AIPWFI_Table SET PGM = 'AIP_XMM' WHERE product_url_csa like '/Users/laurentmichel/IVOANaples/data/wfi_iap/catalogues/AIP_XMM/%'");
		SQLTable.addQueryToTransaction("UPDATE AIPWFI_Table SET PGM = 'BLOX'    WHERE product_url_csa like '/Users/laurentmichel/IVOANaples/data/wfi_iap/catalogues/BLOX/%'");
		SQLTable.addQueryToTransaction("UPDATE AIPWFI_Table SET PGM = 'EIS'     WHERE product_url_csa like '/Users/laurentmichel/IVOANaples/data/wfi_iap/catalogues/EIS/%'");
		SQLTable.commitTransaction();
		
		SQLTable.beginTransaction();
		SQLTable.addQueryToTransaction("UPDATE AIPWFI_Entry SET PGM =  (SELECT t.pgm FROM AIPWFI_Table as t where t.oidsaada = AIPWFI_Entry.oidtable)");
		SQLTable.commitTransaction();
		Database.close();

	}

}
