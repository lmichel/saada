package saadadb.resourcetest;

import java.util.LinkedHashMap;

import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.util.Messenger;
import saadadb.vo.request.SaadaqlRequest;

public class SaadaqlRequestTester {


	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		Messenger.debug_mode = false;
		ArgsParser ap = new ArgsParser(args);
		Database.init(ap.getDBName());
		LinkedHashMap<String, String> pmap = new LinkedHashMap<String, String>();
		Messenger.printMsg(Messenger.TRACE, "Parameters:");
		for( int i=0 ; i<(args.length - 1) ; i++ ) {
			String[] ps = args[i].split("=");
			if( ps.length != 2) {
				FatalException.throwNewException(SaadaException.WRONG_PARAMETER, "Param " + args[i] + " badly formed");
			}
			pmap.put(ps[0], ps[1]);
			Messenger.printMsg(Messenger.TRACE, "  " + ps[0] + " = " +  ps[1]);
		}
		pmap.put("model", "samp");
		SaadaqlRequest request = new SaadaqlRequest("NoSession", "/home/michel/Desktop");
		request.addFormator("votable");
		request.setResponseFilePath("Saadaql");
		request.processRequest(pmap);
	}
}
