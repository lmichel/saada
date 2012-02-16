package saadadb.vo.registry;

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
			+ "xsi:type=\"vg:Authority\">";

	}
	
}
