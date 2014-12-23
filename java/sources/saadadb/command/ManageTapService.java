/**
 * 
 */
package saadadb.command;

import saadadb.vo.tap_old.TapServiceManager;

/**
 * @author laurent
 *
 */
public class ManageTapService extends ManageEntity {

	/**
	 * 
	 */
	public ManageTapService() {
		manager = new TapServiceManager(); 
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args)  {
		processCommand(new ManageTapService(), args);
	}

	/* (non-Javadoc)
	 * @see saadadb.command.ManageEntity#usage()
	 */
	@Override
	protected void usage() {
		System.out.println("USAGE: java ManageTapService -[empty|remove|create]=coll_name [-category=category] SaadaDB_Name");
		System.exit(1);						
	}

}
