package saadadb.command;

import saadadb.collection.MocManager;


/**
 * Command line interface with MocManager
 * @author michel
 *
 */
public class ManageMoc extends ManageEntity{

	/**
	 * 
	 */
	public ManageMoc() {
		manager = new MocManager(); 
	}
	/**
	 * @param args
	 */
	public static void main(String[] args)  {
		processCommand(new ManageMoc(), args);
	}


	/**
	 * 
	 */
	@Override
	public  void usage() {
		System.out.println("USAGE: java ManageMoc -[remove|create]=coll_nameSaadaDB_Name");
		System.exit(1);						
	}
	
}
