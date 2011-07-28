package saadadb.vo.request;

import java.util.Map;

import saadadb.exceptions.QueryException;
import saadadb.util.Messenger;
import saadadb.vo.request.query.VOQuery;

/**
 * Superclass of all parameter bases VO queries
 * @author laurent
 * @version 06/2011
 */
public abstract class VOParameterRequest  extends VORequest  {

	 public VOParameterRequest(VOQuery voquery, String protocol, String version, String sessionID, String reportDir) throws QueryException {
		 super(voquery, "CS", version, sessionID, reportDir);
	 }

	 @Override
	 public void init(Map<String, String> params) throws Exception {
		 this.voQuery.setParameters(params);
		 this.voQuery.buildQuery();
	 }

	 @Override
	 public void runQuery() throws Exception {
		 this.voQuery.runQuery();
		 this.oids = this.voQuery.getOids();
		 Messenger.printMsg(Messenger.TRACE, this.oids.size() + " oids found");
	 }

}
