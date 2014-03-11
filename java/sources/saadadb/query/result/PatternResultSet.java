package saadadb.query.result;

import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Set;

import saadadb.meta.AttributeHandler;
import saadadb.relationship.KeySet;
import saadadb.util.SaadaConstant;

public class PatternResultSet extends OidResultSet {
	/*
	 * Can't require up to 8time the datasize!!
	 * there is something to do (e.g ordering and replacing with dichoto access like KeyIndex) 
	 */
	private KeySet      oidPattern;
	//private Iterator<Long> oidIterator;
	private int position=0;
	private long current_oid;
	
	/** * @version $Id$

	 * @param oidPattern
	 * @throws SQLException 
	 */
	public PatternResultSet(KeySet oidPattern) throws SQLException {
		super(-1);
		this.oidPattern = oidPattern;
		position=0;
//		this.oidIterator = oidPattern.iterator();
	}
	/**
	 * @param oidPattern
	 * @throws SQLException 
	 */
	public PatternResultSet(Set<Long> oidPattern) throws SQLException {
		super(-1);
		this.oidPattern = new KeySet(oidPattern);
		position=0;
//		this.oidIterator = oidPattern.iterator();
	}

	/* (non-Javadoc)
	 * @see saadadb.query.OidResultSet#next()
	 */
	public boolean next() {
		if( position < oidPattern.getSize() ) {
			current_oid = oidPattern.getKey(position);
			position++;
			return true;
		}
		else {
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see saadadb.query.OidResultSet#getShort(int)
	 */
	public char getChar(int col_num)throws SQLException {
		return SaadaConstant.CHAR;
	}
	/* (non-Javadoc)
	 * @see saadadb.query.OidResultSet#getShort(java.lang.String)
	 */
	public char getChar(String col_name)throws SQLException{
		return SaadaConstant.CHAR;
	}
	/* (non-Javadoc)
	 * @see saadadb.query.OidResultSet#getShort(int)
	 */
	public byte getByte(int col_num)throws SQLException {
		return SaadaConstant.BYTE;
	}
	/* (non-Javadoc)
	 * @see saadadb.query.OidResultSet#getShort(java.lang.String)
	 */
	public byte getByte(String col_name)throws SQLException{
		return SaadaConstant.BYTE;
	}
	/* (non-Javadoc)
	 * @see saadadb.query.OidResultSet#getShort(int)
	 */
	public short getShort(int col_num)throws SQLException {
		return SaadaConstant.SHORT;
	}
	/* (non-Javadoc)
	 * @see saadadb.query.OidResultSet#getShort(java.lang.String)
	 */
	public short getShort(String col_name)throws SQLException{
		return SaadaConstant.SHORT;
	}

	/* (non-Javadoc)
	 * @see saadadb.query.OidResultSet#getInt(int)
	 */
	public int getInt(int col_num)throws SQLException{
		return SaadaConstant.INT;
	}	
	/* (non-Javadoc)
	 * @see saadadb.query.OidResultSet#getInt(java.lang.String)
	 */
	public int getInt(String col_name)throws SQLException{
		return SaadaConstant.INT;
	}
	/* (non-Javadoc)
	 * @see saadadb.query.OidResultSet#getInt(int)
	 */
	public float getFloat(int col_num)throws SQLException{
		return SaadaConstant.FLOAT;
	}	
	/* (non-Javadoc)
	 * @see saadadb.query.OidResultSet#getInt(java.lang.String)
	 */
	public float getFloat(String col_name)throws SQLException{
		return SaadaConstant.FLOAT;
	}
	/* (non-Javadoc)
	 * @see saadadb.query.OidResultSet#getInt(int)
	 */
	public double getDouble(int col_num)throws SQLException{
		return SaadaConstant.DOUBLE;
	}	
	/* (non-Javadoc)
	 * @see saadadb.query.OidResultSet#getInt(java.lang.String)
	 */
	public double getDouble(String col_name)throws SQLException{
		return SaadaConstant.DOUBLE;
	}
	/* (non-Javadoc)
	 * @see saadadb.query.OidResultSet#getInt(int)
	 */
	public boolean getBoolean(int col_num)throws SQLException{
		return false;
	}	
	/* (non-Javadoc)
	 * @see saadadb.query.OidResultSet#getInt(java.lang.String)
	 */
	public boolean getBoolean(String col_name)throws SQLException{
		return false;
	}

	/* (non-Javadoc)
	 * @see saadadb.query.OidResultSet#getObject(int)
	 */
	public Object getObject(int col_num) {
		if( col_num == 1 ) {
			return current_oid;
		}
		else {
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see saadadb.query.OidResultSet#getObject(java.lang.String)
	 */
	public Object getObject(String col_name) {
		if( col_name.equals("oidsaada") ) {
			return current_oid;
		}
		else {
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see saadadb.query.OidResultSet#getLong(int)
	 */
	public long getLong(int col_num) {
		if( col_num == 1 ) {
			return current_oid;
		}
		else {
			return SaadaConstant.LONG;
		}
	}

	/* (non-Javadoc)
	 * @see saadadb.query.OidResultSet#getLong(java.lang.String)
	 */
	public long getLong(String col_name) {
		if( col_name.equals("oidsaada") ) {
			return current_oid;
		}
		else {
			return SaadaConstant.LONG;
		}
	}

	/* (non-Javadoc)
	 * @see saadadb.query.OidResultSet#getOid()
	 */
	public long getOid() {
		return this.getLong(1);
	}


	/**
	 * @param long1
	 * @return
	 */
	public boolean contains(long long1) {
		return this.oidPattern.contains(long1);
	}

	/* (non-Javadoc)
	 * @see saadadb.query.OidResultSet#close()
	 */
	public void close() {
	}

	/* (non-Javadoc)
	 * @see saadadb.query.OidResultSet#getSize()
	 */
	public int getSize() {
		return oidPattern.getSize();
	}

	/* (non-Javadoc)
	 * @see saadadb.query.OidResultSet#setCursor(int)
	 */
	public boolean setCursor(int row) {
		if( row < 0 || row >= oidPattern.getSize()) {
			return false;
		}
		else {
			position = row;
			this.current_oid =  oidPattern.getKey(position);	
			return true;
		}
	}
	
	/**
	 * re-init the iterator
	 */
	public void rewind() {
		position = 0;
	}
	@Override
	protected void computeSize() throws SQLException {
		if( oidPattern == null ) {
			this.size = 0;
		}
		else {
			this.size =  oidPattern.getSize();
		}
		
	}
	@Override
	public Set<AttributeHandler> getMeta() {
		AttributeHandler ah = new AttributeHandler();
		ah.setNameattr("oidsaada");
		ah.setNameorg("oidsaada");
		ah.setType("long");
		LinkedHashSet<AttributeHandler> lah = new LinkedHashSet<AttributeHandler>();
		lah.add(ah);
		col_names = new SaadaQLMetaSet(lah, null);
		return null;
	}

	
}
