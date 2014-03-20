package saadadb.products;

import java.io.File;

import saadadb.dataloader.mapping.ProductMapping;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.products.validation.FooProduct;

/**This class redefines method specific in miscs during their collection load.
 *@author Millan Patrick
 *@version 2.0
 *@since 2.0
 */
public class MiscBuilder extends ProductBuilder{
	private static final long serialVersionUID = 1L;

						/* ######################################################
						 * 
						 * ATTRIBUTES, CONSTRUCTOR AND METHODE FOR THE NEW LOADER
						 * 
						 *#######################################################*/

	public MiscBuilder(File file, ProductMapping mapping) throws FatalException{	
		super(file, mapping);
	}
	
	public MiscBuilder(FooProduct productFile, ProductMapping conf) throws SaadaException{	
		super(productFile, conf);
	}

}

