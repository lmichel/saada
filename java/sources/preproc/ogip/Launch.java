package preproc.ogip;

import java.io.File;
import java.util.ArrayList;

import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.SaadaException;
import saadadb.util.Messenger;
import saadadb.vocabulary.RegExp;

public class Launch {

	/**
	 * @param args
	 * @throws LoadDataException 
	 */
	public static void main(String[] args) throws Exception {
		ArgsParser ap = new ArgsParser(args);
		Database.init(ap.getDBName());
		
		String fileName = ap.getFilename();
		File file = new File(fileName);
		ArrayList<File> retour = new ArrayList<File>();

		if( !file.exists() ) {
			AbortException.throwNewException(SaadaException.FILE_ACCESS,"file or directory <" + file.getAbsolutePath() + "> doesn't exist");							
		}
		/*
		 * "filename" is a directory 
		 */
		else if( file.isDirectory() ) {
			String[] dir_content = file.list();
			if( dir_content.length == 0 ) {
				AbortException.throwNewException(SaadaException.FILE_ACCESS,"Directory <" + file.getAbsolutePath() + "> is empty");							
			}
			Messenger.printMsg(Messenger.TRACE, "Reading directory <" + file.getAbsolutePath() + ">");							
			for( int i=0 ; i<dir_content.length ; i++ ) {	
				if( dir_content[i].matches(RegExp.FITS_FILE)  ) {
					retour.add(new File(file.getAbsolutePath() + System.getProperty("file.separator") + dir_content[i]));
				}
			}
			Messenger.printMsg(Messenger.TRACE, dir_content.length + " files found in directory <" + file.getAbsolutePath() + ">");
		}
		/*
		 *  "filename" is a single file to load
		 */
		else {
			Messenger.printMsg(Messenger.TRACE, "One unique file to process <" + file.getAbsolutePath() + ">");							
			retour.add(file);
		}

		
		for( File f: retour){
//			Product product =  new Product(f.getAbsolutePath());
//			VOTableGenerator voGen = new VOTableGenerator(product);
//			Loader loader = new Loader(args);
//			for(int i=0; i<voGen.getVFileName().size(); i++){
//				args[1] = "-filename="+voGen.getVFileName().get(i);
//				try {
//					loader.load();
//				} catch (Exception e) {
//					Messenger.printStackTrace(e);
//					System.exit(1);
//				}
//			}
		}
		Database.close();

	}
}
