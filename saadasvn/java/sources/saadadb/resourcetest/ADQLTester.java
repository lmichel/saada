package saadadb.resourcetest;

import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.query.result.SaadaQLResultSet;
import saadadb.vo.ADQLExecutor;

public class ADQLTester {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		ArgsParser ap = new ArgsParser(args);
		Database.init(ap.getDBName());
		String query = ap.getQuery();
		ADQLExecutor executor = new ADQLExecutor();
		SaadaQLResultSet result = executor.execute(query, -1);
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
