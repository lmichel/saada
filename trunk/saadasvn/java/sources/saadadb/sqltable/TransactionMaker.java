package saadadb.sqltable;

import java.sql.BatchUpdateException;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;

import saadadb.database.Database;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.SaadaException;
import saadadb.util.Messenger;

/**
 * This class offers a global solution to the transaction management.
 * As update  queries can be run from any part of the code, it may be difficult to well
 * place transaction/commit statements.
 * This class is used by SQLTable which normally run queries. 
 * It is initialized when the beginTransaction method is called.
 * From that time, all update queries are stored within the TransactionMaker. 
 * They are executed in one shot when the commitTransaction is called.
 * An error is rise when  beginTransaction is called twice without commit.
 * TransactionMaker instance is unique in SQLTable
 * @author michel
 *
 */
public class TransactionMaker {
	ArrayList<QueryString> queries = new ArrayList<QueryString>();
	private  boolean locked = false;
	private  boolean forced_mode = false;

	public TransactionMaker(boolean forced_mode) {
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "New transaction manager (forced mode = " + forced_mode + ")");
		this.forced_mode   = forced_mode;
		unlock();
	}

	/**
	 * @param query
	 */
	protected void addQuery(String query) {
		if( query != null && query.length() > 0 ) {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Add query #" + queries.size() + " to transaction : " + query);
			queries.add(new QueryString(query, null));
		}
	}
	
	/**
	 * @param query
	 * @param params
	 */
	protected void addQuery(String query, Object[] params) {
		if( query != null && query.length() > 0 ) {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Add query #" + queries.size() + " to transaction : " + query);
			queries.add(new QueryString(query, params));
		}
	}

	protected boolean isFree() {
		if( !locked /*queries.size() == 0 */) {
			return true;
		}
		else {
			return false;
		}
	}

	void lock() {
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "lock transaction manager");
		this.locked = true;
	}
	void unlock() {
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "unlock transaction manager");
		this.locked = false;
		//queries = new ArrayList<String>();
	}
	/**
	 * @throws Exception
	 */
	protected synchronized void makeTransaction() throws AbortException {
		Statement stmt = null;
		String last_q = "";
		lock();
		long start = System.currentTimeMillis();
		try {
			/*
			 * Mysql appreciates very well explicit unlocks before to commit. Otherwise, It could
			 * Require explicit locks everywhere even in SELECT
			 */
			queries.add( new QueryString(Database.getWrapper().unlockTables(), null));
			Database.get_connection().setAutoCommit(false);
			stmt = Database.get_connection().createStatement(); 
			int cpt = 0;
			for(QueryString qs: queries) {
				String q = qs.query;
				cpt++;
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG,"UPDATE: " + q);
				if( q.trim().startsWith("LOADTSVTABLE") ) {
					String[] fs = q.split(" ");
					last_q  = q; 
					if( Database.getWrapper().tsvLoadNotSupported() ) {
						Database.getWrapper().storeTable(fs[1].trim(), Integer.parseInt(fs[2].trim()), fs[3].trim()) ;			
					}
					else {
						String[] stqs = Database.getWrapper().getStoreTable(fs[1].trim(), Integer.parseInt(fs[2].trim()), fs[3].trim());
						for(String stq: stqs ) {
							if (Messenger.debug_mode)
								Messenger.printMsg(Messenger.DEBUG, "LOADTSVTABLE: run: " + stq);
							last_q = stq;
							execStatement(stmt, stq);
						}
					}
				}
				else if( qs.params == null ){
					last_q = q;
					execStatement(stmt, q);
				}
				else {
					PreparedStatement pstmt =  Database.get_connection().prepareStatement(q);
					for( int i=0 ; i<qs.params.length ; i++ ) {
						pstmt.setObject(i+1,qs.params[i] );
					}
					execStatement(pstmt, q);
				}
			}
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "JDBC Commit");
			if( !Database.get_connection().getAutoCommit() ) {
				Database.get_connection().commit();
			}
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Transaction done in " + ((System.currentTimeMillis()-start)/1000F) + " sec");
			queries = new ArrayList<QueryString>();
			stmt.close();
			unlock();
		} catch (BatchUpdateException be) {
			Messenger.printStackTrace(be);
			try {
				stmt.close();
				Messenger.printMsg(Messenger.TRACE, "Rollback");
				Database.get_connection().rollback();
			} catch (Exception e) { }
			unlock();
			checkBatchError(be);
			AbortException.throwNewException(SaadaException.DB_ERROR, be);
		} catch(Exception e) {
			Messenger.printStackTrace(e);
			try {
				stmt.close();
				Messenger.printMsg(Messenger.TRACE, "Rollback");
				Database.get_connection().rollback();
			} catch (Exception e2) { }
			unlock();
			Messenger.printMsg(Messenger.ERROR, "on query : " + last_q);
			AbortException.throwNewException(SaadaException.DB_ERROR, e);
		}
	}

	/**
	 * Wrap the statement execution according to the transaction mode
	 * @param stmt
	 * @param query
	 * @param batch_mode
	 * @throws Exception
	 */
	private void execStatement(Statement stmt, String query) throws Exception {
		Messenger.dbAccess();
		if( !forced_mode ) {
			stmt.executeUpdate(query);
		}
		else {
			Database.get_connection().setAutoCommit(true);
			try {
				stmt.executeUpdate(query);	
			}
			catch (Exception e) {
				Messenger.procAccess();
				Messenger.printMsg(Messenger.WARNING, "SQL error (ignored in transaction in forced mode) "  + e.getMessage());
			}
		}
		Messenger.procAccess();

	}
	/**
	 * Wrap the prepared statement execution according to the transaction mode
	 * @param pstmt
	 * @param query
	 * @param batch_mode
	 * @throws Exception
	 */
	private void execStatement(PreparedStatement pstmt, String query) throws Exception {
		Messenger.dbAccess();
		if( !forced_mode ) {
			pstmt.executeUpdate();
		}
		else {
			Database.get_connection().setAutoCommit(true);
			try {
				pstmt.executeUpdate();	
				Messenger.procAccess();
			}
			catch (Exception e) {
				Messenger.procAccess();
				Messenger.printMsg(Messenger.WARNING, "SQL error (ignored in transaction in forced mode) "  + e.getMessage());
			}
		}
		Messenger.procAccess();

	}

	/**
	 * @param be
	 */
	private void checkBatchError(BatchUpdateException be) {
		int[] counts = be.getUpdateCounts();
		for( int i=0 ; i<counts.length ; i++ ) {
			if( counts[i] < 0 ) {
				for( int j=0 ; j<i ; j++ ) {
					Messenger.printMsg(Messenger.TRACE, "quey #" + j + " " + queries.get(j));
				}
				Messenger.printMsg(Messenger.ERROR, "Error on query #" + i + " " + queries.get(i) ); 
				return;
			}
		}
		/*
		 * If counts are not properly set, we are probably using SQLITE: 
		 * try to parse the message
		 */
		String s = be.getMessage().replaceAll("batch entry ", "");
		int pos = Integer.parseInt(s.substring(0, s.indexOf(":")));
		for( int j=0 ; j<pos ; j++ ) {
			Messenger.printMsg(Messenger.TRACE, "quey #" + j + " " + queries.get(j));
		}
		Messenger.printMsg(Messenger.ERROR, "Error on query #" + pos + " "  + queries.get(pos) ); 
	}

	/**
	 * @return
	 */
	protected String getReport() {
		if( queries.size() == 0 ) {
			return "No query in the current transaction";
		}
		else {
			String retour = "BEGIN TRANSACTION\n";
			for(QueryString qs: queries) {
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG,"UPDATE: " + qs.query);
				retour += qs.query + "\n";		
			}
			return retour + "END TRANSACTION\n";

		}
	}

	/**
	 * 
	 */
	public void abortTransaction()  {
		try {		
			Messenger.dbAccess();
			unlock();	
			queries = new ArrayList<QueryString>();

			Database.get_connection().rollback();
			
//			Messenger.printMsg(Messenger.TRACE, "ABORT CURRENT TRANSACTION!!");
//			Statement stmt = Database.get_connection().createStatement(); 
//			stmt.executeUpdate(Database.getWrapper().abortTransaction());
//			stmt.close();
		} catch (Exception e) {
		}	
	}
	
	/**
	 * @author laurent
	 * @version $Id$
	 */
	class QueryString {
		protected String query;
		protected Object[] params;
		
		public QueryString(String query, Object[] params) {
			this.query = query;
			this.params = params;
		}
		
		
	}

	public static void main(String[] args) throws Exception{
		Messenger.debug_mode = true;

		try {
			Database.init("MUSE");
			SQLTable.beginTransaction();
			SQLTable.addQueryToTransaction("DROP TRIGGER LocalPSF_secclass");
			SQLTable.addQueryToTransaction("CREATE TEMPORARY TABLE tempo_LocalPSF(oidprimary int8, oidsecondary int8, dec float8, lambda float8, ra float8)");
			SQLTable.addQueryToTransaction("CREATE TEMPORARY TABLE p AS SELECT DataCube_IMAGE.oidsaada as oidsaada, pos_dec_csa, pos_ra_csa, product_url_csa, wl_max FROM DataCube_IMAGE");
			SQLTable.addQueryToTransaction("CREATE TEMPORARY TABLE s AS SELECT PSF_IMAGE.oidsaada as oidsaada, product_url_csa FROM PSF_IMAGE");
			SQLTable.addQueryToTransaction("INSERT INTO tempo_LocalPSF(oidprimary, oidsecondary, dec, lambda, ra) SELECT p.oidsaada, s.oidsaada, p.pos_dec_csa, p.wl_max, p.pos_ra_csa  FROM p CROSS JOIN s WHERE substrfff(p.product_url_csa, 3) = substr(s.product_url_csa, 3)");
			SQLTable.commitTransaction();
		} catch(Exception e){}
		SQLTable.beginTransaction();
		SQLTable.addQueryToTransaction("CREATE TEMPORARY TABLE tempo_LocalPSF(oidprimary int8, oidsecondary int8, dec float8, lambda float8, ra float8)");
		SQLTable.addQueryToTransaction("CREATE TEMPORARY TABLE p AS SELECT DataCube_IMAGE.oidsaada as oidsaada, pos_dec_csa, pos_ra_csa, product_url_csa, wl_max FROM DataCube_IMAGE");
		SQLTable.addQueryToTransaction("CREATE TEMPORARY TABLE s AS SELECT PSF_IMAGE.oidsaada as oidsaada, product_url_csa FROM PSF_IMAGE");
		SQLTable.addQueryToTransaction("INSERT INTO tempo_LocalPSF(oidprimary, oidsecondary, dec, lambda, ra) SELECT p.oidsaada, s.oidsaada, p.pos_dec_csa, p.wl_max, p.pos_ra_csa  FROM p CROSS JOIN s WHERE substr(p.product_url_csa, 3) = substr(s.product_url_csa, 3)");
		SQLTable.commitTransaction();

	}

}
