package saadadb.query.region.test.gridcreator;

import healpix.core.HealpixIndex;
import healpix.tools.SpatialVector;

import java.sql.ResultSet;

import saadadb.database.Database;
import saadadb.sqltable.SQLQuery;
import saadadb.sqltable.SQLTable;

/**
 * Class UpdateHPXDirectSQL
 * This class allows to update the healpix column directly in the Database 
 * @author jremy
 * @version $Id$
 *
 */
public class UpdateHPXDirectSQL {
	
	public static final String tableName="collection";
	
	public static final String bddName="base3reso3";

	public static void main(String[] args) throws Exception {
		long tpsdepart=System.currentTimeMillis();
		Database.init(bddName);
		SQLQuery sqlq = new SQLQuery();
		String query = "SELECT s_ra,s_dec FROM "+tableName+";";
		ResultSet rs = sqlq.run(query);

		SQLTable.beginTransaction();
		while (rs.next()){
			int ligne=rs.getRow();
			double ra=rs.getDouble("s_ra");
			double dec=rs.getDouble("s_dec");
			SpatialVector sv = new SpatialVector(ra,dec);
			HealpixIndex hi8 = new HealpixIndex(256);
			HealpixIndex hi10 = new HealpixIndex(1024);
			HealpixIndex hi12 = new HealpixIndex(4096);
			long pix8=hi8.vec2pix_nest(sv);
			long pix10=hi10.vec2pix_nest(sv);
			long pix12=hi12.vec2pix_nest(sv);

			
			String querypix = "UPDATE "+tableName+" SET healpix8 ="+pix8+", healpix10 ="+pix10+", healpix_csa ="+pix12+" WHERE rowid="+ligne+";";

			
			SQLTable.addQueryToTransaction(querypix);
			
			
			
		}
		
		SQLTable.commitTransaction();
		
		long tpsarrivee=System.currentTimeMillis();
		
		long tpstotal=tpsarrivee-tpsdepart;
		
		System.out.println(TpsExec.getTime(tpstotal));
		
	}

}
