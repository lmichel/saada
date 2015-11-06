package saadadb.resourcetest;

import saadadb.database.Database;
import saadadb.exceptions.QueryException;
import saadadb.query.executor.Query;

public class TrueTester {

	public static void main(String[] args) throws QueryException {
		Database.init("ThreeXMMdr5");
		Query q = new Query();
		q.runBasicQuery("Select ENTRY From EnhancedEntry In Enhanced WhereAttributeSaada { namesaada = '3XMM J000537.7+000248'} Limit 1");
		
		q.runBasicQuery("Select ENTRY From CatalogueEntry In CATALOGUE WhereRelation {matchPattern { CatToFitParam,AssObjClass{SpectrumFitEntry},AssObjAttSaada{ _a_fit =T}}} Limit 100");
		Database.close();
	}

}
