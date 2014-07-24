package saadadb.products.inference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import saadadb.exceptions.FatalException;
import saadadb.meta.AttributeHandler;
import saadadb.products.setter.ColumnSingleSetter;
import saadadb.util.Messenger;
import saadadb.util.RegExp;

/**

 * @author michel
 * @version $Id$
 */
public class ObservableKWDetector extends KWDetector {
	public List<String> comments;
	/**
	 * Quantities are bound each to other. they are set together and returned by accessors
	 */
	private ColumnSingleSetter ucd=new ColumnSingleSetter();
	private ColumnSingleSetter unit=new ColumnSingleSetter();
	private ColumnSingleSetter calib=new ColumnSingleSetter();;
	private boolean commentSearched = false;
	private boolean keywordsSearched = false;
	private boolean columnsSearched = false;

	public ObservableKWDetector(Map<String, AttributeHandler> tableAttributeHandler, List<String> comments) {
		super(tableAttributeHandler);
		this.comments = (comments == null)? new ArrayList<String>(): comments;
	}
	public ObservableKWDetector(Map<String, AttributeHandler> tableAttributeHandler
			, Map<String, AttributeHandler> entryAttributeHandler, List<String> comments) {
		super(tableAttributeHandler, entryAttributeHandler);
		this.comments = (comments == null)? new ArrayList<String>(): comments;
	}
	
	/**
	 * DO a global search in metatdata: first in keyword a,d then in infos and comments
	 * @throws FatalException
	 */
	private void search() throws FatalException {
		this.searchInkeywords();
		if( this.unit.notSet()){
			this.searchInComments();
		}
		if( this.unit.notSet()){
			this.searchInColumns();
		}
	}

	/**
	 * @throws FatalException 
	 * 
	 */
	private void searchInColumns() throws FatalException {
		if( this.columnsSearched ){
			return;
		}
		this.columnsSearched = true;
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Searching spectral coordinate in the column names");
		if( this.entryAttributeHandler != null ){
			ColumnSingleSetter ah = this.searchColumns(null, RegExp.SPEC_FLUX_KW, RegExp.SPEC_FLUX_DESC);
			if( !ah.notSet()  ){
				if( ah.getUnit() != null && ah.getUnit().length() > 0 ) {
					this.unit.setByTableColumn(ah.getUnit(), false);
					this.unit.completeMessage("Taken from description of column " + ah.getAttNameOrg());
				} else {
					this.unit.setByTableColumn(ah.getUnit(), false);
					this.unit.completeMessage("Not unit for column " + ah.getAttNameOrg() + " take counts");					
				}
				if( ah.getUcd() != null && ah.getUcd().length() > 0 ) {
					this.ucd.setByTableColumn(ah.getUcd(), false);
					this.ucd.completeMessage("Taken from description of column " + ah.getAttNameOrg());
				} else if( ah.getUnit() != null && ah.getUnit().length() > 0 ){
					this.ucd.setByValue("phot.flux", false);
					this.ucd.completeMessage("Infered from unit");					
				} else {
					this.ucd.setByTableColumn("phot.count", false);
					this.ucd.completeMessage("Infered from unit");					
				}
				if( !this.unit.notSet() && !this.unit.getValue().matches("(?i)(.*count.*)") ) {
					this.calib.setByValue("2", false);
					this.calib.completeMessage("Infered from both ucd and unit");
				} else {
					this.calib.setByValue("1", false);
					this.calib.completeMessage("Infered from both ucd and unit");
				}
			}
		}
	
	}
	/**
	 * 
	 */
	private void searchInComments() {
		if( this.commentSearched ){
			return;
		}
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Search observable quantities in FITS comments or infos");
		this.commentSearched = true;
		Pattern p = Pattern.compile("Image column (.*) is flux \\((.*)\\)");
		int dim = 0;
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Searching flux unit in pixel array");
		for( String c: this.comments ) {
			Matcher m = p.matcher(c);
			if( m.find() && m.groupCount() == 2 ) {
				for( AttributeHandler ah : this.tableAttributeHandler.values() ){
					if( ah.getNameorg().matches("NAXIS\\d$")) {
						int v = Integer.parseInt(ah.getValue());
						dim = (v > dim)? v: dim;
					}
				}
				if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "find range=" + dim + "pixels unit=" + m.group(2).trim());
				this.calib.setByValue( "2", false);
				this.calib.completeMessage("Infered from detected units");

				this.ucd.setValue("phot.flux");
				this.ucd.completeMessage("Infered from detected units");
				String u = m.group(2).trim();
				this.unit.setValue(u);
				this.unit.completeMessage("found in FITS comments or infos");
				if( u.length() > 0 && !u.equalsIgnoreCase("count")) {					
					this.calib.setByValue( "3", false);
					this.calib.completeMessage("Infered from detected units");
					this.ucd.setValue("phot.flux");
					this.ucd.completeMessage("Infered from detected units");
				} else {
					this.calib.setByValue( "2", false);
					this.calib.completeMessage("Infered from detected units");
					this.ucd.setValue("phot.count");
					this.ucd.completeMessage("Infered from detected units");
				}
			}
		}		
	}
	/**
	 * @throws FatalException 
	 * 
	 */
	private void searchInkeywords() throws FatalException {
		if( this.keywordsSearched ){
			return;
		}
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Search observable quantities in keywords");
		this.keywordsSearched = true;
		this.unit = this.searchByName(RegExp.OBSERVABLE_UNIT_KW);
		if( !this.unit.notSet() ){
			this.calib.setByValue( "2", false);
			this.calib.completeMessage("Infered from detected units");

			this.ucd.setValue("phot.flux");
			this.ucd.completeMessage("Infered from detected units");
			this.unit.completeMessage("found in keywords");
			if( this.unit.getValue().length() > 0 && !this.unit.getValue().equalsIgnoreCase("count")) {					
				this.calib.setByValue( "3", false);
				this.calib.completeMessage("Infered from detected units");
				this.ucd.setValue("phot.flux");
				this.ucd.completeMessage("Infered from detected units");
			} else {
				this.calib.setByValue( "2", false);
				this.calib.completeMessage("Infered from detected units");
				this.ucd.setValue("phot.count");
				this.ucd.completeMessage("Infered from detected units");
			}
		}
	}		


	/**
	 * @return
	 * @throws FatalException
	 */
	public ColumnSingleSetter getUcdName() throws FatalException{
		if( Messenger.debug_mode ) 
			Messenger.printMsg(Messenger.DEBUG, "Search for the Observable Unit");
		this.search() ;
		return this.ucd;
	}

	/**
	 * @return
	 * @throws FatalException
	 */
	public ColumnSingleSetter getUnitName() throws FatalException{
		if( Messenger.debug_mode ) 
			Messenger.printMsg(Messenger.DEBUG, "Search for the Observable Unit");
		this.search() ;
		return this.unit;
	}
	public ColumnSingleSetter getCalibStatus() throws FatalException{
		//		Level 0: Raw instrumental data, in a proprietary or internal data-provider defined format, that needs instrument specific tools to be handled.
		//		Level 1: Instrumental data in a standard format (FITS, VOTable, SDFITS, ASDM, etc.) which could be manipulated with standard astronomical packages.
		//		Level 2: Calibrated, science ready data with the instrument signature removed.
		//		Level 3: Enhanced data products like mosaics, resampled or drizzled images, or heavily processed survey fields. Level 3 data products may represent the combination of data from multiple primary observations.		
		if( Messenger.debug_mode ) 
			Messenger.printMsg(Messenger.DEBUG, "Search for the Observable calib status");
		this.search() ;
		return this.calib;
	}

}
