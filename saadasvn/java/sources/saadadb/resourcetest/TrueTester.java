package saadadb.resourcetest;

import java.util.LinkedHashMap;
import java.util.Map;

import saadadb.database.Database;
import saadadb.exceptions.QueryException;
import saadadb.query.executor.Query;
import saadadb.util.Messenger;

public class TrueTester {
	static Map<Integer, String> SFI = new LinkedHashMap<Integer, String>();
	static Map<Integer, String> WAS = new LinkedHashMap<Integer, String>();
	static Map<Integer, String> ORDERBY = new LinkedHashMap<Integer, String>();
	static final int COLL=0;
	static final int CLASS=1;
	static final int EITHER=2;
	static final int BOTH=3;
	static final int ECLASS=4;
	static final int ECOLL=5;
	static final int EBOTH=6;
	static final int NOTHING=7;
	static {
		SFI.put(COLL,"Select ENTRY From * In Enhanced ");
		SFI.put(CLASS,"Select ENTRY From EnhancedEntry In Enhanced ");
		WAS.put(COLL,"WhereAttributeSaada { date_load IS NOT NULL} ");
		WAS.put(CLASS,"WhereAttributeSaada { _iauname IS NOT NULL} ");
		WAS.put(BOTH,"WhereAttributeSaada { date_load IS NOT NULL and _iauname IS NOT NULL} ");
		WAS.put(EITHER,"WhereAttributeSaada { oidsaada IS NOT NULL} ");
		WAS.put(ECOLL,"WhereAttributeSaada { oidsaada IS NOT NULL and date_load IS NOT NULL} ");
		WAS.put(ECLASS,"WhereAttributeSaada { oidsaada IS NOT NULL and _iauname IS NOT NULL} ");
		WAS.put(EBOTH,"WhereAttributeSaada { oidsaada IS NOT NULL and _iauname IS NOT NULL and date_load IS NOT NULL} ");
		WAS.put(NOTHING," ");
		ORDERBY.put(COLL,"Order By date_load ");
		ORDERBY.put(CLASS,"Order By _iauname ");
		ORDERBY.put(EITHER,"Order By oidsaada ");
	}

	public static String getQuery(Integer sfi, Integer was, Integer ob){
		System.out.println("======== " + sfi + " " + was + " " + ob + " " + SFI.get(sfi) + WAS.get(was) + ORDERBY.get(ob));
		return SFI.get(sfi) + WAS.get(was) + ORDERBY.get(ob);
	}
	public static void main(String[] args) throws QueryException {
		try {
		Database.init("ThreeXMMdr5");
		Query q = new Query();

		q.runBasicQuery(getQuery(COLL, NOTHING, COLL));
		q.runBasicQuery(getQuery(COLL, NOTHING, EITHER));
		q.runBasicQuery(getQuery(COLL, COLL, COLL));
		q.runBasicQuery(getQuery(COLL, COLL, EITHER));
		q.runBasicQuery(getQuery(COLL, EITHER, EITHER));
		q.runBasicQuery(getQuery(COLL, EITHER, COLL));
		
		q.runBasicQuery(getQuery(CLASS, EITHER, COLL));
		q.runBasicQuery(getQuery(CLASS, EITHER, CLASS));		
		q.runBasicQuery(getQuery(CLASS, EITHER, EITHER));

		q.runBasicQuery(getQuery(CLASS, COLL, COLL));
		q.runBasicQuery(getQuery(CLASS, COLL, CLASS));		
		q.runBasicQuery(getQuery(CLASS, COLL, EITHER));

		q.runBasicQuery(getQuery(CLASS, CLASS, COLL));
		q.runBasicQuery(getQuery(CLASS, CLASS, CLASS));		
		q.runBasicQuery(getQuery(CLASS, CLASS, EITHER));
		
		q.runBasicQuery(getQuery(CLASS, BOTH, COLL));
		q.runBasicQuery(getQuery(CLASS, BOTH, CLASS));		
		q.runBasicQuery(getQuery(CLASS, BOTH, EITHER));
		
		q.runBasicQuery(getQuery(CLASS, ECOLL, COLL));
		q.runBasicQuery(getQuery(CLASS, ECOLL, CLASS));		
		q.runBasicQuery(getQuery(CLASS, ECOLL, EITHER));

		q.runBasicQuery(getQuery(CLASS, ECLASS, COLL));
		q.runBasicQuery(getQuery(CLASS, ECLASS, CLASS));		
		q.runBasicQuery(getQuery(CLASS, ECLASS, EITHER));
		
		q.runBasicQuery(getQuery(CLASS, EBOTH, COLL));
		q.runBasicQuery(getQuery(CLASS, EBOTH, CLASS));		
		q.runBasicQuery(getQuery(CLASS, EBOTH, EITHER));


		//q.runBasicQuery("Select ENTRY From CatalogueEntry In CATALOGUE WhereRelation {matchPattern { CatToFitParam,AssObjClass{SpectrumFitEntry},AssObjAttSaada{ _a_fit =T}}} Limit 100");
		} catch (Exception e){
			
		} finally {
			Database.close();
		}
	}

}
