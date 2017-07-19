package saadadb.resourcetest;

import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.database.spooler.Spooler;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.QueryException;
import saadadb.query.executor.Query;
import saadadb.query.result.SaadaQLResultSet;

public class PosListWithError {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		ArgsParser ap = new ArgsParser(args);
		try {
		Database.init(ap.getDBName());
		Query q = new Query();
		SaadaQLResultSet srs = q.runQuery("Select ENTRY From MergedEntry In MERGEDCATALOGUE \n WherePosition{isInCircle(\"poslist:/home/michel/Desktop/list.pos\",1.0 ,J2000,FK5)}\n");
		} catch(Exception e){
			e.printStackTrace();
		} finally {
			Spooler.getSpooler().close();
		}
	}

}
