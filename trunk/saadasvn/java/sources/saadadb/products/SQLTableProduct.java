package saadadb.products;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import saadadb.database.Database;
import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.sqltable.SQLQuery;
import saadadb.util.Messenger;
import saadadb.util.SaadaConstant;

public class SQLTableProduct implements ProductFile {
	private SQLQuery query;
	private ResultSet resultSet;
	private String name;
	private Map<String, AttributeHandler> attributeHandlers;
	private Map<String, ArrayList<AttributeHandler>> productMap; 
	private int nbCols;

	/**
	 * @param name
	 * @throws Exception
	 */
	SQLTableProduct(String name) throws Exception{
//		ResultSet rs = Database.getWrapper().getTableColumns(name);
//		this.nbCols = rs.getMetaData().getColumnCount();
//		this.attributeHandlers = new LinkedHashMap<String, AttributeHandler>();
//		this.productMap = new LinkedHashMap<String, ArrayList<AttributeHandler>>();
//		ArrayList<AttributeHandler> ahl = new ArrayList<AttributeHandler>();
//
//		while( rs.next()) {
//			AttributeHandler ah = new AttributeHandler();
//			ah.setNameorg(rs.getString("COLUMN_NAME"));
//			if( ah.getNameorg().matches(".*saada.*")) {
//				//IgnoreException.throwNewException(SaadaException.FILE_FORMAT, "Thae table " + name + " seems to be a Saada table, ti cannot be imported");
//			}
//			ah.setNameattr(rs.getString("COLUMN_NAME").toLowerCase());
//			ah.setType(Database.getWrapper().getJavaTypeFromSQL(rs.getString("TYPE_NAME") ));
//			ah.setComment("Read from SQL table");
//			this.attributeHandlers.put(ah.getNameattr(), ah);
//			ahl.add(ah);
//		}
//		this.productMap.put("SQL table " + name, ahl);
//		this.name = name;
//		this.query = new SQLQuery("SELECT * FROM " + name);
//		this.resultSet = this.query.run();
	}

	@Override
	public boolean hasMoreElements() {
		try {
			return !resultSet.isLast();
		} catch (SQLException e) {
			Messenger.printStackTrace(e);
			return false;
		}
	}

	@Override
	public Object nextElement() {
		try {
			if( resultSet.next() ) {
				ArrayList<Object> retour = new ArrayList<Object>();
				for( int i=1 ; i<= this.nbCols ; i++ ){
					retour.add(this.resultSet.getObject(i));
				}
				return retour;
			}
			return null;
		} catch (SQLException e) {
			Messenger.printStackTrace(e);
			return null;
		}
	}

	@Override
	public String getKWValueQuickly(String key) {
		int cpt = 0;
		for( AttributeHandler ah: this.attributeHandlers.values()) {
			cpt++;
			if( ah.getNameattr().equals(key) || ah.getNameorg().equals(key)){
				try {
					return this.resultSet.getObject(cpt).toString();
				} catch (SQLException e) {
					Messenger.printStackTrace(e);
				}
			}
		}
		return null;
	}

	@Override
	public void setKWEntry(LinkedHashMap<String, AttributeHandler> tah)
	throws IgnoreException {
	}

	@Override
	public double[] getExtrema(String key) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getNRows() throws IgnoreException {
		return SaadaConstant.INT;
	}

	@Override
	public int getNCols() throws IgnoreException {
		return this.nbCols;
	}

	@Override
	public void initEnumeration() throws IgnoreException {		
		try {
			this.query.close();
			this.query = new SQLQuery("SELECT * FROM " + name);
			this.resultSet = this.query.run();
		} catch (QueryException e) {
			IgnoreException.throwNewException(SaadaException.DB_ERROR, e);
		}

	}

	@Override
	public Map<String, ArrayList<AttributeHandler>> getProductMap(String category)
	throws IgnoreException {
		return productMap;
	}

	@Override
	public SpaceFrame getSpaceFrame() {
		return null;
	}

	@Override
	public void setSpaceFrameForTable() throws IgnoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setSpaceFrame() {
		// TODO Auto-generated method stub

	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void closeStream() throws QueryException {
		this.query.close();
	}

	public static void main(String[] args) throws Exception {
		Database.init("ThreeXMM");
		SQLTableProduct stp = new SQLTableProduct("MissionLogEntry");
		int cpt = 0;
		while( stp.hasMoreElements() ){
			System.out.println((cpt++) + " " + stp.nextElement());
			System.out.println(stp.getKWValueQuickly("oidsaada"));
		}
		Database.close();
	}

}
