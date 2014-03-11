package saadadb.query.result;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Set;

import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.QueryException;
import saadadb.meta.AttributeHandler;
import saadadb.sqltable.SQLQuery;
import saadadb.util.Messenger;
import saadadb.util.SaadaConstant;

/**
 * @author michel
 * * @version $Id$

 */
public class SQLLikeResultSet  extends OidResultSet {
	private ResultSet real_resultset;
	private SQLQuery squery ;

	/**
	 * Create a minimalist resultset. Used to hanlde null PointerException in empty query results
	 * @throws Exception
	 */
	public SQLLikeResultSet() throws Exception{
		super(-1);
		AttributeHandler ah = new AttributeHandler();
		ah.setNameattr("oidsaada");
		ah.setNameorg("oidsaada");
		ah.setType("long");
		this.col_names = new SaadaQLMetaSet(null, null);
		this.col_names.add(ah);
		this.size = 0;
	}

	/**
	 * @param real_resultset
	 * @throws Exception 
	 */
	public SQLLikeResultSet(ResultSet real_resultset) throws Exception{
		super(-1);
		this.real_resultset = real_resultset;
		try {
			if( !Database.getWrapper().forwardOnly && !(this.real_resultset.getType() == ResultSet.TYPE_FORWARD_ONLY)) {
				this.real_resultset.last();
				this.size =  this.real_resultset.getRow();
				this.real_resultset.beforeFirst();
			}
			else {
				this.size =  -1;				
			}
			this.col_names = new SaadaQLMetaSet(null, null);
			ResultSetMetaData md = this.real_resultset.getMetaData();
			int col = md.getColumnCount();
			for (int i = 1; i <= col; i++){
				col_names.add(new AttributeHandler(md, i));
			}
		} catch (SQLException e) {
			Messenger.printStackTrace(e);
		}
	}

	/**
	 * @param query
	 * @throws FatalException 
	 */
	public SQLLikeResultSet(String query, AttributeHandler[] ucols) throws Exception {
		super(-1);
		squery = new SQLQuery();
		this.real_resultset = squery.run(query);
		try {
			this.size =  squery.getSize();
			this.col_names = new SaadaQLMetaSet(null, null);
			ResultSetMetaData md = this.real_resultset.getMetaData();
			int col = md.getColumnCount();
			for (int i = 1; i <= col; i++){
				AttributeHandler ah = new AttributeHandler(md, i);
				col_names.add(ah);
				/*
				 * Reports UCD and Utypes column units
				 */
				if( ucols != null ) {
					for( AttributeHandler ah2: ucols) {
						if( ah.getNameattr().equals(ah2.getNameattr())) {
							ah.setUnit(ah2.getUnit());
							ah.setUcd(ah2.getUcd());
							ah.setUtype(ah2.getUtype());
						}
					}
				}
			}
		} catch (SQLException e) {
			Messenger.printStackTrace(e);
		}
	}



	@Override
	protected void computeSize() throws SQLException {
		if( this.real_resultset != null ) {
			this.size =  this.real_resultset.getRow();
		}
		else {
			this.size = 0;
		}
	}


	/* (non-Javadoc)
	 * @see saadadb.query.OidResultSet#next()
	 */
	@Override
	public boolean next() throws SQLException {
		/*
		 * Size can be forced to 0 by the query engine for query with matchPatterns returning empty sets
		 */
		if( this.size == 0 ) {
			return false;
		}
		return this.real_resultset.next();
	}

	/* (non-Javadoc)
	 * @see saadadb.query.result.OidResultSet#getMeta()
	 */
	@Override
	public Set<AttributeHandler> getMeta() {
		return this.col_names.getHandlers();
	}

	/* (non-Javadoc)
	 * @see saadadb.query.OidResultSet#getObject(int)
	 */
	@Override
	public Object getObject(int col_num) throws SQLException {
		return this.real_resultset.getObject(col_num);
	}

	/* (non-Javadoc)
	 * @see saadadb.query.OidResultSet#getObject(java.lang.String)
	 */
	@Override
	public Object getObject(String col_name) throws SQLException {
		return this.real_resultset.getObject(col_name);
	}
	/* (non-Javadoc)
	 * @see saadadb.query.OidResultSet#getLong(int)
	 */
	@Override
	public char getChar(int col_num) throws SQLException {
		Object o = this.real_resultset.getObject(col_num);
		if( o == null ) {
			return SaadaConstant.CHAR;
		}
		else {
			return o.toString().charAt(0);
		}
	}
	/* (non-Javadoc)
	 * @see saadadb.query.OidResultSet#getChar(java.lang.String)
	 */
	@Override
	public char getChar(String col_name) throws SQLException {
		Object o = this.real_resultset.getObject(col_name);
		if( o == null ) {
			return SaadaConstant.CHAR;
		}
		else {
			return o.toString().charAt(0);
		}
	}
	/* (non-Javadoc)
	 * @see saadadb.query.OidResultSet#getLong(int)
	 */
	@Override
	public byte getByte(int col_num) throws SQLException {
		return this.real_resultset.getByte(col_num);
	}
	/* (non-Javadoc)
	 * @see saadadb.query.OidResultSet#getLong(int)
	 */
	@Override
	public byte getByte(String col_name) throws SQLException {
		return this.real_resultset.getByte(col_name);
	}

	/* (non-Javadoc)
	 * @see saadadb.query.OidResultSet#getLong(int)
	 */
	@Override
	public short getShort(int col_num) throws SQLException {
		return this.real_resultset.getShort(col_num);
	}
	/* (non-Javadoc)
	 * @see saadadb.query.OidResultSet#getLong(int)
	 */
	@Override
	public short getShort(String col_name) throws SQLException {
		return this.real_resultset.getShort(col_name);
	}

	/* (non-Javadoc)
	 * @see saadadb.query.OidResultSet#getLong(int)
	 */
	@Override
	public int getInt(int col_num) throws SQLException {
		return this.real_resultset.getInt(col_num);
	}
	/* (non-Javadoc)
	 * @see saadadb.query.OidResultSet#getLong(int)
	 */
	@Override
	public int getInt(String col_name) throws SQLException {
		return this.real_resultset.getInt(col_name);
	}

	/* (non-Javadoc)
	 * @see saadadb.query.OidResultSet#getLong(int)
	 */
	@Override
	public long getLong(int col_num) throws SQLException {
		return this.real_resultset.getLong(col_num);
	}
	/* (non-Javadoc)
	 * @see saadadb.query.OidResultSet#getLong(java.lang.String)
	 */
	@Override
	public long getLong(String col_name) throws SQLException {
		return this.real_resultset.getLong(col_name);
	}
	/* (non-Javadoc)
	 * @see saadadb.query.OidResultSet#getLong(java.lang.String)
	 */
	@Override
	public float getFloat(String col_name) throws SQLException {
		return this.real_resultset.getFloat(col_name);
	}
	/* (non-Javadoc)
	 * @see saadadb.query.OidResultSet#getLong(int)
	 */
	@Override
	public float getFloat(int col_num) throws SQLException {
		return this.real_resultset.getFloat(col_num);
	}
	/* (non-Javadoc)
	 * @see saadadb.query.OidResultSet#getLong(java.lang.String)
	 */
	@Override
	public double getDouble(String col_name) throws SQLException {
		return this.real_resultset.getDouble(col_name);
	}
	/* (non-Javadoc)
	 * @see saadadb.query.OidResultSet#getLong(int)
	 */
	@Override
	public double getDouble(int col_num) throws SQLException {
		return this.real_resultset.getDouble(col_num);
	}
	/* (non-Javadoc)
	 * @see saadadb.query.OidResultSet#getLong(int)
	 */
	@Override
	public boolean getBoolean(int col_num) throws SQLException {
		return this.real_resultset.getBoolean(col_num);
	}
	/* (non-Javadoc)
	 * @see saadadb.query.OidResultSet#getLong(int)
	 */
	@Override
	public boolean getBoolean(String col_name) throws SQLException {
		return this.real_resultset.getBoolean(col_name);
	}

	/* (non-Javadoc)
	 * @see saadadb.query.OidResultSet#getOid()
	 */
	@Override
	public long getOid() throws SQLException {
		return this.getLong(1);
	}
	/* (non-Javadoc)
	 * @see saadadb.query.OidResultSet#close()
	 */
	@Override
	public void close() throws QueryException {
		if( squery != null ) {
			squery.close();
		}
	}


	/* (non-Javadoc)
	 * @see saadadb.query.OidResultSet#getSize()
	 */
	@Override
	public int getSize() {
		return this.size;

	}

	/* (non-Javadoc)
	 * @see saadadb.query.OidResultSet#setCursor(int)
	 */
	@Override
	public boolean setCursor(int row) {
		try {
			this.real_resultset.first();
			return this.real_resultset.relative(row);

		} catch (SQLException e) {
			Messenger.printStackTrace(e);
			return false;
		}
	}

	public void rewind() {
		try {
			this.real_resultset.beforeFirst();
		} catch (SQLException e) {
			Messenger.printStackTrace(e);
		}
	}

	/**
	 * Force the size to 0 Used by the query engine for query with matchPatterns returning empty sets
	 * That allows to have meta data
	 */
	public void flush() {
		this.size = 0;
	}


}
