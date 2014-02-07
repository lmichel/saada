package saadadb.resourcetest;

import java.sql.ResultSet;
import java.sql.Statement;

import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.database.spooler.DatabaseConnection;
import saadadb.database.spooler.Spooler;
import saadadb.vo.tap.TAPToolBox;


public class ADQLFormatorTester {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		ArgsParser ap = new ArgsParser(args);
		Database.init(ap.getDBName());
		DatabaseConnection connection = Database.getConnection();
		Statement stm =connection.getStatement();
		ResultSet rs = stm.executeQuery("pragma cache_size");
		while( rs.next() )System.out.println(rs.getObject(1));
		rs.close();		rs = stm.executeQuery("pragma page_size");
		while( rs.next() )System.out.println(rs.getObject(1));
		rs.close();
		rs = stm.executeQuery("pragma temp_store");
		while( rs.next() )System.out.println(rs.getObject(1));
		rs.close();
		rs = stm.executeQuery("pragma temp_store");
		while( rs.next() )System.out.println(rs.getObject(1));
		Database.giveConnection(connection);
		String query = ap.getQuery();
		TAPToolBox.executeTAPQuery(query, false, "json", 10000, System.out);
		Database.close();
	}

}
