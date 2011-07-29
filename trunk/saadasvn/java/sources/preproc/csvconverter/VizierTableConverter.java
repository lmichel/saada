package preproc.csvconverter;

import java.io.IOException;

import saadadb.command.ArgsParser;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.util.Messenger;
import cds.astro.Ecliptic;
import cds.astro.FK4;
import cds.astro.FK5;
import cds.astro.Galactic;
import cds.savot.model.SavotTD;
import cds.savot.model.SavotTR;
import cds.savot.model.TDSet;


/** * @version $Id$

 * Convert an CSV Vizier ASACII table into a VOTable
 * ASCII file format should look like that
 * #Coosys	J2000:	eq_FK5 J2000
#INFO	-ref=VIZ4a670f4a7af	
#INFO	-out.max=50000	

#RESOURCE=yCat_14910093
#Name: J/ApJ/491/93
#Title: JHK photometry of 9 gamma-ray burst fields (Larson+ 1997)
#Table	J_ApJ_491_93_grbcat:
#Name: J/ApJ/491/93/grbcat
#Title: Catalogue of sources in 9 {gamma}-ray burst fields
#Column	_RAJ2000	(A10)	Right ascension (FK5) Equinox=J2000. (computed by VizieR, not part of the original data)	[ucd=pos.eq.ra;meta.main]
#Column	_DEJ2000	(A9)	Declination (FK5) Equinox=J2000. (computed by VizieR, not part of the original data)	[ucd=pos.eq.dec;meta.main]
#Column	GRB	(I6)	GRB field (name of the GRB)	[ucd=obs.field]
#Column	No	(I3)	Object number in GRB field	[ucd=meta.id]
#Column	Xpos	(I4)	X coordinate	[ucd=pos.cartesian.x;instr.det]
#Column	Ypos	(I3)	Y coordinate	[ucd=pos.cartesian.y;instr.det]
#Column	RAJ2000	(F8.4)	Right ascension (J2000) (1)	[ucd=pos.eq.ra;meta.main]
#Column	DEJ2000	(F8.4)	Declination (J2000) (1)	[ucd=pos.eq.dec;meta.main]
#Column	Jmag	(F5.2)	? J (1.25 micron) magnitude of the object (2)	[ucd=phot.mag;em.IR.J]
#Column	e_Jmag	(F5.2)	? 1-sigma error in the J magnitude (3)	[ucd=stat.error;phot.mag;em.IR.J]
#Column	Hmag	(F5.2)	? H (1.65 micron) magnitude of the object (2)	[ucd=phot.mag;em.IR.H]
#Column	e_Hmag	(F5.2)	? 1-sigma error in Hmag	[ucd=stat.error;phot.mag;em.IR.H]
#Column	K'mag	(F5.2)	Either the K' (2.11 micron) or Ks (2.16 micron) magnitude of the object (2) (4)	[ucd=phot.mag;em.IR.K]
#Column	e_K'mag	(F5.2)	? 1-sigma error in K' or Ks	[ucd=stat.error;phot.mag;em.IR.K]
#Column	S/G	(A1)	[SG] Identifies whether the object has been classified as a star (S) or a galaxy (G)	[ucd=src.class.starGalaxy]
_RAJ2000	_DEJ2000	GRB	No	Xpos	Ypos	RAJ2000	DEJ2000	Jmag	e_Jmag	Hmag	e_Hmag	K'mag	e_K'mag	S/G
"h:m:s"	"d:m:s"			pix	pix	deg	deg	mag	mag	mag	mag	mag	mag	
----------	---------	------	---	----	---	--------	--------	-----	-----	-----	-----	-----	-----	-
19 47 39.2	-70 40 32	910122	  1	 191	359	296.9134	-70.6756	19.22	 0.20	     	     	17.23	 0.10	G

.....
 * USAGE: VizierTableConverter if=inputfilename [of=outputfilename]
 *        The output file if outputfilename if the second parameter is set
 *        The output is set to stdout if outputfilename = -
 *        The output is set to inputfilename.xml if outputfilename is not set.
 * @author michel
 * @date 21/07/2009
 *
 */
public class VizierTableConverter extends ASCIToVOTableConverter {
	public static final String DELIMITER = "[|\\t]";
	public static final String COMMENT_LINE = "#[^s]*:.*";
	public static final String HEADER_LINE = "#Column" + DELIMITER + ".*";
	public static final String COOSYS_LINE = "#Coosys.*";
	public static final String DATA_START = "(-+" + DELIMITER + ")+-+";
	private String unit_line;
	/**
	 * @param inputfile
	 * @throws Exception
	 */
	VizierTableConverter(String inputfile) throws Exception {
		super(inputfile);
	}	

	@Override
	protected void readHeader() throws IOException {
		while( (current_line = reader.readLine()) != null  ) {
			if( current_line.trim().matches(COOSYS_LINE)) {
				if( current_line.matches(".*2000.*")) {
					astroframe = new FK5(2000);
				}
				else if( current_line.matches(".*1950.*")) {
					astroframe = new FK4(1950);
				}
				else if( current_line.matches(".*Galactic.*")) {
					astroframe = new Galactic();
				}
				else if( current_line.matches(".*Ecliptic.*")) {
					astroframe = new Ecliptic();
				}
				else {
					Messenger.printMsg(Messenger.WARNING, current_line + " not understood as astroframe");
				}
				Messenger.printMsg(Messenger.TRACE, "Take " + astroframe + " as astroframe");
			}
			else if( current_line.trim().matches(HEADER_LINE)) {
				header.add(current_line);
			}
			else if( current_line.matches(COMMENT_LINE)) {
				description.add(current_line);
			}
			else if( current_line.matches(DATA_START) ) {
				current_line = current_line.trim();
				break;
			}
			unit_line = current_line;
		}
	}

	@Override
	protected void setColumnDefinition() throws FatalException {
		nb_cols = header.size();
		int cpt = 0;
		for( String c: header) {
			String[] chps = c.split(DELIMITER);
			String[] units = unit_line.split(DELIMITER);
			AttributeHandler ah = new AttributeHandler();
			switch(chps.length) {
			case 5: ah.setUcd(chps[4].trim().substring(5, chps[4].length() - 1)); 
			case 4: ah.setComment(chps[3].trim());
			case 3: ah.setType(checkType(chps[2].trim()));ah.setNameorg(chps[1].trim()); break;
			default: 
				for( String s: chps) System.err.println(s);
				FatalException.throwNewException(SaadaException.FILE_FORMAT, "<" + c + "> : column not defined (" + chps.length + ") fields found");break;
			}
			if( cpt < units.length )
				ah.setUnit(units[cpt].replaceAll("\"", ""));
			ahs.add(ah);
			cpt++;
		}
		
	}
	/* (non-Javadoc)
	 * @see preproc.csvconverter.ASCIToVOTableConverter#checkType(java.lang.String)
	 */
	protected String checkType(String type) throws FatalException {
		if( type.startsWith("(I") ) {
			return "int";
		}
		else if( type.startsWith("(F") ) {
			return "double";
		}
		else if( type.startsWith("(A") ) {
			return "String";
		}
		else {
			FatalException.throwNewException(SaadaException.FILE_FORMAT, "Unknown type  " + type ) ;	
			return null;
		}
	}

	@Override
	protected void writeData() throws Exception {
		Messenger.printMsg(Messenger.DEBUG, "Start to write data");
		int cpt=0;
		while( (current_line = reader.readLine()) != null  ) {
			current_line = current_line.trim();
			if( current_line.length() > 0)
				writeLine();
			cpt++;
			if( (cpt%1000) == 0 ) {
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, cpt + " lines read");
			}
		}
		Messenger.printMsg(Messenger.DEBUG, cpt + " lines read");		
	}

	@Override
	protected void writeLine() throws Exception {
		SavotTR savotTR = new SavotTR();					
		TDSet tdSet = new TDSet();
		int cpt=0;
		String[] v = current_line.trim().split(DELIMITER);
		if( v.length == 0 ) {
			return;
		}
		if( v.length != nb_cols) {
			FatalException.throwNewException(SaadaException.FILE_FORMAT, "field number of row " + v + " is different of the column number (" + nb_cols + ")");
		}
 		for(AttributeHandler ah: ahs) {
			String val = v[cpt];
			if( ah.getType().equals("String") ) {
				val = "<![CDATA[ " + val + "]]>";
			}
			SavotTD td = new SavotTD();
			td.setContent(val);
			tdSet.addItem(td);
			cpt++;
		}
		savotTR.setTDs(tdSet);
		writer.writeTR(savotTR);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			if( args.length != 1 && args.length != 2) {
				usage();
			}
			ArgsParser ap = new ArgsParser(args);
			String ifl = ap.getIf();
			if( ifl.length() == 0 ) {
				usage();								
			}
			VizierTableConverter frw = new VizierTableConverter(ifl);				
			frw.convert(ap.getof());
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			System.exit(1);
		}

	}

}
