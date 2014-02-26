/**
 * 
 */
package saadadb.resourcetest;

import java.util.LinkedHashMap;

import saadadb.database.Database;
import saadadb.exceptions.QueryException;
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
	public static void main(String[] args)  {
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
		 jsonRequest = 
		 "{\"CATALOGUE_ENTRY\": {" +
		 " \"queries\":[" +
		 "             {\"name\":\"query_0\",\"uri\":\"Select ENTRY From CatalogueEntry In CATALOGUE\nWhereRelation {\n    matchPattern{ CatSrcToSrcSpe}\n}\n\nLimit 1000\n\",\"relations\":[]}" +
		 "             ]" +
		 " ,\"files\":["+
		 "             {\"name\":\"EPIC EPN Spectra: OBS 0305751001 SRC# 6\",\"uri\":\"1153484501804908549\",\"relations\":[]}," +
		 "             {\"name\":\"SDSS0004+00\"                            ,\"uri\":\"1441714865071718411\",\"relations\":[]}"
		 +"           ]}" +
		 ",\"MERGEDCATALOGUE_ENTRY\":{"+
		 " \"queries\":[" +
		 "             {\"name\":\"query_1\",\"uri\":\"Select ENTRY From MergedEntry In MERGEDCATALOGUE Limit 10\",\"relations\":[]}" +
		 "             ]" +
		 " ,\"files\":[" +
		 "             {\"name\":\"NULL\"   ,\"uri\":\"580120304957784065\",\"relations\":[]}" +
		 "            ]}" +
		 "}";
		pmap.put("cart", jsonRequest);
		
		CartRequest request = new CartRequest("NoSession", "/home/michel/Desktop/CART");
		try {
			request.addFormator("zip");
		request.setResponseFilePath("ZippedSaadaql");
		request.processRequest(pmap);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Database.close();
	
	}

}
