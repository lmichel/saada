package saadadb.vo.registry;

import saadadb.database.Database;
import saadadb.exceptions.QueryException;

public class Record {
	private Authority authority;
	private static final String header;
	
	static {
		header = "<ri:Resource \n" 
			+ "xmlns:ri=\"http://www.ivoa.net/xml/RegistryInterface/v1.0\" \n"
			+ "xmlns:cs=\"http://www.ivoa.net/xml/ConeSearch/v1.0\" \n"
			+ "xmlns:osn=\"http://www.ivoa.net/xml/OpenSkyNode/v0.2\" \n"
			+ "xmlns:sia=\"http://www.ivoa.net/xml/SIA/v1.0\" \n"
			+ "xmlns:sla=\"http://www.ivoa.net/xml/SLA/v0.2\" \n"
			+ "xmlns:ssa=\"http://www.ivoa.net/xml/SSA/v0.4\" \n"
			+ "xmlns:tsa=\"http://www.ivoa.net/xml/TSA/v0.2\" \n"
			+ "xmlns:vg=\"http://www.ivoa.net/xml/VORegistry/v1.0\" \n"
			+ "xmlns:vr=\"http://www.ivoa.net/xml/VOResource/v1.0\" \n"
			+ "xmlns:vs=\"http://www.ivoa.net/xml/VODataService/v1.0\" \n"
			+ "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n"
			+ "created=\"2007-05-21T00:00:00\" \n"
			+ "status=\"active\" \n"
			+ "updated=\"2007-05-21T00:00:00\" \n"
			+ "xsi:schemaLocation=\"http://www.ivoa.net/xml/VOResource/v1.0 http://www.ivoa.net/xml/VOResource/v1.0 http://www.ivoa.net/xml/VORegistry/v1.0 http://www.ivoa.net/xml/VORegistry/v1.0\" \n"
			+ "xsi:type=\"vg:Authority\">\n";
	}
	
	/**
	 * @throws QueryException
	 */
	public Record() throws QueryException {
		this.authority = new Authority();
		this.authority.load();
	}
	
	/**
	 * @return
	 */
	public StringBuffer getTAPRecord() {
		StringBuffer retour = new StringBuffer();
		retour.append(header);
		retour.append(this.authority.getXML());
		
		retour.append("  <capability standardID=\"ivo://ivoa.net/std/VOSI#availability\">\n");
		retour.append("   <interface xsi:type=\"vs:ParamHTTP\">\n");
		retour.append("       <accessURL use=\"full\">" + Database.getUrl_root() + "/tap/availability</accessURL>\n");
		retour.append("   </interface>\n");
		retour.append("  </capability>\n");
		retour.append("  <capability standardID=\"ivo://ivoa.net/std/VOSI#capabilities\">\n");
		retour.append("   <interface xsi:type=\"vs:ParamHTTP\">\n");
		retour.append("       <accessURL use=\"full\">" + Database.getUrl_root() + "/tap/capabilities</accessURL>\n");
		retour.append("   </interface>\n");
		retour.append("  </capability>\n");
		retour.append("  <capability standardID=\"ivo://ivoa.net/std/VOSI#tables\">\n");
		retour.append("    <interface xsi:type=\"vs:ParamHTTP\">\n");
		retour.append("       <accessURL use=\"full\">" + Database.getUrl_root() + "tap/tables</accessURL>\n");
		retour.append("   </interface>\n");
		retour.append("  </capability>\n");
		retour.append("</ri:Resource>\n"); 
		
		return retour;
	}

	/**
	 * @return
	 */
	public StringBuffer getSIARecord() {
		StringBuffer retour = new StringBuffer();
		retour.append(header);
		retour.append(this.authority.getXML());
		retour.append("<capability standardID=\"ivo://ivoa.net/std/SIA\" xsi:type=\"sia:SimpleImageAccess\">\n");
		retour.append("    <interface role=\"std\" xsi:type=\"vs:ParamHTTP\">\n");
		retour.append("	       <accessURL use=\"base\">\n");
 		retour.append("             http://skyview.gsfc.nasa.gov/cgi-bin/vo/sia.pl?survey=2mass&\n");
		retour.append("        </accessURL>\n");
		retour.append("        <queryType>GET</queryType>\n");
		retour.append("        <resultType>text/xml+votable</resultType>\n");
		retour.append("        <param>\n");
		retour.append("            <name>POS</name>\n");
		retour.append("            <description>\n");
 		retour.append("            Search Position in the form \"ra,dec\" where ra and dec are given in decimal degrees\n");
		retour.append("            in the ICRS coordinate system.\n");
		retour.append("            </description>\n");
		retour.append("            <unit>degrees</unit>\n");
		retour.append("            <dataType>real</dataType>\n");
		retour.append("        </param>\n");
		retour.append("        <param>\n");
		retour.append("            <name>SIZE</name>\n");
 		retour.append("            <description>\n");
 		retour.append("            Size of search region in the RA and Dec. directions.   \n");
 		retour.append("            </description>\n");
 		retour.append("            <unit>degrees</unit>\n");
 		retour.append("            <dataType>real</dataType>\n");
 		retour.append("        </param>\n");
 		retour.append("        <param>\n");
 		retour.append("            <name>FORMAT</name>\n");
 		retour.append("            <description>\n");
 		retour.append("            Requested format of images.\n");
		retour.append("            </description>\n");
		retour.append("            <dataType>string</dataType>\n");
		retour.append("        </param>\n");
 		retour.append("        <param>\n");
 		retour.append("            <name>CFRAME</name>\n");
 		retour.append("            <description>\n");
 		retour.append("            Coordinate frame: ICRS, FK5, FK4, GAL, ECL\n");
 		retour.append("            </description>\n");
		retour.append("            <dataType>string</dataType>\n");
		retour.append("        </param>\n");
		retour.append("        <param>\n");
		retour.append("            <name>EQUINOX</name>\n");
		retour.append("            <description>\n");
		retour.append("            Equinox used in FK4 or FK5 frames.\n");
		retour.append("            </description>\n");
 		retour.append("            <dataType>real</dataType>\n");
		retour.append("        </param>\n");
 		retour.append("    </interface>\n");
 		retour.append("    <imageServiceType>Cutout</imageServiceType>\n");
 		retour.append("    <maxQueryRegionSize>\n");
		retour.append("        <long>360.0</long>\n");
		retour.append("        <lat>180.0</lat>\n");
 		retour.append("    </maxQueryRegionSize>\n");
 		retour.append("    <maxImageExtent>\n");
 		retour.append("        <long>360.0</long>\n");
 		retour.append("        <lat>180.0</lat>\n");
 		retour.append("    </maxImageExtent>\n");
 		retour.append("    <maxImageSize>\n");
		retour.append("        <long>5000</long>\n");
		retour.append("        <lat>5000</lat>\n");
		retour.append("    </maxImageSize>\n");
		retour.append("    <maxFileSize>10000000</maxFileSize>\n");
		retour.append("    <maxRecords>500</maxRecords>\n");
		retour.append("    <testQuery>\n");
		retour.append("        <pos>\n");
		retour.append("            <long>0</long>\n");
		retour.append("            <lat>0</lat>\n");
		retour.append("        </pos>\n");
		retour.append("        <size>\n");
		retour.append("            <long>1</long>\n");
		retour.append("            <lat>1</lat>\n");
		retour.append("        </size>\n");
		retour.append("     </testQuery>\n");
		retour.append("</capability>\n");
		retour.append("<capability>\n");
 		retour.append("<interface xsi:type=\"vr:WebBrowser\"><accessURL use=\"base\">\n");
		retour.append("           http://skyview.gsfc.nasa.gov/cgi-bin/query.pl\n");
		retour.append("         </accessURL></interface>\n");
		retour.append("       </capability>\n");
		return retour;
	}
}
