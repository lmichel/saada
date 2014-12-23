package ajaxservlet.tap_old;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ajaxservlet.SaadaServlet;
import saadadb.database.Database;
import saadadb.database.Repository;
import saadadb.vo.request.formator.QueryResultFormator;
import saadadb.vo.tap_old.TAPToolBox;
import saadadb.vo.tap_old.TAPToolBox.TAPParameters;


/**
 * Servlet which receives a query for the TAP service, executes it on the database and displays the result in the wanted format.<BR />
 * 
 * Queries can be sent in two different ways:
 * <ul>
 * 	<li>GET (HTTP GET ...): <i>only the result of the last query will be returned</i></li>
 * 	<li>POST (HTTP POST ...): <i>the given query is executed on the database and returned</i></li>
 * </ul><BR />
 * 
 * The query is associated with the QUERY parameters and must be expressed in two formats:
 * <ul>
 * 	<li>ADQL (LANG=ADQL)</li>
 * 	<li>SaadaQL (LANG=SaadaQL)</li>
 * </ul><BR /> 
 * 
 * The REQUEST parameter must be present and must be equal to 'doQuery'.<BR />
 * 
 * The format of the result may be specified and must be one of the following:
 * <ul>
 * 	<li>VOTABLE (FORMAT=votable or application/x-votable+xml or text/xml)</li>
 * 	<li>CSV (FORMAT=csv or text/csv)</li>
 * 	<li>TSV (FORMAT=tsv or text/tab-separated-values)</li>
 * 	<li>FITS (FORMAT=fits or application/fits)</li>
 * 	<li>Simple text (FORMAT=text or text/plain)</li>
 * 	<li>HTML (FORMAT=html or text/html)</li>
 * </ul><BR />
 * 
 * <i><b>EXAMPLE:</b>
 * <BR />
 * HTTP POST http://localhost:8080/mydb/tap/sync<BR />
 * REQUEST=doQuery<BR />
 * LANG=ADQL<BR />
 * FORMAT=votable<BR />
 * QUERY=SELECT oidsaada, namesaada FROM EPICSpectrum WHERE "_naxis" > 0
 * </i><BR /><BR />
 * 
 * @author Gr&eacute;gory Mantelet
 * @see SaadaServlet
 */
public class TAPSync extends SaadaServlet {

	private static final long serialVersionUID = 1L;
	
	public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		// TODO Une requête envoyée en GET ne doit pas être exécutée, mais l'ancien résultat (enregistré dans le cache) de la même requête doit être retourné !
		if (req.getParameterMap().size() > 0)
			doPost(req, res);
	}

	public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		printAccess(req, false);
		try{
			// Check and get parameters:
			TAPParameters param = new TAPToolBox.TAPParameters(req);
			
			// Capabilities case:
			if (param.request.equals("getCapabilities"))
				res.sendRedirect(Database.getUrl_root()+"/tap/capabilities");
			
			// Query case:
			else{
				// TODO For now the language version is ignored:
				if (param.lang.lastIndexOf('-') > -1)
					param.lang = param.lang.substring(0, param.lang.lastIndexOf('-'));
				
				// Check if the given language is supported:
				if (!param.lang.equals("SaadaQL") && !param.lang.equals("ADQL"))
					getErrorPage(req, res, "The query language \""+param.lang+"\" is not supported ! Supported formats are: \"ADQL\" and \"SaadaQL\".");
				
				// Execute the query and return a formatted result:
				String contentType = QueryResultFormator.getContentType(param.format);
				if (contentType != null){
					String reportDir =  Repository.getUserReportsPath(req.getSession().getId());
					String fileName = "TAPSync" + "_"+ (int)(Math.random() * 100000);
					fileName = TAPToolBox.executeTAPQuery(param.query, param.lang.equals("SaadaQL"), param.format, param.maxrec, reportDir, fileName);
					
					res.setContentType(contentType);
					this.downloadProduct(req, res,  fileName );

					
				}else
					getErrorPage(req, res, "The output format \""+param.format+"\" is not supported by this TAP service !");
			}
			
		}catch(Exception ex){
			ex.printStackTrace();
			getErrorPage(req, res, ex.getMessage());
			return;
		}
	}

	
}
