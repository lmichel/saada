/**
 * 
 */
package saadadb.command;

import saadadb.vo.tap.TapServiceManager;

/**
 * @author laurent
 *
 */
public class ManageVodmService extends ManageEntity {

	/**
	 * 
	 */
	public ManageVodmService() {
		manager = new TapServiceManager(); 
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args)  {
		processCommand(new ManageVodmService(), args);
	}

	/* (non-Javadoc)
	 * @see saadadb.command.ManageEntity#usage()
	 */
	@Override
	protected void usage() {
		System.out.println("USAGE: java ManageVodmService -[empty|remove|create]=coll_name [-category=category] SaadaDB_Name");
		System.exit(1);						
	}

}
