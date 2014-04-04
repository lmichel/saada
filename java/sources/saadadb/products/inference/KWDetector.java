package saadadb.products.inference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.products.ColumnSetMode;
import saadadb.products.ColumnSetter;
import saadadb.products.ProductFile;
import saadadb.util.Messenger;
import saadadb.util.RegExp;

public abstract class KWDetector {
	protected Map<String, AttributeHandler> tableAttributeHandler;
	protected Map<String, AttributeHandler> entryAttributeHandler;

	/**
	 * @param tableAttributeHandler
	 */
	public KWDetector(Map<String, AttributeHandler> tableAttributeHandler) {
		this.tableAttributeHandler = tableAttributeHandler;
	}
	/**
	 * @param tableAttributeHandler
	 * @param entryAttributeHandler
	 */
	public KWDetector(Map<String, AttributeHandler> tableAttributeHandler, Map<String, AttributeHandler> entryAttributeHandler) {
		this.tableAttributeHandler = tableAttributeHandler;
		this.entryAttributeHandler = entryAttributeHandler;
	}
	/**
	 * @param productFile
	 * @throws SaadaException
	 */
	public KWDetector(ProductFile productFile) throws SaadaException {
		if( productFile != null ) {
			this.tableAttributeHandler = productFile.getAttributeHandler();
			this.entryAttributeHandler =  productFile.getEntryAttributeHandler();
		}
	}

	/**
	 * @param ucd_regexp
	 * @param colname_regexp
	 * @return
	 * @throws FatalException 
	 */
	protected  ColumnSetter search(String ucd_regexp, String colname_regexp) throws FatalException{
		ColumnSetter retour = null;
		if(ucd_regexp!= null ){
			retour = this.searchByUcd(ucd_regexp);
		}
		if( retour.notSet() && colname_regexp != null){
			retour = this.searchByName(colname_regexp);
		}
		return (retour != null)? retour: new ColumnSetter();
	}
	/**
	 * @param ucd_regexp
	 * @param colname_regexp
	 * @return
	 * @throws FatalException 
	 */
	protected  ColumnSetter searchColumns(String ucd_regexp, String colname_regexp) throws FatalException{
		ColumnSetter retour = null;
		if(ucd_regexp!= null ){
			retour = this.searchColumnsByUcd(ucd_regexp);
		}
		if( retour.notSet() && colname_regexp != null){
			retour =  this.searchColumnsByName(colname_regexp);
		}
		return (retour != null)? retour: new ColumnSetter();
	}
	/**
	 * @param ucd_regexp
	 * @return
	 * @throws FatalException 
	 */
	protected ColumnSetter searchByUcd(String ucd_regexp) throws FatalException {
		String msg = "";
		if( Messenger.debug_mode ) 
			msg =  "Search by UCD /" + ucd_regexp + "/";

		for( AttributeHandler ah: this.tableAttributeHandler.values()) {
			if( ah.getUcd().matches(ucd_regexp)){
				if( Messenger.debug_mode ) 
					Messenger.printMsg(Messenger.DEBUG, msg + " Found: " + ah);
				return new ColumnSetter(ah, ColumnSetMode.BY_KEYWORD, false, true);
			}
		}
		if( Messenger.debug_mode ) 
			Messenger.printMsg(Messenger.DEBUG, msg + " Not found");
		return notSetColumnSetter();
	}
	
	/**
	 * @param ucd1_regexp
	 * @param ucd2_regexp
	 * @return
	 * @throws FatalException 
	 */
	protected List<ColumnSetter> searchByUcd(String ucd1_regexp, String ucd2_regexp) throws FatalException {
		String msg = "";
		AttributeHandler ah1 =  null;
		AttributeHandler ah2 =  null;
		List<ColumnSetter> retour = new ArrayList<ColumnSetter>();
		if( Messenger.debug_mode ) 
			msg = "Search by UCDs /" + ucd1_regexp + "/ followed by /" + ucd2_regexp + "/";
		for( AttributeHandler ah: this.tableAttributeHandler.values()) {
			if( ah.getUcd().matches(ucd1_regexp) ){
				ah1 = ah;
				continue;
			}
			if( ah1 != null ) {
				if( ah.getUcd().matches(ucd2_regexp)){
					ah2 = ah;
					if( Messenger.debug_mode ) 
						Messenger.printMsg(Messenger.DEBUG, msg + " Found: " + ah1 + " and " + ah2 );
					retour.add(new ColumnSetter(ah1, ColumnSetMode.BY_KEYWORD, false, true));
					retour.add(new ColumnSetter(ah2, ColumnSetMode.BY_KEYWORD, false, true));
					return retour;
				} else {
					ah1 = null;
				}
			}
		}
		if( Messenger.debug_mode ) 
			Messenger.printMsg(Messenger.DEBUG, msg + " Not found");
		return retour;
	}

	/**
	 * @param ucd_regexp
	 * @return
	 * @throws FatalException 
	 */
	protected ColumnSetter searchByName(String colname_regexp) throws FatalException {
		String msg = "";
		if( Messenger.debug_mode ) 
			msg = "Search by NAME /" + colname_regexp + "/";
		for( AttributeHandler ah: this.tableAttributeHandler.values()) {
			if( ah.getNameorg().matches(colname_regexp) || ah.getNameattr().matches(colname_regexp)){
				if( Messenger.debug_mode ) 
					Messenger.printMsg(Messenger.DEBUG, msg + " Found: " + ah);
				return new ColumnSetter(ah, ColumnSetMode.BY_KEYWORD, false, false);
			}
		}
		if( Messenger.debug_mode ) 
			Messenger.printMsg(Messenger.DEBUG, msg + " Not found");
		return notSetColumnSetter();
	}
	/**
	 * @param colname1_regexp
	 * @param colname2_regexp
	 * @return
	 * @throws FatalException 
	 */
	protected List<ColumnSetter> searchByName(String colname1_regexp, String colname2_regexp) throws FatalException {
		String msg = "";
		AttributeHandler ah1 =  null;
		AttributeHandler ah2 =  null;
		List<ColumnSetter> retour = new ArrayList<ColumnSetter>();
		if( Messenger.debug_mode ) 
			msg = "Search by NAMES /" + colname1_regexp + "/ followed by /" + colname2_regexp + "/";
		for( AttributeHandler ah: this.tableAttributeHandler.values()) {
			if( ah.getNameorg().matches(colname1_regexp) || ah.getNameattr().matches(colname1_regexp)){
				ah1 = ah;
				continue;
			}
			if( ah1 != null ) {
				if( ah.getNameorg().matches(colname2_regexp) || ah.getNameattr().matches(colname2_regexp)){
					ah2 = ah;
					if( Messenger.debug_mode ) 
						Messenger.printMsg(Messenger.DEBUG, msg + " Found: " + ah1 + " and " + ah2 );
					retour.add(new ColumnSetter(ah1, ColumnSetMode.BY_KEYWORD, false, false));
					retour.add(new ColumnSetter(ah2, ColumnSetMode.BY_KEYWORD, false, false));
					return retour;
				} else {
					ah1 = null;
				}
			}
		}
		if( Messenger.debug_mode ) 
			Messenger.printMsg(Messenger.DEBUG, msg + " Not found");
		return retour;
	}

	/**
	 * @param ucd_regexp
	 * @return
	 * @throws FatalException 
	 */
	protected ColumnSetter searchColumnsByUcd(String ucd_regexp) throws FatalException {
		String msg = "";
		if( Messenger.debug_mode ) 
			msg =  "Search column by UCD /" + ucd_regexp + "/";

		for( AttributeHandler ah: this.entryAttributeHandler.values()) {
			if( ah.getUcd().matches(ucd_regexp)){
				if( Messenger.debug_mode ) 
					Messenger.printMsg(Messenger.DEBUG, msg + " Found: " + ah);
				return new ColumnSetter(ah, ColumnSetMode.BY_TABLE_COLUMN, false, true);
			}
		}
		if( Messenger.debug_mode ) 
			Messenger.printMsg(Messenger.DEBUG, msg + " Not found");
		return notSetColumnSetter();
	}
	
	
	/**
	 * @param ucd_regexp
	 * @return
	 * @throws FatalException 
	 */
	protected ColumnSetter searchColumnsByName(String colname_regexp) throws FatalException {
		String msg = "";
		if( Messenger.debug_mode ) 
			msg = "Search column by NAME /" + colname_regexp + "/";
		for( AttributeHandler ah: this.entryAttributeHandler.values()) {
			if( ah.getNameorg().matches(colname_regexp) || ah.getNameattr().matches(colname_regexp)){
				if( Messenger.debug_mode ) 
					Messenger.printMsg(Messenger.DEBUG, msg + " Found: " + ah);
				return new ColumnSetter(ah, ColumnSetMode.BY_TABLE_COLUMN, false, false);
			}
		}
		if( Messenger.debug_mode ) 
			Messenger.printMsg(Messenger.DEBUG, msg + " Not found" );
		return notSetColumnSetter();
	}

	/**
	 * Builsd the ColumnSetter to return when no va;lue has been found
	 * @return
	 */
	private ColumnSetter notSetColumnSetter(){
		ColumnSetter retour = new ColumnSetter();
		retour.completeMessage("Nothing found");
		retour.setNotSet();
		return retour;
	}

	public static void main(String[] args) {
		String[] v = new String [] {"__r", "__raj2000", "__dej2000", "_nogg", "_pgcgal", "_namegal", "_rab1950", "_deb1950", "_cz", "_r_cz", "_bmag", "_r_bmag"};
		for( String s: v){
			System.out.println(s + " " + s.matches(RegExp.FK5_RA_KW));		
		}
		for( String s: v){
			System.out.println(s + " " + s.matches(RegExp.FK4_DEC_KW));		
		}

	}


}
