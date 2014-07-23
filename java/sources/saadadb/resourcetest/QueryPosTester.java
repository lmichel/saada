package saadadb.resourcetest;

import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.exceptions.QueryException;
import saadadb.query.executor.Query;
import saadadb.query.result.SaadaQLResultSet;


/**
 * @author michel
 * This class is use to test queries on position. Both position and search radius are given to the creator.
 * A votable is the built with one source for each node of a grid with 1 arcmin as step and covering 1.5 time
 * the search radius. This VOtable is ingested into the database and a the query centered on that grid is run.
 * IN addition with the position the Votable entries have a column with the distance to the grid center making then easier
 * the control of the object selected by the query
 * The grid is built by moving a position by 1arcmin steps. That is not efficient, but this feature was expected to be used 
 * on others contexts.
*
 */
public class QueryPosTester extends GridBuilder{

	/**
	 * @param pos
	 * @param size
	 * @throws QueryException
	 */
	QueryPosTester(String pos, int size) throws Exception {
		super(pos, size, "PosTester");
	}


	
	private void testQuery() throws Exception {
		Query q = new Query();
		SaadaQLResultSet srs = q.runQuery("Select ENTRY From " + classe + "Entry In " + collection + "\nWhereAttributeClass{_checkpos >= 0} \n WherePosition{isInCircle(\"" + target  + "\"," + size + ",J2000,FK5)}\nOrder By _checkpos");
		int cpt = 0;
		while( srs.next()) {
			System.out.print(Database.getCache().getObject(srs.getOid()).getObs_id()+ " ");
			if( (cpt%4) == 0 ) {
				System.out.println("");
			}
			cpt++;
		}
		System.out.println("");
	}
	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args)  {
		try {
			ArgsParser ap = new ArgsParser(args);
			Database.init(ap.getDBName());
			Database.setAdminMode(ap.getPassword());
			String target = args[0];
			int size = Integer.parseInt(args[1]);
			QueryPosTester qpt = new QueryPosTester(target, size);
			qpt.testQuery();
		} catch(Exception e) {
			e.printStackTrace();
			System.out.println("USAGE: java PosQueryTester position size [-password=dbpassword] DBName");
			System.out.println("  - position: target center (numerical or object name)");
			System.out.println("  - size    : search radius in arcminutes");
			Database.close();
			System.exit(1);
		}
		Database.close();

	}
}
