package saadadb.products;

import java.io.File;

import saadadb.dataloader.mapping.ProductMapping;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.MetaClass;
import saadadb.products.datafile.DataFile;
import saadadb.products.datafile.JsonDataFile;
import saadadb.util.Messenger;

/**This class redefines method specific in miscs during their collection load.
 *@author Millan Patrick
 *@version 2.0
 *@since 2.0
 */
public class MiscBuilder extends ProductBuilder{
	private static final long serialVersionUID = 1L;

	public MiscBuilder(DataFile file, ProductMapping mapping, MetaClass metaClass) throws SaadaException{	
		super(file, mapping, metaClass );
		try {
			this.mapDataFile(dataFile);
			this.dataFile.mapAttributeHandler();
			this.setQuantityDetector();
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			IgnoreException.throwNewException(SaadaException.FILE_FORMAT, e);
		}
	}
	
	public MiscBuilder(DataFile file, ProductMapping mapping) throws SaadaException{	
		super(file, mapping, null );
		try {
			this.mapDataFile(dataFile);
			this.setQuantityDetector();
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			IgnoreException.throwNewException(SaadaException.FILE_FORMAT, e);
		}
	}
	
   public MiscBuilder(JsonDataFile productFile, ProductMapping conf, MetaClass metaClass) throws SaadaException{	
		super(productFile, conf, metaClass);
		try {
			this.bindDataFile(dataFile);
			this.setQuantityDetector();
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			IgnoreException.throwNewException(SaadaException.FILE_FORMAT, e);
		}
	}

}

