package ajaxservlet;



/*
 * 11/2010: Look for the db name in dbname.txt if it exist
 *          Creation of the static variable base_dir: allows open file instead of opening URL with the URL_ROOT
 */
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
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
import saadadb.database.spooler.Spooler;
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
 * 06/2012 Connect to a log file
 *
 */
public class SaadaServlet extends HttpServlet {
	public static final boolean JSON_FILE_MODE = false;
	private static final long serialVersionUID = 1L;
	public static String base_dir ;
	public static boolean secureDownlad = false;

	@Override
	public void init(ServletConfig conf) throws ServletException {
		Messenger.setServletMode();
		super.init(conf);
		base_dir = conf.getServletContext().getRealPath("") + Database.getSepar();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.GenericServlet#destroy()
	 */
	@Override
	public void destroy() {
		Messenger.printMsg(Messenger.TRACE, "Close connection");
		try {
			Spooler.getSpooler().close();
		} catch (Exception e) {
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
			JsonUtils.teePrint(response, JsonUtils.getErrorMsg(accessMessage(request) + " " +msg));
		} catch (Exception e1) {
			Messenger.printStackTrace(e1);
		}
	}

	/**
	 * @param req
	 */
	public void printAccess(HttpServletRequest request, boolean force) {
		if( force || Messenger.debug_mode == true) {
			String full_url = request.getRequestURL().toString();
			String queryString = request.getQueryString();   
			if (queryString != null) {
				full_url += "?"+queryString;
			}		
			Messenger.printMsg(Messenger.TRACE, accessMessage(request) );
		}
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
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Download file " + product_path);
		if( product_path == null ){
			getErrorPage(req, res, "Can't download NULL file");
			return;			
		}
		File f = new File(product_path);
		if( !f.exists() || !f.isFile() ) {
			getErrorPage(req, res, "File " + f.getAbsolutePath() + " does not exist or cannot be read");
			return;
		}
		downloadProduct(req, res, product_path,f.getName());
	}

	/**
	 * @param req
	 * @param res
	 * @param product_path
	 * @param attachement
	 * @param proposedFilename
	 * @throws Exception
	 */
	protected void downloadProduct(HttpServletRequest req, HttpServletResponse res, String product_path,  String proposedFilename) throws Exception{
		String contentType = getServletContext().getMimeType(product_path);
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Download file " + product_path);
		File f = new File(product_path);
		if( !f.exists() || !f.isFile() ) {
			reportJsonError(req, res, "File " + f.getAbsolutePath() + " does not exist or is not a file");
			return;
		}
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
		/*
		 * Put the default filename to lower case to help tools using filename suffix  to identify
		 * their types
		 */
		String fileName = ( proposedFilename != null && proposedFilename.length() > 0 )? proposedFilename
				: f.getName().toLowerCase();

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
		} else if( s_product.toLowerCase().endsWith(".txt") || s_product.toLowerCase().endsWith(".text")   || s_product.toLowerCase().endsWith(".ascii")) {
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
		System.out.println(s_product + " " + res.getContentType());
		res.setHeader("Content-Disposition", "inline; filename=\""+ fileName + "\"");
		res.setHeader("Content-Length"     , Long.toString(f.length()));
		res.setHeader("Last-Modified"      , (new Date(f.lastModified())).toString());
		res.setHeader("Pragma", "no-cache" );
		res.setHeader("Cache-Control", "no-cache" );
		res.setDateHeader( "Expires", 0 );		

		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "GetProduct file " + product_path + " (type: " + res.getContentType() + ")" + contentType);

		BufferedInputStream fl = new BufferedInputStream(new FileInputStream(product_path));
		byte b[] = new byte[1000000];
		int len = 0;
		BufferedOutputStream bos = new BufferedOutputStream(res.getOutputStream());
		while ((len = fl.read(b)) != -1) {
			bos.write(b, 0, len);
		}				
		bos.flush();
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
		File f = new File(urlPath);
		response.setContentType("text/xml");
		if( attachName != null && attachName.length() > 0 ) {
			response.setHeader("Content-Disposition", "inline; filename=\"" + attachName + "\"");
		}
		response.setHeader("Content-Length"     , Long.toString(f.length()));
		response.setHeader("Last-Modified"      , (new Date(f.lastModified())).toString());
		response.setHeader("Pragma", "no-cache" );
		response.setHeader("Cache-Control", "no-cache" );
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "dump resource " + urlPath);
		BufferedInputStream fl = new BufferedInputStream(new FileInputStream(f));
		byte b[] = new byte[1000000];
		int len = 0;
		BufferedOutputStream bos = new BufferedOutputStream(response.getOutputStream());
		while ((len = fl.read(b)) != -1) {
			bos.write(b, 0, len);
		}				
		bos.flush();
		fl.close();
	}

	/**
	 * Write an XML string with a proper http header
	 * @param buffer
	 * @param response
	 * @param attachName
	 * @throws IOException
	 */
	protected void dumpXmlString(StringBuffer buffer, HttpServletResponse response, String attachName) throws IOException {
		response.setContentType("text/xml");
		if( attachName != null && attachName.length() > 0 ) {
			response.setHeader("Content-Disposition", "inline; filename=\"" + attachName + "\"");
		}
		response.setHeader("Content-Length"     , Long.toString(buffer.length()));
		response.setHeader("Last-Modified"      , (new Date()).toString());
		response.setHeader("Pragma", "no-cache" );
		response.setHeader("Cache-Control", "no-cache" );
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "dump String resource "+ buffer.length() + " bytes");
		PrintWriter out = response.getWriter();
		out.print(buffer.toString());
		out.flush();
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

}
