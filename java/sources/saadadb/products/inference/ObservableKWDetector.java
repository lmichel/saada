package saadadb.products.inference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import saadadb.exceptions.FatalException;
import saadadb.meta.AttributeHandler;
import saadadb.products.setter.ColumnExpressionSetter;
import saadadb.util.Messenger;
import saadadb.vocabulary.RegExp;

/**

 * @author michel
 * @version $Id$
 */
public class ObservableKWDetector extends KWDetector {
	public List<String> comments;
	/**
	 * Quantities are bound each to other. they are set together and returned by accessors
	 */
	private ColumnExpressionSetter ucd=new ColumnExpressionSetter("o_ucd");
	private ColumnExpressionSetter unit=new ColumnExpressionSetter("o_unit");
	private ColumnExpressionSetter calib=new ColumnExpressionSetter("o_calib_status");
	private boolean commentSearched = false;
	private boolean keywordsSearched = false;
	private boolean columnsSearched = false;

	public ObservableKWDetector(Map<String, AttributeHandler> tableAttributeHandler, List<String> comments) {
		super(tableAttributeHandler, null);
		this.comments = (comments == null)? new ArrayList<String>(): comments;
	}
	public ObservableKWDetector(Map<String, AttributeHandler> tableAttributeHandler
			, Map<String, AttributeHandler> entryAttributeHandler, List<String> comments) {
		super(tableAttributeHandler, entryAttributeHandler, null);
		this.comments = (comments == null)? new ArrayList<String>(): comments;
	}
	
	/**
	 * DO a global search in metatdata: first in keyword a,d then in infos and comments
	 * @throws Exception 
	 */
	private void search() throws Exception {
		this.searchInkeywords();
		if( this.unit.isNotSet()){
			this.searchInComments();
		}
		if( this.unit.isNotSet()){
			this.searchInColumns();
		}
	}

	/**
	 * @throws Exception 
	 * 
	 */
	private void searchInColumns() throws Exception {
		if( this.columnsSearched ){
			return;
		}
		this.columnsSearched = true;
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Searching spectral coordinate in the column names");
		if( this.entryAttributeHandler != null ){
			ColumnExpressionSetter ah = this.searchColumns(null, RegExp.SPEC_FLUX_KW, RegExp.SPEC_FLUX_DESC);
			if( !ah.isNotSet()  ){
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
				if( !this.unit.isNotSet() && !this.unit.getValue().matches("(?i)(.*count.*)") ) {
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
	 * @throws Exception 
	 * 
	 */
	private void searchInkeywords() throws Exception {
		if( this.keywordsSearched ){
			return;
		}
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Search observable quantities in keywords");
		this.keywordsSearched = true;
		this.unit = this.searchByName("_o_unit", RegExp.OBSERVABLE_UNIT_KW);
		if( !this.unit.isNotSet() ){
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
	 * @throws Exception 
	 */
	public ColumnExpressionSetter getUcdName() throws Exception{
		if( Messenger.debug_mode ) 
			Messenger.printMsg(Messenger.DEBUG, "Search for the Observable Unit");
		this.search() ;
		return this.ucd;
	}

	/**
	 * @return
	 * @throws Exception 
	 */
	public ColumnExpressionSetter getUnitName() throws Exception{
		if( Messenger.debug_mode ) 
			Messenger.printMsg(Messenger.DEBUG, "Search for the Observable Unit");
		this.search() ;
		return this.unit;
	}
	public ColumnExpressionSetter getCalibStatus() throws Exception{
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
