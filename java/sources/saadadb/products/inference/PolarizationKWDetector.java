package saadadb.products.inference;

import hecds.wcs.Modeler;
import hecds.wcs.types.AxeType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.products.setter.ColumnExpressionSetter;
import saadadb.products.setter.ColumnWcsSetter;
import saadadb.util.Messenger;
import saadadb.vocabulary.RegExp;

/**

 * @author michel
 * @version $Id$
 */
public class PolarizationKWDetector extends KWDetector {
	public List<String> comments;
	/**
	 * Quantities are bound each to other. they are set together and returned by accessors
	 */
	private ColumnExpressionSetter polStateSetter=new ColumnExpressionSetter("pol_states");
	
	public PolarizationKWDetector(Map<String, AttributeHandler> tableAttributeHandler, Modeler wcsModeler, List<String> comments) throws SaadaException {
		super(tableAttributeHandler, wcsModeler.getProjection(AxeType.POLARIZATION));
		this.comments = (comments == null)? new ArrayList<String>(): comments;
	}
	public PolarizationKWDetector(Map<String, AttributeHandler> tableAttributeHandler
			, Map<String, AttributeHandler> entryAttributeHandler, Modeler wcsModeler, List<String> comments) throws SaadaException {
		super(tableAttributeHandler, entryAttributeHandler, wcsModeler.getProjection(AxeType.POLARIZATION));
		this.comments = (comments == null)? new ArrayList<String>(): comments;
	}
	
	/**
	 * @throws SaadaException
	 */
	private void detectAxeParams() throws SaadaException {	
		if( isMapped ){
			return;
		}
		this.isMapped = true;
		try {
			if( this.findPolarizationByWCS()  ) {
				return;
			}
		} catch (SaadaException e) {
			IgnoreException.throwNewException(SaadaException.FILE_FORMAT, e);
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			IgnoreException.throwNewException(SaadaException.FILE_FORMAT, e);
		}
		return;
	}

	
	/**
	 * Ask the WCS projection for spectral coordinates
	 * @return true if the dispersion has been found
	 * @throws Exception 
	 */
	private boolean findPolarizationByWCS() throws Exception{
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Searching for polarization in WCS");
		if( this.projection.isUsable()){
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Found polarization in WCS");
			this.polStateSetter     = new ColumnWcsSetter("pol_states"    , "WCS.getStokes()", this.projection);
			return true;
		} else {
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "No polarization coordinate found in WCS");
			return false;
		}
	}

	/**
	 * @return
	 * @throws Exception 
	 */
	public ColumnExpressionSetter getPolarizationStates() throws Exception{
		this.detectAxeParams();
		if( this.polStateSetter == null  || this.polStateSetter.isNotSet()){
			this.polStateSetter = search("pol_states", RegExp.POLARIZATION_UCD, RegExp.POLARIZATION_KW);
		}
		return this.polStateSetter;
	}
}
