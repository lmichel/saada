package saadadb.util;

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
public class DataGenerator {
	static final private DecimalFormat un = new DecimalFormat("0.0");
	static final private DecimalFormat deux = new DecimalFormat("0.0000");
	private final static double UNEMINUTE = 1./60.;
	private int grid_halfsize = 0;
	private final String filename = System.getProperty("java.io.tmpdir") + File.separator + "votable.xml";
	protected final String collection;
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
	 * Target is stored with the oirginal form to be used into the query
	 */
	protected String target;
	protected int size;
	protected int counter = 0;
	private String className;
	private String category;
	private String name;

	/**
	 * @param pos: Center position of the surface covered by the product
	 * @param size: Size in deg of the surface covered by the product
	 * @param collection: collection of the product
	 * @param category: category of the product
	 * @param className: class of product
	 * @param name: da name of the product
	 * @throws Exception
	 */
	public DataGenerator(String pos, int size, String collection, String category, String className, String name) throws Exception {
		this.collection = collection;
		this.className = className;
		this.category = category;
		this.name = name;
		PositionParser pp = new PositionParser(pos);
		this.size = size;
		this.target = pos;
		this.grid_halfsize = (category.equals("TABLE"))? (30*size): 0;
		this.ra = pp.getRa();
		this.dec = pp.getDec();
		this.ra_target = ra;
		this.dec_target = dec;
		deux.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.ENGLISH));
		un.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.ENGLISH));		
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
			} else {
				dec = dec - UNEMINUTE;
			}
		} else  {
			if( dec > 0 ) {
				dec = dec - UNEMINUTE;
			} else {
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
			} else {
				dec = dec - UNEMINUTE;
			}
		} else  {
			if( dec > 0 ) {
				dec = dec - UNEMINUTE;
			} else {
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
		BufferedWriter bf = new BufferedWriter(new FileWriter(this.filename));
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
		bf.write("	<field name=\"name\"  datatype=\"char\" arraysize=\"*\"/>\n");
		bf.write("<DATA>    \n");  
		bf.write("<TABLEDATA>\n");
		for(int i=0 ; i<(this.grid_halfsize) ; i++ ) {
			this.decrementeRA();
			this.decrementeDEC();		 
		}
		for(int d=0 ; d<=(2*this.grid_halfsize) ; d++ ) {
			for(int i=0 ; i<=(2*this.grid_halfsize) ; i++ ) {
				bf.write("<TR>" + this.toXMLString() + "</TR>\n") ;
				this.incrementeRA();
			}				
			for(int j=0 ; j<=(2*this.grid_halfsize) ; j++ ) {
				this.decrementeRA();
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

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
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
		return filename;
	}

	/**
	 * @return a XML row made with the current position
	 */
	public String toXMLString() {
		this.counter++;
		Astrocoo coo =new Astrocoo(Database.getAstroframe(), ra, dec);
		coo.setPrecision(6);
		Astrocoo coo_target =new Astrocoo(Database.getAstroframe(), ra_target, dec_target);
		coo_target.setPrecision(6);
		return "<TD>" + coo.toString("s:") 
		+ "</TD><TD>" + deux.format(ra) 
		+ "</TD><TD>" + deux.format(dec) 
		+ "</TD><TD>" + un.format(coo.distance(coo_target)*60.)
		+ "</TD><TD>" + this.counter
		+ "</TD>" ;
	}

	/**
	 * @throws Exception
	 */
	private void prepareDBCollection(boolean emptyCollection) throws Exception{
		if( Database.getCachemeta().collectionExists(collection) ) {
			if( emptyCollection ) (new CollectionManager(collection)).empty(new ArgsParser(new String[]{ "-category=" + this.category}));				
		} else {
			(new CollectionManager(collection)).create(new ArgsParser(new String[]{"-comment=Created to store a grid"}));
		}

	}

	/**
	 * @throws Exception
	 */
	public void ingestVOTable(boolean emptyCollection) throws Exception {
		this.writeVotable();
		SQLTable.beginTransaction();
		this.prepareDBCollection(emptyCollection);
		SQLTable.commitTransaction();
		Database.getCachemeta().reload(true);
		Loader loader = new Loader(new String[]{
				  "-collection=" + collection
				, "-category=" + this.category
				, "-classfusion=" + this.className
				, "-name=RADEC,'" + this.name +"'"
				, "-ename='" + this.name +"',name,RADEC,'(',checkpos,') '"
				, "-posmapping=only"
				, "-position=RADEC"
				, "-poserror=1"
				, "-poserrorunit=arcsec"
				, "-filename=" + this.getFilename()
				, Database.getDbname()});
		loader.load();  
	}

}
