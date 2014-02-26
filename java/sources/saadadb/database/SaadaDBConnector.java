package saadadb.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.xml.parsers.SAXParserFactory;

import org.postgresql.util.PSQLException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.util.Messenger;
import saadadb.util.SaadaConstant;
import cds.astro.Astroframe;
import cds.astro.Ecliptic;
import cds.astro.FK4;
import cds.astro.FK5;
import cds.astro.Galactic;
import cds.astro.ICRS;

/**
 * @author michel
 * * @version $Id: SaadaDBConnector.java 936 2014-02-07 13:56:44Z laurent.mistahl $

 * 07/2009: set jdbc_connection in autocommit = false
 */
public class SaadaDBConnector extends DefaultHandler {

	protected String dbname;
	protected String root_dir;
	protected String repository;
	protected String description;

	protected String coord_sys;
	protected double coord_equi;
	protected Astroframe astroframe;

	protected String spect_unit;
	protected String spect_type;
	protected String flux_unit;
	protected int healpix_level;
	protected  String jdbc_url = null;	
	protected  String jdbc_driver = null;	
	protected  String jdbc_dbname;
	protected  String jdbc_administrator;
	protected  String jdbc_reader;
	protected  String jdbc_reader_password;
	protected  Connection jdbc_connection;

	protected  String webapp_home;
	protected  String url_root;

	protected DbmsWrapper wrapper;
	private boolean admin_mode = false;
	/*
	 * XML parser buffer
	 */
	private static String str;
	private static String previous;

	private static SaadaDBConnector instance=null;
	public final static String separ = System.getProperty("file.separator");



	/**
	 * @throws FatalException 
	 * 
	 */
	public  SaadaDBConnector() throws FatalException {
		this.init();
	}

	/**
	 * Create a connection in reader mode by default. The admin mode must be asked explicitely
	 * @throws FatalException 
	 */
	protected void init() throws FatalException {
		try {
			if( this.jdbc_url != null ) {
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Open new DB connection");
//				Class.forName(this.jdbc_driver);
//				jdbc_connection = DriverManager.getConnection(this.jdbc_url, this.jdbc_reader, this.jdbc_reader_password);
//				jdbc_connection.setAutoCommit(true);
			}
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			FatalException.throwNewException(SaadaException.DB_ERROR, e);
		}
	}
	
	/**
	 * Provisoire pour tester le spooler
	 * @return
	 * @throws SQLException
	 */
	public Connection getNewConnection() throws SQLException {
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Create a new reader connection on " + this.jdbc_url);
		return  DriverManager.getConnection(this.jdbc_url, this.jdbc_reader, this.jdbc_reader_password);
		
	}
	/**
	 * @param password
	 * @return
	 * @throws SQLException
	 */
	public Connection getNewAdminConnection(String password) throws SQLException {
		Messenger.printMsg(Messenger.DEBUG, "Create a new admin connection on " + this.jdbc_url);
		return  DriverManager.getConnection(this.jdbc_url, this.jdbc_administrator, password);
		
	}

	/**
	 * OPen a new DB connection. Can be used when the DB server has been restarted
	 * @throws FatalException
	 */
	public void reconnect() throws FatalException {
		try {
			if( this.jdbc_url != null ) {
				Messenger.printMsg(Messenger.TRACE, "Reconnect to the DB");
				if( jdbc_connection != null  ) {
					if (Messenger.debug_mode)
						Messenger.printMsg(Messenger.DEBUG, "Close old connection");
					jdbc_connection.close();
				}
				Class.forName(this.jdbc_driver);
				jdbc_connection = DriverManager.getConnection(this.jdbc_url, this.jdbc_reader, this.jdbc_reader_password);
				jdbc_connection.setAutoCommit(true);

			}
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			FatalException.throwNewException(SaadaException.DB_ERROR, e);
		}

	}

	/**
	 * 
	 */
	public void readSaadadbTable() {
		try {
			Class.forName(jdbc_driver);
			if( jdbc_connection != null && !jdbc_connection.isClosed()) {
				Messenger.printMsg(Messenger.WARNING, "Close the former connection");
				jdbc_connection.close();
			}
			jdbc_connection     = getWrapper().getConnection(jdbc_url, jdbc_reader, jdbc_reader_password);				
			jdbc_connection.setAutoCommit(true);
			Statement _stmt = jdbc_connection.createStatement();
			ResultSet rs    = _stmt.executeQuery("Select * from saadadb"); 
			while( rs.next() ) {
				dbname     = rs.getString("name");
				root_dir   = rs.getString("root_dir");
				repository = rs.getString("repository");
				description= rs.getString("description");

				jdbc_administrator = rs.getString("jdbc_administrator");

				url_root   = rs.getString("url_root");
				webapp_home= rs.getString("webapp_home");

				coord_sys  = rs.getString("coord_sys");
				coord_equi = rs.getDouble("coord_equi");   	
				spect_unit = rs.getString("spect_coord_unit");
				spect_type = rs.getString("spect_coord_type");
				flux_unit  = rs.getString("spect_flux_unit");
				try {
					healpix_level = rs.getInt("healpix_level");
				} catch(Exception e){
					Messenger.printMsg(Messenger.WARNING, "Column healpix_level not in saadadb table (take level=15): looks like an old version. Run the upgrade tool please!!");
					healpix_level = 15;
				}
				rs.close();
				_stmt.close();
				if( this.coord_sys.equals("FK4") ) {
					this.astroframe = new FK4(this.coord_equi);
				}
				else if( this.coord_sys.equals("FK5") ) {
					this.astroframe = new FK5(this.coord_equi);
				}
				else if( this.coord_sys.equals("ICRS") ) {
					this.astroframe = new ICRS();
				}
				else if( this.coord_sys.equals("Galactic") ) {
					this.astroframe = new Galactic();
				}
				return;
			}
			FatalException.throwNewException(SaadaException.DB_ERROR, "Cannot read SQL table <saadadb>");
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			System.exit(1);
		}
	}
	/**
	 * @param dbname
	 */
	protected SaadaDBConnector(String dbname) {

	}

	/**
	 * @param dbname
	 * @return
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public static SaadaDBConnector getConnector(String dbname, boolean force) throws FatalException {
		try {
			if( instance == null || force) {
				/*
				 * Normal use, the caller (API or datalaoder) knows the database name
				 * The connector can be configured
				 */
				if( dbname != null ) {
					Class cls = Class.forName("generated." + dbname + ".SaadaDBStrap");
					instance = ((SaadaDBConnector)(cls.newInstance()));
				}
				/*
				 * At SaadaDB creation time, the db name is unknown until the XML file is parsed
				 * We need then a unconfigured connector
				 */
				else {
					instance = new SaadaDBConnector();
				}
			}
			return instance;	
		} catch(Exception e) {
			Messenger.printStackTrace(e);
			FatalException.throwNewException(SaadaException.INTERNAL_ERROR, e);
			return null;
		}
	}

	/**
	 * @return
	 */
	public double getCoord_equi() {
		return coord_equi;
	}
	/**
	 * @return
	 */
	public String getCoord_sys() {
		return coord_sys;
	}
	/**
	 * @return
	 */
	public String getDbname() {
		return dbname;
	}
	/**
	 * @return
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @return
	 */
	public String getFlux_unit() {
		return flux_unit;
	}
	/**
	 * @return
	 */
	public String getJdbc_driver() {
		return jdbc_driver;
	}
	/**
	 * @return
	 */
	public String getJdbc_url() {
		return jdbc_url;
	}
	/**
	 * @return
	 */
	public String getRepository() {
		return repository;
	}
	/**
	 * @return
	 */
	public String getRoot_dir() {
		return root_dir;
	}
	/**
	 * @param root_dir
	 */
	public void setRoot_dir(String root_dir) {
		this.root_dir = root_dir;
	}
	/**
	 * @return
	 */
	public String getSpect_unit() {
		return spect_unit;
	}
	/**
	 * @return
	 */
	public int getHealpix_level() {
		return healpix_level;
	}
	/**
	 * @return
	 * @throws FatalException 
	 */
	public Connection getJDBCConnection() throws FatalException {
		if( this.jdbc_connection == null ) {
			this.init();
		}
		return jdbc_connection;
	}
	/**
	 * @return
	 */
	public String getSpect_type() {
		return spect_type;
	}
	/**
	 * @return Returns the separ.
	 */
	public static String getSepar() {
		return separ;
	}
	/**
	 * @return Returns the jdbc_administrator.
	 */
	public String getJdbc_administrator() {
		return jdbc_administrator;
	}
	/**
	 * @return Returns the jdbc_dbname.
	 */
	public String getJdbc_dbname() {
		return jdbc_dbname;
	}

	/**
	 * @return Returns the jdbc_reader.
	 */
	public String getJdbc_reader() {
		return jdbc_reader;
	}
	/**
	 * @return Returns the url_root.
	 */
	public String getUrl_root() {
		return url_root;
	}
	/**
	 * Should be private byut used any way be the web interface to run on a debug system
	 * @param url_root
	 */
	public void setUrl_root(String url_root) {
		this.url_root = url_root;
	}

	/**
	 * @return Returns the webapp_home.
	 */
	public String getWebapp_home() {
		return webapp_home;
	}

	/**
	 * @return Returns the jdbc_reader_password.
	 */
	public String getJdbc_reader_password() {
		return jdbc_reader_password;
	}

	/*
	 * The following methods ar eused of Saada (not SaadaDB) at creation time to
	 * take database configuration from the XML file.
	 * (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	public void  ParserSaadaDBConfFile(String filename) throws FatalException{
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setValidating(true);
		try{
			factory.newSAXParser().parse(new File(filename), this);
			if( this.coord_sys.equals("FK4") ) {
				this.astroframe = new FK4(this.coord_equi);
			}
			else if( this.coord_sys.equals("FK5") ) {
				this.astroframe = new FK5(this.coord_equi);
			}
			else if( this.coord_sys.equals("ICRS") ) {
				this.astroframe = new ICRS();
			}
			else if( this.coord_sys.equals("Galactic") ) {
				this.astroframe = new Galactic();
			}
			else if( this.coord_sys.equals("Ecliptic") ) {
				this.astroframe = new Ecliptic();;
			}
			else {
				FatalException.throwNewException(SaadaException.UNSUPPORTED_MODE, "Unsupported coordinate system <" + this.coord_sys + ">");
			}
		}catch(Exception e){
			FatalException.throwNewException(SaadaException.FILE_ACCESS, e);
		}
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public void startElement(String namespaceURI, String simpleName, String qualifiedName, Attributes attrs)throws SAXException{
		String elementName = simpleName;
		if(elementName.equals("")){
			elementName = qualifiedName;
		}
		/*
		 * Store to ditinguish "name" elements
		 */
		previous = str;
		str = elementName;

		if(elementName.equals("abscisse")){
			this.spect_type = attrs.getValue("type").trim();
			this.spect_unit = attrs.getValue("unit").trim();
		}
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#characters(char[], int, int)
	 */
	@Override
	public void characters (char buf [], int offset, int len)throws SAXException{
		String s = (new String(buf, offset, len)).trim();
		if(s.equals("")){
			//throw new SAXException(" value of " +str  +  " empty in file  configuration");
		}else if(str.equals("name")){
			if( previous.equals("database") ) {
				this.dbname = s.trim();				
			}
			else if( previous.equals("relational_database") ) {
				this.jdbc_dbname = s.trim();								
			}
			else if( previous.equals("administrator") ) {
				this.jdbc_administrator = s.trim();												
			}
			else if( previous.equals("reader") ) {
				this.jdbc_reader = s.trim();																
			}
		}else if(str.equals("password")){
			this.jdbc_reader_password = s.trim();
		}else if(str.equals("root_dir")){
			this.root_dir = s.trim();
		}else if(str.equals("repository_dir")){
			this.repository = s.trim();
		}else if(str.equals("jdbc_driver")){
			this.jdbc_driver = s.trim();
		}else if(str.equals("jdbc_url")){
			this.jdbc_url = s.trim();
		}else if(str.equals("webapp_home")){
			this.webapp_home = s.trim();
		}else if(str.equals("url_root")){
			this.url_root = s.trim();
		}else if(str.equals("description")){
			this.description = s.trim();
		}else if(str.equals("system")){
			this.coord_sys = s.trim();
		}else if(str.equals("equinox")){
			this.coord_equi = Double.parseDouble(s.trim().replaceAll("J", ""));
		}else if(str.equals("healpix_level")){
			this.healpix_level = Integer.parseInt(s.trim());
		}
	}

	/**
	 * @return Returns the astroframe.
	 */
	public Astroframe getAstroframe() {
		return astroframe;
	}

	/**
	 * @return Returns the admin_mode.
	 */
	public boolean isAdmin_mode() {
		return admin_mode;
	}

	/**
	 * @param admin_mode The admin_mode to set.
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public void setAdminAuth(String password) throws Exception {
		this.admin_mode = true;
		if( jdbc_connection != null ) {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Close reader connection");
			jdbc_connection.close();
		} else {
			Class.forName(this.jdbc_driver);			
		}
		Messenger.printMsg(Messenger.TRACE, "Switch DB connector in Administrator mode on " + this.jdbc_url);
		//Class.forName(this.jdbc_driver);
		jdbc_connection = getWrapper().getConnection(this.jdbc_url, this.jdbc_administrator, password);
		jdbc_connection.setAutoCommit(false);
	}

	/**
	 * @param admin_mode The admin_mode to set.
	 * @throws SQLException 
	 */
	public void setReaderMode(String password) throws Exception {
		this.admin_mode = false;
		if( jdbc_connection != null ) {
			jdbc_connection.close();
		}
		jdbc_connection = getWrapper().getConnection(this.jdbc_url, this.jdbc_reader, this.jdbc_reader_password);
		/*
		 * Needed to allow the interface to restart in case of query failure
		 */
		jdbc_connection.setAutoCommit(true);
	}

	/**
	 * @return Returns the wrapper.
	 * @throws ClassNotFoundException 
	 */
	public DbmsWrapper getWrapper() throws Exception {
		if( wrapper == null ) {
			if( this.jdbc_url.startsWith("jdbc:postgresql") ) {
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, this.jdbc_url + " takes PSQL wrapper");
				this.wrapper = PostgresWrapper.getWrapper(this.jdbc_driver, this.jdbc_url);
			}
			else if( this.jdbc_url.startsWith("jdbc:mysql") ) {
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, this.jdbc_url + " takes MYSQL wrapper");
				this.wrapper = MysqlWrapper.getWrapper(this.jdbc_driver, this.jdbc_url);
			}
			else if( this.jdbc_url.startsWith("jdbc:sqlite") ) {
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, this.jdbc_url + " takes SQLITE wrapper");
				this.wrapper = SQLiteWrapper.getWrapper(this.jdbc_driver, this.jdbc_url);
			}
			else {
				Messenger.printMsg(Messenger.ERROR, "No DBMS wrapper found for JDBC URL " + this.jdbc_url);
				FatalException.throwNewException(SaadaException.INTERNAL_ERROR, "No DBMS wrapper found for JDBC URL " + this.jdbc_url);
			}
		}
		return wrapper;
	}

	/**
	 * @return
	 * @throws SQLException
	 */
	public Statement getStatement() throws SQLException {
		return jdbc_connection.createStatement(wrapper.getDefaultScrollMode(), wrapper.getDefaultConcurentMode());
	}

}
