package saadadb.products;

import java.io.File;

import saadadb.dataloader.mapping.ProductMapping;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.products.validation.FooProduct;

/**
 * @author michel
 * @version $Id$
 */
public class SpectrumBuilder extends ProductBuilder {

	public SpectrumBuilder(FooProduct productFile, ProductMapping conf) throws SaadaException{	
		super(productFile, conf);
	}

	/**
	 * @param fileName
	 * @throws AbortException 
	 * @throws SaadaException 
	 */
	public SpectrumBuilder(DataFile file, ProductMapping mapping) throws SaadaException{		
		super(file, mapping);
	}
}
