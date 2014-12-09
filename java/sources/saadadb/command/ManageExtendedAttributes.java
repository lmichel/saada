package saadadb.command;

import saadadb.configuration.ExtendAttributeManager;

/**
 * @author michel
 * @version $Id$
 *
 */
public class ManageExtendedAttributes extends ManageEntity {

	/**
	 * 
	 */
	public ManageExtendedAttributes() {
		manager = new ExtendAttributeManager(); 
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args)  {
		processCommand(new ManageExtendedAttributes(), args);
	}

	/* (non-Javadoc)
	 * @see saadadb.command.ManageEntity#usage()
	 */
	@Override
	protected void usage() {
		System.out.println("USAGE: java ManageCollAttributeExtend -create=attname  -category=category -type=type SaadaDB_Name");
		System.exit(1);						
	}

}
