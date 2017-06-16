package saadadb.command;

import saadadb.database.Database;


public class ManageProjectionUpdate extends ManageEntity{
	
	/**
	 * 
	 */
	public ManageProjectionUpdate() {
		manager = new ProjectionUpdater(); 
	}
	/**
	 * @param args
	 */
	public static void main(String[] args)  {
		processCommand(new ManageProjectionUpdate(), args);
		Database.close();
	}

	@Override
	public void usage() {
		System.out.println("USAGE: java ManageProduct -remove=oid1,...oidn  SaadaDB_Name");
		System.exit(1);						
	}
	
	
}
