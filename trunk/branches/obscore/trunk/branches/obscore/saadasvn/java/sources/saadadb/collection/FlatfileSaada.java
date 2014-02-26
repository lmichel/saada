package saadadb.collection;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import saadadb.database.Database;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.SaadaException;
import saadadb.sqltable.SQLQuery;
import saadadb.util.Messenger;
import saadadb.util.SaadaConstant;
/**
 * <p>Title: System d'archivage automatique des donnes astrononique</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Observaroire Astronomique Strasbourg</p>
 * @author XXXX
 * @version 00000001
 * 
 * 07/2012: Fix bug: was not  loading extended attributes 
 */ 
public class FlatfileSaada extends SaadaInstance{
    
    public String product_url_csa = SaadaConstant.STRING;
 
	/**
	 * @param oid
	 * @throws SaadaException
	 * @throws CollectionException
	 * @throws SQLException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	@Override
	public void init(long oid) throws Exception {
		/*
		 * Here we test the consistance between the oid and class
		 */
		this.oidsaada = oid;
		SQLQuery squery = new SQLQuery();
		String sql = " Select * from " 
			      + Database.getCachemeta().getCollectionTableName(SaadaOID.getCollectionNum(oid)
			                                                     , SaadaOID.getCategoryNum(oid))
		          + " where  oidsaada = " + oid;
		ResultSet rs = squery.run(sql);
		int cpt=1;
		while( rs.next() ) {
			if( cpt > 1 ) {
				Messenger.printMsg(Messenger.ERROR, "FATAL: Multiple instances with oid " + oid);
				System.exit(1);
			}
			cpt++;
			
			this.setOid(rs.getLong("oidsaada"));
			this.setNameSaada(rs.getString("namesaada"));
			this.setDateLoad(rs.getLong("date_load"));
			/** ----------Attention Super Class-------------* */
			// Class cls = obj.getClass();
			Class cls = this.getClass();
			Vector<Class> vt_class = new Vector<Class>();
			while (!cls.getName().equals("saadadb.collection.SaadaInstance")) {
				vt_class.add(cls);
				cls = cls.getSuperclass();
			}
			for (int k = vt_class.size() - 1; k >= 0; k--) {
				Field fieldlist[] = (vt_class.get(k)).getDeclaredFields();
				for (int i = 0; i < fieldlist.length; i++) {
					setFieldValue(fieldlist[i], rs);
				}
			}
		}
		squery.close();;
	}
	
	
	/* (non-Javadoc)
	 * @see saadadb.collection.SaadaInstance#setProduct_url_csa(java.lang.String)
	 */
	@Override
	public void setProduct_url_csa(String product_url_csa) throws AbortException{
    	if( product_url_csa == null ) {
    		AbortException.throwNewException(SaadaException.WRONG_PARAMETER, "product_url_csa cannot be set to null");
    	}
		this.product_url_csa = product_url_csa;
    }

	/**
	 * @return
	 */
	@Override
	public String getMimeType() {
		return getMimeType(this.product_url_csa);
	}


	@Override
	public String getProduct_url_csa() {
		return this.product_url_csa;
	}


}
  
