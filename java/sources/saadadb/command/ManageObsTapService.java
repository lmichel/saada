/**
 * 
 */
package saadadb.command;

import saadadb.database.Database;
import saadadb.vo.tap.ObstapServiceManager;

/**
 * @author laurent
 *
 */
public class ManageObsTapService extends ManageEntity {

	/**
	 * 
	 */
	public ManageObsTapService() {
		manager = new ObstapServiceManager(); 
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args)  {
		processCommand(new ManageObsTapService(), args);
		Database.close();
	}

	/* (non-Javadoc)
	 * @see saadadb.command.ManageEntity#usage()
	 */
	@Override
	protected void usage() {
		System.out.println("USAGE: java ManageObsTapService -[empty|remove|create]=coll_name [-category=category] [-ukw kw=value] SaadaDB_Name");
		Database.close();
		System.exit(1);						
	}

}
