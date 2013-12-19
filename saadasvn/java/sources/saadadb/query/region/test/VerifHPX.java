package saadadb.query.region.test;



import healpix.core.HealpixIndex;
import healpix.tools.SpatialVector;

import java.sql.ResultSet;

import saadadb.database.Database;
import saadadb.sqltable.SQLQuery;

public class VerifHPX {

	public static final String separator = "\t";

	/**
	 * Method modified, maybe not-functional
	 * Allows to test the updating table
	 * @throws Exception
	 */
	public static void VerifTSV () throws Exception {
		Database.init("clabase");
		SQLQuery sqlq2 = new SQLQuery();
		String query2 = "SELECT * FROM bigbigbig_ENTRY;";
		ResultSet rs2 = sqlq2.run(query2);


		SQLQuery sqlq = new SQLQuery();
		String query = "SELECT * FROM petite_ENTRY;";
		ResultSet rs = sqlq.run(query);
		int count = rs.getMetaData().getColumnCount();
		int counter = 1;
		while (rs.next() && rs2.next()) {


			for (int i=1;i<=count;i++) {

				if (rs.getObject(i) != null && rs2.getObject(i) != null ) {

					if (rs.getObject(i).toString().equals(rs2.getObject(i).toString())) {
					}
					else {
						System.out.println("Numero Colonne : "+i);
						System.out.println("Nom colonne : "+rs.getMetaData().getColumnName(i));
						System.out.println("Rs : "+rs.getObject(i));
						System.out.println("Rs2: "+rs2.getObject(i));
						System.out.println(counter);
					}
				}
				if (rs2.getObject(i)==null && rs.getObject(i)!=null) {
					if (i<19) {
						System.out.println(i+" : Bug");
					}
				}

			}
			counter++;
		}
	}




	/**
	 * Method VerifPixel
	 * Allows to check every pixel of a table with the ra,dec
	 * @throws Exception
	 */
	public static void VerifPixel () throws Exception {
		Database.init("clabase");
		SQLQuery sqlq = new SQLQuery();
		String query = "SELECT * FROM petite_ENTRY;";
		ResultSet rs = sqlq.run(query);



		while (rs.next()){
			double ra=rs.getDouble("pos_ra_csa");
			double dec=rs.getDouble("pos_dec_csa");
			long hpx8 = rs.getLong("healpix8");
			long hpx10 = rs.getLong("healpix10");
			long hpx12 = rs.getLong("healpix12");
			SpatialVector sv = new SpatialVector(ra,dec);
			HealpixIndex hi8 = new HealpixIndex(256);
			HealpixIndex hi10 = new HealpixIndex(1024);
			HealpixIndex hi12 = new HealpixIndex(4096);
			long pix8=hi8.vec2pix_nest(sv);
			long pix10=hi10.vec2pix_nest(sv);
			long pix12=hi12.vec2pix_nest(sv);

			if (hpx8 != pix8 || hpx10 != pix10 || hpx12 != pix12) {
				System.out.println("BOUUUUUUUUUUUUUUUUUUUUUUM");
			}


			System.out.println("Numero de la ligne : "+rs.getRow()+" -- Ra : "+ra+" -- Dec : "+dec);
			System.out.println(" -- Pix8 : "+pix8+" -- Pix10 : "+pix10+" -- Pix12 : "+pix12);
			System.out.println(" -- Pix8 : "+pix8+" -- Pix10 : "+pix10+" -- Pix12 : "+pix12);
		}
	}

	public static void main(String[] args) throws Exception{
		VerifHPX.VerifTSV();
		VerifHPX.VerifPixel();
	}

}

