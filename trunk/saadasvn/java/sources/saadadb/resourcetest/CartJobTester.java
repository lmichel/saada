/**
 * 
 */
package saadadb.resourcetest;

import java.util.LinkedHashMap;
import java.util.Map;

import saadadb.database.Database;
import saadadb.util.Messenger;
import saadadb.vo.cart.CartJob;
import uws.UWSException;

/**
 * @author michel
 * @version $Id$
 *
 */
public class CartJobTester {
	
	public static Map<String, String> lstParam;

	
	/**
	 * @param args
	 * @throws UWSException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws UWSException, InterruptedException {
		Database.init("ThreeXMM");
		Messenger.debug_mode = true;
		lstParam = new LinkedHashMap<String, String>();
		lstParam.put("executionduration", "0");
		lstParam.put("owner", "8D93775D8D134DFEF77DE25526886EA4");
		lstParam.put("cart", "{\"query_2\":{\"queries\":[{\"name\":\"query_6\",\"uri\":\"Select SPECTRUM From EpicSrcSpect In EPIC\nLimit 1\",\"relations\":[\"any-relations\"]}],\"files\":[]}}");
		//lstParam.put("cart", "{\"EPIC_IMAGE\":{\"queries\":[{\"name\":\"query_1\",\"uri\":\"Select IMAGE From * In EPIC\nLimit 10\",\"relations\":[]}],\"files\":[]}}");
		//lstParam.put("cart", "{\"EPIC_IMAGE\":{\"queries\":[{\"name\":\"query_1\",\"uri\":\"Select IMAGE From * In EPIC\nLimit 10\",\"relations\":[]}],\"files\":[]},\"CATALOGUE_ENTRY_CatalogueEntry\":{\"queries\":[{\"name\":\"query_2\",\"uri\":\"Select ENTRY From CatalogueEntry In CATALOGUE\nLimit 10\",\"relations\":[\"any-relations\"]}],\"files\":[]},\"CATALOGUE_FLATFILE\":{\"queries\":[{\"name\":\"query_3\",\"uri\":\"Select FLATFILE From * In CATALOGUE\nLimit 10\",\"relations\":[]}],\"files\":[]}}");
		lstParam.put("format", "json");
		lstParam.put("phase", "RUN");
		CartJob cartJob = new CartJob(lstParam);
		cartJob.work();
	}

}
