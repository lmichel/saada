package saadadb.resourcetest;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map.Entry;
import java.util.Set;

import saadadb.api.SaadaLink;
import saadadb.collection.EntrySaada;
import saadadb.collection.SaadaInstance;
import saadadb.collection.SaadaOID;
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
			Set<AttributeHandler>constrained_attr =  q.buildListAttrHandPrinc();				
			while( ors.next()) {
				//System.out.println(ors.getOId());
				cpt++;
				EntrySaada si = (EntrySaada) Database.getCache().getObject(ors.getOId());		
				System.out.println(si.getNameSaada() + " " + si.pos_ra_csa + " " + si.pos_dec_csa);
				for(AttributeHandler ah: constrained_attr) {
					System.out.print (ah.getNameorg() + "=" + si.getFieldValue(ah.getNameattr()) + " ");
				}
				System.out.println(" " );
				for( Entry<String, CounterpartSelector> e: q.getMatchingCounterpartQueries().entrySet()) {
					String rel_name = e.getKey();
					CounterpartSelector cp_val = e.getValue();
					MetaRelation mr = Database.getCachemeta().getRelation(rel_name);
					Set<SaadaLink> mcp = si.getCounterpartsMatchingQuery(rel_name, e.getValue());
					for( SaadaLink sl:mcp ) {
						long cpoid = sl.getEndindOID();
						SaadaInstance cp = Database.getCache().getObject(cpoid);
						System.out.print("      Name <" + cp.getFieldValue("namesaada") + "> ");
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
