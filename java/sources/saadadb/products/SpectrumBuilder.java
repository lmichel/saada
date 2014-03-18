package saadadb.products;

import java.io.File;

import saadadb.dataloader.mapping.ProductMapping;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.SaadaException;

/**
 * @author michel
 * @version $Id$
 */
public class SpectrumBuilder extends ProductBuilder {
	/**
	 * @param fileName
	 * @throws AbortException 
	 * @throws SaadaException 
	 */
	public SpectrumBuilder(File file, ProductMapping mapping) throws SaadaException{		
		super(file, mapping);
	}
}
