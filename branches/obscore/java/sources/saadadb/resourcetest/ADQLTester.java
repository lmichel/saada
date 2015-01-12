package saadadb.resourcetest;

import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.query.result.SaadaQLResultSet;
import saadadb.vo.tap_old.TAPToolBox;
import tap.ADQLExecutor;
import adqlParser.parser.AdqlParser;

public class ADQLTester {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		ArgsParser ap = new ArgsParser(args);
		Database.init(ap.getDBName());
		String queryStr = ap.getQuery();
		System.out.println(queryStr);
		AdqlParser parse;
//		SaadaDBConsistency dbConsistency;
//		dbConsistency = new SaadaDBConsistency();
//		parse = new AdqlParser(new ByteArrayInputStream(queryStr.getBytes()), null, dbConsistency, new SaadaQueryBuilderTools((SaadaDBConsistency)dbConsistency));
//		parse.enable_tracing();
//		SaadaADQLQuery query = (SaadaADQLQuery)parse.Query();
//
//		System.out.println("-----------------------------");
//
//		System.out.println(query.toSQL());
//System.out.println("-----------------------------");
		//
//		AdqlQuery exec = new AdqlQuery();
//		exec.
//		exec.
		
		TAPToolBox.executeTAPQuery(queryStr, true, "votable", -1, "/tmp", "reportNameRoot.xml");
		Database.close();
		System.exit(1);
		ADQLExecutor executor = new ADQLExecutor();		
		SaadaQLResultSet result = executor.execute(queryStr, -1);
		int cpt =0;
		while( result.next()) {
			if( (cpt % 10) == 0 )
			System.out.println(cpt );
			cpt++;
			if( cpt > 10000 ) 
				break;
		}
		System.out.println(cpt );

	}

}
