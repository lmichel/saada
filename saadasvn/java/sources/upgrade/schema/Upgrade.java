/**
 * 
 */
package upgrade.schema;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import cds.astro.Astroframe;
import cds.astro.FK4;
import cds.astro.FK5;
import cds.astro.Galactic;
import cds.astro.ICRS;

import saadadb.database.Database;
import saadadb.sqltable.Table_SaadaDB;
import saadadb.sqltable.Table_Saada_Class;
import saadadb.sqltable.Table_Saada_Collection;
import saadadb.sqltable.Table_Saada_Relation;
import saadadb.util.Messenger;

/**
 * @author michel
 * @version $Id$
 *
 */
public class Upgrade {

	public static void upgrade() throws Exception {
		Table_Saada_Relation.addStatColumn();
		Table_Saada_Class.addStatColumn();
		Table_Saada_Collection.addStatColumn();		
		Table_SaadaDB.addHealpixColumn();
		saveXMLFile();
	}

	private static void saveXMLFile() throws IOException {

		String filename = Database.getRoot_dir() + Database.getSepar() + "config" + Database.getSepar() + "saadadb.xml";
		(new File(filename)).renameTo(new File(filename + ".org"));;
		Messenger.printMsg(Messenger.TRACE, "Save SaadaDB config in <" + filename + ">");
		BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
		bw.write("<?xml version=\"1.0\" encoding=\"iso-8859-1\" standalone=\"no\"?>\n");
		bw.write("<!DOCTYPE saadadb SYSTEM \"saadadb.dtd\">\n");
		bw.write("<saadadb>\n");
		bw.write("    <database>\n");
		bw.write("        <name><![CDATA[" + Database.getDbname() + "]]></name>\n");
		bw.write("        <description><![CDATA[]]></description>\n");
		bw.write("        <root_dir><![CDATA[" + Database.getRoot_dir() + "]]></root_dir>\n");
		bw.write("        <repository_dir><![CDATA[" + Database.getRepository() + "]]></repository_dir>\n");
		bw.write("    </database>\n");
		bw.write("    <relational_database>\n");
		bw.write("        <name><![CDATA[" + Database.getConnector().getJdbc_dbname()+"]]></name>\n");
		bw.write("        <administrator>\n");
		bw.write("            <name><![CDATA[" + Database.getConnector().getJdbc_administrator() + "]]></name>\n");
		bw.write("        </administrator>\n");
		bw.write("        <reader>\n");
		bw.write("            <name><![CDATA[" + Database.getConnector().getJdbc_reader() + "]]></name>\n");
		bw.write("            <password><![CDATA[" + Database.getConnector().getJdbc_reader_password()+ "]]></password>\n");
		bw.write("        </reader>\n");
		bw.write("        <jdbc_driver>" +Database.getConnector().getJdbc_driver() + "</jdbc_driver>\n");
		bw.write("        <jdbc_url>" + Database.getConnector().getJdbc_url() + "</jdbc_url>\n");
		bw.write("    </relational_database>\n");
		bw.write("    <web_interface>\n");
		bw.write("        <webapp_home><![CDATA[" + Database.getConnector().getWebapp_home() + "]]></webapp_home>\n");
		bw.write("        <url_root><![CDATA[" + Database.getUrl_root() + "]]></url_root>\n");
		bw.write("    </web_interface>\n");
		bw.write("    <spectral_coordinate>\n");
		bw.write("        <abscisse type=\""  +  Database.getSpect_type() + "\" unit=\"" + Database.getSpect_unit() + "\"/>\n");	
		bw.write("    </spectral_coordinate>\n");
		bw.write("    <coordinate_system>\n");
		Astroframe af = Database.getAstroframe();
		if( af instanceof FK5 ){
			bw.write("        <system>FK5</system>\n");				
		} else if( af instanceof FK4 ){
			bw.write("        <system>FK4</system>\n");				
		} else if( af instanceof ICRS ){
			bw.write("        <system>ICRS</system>\n");				
		} else if( af instanceof Galactic ){
			bw.write("        <system>Galactic</system>\n");				
		} else {
			bw.write("        <system></system>\n");			
			Messenger.printMsg(Messenger.WARNING, "Unknown astroframe: " + af);
		}
		bw.write("        <equinox>" + Database.getCoord_equi() + "</equinox>\n");				
		bw.write("        <healpix_level>15</healpix_level>\n");
		bw.write("    </coordinate_system>\n");
		bw.write("</saadadb>\n");
		bw.close();
	}
}
