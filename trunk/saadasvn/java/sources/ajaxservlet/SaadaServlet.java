package ajaxservlet;



/*
 * 11/2010: Look for the db name in dbname.txt if it exist
 *          Creation of the static variable base_dir: allows open file instead of opening URL with the URL_ROOT
 */
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import saadadb.database.Database;
import saadadb.database.Repository;
import saadadb.exceptions.FatalException;
import saadadb.util.Messenger;
import saadadb.util.RegExp;
import ajaxservlet.formator.DefaultPreviews;
import ajaxservlet.json.JsonUtils;

/**
 * Make sure to SaadaDB connection to be open
 * @author michel
 * @version $Id$
 *
 * 07/2011 add method dumpXmlFile
 *
 */
public class SaadaServlet extends HttpServlet {
	private static boolean INIT = false;
	private static boolean INIT_IN_PROGRESS = false;
	public static final boolean JSON_FILE_MODE = false;
	public static String base_dir;
	private static final long serialVersionUID = 1L;


	public static boolean isInit(){
		return INIT;
	}
	@Override
	public void init(ServletConfig conf) throws ServletException {
		super.init(conf);
		base_dir = conf.getServletContext().getRealPath("") + Database.getSepar();
		if( !JSON_FILE_MODE ) {
			try {
				int cpt = 0;
				while( INIT_IN_PROGRESS && cpt < 10) {
					Thread.sleep(500);
					Messenger.printMsg(Messenger.TRACE, conf.getServletName() + " is waiting init to be done");
					cpt ++;
				}
				synchronized (this) {
					if(  !INIT  ) {
						if( !saadaservlet.SaadaServlet.isInit() ) {
							INIT_IN_PROGRESS = true;					
							Messenger.printMsg(Messenger.TRACE, "Ajax interface init started by" + conf.getServletName());
							Messenger.debug_mode = false;
							LocalConfig lc = new LocalConfig();
							Database.init(lc.db_name);
							if( lc.urlroot != null && lc.urlroot.length() > 0 ){
								Database.getConnector().setUrl_root(lc.urlroot);
							}
							if( lc.saadadbroot != null && lc.saadadbroot.length() > 0 ){
								Database.getConnector().setRoot_dir(lc.saadadbroot);
							}
							INIT_IN_PROGRESS = false;
							INIT = true;
							Repository.sweepReportDir();
							Messenger.printMsg(Messenger.TRACE, "Ajax interface init done by "+  conf.getServletName());
						}
						else {
							INIT_IN_PROGRESS = false;
							INIT = true;
							Messenger.printMsg(Messenger.TRACE, "Ajax interface done by saadaservlet.SaadaServlet ");

						}
					}
					/*
					 * Compulsory to restart after a failure
					 */
					Database.get_connection().setAutoCommit(true);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.GenericServlet#destroy()
	 */
	@Override
	public void destroy() {
		Messenger.printMsg(Messenger.TRACE, "Close connection");
		try {
			Database.getConnector().getJDBCConnection().close();
		} catch (FatalException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		super.destroy();
	}

	/**
	 * @param req
	 * @param res
	 * @param account
	 * @param e
	 */
	public void getErrorPage(HttpServletRequest req, HttpServletResponse res, Exception e) {
		try {
			res.getOutputStream().println(DefaultPreviews.getErrorDiv(e));
		} catch (Exception e1) {
			Messenger.printStackTrace(e1);
		}
		Messenger.printStackTrace(e);
	}

	/**
	 * @param req
	 * @param res
	 * @param msg
	 */
	public void getErrorPage(HttpServletRequest req, HttpServletResponse res, String msg) {
		try {
			Messenger.printMsg(Messenger.ERROR, msg);			
			res.setContentType("text/html");
			res.getOutputStream().println(DefaultPreviews.getErrorDiv(msg));
		} catch (Exception e1) {
			Messenger.printStackTrace(e1);
		}
	}

	/**
	 * @param request
	 * @param response
	 * @param e
	 */
	public void reportJsonError(HttpServletRequest request, HttpServletResponse response, Exception  e) {
		Messenger.printStackTrace(e);
		reportJsonError(request, response, e.toString());
	}

	/**
	 * @param request
	 * @param response
	 * @param msg
	 */
	public void reportJsonError(HttpServletRequest request, HttpServletResponse response, String msg) {
		try {
			JsonUtils.teePrint(response.getOutputStream(), JsonUtils.getErrorMsg(accessMessage(request) + " " +msg));
		} catch (Exception e1) {
			Messenger.printStackTrace(e1);
		}
	}

	/**
	 * @param req
	 */
	public void printAccess(HttpServletRequest request, boolean force) {
		String full_url = request.getRequestURL().toString();
		String queryString = request.getQueryString();   
		if (queryString != null) {
			full_url += "?"+queryString;
		}		
		Messenger.printMsg(Messenger.TRACE, accessMessage(request) );
	}

	/**
	 * @param req
	 * @return
	 */
	public String accessMessage(HttpServletRequest req) {
		String full_url = req.getRequestURL().toString();
		String queryString = req.getQueryString();   
		if (queryString != null) {
			full_url += "?"+queryString;
		}		
		return req.getMethod() + " from " + req.getRemoteAddr() + ": " + full_url  + " " + req.getSession().getId();
	}

	/**
	 * @param req
	 * @param res
	 * @param product_path
	 * @throws Exception
	 */
	protected void downloadProduct(HttpServletRequest req, HttpServletResponse res, String product_path ) throws Exception{
		String contentType = getServletContext().getMimeType(product_path);
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Download file " + product_path);
		File f = new File(product_path);
		if( !f.exists() || !f.isFile() ) {
			getErrorPage(req, res, "File " + f.getAbsolutePath() + " does not exist or cannot be read");
			return;
		}
		String name_f =f.getName();;
		String s_product = product_path;
		if( product_path.toLowerCase().endsWith(".gz") ) {
			res.setHeader("Content-Encoding", "gzip");
			s_product = product_path.replaceAll("(?i)(\\.gz$)", "");
		}
		else if( product_path.toLowerCase().endsWith(".gzip") ) {
			res.setHeader("Content-Encoding", "gzip");
			s_product = product_path.replaceAll("(?i)(\\.gzip$)", "");
		}
		else if( product_path.toLowerCase().endsWith(".zip") ) {
			res.setHeader("Content-Encoding", "zip");
			s_product = product_path.replaceAll("(?i)(\\.zip$)", "");
		}

		if( s_product.toLowerCase().endsWith(".htm") || s_product.toLowerCase().endsWith(".html") ) {
			res.setContentType("text/html;charset=ISO-8859-1");
		} else if( s_product.toLowerCase().endsWith(".pdf")  ) {
			res.setContentType("application/pdf");
		} else if( s_product.toLowerCase().endsWith(".png")  ) {
			res.setContentType("image/png");
		} else if( s_product.toLowerCase().endsWith(".jpeg") || s_product.toLowerCase().endsWith(".jpg")) {
			res.setContentType("image/jpeg");
		} else if( s_product.toLowerCase().endsWith(".gif") ) {
			res.setContentType("image/gif");
		} else if( s_product.toLowerCase().endsWith(".txt")  ) {
			res.setContentType("text/plain");
		} else if( s_product.matches(RegExp.FITS_FILE)) {
			res.setContentType( "application/fits");                                                
		} else if( s_product.matches(RegExp.VOTABLE_FILE)) {
			res.setContentType("application/x-votable+xml");                                                
		} else if( s_product.toLowerCase().endsWith(".xml")  ) {
			res.setContentType("text/xml");
		} else if (contentType != null) {
			res.setContentType(contentType);
		} else  {
			res.setContentType("application/octet-stream");
		}

		res.setHeader("Content-Disposition", "attachment; filename=\""+ name_f + "\"");
		res.setHeader("Content-Length"     , Long.toString(f.length()));
		res.setHeader("Last-Modified"      , (new Date(f.lastModified())).toString());
		Messenger.printMsg(Messenger.DEBUG, "GetProduct file " + product_path + " (type: " + res.getContentType() + ")" + contentType);

		BufferedInputStream fl = new BufferedInputStream(new FileInputStream(product_path));
		byte b[] = new byte[1000000];
		int len = 0;
		BufferedOutputStream bos = new BufferedOutputStream(res.getOutputStream());
		while ((len = fl.read(b)) != -1) {
			bos.write(b, 0, len);
		}				
		bos.flush();
		bos.close();
		fl.close();
	}

	/**
	 * Push the content of an XML file to the servlet response
	 * @param urlPath     : Pathn name of the xml file
	 * @param response    : http response
	 * @param attachName  :filename proposed by by the browser download tool
	 * @throws IOException
	 */
	protected void dumpXmlFile(String urlPath, HttpServletResponse response, String attachName) throws IOException {
		response.setContentType("text/xml");
		response.setHeader("Content-Disposition", "attachment; filename=\"" + attachName + "\"");
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "dump resource " + urlPath);
		Scanner s = new Scanner(new FileInputStream(urlPath));
		PrintWriter out = response.getWriter();
		try {
			while (s.hasNextLine()){
				String l = s.nextLine();
				out.println(l);
			}
		}
		finally{
			s.close();
			out.close();
		}
	}


	/**
	 * Returns a <Strin, String> map of the request parameters. Only the first value is taken for each parameter
	 * @param req
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected Map<String, String> getFlatParameterMap (HttpServletRequest req) {
		LinkedHashMap<String, String>	retour = new LinkedHashMap<String, String>();
		Map<String, String[]> op = req.getParameterMap();
		for( String key: op.keySet() ) {
			retour.put(key, op.get(key)[0]);
		}
		return retour;
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
				String dbname = "";
				while( (buff = fr.readLine()) != null ) {
					if( buff.trim().startsWith("#") ) {
						continue;
					}
					else if( buff.matches("saadadbname=" + RegExp.DBNAME)) {
						String retour =  buff.trim().split("=")[1];
						Messenger.printMsg(Messenger.TRACE, "read DB name: " + retour);
						dbname = retour;
					}
					else if( buff.matches("urlroot=.*")) {
						String retour =  buff.trim().split("=")[1];
						Messenger.printMsg(Messenger.TRACE, "readURLRoot: " + retour);
						this.urlroot = retour;
					}
					else if( buff.matches("saadadbroot=.*")) {
						String retour =  buff.trim().split("=")[1];
						Messenger.printMsg(Messenger.TRACE, "saadadbroot: " + retour);
						this.saadadbroot = retour;
					}
					else if( "debug=on".equalsIgnoreCase(buff)) {
						Messenger.debug_mode = true;
					}
				}
				if( dbname.length() > 0 ) {
					db_name = dbname;
					return;
				}

			}
			Messenger.printMsg(Messenger.TRACE, "Take webapp root name as DB name");
			db_name = (new File(getServletContext().getRealPath("/"))).getName();
		}

	}
}
