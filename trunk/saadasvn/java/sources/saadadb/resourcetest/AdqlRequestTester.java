package saadadb.resourcetest;

import java.util.LinkedHashMap;

import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.util.Messenger;
import saadadb.vo.registry.Capabilities;
import saadadb.vo.request.TapAdqlRequest;

public class AdqlRequestTester {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		Messenger.debug_mode = true;
		LinkedHashMap<String, String> pmap = new LinkedHashMap<String, String>();
		Messenger.printMsg(Messenger.TRACE, "Parameters:");
		for( int i=0 ; i<(args.length - 1) ; i++ ) {
			int pos = args[i].indexOf("=");
			String[] ps = args[i].split("=");
			if(pos == -1) {
				FatalException.throwNewException(SaadaException.WRONG_PARAMETER, "Param " + args[i] + " badly formed");
			}
			pmap.put(args[i].substring(0, pos),args[i].substring(pos+1));
			Messenger.printMsg(Messenger.TRACE, "  " + args[i].substring(0, pos-1) + " = " +  args[i].substring(pos+1));
		}
		Database.init(args[args.length - 1]);
		TapAdqlRequest request = new TapAdqlRequest("NoSession", "/home/michel/Desktop");
		request.addFormator("votable");
		request.addFormator("json");
		request.setResponseFilePath(Capabilities.TAP);
		request.processRequest(pmap);
	}
}
