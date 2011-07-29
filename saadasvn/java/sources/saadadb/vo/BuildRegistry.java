package saadadb.vo;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import saadadb.database.Database;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;

/** * @version $Id$

 * Build registry and GLU record from a template
 * @author michel
 *
 */
abstract public class BuildRegistry {
	public static final String template = 
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
		+	"<ri:Resource xmlns:ri=\"http://www.ivoa.net/xml/RegistryInterface/v1.0\"\n"
		+	"    xmlns:cs=\"http://www.ivoa.net/xml/ConeSearch/v1.0\"\n"
		+	"    xmlns:osn=\"http://www.ivoa.net/xml/OpenSkyNode/v0.2\" \n"
		+	"    xmlns:sia=\"http://www.ivoa.net/xml/SIA/v1.0\"\n"
		+	"    xmlns:sla=\"http://www.ivoa.net/xml/SLA/v0.2\"\n"
		+	"    xmlns:ssa=\"http://www.ivoa.net/xml/SSA/v0.4\"\n"
		+	"    xmlns:tsa=\"http://www.ivoa.net/xml/TSA/v0.2\"\n"
		+	"    xmlns:vg=\"http://www.ivoa.net/xml/VORegistry/v1.0\" \n"
		+	"    xmlns:vr=\"http://www.ivoa.net/xml/VOResource/v1.0\"\n"
		+	"    xmlns:vs=\"http://www.ivoa.net/xml/VODataService/v1.0\"\n"
		+	"    xmlns:xlink=\"http://www.w3.org/1999/xlink\"\n"
		+	"    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n"
		+	"    created=\"" + (new SimpleDateFormat("yyyy-MM-dd")).format(new Date()) + "\" \n" 
		+   "    status=\"active\" \n"
		+   "    updated=\"" + (new SimpleDateFormat("yyyy-MM-dd")).format(new Date()) + "\"\n"
		+   "    xsi:schemaLocation=\"http://www.ivoa.net/xml/RegistryInterface/v1.0\n"
		+   "    http://www.ivoa.net/xml/RegistryInterface/v1.0\n"
		+   "    http://www.ivoa.net/xml/VOResource/v1.0\n"
		+   "    http://www.ivoa.net/xml/VOResource/v1.0\n"
		+   "    http://www.ivoa.net/xml/VORegistry/v1.0\n"
		+   "    http://www.ivoa.net/xml/VORegistry/v1.0\n"
		+   "    http://www.ivoa.net/xml/VODataService/v1.0\n"
		+   "    http://www.ivoa.net/xml/VODataService/v1.0\n"
		+   "    http://www.ivoa.net/xml/SIA/v1.0\n"
		+   "    http://www.ivoa.net/xml/SIA/v1.0\n"
		+   "    http://www.ivoa.net/xml/ConeSearch/v1.0\n"
		+   "    http://www.ivoa.net/xml/ConeSearch/v1.0\n"
		+   "    http://www.ivoa.net/xml/OpenSkyNode/v0.2\n"
		+   "    http://www.ivoa.net/xml/OpenSkyNode/v0.2\n"
		+   "    http://www.ivoa.net/xml/SSA/v0.4\n"
		+   "    http://www.ivoa.net/xml/SSA/v0.4\n"
		+   "    http://www.ivoa.net/xml/SLA/v0.2\n"
		+   "    http://esavo.esa.int/xml/SLA-v0.2.xsd\n"
		+   "    http://www.ivoa.net/xml/TSA/v0.2\n"
		+   "    http://esavo.esa.int/xml/TSA-v0.2.xsd\"\n"
		+   "    xsi:type=\"vs:CatalogService\">\n"
		+	"    <title>_TITLE_</title>\n"
		+	"    <shortName>VO Service From SaadaDB " + Database.getName() + "</shortName>\n"
		+	"    <identifier>PUT HERE YOUR THE RESOURCE IDENTIFIER (ivo://.../resource)</identifier>\n"
		+	"    <curation>\n"
		+	"        <publisher ivo-id=\"PUT HERE YOUR PUBLISHER IDENTIFIER (ivo://...)\">\n"
		+	"        PUT HERE YOUR PUBLISHER NAME IN FULL TEXT\n"
		+	"        </publisher>\n"
		+	"        <creator>\n"
		+	"            <name>PUT HERE THE NAME OF THE DATA CREATOR</name>\n"
		+	"            <logo><![CDATA[" + Database.getUrl_root() + "/images/saadatransp-text.gif]]></logo>\n"
		+	"        </creator>\n"
		+	"        <date>" + (new SimpleDateFormat("yyyy-MM-dd")).format(new Date()) + "</date>\n"
		+	"        <contact>\n"
		+	"            <name>PUT HERE YOU NAME/ADDRESS</name>\n"
		+	"            <address>PUT HERE YOU NAME/ADDRESS</address>\n"
		+	"            <email>PUT HERE YOU MAIL</email>\n"
		+	"        </contact>\n"
		+	"    </curation>\n"
		+	"    <content>\n"
		+	"        <subject>LocalData</subject>\n"
		+	"        <!-- Example \n"
		+	"        <subject>Stars</subject>\n"
		+	"        <subject>Galaxies</subject>\n"
		+	"        <subject>Surveys</subject>\n"
		+	"        -->\n"
		+	"        <description>\n"
		+	"        _DESCRIPTION_\n"
		+	"        </description>\n"
		+	"        <referenceURL><![CDATA[" + Database.getUrl_root() + "]]></referenceURL>\n"
		+	"        <!-- Example \n"
		+	"        <type>Archive</type>\n"
		+	"        <type>Organisation</type>\n"
		+	"        -->\n"
		+	"        <contentLevel>Research</contentLevel>\n"
		+	"        <!-- Example -->\n"
		+	"        <!--relationship>\n"
		+	"            <relationshipType>service-for</relationshipType>\n"
		+	"            <relatedResource ivo-id=\"ivo://.....\">\n"
		+	"            RESOURCE DESCRIPTION IN FULL TEXT\n"
		+	"            </relatedResource>\n"
		+	"        </relationship -->\n"
		+	"    </content>\n"
		+	"    <capability standardID=\"_STANDARD_\" xsi:type=\"_TYPE_\">\n"
		+	"        <interface xsi:type=\"vs:ParamHTTP\" _ROLE_>\n"
		+	"            <accessURL use=\"base\"><![CDATA[_URL_]]></accessURL>\n"
		+	"            <queryType>GET</queryType>\n"
		+	"            <resultType>application/xml+votable</resultType>\n"
		+	"        </interface>\n"
		+	"        <complianceLevel>query</complianceLevel>\n"
		+	"        <!-- Example\n"
		+	"        <dataSource>pointed</dataSource>\n"
		+	"        <creationType>archival</creationType>\n"
		+	"        -->\n"
		+	"        <verbosity>false</verbosity>\n"
		+	"        <maxSR>1</maxSR>\n"
		+	"        <maxRecords>1000</maxRecords>\n"
		+	"    </capability>\n"
		+	"    <coverage>\n"
		//+	"        <stc:STCResourceProfile xmlns:stc=\"http://www.ivoa.net/xml/STC/stc-v1.30.xsd\">\n"
		//+	"            <stc:AstroCoordSystem id=\".....\" xlink:href=\"ivo://STClib/CoordSys#UTC-ICRS-TOPO\" xlink:type=\"simple\"/>\n"
		//+	"            <stc:AstroCoordArea coord_system_id=\"......\">\n"
		//+	"            <stc:AllSky/>\n"
		//+	"        </stc:STCResourceProfile>\n"
		+	"        <!-- Example\n"
		+	"        <waveband>Infrared</waveband>\n"
		+	"        <waveband>Optical</waveband>\n"
		+	"        <waveband>UV</waveband>\n"
		+	"        -->\n"
		+	"    </coverage>\n"
		+	"</ri:Resource>";


	/**
	 * @param pseudo_table
	 * @param datamodel
	 * @param withrel
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public static String getConeSearchURL(String pseudo_table, String datamodel, boolean withrel) throws UnsupportedEncodingException {
		String url = Database.getUrl_root() + "/conesearch?collection="  + pseudo_table + "&";
		if( datamodel != null && datamodel.length() > 0 ) {
			url += "dm=" + URLEncoder.encode(datamodel, "iso-8859-1")+ "&";
		}
		if( withrel ) {
			url += "withrel=true&";
		}
		return url;
	}
	/**
	 * @param pseudo_table
	 * @param datamodel
	 * @param withrel
	 * @return
	 * @throws QueryException 
	 */
	public static String getConeSearchDescription(String pseudo_table, String datamodel, boolean withrel) throws QueryException {
		String description = "Cone search service provided by the SaadaDB " + Database.getName() + "\n" ;
		description       += "        Data collection(S) covered by the service: " + (new PseudoTableParser(pseudo_table)) + "\n";
		if( datamodel != null && datamodel.length() > 0 ) {
			description += "        Service compliantwith the data model " + datamodel+ "\n";
		}
		if( withrel ) {
			description += "        Data linked with selected sources are included in query results";
		}
		return description;
	}

	/**
	 * @param pseudo_table
	 * @param withrel
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public static String buildConeSearchGlumark(String pseudo_table, String datamodel, boolean withrel) throws UnsupportedEncodingException {
		String retour;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());

		String dbname = Database.getName();
		String url_root = Database.getUrl_root();
		String s_withrel = "";
		if (withrel == true) {
			s_withrel = "&withrel=true";
		}
		retour = getAladinHelp(dbname, "Cone Search");
		retour += "%ActionName        " + dbname + "-CONE\n";
		retour += "%Description       Cone search access to the SaadaDB "
			+ dbname + "\n";
		retour += "%Aladin.Label      Saada CS: " + dbname + s_withrel 
		+ "\n";
		retour += "%Aladin.Menu       Saada CS\n";
		retour += "%Aladin.LabelPlane " + dbname + " CS\n";
		retour += "%DistribDomain     ALADIN\n";
		retour += "%Owner             CDS'aladin\n";
		retour += "%Url               " +  getConeSearchURL(pseudo_table, datamodel, withrel) + "RA=$1&DEC=$2&SR=$3\n";
		retour += "%Param.Description $1=Right Ascension\n";
		retour += "%Param.Description $2=Declination\n";
		retour += "%Param.DataType    $1=Target(RAd)\n";
		retour += "%Param.DataType    $2=Target(DEd)\n";
		retour += "%Param.Description $3=Size\n";
		retour += "%Param.DataType    $3=Field(RADIUSd)\n";
		retour += "%Param.Value       $3=0.7\n";
		retour += "%ResultDataType    Mime(text/xml)\n";

		return retour;
	}

	/**
	 * @param pseudo_table
	 * @param withrel
	 * @return
	 * @throws UnsupportedEncodingException 
	 * @throws QueryException 
	 * @throws SaadaException
	 */
	public static String buildConeSearchRegistry(String pseudo_table, String datamodel, boolean withrel ) throws QueryException, UnsupportedEncodingException {
		return BuildRegistry.template.replace("_TITLE_", "GIVE A TITLE FOR THIS CONE SEARCH SERVICE")
		.replace("_DESCRIPTION_", getConeSearchDescription(pseudo_table, datamodel, withrel))
		.replace("_STANDARD_", "ivo://ivoa.net/std/ConeSearch")
		.replace("_TYPE_", "cs:ConeSearch")
		.replace("_ROLE_", "role='std'")
		.replace("_URL_", getConeSearchURL(pseudo_table, datamodel, withrel));
	}

	/**
	 * @param pseudo_table
	 * @return
	 */
	public static String buildSkynodeRegistry(String pseudo_table) {
		return "<Not implemted>";
	}

	/**
	 * @param pseudo_table
	 * @param datamodel
	 * @param withrel
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public static String getSIAURL(String pseudo_table, String datamodel, String intersect, String mode, boolean withrel) throws UnsupportedEncodingException {
		String url = Database.getUrl_root() + "/siaservice?collection="  + pseudo_table + "&";
		if( datamodel != null && datamodel.length() > 0 ) {
			url += "dm=" + URLEncoder.encode(datamodel, "iso-8859-1")+ "&";
		}
		if( intersect != null && intersect.length() > 0 ) {
			url += "intersect=" + intersect+ "&";
		}
		if( mode != null && mode.length() > 0 ) {
			url += "mode=" + mode+ "&";
		}
		if( withrel ) {
			url += "withrel=true&";
		}
		return url;
	}
	/**
	 * @param pseudo_table
	 * @param datamodel
	 * @param withrel
	 * @return
	 * @throws QueryException 
	 */
	public static String getSIADescription(String pseudo_table, String datamodel, String intersect, String mode, boolean withrel) throws QueryException {
		String description = "SIA service provided by the SaadaDB " + Database.getName() + "\n" ;
		description       += "        Data collection(S) covered by the service: " + (new PseudoTableParser(pseudo_table)) + "\n";
		if( datamodel != null && datamodel.length() > 0 ) {
			description += "        Service compliantwith the data model " + datamodel+ "\n";
		}
		if( withrel ) {
			description += "        Data linked with selected images are included in query results\n";
		}
		if( intersect != null && intersect.length() > 0 ) {
			description += "        Intersect mode: " + intersect;
		}
		else {
			description += "        Intersect mode: COVERS";			
		}
		if( mode != null && mode.length() > 0 ) {
			description += "        Mode CUTOUT";			
		}
		return description;
	}
	/**
	 * @param pseudo_table
	 * @param withrel
	 * @return
	 * @throws SaadaException
	 * @throws UnsupportedEncodingException 
	 */
	public static String buildSIARegistry(String pseudo_table, String datamodel, String intersect, String mode, boolean withrel) throws SaadaException, UnsupportedEncodingException {
		return BuildRegistry.template.replace("_TITLE_", "GIVE A TITLE FOR THIS SIA SERVICE")
		.replace("_DESCRIPTION_", getSIADescription(pseudo_table, datamodel, intersect, mode, withrel))
		.replace("_STANDARD_", "ivo://ivoa.net/std/SIA")
		.replace("_TYPE_", "ssa:SimpleImageAccess")
		.replace("_ROLE_", "role='std'")
		.replace("_URL_", getSIAURL(pseudo_table, datamodel, intersect, mode, withrel));
	}


	/**
	 * @param pseudo_table
	 * @param withrel
	 * @return
	 * @throws SaadaException
	 * @throws UnsupportedEncodingException 
	 */
	public static String buildSIAGlumark(String pseudo_table, String datamodel, String intersect, String mode, boolean withrel) throws SaadaException, UnsupportedEncodingException {
		String retour;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());

		String dbname = Database.getName();
		String l_withrel = "";
		if (withrel == true) {
			l_withrel = " (with rel)";
		}
		retour = getAladinHelp(dbname, "SIA");
		retour += "%ActionName        " + dbname + "-SIA\n";
		retour += "%Description       SIA access to the SaadaDB " + dbname
		+ "\n";
		retour += "%Aladin.Label      Saada SIA: " + dbname + l_withrel
		+ "\n";
		retour += "%Aladin.Menu       Saada SIA\n";
		retour += "%Aladin.LabelPlane " + dbname + " SIA\n";
		retour += "%DistribDomain     ALADIN\n";
		retour += "%Owner             CDS'aladin\n";
		retour += "%Url               " + getSIAURL(pseudo_table, datamodel, intersect, mode, withrel) + "pos=$1,$2&size=$3\n";
		retour += "%Param.Description $1=Right Ascension\n";
		retour += "%Param.Description $2=Declination\n";
		retour += "%Param.DataType    $1=Target(RAd)\n";
		retour += "%Param.DataType    $2=Target(DEd)\n";
		retour += "%Param.Description $3=Size\n";
		retour += "%Param.DataType    $3=Field(RADIUSd)\n";
		retour += "%Param.Value       $3=0.7\n";
		retour += "%ResultDataType    Mime(sia/xml)\n";
		return retour;
	}


	/**
	 * @param pseudo_table
	 * @param datamodel
	 * @param withrel
	 * @return
	 * @throws QueryException 
	 */
	public static String buildSSARegistry(String pseudo_table, String datamodel, boolean withrel) throws Exception {
		String url = Database.getUrl_root() + "/ssaservice?collection="  + pseudo_table + "&";
		String description = "SSA service provided by the SaadaDB " + Database.getName() + "\n" ;
		description       += "        Data collection(S) covered by the service: " + (new PseudoTableParser(pseudo_table)) + "\n";
		if( datamodel != null && datamodel.length() > 0 ) {
			description += "        Service compliantwith the data model " + datamodel+ "\n";
			url += "dm=" + URLEncoder.encode(datamodel, "iso-8859-1")+ "&";
		}
		if( withrel ) {
			description += "        Data linked with selected spectra are included in query results";
			url += "withrel=true&";
		}

		return BuildRegistry.template.replace("_TITLE_", "GIVE A TITLE FOR THIS SSA SERVICE")
		.replace("_DESCRIPTION_", description)
		.replace("_STANDARD_", "ivo://ivoa.net/std/SSA")
		.replace("_TYPE_", "ssa:SimpleSpectralAccess")
		.replace("_ROLE_", "")
		.replace("_URL_", url);

	}



	/**
	 * @param dbname
	 * @param category
	 * @return
	 */
	private static String getAladinHelp(String dbname, String category) {
		String retour;
		retour = "------------------------------------------------------------------------------\n";
		retour += " Glu mark for Aladin\n";
		retour += "\n";
		retour += " The following Glu mark enables Aladin to see your "
			+ dbname + " SaadaDB \n";
		retour += " as a VO " + category + " provider\n";
		retour += "   1- If the file AlaGlu.dic doesn't exist in the Aladin.jar current directory,\n";
		retour += "      extract it from Aladin.jar: jar xvf Aladin.jar AlaGlu.dic \n";
		retour += "   2- Append the following text to AlaGlu.dic\n";
		retour += "   3- Restarts Aladin: java -jar Aladin.jar\n";
		retour += "------------------------------------------------------------------------------\n";
		return retour;
	}

}
