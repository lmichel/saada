package saadadb.resourcetest;

import saadadb.collection.Category;
import saadadb.collection.SaadaInstance;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.query.executor.Query;
import saadadb.query.result.SaadaInstanceResultSet;
import saadadb.util.Messenger;

/**
 * @author laurentmichel
 * * @version $Id$

 */
public class QueryAllColumnRunner {

	private static void usage() {
		Messenger.printMsg(Messenger.ERROR, "USAGE QueryAllColumnRunner -query=... DBNAME");
		System.exit(1);		
	}
	public static void main(String[] args) throws Exception {
		ArgsParser ap = new ArgsParser(args);
		Database.init(ap.getDBName());
		String query = ap.getQuery();
		if( query == null || query.length() == 0 ) {
			usage();
		}

		Query q = new Query (query);
		q.parse();
		SaadaInstance si = (SaadaInstance) Class.forName(
				"generated." + ap.getDBName() + "." + Category.explain(q.getSfiClause().getCatego()) + "UserColl")
				.newInstance();
		SaadaInstanceResultSet sirs = q.runAllColumnsQuery(si, query);
		int cpt=0;
		while(sirs.next()) {
			SaadaInstance sri = sirs.getInstance();
			System.out.println(cpt + ": " + sri.getOid() + " " + sri.getNameSaada());
			cpt++;
		}
	}
}
