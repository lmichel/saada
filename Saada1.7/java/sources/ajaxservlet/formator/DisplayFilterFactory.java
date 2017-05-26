package ajaxservlet.formator;

import javax.servlet.http.HttpServletRequest;

import saadadb.exceptions.FatalException;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.util.Messenger;
import ajaxservlet.accounting.UserAccount;
import ajaxservlet.accounting.UserTrap;

/**
 *  * @version $Id$

 * @author Clémentine Frère
 * 
 * contact : frere.clementine[at]gmail.com
 * 
 * 02/2014: UserAccounts as getter parameters instead of HTTRequest
 *
 */

public class DisplayFilterFactory {

	public DisplayFilterFactory() {
	}

	
	/**
	 * provides the right DisplayFilter given the
	 * collection, the category and an Http request
	 * Must not be used within a servlet where the session is just created.
	 * That can duplicate the session .
	 * Use rather {@link #getFilter(String, String, String, UserAccount) }
     *
	 * @param coll
	 * @param cat
	 * @param saadaclass
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public static DisplayFilter getFilter(String coll, String cat, String saadaclass, HttpServletRequest request) throws Exception {
		 return getFilter(coll, cat, saadaclass, UserTrap.getUserAccount(request));
	}
	
	
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
	public static DisplayFilter getFilter(String coll, String cat, String saadaclass, UserAccount useracc) throws Exception {
		/*
		 * Look first for a user defined filter
		 */
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "look for the filter in the user account");
		StoredFilter sf = useracc.getFilter(coll, cat, saadaclass);
		if (sf != null) {
			return new DynamicClassDisplayFilter(sf, coll, saadaclass);
		}
		/*
		 * Look then for a global filter
		 */
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Not found, take the global one");
		FilterBase.init(false);
		sf = FilterBase.getVisibleColumnsFilter(coll, cat);
		if (sf != null) {
			return new DynamicClassDisplayFilter(sf, coll, saadaclass);
		}
		
		FatalException.throwNewException(SaadaException.MISSING_RESOURCE, "Filter base not initalized for " + coll + "." + cat );
		return null;
	}

	/**
	 * Provides the right StoredFilter given the
	 * collection, the category and an Http request
	 * 
	 * First looks up the userbase, then the filterbase
	 * Must not be used within a servlet where the session is just created.
	 * That can duplicate the session .
	 * Use rather {@link #getStoredFilter(String, String, String, UserAccount) }
	 * @param coll
	 * @param cat
	 * @param saadaclass
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public static DisplayFilter getStoredFilter(String coll, String cat, String saadaclass, HttpServletRequest request) throws Exception {
		 return getFilter(coll, cat, saadaclass, UserTrap.getUserAccount(request));
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
	public static StoredFilter getStoredFilter(String coll, String cat, String saadaclass, UserAccount useracc) throws Exception {
		FilterBase.init(false);
		StoredFilter sf = useracc.getFilter(coll, cat, saadaclass);
		if (sf != null) {
			return sf;
		}
		return  FilterBase.getVisibleColumnsFilter(coll, cat);
	}
	
	/**
	 * {@link FilterBase} wrapper: similar to FilterBase.getDefaultJSON
	 * @param cat
	 * @return
	 * @throws QueryException
	 * @throws FatalException
	 */
	public static String getDefaultJSON(String cat) throws QueryException, FatalException {
			return FilterBase.getVisibleJSONDisplayFilter(cat);
	}
}
