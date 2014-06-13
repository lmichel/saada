package saadadb.products.ppknowledge;

import java.util.Map;

import saadadb.enums.ColumnSetMode;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.products.ColumnSetter;
import saadadb.util.SaadaConstant;

/**
 * Fits file built by the IRAM pipeline
 * http://www.iram.fr/IRAMFR/GILDAS/doc/html/class-html/node82.html
 * 
 * @author michel
 * @version $Id$
 */
public class Grenoble extends PipelineParser {

	public Grenoble(Map<String, AttributeHandler> attributeHandlers) {
		super(attributeHandlers);
	}

	public ColumnSetter getTargetName() throws SaadaException {
		AttributeHandler ah = this.getAttributeHandler("OBJECT");
		return (ah == null)? new ColumnSetter(): new ColumnSetter(ah, ColumnSetMode.BY_KEYWORD);
	}
	public ColumnSetter getFacilityName() throws SaadaException {
		AttributeHandler ah = this.getAttributeHandler("TELESCOP");
		return (ah == null)? new ColumnSetter(): new ColumnSetter(ah, ColumnSetMode.BY_KEYWORD);
	}
	public ColumnSetter getInstrumentName() throws SaadaException {
		AttributeHandler ah = this.getAttributeHandler("TELESCOP");
		return (ah == null)? new ColumnSetter(): new ColumnSetter(ah, ColumnSetMode.BY_KEYWORD);
	}

	public ColumnSetter getEMin() throws SaadaException {
		double retour = SaadaConstant.DOUBLE;
		try { 
			retour = getValue("RESTFREQ") + getValue("CRVAL1") + (0 - getValue("CRPIX1"))*getValue("CDELT1");
			return new ColumnSetter(String.valueOf(retour), false, "Computed by the knowledge base");
		} catch (Exception e) {
			return  new ColumnSetter(e.getMessage());
		}
	}
	public ColumnSetter getEMax() throws SaadaException {
		return new ColumnSetter();
	}
	public ColumnSetter getEUnit() throws SaadaException {
		return new ColumnSetter("Hz", false);
	}

}
