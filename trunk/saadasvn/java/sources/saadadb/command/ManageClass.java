package saadadb.command;

import saadadb.collection.ClassManager;


public class ManageClass extends ManageEntity {

	/** * @version $Id$

	 * 
	 */
	public ManageClass() {
		manager = new ClassManager(); 
	}
	/**
	 * @param args
	 */
	public static void main(String[] args)  {
		processCommand(new ManageClass(), args);
	}



	@Override
	public void usage() {
		System.out.println("USAGE: java ManageCclass -[empty|remove]=class_name [-force] SaadaDB_Name");
		System.exit(1);						
	}

}
