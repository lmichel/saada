package saadadb.query.region.test.gridcreator;

import java.util.LinkedHashMap;

import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.util.Messenger;
import saadadb.vo.request.SaadaqlRequest;

/**
 * Class SaadaQLRequest
 * Allow to put the SQL request in parameter into a VOTable file
 * 
 * @author jremy
 * @version $Id$
 *
 */
public class SaadaQLRequest {


	/**
	 * @param args
	 * @throws Exception
	 */

	public static void main(String[] args) throws Exception {
		Messenger.debug_mode = true;
		Database.init(args[args.length - 1]);
		String name = args[args.length - 2];
		LinkedHashMap<String, String> pmap = new LinkedHashMap<String, String>();
		Messenger.printMsg(Messenger.TRACE, "Parameters:");
		for( int i=0 ; i<(args.length - 2) ; i++ ) {
			String[] ps = args[i].split("_");
			if( ps.length != 2) {
				FatalException.throwNewException(SaadaException.WRONG_PARAMETER, "Param " + args[i] + " badly formed");
			}
			pmap.put(ps[0], ps[1]);
			Messenger.printMsg(Messenger.TRACE, "  " + ps[0] + " = " +  ps[1]);
		}
		
		SaadaqlRequest request = new SaadaqlRequest("NoSession", "/home/jremy/Bureau");
		request.addFormator("votable");
		request.setResponseFilePath(name);
		request.processRequest(pmap);
	

	}
	
	public static void execute(String[] args) throws Exception {
		Messenger.debug_mode = true;
		Database.init(args[args.length - 1]);
		String name = args[args.length - 2];
		LinkedHashMap<String, String> pmap = new LinkedHashMap<String, String>();
		Messenger.printMsg(Messenger.TRACE, "Parameters:");
		for( int i=0 ; i<(args.length - 2) ; i++ ) {
			String[] ps = args[i].split("_");
			if( ps.length != 2) {
				FatalException.throwNewException(SaadaException.WRONG_PARAMETER, "Param " + args[i] + " badly formed");
			}
			pmap.put(ps[0], ps[1]);
			Messenger.printMsg(Messenger.TRACE, "  " + ps[0] + " = " +  ps[1]);
		}
		
		SaadaqlRequest request = new SaadaqlRequest("NoSession", "/home/jremy/Bureau");
		request.addFormator("votable");
		request.setResponseFilePath(name);
		request.processRequest(pmap);
	

	}
}
