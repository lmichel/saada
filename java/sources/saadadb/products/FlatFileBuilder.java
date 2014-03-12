package saadadb.products;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;

import nom.tam.fits.FitsException;
import saadadb.collection.SaadaOID;
import saadadb.collection.obscoremin.SaadaInstance;
import saadadb.dataloader.mapping.ProductMapping;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.SaadaException;
import saadadb.generationclass.SaadaClassReloader;
import saadadb.meta.AttributeHandler;
import saadadb.meta.MetaClass;
import saadadb.util.Messenger;

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
