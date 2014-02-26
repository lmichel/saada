package saadadb.query.region.test;



import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.sql.ResultSet;

import saadadb.database.Database;
import saadadb.query.region.test.gridcreator.TpsExec;
import saadadb.sqltable.SQLQuery;
import saadadb.sqltable.SQLTable;

/**
 * Class TSVexplorator
 * Allow to treat a TSV file
 * @author jremy
 * @version $Id$
 *
 */
public class TSVexplorator {
	
	public static String tableName;
	
	public static String dbName;
	
	public static String file;
	
	public static String file2;
	
	public static final String separator = "\t";

	public static final String finligne = "\n";

	/**
	 * This method allows to write the content of the table into the file
	 * @throws Exception
	 */
	public static void write () throws Exception {
		System.out.println(file);
		BufferedWriter bfw = new BufferedWriter(new FileWriter(file));
		String query="SELECT * FROM "+tableName;
		SQLQuery sqlq = new SQLQuery();
		ResultSet rs = sqlq.run(query);
		int count = rs.getMetaData().getColumnCount();
		while(rs.next()) {
			String s="";
			for (int i=1;i<=count;i++) {

				if (rs.getObject(i)!=null) {

					s+=rs.getObject(i).toString();
				}
				else {
					s+="null";
				}
				if (i!=count) {
					s+=separator;
				}
				else {
					s+=finligne;
				}
			}
			bfw.write(s);

		}

		bfw.close();
	}

	/**
	 * This method allows to read the content of the file
	 * @param fich : name of the .tsv
	 * @throws Exception
	 */
	public  static void read (String fich) throws Exception{
		BufferedReader bfr = new BufferedReader(new FileReader(fich));
		String line = bfr.readLine();
		while (line!=null) {
			System.out.println(line);
			line = bfr.readLine();
		}
		bfr.close();
	}

	/**
	 * This method allows to put from a tsv file to another one
	 * @throws Exception
	 */
	public static void change () throws Exception {
		BufferedReader bfr = new BufferedReader(new FileReader(file));
		BufferedWriter bfw = new BufferedWriter(new FileWriter(file2));
		String line = bfr.readLine();
		while (line!=null) {
			String [] tab = line.split(separator);


			String s="";

			for (int i=0;i<tab.length;i++) {

				s+=tab[i];
				if (i!=tab.length-1) {
					s+=separator;
				}
				else {
					s+=finligne;
				}
			}
			bfw.write(s);
			line=bfr.readLine();
		}
		bfr.close();
		bfw.close();
	}
	
	/**
	 * This method allows to load the .tsv file
	 * @throws Exception
	 */
	public static void load () throws Exception {
		SQLTable.beginTransaction();
		SQLTable.addQueryToTransaction("LOADTSVTABLE grande_ENTRY 21 " + file2);
		SQLTable.commitTransaction();
	}



	public static void main(String[] args) throws Exception {
		//Attention le loader va vers une autre table
		
		dbName=args[0];
		tableName=args[1];
		String choix=args[2];

		file="/tmp/"+dbName+"_"+tableName+"_"+1;
		file2="/tmp/"+dbName+"_"+tableName+"_"+2;

		
		Database.init(dbName);
		
		if (choix.equals("writechangeload")) {
			long b = System.currentTimeMillis();
			TSVexplorator.write();
			long c = System.currentTimeMillis();
			TSVexplorator.change();
			long d = System.currentTimeMillis();
			TSVexplorator.load();
			long e = System.currentTimeMillis();
			
			System.out.println("Temps Ecriture Fichier TSV : "+TpsExec.getTime(c-b));
			System.out.println("Temps Modification Fichier TSV : "+TpsExec.getTime(d-c));
			System.out.println("Temps Chargement Fichier TSV : "+TpsExec.getTime(e-d));
			System.out.println("Temps Total : "+TpsExec.getTime(e-b));
		}
		
		if (choix.equals("writechange")) {
			TSVexplorator.write();
			TSVexplorator.change();	
		}
		
		if (choix.equals("read")) {
			TSVexplorator.read(args[3]);
		}
		
		if (choix.equals("load")) {
			TSVexplorator.load();
		}
		
		if (choix.equals("write")) {
			TSVexplorator.write();	
		}
		
		
		
		
	}

}
