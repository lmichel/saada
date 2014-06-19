package saadadb.products.inference;

import java.util.Map;

import saadadb.exceptions.FatalException;
import saadadb.meta.AttributeHandler;
import saadadb.products.ColumnSetter;
import saadadb.util.Messenger;

/**

 * @author michel
 * @version $Id$
 */
public class ObservableKWDetector extends KWDetector {

	public ObservableKWDetector(Map<String, AttributeHandler> tableAttributeHandler) {
		super(tableAttributeHandler);
	}
	public ObservableKWDetector(Map<String, AttributeHandler> tableAttributeHandler, Map<String, AttributeHandler> entryAttributeHandler) {
		super(tableAttributeHandler, entryAttributeHandler);
	}

	public ColumnSetter getUcdName() throws FatalException{
		if( Messenger.debug_mode ) 
			Messenger.printMsg(Messenger.DEBUG, "Search for the Observable UCD");
		return new ColumnSetter();
	}
	public ColumnSetter getUnitName() throws FatalException{
		if( Messenger.debug_mode ) 
			Messenger.printMsg(Messenger.DEBUG, "Search for the Observable UCD");
		return new ColumnSetter();
	}
	public ColumnSetter getCalibStatus() throws FatalException{
//		Level 0: Raw instrumental data, in a proprietary or internal data-provider defined format, that needs instrument specific tools to be handled.
//		Level 1: Instrumental data in a standard format (FITS, VOTable, SDFITS, ASDM, etc.) which could be manipulated with standard astronomical packages.
//		Level 2: Calibrated, science ready data with the instrument signature removed.
//		Level 3: Enhanced data products like mosaics, resampled or drizzled images, or heavily processed survey fields. Level 3 data products may represent the combination of data from multiple primary observations.		
		if( Messenger.debug_mode ) 
			Messenger.printMsg(Messenger.DEBUG, "Search for the Observable calib status");
		return new ColumnSetter();
	}

}
