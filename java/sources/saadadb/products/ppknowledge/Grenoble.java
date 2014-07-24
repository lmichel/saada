package saadadb.products.ppknowledge;

import java.util.Map;

import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.products.setter.ColumnSingleSetter;
import saadadb.util.Messenger;
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

	@Override
	public ColumnSingleSetter getTargetName() throws SaadaException {
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "getTargetName: read knowledge base");
		return this.getColmumnSetter("OBJECT");
	}
	@Override
	public ColumnSingleSetter getFacilityName() throws SaadaException {
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "getFacilityName: read knowledge base");
		return this.getColmumnSetter("TELESCOP");
	}
	@Override
	public ColumnSingleSetter getInstrumentName() throws SaadaException {
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "getInstrumentName: read knowledge base");
		return this.getColmumnSetter("TELESCOP");
	}

	@Override
	public ColumnSingleSetter getEMin() throws SaadaException {
		double retour = SaadaConstant.DOUBLE;
		try { 
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "getEMin: read knowledge base");
			retour = getValue("RESTFREQ") + getValue("CRVAL1") + (0 - getValue("CRPIX1"))*getValue("CDELT1");
			ColumnSingleSetter cs = new ColumnSingleSetter();
			cs.setUnit("Hz");
			cs.setByWCS(String.valueOf(retour), false);
			cs.completeMessage("Issued from the knowledge base");
			// avoid multiple String<>double conversions
			cs.storedValue = retour;
			return cs;
		} catch (Exception e) {
			return  new ColumnSingleSetter(e.getMessage());
		}
	}
	@Override
	public ColumnSingleSetter getEMax() throws SaadaException {
		double retour = SaadaConstant.DOUBLE;
		try { 
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "getEMax: read knowledge base");
			retour = getValue("RESTFREQ") + getValue("CRVAL1") + (getValue("NAXIS1") - getValue("CRPIX1"))*getValue("CDELT1");
			ColumnSingleSetter cs = new ColumnSingleSetter();
			cs.setUnit("Hz");
			cs.setByWCS(String.valueOf(retour), false);
			cs.completeMessage("Issued from the knowledge base");
			// avoid multiple String<>double conversions
			cs.storedValue = retour;
			return cs;
		} catch (Exception e) {
			return  new ColumnSingleSetter(e.getMessage());
		}
	}
	@Override
	public ColumnSingleSetter getEUnit() throws SaadaException {
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "getEUnit: read knowledge base");
		return new ColumnSingleSetter("Hz", false, "Issued from the knowledge base");
	}
	@Override
	public ColumnSingleSetter getResPower() throws SaadaException {
		double retour = SaadaConstant.DOUBLE;
		try { 			
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "getResPower: read knowledge base");
			retour  = getValue("RESTFREQ") + getValue("CRVAL1") + (getValue("NAXIS1") - getValue("CRPIX1"))*getValue("CDELT1");
			retour += getValue("RESTFREQ") + getValue("CRVAL1") + (0 - getValue("CRPIX1"))*getValue("CDELT1");
			retour /= 2*getValue("CDELT1");
			retour = Math.abs(retour);
			ColumnSingleSetter cs = new ColumnSingleSetter();
			cs.setUnit("");
			cs.setByWCS(String.valueOf(retour), false);
			cs.completeMessage("Issued from the knowledge base F/CDELT");
			// avoid multiple String<>double conversions
			cs.storedValue = retour;
			return cs;
		} catch (Exception e) {
			return  new ColumnSingleSetter(e.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see saadadb.products.ppknowledge.PipelineParser#getTMin()
	 */
	@Override
	public ColumnSingleSetter getTMin() throws SaadaException {
		try { 
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "getTMin: read knowledge base");
			ColumnSingleSetter cs = new ColumnSingleSetter();
			cs.setByWCS(getStringValue("DATE-OBS"), false);
			cs.completeMessage("Issued from the knowledge base");
			return cs;
		} catch (Exception e) {
			return  new ColumnSingleSetter(e.getMessage());
		}
	}
	
	/* (non-Javadoc)
	 * @see saadadb.products.ppknowledge.PipelineParser#getUnitName()
	 */
	@Override
	public ColumnSingleSetter getUnitName() throws SaadaException {
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "getUcdName: read knowledge base");
		ColumnSingleSetter cs = new ColumnSingleSetter();
		cs.setByValue("count", false);
		cs.completeMessage("Issued from the knowledge base");
		return cs;
	}
	/* (non-Javadoc)
	 * @see saadadb.products.ppknowledge.PipelineParser#getUcdName()
	 */
	@Override
	public ColumnSingleSetter getUcdName() throws SaadaException {
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "getUcdName: read knowledge base");
		ColumnSingleSetter cs = new ColumnSingleSetter();
		cs.setByValue("phot.count", false);
		cs.completeMessage("Issued from the knowledge base");
		return cs;
	}
	/* (non-Javadoc)
	 * @see saadadb.products.ppknowledge.PipelineParser#getCalibStatus()
	 */
	@Override
	public ColumnSingleSetter getCalibStatus() throws SaadaException {
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "getCalibStatus: read knowledge base");
		ColumnSingleSetter cs = new ColumnSingleSetter();
		cs.setByValue("2", false);
		cs.completeMessage("Issued from the knowledge base");
		return cs;
	}
}
