package saadadb.command;

import saadadb.collection.CollectionManager;


/**
 * Command line interface with CollectionManager
 * @author michel
 *
 */
public class ManageCollection extends ManageEntity{

	/**
	 * 
	 */
	public ManageCollection() {
		manager = new CollectionManager(); 
	}
	/**
	 * @param args
	 */
	public static void main(String[] args)  {
		processCommand(new ManageCollection(), args);
	}


	/**
	 * 
	 */
	@Override
	public  void usage() {
		System.out.println("USAGE: java ManageCollection -[empty|remove|create]=coll_name [-comment=description] SaadaDB_Name");
		System.exit(1);						
	}
	
}
