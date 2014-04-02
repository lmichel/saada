package saadadb.products.inference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
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
		System.out.println("CREATOR 1 " + this.tableAttributeHandler.size() + " " + this.getClass().getName());
	}
	/**
	 * @param tableAttributeHandler
	 * @param entryAttributeHandler
	 */
	public KWDetector(Map<String, AttributeHandler> tableAttributeHandler, Map<String, AttributeHandler> entryAttributeHandler) {
		this.tableAttributeHandler = tableAttributeHandler;
		this.entryAttributeHandler = entryAttributeHandler;
		System.out.println("CREATOR 2 " + this.tableAttributeHandler.size()+ " " + this.entryAttributeHandler.size()+ " " + this.getClass().getName());
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
		System.out.println("CREATOR 3 " + this.tableAttributeHandler.size()+ " " + this.entryAttributeHandler.size()+ " " + this.getClass().getName());
	}

	/**
	 * @param ucd_regexp
	 * @param colname_regexp
	 * @return
	 */
	protected  AttributeHandler search(String ucd_regexp, String colname_regexp){
		AttributeHandler retour = null;
		if(ucd_regexp!= null ){
			retour = this.searchByUcd(ucd_regexp);
		}
		if( retour == null && colname_regexp != null){
			retour = this.searchByName(colname_regexp);
		}
		return retour;
	}
	/**
	 * @param ucd_regexp
	 * @param colname_regexp
	 * @return
	 */
	protected  AttributeHandler searchColumns(String ucd_regexp, String colname_regexp){
		AttributeHandler retour = null;
		if(ucd_regexp!= null ){
			retour = this.searchColumnsByUcd(ucd_regexp);
		}
		if( retour == null && colname_regexp != null){
			retour = this.searchColumnsByName(colname_regexp);
		}
		return retour;
	}
	/**
	 * @param ucd_regexp
	 * @return
	 */
	protected AttributeHandler searchByUcd(String ucd_regexp) {
		String msg = "";
		if( Messenger.debug_mode ) 
			msg =  "Search by UCD /" + ucd_regexp + "/";

		for( AttributeHandler ah: this.tableAttributeHandler.values()) {
			if( ah.getUcd().matches(ucd_regexp)){
				if( Messenger.debug_mode ) 
					Messenger.printMsg(Messenger.DEBUG, msg + " Found: " + ah);
				return ah;
			}
		}
		if( Messenger.debug_mode ) 
			Messenger.printMsg(Messenger.DEBUG, msg + " Not found");
		return null;
	}
	
	/**
	 * @param ucd1_regexp
	 * @param ucd2_regexp
	 * @return
	 */
	protected List<AttributeHandler> searchByUcd(String ucd1_regexp, String ucd2_regexp) {
		String msg = "";
		AttributeHandler ah1 =  null;
		AttributeHandler ah2 =  null;
		List<AttributeHandler> retour = new ArrayList<AttributeHandler>();
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
					retour.add(ah1);
					retour.add(ah2);
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
	 */
	protected AttributeHandler searchByName(String colname_regexp) {
		String msg = "";
		if( Messenger.debug_mode ) 
			msg = "Search by NAME /" + colname_regexp + "/";
		for( AttributeHandler ah: this.tableAttributeHandler.values()) {
			if( ah.getNameorg().matches(colname_regexp) || ah.getNameattr().matches(colname_regexp)){
				if( Messenger.debug_mode ) 
					Messenger.printMsg(Messenger.DEBUG, msg + " Found: " + ah);
				return ah;
			}
		}
		if( Messenger.debug_mode ) 
			Messenger.printMsg(Messenger.DEBUG, msg + " Not found");
		return null;
	}
	/**
	 * @param colname1_regexp
	 * @param colname2_regexp
	 * @return
	 */
	protected List<AttributeHandler> searchByName(String colname1_regexp, String colname2_regexp) {
		String msg = "";
		AttributeHandler ah1 =  null;
		AttributeHandler ah2 =  null;
		List<AttributeHandler> retour = new ArrayList<AttributeHandler>();
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
					retour.add(ah1);
					retour.add(ah2);
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
	 */
	protected AttributeHandler searchColumnsByUcd(String ucd_regexp) {
		String msg = "";
		if( Messenger.debug_mode ) 
			msg =  "Search column by UCD /" + ucd_regexp + "/";

		for( AttributeHandler ah: this.entryAttributeHandler.values()) {
			if( ah.getUcd().matches(ucd_regexp)){
				if( Messenger.debug_mode ) 
					Messenger.printMsg(Messenger.DEBUG, msg + " Found: " + ah);
				return ah;
			}
		}
		if( Messenger.debug_mode ) 
			Messenger.printMsg(Messenger.DEBUG, msg + " Not found");
		return null;
	}
	
	
	/**
	 * @param ucd_regexp
	 * @return
	 */
	protected AttributeHandler searchColumnsByName(String colname_regexp) {
		String msg = "";
		if( Messenger.debug_mode ) 
			msg = "Search column by NAME /" + colname_regexp + "/";
		for( AttributeHandler ah: this.entryAttributeHandler.values()) {
			if( ah.getNameorg().matches(colname_regexp) || ah.getNameattr().matches(colname_regexp)){
				if( Messenger.debug_mode ) 
					Messenger.printMsg(Messenger.DEBUG, msg + " Found: " + ah);
				return ah;
			}
		}
		if( Messenger.debug_mode ) 
			Messenger.printMsg(Messenger.DEBUG, msg + " Not found" );
		return null;
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
