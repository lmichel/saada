package saadadb.products;

import java.io.File;

import saadadb.dataloader.mapping.ProductMapping;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.MetaClass;
import saadadb.products.datafile.DataFile;
import saadadb.products.datafile.FooProduct;

/**
 * @author michel
 * @version $Id$
 */
public class SpectrumBuilder extends ProductBuilder {

	/**
	 * @param productFile
	 * @param conf
	 * @param metaClass
	 * @throws SaadaException
	 */
	public SpectrumBuilder(FooProduct productFile, ProductMapping conf, MetaClass metaClass) throws SaadaException{	
		super(productFile, conf, metaClass);
	}


	/**
	 * @param file
	 * @param mapping
	 * @param metaClass
	 * @throws SaadaException
	 */
	public SpectrumBuilder(DataFile file, ProductMapping mapping, MetaClass metaClass) throws SaadaException{		
		super(file, mapping, metaClass);
	}
	public SpectrumBuilder(DataFile file, ProductMapping mapping) throws SaadaException{		
		super(file, mapping, null);
	}
}
