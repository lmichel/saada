package ajaxservlet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import saadadb.database.Database;
import saadadb.database.Repository;
import saadadb.database.spooler.Spooler;
import saadadb.util.Messenger;
import saadadb.util.RegExp;


/**
 * Listener invoked by Tomcat at starting time and at shutdown time.
 * It is declared in web.xml
 * @author michel
 * 01/2014: close JDBC connection when the context is destroyed
 * 04/2015: unregister drivers when the context is destroyed
 */
public class InitBaseAtStart  implements ServletContextListener , HttpSessionListener{
	private String base_dir, app_dir;
	ServletContext servletContext;

	public void contextDestroyed(ServletContextEvent event) {
		Messenger.printMsg(Messenger.TRACE, "ByeBye");
		try {
			/*
			 * This method is also called at starting time
			 */
			if( Database.getConnector() != null) {
				Spooler.getSpooler().close();
				Enumeration<Driver> drivers = DriverManager.getDrivers();
				while(drivers.hasMoreElements()) {
					Driver d = drivers.nextElement();
					Messenger.printMsg(Messenger.TRACE, "Driver " + d + " unregistered");   
					DriverManager.deregisterDriver(d);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void contextInitialized(ServletContextEvent event) {
		try {
			servletContext = event.getServletContext();
			base_dir = servletContext.getRealPath("") + Database.getSepar();
			app_dir = servletContext.getContextPath().replaceAll("/", "");
			Messenger.printMsg(Messenger.TRACE, "base_dir: " + base_dir);
			Messenger.printMsg(Messenger.TRACE, "app_dir: " + app_dir);
			Messenger.debug_mode = false;
			LocalConfig lc = new LocalConfig();
			Database.init(lc.db_name);

			if( lc.urlroot != null && lc.urlroot.length() > 0 ){
				Database.getConnector().setUrl_root(lc.urlroot);
			}
			if( lc.saadadbroot != null && lc.saadadbroot.length() > 0 ){
				Database.getConnector().setRoot_dir(lc.saadadbroot);
			}
			Repository.sweepReportDir();
			Messenger.printMsg(Messenger.TRACE, "Init done");


		} catch (Exception e) {
			Messenger.printMsg(Messenger.ERROR, "Can't init the node base : " + e.getMessage());
		}
	}

	public void sessionCreated(HttpSessionEvent event) {
	}
	public void sessionDestroyed(HttpSessionEvent event) {
		Messenger.printMsg(Messenger.TRACE, "Session " + event.getSession().getId() + " destroyed");    
	}


	/**
	 * Look at the file dbname.txt located at application root.
	 * Take the DBname, the url_root and the debug mode from this file.
	 * That is used to have multiple webapps dealing with the same DB (debig mode e.g.)
	 * @author laurent
	 *
	 */
	class LocalConfig{
		private String db_name = null;
		private String urlroot = null;
		private String saadadbroot = null;

		LocalConfig()  throws Exception{
			File f = new File(base_dir + "dbname.txt");
			if( f.exists() ) {
				Messenger.printMsg(Messenger.TRACE, "file dbname.txt found at webapp root");
				BufferedReader fr = new BufferedReader(new FileReader(f));
				String buff;
				while( (buff = fr.readLine()) != null ) {
					if( buff.trim().startsWith("#") ) {
						continue;
					}
					else if( buff.matches("saadadbname=" + RegExp.DBNAME)) {
						String retour =  buff.trim().split("=")[1];
						Messenger.printMsg(Messenger.TRACE, "read DB name: " + retour);
						db_name = retour;
					}
					else if( buff.matches("urlroot=.*")) {
						String retour =  buff.trim().split("=")[1];
						Messenger.printMsg(Messenger.TRACE, "readURLRoot: " + retour);
						this.urlroot = retour;
						Database.setUrl_root(this.urlroot);
					}
					else if( buff.matches("logfile=.*")) {
						String retour =  buff.trim().split("=")[1];
						if( "none".equalsIgnoreCase(retour) ) {
							return;
						} else if( "default".equals(retour) ) {
							retour = System.getProperty("catalina.home") + File.separator + "logs" + File.separator + app_dir + ".log";
						} else if( !retour.startsWith(File.separator) ) {
							retour = System.getProperty("catalina.home") + File.separator + "logs" + File.separator + retour;
						}
						Messenger.printMsg(Messenger.TRACE, "set log file: " + retour);
						Messenger.init(retour);
					}
					else if( buff.matches("saadadbroot=.*")) {
						String retour =  buff.trim().split("=")[1];
						Messenger.printMsg(Messenger.TRACE, "saadadbroot: " + retour);
						this.saadadbroot = retour;
					}
					else if( "securedownload=true".equalsIgnoreCase(buff.trim())) {
						Messenger.printMsg(Messenger.TRACE, "Set download product in secure mode");
						SaadaServlet.secureDownlad = true;
					}
					else if( "debug=on".equalsIgnoreCase(buff.trim())) {
						Messenger.debug_mode = true;
					}
				}
				if( db_name.length() > 0 ) {
					return;
				}

			}
			Messenger.printMsg(Messenger.TRACE, "Take webapp root name as DB name");
			db_name = (new File(servletContext.getRealPath("/"))).getName();
		}

	}
}



