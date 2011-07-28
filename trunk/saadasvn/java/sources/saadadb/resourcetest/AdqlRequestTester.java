package saadadb.resourcetest;

import java.util.LinkedHashMap;

import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.util.Messenger;
import saadadb.vo.request.TapAdqlRequest;

public class AdqlRequestTester {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		Messenger.debug_mode = false;
		Database.init(args[args.length - 1]);
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
		TapAdqlRequest request = new TapAdqlRequest("NoSession", "/home/michel/Desktop");
		request.addFormator("votable");
		request.addFormator("json");
		request.setResponseFilePath("TAP");
		request.processRequest(pmap);
	}
}
