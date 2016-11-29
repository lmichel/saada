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
	/**
	 * Map of the filters defining the columns to be displayed 
	 * This filters can be set in REP/config/userfilters
	 */
	private static HashMap<String, StoredFilter> visiblesFilters;
	/**
	 * Map of the filters defining the columns possibly displayed
	 * Those filter are hard-coded in  the CATEGORYDisplayFilter classes 
	 */
	private static HashMap<String, StoredFilter> columnsFilters;
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
	 * Create the filter maps
	 * Both maps are set with default values hard-coded in the CATEGORYDysplayFilter classes
	 * visible filters (filters defining visible columns) can be overridden with filters read out
	 * from the repository
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
			visiblesFilters = new HashMap<String, StoredFilter>();
			columnsFilters = new HashMap<String, StoredFilter>();
			FileReader fr;
			StoredFilter sf;
			File tmp;
			/*
			 * Init both column and visible filters with the default filters hard-coded
			 * in the CATEGORYDysplayFilter classes
			 */
			FilterBase.initVisibleFilters();
			FilterBase.initColumnFilters();
			/*
			 * Read visible filters with those read into the repository.
			 * Visible filters define the columns to be displayed
			 */
			WorkDirectory.validWorkingDirectory(filterDirectory);
			File rootdir = new File(filterDirectory);
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
						System.out.println("@@@@@@@@@@@@@@@@ loading " + filename + " " + sf.getTreepath());
						FilterBase.visiblesFilters.put(sf.getTreepath(), sf);
					}
				}
			} 
			if (FilterBase.visiblesFilters.size() > 0) {
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
	public static StoredFilter getVisibleColumnsFilter(String coll, String cat) {
		StoredFilter result = null;
		String keys = coll+"."+cat;
		result = visiblesFilters.get(keys);

		if (result == null) {
			String defkeys = FilterKeys.ANY_COLLECTION + "." + cat;
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "No filter matching key " + keys + " found: take the default " + defkeys);
			result = visiblesFilters.get(defkeys);
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
	public static StoredFilter getVisibleColumnsFilter(String coll, String cat, String saadaclass) {

		StoredFilter result = null;
		String keys = coll+"."+cat + "." + saadaclass;
		result = visiblesFilters.get(keys);

		if (result == null) {
			String defkeys = FilterKeys.ANY_COLLECTION + "." + cat;
			result = visiblesFilters.get(defkeys);
		}
		return result;
	}

	/**
	 * provides the displayable filter corresponding to the
	 * given category & collection (or the
	 * FilterKeys.ANY_COLLECTION one if it doesn't exist)
	 * 
	 * @param coll
	 * @param cat
	 * @return
	 */
	public static StoredFilter getColumnFilter(String coll, String cat) {
		StoredFilter result = null;
		String keys = coll+"."+cat;
		result = columnsFilters.get(keys);

		if (result == null) {
			String defkeys = FilterKeys.ANY_COLLECTION + "." + cat;
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "No filter matching key " + keys + " found: take the default " + defkeys);
			result = columnsFilters.get(defkeys);
		}
		return result;
	}
	
	/**
	 * provides the displayable filter corresponding to the
	 * given category, collection and class (or the
	 * FilterKeys.ANY_COLLECTION one if it doesn't exist)
	 * 
	 * @param coll
	 * @param cat
	 * @param saadaclass
	 * @return
	 */
	public static StoredFilter getColumnFilter(String coll, String cat, String saadaclass) {

		StoredFilter result = null;
		String keys = coll+"."+cat + "." + saadaclass;
		System.out.println("@@@@@@@@@@@@@ getColumnFilter 1 " + keys);
		result = columnsFilters.get(keys);

		if (result == null) {
			String defkeys = FilterKeys.ANY_COLLECTION + "." + cat;
			System.out.println("@@@@@@@@@@@@@ getColumnFilter " + defkeys);
			result = columnsFilters.get(defkeys);
		}
		return result;
	}



	/**
	 * add the given filter to the base
	 * @param sf
	 */
	public static void addVisibleDisplayFilter(StoredFilter sf) {
		String coll = sf.getFirstCollection();
		String cat = sf.getCategory();
		String kw = coll+"."+cat;
		FilterBase.visiblesFilters.put(kw, sf);
	}

	/**
	 * add the given filter for displayable columns to the base
	 * @param sf
	 */
	public static void addColumnFilter(StoredFilter sf) {
		String coll = sf.getFirstCollection();
		String cat = sf.getCategory();
		String kw = coll+"."+cat;
		FilterBase.columnsFilters.put(kw, sf);
	}


	/** 
	 * creates the default display filters 
	 * (first initialization)
	 * 
	 * @throws QueryException
	 * @throws FatalException
	 */
	private static void initVisibleFilters() throws Exception {

		FilterBase.addVisibleDisplayFilter(new StoredFilter(getVisibleJSONDisplayFilter("IMAGE")));
		FilterBase.addVisibleDisplayFilter(new StoredFilter(getVisibleJSONDisplayFilter("ENTRY")));
		FilterBase.addVisibleDisplayFilter(new StoredFilter(getVisibleJSONDisplayFilter("FLATFILE")));
		FilterBase.addVisibleDisplayFilter(new StoredFilter(getVisibleJSONDisplayFilter("MISC")));
		FilterBase.addVisibleDisplayFilter(new StoredFilter(getVisibleJSONDisplayFilter("TABLE")));
		FilterBase.addVisibleDisplayFilter(new StoredFilter(getVisibleJSONDisplayFilter("SPECTRUM")));

	}
	/**
	 * @throws Exception
	 */
	private static void initColumnFilters() throws Exception {

		FilterBase.addColumnFilter(new StoredFilter(getJSONDisplayFilter("IMAGE")));
		FilterBase.addColumnFilter(new StoredFilter(getJSONDisplayFilter("ENTRY")));
		FilterBase.addColumnFilter(new StoredFilter(getJSONDisplayFilter("FLATFILE")));
		FilterBase.addColumnFilter(new StoredFilter(getJSONDisplayFilter("MISC")));
		FilterBase.addColumnFilter(new StoredFilter(getJSONDisplayFilter("TABLE")));
		FilterBase.addColumnFilter(new StoredFilter(getJSONDisplayFilter("SPECTRUM")));

	}

	/**
	 * provides the JSON String of the default
	 * filter from the given category
	 * @param cat
	 * @return
	 * @throws QueryException
	 * @throws FatalException
	 */
	public static String getVisibleJSONDisplayFilter(String cat) throws QueryException, FatalException {
		if ("TABLE".equals(cat)) {
			return new TableDisplayFilter(null).getVisibleJSONDisplayFilter();
		} else if ("ENTRY".equals(cat)) {
			return new EntryDisplayFilter(null).getVisibleJSONDisplayFilter();
		} else if ("IMAGE".equals(cat)) {
			return new ImageDisplayFilter(null).getVisibleJSONDisplayFilter();
		} else if ("SPECTRUM".equals(cat)) {
			return new SpectrumDisplayFilter(null).getVisibleJSONDisplayFilter();
		} else if ("MISC".equals(cat)) {
			return new MiscDisplayFilter(null).getVisibleJSONDisplayFilter();
		} else if ("FLATFILE".equals(cat)) {
			return new FlatfileDisplayFilter(null).getVisibleJSONDisplayFilter();
		} else {
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "Category does not exist");
		}
		return null;
	}

	/**
	 * provides the JSON String of the default displayable
	 * filter from the given category
	 * @param cat
	 * @return
	 * @throws QueryException
	 * @throws FatalException
	 */
	public static String getJSONDisplayFilter(String cat) throws QueryException, FatalException {
		if ("TABLE".equals(cat)) {
			return new TableDisplayFilter(null).getJSONDisplayFilter();
		} else if ("ENTRY".equals(cat)) {
			return new EntryDisplayFilter(null).getJSONDisplayFilter();
		} else if ("IMAGE".equals(cat)) {
			return new ImageDisplayFilter(null).getJSONDisplayFilter();
		} else if ("SPECTRUM".equals(cat)) {
			return new SpectrumDisplayFilter(null).getJSONDisplayFilter();
		} else if ("MISC".equals(cat)) {
			return new MiscDisplayFilter(null).getJSONDisplayFilter();
		} else if ("FLATFILE".equals(cat)) {
			return new FlatfileDisplayFilter(null).getJSONDisplayFilter();
		} else {
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "Category does not exist");
		}
		return null;
	}

}
