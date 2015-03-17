package saadadb.products;

import java.io.File;

import saadadb.collection.Category;
import saadadb.collection.obscoremin.SaadaInstance;
import saadadb.dataloader.mapping.ProductMapping;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.MetaClass;
import saadadb.products.datafile.DataFile;
import saadadb.products.datafile.JsonDataFile;

/** 
 * @version $Id$
 * 
 */
public class FlatFileBuilder extends ProductBuilder {

	public FlatFileBuilder(JsonDataFile productFile, ProductMapping conf) throws SaadaException{	
		super(productFile, conf, null);
	}

	public FlatFileBuilder(DataFile file, ProductMapping conf) throws SaadaException {
		super(file, conf, null);
	}
	
	/* (non-Javadoc)
	 * @see saadadb.products.ProductBuilder#setMetaclass(saadadb.meta.MetaClass)
	 */
	public void setMetaclass(MetaClass mc) {
		metaClass = null;
	}

	
	/**
	 * Used by FLatFileMapper to load flafiles by burst using a single instance of the this class
	 * @param si
	 * @throws Exception 
	 * @throws AbortException 
	 */
	public void bindInstanceToFile(SaadaInstance si, DataFile file) throws Exception {
		this.dataFile = file;
		if( this.productIngestor == null ){
			this.productIngestor = new ProductIngestor(this);
		}
		this.productIngestor.bindInstanceToFile();
	}
	

	/* (non-Javadoc)
	 * @see saadadb.products.ProductBuilder#getCategory()
	 */
	public int getCategory(){
		return Category.FLATFILE;
	}

}
