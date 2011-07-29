package saadadb.resourcetest;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import saadadb.collection.CollectionManager;
import saadadb.collection.SaadaInstance;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.dataloader.Loader;
import saadadb.exceptions.AbortException;
import saadadb.query.executor.Query;
import saadadb.query.result.SaadaQLMetaSet;
import saadadb.query.result.SaadaQLResultSet;
import saadadb.sqltable.SQLTable;
import saadadb.util.Messenger;

/*
 * This class build and load 2 VOTablex with 2*8 columns with UCDs matching all possibilities of format (see below)
 * Values are encoded in order to easily check the query results
 * 
 *               | alpha ----|unit    (ucd = [string.alpha.unit] val:A1 ...)
 *               |           |wo-unit (ucd = [string.alpha.wounit] val:X1 ...)
 *     | String -| alphanum -|unit    (ucd = [string.alphanum.unit] val:10000 ...)
 *     |         |           |wo-unit (ucd = [string.alphanum.wounit] val:-10000 ...)
 *     |         | num ------|unit    (ucd = [string.num.unit] val:1000 ...)
 *     |                     |wo-unit (ucd = [string.num.wounit] val:-1000 ...)
 *ucd -| 
 *     | num -| unit    (ucd = [num.unit] val:0 100)
 *            | wo-unit (ucd = [num.wounit] val:0 -100)
 */
/**
 * @author michel
 * * @version $Id$

 */
public class QueryUCDTester { 
	private final String[] filenames = new String[]{"/tmp/ucdvotable1.xml", "/tmp/ucdvotable2.xml"};
	private final String[] collections = new String[]{"UCDTester1", "UCDTester2"};
	private final String[] classes = new String[]{"UCDTagged1", "UCDTagged2"};
	static final private DecimalFormat un = new DecimalFormat("0.0");
	static final private DecimalFormat deux = new DecimalFormat("0.0000");

	/**
	 * @throws Exception
	 */
	public QueryUCDTester() throws Exception {
		deux.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.ENGLISH));
		un.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.ENGLISH));
	}
	/**
	 * @throws Exception
	 */
	private void buildVOTables() throws Exception {
		int num_col=1;
		for( String collection: this.collections) {
			String unit ="m/s";
			int num_file=1;
			for( String classe: this.classes) {
				String filename = "/tmp/" + collection + "_" + classe + ".votable";
				BufferedWriter bf = new BufferedWriter(new FileWriter(filename));
				Messenger.printMsg(Messenger.TRACE, "Build " + filename);
				bf.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
				bf.write("<VOTABLE version=\"1.1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
				bf.write("  xmlns=\"http://www.ivoa.net/xml/VOTable/v1.1\"\n");
				bf.write("  xsi:schemaLocation=\"http://www.ivoa.net/xml/VOTable/v1.1 http://www.ivoa.net/xml/VOTable/v1.1\">\n");
				bf.write(" <DESCRIPTION>\n");
				bf.write("   Test des UCDs\n");
				bf.write(" </DESCRIPTION>\n");
				bf.write("<RESOURCE ID=\"UCDTagged\" name=\"UCDTagged\">\n");
				bf.write("  <TABLE ID=\"UCDTagged\" name=\"UCDTagged\">\n");
				for( int table_num=1; table_num<=2 ; table_num++) {
					String colname_prefix = "c" + num_col + "t" + num_file + "c" + table_num + "_";
					bf.write("	<field name=\"" + colname_prefix + "sau\"    datatype=\"char\" arraysize=\"*\" ucd=\"string.alpha.unit\" unit=\""+ unit + "\" />\n");
					bf.write("	<field name=\"" + colname_prefix + "sasu\"   datatype=\"char\" arraysize=\"*\" ucd=\"string.alpha.wounit\" />\n");
					bf.write("	<field name=\"" + colname_prefix + "sanu\"   datatype=\"char\" arraysize=\"*\" ucd=\"string.alphanum.unit\" unit=\""+ unit + "\" />\n");
					bf.write("	<field name=\"" + colname_prefix + "sansu\"  datatype=\"char\" arraysize=\"*\" ucd=\"string.alphanum.wounit\" />\n");
					bf.write("	<field name=\"" + colname_prefix + "snu\"    datatype=\"char\" arraysize=\"*\" ucd=\"string.num.unit\" unit=\""+ unit + "\" />\n");
					bf.write("	<field name=\"" + colname_prefix + "snsu\"   datatype=\"char\" arraysize=\"*\" ucd=\"string.num.wounit\" />\n");
					bf.write("	<field name=\"" + colname_prefix + "nu\"     datatype=\"int\" ucd=\"num.unit\" unit=\""+ unit + "\" />\n");
					bf.write("	<field name=\"" + colname_prefix + "nsu\"    datatype=\"int\" ucd=\"num.wounit\" />\n");
				}

				bf.write("<DATA>    \n");  
				bf.write("<TABLEDATA>\n");
				for( int row=0 ; row<10 ; row++) {
					bf.write("<TR>");
					for( int table_num=1; table_num<=2 ; table_num++) {
						String num_prefix = num_file + "" + table_num;
						bf.write("<TD>A" + num_prefix + "_" + row + "</TD>");
						bf.write("<TD>X" + num_prefix + "_" + row + "</TD>");
						bf.write("<TD>" + num_prefix + "000" + row + "</TD>");
						bf.write("<TD>-" + num_prefix + "000" + row + "</TD>");
						bf.write("<TD>" + num_prefix + "00" + row + "</TD>");
						bf.write("<TD>-" + num_prefix + "00" + row + "</TD>");
						bf.write("<TD>" + num_prefix + "0" + row + "</TD>");
						bf.write("<TD>-" + num_prefix + "0" + row + "</TD>");
					}
					bf.write("</TR>\n");
				}

				bf.write("</TABLEDATA>\n") ;
				bf.write("</DATA>\n") ;
				bf.write("</TABLE>\n") ;
				bf.write("</RESOURCE>\n") ;
				bf.write("</VOTABLE>\n") ;
				bf.close();
				num_file++;
				unit ="km/h";
			}
			num_col++;
		}
	}

	/**
	 * @throws Exception
	 */
	public void prepareDB() throws Exception {
		buildVOTables();
		prepareDBCollection();
		ingestVOTable();
	}
	/**
	 * @throws Exception
	 */
	private void prepareDBCollection() throws Exception{
		for( String collection: collections) {
			try {
				Database.getCachemeta().getCollection(collection) ;
				SQLTable.beginTransaction();
				(new CollectionManager(collection)).empty(null);
				SQLTable.commitTransaction();
			}
			catch (Exception e) {
				(new CollectionManager(collection)).create(new ArgsParser(new String[]{"-comment=Created to test queries by positions"}));
			}
		}
	}

	/**
	 * @throws Exception
	 */
	private void ingestVOTable() throws Exception {
		for( String collection: this.collections) {
			for( String classe: this.classes) {
				String filename = "/tmp/" + collection + "_" + classe + ".votable";

				Loader loader = new Loader(new String[]{"-classifier=" + collection + "_" + classe
						, "-collection=" + collection
						, "-category=table"
						, "-silent"
						, "-filename=" + filename
						, Database.getDbname()});
				loader.load(); 
			}
		}
	}

	public void setOneQueriabale() throws AbortException {
		SQLTable.addQueryToTransaction("UPDATE  saada_metaclass_entry SET queriable=true WHERE  name_attr LIKE '%c1_s%' OR  name_attr LIKE '%c1_n%'"
				, "saada_metaclass_entry");
		SQLTable.addQueryToTransaction("UPDATE  saada_metaclass_entry SET queriable=false WHERE  name_attr LIKE '%c2_s%' OR  name_attr LIKE '%c2_n%'"
				, "saada_metaclass_entry");
	}

	public void setBothQueriabale() throws AbortException {
		SQLTable.addQueryToTransaction("UPDATE  saada_metaclass_entry SET queriable=true WHERE name_coll like 'UCDTester%'"
				, "saada_metaclass_entry");
	}
	public void setNoneQueriabale() throws AbortException {
		SQLTable.addQueryToTransaction("UPDATE  saada_metaclass_entry SET queriable=false WHERE name_coll like 'UCDTester%'"
				, "saada_metaclass_entry");
	}

	public void runQuery(String query) throws Exception {
		Query q = new Query();
		SaadaQLResultSet srs = q.runQuery(query);
		System.out.println("------------------------------------------------");
		System.out.println(query);
		System.out.println("-------------------");
		if( srs == null ) {
			System.out.println("Result NULL");
		}
		else {
			SaadaQLMetaSet rs_cols = srs.getCol_names();
			System.out.println(rs_cols);
			while( srs.next() ){
				SaadaInstance si = Database.getCache().getObject(srs.getOid());
				String sic = si.getSaadaClass().getName();
				System.out.print(si.getNameSaada() + ":\t");
				for(String s: rs_cols.keySet() ) {
					if( rs_cols.getClassColumnHandler(s, sic) == null ) {
						continue;
					}
					String ucd = rs_cols.getClassColumnHandler(s, sic).getUcd();
					System.out.print(ucd + ": " 
							+ si.getFieldByUCD(ucd, true).getNameattr() 
							+ "=" + si.getFieldValueByUCD(ucd, true)+ rs_cols.getClassColumnHandler(s, sic).getUnit()) ;
					System.out.print(" (" + s + "="  + rs_cols.getSQLColumnHandler(s).getUnit() + ")"
							+ "\t");

				}
				System.out.println("");
			}
		}
	}

	public static void main(String[] args){
		try {
			ArgsParser ap = new ArgsParser(args);
			Database.init(ap.getDBName());
			Database.getConnector().setAdminMode(ap.getPassword());
			QueryUCDTester qut = new QueryUCDTester();
			if( ap.getBuild() || ap.getAll() ) {
				qut.prepareDB();
			}
			if( ap.getAll() ) {
				qut.setOneQueriabale();
				Database.getCachemeta().reload(true);
				qut.runQuery("Select ENTRY From * In * \nWhereUCD{ [num.unit] > 2000 [km/h]}");
				qut.runQuery("Select ENTRY From * In * \nWhereUCD{ [num.unit] > 2000 [none]}");
				qut.runQuery("Select ENTRY From * In * \nWhereUCD{ [string.alphanum.wounit] = '-110000' [none]}");
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}