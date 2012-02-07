package saadadb.resourcetest;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.meta.AttributeHandler;
import saadadb.query.executor.Query;
import saadadb.query.result.OidsaadaResultSet;
import saadadb.query.result.SaadaQLResultSet;
import saadadb.sqltable.SQLLargeQuery;
import saadadb.util.Messenger;

public class QueryRunner {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ArgsParser ap;
		try {
			ap = new ArgsParser(args);
			Database.init(ap.getDBName());
			String query = ap.getQuery();
			Connection large_connection = DriverManager.getConnection(Database.getConnector().getJdbc_url(),Database.getConnector().getJdbc_reader(), Database.getConnector().getJdbc_reader_password());
			Statement _stmts =large_connection.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
//			 _stmts.executeUpdate("pragma cache_size=4000"); 
//			 _stmts.executeUpdate("pragma page_size=4096"); 
			 large_connection.close();

			Messenger.printMsg(Messenger.TRACE, "Processing query " + query);
			Query q = new Query();
//			SaadaQLResultSet srs = q.runQuery(query);
//			Messenger.printMsg(Messenger.TRACE, "Declared size " + srs.getSize() );
//			int cpt = 0;
//			while( srs.next()) {
//				//System.out.println(srs.getOid());
//				cpt++;
//			}
//			Messenger.printMsg(Messenger.TRACE, cpt  + " oids found");
			OidsaadaResultSet ors = q.runBasicQuery(query);
			for( AttributeHandler ah:  q.buildListAttrHandPrinc()) {
				System.out.println(ah.getNameattr());
			}
//			Messenger.printMsg(Messenger.TRACE, "Declared size " + srs.getSize() );
			int cpt = 0;
			while( ors.next()) {
				//System.out.println(ors.getOId());
				cpt++;
			}
			Messenger.printMsg(Messenger.TRACE, cpt  + " oids found");
		} catch (Exception e) {
			e.printStackTrace();
		}
		

	}

}
