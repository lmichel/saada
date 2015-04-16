package ajaxservlet.accounting;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;

import saadadb.compat.Files;
import saadadb.database.Repository;
import saadadb.util.Messenger;
import saadadb.util.WorkDirectory;
import ajaxservlet.formator.FilterBase;
import ajaxservlet.formator.FilterKeys;
import ajaxservlet.formator.StoredFilter;

/**
 * @author michel
 * @version $Id$
 */
public class UserAccount implements Serializable {
	private static final long serialVersionUID = 1L;
	protected final String                  sessionId;
	protected final String                  filterDirectory;
	protected QueryContext                  queryContext;
	protected HashMap<String, StoredFilter> userfilters;
	private String 							reportDir;
	public static final String              cartDirectory = "cartBuilder";
	
	UserAccount(String session_id) throws Exception {
		this.sessionId = session_id;
		this.filterDirectory = FilterBase.filterDirectory + File.separator + this.sessionId + File.separator ;
		this.userfilters = new HashMap<String, StoredFilter>();
		/*
		 * Init UWS quue of ZIP archives
		 */
		this.reportDir = Repository.getUserReportsPath(sessionId);;
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Create storage directory " + this.reportDir + " for session " + session_id);
		/*
		 * User report directory must not be deleted because it can be populated out of any session context
		 * e.g. a TAP request will put query report on the behalf of a session ID (sent by cookie) but the 
		 * TAP servlet does not create HTTP sessions.
		 */
		File f = new File(this.reportDir);
		if( !f.exists()) {
			(new File(this.reportDir)).mkdir();			
		}
		else if( !f.isDirectory() || ! f.canWrite() ) {
			Files.deleteFile(this.reportDir);
			(new File(this.reportDir)).mkdir();
		}
	}
	/**
	 * @return
	 */
	public String getReportDir() {
		return this.reportDir;
	}

	/**
	 * @return
	 */
	public String getCartDir() {
		return this.reportDir + File.separator + cartDirectory + File.separator;
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
	 * add the filter to the user filters base
	 * @param sf filter to be added
	 */
	public void addFilter(StoredFilter sf) throws Exception{
		String coll = sf.getFirstCollection();
		String cat = sf.getCategory();
		if (coll.compareTo(FilterKeys.ANY_COLLECTION) == 0) {
			this.resetCat(cat);
		}
		String kw = sf.getTreepath();
		userfilters.remove(kw);
		userfilters.put(kw, sf);
		/*
		 * Save the filter in the user base
		 */
		if( FilterBase.filterDirectory != null ) {
			WorkDirectory.validWorkingDirectory(this.filterDirectory );
			sf.store(this.filterDirectory + "df." + kw + ".json");
		} else {
			Messenger.printMsg(Messenger.WARNING, "Cannot store user filter in " + this.filterDirectory );
		}

	}


	/**
	 * get a filter from the user base given the collection and category
	 * -- if there's no specific filter for the parameter, looks
	 * for a category filter (FilterKeys.ANY_COLLECTION)
	 * -- if no filter is found, returns null
	 * @param coll
	 * @param cat
	 * @return a filter or null
	 */
	public StoredFilter getFilter(String coll, String cat, String saadaclass)  throws Exception{
		StoredFilter result = null;
		String keys = coll+"."+cat;
		/*
		 * returns the class level filter if is exists
		 */
		if(saadaclass != null && saadaclass.length() > 0 && !saadaclass.equals(FilterKeys.ANY_CLASS) && !saadaclass.equals("*")) {
			result = userfilters.get(keys + "." + saadaclass);			
			if( result != null ) {
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "filter " + keys + " found in user session");
				return result;
			}
		}
		/*
		 * Look for the collection level one otherwise
		 */
		result = userfilters.get(keys);
		if( result != null ) {				
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "filter " + keys + " found in user session");
		return result;
		}
		/*
		 * Take the default for classes
		 */
		String defkeys = FilterKeys.ANY_CLASS+ "." +cat;
		result = userfilters.get(defkeys);
		if( result != null ) {				
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "delault filter " + keys + " found in user session");
		return result;
		}
		/*
		 * Take the default one finally
		 */
		defkeys = FilterKeys.ANY_COLLECTION+ "." +cat;
		result = userfilters.get(defkeys);
		if( result != null ) {				
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "delault filter " + keys + " found in user session");
		return result;
		}
		return null;

	}


	/**
	 * deletes all the filters applying to the
	 * given category in the userbase
	 * @param category
	 */
	public void resetCat(String category) throws Exception {
		HashMap<String, StoredFilter> result = new HashMap<String, StoredFilter>();
		for (String key : userfilters.keySet()) {
			if (!key.endsWith(category)) {
				result.put(key, userfilters.get(key));
			} else {
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Reset filter " + key);
			}
		}
		userfilters = result;

		WorkDirectory.validWorkingDirectory(this.filterDirectory);
		File directory = new File (this.filterDirectory);
		String[] list = directory.list();
		for (int i = 0; i < list.length; i++) {
			System.out.println(list[i]);
			if (list[i].endsWith(category+".json")) {
				File entry = new File(directory, list[i]);
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Delet filter " + entry.getAbsolutePath());
				entry.delete();
			}
		}
	}


	/**
	 * deletes the filter applied to the given
	 * collection and category
	 * @param collection
	 * @param category
	 */
	public void resetFilter(String collection, String category)  throws Exception{
		String key = collection+"."+category;
		userfilters.remove(key);
		WorkDirectory.validWorkingDirectory(this.filterDirectory);
		File directory = new File (this.filterDirectory);
		String[] list = directory.list();
		for (int i = 0; i < list.length; i++) {
			if (list[i].endsWith(key + ".json")) {
				File entry = new File(directory, list[i]);
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Delete filter " + entry.getAbsolutePath());
				entry.delete();
			}
		}
	}


	/**
	 * resets the userfilter base and cleans
	 * the userfilters ..config/directory
	 */
	public void resetAll()  throws Exception{
		userfilters = new HashMap<String, StoredFilter>();
		WorkDirectory.validWorkingDirectory(this.filterDirectory);
		File directory = new File(this.filterDirectory);

		String[] list = directory.list();

		if (list != null) {
			for (int i = 0; i < list.length; i++) {
				File entry = new File(directory, list[i]);
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Delete filter " + entry.getAbsolutePath());
				entry.delete();
			}
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Delete filter directory " + directory.getAbsolutePath());
			directory.delete();
		}
	}

	/**
	 * 
	 */
	public void destroySession() throws Exception {				
		Messenger.printMsg(Messenger.TRACE, "Remove resources of session" + sessionId);
		resetAll();		
		Files.deleteFile(reportDir);
	}
}
