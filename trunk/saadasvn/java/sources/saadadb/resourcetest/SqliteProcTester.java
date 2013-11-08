package saadadb.resourcetest;

import java.util.LinkedHashMap;

import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.util.Messenger;
import saadadb.vo.request.SaadaqlRequest;

public class SqliteProcTester {


	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		Messenger.debug_mode = false;
		Database.init("SQLITE");
		LinkedHashMap<String, String> pmap = new LinkedHashMap<String, String>();
		pmap.put("query", "Select ENTRY From * In Collection0 WherePosition {  isInCircle(\"02:45:16.35+42:57:29.1\", 60, J2000,ICRS) }");
		pmap.put("model", "samp");
		Messenger.debug_mode = true;
		SaadaqlRequest request = new SaadaqlRequest("NoSession", "/home/michel/Desktop");
		request.addFormator("votable");
		request.setResponseFilePath("Saadaql");
		request.processRequest(pmap);
	}
}
