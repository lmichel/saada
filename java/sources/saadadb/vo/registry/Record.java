package saadadb.vo.registry;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import saadadb.database.Database;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.util.Messenger;
import saadadb.vo.VOLimits;

/**
 * @author michel
 * @version $Id$
 *
 */
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
		authority = Authority.getInstance();
		this.authority.load();
	}

	/**
	 * @param capability Capability to be set in the registry
	 * @return
	 * @throws QueryException
	 */
	public String getRecord(Capability capability) throws QueryException {
		String protocol = capability.getProtocol();
		try {
			if( Capability.TAP.equals(protocol)) {
				return getTAPRecord().toString();
			} else if( Capability.SIA.equals(protocol)) {
				return getSIARecord(capability).toString();

			} else if( Capability.SSA.equals(protocol)) {
				return getSSARecord(capability).toString();

			} else if( Capability.ConeSearch.equals(protocol)) {
				return getCSRecord(capability).toString();
			}
		} catch (Exception e) {
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER, e);
		}
		QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "Not registry record available for protocol " + protocol);
		return null;
	}

	/**
	 * @return
	 */
	public StringBuffer getTAPRecord() {
		StringBuffer retour = new StringBuffer();
		retour.append(header);
		retour.append(this.authority.getXML());
		
		retour.append("<capability standardID=\"ivo://ivoa.net/std/TAP\" xsi:type=\"tr:TableAccess\">\n");
		retour.append("<interface role=\"std\" xsi:type=\"vs:ParamHTTP\">\n");
		retour.append("<accessURL use=\"base\">\n");
		retour.append(Database.getUrl_root() + "/tap\n");
		retour.append("</accessURL>\n");
		retour.append("</interface>\n");
		retour.append("<dataModel ivo-id=\"ivo://ivoa.net/std/ObsCore-1.0\">Obscore 1.0</dataModel>\n");
		retour.append("<dataModel ivo-id=\"ivo://ivoa.net/std/RegTAP/vor\">Registry 1.0</dataModel>\n");
		retour.append("<language>\n");
		retour.append("<name>ADQL</name>\n");
		retour.append("<version ivo-id=\"ivo://ivoa.net/std/ADQL#v2.0\">2.0</version>\n");
		retour.append("<description>ADQL 2.0</description>\n");
		retour.append("<languageFeatures type=\"ivo://ivoa.net/std/TAPRegExt#features-adqlgeo\">\n");
		retour.append("<feature>\n");
		retour.append("<form>BOX</form>\n");
		retour.append("</feature>\n");
		retour.append("<feature>\n");
		retour.append("<form>POINT</form>\n");
		retour.append("</feature>\n");
		retour.append("<feature>\n");
		retour.append("<form>CIRCLE</form>\n");
		retour.append("</feature>\n");
		retour.append("<feature>\n");
		retour.append("<form>CONTAINS</form>\n");
		retour.append("</feature>\n");
		retour.append("</languageFeatures>\n");
		retour.append("</language>\n");
		
		retour.append("<outputFormat ivo-id=\"ivo://ivoa.net/std/TAPRegExt#output-votable-binary\">\n");
		retour.append("<mime>text/xml</mime>\n");
		retour.append("</outputFormat>\n");
		
		retour.append("<retentionPeriod>\n");
		retour.append("<default>3600</default>\n");
		retour.append("</retentionPeriod>\n");
		retour.append("<executionDuration>\n");
		retour.append("<default>3600</default>\n");
		retour.append("</executionDuration>\n");
		retour.append("<outputLimit>\n");
		retour.append("<default unit=\"row\">10000</default>\n");
		retour.append("<hard unit=\"row\">10000</hard>\n");
		retour.append("</outputLimit>\n");
		retour.append("</capability>\n");
		
		retour.append("<capability standardID=\"ivo://ivoa.net/std/VOSI#availability\">\n");
		retour.append("<interface xsi:type=\"vs:ParamHTTP\">\n");
		retour.append("<accessURL use=\"full\">" + Database.getUrl_root() + "/tap/availability</accessURL>\n");
		retour.append("</interface>\n");
		retour.append("</capability>\n");
		retour.append("<capability standardID=\"ivo://ivoa.net/std/VOSI#capabilities\">\n");
		retour.append("<interface xsi:type=\"vs:ParamHTTP\">\n");
		retour.append("<accessURL use=\"full\">" + Database.getUrl_root() + "/tap/capabilities</accessURL>\n");
		retour.append("</interface>\n");
		retour.append("</capability>\n");
		retour.append("<capability standardID=\"ivo://ivoa.net/std/VOSI#tables\">\n");
		retour.append("<interface xsi:type=\"vs:ParamHTTP\">\n");
		retour.append("<accessURL use=\"full\">" + Database.getUrl_root() + "/tap/tables</accessURL>\n");
		retour.append("</interface>\n");
		retour.append("</capability>\n");
		retour.append("</ri:Resource>\n"); 

		return retour;
	}

	public StringBuffer getSIARecord(Capability capability) throws Exception {
		String url = Database.getUrl_root() + "/siaservice?collection=["  + capability.getDataTreePath().collection + "]&withrel=true&";
		StringBuffer retour = new StringBuffer();
		retour.append(header);
		retour.append(this.authority.getXML());
		retour.append(this.filterTemplate("reg.template.sia.xml", url, capability));
		retour.append("</ri:Resource>\n"); 
		return retour;
	}

	/**
	 * returns the content of the registry record template with strings between @@ are replaced with
	 * real values
	 * @param filename registry template filename
	 * @param url      base url f the service
	 * @param capability service capability
	 * @return
	 * @throws Exception
	 */
	public CharSequence filterTemplate(String filename, String url, Capability capability) throws Exception {
		StringBuffer retour = new StringBuffer();
		BufferedReader br = new BufferedReader(new FileReader(Database.getRoot_dir() 
				+ File.separator + "config" + File.separator + filename));
		Pattern p = Pattern.compile(".*(@@[A-Z_]+@@).*", Pattern.DOTALL);
		String boeuf;
		while( (boeuf = br.readLine()) != null ) {
			Matcher m = p.matcher(boeuf);
			if( m.find() ) {
				for( int i=1 ; i<=m.groupCount() ; i++ ) {
					String grp = m.group(i);
					if( grp.startsWith("@@VOLIMIT_")) {
						String fgrp = grp.substring(10, grp.length()-2);
						String rpl = VOLimits.class.getField(fgrp).get(null).toString();
						boeuf = boeuf.replace(grp, rpl);
					} else if( "@@SERVICE_URL@@".equals(grp)) {
						boeuf = boeuf.replace(grp, url);
					} else if( "@@COOSYS@@".equals(grp)) {
						boeuf = boeuf.replace(grp, Database.getCooSys());
					} else if( "@@ROOT_URL@@".equals(grp)) {
						boeuf = boeuf.replace(grp, Database.getUrl_root());
					} else {
						Messenger.printMsg(Messenger.WARNING, "Unkonw Tag " + grp + " replaced with ???");
						boeuf = boeuf.replace(grp, "???");
					}
				}
			}
			retour.append(boeuf.trim() + "\n");
		}
		return retour;
	}

	public StringBuffer getSSARecord(Capability capability){
		StringBuffer retour = new StringBuffer();
		retour.append(header);
		retour.append(this.authority.getXML());
		retour.append("</ri:Resource>\n"); 
		return retour;

	}
	public StringBuffer getCSRecord(Capability capability){
		StringBuffer retour = new StringBuffer();
		retour.append(header);
		retour.append(this.authority.getXML());
		retour.append("</ri:Resource>\n"); 
		return retour;
	}
	

}
