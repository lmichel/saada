package saadadb.query.parser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import saadadb.database.Database;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.products.inference.Coord;
import saadadb.util.Messenger;
import saadadb.vocabulary.RegExp;
import cds.astro.Astrocoo;
import cds.astro.Astroframe;
import cds.astro.FK5;

/**
 * @author michel
 * @version $Id$
 * 02/2014: Do the conversion only if the given frame differs from the this of the DB
 * 10/2014: set a text report
 */
public class PositionParser {
	private String position;
	private int format=0 ;
	private double ra=-1., dec=-1;;
	public static final int DECIMAL=1;
	public static final int HMS=2;
	public static final int NAME=3;
	public static final int NOFORMAT=0;
	private static Pattern p = Pattern.compile(RegExp.POSITION_COORDINATE);	
	private Astroframe astroframe;
	private String arg;
	private String report="";
	
	/**
	 * @param position
	 * @throws ParsingException
	 */
	public PositionParser(String position) throws QueryException {
		this.position = position.trim();
		this.arg = position.trim();
		this.astroframe = (Astroframe) Database.getAstroframe();
		this.checkPrintable();
		this.setFormat();
	}
	
	/**
	 * @param position
	 * @throws ParsingException
	 */
	public PositionParser(String position, Astroframe astroframe) throws QueryException {
		this.position = position.trim();
		this.astroframe = astroframe;
		this.checkPrintable();
		this.setFormat();
	}
	
	/**
	 * @param str
	 * @return
	 * @throws ParsingException
	 */
	public static final double[] getRaDec(String str, Astroframe astroframe) throws QueryException{
		PositionParser pp = new PositionParser(str,astroframe);
		return new double[]{pp.getRa(),pp.getDec()};
	}
	
	private final void checkPrintable() throws QueryException {
		for( int i=0 ; i<position.length() ; i++ ){
			if( position .charAt(i) < 32 ) {
				QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "'" + position + "' Non printable characters in position.");								
			}
		}		
	}
	
	/**
	 * Extract from the position string the coordinates by using Sezame to solve name
	 * or by using Astroframe to convert coordinates format in double values.
	 * Coordinates are returned n the database system
	 * @throws ParsingException 
	 */
	private void setFormat() throws QueryException {
		Matcher m = p.matcher(position);
		if( position.trim().matches("[0-9]+[:\\s]([0-9]+[:\\s])?[0-9]+(\\.[0-9]+)?[,;\\s]*[+-][0-9]+[:\\s]([0-9]+[:\\s])?[0-9]+(\\.[0-9]+)?") ) {
			this.format = HMS;
			this.position = this.position.replaceAll("[,;]", "");		
			try {
				Astrocoo acoo = new Astrocoo(this.astroframe,this.position);
				this.ra = acoo.getLon();
				this.dec = acoo.getLat();
				this.report = "Converted from sexadecimal";
			} catch (Exception e) {
				QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "'" + this.arg + "' Position format not recognized");				
			}

		}
		/*
		 * Decimal case must be parsed because Astroframe requires a signed declination
		 * and exponential notation is not supported
		 */
		else if( m.find() && m.groupCount() == 2 ) {
			DecimalFormat deux = new DecimalFormat("0.000000");
			deux.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.ENGLISH));
			/*
			 * Extract both coordinates from the position string and format them
			 * into a format recognized by Astroframe
			 */
			try {
				this.ra = Double.parseDouble(m.group(1).trim().replace('E', 'e'));
				this.dec = Double.parseDouble(m.group(2).trim().replaceAll("[,:;]", " ")
						.replaceAll("\\-\\+", " ")
						.replace('E', 'e')
				);
				this.format = DECIMAL;
			} catch(Exception e) {
				Messenger.printStackTrace(e);
				return;
			}
			if( this.dec > 0 ) {
				this.position  = deux.format(ra) + " +" +  deux.format(this.dec);
			}
			else {
				this.position  = deux.format(ra) + " " + deux.format(this.dec);
			}
			//System.out.println("==> " + this.position);
		}
		else {
			this.format = NAME;        	
			String ResolveName = this.resolvesName();
			if (ResolveName != null ){
				this.report = "Resolved by Sesam";
				this.position = ResolveName.trim();
				/*
				 * Sezam returns FK5/J2000 coordinates, but input coordinates can be expressed 
				 * in another system. The following conversion assures thet the returned position will
				 * be expressed in the right system even if it is a name to solve.
				 */
				if( this.astroframe != null ) {
					try {
						Astrocoo acoo = new Astrocoo(this.astroframe,this.position);
						double converted_coord[] = Coord.convert(new FK5(), new double[]{acoo.getLon(), acoo.getLat()}, this.astroframe);
						this.ra = converted_coord[0];
						this.dec = converted_coord[1];
						this.position = new Astrocoo(this.astroframe, this.ra, this.dec).toString("s:");
					} catch (Exception e) {
						QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "'" + this.arg + "' Position returned by Sezam canot be converted in system <" + this.astroframe + ">");
					}	
				}
			}
			else {
				this.format = NOFORMAT;     
				String msg = "<" + this.position + "> Can not be resolved";
				this.position = "";
				QueryException.throwNewException(SaadaException.WRONG_PARAMETER, msg);
			}
		}
		
		if( this.format != NOFORMAT  ){
			try {
				/*
				 * Convert the result in the Database coord system
				 */
				if( this.astroframe != null ) {
					double converted_coord[];
					if( !this.astroframe.getClass().getName().equals( Database.getAstroframe().getClass().getName()) ) {
						converted_coord = Coord.convert(this.astroframe, new double[]{this.ra, this.dec}, Database.getAstroframe());
						if (Messenger.debug_mode)
							Messenger.printMsg(Messenger.DEBUG, "Convert " + this.ra + " " + this.dec + " " + this.astroframe + " to " + converted_coord[0]+ " " + converted_coord[1] + " " + Database.getAstroframe());
					} else {
						converted_coord = new double[]{this.ra, this.dec};
					}
					this.ra=converted_coord[0];
					this.dec=converted_coord[1];
				}
			} catch (Exception e) {
				e.printStackTrace();
				QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "'" + this.arg + "' Position format not recognized");
			}	
		}
		else {
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "'" + this.arg + "' Position format not recognized");
		}		
	}
	
    /**
     * @return
     */
    public int getFormat() {
    	return this.format;
    }
    
 
    /**
     * @return
     */
    public  String resolvesName() {
    	return PositionParser.resolvesName(this.position);
     }
    
    /**
     * @param name
     * @return
     */
    public static String resolvesName(String name) {
    	try {
    		String target = URLEncoder.encode(name, "iso-8859-1");
    		URL cgi_sesame = new URL("http://cdsweb.u-strasbg.fr/cgi-bin/nph-sesame/-ox?" + target);
    		BufferedReader br = new BufferedReader(new InputStreamReader(cgi_sesame.openStream()));
    		String inputLine;
    		String xml = "";
    		while ((inputLine = br.readLine()) != null) {
    			xml += inputLine.replaceAll("JPOS", "jpos");
    		}
    		br.close();
    		
    		if (xml.indexOf("<jpos>")>0) {
    			return xml.substring(xml.indexOf("<jpos>")+6, xml.indexOf("</jpos>"));
    		}
    		else {
    			return  null;
    		}
    	}
    	catch (Exception e ) {
    		Messenger.printMsg(Messenger.ERROR, "Service 'http://cdsweb.u-strasbg.fr/cgi-bin/nph-sesame/' not available");;
    	return null;
    	}
    }
    
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Messenger.debug_mode = false;
		Database.init("DEVBENCH1_5");
		String[] positions = {
				  "12.34+123.5"
				, "12.34 +123.5"
				, "+12.34,+123.5"
				, "12.34: +123.5"
				, "-12.34-+123.5"
				, "277.5,12.9"
				, "M104"
				, "1:2:3.8 +21:32:43.3"
				, "1:2:3.8+21:32:43.3"
				, "1 2 3.8 +21 32 43.3"
				, "1 2 3.8+21:32:43.3"
				, "1 2 3.8;+21:32:43.3"
				, "0,+0"
				, "0+0"
				, "0,33330"
				, "1e-5,3e-1"
				, "30,60"
				, "30,-60."
				, "10,+39."
				, "10.,39."
				, "00:00:27.67920-25:04:42.7440"
				, "40.500000 +42.740000"
				, "1:37:51.01+0:38:36.6"
				, "1:38:51.01+0:38:36.6"
				, "dasdasdaadsa"
				, "05 23.7 -69 37"
				, "PG1216+069"
				
		};

		for( int i=0 ; i<positions.length ; i++ ) {
			PositionParser pp;
			try {
				pp = new PositionParser(positions[i]);
				System.out.println(positions[i] + "    " + pp.getFormat() + " ==>" + pp.getRa() + " " + pp.getDec());
			} catch (QueryException e) {
				Messenger.printStackTrace(e);
			}
		}

		Database.close();
	}

	/**
	 * @return Returns the dec.
	 */
	public double getDec() {
		return dec;
	}

	/**
	 * @return Returns the ra.
	 */
	public double getRa() {
		return ra;
	}

	/**
	 * @return Returns the position.
	 */
	public String getPosition() {
		return position;
	}
	
	/**
	 * @return
	 */
	public String getReport() {
		return this.report;
	}

}
