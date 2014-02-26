package saadadb.products;

import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;

public class MappingTester {

	public static void test1() throws FatalException, Exception {
		String [][] atts = {{"RA", "", ""},
	            {"DEC", "", ""} ,
	            {"alpha", "", ""},
	            {"delta", "", ""},
	            {"poserror", "pos.eq;stat.error", "arcsec"}
	            };
		Product p = new Product(null, null);
		p.testMapping(new ArgsParser(new String[]{"-category=misc", "-collection=ABSC"}), atts);
		p = new Product(null, null);
		p.testMapping(new ArgsParser(new String[]{"-category=misc", "-collection=ABSC", "-posmapping=first"}), atts);
		p = new Product(null, null);
		p.testMapping(new ArgsParser(new String[]{"-name=alpha,'+',delta", "-category=misc", "-collection=ABSC", "-posmapping=first", "-position=alpha,delta", "-poserror=10,5,43", "-poserrorunit=mas"}), atts);	
	}
	/**
	 * @param args
	 * @throws Exception 
	 * @throws FatalException 
	 */
	public static void main(String[] args) throws FatalException, Exception {
		Database.init((new ArgsParser(args)).getDBName());
		test1();
		Database.close();
	}

}
