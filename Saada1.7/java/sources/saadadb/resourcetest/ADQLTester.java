package saadadb.resourcetest;

import java.io.ByteArrayInputStream;

import adqlParser.SaadaADQLQuery;
import adqlParser.SaadaDBConsistency;
import adqlParser.SaadaQueryBuilderTools;
import adqlParser.parser.AdqlParser;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.database.Repository;
import saadadb.query.result.SaadaQLResultSet;
import saadadb.vo.request.query.AdqlQuery;
import saadadb.vo.tap.TAPToolBox;

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
		//for( int i=0 ; i<100 ; i++ )
		TAPToolBox.executeTAPQuery(queryStr, true, "votable", 2, "/tmp", "reportNameRoot.xml");
		Database.close();
		System.exit(1);

	}

}
