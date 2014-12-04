/**
 * 
 */
package saadadb.dataloader.testprov;

import java.io.File;

import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.dataloader.Loader;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.util.Messenger;

/**
 * @author michel
 * @version $Id$
 */
public class JsonLoader {

	/**
	 * @param args
	 * @throws FatalException 
	 */
	public static void main(String[] args) throws Exception {
		try {
		ArgsParser ap = new ArgsParser(args);			
		Database.init(ap.getDBName());
		String filename = ap.getFilename().replace("json:", "");
		String fn=null;
		if( filename.charAt(0) != File.separatorChar ){
			fn = Database.getRoot_dir() + File.separator + "datatest" + File.separator + filename;
			if( !(new File(fn)).exists() ) {
				fn = filename.replace(Database.getRoot_dir() + File.separator + "datatest" + File.separator, "/home/michel/workspace/SaadaObscore/datatest/");
			}
			if( !(new File(fn)).exists() ) {
				FatalException.throwNewException(SaadaException.WRONG_PARAMETER, "No file " + filename + " found");			
			}
				
		}
		Messenger.printMsg(Messenger.TRACE, "Load " + fn);
		
		ap = ArgsParser.getArgsParserFromJson(fn, args[args.length - 1]);
		System.out.println(ap);
		Loader loader  = new Loader(ap);
		loader.load();
		} catch (Exception e){
			e.printStackTrace();
		
		}
		Database.exit();
	}

}
