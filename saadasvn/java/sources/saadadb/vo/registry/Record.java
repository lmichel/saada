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
}
