/**
 * 
 */
package saadadb.vo.query;

import java.util.Map;


/**
 * Request and subclasses handle totality  of the process, from the params to the
 * report delivery
 * At servlet level, we sould have something like
 * <code>
 * voRequest = new VORequest();
 * voRequest.init(request.getParams())
 * voRequest.runQuery();
 * voRequest.writeReport();
 * .. send rreport to the response
 * </code>
 * @author laurentmichel
 *@version $Id: VORequest.java 118 2012-01-06 14:33:51Z laurent.mistahl $
 */
public abstract class VORequest {
	final protected VOQuery voQuery;
	final protected String name;
	final protected String version;
	final protected String sessionID;
	final protected String reportDir;
	/**
	 * @param voQuery
	 * @param name
	 * @param version
	 * @param sessionID
	 */
	public VORequest(VOQuery voQuery, String name, String version,
			String sessionID, String reportDir) {
		super();
		this.voQuery = voQuery;
		this.name = name;
		this.version = version;
		this.sessionID = sessionID;
		this.reportDir = reportDir;
	}
	
	abstract void init(Map<String, String> params);
	
	abstract void runQuery();
	
	abstract String writeReport();
	

}
