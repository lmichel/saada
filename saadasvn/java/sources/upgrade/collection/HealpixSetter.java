package upgrade.collection;

import healpix.core.HealpixIndex;
import healpix.tools.SpatialVector;

import java.sql.ResultSet;

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
	private final boolean force;
	private HealpixIndex healpixIndex ;
	public static final int COMMIT_FREQUENCY = 100000;

	/**
	 * @param tableName :name of the table where the column healpix_csa must be set. No check about the presence of columns pos_ra/dev_csa and oidsaada
	 * @throws Exception
	 */
	public HealpixSetter(String tableName, boolean force) throws Exception {
		super();
		this.tableName = tableName;
		this.healpixIndex = new HealpixIndex(1 << Database.getHeapix_level());
		this.force = force;
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
		String nf =  (force)? "": "AND "+tableName+".healpix_csa IS NULL";
		SQLTable.addQueryToTransaction(
				  "UPDATE " +tableName
				+ " SET healpix_csa = ( SELECT healpix_csa FROM " + tmpTableName 
				                    + " WHERE "+tableName+".oidsaada = "+tmpTableName+".oidsaada  "+ nf + ") WHERE healpix_csa IS NULL;");

	}
		
	/**
	 * @throws Exception
	 */
	public void set() throws Exception {
		Messenger.printMsg(Messenger.TRACE, "Set Healpix value in table " + tableName);
		SQLQuery sqlq = new SQLQuery();
		String nf =  (force)? "": "AND healpix_csa IS NULL";
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

	public static void main(String[] args) throws Exception{
		HealpixIndex healpixIndex = new HealpixIndex(1 << 14);
		System.out.println(healpixIndex.vec2pix_nest(new SpatialVector(110.057 , -31.52586111111111)));

	}
}
