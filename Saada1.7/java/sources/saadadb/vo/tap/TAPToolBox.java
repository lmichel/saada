package saadadb.vo.tap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import saadadb.cache.CacheManager;
import saadadb.collection.SaadaInstance;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.query.result.SaadaQLResultSet;
import saadadb.util.Messenger;
import saadadb.vo.ADQLExecutor;
import saadadb.vo.SaadaQLExecutor;
import saadadb.vo.formator.TapToJsonFormator;
import saadadb.vo.formator.TapToVOTableFormator;
import saadadb.vo.request.TapAdqlRequest;
import uws.job.AbstractJob;

/**
 * This class gathers some useful functions to build a TAP service.<BR />
 * <b><u>Example:</u></b><br />
 * public class ATapServlet extends HttpServlet {<br />
 * 		public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {<br />
 * 			// Get the http request parameters:<br />
 * 			TAPParameters params = TAPToolBox.getParamsMap(req);<br />
 * <br />
 * 			// Set the appropriate content type:<br />
 * 			res.setContentType(TAPToolBox.getContentType(params.format));<br />
 * <br />
 * 			// Execute the query and format the result in the output of this servlet:<br />
 * 			TAPToolBox.executeTAPQuery(params.query, false, params.format, res.getOutputStream());<br />
 * 			// OR Execute the query and format the result in a file:<br />
 * 			FileOutputStream resultFile = new FileOutputStream(new File("result"+TAPToolBox.getFormatExtension(params.format)));<br />
 * 			TAPToolBox.executeTAPQuery(params.query, false, params.format, resultFile);<br />
 * 		}<br />
 * }<br />
 * 
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 04/2010
 * @version 07/2011 getContentType and getFormat moved to VOResultformator
 */
public class TAPToolBox {

	/**
	 * This class describes all defined parameters of a TAP request.
	 * 
	 * @author Gr&eacute;gory Mantelet (CDS)
	 * @version 04/2010
	 */
	public static class TAPParameters {
		public String request = null;
		public String version = "1.0";
		public String lang = null;
		public String query = null;
		public String format = "votable";
		public int maxrec = -1;
		public String runid = null;
		public String upload = null;
		public Map<String,String> otherParameters = null;

		public TAPParameters(Map<String,String> params) throws Exception {
			otherParameters = new HashMap<String,String>();

			// Extract and identify each pair (key,value):
			for(String key : params.keySet())
				setTAPParameter(key, params.get(key));

			// Check parameters:
			checkTAPParameters();
		}

		public TAPParameters(HttpServletRequest req) throws Exception {
			otherParameters = new HashMap<String,String>();

			// Extract and identify each pair (key,value):
			Map<?,?> params = req.getParameterMap();
			for(Object key : params.keySet()){
				String name = key.toString(), value = ((String[])params.get(key))[0];
				setTAPParameter(name, value);
			}

			// Check parameters:
			checkTAPParameters();
		}

		public TAPParameters(AbstractJob job) throws Exception {
			otherParameters = new HashMap<String,String>();

			// Extract and identify each pair (key,value):
			for(String key : job.getAdditionalParameters()){
				String value = job.getAdditionalParameterValue(key);
				setTAPParameter(key, value);
			}

			// Check parameters:
			checkTAPParameters();
		}

		protected void setTAPParameter(String key, String value) throws Exception {
			// REQUEST case:
			if (key.equalsIgnoreCase("request")){
				if (!value.equals("doQuery") && !value.equals("getCapabilities"))
					throw new Exception("The parameter \"REQUEST\" must be equal to \"doQuery\" or \"getCapabilities\" (now: REQUEST=\""+request+"\" ! (note: the parameter name is not case sensitive instead of the parameter value which must have exactly the same case)");
				else
					request = value;
				// LANG case:
			}else if (key.equalsIgnoreCase("lang"))
				lang = value;
			// QUERY case:
			else if (key.equalsIgnoreCase("query"))
				query = value;
			// VERSION case:
			else if (key.equalsIgnoreCase("version"))
				version = value;
			// FORMAT case:
			else if (key.equalsIgnoreCase("format"))
				format = value;
			// MAXREC case:
			else if (key.equalsIgnoreCase("maxrec")){
				try{
					maxrec = Integer.parseInt(value);
				}catch(NumberFormatException nfe){
					throw new Exception("The \"MAXREC\" parameter must be a numeric value (now: MAXREC="+value+") !");
				}
				// RUNID case:
			}else if (key.equalsIgnoreCase("runid"))
				runid = value;
			// UPLOAD case:
			else if (key.equalsIgnoreCase("upload"))
				upload = value;
			// OTHER case:
			else
				otherParameters.put(key, value);
		}

		protected void checkTAPParameters() throws Exception {
			// Check that required parameters are not NON-NULL:
			if (request == null)
				throw new Exception("The parameter \"REQUEST\" must be provided and its value must be equal to \"doQuery\" or \"getCapabilities\" !");
			if (request.equals("doQuery") && lang == null)
				throw new Exception("The parameter \"LANG\" must be provided if REQUEST=doQuery !");
			if (request.equals("doQuery") && query == null)
				throw new Exception("The parameter \"QUERY\" must be provided if REQUEST=doQuery !");

			// Set default value for some parameters if needed:
			if (format == null)
				format = "votable";
			if (maxrec < 0)
				maxrec = -1;
		}

		/**
		 * Builds a TAPParameters object only with the three required TAP parameters.
		 * @param reqMethod			The REQUEST value.
		 * @param queryLanguage		The LANG value.
		 * @param queryExp			The QUERY value.
		 */
		public TAPParameters(String reqMethod, String queryLanguage, String queryExp) throws Exception {
			request = reqMethod;
			lang = queryLanguage;
			query = queryExp;
			otherParameters = new HashMap<String,String>();
			checkTAPParameters();
		}
	}


	/**
	 * Executes the given query and format its result in function of the given format.
	 * Uses the new VO request framework.
	 * Do not support XSL transformations: Make no sense in AJAX context
	 * 
	 * @param query			The query to execute (ADQL or SaadaQL).
	 * @param saadaQL		Indicates whether the given query is in SaadaQL else it is supposed to be in ADQL.
	 * @param format		The result format (VOTABLE (default), HTML, CSV, TSV).
	 * @param limit		    Result size limit.
	 * @param outputDir  	Output directory.
	 * @param outputFile	Output file name.
	 * @return              the full path of the report file
	 * @throws IOException
	 * @throws Exception
	 */
	public static String executeTAPQuery(String query, boolean saadaQL, String format, int limit, String outputDir, String reportFile) throws IOException, Exception{
		LinkedHashMap<String, String> pmap = new LinkedHashMap<String, String>();
		pmap.put("query", query);
		pmap.put("limit", Integer.toString(limit));
		// VOTABLE case:
		if (format.equalsIgnoreCase("votable") || format.equalsIgnoreCase("text/xml") || format.equalsIgnoreCase("application/x-votable+xml")){
			TapAdqlRequest request = new TapAdqlRequest("NoSession", outputDir);
			request.addFormator("votable");
			request.setResponseFilePath(reportFile);
			request.processRequest(pmap);
			return  request.getResponseFilePath()[0];

		} else if (format.equalsIgnoreCase("json") || format.equalsIgnoreCase("application/json")){
			TapAdqlRequest request = new TapAdqlRequest("NoSession", outputDir);
			request.addFormator("json");
			request.setResponseFilePath(reportFile);
			request.processRequest(pmap);
			return  request.getResponseFilePath()[0];
			// Other cases:
		}else{
			QueryException.throwNewException(SaadaException.UNSUPPORTED_MODE, "Format " + format + " not supported");
			return null;
			}			
		}
		
		/**
		 * Format the response corresponding to the given list of oid in the specified text format (for now only: CSV and TSV).
		 * 
		 * @param oids				A list of oid.
		 * @param columns			Columns which must be valued in the formatted result.
		 * @param columnSeparator	The string that must separate each column value.
		 * @param output			The stream in which the formatted result must be written.
		 * 
		 * @throws Exception		<ul><li>An IOException if it's impossible to get a valid Writer object from the given HttpServletResponse.</li>
		 * 							<li>A FatalException if an oid isn't associated with a SaadaInstance object.</li>
		 * 							<li>An Exception if a column doesn't exist or if it's impossible to get its value.</li></ul>
		 */
		public static void formatInText(long[] oids, String[] columns, String columnSeparator, OutputStream output) throws Exception {		
			CacheManager cache = Database.getCache();

			// Write a line for each oid:
			for (long oid : oids){
				// get the corresponding instance from the cache:
				SaadaInstance obj = cache.getObject(oid);
				if (obj == null)
					throw new Exception("No data are found for the oid \""+oid+"\" !");
				// write the value of all selected columns with the appropriate separation character:
				int i = 0;
				for(String column : columns){
					if (i++ > 0) output.write(columnSeparator.getBytes());
					try{
						Object value = obj.getFieldValue(column);
						output.write(((value == null)?"":value.toString()).getBytes());
					}catch(NoSuchFieldException ex){;}
				}
				output.write("\n".getBytes());
			}
		}

		/**
		 * Format the given result in the specified text format (for now only: CSV and TSV).
		 * 
		 * @param rs				The results to format in text.
		 * @param columnSeparator	The string that must separate each column value.
		 * @param output			The stream in which the formatted result must be written.
		 * 
		 * @throws Exception		<ul><li>An IOException if it's impossible to get a valid Writer object from the given HttpServletResponse.</li>
		 * 							<li>A FatalException if an oid isn't associated with a SaadaInstance object.</li>
		 * 							<li>An Exception if a column doesn't exist or if it's impossible to get its value.</li></ul>
		 */
		public static void formatInText(SaadaQLResultSet rs, String columnSeparator, OutputStream output) throws Exception {			
			int nbColumns = rs.getMeta().size();

			while(rs.next()){
				for(int i=1; i<=nbColumns; i++){
					if (i > 1) output.write(columnSeparator.getBytes());
					try {
						Object value = rs.getObject(i);
						output.write(((value==null)?"":value.toString()).getBytes());
					}catch(SQLException ex){ex.printStackTrace();}
				}
				output.write("\n".getBytes());
			}
		}

		/**
		 * Transforms the given XML file (xmlSrcPath) into the given HTML file (htmlDestPath)
		 * thanks to the given XSLT file (xsltPath).
		 * 
		 * @param xmlSrcPath				The path of the XML file to transform in HTML.
		 * @param htmlDestPath				The path of the file which will be the HTML version of the given XML file.
		 * @param xsltPath					The path of the XSLT file to use to do the transformation.
		 * @throws FileNotFoundException	If it's impossible to create a stream from one of the three given path.
		 * @throws TransformerException		If there is a problem during the transformation.
		 */
		public static final void transformXMLToHTML(String xmlSrcPath, String htmlDestPath, String xsltPath) throws FileNotFoundException, TransformerException{
			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transformer = tFactory.newTransformer(new StreamSource(xsltPath));
			transformer.transform(new StreamSource(xmlSrcPath), new StreamResult(new FileOutputStream(htmlDestPath)));
		}

		/**
		 * Transforms the given XML file (xmlSrcPath) in HTML into the given stream (htmlDest)
		 * thanks to the given XSLT file (xsltPath).
		 * 
		 * @param xmlSrcPath				The path of the XML file to transform in HTML.
		 * @param htmlDest					The stream in which the HTML version of the given XML file must be sent.
		 * @param xsltPath					The path of the XSLT file to use to do the transformation.
		 * @throws FileNotFoundException	If it's impossible to create a stream from one of the three given path.
		 * @throws TransformerException		If there is a problem during the transformation.
		 */
		public static final void transformXMLToHTML(String xmlSrcPath, OutputStream htmlDest, String xsltPath) throws FileNotFoundException, TransformerException{
			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transformer = tFactory.newTransformer(new StreamSource(xsltPath));
			transformer.transform(new StreamSource(xmlSrcPath), new StreamResult(htmlDest));
		}
		
		/**
		 * @param qString
		 * @return
		 * @throws FatalException
		 */
		public static final String setBooleanInContain(String qString) throws FatalException{
			/*
			 * As ADQL requires 0 or 1 as value returned by the the CONTAIN operator and SaadaSQL procedures used there return
			 * true or false with DBMS implementing boolean, the ADQL query is modified to replace 0 or 1 with the good operands.
			 * Not sure to ever work!
			 */
			Pattern p = Pattern.compile("(?i)(?:(?:(CONTAINS\\s*\\([^=]+\\))\\s*([^\\s]+)\\s*([10])))", Pattern.DOTALL);
			Matcher m = p.matcher(qString);
			while(m.find()  ){
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Replace 1/0 operands for CONTAINS operator with apropriate boolean values");
				String opd = m.group(3);
				if( opd.equals("1") ) {
					opd = Database.getWrapper().getBooleanAsString(true);
					if( "true".equals(opd )) { opd = "'" + opd + "'";}
				}
				else {
					opd = Database.getWrapper().getBooleanAsString(false);
					if( "false".equals(opd )) { opd = "'" + opd + "'";}
				}
				qString = qString.replace(m.group(0), m.group(1) + " " +  m.group(2)+ " " +  opd);
			}
			return qString;

		}
		

	}
