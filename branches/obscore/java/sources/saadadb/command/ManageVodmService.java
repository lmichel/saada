/**
 * 
 */
package saadadb.command;

import saadadb.vo.tap.DmServiceManager;

/**
 * @author laurent
 *
 */
public class ManageVodmService extends ManageEntity {

	/**
	 * 
	 */
	public ManageVodmService() {
		manager = new DmServiceManager(); 
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
		System.out.println("USAGE: java ManageVodmService -[empty|remove|create]=dm_name  SaadaDB_Name");
		System.exit(1);						
	}

}
