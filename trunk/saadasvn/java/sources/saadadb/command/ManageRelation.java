package saadadb.command;

import saadadb.relationship.RelationManager;




/**
 * Command line interface with RelationManager
 * @author michel
 *
 */
public class ManageRelation extends ManageEntity {

	/**
	 * 
	 */
	public ManageRelation() {
		manager = new RelationManager(); 
	}

	/**
	 * 
	 */
	@Override
	public void usage() {
		System.out.println("USAGE: java ManageRelation -[empty|remove|create]=relation_name [-comment=description] SaadaDB_Name");
		System.exit(1);						
	}


	/**
	 * @param args
	 */
	public static void main(String[] args)  {
		processCommand(new ManageRelation(), args);
	}



}
