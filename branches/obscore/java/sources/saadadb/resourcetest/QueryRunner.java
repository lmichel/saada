package saadadb.resourcetest;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map.Entry;
import java.util.Set;

import saadadb.api.SaadaLink;
import saadadb.collection.obscoremin.SaadaInstance;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.meta.AttributeHandler;
import saadadb.meta.MetaRelation;
import saadadb.query.executor.Query;
import saadadb.query.matchpattern.CounterpartSelector;
import saadadb.query.result.OidsaadaResultSet;
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
//				//System.out.println(srs.oidsaada);
//				cpt++;
//			}
//			Messenger.printMsg(Messenger.TRACE, cpt  + " oids found");
			OidsaadaResultSet ors = q.runBasicQuery(query);
			for( AttributeHandler ah:  q.buildListAttrHandPrinc()) {
				System.out.println(ah.getNameattr());
			}
//			Messenger.printMsg(Messenger.TRACE, "Declared size " + srs.getSize() );
			int cpt = 0;
			Set<AttributeHandler>constrained_attr =  q.buildListAttrHandPrinc();				
			while( ors.next()) {
				//System.out.println(ors.getOId());
				cpt++;
				SaadaInstance si = Database.getCache().getObject(ors.getOId());		
				System.out.println(si.obs_id);
				for(AttributeHandler ah: constrained_attr) {
					System.out.println(" ATT " + ah.getNameorg() + " " + si.getFieldValue(ah.getNameattr()));
				}
				for( Entry<String, CounterpartSelector> e: q.getMatchingCounterpartQueries().entrySet()) {
					String rel_name = e.getKey();
					CounterpartSelector cp_val = e.getValue();
					MetaRelation mr = Database.getCachemeta().getRelation(rel_name);
					Set<SaadaLink> mcp = si.getCounterpartsMatchingQuery(rel_name, e.getValue());
					for( SaadaLink sl:mcp ) {
						long cpoid = sl.getEndindOID();
						SaadaInstance cp = Database.getCache().getObject(cpoid);
						System.out.print("      Name <" + cp.getFieldValue("namesaada") + "> " + cp.getFieldValueByUCD("meta.record", false));
						for( String q2: cp_val.getQualif_query().keySet()) {
							System.out.print(" " + q2 + "=" + sl.getQualifierValue(q2) );
						}
						System.out.println("");
					}
				
				}
			}
			Messenger.printMsg(Messenger.TRACE, cpt  + " oids found");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Database.close();

	}

}
