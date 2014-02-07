package upgrade.collection;

import healpix.core.HealpixIndex;
import healpix.tools.SpatialVector;

import java.sql.ResultSet;
import java.sql.Statement;

import saadadb.database.Database;
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
//		SQLTable.addQueryToTransaction(Database.getWrapper().getUpdateWithJoin(this.tableName
//				, this.tmpTableName
//				, this.tableName + ".oidsaada = " + this.tmpTableName + ".oidsaada"
//				, this.tableName
//				, new String[]{"healpix_csa"}
//		        , new String[]{this.tmpTableName + ".healpix_csa"}
//				, this.tmpTableName + ".healpix_csa IS NOT NULL"));	
		String nf = "AND "+tableName+".healpix_csa IS NULL";
		SQLTable.addQueryToTransaction(
				  "UPDATE " +tableName
				+ " SET healpix_csa = ( SELECT healpix_csa FROM " + tmpTableName 
				                    + " WHERE "+tableName+".oidsaada = "+tmpTableName+".oidsaada  "+ nf + ") WHERE healpix_csa IS NULL;");

	}
		
	/**
	 * @throws Exception
	 */
	public void setOld() throws Exception {
		Messenger.printMsg(Messenger.TRACE, "Set Healpix value in table " + tableName);
		SQLQuery sqlq = new SQLQuery();
		String nf =  "AND healpix_csa IS NULL";
		ResultSet rs = sqlq.run("SELECT oidsaada, pos_ra_csa, pos_dec_csa FROM " 
				+ tableName 
				+ " WHERE pos_ra_csa IS NOT NULL AND pos_dec_csa IS NOT NULL " + nf);
		int cpt = 0;
		boolean tOpen = false;
		while (rs.next()){
			double ra     = rs.getDouble("pos_ra_csa");
			double dec    = rs.getDouble("pos_dec_csa");
			long oidsaada = rs.getLong("oidsaada");
			
			if( cpt == 0 ) {
				SQLTable.beginTransaction();
				SQLTable.addQueryToTransaction(Database.getWrapper().getCreateTempoTable(this.tmpTableName, "(oidsaada int8, healpix_csa int8)"));	
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
		if( tOpen ) {
			join();
			SQLTable.commitTransaction();
		}
		SQLTable.beginTransaction();
		SQLTable.indexColumnOfTable(tableName, "healpix_csa", null);	
		SQLTable.commitTransaction();
	}
	public void set() throws Exception {
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

	public int setBurst() throws Exception {
		Statement stmt =  Database.get_connection().createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
                ResultSet.CONCUR_UPDATABLE);
		String nf =  "AND healpix_csa IS NULL";
		ResultSet rs = stmt.executeQuery("SELECT * FROM " 
				+ tableName 
				+ " WHERE pos_ra_csa IS NOT NULL AND pos_dec_csa IS NOT NULL " + nf + " limit " + COMMIT_FREQUENCY);
		int cpt = 0;
		while (rs.next()){
			double ra     = rs.getDouble("pos_ra_csa");
			double dec    = rs.getDouble("pos_dec_csa");
			rs.updateLong("healpix_csa", healpixIndex.vec2pix_nest(new SpatialVector(ra,dec)));
			rs.updateRow();
			cpt++;
			if( cpt > COMMIT_FREQUENCY) {
				break;
			}
		}
		stmt.close();
		return cpt;
	}
}