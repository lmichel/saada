/**
 * 
 */
package saadadb.resourcetest;

import java.util.LinkedHashMap;

import saadadb.database.Database;
import saadadb.util.Messenger;
import saadadb.vo.request.CartRequest;

/**
 * @author laurent
 * @version $Id$
 */
public class CartRequestTester {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		Messenger.debug_mode = false;
		Database.init(args[args.length - 1]);
		
		LinkedHashMap<String, String> pmap = new LinkedHashMap<String, String>();
		String jsonRequest = "{" 
		+ "\"SpectroscopicSample_SPECTRUM\":{\""
		+ "queries\":["
		+ "{\"name\":\"XBSEntry\",\"uri\":\"Select ENTRY From XBSEntry In SpectroscopicSample\", \"relations\": [\"any-relations\"]}"
		+ "]"
		+",\"files\":["
		+ "{\"name\":\"XBSJ000532.7+200716_osp.imh\",\"uri\":\"1154047670801661979\", \"relations\": [\"any-relations\"]}"
		+ "]"
		+ "}"
		+ "}";
		
		 jsonRequest = "{\"CATALOGUE_ENTRY\":{"
		 + "\"queries\":[{\"name\":\"query_0\","
		 + "\"uri\":\"Select ENTRY From CatalogueEntry In CATALOGUE\nWhereAttributeSaada {\n    "
		 +"_iauname = '3XMM J000441.2+000711'\n}\n\",\"relations\":[\"any-relations\"]}],\"files\":[]}}";

		pmap.put("cart", jsonRequest);
		
		CartRequest request = new CartRequest("NoSession", "/home/michel/Desktop/CART");
		request.addFormator("zip");
		request.setResponseFilePath("ZippedSaadaql");
		request.processRequest(pmap);
		
	}

}
