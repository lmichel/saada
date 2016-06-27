package saadadb.command;

import saadadb.collection.ClassManager;


public class ManageClass extends ManageEntity {

	/**
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
		System.out.println("USAGE: java ManageClass -[empty|remove|rename]=class_name [-name=newname] [-force] SaadaDB_Name");
		System.exit(1);						
	}

}
