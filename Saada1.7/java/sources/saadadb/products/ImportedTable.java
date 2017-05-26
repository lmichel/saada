package saadadb.products;

import saadadb.prdconfiguration.ConfigurationDefaultHandler;

public class ImportedTable extends Product {
	private String tableName;

	public ImportedTable(String tableName, ConfigurationDefaultHandler conf) throws Exception{	
		super(null, conf);
		this.tableName = tableName;
		this.configuration = conf;
		this.productFile = new SQLTableProduct(tableName);				
	}
	
	/**
	 * 
	 */
	protected void mapCollectionAttributes() {
		this.mapInstanceName();
		this.mapIgnoredAndExtendedAttributes();
	}
	/* 
	 * Does nothing for tables
	 * (non-Javadoc)
	 * @see saadadb.products.Product#setAstrofFrame()
	 */
	public void setAstrofFrame() {
	}
	

	/* 
	 * 	Does nothing for tables
	 * (non-Javadoc)
	 * @see saadadb.products.Product#setPositionFields(int)
	 */
	public void setPositionFields(int line) {	
	}

}
