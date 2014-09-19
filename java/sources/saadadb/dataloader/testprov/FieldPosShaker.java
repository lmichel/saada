package saadadb.dataloader.testprov;

import java.util.TreeSet;

import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;

/**
 * Checks field formats for position
 * @author laurentmichel
 *
 */
public class FieldPosShaker extends FieldShaker {


	FieldPosShaker(String jsonFilename) throws Exception{
		super(jsonFilename);
		this.paramsOfInterest = new TreeSet<String>();
		this.paramsOfInterest.add("s_ra");
		this.paramsOfInterest.add("s_dec");
	}

		/**
		 * @param args
		 * @throws FatalException 
		 */
		public static void main(String[] args) throws Exception {
			ArgsParser ap = new ArgsParser(args);
			//Database.init(ap.getDBName());
			(new FieldPosShaker(ap.getFilename())).processAll();
		}

	}
