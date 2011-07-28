package saadadb.command;

import saadadb.collection.ProductManager;


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
	}



	@Override
	public void usage() {
		System.out.println("USAGE: java ManageCclass -[empty|remove]=class_name [-force] SaadaDB_Name");
		System.exit(1);						
	}
	
	
}
