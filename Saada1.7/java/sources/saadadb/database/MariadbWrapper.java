package saadadb.database;


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.TreeSet;

import saadadb.command.ArgsParser;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.util.Messenger;

/**
 * @author michel
 * @version $Id$
 *
 * 11/2013 : creatDB: grant access to reader@localhost no to reader
 */
public class MariadbWrapper extends MysqlWrapper {
	private final TreeSet<String> recorded_tmptbl = new TreeSet<String>();

	/** * @version $Id$

	 * @param server_or_driver
	 * @param port_or_url
	 * @throws ClassNotFoundException
	 */
	private MariadbWrapper(String server_or_driver, String port_or_url) throws ClassNotFoundException {
		super(server_or_driver, port_or_url);
		test_base = "testbasededonnees";
		test_table = "TableTest";
		if( server_or_driver.startsWith("org.mariadb.jdbc.Driver") ) {
			this.driver = server_or_driver;
			this.url = port_or_url;

		}
		else {
			driver = "org.mariadb.jdbc.Driver";
			url = "jdbc:mariadb:";
			Class.forName(driver);
			if( server_or_driver != null && server_or_driver.length() > 0 ) {
				url += "//" + server_or_driver ;
				if( port_or_url != null && port_or_url.length() > 0 ) {
					url += ":" + port_or_url;
				}
				url += "/";
			}
		}
	}

	@Override
	public String[] getStoreTable(String table_name, int ncols, String table_file) throws Exception {
		return  new String[] {
				//"LOCK TABLE " + table_name + " WRITE",
				"ALTER TABLE " + table_name + " DISABLE KEYS",
				"LOAD DATA LOCAL INFILE '" + table_file.replaceAll("\\\\", "\\\\\\\\") + "' INTO TABLE "  +  table_name,
				"ALTER TABLE " + table_name + " ENABLE KEYS"};

	}

	/* (non-Javadoc)
	 * @see saadadb.database.DbmsWrapper#getDBMS()
	 */
	@Override
	public String getDBMS(){
		return "MariaDB";
	}

	/**
	 * @param server_or_url
	 * @param port_or_url
	 * @return
	 * @throws ClassNotFoundException
	 */
	public static DbmsWrapper getWrapper(String server_or_url, String port_or_url) throws ClassNotFoundException {
		if( wrapper != null ) {
			return wrapper; 
		}
		return new MariadbWrapper(server_or_url, port_or_url);
	}



	public static void main(String[] args) {
		try {
			ArgsParser ap = new ArgsParser(args);
			Messenger.debug_mode = true;
			DbmsWrapper dbmswrapper = MariadbWrapper.getWrapper("localhost", ""); 
			dbmswrapper.setAdminAuth("saadmin", ap.getPassword());
			dbmswrapper.checkAdminPrivileges("/tmp", false);
			dbmswrapper.setReaderAuth("reader", "reader");
			dbmswrapper.checkReaderPrivileges();
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			System.err.println(e.getMessage());
		}
	}


}
