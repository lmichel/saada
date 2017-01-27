package saadadb.products.ppknowledge;

import java.util.Map;

import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.products.setter.ColumnExpressionSetter;
import saadadb.util.Messenger;
import saadadb.util.SaadaConstant;

/**
 * Fits file built by the GEMINI/GMOS pipeline
 * 
 * @author michel
 * @version $Id$
 */
public class GMOS extends PipelineParser {

	public GMOS(Map<String, AttributeHandler> attributeHandlers) {
		super(attributeHandlers);
	}

	@Override
	public ColumnExpressionSetter getTargetName() throws SaadaException {
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "getTargetName: read knowledge base");
		return this.getColmumnSetter("OBJECT");
	}
	@Override
	public ColumnExpressionSetter getCalibLevel() throws Exception {
		int retour = SaadaConstant.INT;
		try { 
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "getCalibLevel: read knowledge base");
			retour = 3;
			ColumnExpressionSetter cs = new ColumnExpressionSetter("calib_level");
			cs.setByWCS(String.valueOf(retour), false);
			cs.completeDetectionMsg("Issued from the knowledge base");
			cs.storedValue = 3;
			return cs;
		} catch (Exception e) {
			return  new ColumnExpressionSetter("calib_level", e.getMessage());
		}
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
		return this.getColmumnSetter("INSTRUME");
	}

	@Override
	public ColumnExpressionSetter getEMin() throws Exception {
		double retour = SaadaConstant.DOUBLE;
		try { 
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "getEMin: read knowledge base");
			retour = 4126.81;
			ColumnExpressionSetter cs = new ColumnExpressionSetter("em_min");
			cs.setUnit("nm");
			cs.setByValue(String.valueOf(retour), false);
			cs.completeDetectionMsg("Issued from the knowledge base");
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
			retour = 7081.17;
			ColumnExpressionSetter cs = new ColumnExpressionSetter("em_max");
			cs.setUnit("nm");
			cs.setByValue(String.valueOf(retour), false);
			cs.completeDetectionMsg("Issued from the knowledge base");
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
		ColumnExpressionSetter cs = new ColumnExpressionSetter("e_unit");
		cs.setByValue("nm", false);
		cs.completeDetectionMsg("Issued from the knowledge base");
		return cs;
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
			cs.setByValue(getStringValue("DATE-OBS") + " " + (getStringValue("TIME-OBS")), false);
			cs.completeDetectionMsg("Issued from the knowledge base");
			return cs;
		} catch (Exception e) {
			return  new ColumnExpressionSetter("t_min", e.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see saadadb.products.ppknowledge.PipelineParser#getTMin()
	 */
	@Override
	public ColumnExpressionSetter getExpTime() throws Exception {
		try { 
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "getTMin: read knowledge base");
			ColumnExpressionSetter cs = new ColumnExpressionSetter("t_min");
			cs.setByValue(getStringValue("EXPTIME"), false);
			cs.completeDetectionMsg("Issued from the knowledge base");
			cs.setUnit("s");
			return cs;
		} catch (Exception e) {
			return  new ColumnExpressionSetter("t_exptime", e.getMessage());
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
		cs.completeDetectionMsg("Issued from the knowledge base");
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
		cs.completeDetectionMsg("Issued from the knowledge base");
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
		cs.completeDetectionMsg("Issued from the knowledge base");
		return cs;
	}
}
