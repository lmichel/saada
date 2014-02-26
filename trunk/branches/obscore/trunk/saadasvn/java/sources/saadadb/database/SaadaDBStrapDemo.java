package saadadb.database;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;


/** * @version $Id$

 * This class is never used. It is a template for the code generator GenerationClassSaada
 * @author michel
 *
 */
public class SaadaDBStrapDemo extends SaadaDBConnector {

	protected SaadaDBStrapDemo(String dbname) {
		super(dbname);
	}
	
	protected SaadaDBStrapDemo() throws FatalException {
		super();
	}

	/* (non-Javadoc)
	 * @see saadadb.database.SaadaDBConnector#init()
	 */
	protected void init() throws FatalException {
		jdbc_url = "";	
		jdbc_driver = "";	
		jdbc_reader = "";	
		jdbc_reader_password = "";
		try {
			jdbc_connection = DriverManager.getConnection(jdbc_url, jdbc_reader, jdbc_reader_password);
			Statement _stmt = jdbc_connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
			ResultSet rs = _stmt.executeQuery("Select * from saadadb"); 
			while( rs.next() ) {
				dbname = rs.getString("name");
				root_dir= rs.getString("root_dir");
				repository= rs.getString("repository");
				url_root= rs.getString("url_root");
				description= rs.getString("description");
				
		      	jdbc_administrator = rs.getString("jdbc_administrator");
		      	 
		      	coord_sys= rs.getString("coord_sys");
				coord_equi= rs.getDouble("coord_equi");
				
				spect_unit= rs.getString("spect_coord_unit");
				spect_type= rs.getString("spect_coord_type");
				flux_unit= rs.getString("spect_flux_unit");
				return;
			}
			FatalException.throwNewException(SaadaException.DB_ERROR, "Cannot read SQL table <saadadb>");
		} catch (SQLException e) {
			FatalException.throwNewException(SaadaException.DB_ERROR, e);
		}
	}
}
