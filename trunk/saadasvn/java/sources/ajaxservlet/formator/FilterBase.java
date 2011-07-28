package ajaxservlet.formator;

import java.io.*;
import java.util.*;


import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.QueryException;
import saadadb.meta.VOResource;
import saadadb.util.Messenger;

/**
 * 
 * @author Clémentine Frère
 * 
 * contact : frere.clementine[at]gmail.com
 *
 */

public class FilterBase {
	private static HashMap<String, StoredFilter> filters;
	String directory;
	private boolean loaded = false;

	
	/**
	 * builds a new filterbase which will 
	 * create the defaults filters files and
	 * add all the existing filters in the
	 * given folder using loadFilters
	 * 
	 * @param dir
	 * @param force
	 * @throws QueryException
	 * @throws FatalException
	 */
	public FilterBase(String dir, boolean force) throws QueryException, FatalException {
		FilterBase.filters = new HashMap<String, StoredFilter>();
		directory = dir;
		this.loadFilters(dir, force);
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
	 * @throws QueryException
	 * @throws FatalException
	 */
	private boolean loadFilters(String dir, boolean force) throws QueryException, FatalException {
		try {
			if (!loaded || force) {
				filters = new HashMap<String, StoredFilter>();
				FileReader fr;
				StoredFilter sf;
				String cat, coll;
				File tmp;

				File rootdir = new File(dir);
				File fdir = new File(dir);
				this.initDefaultsFilter();
				if( !fdir.exists() ) {
					fdir = new File(Database.getRepository() + Database.getSepar() + "config" + Database.getSepar());
					if( !fdir.exists() ) {
						Messenger.printMsg(Messenger.TRACE, "No filterbase found");
						return false;
					}
				}
				String path = rootdir.getAbsolutePath();
				String[] list = rootdir.list();

				if (list.length > 0) {
					for (int i = 0; i < list.length; i++) {
						String filename = list[i];
						if (filename.startsWith("df.") && filename.endsWith(".json")) {
							tmp = new File(path + Database.getSepar() + filename);
							fr = new FileReader(tmp);
							sf = new StoredFilter(fr);
							coll = sf.getFirstCollection();
							cat = sf.getCategory();
							String kw = coll+"."+cat;

							FilterBase.filters.put(kw, sf);
						}
					}
				}
				if (FilterBase.filters.size() > 0) {
					Messenger.printMsg(Messenger.TRACE, "Init FilterBase : done.");
					loaded = true;
				}
				return true;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return false;
	}

	
	/**
	 * provides the filter corresponding to the
	 * given category & collection (or the
	 * "Any-Collection" one if it doesn't exist)
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
			String defkeys = "Any-Collection."+cat;
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

		FilterBase.filters.put(kw, sf);
	}
	
	
	/** 
	 * creates the default filters 
	 * (first initialization)
	 * 
	 * @throws QueryException
	 * @throws FatalException
	 */
	private void initDefaultsFilter() throws QueryException, FatalException {
		try {
			File rootdir = new File(directory);
			String path = rootdir.getAbsolutePath();
			
			String cat = "ENTRY";
			File current = new File(path + Database.getSepar() + "df." + cat + ".json");
			FileWriter fw = new FileWriter(current);
			fw.write(DisplayFilterFactory.getDefaultJSON(cat));
			fw.close();
			FileReader fr = new FileReader(current);
			StoredFilter sf = new StoredFilter(fr);
			FilterBase.addFilter(sf);
			
			
			cat = "FLATFILE";
			current = new File(path + Database.getSepar() + "df." + cat + ".json");
			fw = new FileWriter(current);
			fw.write(DisplayFilterFactory.getDefaultJSON(cat));
			fw.close();
			fr = new FileReader(current);
			sf = new StoredFilter(fr);
			FilterBase.addFilter(sf);
			
			cat = "IMAGE";
			current = new File(path + Database.getSepar() + "df." + cat + ".json");
			fw = new FileWriter(current);
			fw.write(DisplayFilterFactory.getDefaultJSON(cat));
			fw.close();
			fr = new FileReader(current);
			sf = new StoredFilter(fr);
			FilterBase.addFilter(sf);
			
			cat = "MISC";
			current = new File(path + Database.getSepar() + "df." + cat + ".json");
			fw = new FileWriter(current);
			fw.write(DisplayFilterFactory.getDefaultJSON(cat));
			fw.close();
			fr = new FileReader(current);
			sf = new StoredFilter(fr);
			FilterBase.addFilter(sf);
			
			cat = "TABLE";
			current = new File(path + Database.getSepar() + "df." + cat + ".json");
			fw = new FileWriter(current);
			fw.write(DisplayFilterFactory.getDefaultJSON(cat));
			fw.close();
			fr = new FileReader(current);
			sf = new StoredFilter(fr);
			FilterBase.addFilter(sf);
			
			cat = "SPECTRUM";
			current = new File(path + Database.getSepar() + "df." + cat + ".json");
			fw = new FileWriter(current);
			fw.write(DisplayFilterFactory.getDefaultJSON(cat));
			fw.close();
			fr = new FileReader(current);
			sf = new StoredFilter(fr);
			FilterBase.addFilter(sf);
			
			
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

}
