package upgrade.collection;

import healpix.core.HealpixIndex;
import healpix.tools.SpatialVector;

import java.sql.ResultSet;
import java.sql.Statement;

import saadadb.database.Database;
import saadadb.database.spooler.DatabaseConnection;
import saadadb.database.spooler.Spooler;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.FatalException;
import saadadb.sqltable.SQLQuery;
import saadadb.sqltable.SQLTable;
import saadadb.util.Messenger;

/**
 * @author michel
 * @version $Id$
 *
 */
public class HealpixSetter {
	private final String tableName;
	private final String tmpTableName = "Healpixstore";
	private HealpixIndex healpixIndex ;
	public static final int COMMIT_FREQUENCY = 100000;

	/**
	 * @param tableName :name of the table where the column healpix_csa must be set. No check about the presence of columns pos_ra/dev_csa and oidsaada
	 * @throws Exception
	 */
	public HealpixSetter(String tableName) throws Exception {
		super();
		this.tableName = tableName;
		this.healpixIndex = new HealpixIndex(1 << Database.getHeapix_level());
	}
	
	/**
	 * @throws AbortException
	 * @throws FatalException
	 */
	private void join() throws AbortException, FatalException {
		String nf = "AND "+tableName+".healpix_csa IS NULL";
		SQLTable.addQueryToTransaction(
				  "UPDATE " +tableName
				+ " SET healpix_csa = ( SELECT healpix_csa FROM " + tmpTableName 
				                    + " WHERE "+tableName+".oidsaada = "+tmpTableName+".oidsaada  "+ nf + ") WHERE healpix_csa IS NULL;");

	}
		
	/**
	 * @throws Exception
	 */
	public void setWithTmpTable() throws Exception {
		Messenger.printMsg(Messenger.TRACE, "Set Healpix value in table " + tableName);
		SQLTable.beginTransaction();
		SQLTable.createTable(this.tmpTableName, " oidsaada int8, healpix_csa int8", "oidsaada", true);
		SQLTable.commitTransaction();

		SQLQuery sqlq = new SQLQuery();
		String nf =  "AND healpix_csa IS NULL";
		ResultSet rs = sqlq.run("SELECT oidsaada, s_ra, s_dec FROM " 
				+ tableName 
				+ " WHERE pos_ra_csa IS NOT NULL AND pos_dec_csa IS NOT NULL " + nf);
		int cpt = 0;
		boolean tOpen = false;
		while (rs.next()){
			double ra     = rs.getDouble("s_ra");
			double dec    = rs.getDouble("s_dec");
			long oidsaada = rs.getLong("oidsaada");
			if( cpt == 0 ) {
				SQLTable.beginTransaction();
				tOpen = true;
			}
			SQLTable.addQueryToTransaction("INSERT INTO " + this.tmpTableName 
					+ " (oidsaada,healpix_csa) VALUES (" + oidsaada 
					+ "," + healpixIndex.vec2pix_nest(new SpatialVector(ra,dec)) +");");
			if( cpt > 0 && (cpt%COMMIT_FREQUENCY) == 0 ) {
				join();
				SQLTable.commitTransaction();
				SQLTable.beginTransaction();
				SQLTable.addQueryToTransaction(Database.getWrapper().getCreateTempoTable(this.tmpTableName, "(oidsaada int8, healpix_csa int8)"));	
				tOpen = true;
				cpt = 0;
			}
			cpt++;			
		}
		sqlq.close();
		System.out.println(Spooler.getSpooler());			
		if( tOpen ) {
			join();
			SQLTable.commitTransaction();
		}
		System.out.println("@@@@@@@@@@@@ F");			
		SQLTable.beginTransaction();
		SQLTable.indexColumnOfTable(tableName, "healpix_csa", null);	
		SQLTable.dropTable(this.tmpTableName);
		SQLTable.commitTransaction();
	}
	public void setWithUpdate() throws Exception {
		Messenger.printMsg(Messenger.TRACE, "Set Healpix value in table " + tableName);
		int sum = 0;
		int v;
		while( (v = setBurst()) > 0 ){ 
			sum += v;
			Messenger.printMsg(Messenger.TRACE, sum + " rows updated");			
		}
		SQLTable.beginTransaction();
		SQLTable.indexColumnOfTable(tableName, "healpix_csa", null);	
		SQLTable.commitTransaction();
	}
	public void set() throws Exception {
		if( Database.getWrapper().supportJDBCUpdate() ) {
			this.setWithUpdate();
		} else {
			this.setWithTmpTable();
		}
	}

	public int setBurst() throws Exception {
		DatabaseConnection connection = Database.getAdminConnection();
		Statement stmt =  connection.getUpdatableStatement();
		String nf =  "AND healpix_csa IS NULL";
		ResultSet rs = stmt.executeQuery("SELECT * FROM " 
				+ tableName 
				+ " WHERE s_ra IS NOT NULL AND s_dec IS NOT NULL " + nf + " limit " + COMMIT_FREQUENCY);
		int cpt = 0;
		while (rs.next()){
			double ra     = rs.getDouble("s_ra");
			double dec    = rs.getDouble("s_dec");
			rs.updateLong("healpix_csa", healpixIndex.vec2pix_nest(new SpatialVector(ra,dec)));
			rs.updateRow();
			cpt++;
			if( cpt > COMMIT_FREQUENCY) {
				break;
			}
		}
		Database.giveAdminConnection();
		return cpt;
	}
}
