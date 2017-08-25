package saadadb.command;

import saadadb.database.Database;


public class RecomputeProjections extends ManageEntity{
	
	/**
	 * 
	 */
	public RecomputeProjections() {
		manager = new ProjectComputer(); 
	}
	/**
	 * @param args
	 */
	public static void main(String[] args)  {
		processCommand(new RecomputeProjections(), args);
		Database.close();
	}

	@Override
	public void usage() {
		System.out.println("USAGE: java ManageProduct -remove=oid1,...oidn  SaadaDB_Name");
		System.exit(1);						
	}
	
	
}
