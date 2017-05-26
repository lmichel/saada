package saadadb.resourcetest;

import java.util.LinkedHashMap;

import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.util.Messenger;
import saadadb.vo.request.SIAP2Request;
import saadadb.vo.request.SIAPRequest;

/**
 * Run a SIAP quey
 * Take SIAP params as args:
 * pos       : 
 * size      : ra,dec (degree)
 * format    : METADATA ALL GRAPHIC image/fits image/png image/jpeg text/html 
 * intersect : COVERS ENCLOSED CENTERS OVERLAPS
 * mode      : cutout
 * in addition with
 * collection: 
 * filename  : response path
 * @author laurent
 *
 */
public class Siap1RequestTester {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		Messenger.debug_mode = true;
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
		//TODO Change ile PATH
		//SIAPRequest request = new SIAPRequest("NoSession", "/home/michel/Desktop");
		SIAPRequest request = new SIAPRequest("NoSession", "/home/michel/Desktop");
		request.addFormator("votable");
		request.setResponseFilePath("SIAP");
		request.processRequest(pmap);
		Database.close();

	}
}
