package saadadb.database;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import saadadb.configuration.RelationConf;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.sqltable.SQLTable;
import saadadb.util.Messenger;
import cds.astro.Coo;
import cds.astro.Qbox;


/**
 * @author michel
 * * @version $Id$

 * 07/2009: getInCircle uses QBOX for small cones and BOX + distance for larger ones 
 * 04/2012: Use qbox  i ADQL if columns are pos_ra/dec_csa
 * 11/2013: method setLocalBehavior invoked before to return a connection
 */
abstract public class DbmsWrapper {
	protected static DbmsWrapper wrapper;
	protected DBUser reader;
	protected DBUser admin;
	//	protected  Connection admin_connection;
	//	protected  Connection reader_connection;
	protected  String driver = null;
	protected String url = null;

	protected  String test_base;
	protected  String test_table ;
	public final boolean forwardOnly;

	/**
	 * @param forwardOnly
	 */
	protected DbmsWrapper(boolean forwardOnly) {
		this.forwardOnly = forwardOnly;
	}

	/**
	 * @return the generic name of the DBMS
	 */
	public abstract String getDBMS();
	/** 
	 * @param tmp_dir
	 * @param clean_after
	 * @return
	 * @throws Exception
	 */
	public boolean checkAdminPrivileges(String tmp_dir, boolean clean_after) throws Exception {
		Messenger.printMsg(Messenger.TRACE, "Check privilege for admin role in "  + this.url + test_base);
		Connection admin_connection = null;

		try {
			/*
			 * Drop the test DB if it exists
			 */
			dropTestDB(tmp_dir);
			/*
			 * Create the test DB
			 */
			createTestDB(tmp_dir);
			/*
			 * Connect the test DB
			 */
			String adm, admp, read, readp;
			if( this.admin == null ) {
				adm = "";
				admp = null;
			} else {
				adm = this.admin.getName();
				admp = this.admin.getPassword();
			} if( this.reader == null ) {
				read = "";
				readp = null;
			} else {
				read = this.reader.getName();
				readp = this.reader.getPassword();
			}
			/*
			 * Connect the Test db
			 */
			Database.setConnector(new SaadaDBStandAloneConnector(this.url + test_base, this.driver, test_base, adm, read, readp));
			Database.setAdminMode(admp);
			admin_connection = Database.getConnector().getJDBCConnection();
			admin_connection.setAutoCommit(true);
			/*
			 * Populate the Test db
			 */
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Populate DB " + test_base);
			Statement stmt = admin_connection.createStatement(this.getDefaultScrollMode(), this.getDefaultConcurentMode())	;
			loadSQLProcedures(stmt);		
			admin_connection.setAutoCommit(true);
			/*
			 * Create a table
			 */
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "UPDATE " + "CREATE TABLE " + test_table + " ( name text)");
			stmt.executeUpdate("CREATE TABLE " + test_table + " ( name text)" );
			String q = grantSelectToPublic(test_table);
			if( q != null && q.length() > 0 )
				stmt.executeUpdate(q);
			/*
			 * Populate the table from a file
			 */
			String tmp_filename = tmp_dir + Database.getSepar() + test_base.substring(test_base.lastIndexOf(Database.getSepar()) + 1)  + ".psqldump";
			BufferedWriter tmpfile = new BufferedWriter(new FileWriter(tmp_filename));
			tmpfile.write("'Pierre'\n");
			tmpfile.write("'Paul'\n");			
			tmpfile.write("'et les autres'\n");
			tmpfile.close();

			if( this.tsvLoadNotSupported() ) {
				this.storeTable(test_table, -1, tmp_filename) ;			
			} else {
				for(String str: this.getStoreTable(test_table, -1, tmp_filename) ) {
					stmt.executeUpdate(str);					
				}
			}
			stmt.close();

			stmt = admin_connection.createStatement(this.getDefaultScrollMode(), this.getDefaultConcurentMode())	;
			String qt = "select count(*) from " + test_table;
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Test query " + qt);
			ResultSet rs = stmt.executeQuery(qt);
			while( rs.next()) {
				int rc;
				if( (rc = rs.getInt(1)) != 3 ) {
					rs.close();
					admin_connection.close();
					IgnoreException.throwNewException(SaadaException.CORRUPTED_DB, "Wrong row count: <" + rc + "> return by SQL select.");
				} else {
					Messenger.printMsg(Messenger.TRACE, "procedure returns 0.5: OK");
				}
				break;
			}
			rs.close();
			qt = "select corner00_dec(0, 1) , boxoverlaps(1.0,2.0,3.0,4.0,5.0,6.0,7.0)";
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Test query " + qt);
			rs = stmt.executeQuery(qt);
			while( rs.next()) {
				double result =  rs.getDouble(1);
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Returns " + rs.getDouble(1) + " " + rs.getObject(2));
				if( result != -0.5 ) {
					rs.close();
					admin_connection.close();
					IgnoreException.throwNewException(SaadaException.CORRUPTED_DB, "Wrong result: <" + result + "> return by SQL select, should be 0.5");					
				}
				Messenger.printMsg(Messenger.TRACE, "SQL procedures seem to be OK");
				break;
			}
			rs.close();
			admin_connection.close();
			if( clean_after) {
				/*
				 * Drop the base
				 */
				/*
				 * Give 5 sec to the server to close th connection 
				 */
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
				}
				dropDB(null, test_base);
			}
			/*
			 * Make sure the db is closed in order to allow the user to make a second attempt
			 */
		} catch (SaadaException e) {
			Messenger.printStackTrace(e);
			if( admin_connection != null )
				admin_connection.close();
			FatalException.throwNewException(SaadaException.DB_ERROR, e.getMessage()) ;

		} catch (Exception e) {
			Messenger.printStackTrace(e);
			if( admin_connection != null )
				admin_connection.close();
			FatalException.throwNewException(SaadaException.DB_ERROR, e.getMessage()) ;
		}
		/*
		 * If something goes wrong an exception is risen
		 */
		return true;

	}

	/**
	 * @return
	 * @throws SQLException
	 * @throws IgnoreException
	 */
	public boolean checkReaderPrivileges() throws SQLException, IgnoreException {
		Messenger.printMsg(Messenger.TRACE, "Check privilege for reader role in " + url + test_base);
		if( reader == null ) {
			IgnoreException.throwNewException(SaadaException.WRONG_DB_ROLE, "No Reader Role");			
		}
		Connection reader_connection = DriverManager.getConnection(url + test_base , reader.getName(), reader.getPassword());
		Statement stmt = reader_connection.createStatement(this.getDefaultScrollMode(), this.getDefaultConcurentMode())	;
		String qt = "select count(*) from " + test_table;
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Test query " + qt);
		ResultSet rs = stmt.executeQuery(qt);
		while( rs.next()) {
			if( rs.getInt(1) != 3 ) {
				reader_connection.close();
				IgnoreException.throwNewException(SaadaException.CORRUPTED_DB, "Wrong row count: <" + rs.getRow() + "> return by SQL select.");
			}
			else {
				Messenger.printMsg(Messenger.TRACE, "test table readout: 3 rows found: OK");
			}
		}

		/*
		 * If something goes wrong an axception is risen
		 */
		reader_connection.close();
		return true;
	}

	/**
	 * @param name
	 * @param password
	 */
	public void setAdminAuth(String name, String password) {
		admin = new DBUser(name, password);		
	}

	/**
	 * @param name
	 * @param password
	 */
	public void setAdminAuth(String name, char[] password) {
		admin = new DBUser(name, password);		
	}
	/**
	 * @param name
	 * @param password
	 */
	public void setReaderAuth(String name, String password) {
		reader = new DBUser(name, password);		
	}
	/**
	 * @param name
	 * @param password
	 */
	public void setReaderAuth(String name, char[] password) {
		reader = new DBUser(name, password);		
	}

	/**
	 * @param tmp_dir
	 * @throws SQLException
	 */
	protected void dropTestDB(String tmp_dir) throws SQLException {
		/*
		 * Drop the test DB if it exists
		 */
		if( dbExists(null, test_base) ) {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "DB " + test_base + " already exists: drop it");
			dropDB(null, test_base);
			/*
			 * Give 5 sec to the server to close th connection 
			 */
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
			}
		}
	}

	/**
	 * @param tmp_dir
	 * @throws Exception
	 */
	protected void createTestDB(String tmp_dir) throws Exception {
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Create test DB " +  test_base);
		createDB(test_base);
	}
	/**
	 * @return
	 * @throws SQLException 
	 * @throws Exception 
	 */
	public Connection getConnection(String url, String user, String password) throws SQLException, Exception {
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "User " + user + " Connecting to " + url);
		Connection retour = DriverManager.getConnection(url , user, password);
		setLocalBehavior(retour);
		return retour;		
	}

	/**
	 * Return a new connection used for large queries. Connection must be transient to avoid memory
	 * to explode as it is used
	 * That cannot work with sqlite because we would have to load again SQL procs which is quite difficult
	 * @return
	 * @throws SQLException
	 */
	public Connection openLargeQueryConnection() throws Exception{
		Connection retour = DriverManager.getConnection(Database.getConnector().getJdbc_url()
				,Database.getConnector().getJdbc_reader()
				, Database.getConnector().getJdbc_reader_password());
		retour.setAutoCommit(false);
		setLocalBehavior(retour);
		return retour;
	}

	/**
	 * @param conn
	 */
	public void setLocalBehavior(Connection conn) throws Exception {}

	/**
	 * Close the connection open for large queries
	 * @param largeConnection
	 * @throws SQLException
	 */
	public void closeLargeQueryConnection(Connection largeConnection) throws SQLException{
		largeConnection.close();
	}

	/**() 
	 * @return
	 */
	public int getDefaultScrollMode() {
		return ResultSet.TYPE_SCROLL_INSENSITIVE;
	}
	/**
	 * @return
	 */
	public int getDefaultConcurentMode() {
		return ResultSet.CONCUR_READ_ONLY;
	}
	/**
	 * returns an option to be appended to jdbcurl
	 * @return
	 */
	public String getOptions() {
		return "";
	}


	/**
	 * Does not return a statement bu executye it because of SQLLite which has no SQL stement doing that.
	 * JDBC meta data cannot access column description for temporary tables: column number can be given by ncols
	 * @param table_name
	 * @param ncols used to indicate the number of columns (automatic if -1)
	 * @param table_file
	 * @throws Exception
	 */
	public abstract String[] getStoreTable(String table_name, int ncols, String table_file) throws Exception;

	/**
	 * Says if a drop table can be achieved within a transaction (false for SQLIte)
	 * @return
	 */
	public boolean supportDropTableInTransaction() {
		return true;
	}
	/**
	 * Says whether the DBMS can alter a column of an existing table 
	 * @return
	 */
	public boolean supportAlterColumn() {
		return true;
	}
	/**
	 * Returns true if there is no SQL statement loading data from a file
	 * In that case, the wrapper does this job out of the current transaction
	 * @return
	 */
	public boolean tsvLoadNotSupported() {
		return false;
	}

	public  void storeTable(String tableName, int ncols, String tableFile) throws Exception {
	}

	/**
	 * Returns an SQL statement checking that the position expressed by the expression ra_stm/dec_stm is contained into the box 
	 * @param ra_stm
	 * @param dec_stm
	 * @param box_ra
	 * @param box_dec
	 * @param box_size_ra
	 * @param box_size_dec
	 * @return
	 */
	static public String getIsInBoxConstraint(String ra_stm, String  dec_stm, String box_ra, String box_dec,  String box_size_ra,  String box_size_dec) {
		return "isinbox("+box_ra+","+box_dec+","+box_size_ra+","+box_size_dec+","+ra_stm+","+dec_stm+")";
	}

	/**
	 * The candidate image overlaps some part of the ROI.
	 * @param prefix
	 * @param ra
	 * @param dec
	 * @param size
	 * @return
	 */
	static public String getImageOverlapConstraint(String prefix, double ra, double  dec, double size ) {
		return "boxoverlaps(" + ra  
		+ ", " + dec 
		+ ", " + size 
		+ ", " + prefix + "pos_ra_csa"
		+ ", " + prefix + "pos_dec_csa"
		+ ", " + prefix + "size_alpha_csa"
		+ ", " + prefix + "size_delta_csa)";
	}

	/**
	 * The candidate image covers or includes the entire ROI.
	 * The 4 ROI corners must contained within the image
	 * @param prefix
	 * @param ra
	 * @param dec
	 * @param size_ra
	 * @param size_dec
	 * @return
	 */
	static public String getImageCoverConstraint(String prefix, double ra, double  dec, double size_ra, double size_dec ) {
		return "boxcovers(" + ra + "," + dec + "," + size_ra  + ", " + prefix + "pos_ra_csa," + prefix + "pos_dec_csa, " + prefix + "size_alpha_csa,"+ prefix + "size_delta_csa)";
	}

	/**
	 * The candidate image overlaps the center of the ROI.
	 * @param prefix
	 * @param ra
	 * @param dec
	 * @param size_ra
	 * @param size_dec
	 * @return
	 */
	static public String getImageCenterConstraint(String prefix, double ra, double  dec, double size_ra, double size_dec ) {
		return "boxcenter(" + ra + "," + dec + "," + size_ra  + ", " + prefix + "pos_ra_csa," + prefix + "pos_dec_csa, " + prefix + "size_alpha_csa,"+ prefix + "size_delta_csa)";
	}
	/**
	 * The candidate image is entirely enclosed by the ROI
	 * The 4 image corners must be in the ROI
	 * @param prefix
	 * @param ra
	 * @param dec
	 * @param size_ra
	 * @param size_dec
	 * @return
	 */
	static public String getImageEnclosedConstraint(String prefix, double ra, double  dec, double size_ra ) {
		return "boxenclosed(" + ra + "," + dec + "," + size_ra  + ", " + prefix + "pos_ra_csa," + prefix + "pos_dec_csa, " + prefix + "size_alpha_csa,"+ prefix + "size_delta_csa)";
	}
	/**
	 * @param prefix
	 * @param asc
	 * @param dec
	 * @param size
	 * @return
	 */
	static public String getIsInCircleConstraint(String prefix, double asc, double  dec, double size) {
		Coo coo = new Coo(asc, dec);
		Enumeration<Qbox> qenum = Qbox.circle(coo, size);
		Qbox qb;
		String anystring="";
		String somestring="";
		String alias = "";
		int any=0, some=0;
		if( prefix != null && prefix.length() > 0 ) {
			alias = prefix + ".";
		}
		int last_any_qbnum = -1, any_start = -1, any_stop = -1;
		int last_some_qbnum = -1, some_start = -1, some_stop = -1;
		int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
		int nb_qboxes=0;
		while( qenum.hasMoreElements()  ) {
			qb = qenum.nextElement();
			int qbnum = qb.box();
			/*
			 * extrema detection
			 */
			if( qbnum < min ) min = qbnum;
			if( qbnum > max ) max = qbnum;
			/*
			 * Processing QBOXES covered by the circle
			 */
			if( qb.isAny()) {
				any++;;
				if( any_start == -1 ) {
					any_start = qbnum;
					last_any_qbnum= qbnum;
				}
				if( qbnum != (last_any_qbnum+1) && qbnum != last_any_qbnum) {
					any_stop = last_any_qbnum;
					if( anystring.length() != 0 ) {
						anystring += "\n OR ";
					}
					if( any_start == any_stop ) {
						anystring += alias + "sky_pixel_csa = " + any_start;
					} else {
						anystring += alias + "sky_pixel_csa >= " + any_start + " AND " + alias + "sky_pixel_csa <= " + any_stop ;
					}
					any_start = qbnum;
				}
				last_any_qbnum = qbnum;
			}
			/*
			 * Processing QBOXES interstecting with the circle
			 */
			else {
				some++;;
				if( some_start == -1 ) {
					some_start = qbnum;
					last_some_qbnum= qbnum;
				}
				if( qbnum != (last_some_qbnum+1) && qbnum != last_some_qbnum) {
					some_stop = last_some_qbnum;
					if( somestring.length() != 0 ) {
						somestring += "\n OR ";
					}
					if( some_start == some_stop ) {
						somestring += alias + "sky_pixel_csa = " + some_start;
					} else {
						somestring += alias + "sky_pixel_csa >=" + some_start + " AND " + alias + "sky_pixel_csa <= " + some_stop ;
					}
					some_start = qbnum;
				}
				last_some_qbnum = qbnum;
			}
			nb_qboxes++;
		}
		/*
		 * Compute a constraint delimiting a square surrounding the cone
		 */
		String B = "";
		//		if( dec > -85 && dec < 85 ) {
		//			double ra_min = asc - size/Math.abs(Math.cos(57.3*dec));
		//			if( ra_min < 0 ) ra_min = 360 + ra_min;
		//			double ra_max = asc +  size/Math.abs(Math.cos(57.3*dec));
		//			if( ra_min > 360 ) ra_min = ra_min - 360;
		//			double dec_min = dec -  size;
		//			double dec_max = dec +  size;
		//			B = "(" + prefix + ".pos_ra_csa > " + ra_min + " AND " + prefix + ".pos_ra_csa < " + ra_max 
		//			+ " AND " + prefix + ".pos_dec_csa > " + dec_min + " AND " + prefix + ".pos_dec_csa < " + dec_max + ")";
		//		}
		B = "isinbox(" + asc + ", " + dec + ", " + size  + ", " + size + ", " + alias + "pos_ra_csa" + ", " + alias + "pos_dec_csa) ";
		/*
		 * Compute the BQoc constraint
		 */
		if( any > 0 ) {
			if( anystring.length() != 0 ) {
				anystring += "\n OR ";
			}
			if( any_start == last_any_qbnum ) {
				anystring += alias + "sky_pixel_csa = " + any_start;
			} else {
				anystring += alias + "sky_pixel_csa >= " + any_start + " AND " + alias + "sky_pixel_csa <= " + last_any_qbnum ;
			}
		}
		if( some > 0 ) {
			if( somestring.length() != 0 ) {
				somestring += "\n OR ";
			}
			if( some_start == last_some_qbnum ) {
				somestring += alias + "sky_pixel_csa = " + some_start;
			} else {
				somestring += alias + "sky_pixel_csa >= " + some_start + " AND " + alias + "sky_pixel_csa <= " + last_some_qbnum ;
			}
		}
		String Q = "(" + alias + "sky_pixel_csa >= " + min + " AND " + alias + "sky_pixel_csa <= " + max + ") \nAND (\n"; 
		if( anystring.length() > 0 ) {
			Q += "(" + anystring + ")";
		}
		/*
		 * Compute the distance constraint and achieve the QBox one
		 */
		String D="";
		if( somestring.length() > 0 ) {
			double x1 = Math.cos(Math.toRadians(dec))*Math.cos(Math.toRadians(asc));
			double y1 = Math.cos(Math.toRadians(dec))*Math.sin(Math.toRadians(asc));
			double z1 = Math.sin(Math.toRadians(dec));
			D = " degrees((2*asin( sqrt("
				+   " ("+x1+"-"+alias+"pos_x_csa)*("+x1+"-"+alias+"pos_x_csa)"
				+   "+("+y1+"-"+alias+"pos_y_csa)*("+y1+"-"+alias+"pos_y_csa)"
				+   "+("+z1+"-"+alias+"pos_z_csa)*("+z1+"-"+alias+"pos_z_csa) )/2)))" ;
			D = "(abs("+D+")< "+size+")";

			if( anystring.length() != 0 ) {
				Q += "\nOR\n";
			}

			Q += "((" + somestring + ") AND " + D + ")";
		}
		Q += ")";

		if( nb_qboxes > 100 && B.length() != 0 ){
			return B + "\nAND " + D;
			//return B ;
		} else {
			return Q;
		}
	}

	/**
	 * @param prefix
	 * @param asc
	 * @param dec
	 * @param size
	 * @return
	 */
	static public String getADQLIsInCircleConstraint(String asc, String dec, String circleAsc, String circleDec, String radius) {
		if( asc.equalsIgnoreCase("pos_ra_csa") && asc.equalsIgnoreCase("pos_dec_csa") ) {
			return getIsInCircleConstraint("", Double.parseDouble(circleAsc), Double.parseDouble(circleDec), Double.parseDouble(radius));
		}	

		// Compute a constraint delimiting a square surrounding the cone:
		String B = getIsInBoxConstraint(asc, dec, circleAsc, circleDec, "("+radius+")*2", "("+radius+")*2");

		// Compute the distance constraint and achieve the QBox one:
		String D = "distancedegree("+circleAsc+", "+circleDec+", "+asc+", "+dec+")";
		D = "(abs("+D+")< "+radius+")";

		return B + "\nAND " + D;
	}

	/**
	 * @param dbname
	 * @throws SQLException
	 * @throws FatalException 
	 * @throws Exception 
	 */
	public abstract void createDB(String dbname) throws Exception;

	/**
	 * Method used before the SAADADB creation. That is why the repository must be specified in parameters
	 * @param repository
	 * @param dbname
	 * @throws SQLException
	 */
	public abstract void dropDB(String repository, String dbname) throws SQLException;

	/**
	 * Method used before the SAADADB creation. That is why the repository must be specified in parameters
	 * @param repository  used for embedded DB
	 * @param dbname  
	 * @return
	 */
	public abstract boolean dbExists(String repository, String dbname);


	/**
	 * @throws SQLException
	 */
	public abstract void cleanUp() throws SQLException;


	/**
	 * 
	 */
	public abstract String abortTransaction() ;

	/**
	 * 
	 */
	public abstract String lockTable(String table) ;

	/**
	 * @param write_table
	 * @param read_table
	 * @return
	 * @throws Exception 
	 */
	public abstract String lockTables(String[] write_table, String[] read_table) throws Exception;

	/**
	 * 
	 */
	public abstract String dropTable(String table) ;

	/**
	 * @param table
	 * @return
	 */
	public abstract String grantSelectToPublic(String table);

	/**
	 * @param java_type
	 * @return
	 */
	public static String getDBTypeFromJavaType(String java_type){
		return null;
	}

	/**
	 * On Mysql, text type can neither be indexed nor used as prim key. So varchar(255) is used despite of 
	 * the limitation length. Tus type must be define for each RDBMS
	 * @return
	 */
	public abstract String getIndexableTextType() ;

	/**
	 * @param val
	 * @return
	 */
	public abstract String getBooleanAsString(boolean val);

	public abstract boolean getBooleanValue(Object rsval);


	/**
	 * @param type
	 * @return
	 * @throws FatalException 
	 */
	public abstract String getSQLTypeFromJava( String type) throws FatalException ;

	/**
	 * @param type
	 * @return
	 * @throws FatalException 
	 */
	public abstract String getJavaTypeFromSQL( String type) throws FatalException ;


	/**
	 * @param table_to_update
	 * @param table_to_join
	 * @param join_criteria  statement used to filter the join between table_to_update and table_to_join
	 * @param key_alias
	 * @param keys   names f the columns to be updated
	 * @param values values to be affected to the keys (columns names or constants)
	 * @param select_criteria statement used to filter rows of table_to_update
	 * @return
	 */
	public abstract String getUpdateWithJoin(String table_to_update, String table_to_join, String join_criteria, String key_alias, String[] keys, String[] values, String select_criteria) ;

	/**
	 * Returns the name of the table to lock for GRANT user privil�ges
	 * @return
	 */
	public String[] getUserTables() {
		return null;
	}

	/**
	 * @return Returns the driver.
	 */
	public String getDriver() {
		return driver;
	}

	/**
	 * @return
	 */
	public String unlockTables() {
		return "";
	}

	/**
	 * @return
	 */
	public abstract String getRegexpOp();

	/**
	 * @param arg1
	 * @param arg2
	 * @return
	 */
	public abstract String getStrcatOp(String... args);
	/**
	 * @return
	 */
	public abstract String getInsertStatement(String where, String[] fields, String[] values);

	/**
	 * @param table
	 * @param table_columns
	 * @param columns_alias
	 * @return
	 * @throws Exception 
	 */
	public String selectIntoTempoTable(String table, String[] table_columns, String[] columns_alias) throws Exception{
		String retour = "INSERT INTO " + table + "(";
		String cn="", ca="";
		for( int i=0 ; i<table_columns.length ; i++ ) {
			if( cn.length() != 0 ) {
				cn += ", ";
				ca += ", ";
			}
			cn += table_columns[i];
			ca += columns_alias[i];
		}
		retour += cn + ")\nSELECT " + ca + " ";
		return retour;
	}

	/**
	 * @return Returns the url.
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Returms the JDBC URLS is it gigures out in saadadb.xml
	 * @param repository:  Used in case of embedded DB
	 * @param dbname: Name of the RDB (usually the same as the the SaadaDB name)
	 * @return
	 */
	public String getJdbcURL(String repository, String dbname) {
		return this.getUrl() + dbname + this.getOptions() ;
	}

	/**
	 * @param relation_conf
	 * @throws SaadaException 
	 */
	public abstract void createRelationshipTable(RelationConf relation_conf) throws SaadaException ;

	/**
	 * The trigger can dramatically slow down the insertion rate In case of massive insertion in the relation table (SQLLITE)
	 * So we remove it before to do such operation. Its job will be done by  @see setClassColumns
	 * @param relationName
	 * @throws AbortException
	 */
	public abstract void suspendRelationTriggger(String relationName) throws AbortException;

	/**
	 * Populate the class columns and restore the trigger so that these columns will be kept uptodate
	 * whatever the opeations done on the table
	 * @param relationName
	 * @throws AbortException
	 */
	public abstract void setClassColumns(String relationName) throws AbortException;
	/**
	 * Return the statement indexing the secondary class ids  of the relationship behavior specific for MySQL 
	 * {@link MysqlWrapper#getSecondaryClassRelationshipIndex(String)}  
	 * @param relationName
	 * @return
	 */
	public  abstract String getSecondaryClassRelationshipIndex(String relationName);	

	/**
	 * Return the statement indexing the primary class ids  of the relationship behavior specific for MySQL 
	 * {@link MysqlWrapper#getPrimaryClassRelationshipIndex(String)}  
	 * @param relationName
	 * @return
	 */
	public abstract String getPrimaryClassRelationshipIndex(String relationName);
	
	/**
	 * Return the statement indexing the primary oid of the relationship behavior specific for MySQL 
	 * {@link MysqlWrapper#getPrimaryRelationshipIndex(String)}  
	 * @param relationName
	 * @return
	 */
	public String [] getPrimaryRelationshipIndex(String relationName) {
		return new String[]{"CREATE INDEX " + relationName + "_oidprimary ON " + relationName + "(oidprimary)"};
	}

	/**
	 * Return the column (native or computed) used to select primary class in a relationship table
	 * @return
	 */
	public abstract String getPrimaryClassColumn() ;
	/**
	 * Return the column (native or computed) used to select secondary class in a relationship table
	 * @return
	 */
	public abstract String getSecondaryClassColumn() ;

	/**
	 * Returns true if the table table_name does exists in the DB. This method handle the case sensitivity of the wrapped RDBMS
	 * @param searched_table
	 * @return
	 */
	public abstract boolean tableExist(String searched_table)throws Exception;

	/**
	 * Return the (COLUMN_NAME, TYPE_NAME) tuples for all columns of the searched_table
	 * if it exist. Retuen NULL else.
	 * @param searched_table
	 * @return
	 * @throws Exception
	 */
	public abstract ResultSet getTableColumns(String searched_table)throws Exception;

	/**
	 * Return a map <index_name, column_name> of all ndexes of table searched_table if it exists
	 * Returns null else
	 * @param table
	 * @return
	 * @throws FatalException
	 */
	public abstract Map<String, String> getExistingIndex(String table) throws FatalException;

	/**
	 * Used by queries looking like
	 * SELECT xxxx FROM yyyy getExceptStatement() (SELECT zzzz...)
	 * @param key used bu Mysql not in statement
	 * @return
	 */
	public abstract String getExceptStatement(String key) ;

	/**
	 * @param table_name
	 * @param index_name
	 * @return
	 */
	public abstract String getDropIndexStatement(String table_name, String index_name) ;

	/**
	 * Some index (e.g. on primary keys with SQLITE) can be undroppable. Trying to drop them rise fatal errors
	 * @param indexname
	 * @return
	 */
	public boolean isIndexDroppable(String indexname) {
		return true;
	}
	/**
	 * @param coll_name
	 * @param cat
	 * @return
	 * @throws FatalException
	 */
	public abstract String getCollectionTableName(String coll_name, int cat) throws FatalException ;

	/**
	 * Returns an alias (e.g. as XXX) to be appended to complex SQL queries :
	 * select X from(...UNION/JOIN..) USING (oidsaada) as XXX
	 * @param alias
	 * @return
	 */
	public abstract String getGlobalAlias(String alias) ;

	/**
	 * @param token
	 * @return
	 */
	public abstract String castToString(String token);

	/**
	 * This statement ask INSERT statement to use default values for columns not set (not supported by SQLITE)
	 * @return
	 */
	public String getInsertAutoincrementStatement() {
		return "DEFAULT";
	}

	/**
	 * Return serial token for primary keys
	 * @return
	 */
	public  String getSerialToken() {
		return "SERIAL";
	}

	/**
	 * Statement setting a ' into an insert
	 * @return
	 */
	public  String getEscapeQuote(String val) {
		if( val != null ) {
			return val.replaceAll("'", "\\\\'");
		} else {
			return "";
		}

	}

	/**
	 * This features is used to save the case or to put reserved keywords in SQL statement
	 * e.g. create a table named keys with MySQL
	 * @param entity
	 * @return
	 */
	public  String getQuotedEntity(String entity) {
		return "\"" + entity + "\"";
	}

	/**
	 * @return
	 */
	public  String getAsciiNull() {
		return "\\N";
	}

	/**
	 * Return a string to be added to the DB name to get the name of the temporary area.
	 * @param dbname
	 * @return
	 */
	public abstract String getTempodbName(String dbname) ;

	/**
	 * Although being standard SQL, this statement must be adapted, because Mysql can not handle queries with
	 * multiple references to one tempo table. So temporary tables must be considered persistent table and then must
	 * be removed explicitly:  http://bugs.mysql.com/bug.php?id=10327
	 * @param table_name
	 * @return
	 * @throws FatalException 
	 */
	public abstract String getCreateTempoTable(String table_name, String fmt) throws FatalException;

	/**
	 * Usage of () can make problems
	 * @param tablename
	 * @param select
	 * @return
	 */
	public String getCreateTableFromSelectStatement(String tablename, String select) {
		return "CREATE TABLE " + tablename + " AS (" + select + ")";
	}

	/**
	 * @param main_query
	 * @param sec_query
	 * @return
	 * @throws FatalException 
	 */
	public String getSelectWithExcept(String main_query, String key, String sec_query) throws FatalException {
		return main_query + " " 
		+ this.getExceptStatement(key) 
		+ "(" + sec_query + ")";
	}

	/**
	 * return query returning all key1 value of tavle1 which are not in the key2 col
	 * @param leftTable
	 * @param leftKey
	 * @param rightTable
	 * @param rightKey
	 * @return
	 * @throws FatalException
	 */
	public String getNullLeftJoinSelect(String leftTable, String leftKey, String rightTable, String rightKey) throws FatalException {		
		return "SELECT  " + leftKey + " FROM " + leftTable + " l"
		+ " LEFT JOIN " + rightTable + " r"
		+ " ON l." + leftKey + " = r." + rightKey 
		+ " WHERE r." + rightKey + " IS NULL";
	}

	/**
	 * return query removing all key1 value of tavle1 which are not in the key2 col
	 * @param leftTable
	 * @param leftKey
	 * @param rightTable
	 * @param rightKey
	 * @return
	 * @throws FatalException
	 */
	public String getNullLeftJoinDelete(String leftTable, String leftKey, String rightTable, String rightKey) throws FatalException {		
		return "DELETE " + leftTable + " FROM " + leftTable 
		+ " LEFT JOIN " + rightTable 
		+ " ON " + leftTable + "." + leftKey + " = " + rightTable + "." + rightKey 
		+ " WHERE " + rightTable + "." + rightKey + " IS NULL";
	}
	/**
	 * Returns tempodbname.tablename for DB system requiring a separate DB for tempo tables
	 * @param table_name
	 * @return
	 */
	public abstract String getTempoTableName(String table_name)throws FatalException;

	public abstract String getDropTempoTable(String table_name) throws FatalException;

	/**
	 * Returns a query sequence because SQLITE cannot directly change a column type
	 * @param table
	 * @param column
	 * @return
	 * @throws QueryException 
	 * @throws Exception 
	 */
	public abstract String[] changeColumnType(String table, String column, String type) throws QueryException, Exception;

	/**
	 * @param table
	 * @param column
	 * @param type
	 * @return
	 * @throws QueryException
	 * @throws Exception
	 */
	public abstract String[] addColumn(String table, String column, String type) throws QueryException, Exception;

	/**
	 * @param table
	 * @param column
	 * @return
	 * @throws QueryException
	 */
	public String dropColumn(String table, String column)throws QueryException {
		return "ALTER TABLE " + table + " DROP COLUMN  " + column;
	}

	/**
	 * 	operation not supported by SQLite
	 * @param table
	 * @param column
	 * @param newName
	 * @return
	 * @throws QueryException
	 * @throws Exception 
	 */
	public abstract String renameColumn(String table, String column, String newName) throws  Exception ;

	/**
	 * Thank to MySQL we have to manage our own temporary tables. Their names must be recorded and available to
	 * the the cleaner of SQLTable. This service must be called explicitly by the programm.
	 * @return
	 */
	public Set<String> getReferencedTempTable() {
		return new TreeSet<String>() ;
	}



	/**
	 * Return an existing directory where SQL porcedurem are. Look first in ECLIPSE WS and then in SAADA_DB_HOME
	 * @return
	 * @throws Exception
	 */
	abstract protected File getProcBaseRef() throws Exception ;

	/**
	 * INstall  the language used by the procedure (does nothing except for PSQL) 
	 */
	protected void installLanguage() throws Exception {

	}
	protected void installLanguage(Statement stmt) throws Exception {

	}

	/**
	 * Works with MySQl which can not CREATE or REPLACE a function
	 * @return
	 * @throws Exception 
	 */
	protected String[] removeProc() throws Exception {
		return new String[0];
	}
	/**
	 * @throws Exception
	 */
	public void loadSQLProcedures() throws Exception {
		SQLTable.beginTransaction();
		this.installLanguage();
		String[] rp = this.removeProc();
		for( String p: rp) {
			SQLTable.addQueryToTransaction(p);
		}
		File bf = this.getProcBaseRef();
		if( bf != null ) {
			String[] fs = bf.list();
			Messenger.printMsg(Messenger.TRACE, "Reading SQL proc files from " + bf.getAbsolutePath());
			for( String f: fs ) {
				if( f.endsWith(".sql") ) {
					if (Messenger.debug_mode)
						Messenger.printMsg(Messenger.DEBUG, "Reading  SQL file " + f);
					BufferedReader br = new BufferedReader(new FileReader(bf.getAbsolutePath() + Database.getSepar() + f) );
					String str;
					StringBuffer sb = new StringBuffer();
					while( (str = br.readLine()) != null) {
						sb.append(str + "\n");
					}
					br.close();
					SQLTable.addQueryToTransaction(sb.toString());
				}
			}
		}
		SQLTable.commitTransaction();
	}

	public void loadSQLProcedures(Statement stmt) throws Exception {
		SQLTable.beginTransaction();
		this.installLanguage(stmt);
		String[] rp = this.removeProc();
		for( String p: rp) {
			stmt.executeUpdate(p);
			//SQLTable.addQueryToTransaction(p);
		}
		File bf = this.getProcBaseRef();
		if( bf != null ) {
			String[] fs = bf.list();
			Messenger.printMsg(Messenger.TRACE, "Reading SQL proc files from " + bf.getAbsolutePath());
			for( String f: fs ) {
				if( f.endsWith(".sql") ) {
					if (Messenger.debug_mode)
						Messenger.printMsg(Messenger.DEBUG, "Reading  SQL file " + f);
					BufferedReader br = new BufferedReader(new FileReader(bf.getAbsolutePath() + Database.getSepar() + f) );
					String str;
					StringBuffer sb = new StringBuffer();
					while( (str = br.readLine()) != null) {
						sb.append(str + "\n");
					}
					br.close();
					stmt.executeUpdate(sb.toString());
					//SQLTable.addQueryToTransaction(sb.toString());
				}
			}
		}
		SQLTable.commitTransaction();
	}

	/**
	 * Method used to detect if a password must be asked
	 * @return
	 */
	public boolean supportAccount() {
		return true;
	}

	/**
	 * Does it for DBMS supporting this feature, not for SQLITE which consider the fetch size as a result limit
	 * @param stmt
	 * @param size
	 * @throws SQLException
	 */
	public void setFetchSize(Statement stmt, int size) throws SQLException {
		stmt.setFetchSize(size);
	}
	/*
	 * Returns a map of SQL statement helping the setup of the relationships
	 */
	public abstract Map<String, String> getConditionHelp();

	public static void main(String[] args) throws AbortException, FatalException, SQLException, ClassNotFoundException {
		System.out.println(DbmsWrapper.getIsInCircleConstraint("", 0, 0, 1));
		//		ArgsParser ap = new ArgsParser(args);
		//		Database.init(ap.getDBName());
		//		Database.getConnector().setAdminMode("password");
		//		SQLTable.runQueryUpdateSQL(Database.getWrapper().loadSQLProcedures(), false, null);

	}


}
