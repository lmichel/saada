package saadadb.products;

import java.io.File;

import saadadb.dataloader.mapping.ProductMapping;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.MetaClass;
import saadadb.products.datafile.DataFile;
import saadadb.products.datafile.FooProduct;
import saadadb.util.Messenger;

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
		try {
			this.bindDataFile(dataFile);
			this.setQuantityDetector();
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			IgnoreException.throwNewException(SaadaException.FILE_FORMAT, e);
		}
	}


	/**
	 * @param file
	 * @param mapping
	 * @param metaClass
	 * @throws SaadaException
	 */
	public SpectrumBuilder(DataFile file, ProductMapping mapping, MetaClass metaClass) throws SaadaException{		
		super(file, mapping, metaClass);
		try {
			this.bindDataFile(dataFile);
			this.setQuantityDetector();
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			IgnoreException.throwNewException(SaadaException.FILE_FORMAT, e);
		}
	}
	public SpectrumBuilder(DataFile file, ProductMapping mapping) throws SaadaException{		
		super(file, mapping, null);
		try {
			this.bindDataFile(dataFile);
			this.setQuantityDetector();
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			IgnoreException.throwNewException(SaadaException.FILE_FORMAT, e);
		}
	}
}
