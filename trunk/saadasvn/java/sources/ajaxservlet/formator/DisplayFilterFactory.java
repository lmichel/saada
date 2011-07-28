package ajaxservlet.formator;

import javax.servlet.http.HttpServletRequest;

import ajaxservlet.accounting.*;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.util.Messenger;

/**
 * 
 * @author Clémentine Frère
 * 
 * contact : frere.clementine[at]gmail.com
 *
 */

public class DisplayFilterFactory {

	public DisplayFilterFactory() {
	}

	
	
//	public static DisplayFilter getFilter(String coll, String cat) throws Exception {
//		System.out.println("\n\n1\n\n");
//		new FilterBase(Database.getRoot_dir() + Database.getSepar() + "config" + Database.getSepar());
//		DisplayFilter result = null;
//		StoredFilter sf = FilterBase.get(coll, cat);
//		if (sf != null) return new DynamicDisplayFilter(sf, coll);
//
//		if ("TABLE".equals(cat)) {
//			result = new TableDisplayFilter(coll);
//		} else if ("ENTRY".equals(cat)) {
//			result = new EntryDisplayFilter(coll);
//		} else if ("IMAGE".equals(cat)) {
//			result = new ImageDisplayFilter(coll);
//		} else if ("SPECTRUM".equals(cat)) {
//			result = new SpectrumDisplayFilter(coll);
//		} else if ("MISC".equals(cat)) {
//			result = new MiscDisplayFilter(coll);
//		} else if ("FLATFILE".equals(cat)) {
//			result = new FlatfileDisplayFilter(coll);
//		} else {
//			QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "Category does not exist");
//		}
//		return result;
//	}

	
	/**
	 * provides the right DisplayFilter given the
	 * collection, the category and an Http request
	 * 
	 * First looks up the userbase, then the filterbase
	 * 
	 * @param coll
	 * @param cat
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public static DisplayFilter getFilter(String coll, String cat, HttpServletRequest request) throws Exception {
		UserAccount useracc = UserTrap.getUserAccount(request);
		StoredFilter sf = useracc.getFilter(coll, cat);
		if (sf != null) {
			return new DynamicDisplayFilter(sf, coll);
		}
		
		
		new FilterBase(Database.getRoot_dir() + Database.getSepar() + "config" + Database.getSepar(), false);
		DisplayFilter result = null;
		sf = FilterBase.get(coll, cat);
		if (sf != null) return new DynamicDisplayFilter(sf, coll);

		if ("TABLE".equals(cat)) {
			result = new TableDisplayFilter(coll);
		} else if ("ENTRY".equals(cat)) {
			result = new EntryDisplayFilter(coll);
		} else if ("IMAGE".equals(cat)) {
			result = new ImageDisplayFilter(coll);
		} else if ("SPECTRUM".equals(cat)) {
			result = new SpectrumDisplayFilter(coll);
		} else if ("MISC".equals(cat)) {
			result = new MiscDisplayFilter(coll);
		} else if ("FLATFILE".equals(cat)) {
			result = new FlatfileDisplayFilter(coll);
		} else {
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "Category does not exist");
		}
		return result;
	}

	
	/**
	 * provides the right StoredFilter given the
	 * collection, the category and an Http request
	 * 
	 * First looks up the userbase, then the filterbase
	 * 
	 * @param coll
	 * @param cat
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public static StoredFilter getStoredFilter(String coll, String cat, HttpServletRequest request) throws Exception {
		UserAccount useracc = UserTrap.getUserAccount(request);
		StoredFilter sf = useracc.getFilter(coll, cat);
		if (sf != null) return sf;
		
		new FilterBase(Database.getRoot_dir() + Database.getSepar() + "config" + Database.getSepar(), false);
		DisplayFilter result = null;
		sf = FilterBase.get(coll, cat);
		if (sf != null) return sf;
		
		return null;
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
			return new TableDisplayFilter(null).getJSONString();
		} else if ("ENTRY".equals(cat)) {
			return new EntryDisplayFilter(null).getJSONString();
		} else if ("IMAGE".equals(cat)) {
			return new ImageDisplayFilter(null).getJSONString();
		} else if ("SPECTRUM".equals(cat)) {
			return new SpectrumDisplayFilter(null).getJSONString();
		} else if ("MISC".equals(cat)) {
			return new MiscDisplayFilter(null).getJSONString();
		} else if ("FLATFILE".equals(cat)) {
			return new FlatfileDisplayFilter(null).getJSONString();
		} else {
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "Category does not exist");
		}
		return null;
	}
}
