package saadadb.products;

import java.io.File;

import saadadb.collection.obscoremin.SaadaInstance;
import saadadb.dataloader.mapping.ProductMapping;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.FatalException;
import saadadb.meta.MetaClass;

/** 
 * @version $Id$
 * 
 */
public class FlatFileBuilder extends ProductBuilder {

	public FlatFileBuilder(File file, ProductMapping conf) throws FatalException {
		super(file, conf);
	}
	
	/* (non-Javadoc)
	 * @see saadadb.products.ProductBuilder#setMetaclass(saadadb.meta.MetaClass)
	 */
	public void setMetaclass(MetaClass mc) {
		metaclass = null;
	}

	
	/**
	 * Used by FLatFileMapper to load flafiles by burst using a single instance of the this class
	 * @param si
	 * @throws Exception 
	 * @throws AbortException 
	 */
	public void bindInstanceToFile(SaadaInstance si, File file) throws Exception {
		this.file = file;
		this.productFile = new AnyFile(this);
		if( this.productIngestor == null ){
			this.productIngestor = new ProductIngestor(this);
		}
		this.productIngestor.bindInstanceToFile(si);
	}
	

}
