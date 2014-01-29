package saadadb.query.result;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Set;

import saadadb.database.Database;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.query.executor.Query;
import saadadb.sqltable.SQLTable;
import saadadb.util.Messenger;
import saadadb.util.SaadaConstant;

public class SaadaQLResultSet extends OidResultSet {
	protected final SQLLikeResultSet oidSQL;

	protected final PatternResultSet oidRelation;
	protected int current_pos = 0;
//	private LinkedHashSet<AttributeHandler> col_names = new LinkedHashSet<AttributeHandler>();
	// private SaadaQLMetaSet col_names;


	/**
	 * @param sql_query
	 * @param oidPattern
	 * @param limit
	 * @param const_att
	 * @throws Exception
	 * @version $Id$
	 */
	public SaadaQLResultSet(String sql_query, Set<Long> oidPattern, int limit, AttributeHandler[] ucols, Set<AttributeHandler> const_att) throws Exception {
		super(limit);
		if( sql_query != null && sql_query.length() > 0 ) {
			oidSQL = new SQLLikeResultSet(sql_query, ucols);
			col_names = new SaadaQLMetaSet(oidSQL.col_names.getHandlers(), const_att);
		
		}
		else {
			oidSQL = null;
		}
		if( oidPattern != null ) {
			oidRelation = new PatternResultSet(oidPattern);
			/*
			 * oidsaada is taken from oidRelation only if it has not been 
			 * put by oidSQL which has necessary a column named oidsaada
			 */
			if( col_names == null || col_names.size() == 0 ) {
				AttributeHandler ah = new AttributeHandler();
				ah.setNameattr("oidsaada");
				ah.setNameorg("oidsaada");
				ah.setType("long");
				LinkedHashSet<AttributeHandler> lah = new LinkedHashSet<AttributeHandler>();
				lah.add(ah);
				col_names = new SaadaQLMetaSet(lah, null);
			}
		}
		else {
			oidRelation = null;
		}
		if( limit <= 0 ) {
			this.limit = Integer.MAX_VALUE;
		}
		else {
			this.limit = limit;
		}
		this.computeSize();
	}

	/**
	 * @param real_resultset
	 * @throws Exception
	 */
	public SaadaQLResultSet(ResultSet real_resultset) throws Exception{
		super(-1);
		oidSQL = new SQLLikeResultSet(real_resultset);
		this.size = oidSQL.getSize();
		oidRelation = null;
	}
	/* (non-Javadoc)
	 * @see saadadb.query.result.OidResultSet#getMeta()
	 */
	@Override
	public Set<AttributeHandler> getMeta() {
		return this.col_names.getHandlers();
	}
 

	/* (non-Javadoc)
	 * @see saadadb.query.OIdResultSet#next()
	 */
	@Override
	public boolean next() throws SQLException {
		/*
		 * maptchPattern(s) only: read oidRelation result set
		 */
		if( oidSQL == null && oidRelation != null) {
			if( this.current_pos < this.limit ) {
				this.current_pos++;
				return oidRelation.next();
			}
			else {
				return false;
			}
		}
		/*
		 * SQL only: read oidSQL result set
		 */
		else if( oidSQL != null && oidRelation == null) {
			return oidSQL.next();
		}
		/*
		 * Both SQL and maptchPattern: extract from oidSQL result set the next oid already 
		 * being into the oidRelation result set
		 */
		else if( oidSQL != null && oidRelation != null) {
			while(this.oidSQL.next() && this.current_pos < this.limit){
				if( this.oidRelation.contains(this.oidSQL.getLong("oidsaada"))){
					this.current_pos++;
					return true;
				}
			}
			return false;
		}
		else {
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see saadadb.query.OidResultSet#getBoolean(int)
	 */
	@Override
	public boolean getBoolean(int col_num) throws SQLException {
		if( this.oidSQL != null) {
			return this.oidSQL.getBoolean(col_num);
		}
		else if( this.oidRelation != null) {
			return this.oidRelation.getBoolean(col_num);
		}
		else {
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see saadadb.query.OidResultSet#getBoolean(java.lang.String)
	 */
	@Override
	public boolean getBoolean(String col_name) throws SQLException {
		if( this.oidSQL != null) {
			return this.oidSQL.getBoolean(col_name);
		}
		else if( this.oidRelation != null) {
			return this.oidRelation.getBoolean(col_name);
		}
		else {
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see saadadb.query.OidResultSet#getDouble(int)
	 */
	@Override
	public double getDouble(int col_num) throws SQLException {
		if( this.oidSQL != null) {
			return this.oidSQL.getDouble(col_num);
		}
		else if( this.oidRelation != null) {
			return this.oidRelation.getDouble(col_num);
		}
		else {
			return SaadaConstant.DOUBLE;
		}
	}

	/* (non-Javadoc)
	 * @see saadadb.query.OidResultSet#getDouble(java.lang.String)
	 */
	@Override
	public double getDouble(String col_name) throws SQLException {
		if( this.oidSQL != null) {
			return this.oidSQL.getDouble(col_name);
		}
		else if( this.oidRelation != null) {
			return this.oidRelation.getDouble(col_name);
		}
		else {
			return SaadaConstant.DOUBLE;
		}
	}

	/* (non-Javadoc)
	 * @see saadadb.query.OidResultSet#getFloat(int)
	 */
	@Override
	public float getFloat(int col_num) throws SQLException {
		if( this.oidSQL != null) {
			return this.oidSQL.getFloat(col_num);
		}
		else if( this.oidRelation != null) {
			return this.oidRelation.getFloat(col_num);
		}
		else {
			return SaadaConstant.FLOAT;
		}
	}

	/* (non-Javadoc)
	 * @see saadadb.query.OidResultSet#getFloat(java.lang.String)
	 */
	@Override
	public float getFloat(String col_name) throws SQLException {
		if( this.oidSQL != null) {
			return this.oidSQL.getFloat(col_name);
		}
		else if( this.oidRelation != null) {
			return this.oidRelation.getFloat(col_name);
		}
		else {
			return SaadaConstant.FLOAT;
		}
	}

	/* (non-Javadoc)
	 * @see saadadb.query.OidResultSet#getInt(int)
	 */
	@Override
	public int getInt(int col_num) throws SQLException {
		if( this.oidSQL != null) {
			return this.oidSQL.getInt(col_num);
		}
		else if( this.oidRelation != null) {
			return this.oidRelation.getInt(col_num);
		}
		else {
			return SaadaConstant.INT;
		}
	}
	/* (non-Javadoc)
	 * @see saadadb.query.OidResultSet#getInt(java.lang.String)
	 */
	@Override
	public int getInt(String col_name) throws SQLException {
		if( this.oidSQL != null) {
			return this.oidSQL.getInt(col_name);
		}
		else if( this.oidRelation != null) {
			return this.oidRelation.getInt(col_name);
		}
		else {
			return SaadaConstant.INT;
		}
	}
	/* (non-Javadoc)
	 * @see saadadb.query.OidResultSet#getShort(int)
	 */
	@Override
	public short getShort(int col_num) throws SQLException {
		if( this.oidSQL != null) {
			return this.oidSQL.getShort(col_num);
		}
		else if( this.oidRelation != null) {
			return this.oidRelation.getShort(col_num);
		}
		else {
			return SaadaConstant.SHORT;
		}
	}
	/* (non-Javadoc)
	 * @see saadadb.query.OidResultSet#getShort(java.lang.String)
	 */
	@Override
	public short getShort(String col_name) throws SQLException {
		if( this.oidSQL != null) {
			return this.oidSQL.getShort(col_name);
		}
		else if( this.oidRelation != null) {
			return this.oidRelation.getShort(col_name);
		}
		else {
			return SaadaConstant.SHORT;
		}
	}
	/* (non-Javadoc)
	 * @see saadadb.query.OidResultSet#getShort(int)
	 */
	@Override
	public byte getByte(int col_num) throws SQLException {
		if( this.oidSQL != null) {
			return this.oidSQL.getByte(col_num);
		}
		else if( this.oidRelation != null) {
			return this.oidRelation.getByte(col_num);
		}
		else {
			return SaadaConstant.BYTE;
		}
	}
	/* (non-Javadoc)
	 * @see saadadb.query.OidResultSet#getShort(int)
	 */
	@Override
	public byte getByte(String col_name) throws SQLException {
		if( this.oidSQL != null) {
			return this.oidSQL.getByte(col_name);
		}
		else if( this.oidRelation != null) {
			return this.oidRelation.getByte(col_name);
		}
		else {
			return SaadaConstant.BYTE;
		}
	}
	/* (non-Javadoc)
	 * @see saadadb.query.OidResultSet#getShort(int)
	 */
	@Override
	public char getChar(int col_num) throws SQLException {
		if( this.oidSQL != null) {
			return this.oidSQL.getChar(col_num);
		}
		else if( this.oidRelation != null) {
			return this.oidRelation.getChar(col_num);
		}
		else {
			return SaadaConstant.CHAR;
		}
	}
	/* (non-Javadoc)
	 * @see saadadb.query.OidResultSet#getShort(int)
	 */
	@Override
	public char getChar(String col_name) throws SQLException {
		if( this.oidSQL != null) {
			return this.oidSQL.getChar(col_name);
		}
		else if( this.oidRelation != null) {
			return this.oidRelation.getChar(col_name);
		}
		else {
			return SaadaConstant.CHAR;
		}
	}	/* (non-Javadoc)
	 * @see saadadb.query.OIdResultSet#getObject(int)
	 */
	@Override
	public Object getObject(int col_num) throws SQLException {
		if( this.oidSQL != null) {
			return this.oidSQL.getObject(col_num);
		}
		else if( this.oidRelation != null) {
			return this.oidRelation.getObject(col_num);
		}
		else {
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see saadadb.query.OIdResultSet#getObject(java.lang.String)
	 */
	@Override
	public Object getObject(String col_name) throws SQLException {
		if( this.oidSQL != null) {
			return this.oidSQL.getObject(col_name);
		}
		else if( this.oidRelation != null) {
			return this.oidRelation.getObject(col_name);
		}
		else {
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see saadadb.query.OIdResultSet#getLong(int)
	 */
	@Override
	public long getLong(int col_num) throws SQLException {
		if( this.oidSQL != null) {
			return this.oidSQL.getLong(col_num);
		}
		else if( this.oidRelation != null) {
			return this.oidRelation.getLong(col_num);
		}
		else {
			return SaadaConstant.LONG;
		}
	}

	/* (non-Javadoc)
	 * @see saadadb.query.OIdResultSet#getLong(java.lang.String)
	 */
	@Override
	public long getLong(String col_name) throws SQLException {
		if( this.oidSQL != null) {
			return this.oidSQL.getLong(col_name);
		}
		else if( this.oidRelation != null) {
			return this.oidRelation.getLong(col_name);
		}
		else {
			return SaadaConstant.LONG;
		}
	}

	/* (non-Javadoc)
	 * @see saadadb.query.OidResultSet#getOid()
	 */
	@Override
	public long getOid() throws SQLException {
		return getLong(1);
	}

	/* (non-Javadoc)
	 * @see saadadb.query.OIdResultSet#close()
	 */
	@Override
	public void close() throws QueryException {
		if( this.oidSQL != null) {
			this.oidSQL.close();
		}
		if( this.oidRelation != null) {
			this.oidRelation.close();
		}
	}

	/**
	 * COnvert the result set into a set of oids (oids are copied)
	 * @return
	 * @throws SQLException
	 */
	public final Set<Long> getOidSet() throws SQLException{
		Set<Long> sl = new LinkedHashSet<Long>(); 
		while( this.next() ) {
			sl.add(this.getLong("oidsaada"));
		}
		return sl;
	}

	/* (non-Javadoc)
	 * @see saadadb.query.OidResultSet#getSize()
	 */
	@Override
	public int getSize() {
		return this.size;
	}

	/* (non-Javadoc)
	 * @see saadadb.query.OidResultSet#getSize()
	 */
	@Override
	protected void computeSize() {
		int pos = 0;
		/*
		 * maptchPattern(s) only: read oidRelation result set
		 */
		if( oidSQL == null && oidRelation != null) {
			size =  oidRelation.getSize();
		}
		/*
		 * SQL only: read oidSQL result set
		 */
		else if( oidSQL != null && oidRelation == null) {
			size = oidSQL.getSize();
		}
		/*
		 * Both SQL and maptchPattern: extract from oidSQL result set the next oid already 
		 * being into the oidRelation result set
		 */
		else if( oidSQL != null && oidRelation != null) {
			/*
			 * occurs when resultset is in mode TYPE_FORWARD_ONLY
			 */
			if( oidSQL.getSize() == -1  ) {
				size =  -1;
				return;
			}
			try {
				while(this.oidSQL.next() && pos < this.limit){
					if(this.oidRelation==null || this.oidRelation.contains(this.oidSQL.getLong("oidsaada"))){
						pos++;
					}
				}
				this.oidRelation.setCursor(0);
				this.oidSQL.rewind();
				size =  pos;
			} catch (SQLException e) {
				Messenger.printStackTrace(e);
				size = -1;
			}
		}
		else {
			size = -1;
		}
	}

	@Override
	public boolean setCursor(int row) {
		/*
		 * maptchPattern(s) only: read oidRelation result set
		 */
		if( oidSQL == null && oidRelation != null) {
			this.current_pos = row;
			return oidRelation.setCursor(row);
		}
		/*
		 * SQL only: read oidSQL result set
		 */
		else if( oidSQL != null && oidRelation == null) {
			this.current_pos = row;
			return oidSQL.setCursor(row);
		}
		/*
		 * Both SQL and maptchPattern: extract from oidSQL result set the next oid already 
		 * being into the oidRelation result set
		 */
		else if( oidSQL != null && oidRelation != null) {
			this.current_pos = 0;
			try {
				this.oidSQL.rewind();
				while(this.oidSQL.next() && this.current_pos < this.limit){
					if(this.oidRelation==null || this.oidRelation.contains(this.oidSQL.getLong("oidsaada"))){
						if( this.current_pos == row  ) {
							return true;
						}
						this.current_pos++;
					}
				}
			} catch (SQLException e) {
				Messenger.printStackTrace(e);
			}
			return false;
		}
		else {
			return false;
		}
	}

	/**
	 * re-init the resultset iterators
	 */
	public void rewind() {
		if( oidRelation != null) {
			oidRelation.rewind();
		}
		/*
		 * SQL only: read oidSQL result set
		 */
		if( oidSQL != null ) {
			oidSQL.rewind();
		}
		this.current_pos = 0;
	}
	/**
	 * @return Returns the col_names.
	 */
	public SaadaQLMetaSet getCol_names() {
		return col_names;
	}
//	/**
//	 * @return
//	 * @throws SaadaException 
//	 */
//	public String storeInTempTableORG() throws Exception {
//		String table_name =  "srs_" + System.currentTimeMillis();
//		String format = "";
//		String query = "INSERT INTO " + table_name + "(";
//		for( AttributeHandler ah: this.col_names) {
//			if( format.length() > 0 ) {
//				format += ", ";
//				query += ",";
//			}
//			format += ah.getNameattr() + " " + Database.getWrapper().getSQLTypeFromJava(ah.getType());
//			query += ah.getNameattr() + " ";
//		}
//		SQLTable.createTable(table_name, format, "oidsaada", true);
//		query += ")\n";
//		int num_row = 0;
//		/*
//		 * We make a multiple insertion by using SELECT/UNION in order to be compliant with PSQL 8.*
//		 * The faster solution based on an ASCI file can not be used here because this method is used
//		 * by the Web interface which has no admin privileges
//		 */
//		/*
//		 * Build a marker for text columns
//		 */
//		if( this.size > 0 ) {
//			boolean quoted[] = new boolean[this.col_names.size() + 1];
//			int i=1; // starts with one as JDBC column numbers
//			for( AttributeHandler ah: this.col_names) {
//				if( "String".equals(ah.getType()) ) {
//					quoted[i] = true;
//				}
//				else {
//					quoted[i] = false;				
//				}
//				i++;
//			}
//			while( this.next()) {
//				if( num_row > 0 ) {
//					query += "\nUNION\n";
//				}
//				query += "(SELECT ";
//				for( i=1 ; i<=this.col_names.size() ; i++  ) {
//					String val = this.getObject(i).toString(); 
//					if( i > 1) {
//						query += ",";
//					}
//					if( quoted[i]) {
//						query += "'";
//					}
//					if( val.equals("NaN") || val.equals("") || val.equals("Infinity")) {					
//						query += "null";
//					}
//					else {
//						query += val;
//					}
//					if( quoted[i]) {
//						query += "'";
//					}
//				}
//				query += ")";
//				num_row++;
//			}
//			SQLTable.runQueryUpdateSQL(query
//					, false
//					, table_name);
//		}
//
//		return table_name;
//	}
	/**
	 * @return
	 * @throws SaadaException 
	 */
	public String storeInTempTable() throws Exception {
		String tempo_table =  "srs_" + System.currentTimeMillis();
		if( this.size == 0)  {
			return tempo_table;
		}
		String format = "";
		String insert_query = "INSERT INTO " + Database.getWrapper().getTempoTableName(tempo_table) + "(";
		for( String s: this.col_names.keySet()) {
			AttributeHandler ah = this.col_names.getSQLColumnHandler(s);
			if( format.length() > 0 ) {
				format += ", ";
				insert_query += ",";
			}
			format += ah.getNameattr() + " " + Database.getWrapper().getSQLTypeFromJava(ah.getType());
			insert_query += ah.getNameattr() + " ";
		}
		SQLTable.createTemporaryTable(tempo_table, format, "oidsaada", true);
		insert_query += ")\n";
		int oidsize = this.size;
		int currentoid = 0;
		int BUNCH=1000;
		String query = "";
		int num_row = 0;
		while( currentoid < oidsize) {
			boolean quoted[] = new boolean[this.col_names.size() + 1];
			int i=1; // starts with one as JDBC column numbers
			for( String s: this.col_names.keySet()) {
				AttributeHandler ah = this.col_names.getSQLColumnHandler(s);
				if( "String".equals(ah.getType()) ) {
					quoted[i] = true;
				}
				else {
					quoted[i] = false;				
				}
				i++;
			}

			this.next() ;
			if( num_row > 0 ) {
				query += "\nUNION\n";
			}
			query += "(SELECT ";
			for( i=1 ; i<=this.col_names.size() ; i++  ) {
				String val = "";
				/*
				 * Queries with columns made with computations of native columns aer typed as byte[]
				 */
				Object obj = this.getObject(i);
				if( obj == null ) {
					val = null;
				}
				else if( obj.getClass().getName().equals("[B")) {
					val = new String((byte[])obj);
				}
				else {
					val = obj.toString();
				}
				//this.getObject(i).toString(); 
				if( i > 1) {
					query += ",";
				}
				if( quoted[i]) {
					query += "'";
				}
				if( val == null || val.equals("NaN") || val.equals("") || val.equals("Infinity")) {					
					if( quoted[i]) {
						query += "null";
					}
					else {
						query += "null::numeric";
					}
				}
				else {
					query += val;
				}
				if( quoted[i]) {
					query += "'";
				}
			}
			query += ")";
			num_row++;
			if( num_row >= BUNCH ) {
				SQLTable.addQueryToTransaction(insert_query + query
						, Database.getWrapper().getTempoTableName(tempo_table));

				query = "";
				num_row = 0;
			}
			currentoid++;
		}
		if( !query.equals("")) {
			SQLTable.addQueryToTransaction(insert_query + query);

			query = "";
			num_row = 0;
		}
		return tempo_table;
	}
	

	public static void main(String[] args) throws Exception {
		Messenger.debug_mode = false;
		Database.init("XCATDBi");
		//		Database.getConnector().setAdminMode("");
		Query q = new Query();
		//		SQLLikeResultSet srs = q.runAllColumnsQuery("Select ENTRY From * In Collection0 WhereUCD { [pos.angDistance] > 1 [deg] }");
		//		for( AttributeHandler s: srs.col_names) {
		//			System.out.println(s.getNameattr() + " " + s.getType() );
		//		}
		System.out.println(Runtime.getRuntime().freeMemory());
		SaadaQLResultSet query_result = 
			q.runQuery("Select ENTRY From * In ARCH_CAT "
					+ " WhereRelation{ "
					+ "     matchPattern{ArchSrcToCatSrc"
					+ "     AssObjClass{CatalogueEntry}"
					+ "     AssObjAttClass{ _detid > 800 }"
					+ "     }}	 "
			);
		//System.out.println(q.getReport().getReport());
		System.out.println(Runtime.getRuntime().freeMemory());
		System.gc();
		System.out.println(Runtime.getRuntime().freeMemory());
		Database.close();
	}


}
