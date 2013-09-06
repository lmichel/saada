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
import saadadb.vo.ADQLExecutor;
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
//		AdqlParser parse;
//		SaadaDBConsistency dbConsistency;
//		dbConsistency = new SaadaDBConsistency();
//		parse = new AdqlParser(new ByteArrayInputStream(queryStr.getBytes()), null, dbConsistency, new SaadaQueryBuilderTools((SaadaDBConsistency)dbConsistency));
//		SaadaADQLQuery query = (SaadaADQLQuery)parse.Query();
//		System.out.println(query.toSQL());
		//System.exit(1);
System.out.println("-----------------------------");
		//
//		AdqlQuery exec = new AdqlQuery();
//		exec.
//		exec.
		ADQLExecutor executor = new ADQLExecutor();		
		
		TAPToolBox.executeTAPQuery(queryStr, true, "votable", 10, "/home/michel/Desktop", "reportNameRoot.xml");
		System.exit(1);
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
