package saadadb.resourcetest;

import saadadb.collection.SaadaInstance;
import saadadb.database.Database;
import saadadb.meta.UTypeHandler;
import saadadb.meta.VOResource;
import saadadb.query.executor.Query;
import saadadb.query.result.SaadaQLResultSet;
import saadadb.util.Messenger;

public class QueryDMTester {

	public static void main(String[] args) throws Exception{
		Messenger.debug_mode = false;
		Database.init("DEVBENCH1_5_1");
		//String qstr = "Select IMAGE From * In GOODS WhereDM{ [Access.Format] != 4 [m/s] }";
		String qstr = "Select IMAGE From * In GOODS "
                    + "WhereDM {"
                    + "    [SpectralAxis.cov.location.val;stc:double1Type] > 1 [nm]"
                    + "}";
			;
		Query q = new Query();
		VOResource vor = Database.getCachemeta().getVOResource("LittleCharac");
		q.setDM(vor.getName());
		//q.runQuery(qstr);
		//System.out.println(q.getPrincipal_sql());
		Messenger.debug_mode = true;
		SaadaQLResultSet srs = q.runQuery(qstr);
		System.out.println(srs.getSize() + " objects found");
		while(srs.next()) {
			SaadaInstance si =  Database.getCache().getObject(srs.getOid());
			si.activateDataModel(vor.getName());
			for( UTypeHandler uth: vor.getUTypeHandlers()) {
				try {
				System.out.println(uth.getNickname() + " " + si.getFieldValue(uth.getNickname(), srs));
				} catch (Exception e) {
					System.out.println(uth.getNickname() + " NOT FUND");
					
				}
			}
		}
	}
}
