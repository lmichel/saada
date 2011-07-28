package ajaxservlet.accounting;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;

import saadadb.compat.Files;
import saadadb.database.Database;
import saadadb.database.Repository;
import saadadb.util.Messenger;
import ajaxservlet.formator.StoredFilter;

/**
 * @author michel
 * @version $Id$
 */
public class UserAccount implements Serializable {
	private static final long serialVersionUID = 1L;
	protected String                        sessionId;
	protected QueryContext                  queryContext;
	protected HashMap<String, StoredFilter> userfilters;
	private String 							reportDir;
	
	UserAccount(String session_id) throws Exception {
		this.sessionId = session_id;
		userfilters = new HashMap<String, StoredFilter>();
		/*
		 * Init UWS quue of ZIP archives
		 */
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Create UWS ZIP archive queue for session " + session_id);
		reportDir = Repository.getUserReportsPath(sessionId);;
		Files.deleteFile(reportDir);
		(new File(reportDir)).mkdir();
	}
	/**
	 * @return
	 */
	public String getReportDir() {
		return reportDir;
	}

	/**
	 * @param queryContext
	 */
	public void setQueryContext(QueryContext queryContext) {
		this.queryContext = queryContext;
	}

	/**
	 * @return
	 */
	public QueryContext getQueryContext() {
		return queryContext;
	}
	
	/**
	 * @return
	 */
	public String getSessionID() {
		return sessionId;
	}
	
	
	/**
	 * add the filter to the userfilters base
	 * @param sf filter to be added
	 */
	public void addFilter(StoredFilter sf) {
		String coll = sf.getFirstCollection();
		String cat = sf.getCategory();
		if (coll.compareTo("Any-Collection") == 0) {
			this.resetCat(cat);
		}
		String kw = coll+"."+cat;

		userfilters.remove(kw);
		userfilters.put(kw, sf);
	}
	
	
	/**
	 * get a filter from the user base given the collection and category
	 * -- if there's no specific filter for the parameter, looks
	 * for a category filter ("Any-Collection")
	 * -- if no filter is found, returns null
	 * @param coll
	 * @param cat
	 * @return a filter or null
	 */
	public StoredFilter getFilter(String coll, String cat) {
		
		StoredFilter result = null;
		String keys = coll+"."+cat;
		result = userfilters.get(keys);
		
		if (result == null) {
			String defkeys = "Any-Collection."+cat;
			result = userfilters.get(defkeys);
		}
		
		return result;
	}
	
	
	/**
	 * deletes all the filters applying to the
	 * given category in the userbase
	 * @param cat
	 */
	public void resetCat(String cat) {
		HashMap<String, StoredFilter> result = new HashMap<String, StoredFilter>();
		for (String key : userfilters.keySet()) {
			if (!key.endsWith(cat)) {
				result.put(key, userfilters.get(key));
			}
		}
		userfilters = result;

		String dir  = Database.getRoot_dir() + Database.getSepar() + "config" + Database.getSepar() + "userfilters";
		File directory = new File (dir);
		String[] list = directory.list();
		for (int i = 0; i < list.length; i++) {
			if (list[i].endsWith(cat+".json")) {
				File entry = new File(directory, list[i]);
				entry.delete();
			}
		}
	}
	
	
	/**
	 * deletes the filter applied to the given
	 * collection and category
	 * @param coll
	 * @param cat
	 */
	public void resetFilter(String coll, String cat) {
		String key = coll+"."+cat;
		userfilters.remove(key);
		String dir  = Database.getRoot_dir() + Database.getSepar() + "config" + Database.getSepar() + "userfilters";
		File directory = new File (dir);
		String[] list = directory.list();
		for (int i = 0; i < list.length; i++) {
			if (list[i].endsWith(coll+"_"+cat+".json")) {
				File entry = new File(directory, list[i]);
				entry.delete();
			}
		}
	}
	
	
	/**
	 * resets the userfilter base and cleans
	 * the userfilters ..config/directory
	 */
	public void resetAll() {
		userfilters = new HashMap<String, StoredFilter>();
		String dir  = Database.getRoot_dir() + Database.getSepar() + "config" + Database.getSepar() + "userfilters";
		File directory = new File(dir);
		
		String[] list = directory.list();
		
		if (list != null) {
		   for (int i = 0; i < list.length; i++) {
		      File entry = new File(directory, list[i]);
		      entry.delete();
		   }
		   directory.delete();
		}
	}
	
	/**
	 * 
	 */
	public void destroySession() {				
		Messenger.printMsg(Messenger.TRACE, "Remove resources of session" + sessionId);
		resetAll();		
		Files.deleteFile(reportDir);

	}
}
