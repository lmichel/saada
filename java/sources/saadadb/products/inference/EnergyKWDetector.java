package saadadb.products.inference;

import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.products.ProductFile;
import saadadb.util.Messenger;
import saadadb.util.RegExp;
import saadadb.util.SaadaConstant;

public class EnergyKWDetector extends KWDetector {
	private SpectralCoordinate spectralCoordinate;
	private ProductFile productFile;
	private boolean mustSearchUnit = false;

	public EnergyKWDetector(ProductFile productFile) throws SaadaException {
		super(productFile.getAttributeHandler(), productFile.getEntryAttributeHandler());
		this.productFile = productFile;
	}

	public void mustSearchUnit() {
		this.mustSearchUnit = true;
	}
	/**
	 * @param ds
	 * @return
	 */
	private void setMinMaxValues(double[] ds) {
		if( ds == null || ds.length != 2 ) {
			this.spectralCoordinate.setOrgMax(SaadaConstant.DOUBLE);
			this.spectralCoordinate.setOrgMin(SaadaConstant.DOUBLE);
		} else {
			this.spectralCoordinate.setOrgMax(ds[1]);
			this.spectralCoordinate.setOrgMin(ds[0]);		
		}
	}

	private boolean mapCollectionSpectralCoordinateAuto() throws Exception {	
		spectralCoordinate = new SpectralCoordinate();
		return ( this.findSpectralCoordinateByUCD()    ||  !this.findSpectralCoordinateByWCS() 
				||  !this.findSpectralCoordinateByKW() || this.findSpectralCoordinateInPixels());
	}

	/**
	 * @return
	 * @throws Exception 
	 */
	private boolean findSpectralCoordinateInPixels() throws Exception {
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Searching spectral coordinates in pixel");
		double[] ext = this.productFile.getExtrema(null);
		if( ext != null ) {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Spectral coordinates found in FITS image pixels");
			this.setMinMaxValues(ext);	
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * @return
	 * @throws Exception 
	 */
	private boolean findSpectralCoordinateByWCS() throws Exception{
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Searching spectral coordinates in WCS keywords");
		return  spectralCoordinate.convertWCS(this.tableAttributeHandler);
	}

	/**
	 * @return
	 * @throws IgnoreException 
	 */
	private boolean findSpectralCoordinateByKW() throws Exception{

		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Searching spectral coordinate in KWs ");
		if( this.mustSearchUnit ) spectralCoordinate.setOrgUnit(SaadaConstant.STRING);				

		/*
		 * If no range set in params, try to find it out from fields
		 */	
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "No Spectral coordinate found in header: explore columns names");
		if( this.entryAttributeHandler != null ){
			AttributeHandler ah = this.searchColumnsByName(RegExp.SPEC_AXIS_KW);
			if( ah != null ){
				this.setMinMaxValues(this.productFile.getExtrema(ah.getNameorg()));
				if( mustSearchUnit && ah.getUnit() != null ) {
					spectralCoordinate.setOrgUnit(ah.getUnit());
					if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, spectralCoordinate.getOrgMin() + " " + spectralCoordinate.getOrgMax() + " " + spectralCoordinate.getOrgUnit());
				}
				return true;
			}
		}
		return  false;
	}

	/**
	 * @return
	 * @throws Exception 
	 */
	private boolean findSpectralCoordinateByUCD() throws Exception{

		boolean findMin = false;
		boolean findMax = false;

		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Searching spectral coordinates by UCDs or UTypes ");
		String unit = "";
		AttributeHandler ah = this.searchByUcd(RegExp.SPEC_MIN_UCD);
		if( ah !=null){
			spectralCoordinate.setOrgMin(Double.parseDouble(ah.getValue()));
			findMin = true;
			unit = ah.getUnit();
		}
		ah = this.searchByUcd(RegExp.SPEC_MAX_UCD);
		if( ah !=null){
			spectralCoordinate.setOrgMax(Double.parseDouble(ah.getValue()));
			findMax = true;
			if( unit == null || unit.length() == 0) unit = ah.getUnit();
		}

		if( mustSearchUnit ) {
			if (unit == null || unit.length() == 0 ){
				spectralCoordinate.setOrgUnit(SaadaConstant.STRING);	
			} else {
				Messenger.printMsg(Messenger.TRACE, "Spectral coordinates found:"+spectralCoordinate.getOrgMin() + " to " + spectralCoordinate.getOrgMax() + " " + spectralCoordinate.getOrgUnit());
				spectralCoordinate.setOrgUnit(unit);

			}
		} 
		if ( !findMax  || !findMin ){
			/*
			 * If no range set in params, try to find it out from fields
			 */	
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "No Spectral coordinates found in header UCDs or UTypes: explore column definitions");
			findMin = false;
			ah = this.searchColumnsByUcd(RegExp.SPEC_BAND_UCD);
			if( ah !=null){
				this.setMinMaxValues(this.productFile.getExtrema(ah.getNameorg()));
				if( unit == null || unit.length() == 0) unit = ah.getUnit();
				findMin = true;
			}
			if( mustSearchUnit ) {
				if( unit == null || unit.length() == 0) {
					spectralCoordinate.setOrgUnit(SaadaConstant.STRING);				
				} else {
					spectralCoordinate.setOrgUnit(unit);				
				}
				if( findMin ) {
					Messenger.printMsg(Messenger.TRACE, "Spectral coordinates found:"+spectralCoordinate.getOrgMin() + " to " + spectralCoordinate.getOrgMax() + " " + spectralCoordinate.getOrgUnit());
					return true;
				} else {
					if (Messenger.debug_mode)
						Messenger.printMsg(Messenger.DEBUG, "Spectral coordinates not found by using UCDs and UTYPES");
					return false;
				}
			}
			return findMin;
		}
		return false;
	}

	public SpectralCoordinate getSpectralCoordinate() throws Exception{
		if( spectralCoordinate == null ){
			this.mapCollectionSpectralCoordinateAuto();
		}
		return spectralCoordinate;
	}
}
