package saadadb.resourcetest;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import saadadb.collection.CollectionManager;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.database.Repository;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.QueryException;
import saadadb.sqltable.SQLQuery;
import saadadb.sqltable.SQLTable;
import saadadb.util.Messenger;
import cds.astro.Astrocoo;
import cds.astro.Coo;

/**
 * This class build a collection and populate it with one pseudo image defined by a position and size.
 * After is look for the area where a search box matches that image in various SIAP modes.
 * 2 boxes are tested: one smaller than the image and one bigger
 * @author michel
 *
 */
public class SIAPQueryTester {
	String collection, collection_table;
	double[][]  img_corners= new double[][]{new double[2], new double[2], new double[2], new double[2]};
	double ra, dec, size_ra, size_dec;
	static final private DecimalFormat un = new DecimalFormat("0.0");
	static final private DecimalFormat deux = new DecimalFormat("0.0000");
	double[] upper_limit = new double[2];
	double[] lower_limit = new double[2];
	double[] left_limit = new double[2];
	double[] right_limit = new double[2];
	private int status = 0;
	static final int CENTER=0;
	static final int ENCLOSED=1;
	static final int COVERS=2;
	static final int OVERLAPS=3;
	double pas_ra = 0;
	double pas_dec = 0;
	double ra_box;
	double dec_box;
	double size_ra_box;
	double size_dec_box;
	boolean bigroi_mode = false;

	SIAPQueryTester() throws Exception {
		this.collection = "SIAPTest";
		this.collection_table = collection + "_image";
		deux.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.ENGLISH));
		un.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.ENGLISH));
		prepareDBCollection();
	}

	public void setImage(double ra,double  dec, double size_ra, double size_dec) throws Exception {
		this.ra = ra;
		this.dec = dec;
		this.size_ra = size_ra;
		this.size_dec = size_dec;
		this.pas_ra = this.size_ra/100.0;
		this.pas_dec = this.size_dec/100.0;
		deux.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.ENGLISH));
		un.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.ENGLISH));
		SQLTable.addQueryToTransaction("DELETE FROM " + collection_table);
		SQLTable.addQueryToTransaction(Database.getWrapper().getInsertStatement(collection_table, new String[]{"oidsaada", "namesaada", "pos_ra_csa" , "pos_dec_csa" , "size_alpha_csa" , "size_delta_csa"},
				new String[] {"0", "'imageTestSiap'", Double.toString(ra), Double.toString(dec), Double.toString(size_ra), Double.toString(size_dec)}));

		String q = "Select corner00_dec(" + this.dec + " , " + this.size_dec 
		+ "), corner00_ra(" + this.ra + " , "+ this.dec + " , " + this.size_ra +  " , " + this.size_dec 
		+ "), corner10_ra(" + this.ra + " , "+ this.dec + " , " + this.size_ra +  " , " + this.size_dec
		+ "), corner01_dec(" + this.dec  +  " , " + this.size_dec
		+ "), corner01_ra(" + this.ra + " , "+ this.dec + " , " + this.size_ra +  " , " + this.size_dec 
		+ "), corner11_ra(" + this.ra + " , "+ this.dec + " , " + this.size_ra +  " , " + this.size_dec + ")";
		SQLQuery squery = new SQLQuery();
		ResultSet rs = squery.run(q);
		while( rs.next() ) {
			img_corners[0][1] = rs.getDouble(1);
			img_corners[1][1] = rs.getDouble(1);
			img_corners[0][0] = rs.getDouble(2);
			img_corners[1][0] = rs.getDouble(3);
			img_corners[2][1] = rs.getDouble(4);
			img_corners[3][1] = rs.getDouble(4);
			img_corners[2][0] = rs.getDouble(5);
			img_corners[3][0] = rs.getDouble(6);
		}
//		squery.close();
//				System.out.println( + this.ra + " , "+ this.dec + " , " + this.size_ra +  " , " + this.size_dec);
//				for( int i=0 ; i<4 ; i++ ) {
//					System.out.println("[" + i + " 0] "  + img_corners[i][0] + "[" + i + " 1] "  + img_corners[i][1]);
//		
//				}
//				System.exit(1);
	}
	/**
	 * @throws Exception
	 */
	private void prepareDBCollection() throws Exception{
		try {
			Database.getCachemeta().getCollection(collection) ;
			SQLTable.beginTransaction();
			(new CollectionManager(collection)).remove(null);
			SQLTable.commitTransaction();
			Database.getCachemeta().reload(true);
		}
		catch (Exception e) {
		}
		SQLTable.beginTransaction();
		(new CollectionManager(collection)).create(new ArgsParser(new String[]{"-comment=Created to store a grid"}));
		SQLTable.commitTransaction();
	}

	/**
	 * Returns the SIPA constrain matching the SIAP query mode
	 * @param mode
	 * @param prefix
	 * @param ra_box
	 * @param dec_box
	 * @param size_ra_box
	 * @param size_dec_box
	 * @return
	 * @throws FatalException
	 * @throws QueryException
	 */
	private String getConstraint(int mode, String prefix, double ra_box, double dec_box, double size_ra_box, double size_dec_box) throws FatalException, QueryException {
		switch(mode) {
		case CENTER: 
			return Database.getWrapper().getImageCenterConstraint("i.", ra_box, dec_box, size_ra_box, size_dec_box);
		case ENCLOSED: 
			return Database.getWrapper().getImageEnclosedConstraint("i.", ra_box, dec_box, size_ra_box);
		case COVERS: 
			return Database.getWrapper().getImageCoverConstraint("i.", ra_box, dec_box, size_ra_box, size_dec_box);
		default: if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Take OVERLAPS mode by default");		
		return Database.getWrapper().getImageOverlapConstraint("i.", ra_box, dec_box, size_ra_box);
		}
	}

	private static String  getMODE(int mode)  {
		switch(mode) {
		case CENTER: return "CENTER";
		case ENCLOSED: return "ENCLOSED";
		case COVERS: return "COVERS";
		case OVERLAPS: return "OVERLAPS";
		default: return "NOT IMPLEMENTED";
		}
	}
	public void setBox() {
		this.status = 0;
		if( !bigroi_mode ) {
			this.ra_box = ra;
			this.dec_box = dec;
			this.size_ra_box = size_ra/2;
			this.size_dec_box = size_dec/2.;
		}
		else {
			this.ra_box = ra;
			this.dec_box = dec;
			this.size_ra_box = size_ra*2;
			this.size_dec_box = size_dec*2;
		}
	}
	/**
	 * Starting from the image center, scan for the limits of the area where the center of the search box matches the query
	 * @throws Exception
	 */
	public void lookForLimits(int mode) throws Exception {
		if( mode == SIAPQueryTester.ENCLOSED) {
			bigroi_mode = true;
		}
		else {
			bigroi_mode = false;

		}
		setBox();
		boolean pole_passed = false;
		/*
		 * Limit sup
		 */
		boolean at_least_one = false;
		double old_dec_box = dec_box;
		double old_ra_box  = ra_box;
		old_ra_box = ra_box = ra;
		old_dec_box = dec_box = dec;
		int sens = +1;
		upper_limit[0] = ra_box;
		// limit sup
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "look for upper border");
		while( true ) {
			boolean found = false;
			SQLQuery squery = new SQLQuery();
			ResultSet rs = squery.run("Select namesaada from " + collection_table + " as i WHERE " + getConstraint(mode, "i.", ra_box, dec_box, size_ra_box, size_dec_box));
			while( rs.next() ) {
				found = true;
				at_least_one = true;
			}
			squery.close();
			//			System.out.println("Select namesaada from " + collection_table + " as i WHERE " + getConstraint(mode, "i.", ra_box, dec_box, size_ra_box, size_dec_box));
			//			squery = new SQLQuery();
			//			rs = squery.run("Select distancedegree(pos_ra_csa, pos_dec_csa, " +  ra_box + " , " + dec_box + ") from " + collection_table);
			//			while( rs.next() ) {
			//				System.out.println(ra_box + " " + dec_box + "   " + rs.getObject(1) + " " + (rs.getDouble(1)*3600));
			//			}
			//			squery.close();
			if( !at_least_one ) {
				this.status  = -1;
				return;
			}
			if( !found  ) {
				upper_limit[0] = ra_box;
				upper_limit[1] = old_dec_box;
				break;
			}
			old_dec_box = dec_box;
			dec_box += sens * this.pas_dec;
			if( dec_box > 90 ) {
				dec_box = 180 - dec_box;
				sens = -1;
				pole_passed = true;
				ra_box += 180;
				if( ra_box > 360 )ra_box -= 360;
			}
		}
		/*
		 * Limit inf: restart from the image center in order to not be lost in a parallel univers near the poles
		 */
		old_ra_box = ra_box = ra;
		old_dec_box = dec_box = dec;
		at_least_one = false;
		sens = 1;
		lower_limit[0] = ra_box;
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "look for lower border");
		while( true ) {
			boolean found = false;
			SQLQuery squery = new SQLQuery();
			ResultSet rs = squery.run("Select namesaada from " + collection_table + " as i WHERE " + getConstraint(mode, "i.", ra_box, dec_box, size_ra_box, size_dec_box));
			while( rs.next() ) {
				found = true;
				at_least_one = true;
			}
			squery.close();
			//			squery = new SQLQuery();
			//			rs = squery.run("Select distancedegree(pos_ra_csa, pos_dec_csa, " +  ra_box + " , " + dec_box + ") from " + collection_table);
			//			while( rs.next() ) {
			//				System.out.println(ra_box + " " + dec_box + "   " + rs.getObject(1)+ " " + (rs.getDouble(1)*3600));
			//			}
			//			squery.close();
			if( !at_least_one ) {
				this.status  = -1;
				return;
			}
			if( !found  ) {
				lower_limit[0] = ra_box;
				lower_limit[1] = old_dec_box;
				break;
			}
			old_dec_box =  dec_box;
			dec_box -=  sens * this.pas_dec;
			if( dec_box < -90 ) {
				dec_box = -180 - dec_box;
				sens = -1;
				pole_passed = true;
				ra_box += 180;
				if( ra_box > 360 )ra_box -= 360;
			}
		}
		/*
		 * Pole out of the image
		 */
		if( !pole_passed ) {

			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "look for left border");
			at_least_one = false;
			old_ra_box = ra_box = ra;
			old_dec_box = dec_box = dec;
			while( true ) {
				boolean found = false;
				SQLQuery squery = new SQLQuery();
				ResultSet rs = squery.run("Select namesaada from " + collection_table + " as i WHERE " + getConstraint(mode, "i.", ra_box, dec_box, size_ra_box, size_dec_box));
				while( rs.next() ) {
					found = true;
					at_least_one = true;
				}
				squery.close();
				if( !at_least_one ) {
					this.status  = -1;
					return;
				}
				if( !found  ) {
					left_limit[0] = old_ra_box;
					left_limit[1] = old_dec_box;
					//	Messenger.printMsg(Messenger.TRACE, "01 " + deux.format(corner01[0]) + " " + deux.format(corner01[1]));
					break;
				}
				old_ra_box = ra_box;
				ra_box -= (this.pas_ra)/* *Math.abs(Math.cos(Math.toRadians(corner01[1]))) */;
				if( ra_box < 0 ) {
					ra_box = 360 + ra_box;
				}
			}
			ra_box = ra;
			old_ra_box = ra_box = ra;
			old_dec_box = dec_box = dec;
			at_least_one = false;
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "look for right border");
			while( true ) {
				boolean found = false;
				SQLQuery squery = new SQLQuery();
				ResultSet rs = squery.run("Select namesaada from " + collection_table + " as i WHERE " + getConstraint(mode, "i.", ra_box, dec_box, size_ra_box, size_dec_box));
				while( rs.next() ) {
					found = true;
					at_least_one = true;
				}
				squery.close();
				if( !at_least_one ) {
					this.status  = -1;
					return;
				}
				if( !found  ) {
					right_limit[0] = old_ra_box;
					right_limit[1] = old_dec_box;
					//	Messenger.printMsg(Messenger.TRACE, "11 " + deux.format(corner11[0]) + " " + deux.format(corner11[1]));
					break;
				}
				old_ra_box = ra_box;
				ra_box += (this.pas_ra)/* *Math.abs(Math.cos(Math.toRadians(corner11[1])))*/;
				if( ra_box > 360 ) {
					ra_box = ra_box - 360;
				}
			}
		}
		else {
			Messenger.printMsg(Messenger.TRACE, "Pole passed: both left and right borders not relevant");
			left_limit[0] = (upper_limit[0] + lower_limit[0])/2;
			if( left_limit[0] > 180 ) {
				left_limit[0] = 360 - left_limit[0];
			}
			left_limit[1] = upper_limit[1];
			right_limit[1] = upper_limit[1];
			right_limit[0] = left_limit[0] + 180;
		}
	}


	/**
	 * @return
	 */
	public String getReport(){
		String retour="";
		retour += "    Image = " + deux.format(this.ra) + " " + deux.format(this.dec) + " size=" + 	 deux.format(this.size_ra) + "x" + deux.format(this.size_dec) + " deg "
		+ deux.format(this.size_ra*60) + "x" + deux.format(this.size_dec*60) + " arcmin\n";
		retour += "    " + deux.format(img_corners[2][0]) + " " + deux.format(img_corners[2][1]) + "  "
		+ deux.format(img_corners[3][0]) + " " + deux.format(img_corners[3][1])+ "\n";
		retour += "    " + deux.format(img_corners[0][0]) + " " + deux.format(img_corners[0][1]) + "  "
		+ deux.format(img_corners[1][0]) + " " + deux.format(img_corners[1][1]) + "\n";
		retour += "    Box   = " + deux.format(this.size_ra_box) + "x" + deux.format(this.size_dec_box) + " deg "
		+ deux.format(this.size_ra_box*60) + "x" + deux.format(this.size_dec_box*60) + " arcmin\n";
		if( this.status == -1 ) {
			retour +=  "no match\n";
		}
		else {
			retour += "    " + deux.format(left_limit[0]) + " " + deux.format(left_limit[1]) + "  "
			+ deux.format(lower_limit[0]) + " " + deux.format(lower_limit[1]) + "\n";
			retour += "    " + deux.format(right_limit[0]) + " " + deux.format(right_limit[1]) + "  "
			+ deux.format(upper_limit[0]) + " " + deux.format(upper_limit[1]) + "\n";
			retour += "    Height       = " + deux.format(left_limit[1] - right_limit[1])+ "\n";
			retour += "    Width top    = " + deux.format((new Astrocoo(Database.getAstroframe(),  left_limit[0], left_limit[1])).distance(new Coo(lower_limit[0], lower_limit[1])))+ "\n";
			retour += "    Width bottom = " + deux.format((new Astrocoo(Database.getAstroframe(),  right_limit[0], right_limit[1])).distance(new Coo(upper_limit[0], right_limit[1])))+ "\n";
		}
		return retour;
	}

	/**
	 * @throws IOException
	 */
	private void buildFootprint() throws IOException {
		double size_ra_arcsec = this.size_ra_box*3600/2.0;
		double size_de_arcsec = this.size_ra_box*3600/2.0;
		FileWriter fos = new FileWriter(Repository.getTmpPath() + Database.getSepar() + "siapwindow.foot");
		fos.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		fos.write("<VOTABLE xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" version=\"1.1\" xmlns=\"http://www.ivoa.net/xml/VOTable/v1.1\" xsi:schemaLocation=\"http://www.ivoa.net/xml/VOTable/v1.1 http://www.ivoa.net/xml/VOTable/v1.1\">\n");
		fos.write("<RESOURCE ID=\"SiapWindow\" utype=\"dal:footprint.geom\">\n");
		fos.write("     <PARAM datatype=\"char\" arraysize=\"*\" ID=\"TelescopeName\" value=\"MyTelescope\" />\n");
		fos.write("	    <PARAM datatype=\"char\" arraysize=\"*\" ID=\"InstrumentName\" value=\"MyInstrument\" />\n");
		fos.write("     <PARAM datatype=\"char\" arraysize=\"*\" ID=\"InstrumentDescription\" value=\"My footprint description\" />\n");
		fos.write("	    <PARAM datatype=\"char\" arraysize=\"*\" ID=\"Origin\" value=\"My footprint origin\" />\n");
		fos.write("	    <PARAM datatype=\"char\" arraysize=\"*\" utype=\"stc:AstroCoordSystem.CoordFrame.CARTESIAN\" name=\"reference frame\" value=\"*\"/>\n");
		fos.write("	    <PARAM name=\"projection\" utype=\"stc:AstroCoordSystem.CoordFrame.Cart2DRefFrame.projection\" datatype=\"char\" arraysize=\"*\" value=\"TAN\"/>\n");
		fos.write("	    <PARAM name=\"RA\" ucd=\"pos.eq.ra;meta.main\" ref=\"J2000\" datatype=\"char\" arraysize=\"13\" unit=\"&quot;h:m:s&quot;\" utype=\"stc:AstroCoordSys/SpaceFrame/OffsetCenter[1]\" value=\"13:29:55.7000\" />\n");
		fos.write("	    <PARAM name=\"DEC\" ucd=\"pos.eq.dec;meta.main\" ref=\"J2000\" datatype=\"char\" arraysize=\"12\" unit=\"&quot;d:m:s&quot;\" utype=\"stc:AstroCoordSys/SpaceFrame/OffsetCenter[2]\" value=\"+47:17:13.00\" />\n");
		fos.write("	    <PARAM name=\"PA\" ucd=\"pos.posAng\" datatype=\"float\" unit=\"deg\" utype=\"stc:AstroCoordSys/SpaceFrame/PositionAngle\" value=\"0.0\" />\n");
		fos.write("	    <PARAM name=\"Rollable\" value=\"false\" />\n");
		fos.write("	    <PARAM name=\"Movable\" value=\"true\" />\n");
		fos.write("	    <PARAM name=\"Color\" value=\"blue\" />\n");
		fos.write("	    <RESOURCE ID=\"shape_0\" name=\"shape_0\">\n");
		fos.write("	    <TABLE utype=\"dal:footprint.geom.segment\">\n");
		fos.write("	    <PARAM datatype=\"char\" name=\"Shape\" arraysize=\"*\" value=\"Polygon\" utype=\"dal:footprint.geom.segment.shape\" />\n");
		fos.write("	    <FIELD unit=\"arcsec\" datatype=\"double\" name=\"xPtPosition\" utype=\"stc:AstroCoordArea.Polygon.Vertex.Position.C1\" />\n");
		fos.write("	    <FIELD unit=\"arcsec\" datatype=\"double\" name=\"yPtPosition\" utype=\"stc:AstroCoordArea.Polygon.Vertex.Position.C2\" />\n");
		fos.write("	    <DATA>\n");
		fos.write("	    <TABLEDATA>\n");
		fos.write("	        <TR><TD>-" + size_ra_arcsec + "</TD><TD>" + size_de_arcsec + "</TD></TR>\n");
		fos.write("	        <TR><TD>" + size_ra_arcsec + "</TD><TD>" + size_de_arcsec + "</TD></TR>\n");
		fos.write("	        <TR><TD>" + size_ra_arcsec + "</TD><TD>-" + size_de_arcsec + "</TD></TR>\n");
		fos.write("	        <TR><TD>-" + size_ra_arcsec + "</TD><TD>-" + size_de_arcsec + "</TD></TR>\n");
		fos.write("	    </TABLEDATA>\n");
		fos.write("	    </DATA>\n");
		fos.write("	    </TABLE>\n");
		fos.write("	    </RESOURCE>\n");
		fos.write("		</RESOURCE>\n");
		fos.write("		</VOTABLE>\n");
		fos.close();
	}

	/**
	 * Returns a script command drawing in Aladin the area where the center of the search box match the query i
	 * @return
	 * @throws IOException 
	 */
	public String getAladinCommand() throws IOException {
		int  pdist = (int) (size_ra * 3600/2);
		int  gdist = (int) (size_ra_box * 3600/2) + pdist;
		this.buildFootprint();
		return 
		//	  "img=get aladin() " + deux.format(this.ra) + " " + deux.format(this.dec)
		"get Skyview(300,0.5,\"DSS2 Red\",Tan,J2000,0,NN) " + deux.format(this.ra) + " " + deux.format(this.dec)		+ " ;sync;draw mode(RADEC)"
		+ ";\ndraw line(" + deux.format(right_limit[0]) + " " + deux.format(right_limit[1]) + " " + deux.format(upper_limit[0]) + " " + deux.format(upper_limit[1]) + ")"
		+ ";\ndraw line(" + deux.format(upper_limit[0]) + " " + deux.format(upper_limit[1]) + " " + deux.format(left_limit[0]) + " " + deux.format(left_limit[1]) + ")"
		+ ";\ndraw line(" + deux.format(left_limit[0]) + " " + deux.format(left_limit[1]) + " " + deux.format(lower_limit[0]) + " " + deux.format(lower_limit[1]) + ")"
		+ ";\ndraw line(" + deux.format(lower_limit[0]) + " " + deux.format(lower_limit[1]) + " " + deux.format(right_limit[0]) + " " + deux.format(right_limit[1]) + ")"
		+ ";\ndraw circle(" + deux.format(this.ra) + ", " + deux.format(this.dec) + ", "+ gdist + "arcsec ) " 
		+ ";\nset \"Drawing 1\" color=red;sync;select img;"
		+ ";\nset \"Drawing 1\" PlaneID=SearchBox;"
		+ ";\ndraw line(" + deux.format(img_corners[0][0]) + " " + deux.format(img_corners[0][1]) + " " + deux.format(img_corners[1][0]) + " " + deux.format(img_corners[1][1]) + ")"
		+ ";\ndraw line(" + deux.format(img_corners[1][0]) + " " + deux.format(img_corners[1][1]) + " " + deux.format(img_corners[3][0]) + " " + deux.format(img_corners[3][1]) + ")"
		+ ";\ndraw line(" + deux.format(img_corners[3][0]) + " " + deux.format(img_corners[3][1]) + " " + deux.format(img_corners[2][0]) + " " + deux.format(img_corners[2][1]) + ")"
		+ ";\ndraw line(" + deux.format(img_corners[2][0]) + " " + deux.format(img_corners[2][1]) + " " + deux.format(img_corners[0][0]) + " " + deux.format(img_corners[0][1]) + ")"
		+ ";\ndraw circle(" + deux.format(this.ra) + ", " + deux.format(this.dec) + ", "+ pdist + "arcsec ) " 
		+ ";\nset \"Drawing 2\" color=blue;"
		+ ";\nset \"Drawing 2\" PlaneID=Image;"
		+ ";\ngrid on"
		+ ";\nLoad " + Repository.getTmpPath() + Database.getSepar() + "siapwindow.foot"
		+ ";\nget fov(SiapWindow) " +  deux.format(right_limit[0]) + " " + deux.format(right_limit[1])
		+ ";\nget fov(SiapWindow) " +  deux.format(upper_limit[0]) + " " + deux.format(upper_limit[1])
		+ ";\nget fov(SiapWindow) " +  deux.format(left_limit[0]) + " " + deux.format(left_limit[1])
		+ ";\nget fov(SiapWindow) " +  deux.format(lower_limit[0]) + " " + deux.format(lower_limit[1])
		+ ";\nset SIAP* color=red";

	}

	/**
	 * @param pos
	 * @param image_size
	 * @param mode
	 * @param fw
	 * @throws Exception
	 */
	public void processPosition(double[] pos, double[] image_size, int mode, PrintStream fw) throws Exception {
		Messenger.printMsg(Messenger.TRACE, " pos " + deux.format(pos[0]) + " " + deux.format(pos[1]));
		/*
		 * Image width is adapted to have the same apparent size
		 */
		SQLTable.beginTransaction();
		this.setImage(pos[0], pos[1], image_size[0], image_size[1]);
		SQLTable.commitTransaction();
		/*
		 * Run with a box smaller than the image
		 */
		this.lookForLimits(mode);
		if( this.bigroi_mode)
			fw.println("Mode " + SIAPQueryTester.getMODE(mode) + " (big box)");
		else 
			fw.println("Mode " + SIAPQueryTester.getMODE(mode) + " (small box)");
		fw.println(this.getReport());
		fw.println(this.getAladinCommand());
		fw.println("------------------------------------");
		/*
		 * Empty the table in order to not pollute the base with a oidsaada null
		 */
		SQLTable.beginTransaction();
		SQLTable.addQueryToTransaction("DELETE from " + collection_table);
		SQLTable.commitTransaction();
	}

	/**
	 * Main test function
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		PrintStream fw = null;
		try {
			ArgsParser ap= new ArgsParser(args);
			if( ap.getFilename() != null && ap.getFilename().length() > 0 ) {
				fw = new PrintStream(new File(ap.getFilename()));
			}
			else {
				fw = System.out;
			}
			/*
			 * Test parameter set
			 */
			double[][] pos_to_scan = new double[][]{
					   new double[]{90.0,   0.0} 
					,  new double[]{90.0,  45.0} 
					,  new double[]{90.0, -45.0}
					,  new double[]{90.0,  89.0} 
					,  new double[]{90.0, -89.0}
					,  new double[]{90.0,  89.95} 
					,  new double[]{90.0, -89.95}
					,  new double[]{90.0,  89.99} 
					,  new double[]{90.0, -89.99}
					,  new double[]{ 0.0,   0.0} 
					,  new double[]{ 0.0,  70.0} 
					,  new double[]{ 0.0, -70.0} 
					,  new double[]{ 0.0,  89.0} 
					,  new double[]{ 0.0, -89.0} 
					,  new double[]{ 0.0, +89.99} 
					,  new double[]{ 0.0, -89.99} 
					,  new double[]{360.0,   0.0}
					,  new double[]{360.0,  10.0}
					,  new double[]{360.0, -10.0}
			};	
			int[] modes = new int[]{
					SIAPQueryTester.OVERLAPS
					, SIAPQueryTester.CENTER  
					, SIAPQueryTester.ENCLOSED
					, SIAPQueryTester.COVERS
			};
			double[] image_size = new double[]{1./10., 1./10.};
			/*
			 * Init SaadaDB
			 */
			Database.init(ap.getDBName());

			Database.setAdminMode(ap.getPassword());
			/*
			 * Reload SQL procedures
			 */
			SQLTable.beginTransaction();
			Database.getWrapper().loadSQLProcedures();
			SQLTable.commitTransaction();
			Database.getConnector().getDbname();
			/*
			 * run the test
			 */
			SIAPQueryTester sqt = new SIAPQueryTester();
			Messenger.printMsg(Messenger.TRACE, "Starting to write in " + ap.getFilename());

//			sqt.processPosition(new double[]{ 90.0, 89.95}, image_size, SIAPQueryTester.OVERLAPS, fw);
//			System.exit(1);

			for( int mode: modes ) {
				Messenger.printMsg(Messenger.TRACE, "Mode " + SIAPQueryTester.getMODE(mode));
				for( double[] pos: pos_to_scan ) {
					sqt.processPosition(pos, image_size, mode, fw);
				}
			}
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
			if( fw != null ) fw.close();
		}
	}
}
