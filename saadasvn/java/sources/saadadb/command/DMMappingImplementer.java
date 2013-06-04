package saadadb.command;

import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.generationclass.DMImplementer;
import saadadb.util.Messenger;

/**
 * Apply the DM mapping file given as parameter
 * @author michel
 *
 */
public class DMMappingImplementer {
	

	public static void main(String[] args) throws FatalException {
		try {
			ArgsParser ap = new ArgsParser(args);
			String fn = ap.getFilename();
			if( fn == null || fn.length() == 0 ) {
				FatalException.throwNewException(SaadaException.WRONG_PARAMETER, "USAGE: java .... DMMappingImplementer [mappingfile]");
			}
			Database.init(ap.getDBName());
			Database.setAdminMode(ap.getPassword());
			
			DMImplementer dmi= new DMImplementer(fn);
			dmi.putDMInJavaClass(Database.getClassLocation());
		} catch(Exception e) {
			Messenger.printStackTrace(e);
			FatalException.throwNewException(SaadaException.INTERNAL_ERROR, e);			
		}
	}
}
