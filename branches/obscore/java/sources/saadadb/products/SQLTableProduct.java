package saadadb.products;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import saadadb.database.Database;
import saadadb.dataloader.mapping.ProductMapping;
import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.products.inference.QuantityDetector;
import saadadb.sqltable.SQLQuery;
import saadadb.util.Messenger;
import saadadb.util.SaadaConstant;

public class SQLTableProduct implements DataFile {
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
	public Object[] getExtrema(String key) throws Exception {
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
	public Map<String, List<AttributeHandler>> getProductMap(int category) throws IgnoreException {
		return null;
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

	@Override
	public Map<String, AttributeHandler> getEntryAttributeHandler()
			throws SaadaException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, AttributeHandler> getAttributeHandlerCopy() throws SaadaException{
		// TODO Auto-generated method stub
		return null;
	}




	@Override
	public QuantityDetector getQuantityDetector(ProductMapping productMapping) throws SaadaException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, DataFileExtension> getProductMap() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void bindBuilder(ProductBuilder builder) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<ExtensionSetter> reportOnLoadedExtension() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCanonicalPath() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAbsolutePath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getParent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long length() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean delete() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<String> getComments() throws SaadaException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateAttributeHandlerValues() throws Exception {
		// TODO Auto-generated method stub
		
	}



}
