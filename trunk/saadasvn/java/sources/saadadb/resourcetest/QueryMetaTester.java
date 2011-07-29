package saadadb.resourcetest;

import saadadb.collection.SaadaInstance;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.query.executor.Query;
import saadadb.query.result.SaadaQLResultSet;

/**
 * @author laurentmichel
 * * @version $Id$

 */
public class QueryMetaTester {

	public static void main(String[] args)  {
		ArgsParser ap;
		try {
			ap = new ArgsParser(args);
			Database.init(ap.getDBName());
			if( ap.getRelation() != null ) {
				Query q = new Query();
				SaadaQLResultSet srs =  q.runQuery("Select ENTRY From * In StartingGrid WhereRelation{ matchPattern { " + ap.getRelation() + " }}");
				int prim=0 , sec=0;
				while(srs.next()) {
					prim++;
					SaadaInstance si = Database.getCache().getObject(srs.getOid());
					long[] cps = si.getCounterparts(ap.getRelation());
					double ref = new Double(si.getFieldValue("_checkpos").toString());
					for( long cp: cps ) {
						SaadaInstance cpi = Database.getCache().getObject(cp);
						sec++;
					}
				}
				System.out.println(Database.getCachemeta().getRelation(ap.getRelation()));
				
			}
		} catch (Exception e1) {
			e1.printStackTrace();
			System.out.println("USAGE: java QueryMetaRelation -relation=relation_name SAADADB_NAME");
			System.exit(1);
		}
	}
}
