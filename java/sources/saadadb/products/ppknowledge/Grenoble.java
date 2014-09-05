package saadadb.products.ppknowledge;

import java.util.Map;

import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.products.setter.ColumnExpressionSetter;
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
	public ColumnExpressionSetter getTargetName() throws SaadaException {
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "getTargetName: read knowledge base");
		return this.getColmumnSetter("OBJECT");
	}
	@Override
	public ColumnExpressionSetter getFacilityName() throws SaadaException {
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "getFacilityName: read knowledge base");
		return this.getColmumnSetter("TELESCOP");
	}
	@Override
	public ColumnExpressionSetter getInstrumentName() throws SaadaException {
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "getInstrumentName: read knowledge base");
		return this.getColmumnSetter("TELESCOP");
	}

	@Override
	public ColumnExpressionSetter getEMin() throws Exception {
		double retour = SaadaConstant.DOUBLE;
		try { 
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "getEMin: read knowledge base");
			retour = getValue("RESTFREQ") + getValue("CRVAL1") + (0 - getValue("CRPIX1"))*getValue("CDELT1");
			ColumnExpressionSetter cs = new ColumnExpressionSetter("em_min");
			cs.setUnit("Hz");
			cs.setByWCS(String.valueOf(retour), false);
			cs.completeMessage("Issued from the knowledge base");
			// avoid multiple String<>double conversions
			cs.storedValue = retour;
			return cs;
		} catch (Exception e) {
			return  new ColumnExpressionSetter("em_min", e.getMessage());
		}
	}
	@Override
	public ColumnExpressionSetter getEMax() throws Exception {
		double retour = SaadaConstant.DOUBLE;
		try { 
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "getEMax: read knowledge base");
			retour = getValue("RESTFREQ") + getValue("CRVAL1") + (getValue("NAXIS1") - getValue("CRPIX1"))*getValue("CDELT1");
			ColumnExpressionSetter cs = new ColumnExpressionSetter("em_max");
			cs.setUnit("Hz");
			cs.setByWCS(String.valueOf(retour), false);
			cs.completeMessage("Issued from the knowledge base");
			// avoid multiple String<>double conversions
			cs.storedValue = retour;
			return cs;
		} catch (Exception e) {
			return  new ColumnExpressionSetter("em_max", e.getMessage());
		}
	}
	@Override
	public ColumnExpressionSetter getEUnit() throws Exception {
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "getEUnit: read knowledge base");
		//return new ColumnExpressionSetter("Hz", false, "Issued from the knowledge base");
		return new ColumnExpressionSetter("Hz");

	}
	@Override
	public ColumnExpressionSetter getResPower() throws Exception {
		double retour = SaadaConstant.DOUBLE;
		try { 			
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "getResPower: read knowledge base");
			retour  = getValue("RESTFREQ") + getValue("CRVAL1") + (getValue("NAXIS1") - getValue("CRPIX1"))*getValue("CDELT1");
			retour += getValue("RESTFREQ") + getValue("CRVAL1") + (0 - getValue("CRPIX1"))*getValue("CDELT1");
			retour /= 2*getValue("CDELT1");
			retour = Math.abs(retour);
			ColumnExpressionSetter cs = new ColumnExpressionSetter("em_res_power");
			cs.setUnit("");
			cs.setByWCS(String.valueOf(retour), false);
			cs.completeMessage("Issued from the knowledge base F/CDELT");
			// avoid multiple String<>double conversions
			cs.storedValue = retour;
			return cs;
		} catch (Exception e) {
			return  new ColumnExpressionSetter("em_res_power", e.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see saadadb.products.ppknowledge.PipelineParser#getTMin()
	 */
	@Override
	public ColumnExpressionSetter getTMin() throws Exception {
		try { 
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "getTMin: read knowledge base");
			ColumnExpressionSetter cs = new ColumnExpressionSetter("t_min");
			cs.setByWCS(getStringValue("DATE-OBS"), false);
			cs.completeMessage("Issued from the knowledge base");
			return cs;
		} catch (Exception e) {
			return  new ColumnExpressionSetter("t_min", e.getMessage());
		}
	}
	
	/* (non-Javadoc)
	 * @see saadadb.products.ppknowledge.PipelineParser#getUnitName()
	 */
	@Override
	public ColumnExpressionSetter getUnitName() throws SaadaException {
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "getUcdName: read knowledge base");
		ColumnExpressionSetter cs = new ColumnExpressionSetter("t_max");
		cs.setByValue("count", false);
		cs.completeMessage("Issued from the knowledge base");
		return cs;
	}
	/* (non-Javadoc)
	 * @see saadadb.products.ppknowledge.PipelineParser#getUcdName()
	 */
	@Override
	public ColumnExpressionSetter getUcdName() throws SaadaException {
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "getUcdName: read knowledge base");
		ColumnExpressionSetter cs = new ColumnExpressionSetter("o_ucd");
		cs.setByValue("phot.count", false);
		cs.completeMessage("Issued from the knowledge base");
		return cs;
	}
	/* (non-Javadoc)
	 * @see saadadb.products.ppknowledge.PipelineParser#getCalibStatus()
	 */
	@Override
	public ColumnExpressionSetter getCalibStatus() throws SaadaException {
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "getCalibStatus: read knowledge base");
		ColumnExpressionSetter cs = new ColumnExpressionSetter("o_calib_status");
		cs.setByValue("2", false);
		cs.completeMessage("Issued from the knowledge base");
		return cs;
	}
}
