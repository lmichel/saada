package saadadb.products.ppknowledge;

import java.util.Map;

import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.products.ColumnSetter;
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
	public ColumnSetter getTargetName() throws SaadaException {
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "getTargetName: read knowledge base");
		return this.getColmumnSetter("OBJECT");
	}
	@Override
	public ColumnSetter getFacilityName() throws SaadaException {
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "getFacilityName: read knowledge base");
		return this.getColmumnSetter("TELESCOP");
	}
	@Override
	public ColumnSetter getInstrumentName() throws SaadaException {
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "getInstrumentName: read knowledge base");
		return this.getColmumnSetter("TELESCOP");
	}

	@Override
	public ColumnSetter getEMin() throws SaadaException {
		double retour = SaadaConstant.DOUBLE;
		try { 
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "getEMin: read knowledge base");
			retour = getValue("RESTFREQ") + getValue("CRVAL1") + (0 - getValue("CRPIX1"))*getValue("CDELT1");
			ColumnSetter cs = new ColumnSetter();
			cs.setUnit("Hz");
			cs.setByWCS(String.valueOf(retour), false);
			cs.completeMessage("Issued from the knowledge base");
			// avoid multiple String<>double conversions
			cs.storedValue = retour;
			return cs;
		} catch (Exception e) {
			return  new ColumnSetter(e.getMessage());
		}
	}
	@Override
	public ColumnSetter getEMax() throws SaadaException {
		double retour = SaadaConstant.DOUBLE;
		try { 
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "getEMax: read knowledge base");
			retour = getValue("RESTFREQ") + getValue("CRVAL1") + (getValue("NAXIS1") - getValue("CRPIX1"))*getValue("CDELT1");
			ColumnSetter cs = new ColumnSetter();
			cs.setUnit("Hz");
			cs.setByWCS(String.valueOf(retour), false);
			cs.completeMessage("Issued from the knowledge base");
			// avoid multiple String<>double conversions
			cs.storedValue = retour;
			return cs;
		} catch (Exception e) {
			return  new ColumnSetter(e.getMessage());
		}
	}
	@Override
	public ColumnSetter getEUnit() throws SaadaException {
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "getEUnit: read knowledge base");
		return new ColumnSetter("Hz", false, "Issued from the knowledge base");
	}
	@Override
	public ColumnSetter getResPower() throws SaadaException {
		double retour = SaadaConstant.DOUBLE;
		try { 			
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "getResPower: read knowledge base");
			retour  = getValue("RESTFREQ") + getValue("CRVAL1") + (getValue("NAXIS1") - getValue("CRPIX1"))*getValue("CDELT1");
			retour += getValue("RESTFREQ") + getValue("CRVAL1") + (0 - getValue("CRPIX1"))*getValue("CDELT1");
			retour /= 2*getValue("CDELT1");
			retour = Math.abs(retour);
			ColumnSetter cs = new ColumnSetter();
			cs.setUnit("");
			cs.setByWCS(String.valueOf(retour), false);
			cs.completeMessage("Issued from the knowledge base F/CDELT");
			// avoid multiple String<>double conversions
			cs.storedValue = retour;
			return cs;
		} catch (Exception e) {
			return  new ColumnSetter(e.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see saadadb.products.ppknowledge.PipelineParser#getTMin()
	 */
	@Override
	public ColumnSetter getTMin() throws SaadaException {
		try { 
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "getTMin: read knowledge base");
			ColumnSetter cs = new ColumnSetter();
			cs.setByWCS(getStringValue("DATE-OBS"), false);
			cs.completeMessage("Issued from the knowledge base");
			return cs;
		} catch (Exception e) {
			return  new ColumnSetter(e.getMessage());
		}
	}
	
	/* (non-Javadoc)
	 * @see saadadb.products.ppknowledge.PipelineParser#getUnitName()
	 */
	@Override
	public ColumnSetter getUnitName() throws SaadaException {
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "getUcdName: read knowledge base");
		ColumnSetter cs = new ColumnSetter();
		cs.setByValue("count", false);
		cs.completeMessage("Issued from the knowledge base");
		return cs;
	}
	/* (non-Javadoc)
	 * @see saadadb.products.ppknowledge.PipelineParser#getUcdName()
	 */
	@Override
	public ColumnSetter getUcdName() throws SaadaException {
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "getUcdName: read knowledge base");
		ColumnSetter cs = new ColumnSetter();
		cs.setByValue("phot.count", false);
		cs.completeMessage("Issued from the knowledge base");
		return cs;
	}
	/* (non-Javadoc)
	 * @see saadadb.products.ppknowledge.PipelineParser#getCalibStatus()
	 */
	@Override
	public ColumnSetter getCalibStatus() throws SaadaException {
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "getCalibStatus: read knowledge base");
		ColumnSetter cs = new ColumnSetter();
		cs.setByValue("2", false);
		cs.completeMessage("Issued from the knowledge base");
		return cs;
	}
}
