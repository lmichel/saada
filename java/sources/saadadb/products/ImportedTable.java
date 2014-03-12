package saadadb.products;

import saadadb.dataloader.mapping.ProductMapping;
import saadadb.exceptions.SaadaException;

public class ImportedTable extends ProductBuilder {
	private String tableName;

	public ImportedTable(String tableName, ProductMapping mapping) throws Exception{	
		super(null, mapping);
		this.tableName = tableName;
		this.mapping = mapping;
		this.productFile = new SQLTableProduct(tableName);				
	}
	
	/**
	 * @throws SaadaException 
	 * 
	 */
	protected void mapCollectionAttributes() throws SaadaException {
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
