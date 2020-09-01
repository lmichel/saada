package saadadb.command;

import saadadb.database.Database;
import saadadb.products.updaters.FoVUpdater;


public class ManageFoVUpdate extends ManageEntity{
	
	/**
	 * 
	 */
	public ManageFoVUpdate() {
		manager = new FoVUpdater(); 
	}
	/**
	 * @param args
	 */
	public static void main(String[] args)  {
		processCommand(new ManageFoVUpdate(), args);
		Database.close();
	}

	@Override
	public void usage() {
		System.out.println("USAGE: java ManageFoVUpdate -populate=[oid1,...oidn or query]  SaadaDB_Name");
		System.exit(1);						
	}
	
	
}
