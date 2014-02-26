package saadadb.query.region.test.gridcreator;

/******************************************************************************
 *  This file is part of SAADA 1.7.0
 *
 *  SAADA is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  SAADA is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with SAADA; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *  
 *  (c) Copyright 2003, 2013 L. MICHEL, H. NGUYEN NGOC, F.X. PINEEAU 
 *                           CNES/Universite Louis Pasteur/CNRS
 * 
 *  FITSWCS  (c) Copyright 1996 Raymond L. Plante Mark Calabretta Jef Poskanser
 *           (c) Copyright 1991 1996 Free Software Foundation Inc.       
 *  Sezam    (c) Copyright 2002-2003 Andre Schaaff Universite Louis Pasteur / CNRS
 *  FITS tam (c) Copyright 1997-2008: Thomas McGlynn 1997-2007.
 *  Axis     (c) Copyright 2001,2004 The Apache Software Foundation.
 *  CDS      (c) Copyright 1999-2007 - Universite Louis Pasteur / CNRS
 ******************************************************************************/

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import saadadb.collection.CollectionManager;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.dataloader.old.Loader;
import saadadb.exceptions.QueryException;
import saadadb.query.parser.PositionParser;
import saadadb.sqltable.SQLTable;
import cds.astro.Astrocoo;

/*
 * This class is use to test queries on position. Both position and search radius are given to the creator.
 * A votable is the built with one source for each node of a grid with 1 arcmin as step and covering 1.5 time
 * the search radius. This VOtable is ingested into the database and a the query centered on that grid is run.
 * IN addition with the position the Votable entries have a column with the distance to the grid center making then easier
 * the control of the object selected by the query
 * The grid is built by moving a position by 1arcmin steps. That is not efficient, but this feature was expected to be used 
 * on others contexts.
 */
public class GridBuilder {
	static final private DecimalFormat un = new DecimalFormat("0.0");
	static final private DecimalFormat deux = new DecimalFormat("0.0000");
	private final static double UNEMINUTE = 1./60.;
	private int grid_halfsize = 0;
	private String filename;
	protected final String collection;
	protected final String classe;
	/*
	 * Current position used to build the grid
	 */
	private double ra;
	private double dec;
	/*
	 * Target decimal position
	 */
	private double ra_target;
	private double dec_target;
	/*
	 * 
	 * 
	 * Target is stored with the oirginal form to be used into the query
	 */
	protected String target;
	protected int size;

	private double ra_init;
	private double dec_init;


	/**
	 * @param pos
	 * @param size
	 * @throws QueryException
	 */
	GridBuilder(String pos, int size, String collection, String nom) throws Exception {
		this.filename=nom;
		this.collection = collection;
		this.classe = collection + "Grid";
		PositionParser pp = new PositionParser(pos);
		this.size = size;
		this.target = pos;
		grid_halfsize = size; //demi hauteur en arcmin
		ra = pp.getRa();
		dec = pp.getDec();
		ra_init = pp.getRa();
		dec_init = pp.getDec();
		ra_target = ra;
		dec_target = dec;
		deux.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.ENGLISH));
		un.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.ENGLISH));

		this.writeVotable();
		SQLTable.beginTransaction();
		this.prepareDBCollection();
		SQLTable.commitTransaction();
		Database.getCachemeta().reload(true);
		this.ingestVOTable();
	}

	public GridBuilder(String choix,String bdd, String collection) throws Exception {
		Database.init(bdd);
		this.collection = collection;
		this.classe = collection + "Grid";
		SQLTable.beginTransaction();
		if( "add".equals(choix) ) {
			this.prepareDBCollection();
		}
		else if( "delete".equals(choix) ){
			this.deleteDBCollection();
		}
		else if( "empty".equals(choix) ){
			this.emptyDBCollection();
		}
		else {
			System.out.println("Erreur choix");
		}
		SQLTable.commitTransaction();
		Database.getCachemeta().reload(true);
	}


	/**
	 * Increase the current as ascension with 1 arcmin
	 * Handle circular periodicity
	 */
	private void incrementeRA() {
		ra = ra + UNEMINUTE;
		if( ra >= 360 ) {
			ra = ra - 360;
		}
	}
	/**
	 * Decrease the current ascencion with 1 arcmin
	 * Handle circular periodicity
	 */
	private void decrementeRA() {
		ra = ra - UNEMINUTE;
		if( ra < 0 ) {
			ra = 360 + ra;
		}
	}

	/**
	 * Increase the current as declination with 1 arcmin
	 * Handle pole
	 */

	private void incrementeDEC() {
		if( ra < 180 ) {
			if( dec > 0 ) {
				dec = dec + UNEMINUTE;
				if( dec > 90) {
					dec = 180 - dec;
					ra = 180 + ra;
				}	
			}
			else if (dec==0){
				dec=dec + UNEMINUTE;
			}
			else {
				dec = dec - UNEMINUTE;
			}
		}
		else  {
			if( dec > 0 ) {
				dec = dec - UNEMINUTE;
			}
			else if (dec==0){
				dec = dec + UNEMINUTE;
			}
			else {
				dec = dec + UNEMINUTE;
				if( dec < -90) {
					dec = -dec-180;
					ra = ra -180;
				}	
			}
		}
	}

	/**
	 * Decrease the current as declination with 1 arcmin
	 * Handle pole
	 */
	private void decrementeDEC() {
		if( ra > 180 ) {
			if( dec > 0 ) {
				dec = dec + UNEMINUTE;
				if( dec > 90) {
					dec = 180 - dec;
					ra = ra  - 180 ;
				}	
			}
			else if (dec==0){
				dec = dec - UNEMINUTE;
			}
			else {
				dec = dec - UNEMINUTE;
			}
		}
		else  {
			if( dec > 0 ) {
				dec = dec - UNEMINUTE;
			}
			else if (dec==0){
				dec = dec - UNEMINUTE;
			}
			else {

				dec = dec + UNEMINUTE;
				if( dec < -90) {
					dec = -dec-180;
					ra = ra +180;
				}	
			}
		}
	}

	/**
	 * Build the grid and generate the VOTable
	 * @throws IOException
	 * @throws QueryException
	 */
	public void writeVotable() throws IOException, QueryException {
		File vot = new File ("/tmp/"+this.filename+".xml");
		vot.createNewFile();
		BufferedWriter bf = new BufferedWriter(new FileWriter(vot));
		bf.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		bf.write("<VOTABLE version=\"1.1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
		bf.write("  xmlns=\"http://www.ivoa.net/xml/VOTable/v1.1\"\n");
		bf.write("  xsi:schemaLocation=\"http://www.ivoa.net/xml/VOTable/v1.1 http://www.ivoa.net/xml/VOTable/v1.1\">\n");
		bf.write(" <DESCRIPTION>\n");
		bf.write("   Test des positions\n");
		bf.write(" </DESCRIPTION>\n");
		bf.write("<RESOURCE ID=\"secondary1\" name=\"secondary1\">\n");
		bf.write("  <TABLE ID=\"secondary1\" name=\"secondary1\">\n");
		bf.write("	<field name=\"RADEC\"  datatype=\"char\" arraysize=\"*\"/>\n");
		bf.write("	<field name=\"ascension\" datatype=\"float\"/>\n");
		bf.write("	<field name=\"declination\" datatype=\"float\"/>\n");
		bf.write("	<field name=\"checkpos\" datatype=\"float\"/>\n");
		bf.write("<DATA>    \n");  
		bf.write("<TABLEDATA>\n");


		ra=ra_init;
		dec=dec_init;

		for(int i=0 ; i<(this.grid_halfsize) ; i++ ) {
			this.decrementeRA();	 
		}
		for(int d=0 ; d<=(this.grid_halfsize) ; d++ ) {
			for(int i=0 ; i<=(2*this.grid_halfsize) ; i++ ) {
				bf.write("<TR>" + this.toXMLString() + "</TR>\n") ;
				this.incrementeRA();
			}				
			for(int j=0 ; j<=(2*this.grid_halfsize) ; j++ ) {
				this.decrementeRA();
			}
			this.decrementeDEC();		 
		}

		ra=ra_init;
		dec=dec_init;

		for(int i=0 ; i<(this.grid_halfsize) ; i++ ) {
			this.incrementeRA();	 
		}
		for(int d=0 ; d<=(this.grid_halfsize) ; d++ ) {
			for(int i=0 ; i<=(2*this.grid_halfsize) ; i++ ) {
				bf.write("<TR>" + this.toXMLString() + "</TR>\n") ;
				this.decrementeRA();
			}				
			for(int j=0 ; j<=(2*this.grid_halfsize) ; j++ ) {
				this.incrementeRA();
			}
			this.incrementeDEC();		 
		}

		bf.write("</TABLEDATA>\n") ;
		bf.write("</DATA>\n") ;
		bf.write("</TABLE>\n") ;
		bf.write("</RESOURCE>\n") ;
		bf.write("</VOTABLE>\n") ;
		bf.close();
	}

	public String toString() {
		Astrocoo coo =new Astrocoo(Database.getAstroframe(), ra, dec);
		Astrocoo coo_target =new Astrocoo(Database.getAstroframe(), ra_target, dec_target);
		//Coo.setDecimals(4);
		coo.setPrecision(6);
		return coo.toString("s:") + " " +  un.format(coo.distance(coo_target)*60.);
	}
	/**
	 * @return the filename
	 */
	public String getFilename() {
		return "/tmp/"+filename+".xml";
	}

	/**
	 * @return a XML row made with the current position
	 */
	public String toXMLString() {
		Astrocoo coo =new Astrocoo(Database.getAstroframe(), ra, dec);
		coo.setPrecision(6);
		Astrocoo coo_target =new Astrocoo(Database.getAstroframe(), ra_target, dec_target);
		coo_target.setPrecision(6);
		return "<TD>" + coo.toString("s:") + "</TD><TD>" + deux.format(ra) + "</TD><TD>" + deux.format(dec) + "</TD><TD>" + un.format(coo.distance(coo_target)*60.)+ "</TD>" ;
	}

	private void prepareDBCollection() throws Exception{
		if( Database.getCachemeta().collectionExists(collection) ) {			
		}
		else {
			(new CollectionManager(collection)).create(new ArgsParser(new String[]{"-collection="+collection}));
		}
	}

	private void deleteDBCollection() throws Exception {
		if( Database.getCachemeta().collectionExists(collection) ) {
			(new CollectionManager(collection)).remove(new ArgsParser(new String[]{"-collection="+collection}));
		}
	}

	private void emptyDBCollection() throws Exception {
		if( Database.getCachemeta().collectionExists(collection) ) {
			(new CollectionManager(collection)).empty(new ArgsParser(new String[]{"-collection="+collection}));
		}
	}

	/**
	 * @throws Exception
	 */
	private void ingestVOTable() throws Exception {
		Loader loader = new Loader(new String[]{"-classifier=" + classe
				, "-collection=" + collection
				, "-category=table"
				, "-ename=RADEC,'(',checkpos,')'"
				, "-posmapping=only"
				, "-position=RADEC"
				, "-poserror=1"
				, "-poserrorunit=arcsec"
				, "-filename=" + this.getFilename()
				, "-noindex"
				, Database.getDbname()});	
		loader.load();  
	}


	public static void execute(String[] arg) {

		ArgsParser ap;
		try {
			ap = new ArgsParser(arg);
			String target = arg[0];
			int size = Integer.parseInt(arg[1]);
			String nomCOL=arg[2];
			String nomVOT=arg[3];
			Database.init(arg[4]);
			Database.setAdminMode(ap.getPassword());

			new GridBuilder(target, size, nomCOL,nomVOT);

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("USAGE java GridBuilder [position] [size (minutes)] -collection=[collection] SAADADB_NAME");

		}
	}
}
