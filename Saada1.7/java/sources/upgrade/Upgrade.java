package upgrade;

import saadadb.command.ArgsParser;
import saadadb.database.Database;

public class Upgrade {


	public static void upgrade(ArgsParser ap) throws Exception {
		upgrade.schema.Upgrade.upgrade();
		Database.init(ap.getDBName());
		Database.setAdminMode(ap.getPassword());
		upgrade.collection.Upgrade.upgrade();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ArgsParser ap;
		try {
			ap = new ArgsParser(args);
			Database.init(ap.getDBName());
			upgrade(ap);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			Database.close();
		}


	}

}
