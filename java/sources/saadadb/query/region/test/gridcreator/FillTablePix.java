package saadadb.query.region.test.gridcreator;

import healpix.core.HealpixIndex;
import healpix.tools.SpatialVector;

import java.sql.ResultSet;

import ajaxservlet.IsAlive;

import cds.astro.Astroframe;
import cds.astro.Coo;

import saadadb.database.Database;
import saadadb.database.spooler.DatabaseConnection;
import saadadb.database.spooler.Spooler;
import saadadb.sqltable.SQLQuery;
import saadadb.sqltable.SQLTable;

/**
 * Class FillTablePix
 * Allows to fill an intermediate table with the healpix pixel to update a big table
 * @author jremy
 * @version $Id$
 *
 */
public class FillTablePix {

	/**
	 * Attribute tableName
	 * Name of the table to update
	 */
	public static String tableName;
	
	/**
	 * Attribute tableTMP
	 * Name of the intermediate table
	 */
	public static final String tableTMP="ttmp";

	/**
	 * Attribute bddName
	 * Name of the the Database
	 */
	public static String bddName;
	
	/**
	 * This method allow to prepare the intermediate table
	 * @throws Exception
	 */
	public static void prepareTable() throws Exception{
		DatabaseConnection connection = Spooler.getSpooler().getConnection();
		
		if(Database.getWrapper().tableExist(connection, tableTMP)) {
			SQLTable.beginTransaction();
			SQLTable.dropTable(tableTMP);
			SQLTable.commitTransaction();
		}	
		Spooler.getSpooler().give(connection);
		SQLTable.beginTransaction();
		SQLTable.createTable(tableTMP,"idpoint LONG, hpx8 LONG, hpx10 LONG, hpx12 LONG","idpoint",false);
		SQLTable.commitTransaction();
	}

	/**
	 * This method allow to fill in the intermediate table with the pixels
	 * @throws Exception
	 */
	public static void fillTable() throws Exception {

		SQLQuery sqlq = new SQLQuery();
		String query = "SELECT s_ra,s_dec FROM "+tableName;
		ResultSet rs = sqlq.run(query);
		HealpixIndex hi8 = new HealpixIndex(256);
		HealpixIndex hi10 = new HealpixIndex(1024);
		HealpixIndex hi12 = new HealpixIndex(4096);
		SQLTable.beginTransaction();

		while (rs.next()){
			long oid=rs.getRow();
			double ra=rs.getDouble("s_ra");
			double dec=rs.getDouble("s_dec");
			SpatialVector sv = new SpatialVector(ra, dec);
			long pix8=hi8.vec2pix_nest(sv);
			long pix10=hi10.vec2pix_nest(sv);
			long pix12=hi12.vec2pix_nest(sv);

			String query3="INSERT INTO "+tableTMP+" (idpoint,hpx8,hpx10,hpx12) VALUES ("+oid+","+pix8+","+pix10+","+pix12+");";
			SQLTable.addQueryToTransaction(query3);

		}

		SQLTable.commitTransaction();
	}
	
	/**
	 * This method execute the main program
	 * @param tN : tableName
	 * @param bN : DatabaseName
	 * @throws Exception
	 */
	public static void execute(String tN, String bN) throws Exception {
		tableName=tN+"_ENTRY";
		bddName=bN;

		Database.init(bddName);
		FillTablePix.prepareTable();
		FillTablePix.fillTable();

	}

}