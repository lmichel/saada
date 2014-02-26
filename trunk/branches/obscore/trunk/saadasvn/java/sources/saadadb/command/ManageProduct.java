package saadadb.command;

import saadadb.collection.ProductManager;
import saadadb.database.Database;


public class ManageProduct extends ManageEntity{
	
	/**
	 * 
	 */
	public ManageProduct() {
		manager = new ProductManager(); 
	}
	/**
	 * @param args
	 */
	public static void main(String[] args)  {
		processCommand(new ManageProduct(), args);
		Database.close();
	}

	@Override
	public void usage() {
		System.out.println("USAGE: java ManageProduct -remove=oid1,...oidn  SaadaDB_Name");
		System.exit(1);						
	}
	
	
}
