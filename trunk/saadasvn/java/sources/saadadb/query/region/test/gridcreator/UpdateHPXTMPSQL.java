package saadadb.query.region.test.gridcreator;

import saadadb.database.Database;
import saadadb.sqltable.SQLTable;

/**
 * Class UpdateHPXTMPSQL
 * Allows to update the healpix column by creating a tmp table
 * @author jremy
 * @version $Id$
 *
 */
public class UpdateHPXTMPSQL {
	
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
	
	public static void execute(String tN, String bN) throws Exception {
		tableName=tN+"_ENTRY";
		bddName=bN;

		Database.init(bddName);

		SQLTable.beginTransaction();

		String query="UPDATE "+tableName+" SET healpix8 = ( SELECT hpx8 FROM "+tableTMP +" WHERE "+tableName+".rowid = "+tableTMP+".rowid) WHERE healpix8 IS NULL;";
		SQLTable.addQueryToTransaction(query);
		query="UPDATE "+tableName+" SET healpix10 = ( SELECT hpx10 FROM "+tableTMP +" WHERE "+tableName+".rowid = "+tableTMP+".rowid) WHERE healpix10 IS NULL;";
		SQLTable.addQueryToTransaction(query);
		query="UPDATE "+tableName+" SET healpix_csa = ( SELECT hpx12 FROM "+tableTMP +" WHERE "+tableName+".rowid = "+tableTMP+".rowid) WHERE healpix_csa IS NULL;";
		SQLTable.addQueryToTransaction(query);

		SQLTable.commitTransaction();

	}

	public static void main(String[] args) throws Exception {

		long tpsdepart=System.currentTimeMillis();

		Database.init(bddName);

		SQLTable.beginTransaction();

		String query="UPDATE "+tableName+" SET healpix8 = ( SELECT hpx8 FROM "+tableTMP +" WHERE "+tableName+".rowid = "+tableTMP+".rowid);";
		SQLTable.addQueryToTransaction(query);
		query="UPDATE "+tableName+" SET healpix10 = ( SELECT hpx10 FROM "+tableTMP +" WHERE "+tableName+".rowid = "+tableTMP+".rowid);";
		SQLTable.addQueryToTransaction(query);
		query="UPDATE "+tableName+" SET healpix_csa = ( SELECT hpx12 FROM "+tableTMP +" WHERE "+tableName+".rowid = "+tableTMP+".rowid);";
		SQLTable.addQueryToTransaction(query);

		SQLTable.commitTransaction();

		long tpsarrivee=System.currentTimeMillis();

		long tpstotal=tpsarrivee-tpsdepart;

		System.out.println(TpsExec.getTime(tpstotal));
	}

}
