package preproc.classbuilder;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import saadadb.collection.SaadaOID;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.meta.MetaClass;
import saadadb.sqltable.SQLQuery;
import saadadb.sqltable.SQLTable;
import saadadb.util.Messenger;

/**
 * @author laurentmichel
 * * @version $Id$

 */
public class OIdSetter {

	public static void  main(String[] args)  {
		try {
			ArgsParser ap = new ArgsParser(args);
			Database.init(ap.getDBName());
			MetaClass mc =Database.getCachemeta().getClass(ap.getClassName());
			Database.getConnector().setAdminMode("saadadmin");

			long oid = SaadaOID.newOid(ap.getClassName());
			if( oid < SaadaOID.getMaskForClass(ap.getClassName()) + 1) {
				oid = SaadaOID.getMaskForClass(ap.getClassName());
			}
			Messenger.printMsg(Messenger.TRACE, "oid starts from 0x" + Long.toHexString(oid) + " " + oid);
			SQLQuery squery = new SQLQuery();
			ResultSet rs = squery.run("select _access_url FROM " + mc.getName() + " where oidsaada = -1");
			ArrayList<String> al = new ArrayList<String>();
			while( rs.next() ) al.add( rs.getString(1));
			squery.close();
			
			int cpt = 0;
			SQLTable.beginTransaction();
			PreparedStatement updateSales = Database.get_connection().prepareStatement(
					"update " + mc.getName() + " set oidsaada = ? where _access_url = ?");

			for( String url: al) {
				cpt++;
				//String url = rs.getString(1);
				//				SQLTable.runQueryUpdateSQL("update " + mc.getName() + " set oidsaada = " + oid + " where _access_url = '" + url + "'", false);

				updateSales.setLong(1, oid); 
				updateSales.setString(2, url); 
				updateSales.executeUpdate();
				if( (cpt%100) == 0 ) {
					Messenger.printMsg(Messenger.TRACE, cpt + " oid set (current = 0xSRSPEC" + Long.toHexString(oid) + ")");
					SQLTable.commitTransaction();
					SQLTable.beginTransaction();
				}
				oid++;
			}
			SQLTable.commitTransaction();

		}
		catch(Exception e) {
			Messenger.printStackTrace(e);
			System.exit(1);
		}
	}
}
