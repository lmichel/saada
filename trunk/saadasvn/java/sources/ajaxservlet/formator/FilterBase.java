package ajaxservlet.formator;

import java.io.File;
import java.io.FileReader;
import java.util.Date;
import java.util.HashMap;

import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.util.Messenger;
import saadadb.util.WorkDirectory;

/**
 * 
 * @author Clémentine Frère
 * 
 * contact : frere.clementine[at]gmail.com
 * * @version $Id$

 */

public class FilterBase {
	private static HashMap<String, StoredFilter> filters;
	public static final String filterDirectory = Database.getRepository() + Database.getSepar() + "config" + Database.getSepar() + "userfilters";
	private static boolean loaded = false;
	private static long lastInit = 0;
	public static final long INIT_PERIOD = 60;


	/**
	 * builds a new filterbase which will 
	 * create the defaults filters files and
	 * add all the existing filters in the
	 * given folder using loadFilters
	 * 
	 * @param dir
	 * @param force
	 * @throws Exception
	 */
	public static void init(boolean force) throws Exception {
		FilterBase.loadFilters(force);
	}


	/**
	 * create the defaults filters files and
	 * add all the existing filters in the
	 * given folder using loadFilters
	 * 
	 * force is used to reset the base
	 * @param dir
	 * @param force if true, resets the base
	 * @return
	 * @throws Exception
	 */
	private static boolean loadFilters(boolean force) throws Exception {
		long tc = (new Date()).getTime()/1000;
		if (!loaded || force || (tc -lastInit) > INIT_PERIOD ) {
			Messenger.printMsg(Messenger.TRACE, "Init filter base");
			lastInit = tc;
			filters = new HashMap<String, StoredFilter>();
			FileReader fr;
			StoredFilter sf;
			String cat, coll;
			File tmp;

			WorkDirectory.validWorkingDirectory(filterDirectory);
			File rootdir = new File(filterDirectory);
			FilterBase.initDefaultsFilter();
			String path = rootdir.getAbsolutePath();
			String[] list = rootdir.list();
			Messenger.printMsg(Messenger.TRACE, "Load filters from " + rootdir.getAbsolutePath());
			if (list != null && list.length > 0) {
				for (int i = 0; i < list.length; i++) {
					String filename = list[i];
					if (filename.startsWith("df.") && filename.endsWith(".json")) {
						Messenger.printMsg(Messenger.TRACE, "Load filter " + filename);
						tmp = new File(path + Database.getSepar() + filename);
						fr = new FileReader(tmp);
						sf = new StoredFilter(fr);
						FilterBase.filters.put(sf.getTreepath(), sf);
					}
				}
			} 
			if (FilterBase.filters.size() > 0) {
				Messenger.printMsg(Messenger.TRACE, "Init FilterBase : done.");
				loaded = true;
			}
			return true;
		}
		return false;
	}


	/**
	 * provides the filter corresponding to the
	 * given category & collection (or the
	 * FilterKeys.ANY_COLLECTION one if it doesn't exist)
	 * 
	 * @param coll
	 * @param cat
	 * @return
	 */
	public static StoredFilter get(String coll, String cat) {
		StoredFilter result = null;
		String keys = coll+"."+cat;
		result = filters.get(keys);

		if (result == null) {
			String defkeys = FilterKeys.ANY_COLLECTION + "." + cat;
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "No filter matching key " + keys + " found: take the default " + defkeys);
			result = filters.get(defkeys);
		}
		return result;
	}
	
	/**
	 * provides the filter corresponding to the
	 * given category, collection and class (or the
	 * FilterKeys.ANY_COLLECTION one if it doesn't exist)
	 * 
	 * @param coll
	 * @param cat
	 * @param saadaclass
	 * @return
	 */
	public static StoredFilter get(String coll, String cat, String saadaclass) {

		StoredFilter result = null;
		String keys = coll+"."+cat + "." + saadaclass;
		result = filters.get(keys);

		if (result == null) {
			String defkeys = FilterKeys.ANY_COLLECTION+cat;
			result = filters.get(defkeys);
		}
		return result;
	}



	/**
	 * add the given filter to the base
	 * @param sf
	 */
	public static void addFilter(StoredFilter sf) {
		String coll = sf.getFirstCollection();
		String cat = sf.getCategory();
		String kw = coll+"."+cat;
System.out.println("add " + kw);
		FilterBase.filters.put(kw, sf);
	}


	/** 
	 * creates the default filters 
	 * (first initialization)
	 * 
	 * @throws QueryException
	 * @throws FatalException
	 */
	private static void initDefaultsFilter() throws Exception {

		FilterBase.addFilter(new StoredFilter(getDefaultJSON("IMAGE")));
		FilterBase.addFilter(new StoredFilter(getDefaultJSON("ENTRY")));
		FilterBase.addFilter(new StoredFilter(getDefaultJSON("FLATFILE")));
		FilterBase.addFilter(new StoredFilter(getDefaultJSON("MISC")));
		FilterBase.addFilter(new StoredFilter(getDefaultJSON("TABLE")));
		FilterBase.addFilter(new StoredFilter(getDefaultJSON("SPECTRUM")));

	}

	/**
	 * provides the JSONString of the default
	 * filter from the given category
	 * 
	 * @param cat
	 * @return
	 * @throws QueryException
	 * @throws FatalException
	 */
	public static String getDefaultJSON(String cat) throws QueryException, FatalException {
		if ("TABLE".equals(cat)) {
			return new TableDisplayFilter(null).getRawJSON();
		} else if ("ENTRY".equals(cat)) {
			return new EntryDisplayFilter(null).getRawJSON();
		} else if ("IMAGE".equals(cat)) {
			return new ImageDisplayFilter(null).getRawJSON();
		} else if ("SPECTRUM".equals(cat)) {
			return new SpectrumDisplayFilter(null).getRawJSON();
		} else if ("MISC".equals(cat)) {
			return new MiscDisplayFilter(null).getRawJSON();
		} else if ("FLATFILE".equals(cat)) {
			return new FlatfileDisplayFilter(null).getRawJSON();
		} else {
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "Category does not exist");
		}
		return null;
	}

}
