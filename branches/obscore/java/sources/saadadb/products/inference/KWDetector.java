package saadadb.products.inference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import saadadb.enums.ColumnSetMode;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.products.DataFile;
import saadadb.products.setter.ColumnExpressionSetter;
import saadadb.util.Messenger;
import saadadb.util.RegExp;

/**
 * Do search operations in a set of keyword.
 * @author michel
 * @version $Id$
 */
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
	 * Take the WK map from a data file
	 * @param productFile
	 * @throws SaadaException
	 */
	public KWDetector(DataFile productFile) throws SaadaException {
		if( productFile != null ) {
			this.tableAttributeHandler = productFile.getAttributeHandler();
			this.entryAttributeHandler =  productFile.getEntryAttributeHandler();
		}
	}

	/**
	 * Incorporate both search be UCD (first) and by column name in the table AttributeHandlers
	 * @param ucd_regexp regular expression to be applied to the UCDs
	 * @param colname_regexp regular expression to be applied to the column name
	 * @return
	 * @throws Exception 
	 */
	protected  ColumnExpressionSetter search(String ucd_regexp, String colname_regexp) throws Exception{
		ColumnExpressionSetter retour = new ColumnExpressionSetter();
		if(ucd_regexp!= null ){
			retour = this.searchByUcd(ucd_regexp);
		}
		if( retour.notSet() && colname_regexp != null){
			retour = this.searchByName(colname_regexp);
		}
		return retour;
	}
	
	/**
	 * Incorporate both search be UCD (first) and by column name in the entrty AttributeHandlers
	 * @param ucd_regexp regular expression to be applied to the UCDs
	 * @param colname_regexp regular expression to be applied to the column name
	 * @return
	 * @throws Exception 
	 */
	protected  ColumnExpressionSetter searchColumns(String ucd_regexp, String colname_regexp) throws Exception{
		ColumnExpressionSetter retour = new ColumnExpressionSetter();
		if(ucd_regexp != null ){
			retour = this.searchColumnsByUcd(ucd_regexp);
		}
		if( retour.notSet() && colname_regexp != null){
			retour =  this.searchColumnsByName(colname_regexp);
		}
		return retour;
	}
	/**
	 * Incorporate  search be UCD (first) then by column name and then by description in the entry AttributeHandlers
	 * @param ucd_regexp
	 * @param colname_regexp
	 * @param desc_regexp
	 * @return
	 * @throws Exception 
	 */
	protected  ColumnExpressionSetter searchColumns(String ucd_regexp, String colname_regexp, String desc_regexp) throws Exception{
		ColumnExpressionSetter retour = new ColumnExpressionSetter();
		if(ucd_regexp != null ){
			retour = this.searchColumnsByUcd(ucd_regexp);
		}
		if( retour.notSet() && colname_regexp != null){
			retour =  this.searchColumnsByName(colname_regexp);
		}
		if( retour.notSet() && colname_regexp != null){
			retour =  this.searchColumnsByDescription(desc_regexp);
		}
		return retour;
	}
	/**
	 * Search the first attribute handler with an UCD matching ucd_regexp
	 * @param ucd_regexp
	 * @return
	 * @throws Exception 
	 */
	protected ColumnExpressionSetter searchByUcd(String ucd_regexp) throws Exception {
		String msg = "";
		if( Messenger.debug_mode ) 
			msg =  "Search by UCD /" + ucd_regexp + "/";

		for( AttributeHandler ah: this.tableAttributeHandler.values()) {
			if( ah.getUcd().matches(ucd_regexp)){
				if( Messenger.debug_mode ) 
					Messenger.printMsg(Messenger.DEBUG, msg + " Found: " + ah);
				//return new ColumnExpressionSetter(ah, ColumnSetMode.BY_KEYWORD, false, true);
				ColumnExpressionSetter setter = new ColumnExpressionSetter(ah);
				setter.completeMessage("kw " + ah.getNameorg() + " detected by UCD");
				return setter;
			}
		}
		if( Messenger.debug_mode ) 
			Messenger.printMsg(Messenger.DEBUG, msg + " Not found");
		return notSetColumnSetter();
	}
	
	/**
	 * Search the 2 attribute handlers with a UCD matching respectively ucd1_regexp and String ucd2_regexp
	 * @param ucd1_regexp
	 * @param ucd2_regexp
	 * @return
	 * @throws Exception 
	 */
	protected List<ColumnExpressionSetter> searchByUcd(String ucd1_regexp, String ucd2_regexp) throws Exception {
		String msg = "";
		AttributeHandler ah1 =  null;
		AttributeHandler ah2 =  null;
		List<ColumnExpressionSetter> retour = new ArrayList<ColumnExpressionSetter>();
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
//					retour.add(new ColumnExpressionSetter(ah1, ColumnSetMode.BY_KEYWORD, false, true));
//					retour.add(new ColumnExpressionSetter(ah2, ColumnSetMode.BY_KEYWORD, false, true));
					retour.add(new ColumnExpressionSetter(ah1));
					retour.add(new ColumnExpressionSetter(ah2));
					retour.get(0).completeMessage("kw " + ah1.getNameorg() + " detected by UCD");
					retour.get(1).completeMessage("kw " + ah2.getNameorg() + " detected by UCD");
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
	 * Search the first attribute handler with a name matching colname_regexp
	 * @param ucd_regexp
	 * @return
	 * @throws Exception 
	 */
	protected ColumnExpressionSetter searchByName(String colname_regexp) throws Exception {
		String msg = "";
		if( Messenger.debug_mode ) 
			msg = "Search by NAME /" + colname_regexp + "/";
		for( AttributeHandler ah: this.tableAttributeHandler.values()) {
			if( ah.getNameorg().matches(colname_regexp) || ah.getNameattr().matches(colname_regexp)){
				if( Messenger.debug_mode ) 
					Messenger.printMsg(Messenger.DEBUG, msg + " Found: " + ah);
				ColumnExpressionSetter setter = new ColumnExpressionSetter(ah);
				setter.completeMessage("kw " + ah.getNameorg() + " detected by name");
				return setter;
			}
		}
		if( Messenger.debug_mode ) 
			Messenger.printMsg(Messenger.DEBUG, msg + " Not found");
		return notSetColumnSetter();
	}
	/**
	 * Search the 2 attribute handlers with a name matching respectively colname1_regexp and String colname2_regexp
	 * @param colname1_regexp
	 * @param colname2_regexp
	 * @return
	 * @throws Exception 
	 */
	protected List<ColumnExpressionSetter> searchByName(String colname1_regexp, String colname2_regexp) throws Exception {
		String msg = "";
		AttributeHandler ah1 =  null;
		AttributeHandler ah2 =  null;
		List<ColumnExpressionSetter> retour = new ArrayList<ColumnExpressionSetter>();
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
//					retour.add(new ColumnExpressionSetter(ah1, ColumnSetMode.BY_KEYWORD, false, false));
//					retour.add(new ColumnExpressionSetter(ah2, ColumnSetMode.BY_KEYWORD, false, false));
					retour.add(new ColumnExpressionSetter(ah1));
					retour.add(new ColumnExpressionSetter(ah2));
					retour.get(0).completeMessage("kw " + ah1.getNameorg() + " detected by name");
					retour.get(1).completeMessage("kw " + ah2.getNameorg() + " detected by name");
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
	 * Search the first entry attribute handler with an UCD matching ucd_regexp
	 * @param ucd_regexp
	 * @return
	 * @throws Exception 
	 */
	protected ColumnExpressionSetter searchColumnsByUcd(String ucd_regexp) throws Exception {
		String msg = "";
		if( Messenger.debug_mode ) 
			msg =  "Search column by UCD /" + ucd_regexp + "/";

		for( AttributeHandler ah: this.entryAttributeHandler.values()) {
			if( ah.getUcd().matches(ucd_regexp)){
				if( Messenger.debug_mode ) 
					Messenger.printMsg(Messenger.DEBUG, msg + " Found: " + ah);
				//return new ColumnExpressionSetter(ah, ColumnSetMode.BY_TABLE_COLUMN, false, true);
				ColumnExpressionSetter setter = new ColumnExpressionSetter(ah,ColumnSetMode.BY_TABLE_COLUMN);
				setter.completeMessage("col " + ah.getNameorg() + " detected by UCD");
				return setter;
			}
		}
		if( Messenger.debug_mode ) 
			Messenger.printMsg(Messenger.DEBUG, msg + " Not found");
		return notSetColumnSetter();
	}

	/**
	 * Search the 2 colmuns attribute handlers with a UCD matching respectively ucd1_regexp and String ucd2_regexp
	 * @param ucd1_regexp
	 * @param ucd2_regexp
	 * @return
	 * @throws Exception 
	 */
	protected List<ColumnExpressionSetter> searchColumnByUcd(String ucd1_regexp, String ucd2_regexp) throws Exception {
		String msg = "";
		AttributeHandler ah1 =  null;
		AttributeHandler ah2 =  null;
		List<ColumnExpressionSetter> retour = new ArrayList<ColumnExpressionSetter>();
		if( Messenger.debug_mode ) 
			msg = "Search by UCDs /" + ucd1_regexp + "/ followed by /" + ucd2_regexp + "/";
		for( AttributeHandler ah: this.entryAttributeHandler.values()) {
			if( ah.getUcd().matches(ucd1_regexp) ){
				ah1 = ah;
				continue;
			}
			if( ah1 != null ) {
				if( ah.getUcd().matches(ucd2_regexp)){
					ah2 = ah;
					if( Messenger.debug_mode ) 
						Messenger.printMsg(Messenger.DEBUG, msg + " Found: " + ah1 + " and " + ah2 );
//					retour.add(new ColumnExpressionSetter(ah1, ColumnSetMode.BY_KEYWORD, false, true));
//					retour.add(new ColumnExpressionSetter(ah2, ColumnSetMode.BY_KEYWORD, false, true));
					retour.add(new ColumnExpressionSetter(ah1));
					retour.add(new ColumnExpressionSetter(ah2));
					retour.get(0).completeMessage("col " + ah1.getNameorg() + " detected by UCD");
					retour.get(1).completeMessage("col " + ah2.getNameorg() + " detected by UCD");
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
	 * Search the first entry attribute handler with a name matching colname_regexp
	 * @param ucd_regexp
	 * @return
	 * @throws Exception 
	 */
	protected ColumnExpressionSetter searchColumnsByName(String colname_regexp) throws Exception {
		String msg = "";
		if( Messenger.debug_mode ) 
			msg = "Search column by NAME /" + colname_regexp + "/";
		for( AttributeHandler ah: this.entryAttributeHandler.values()) {
			if( ah.getNameorg().matches(colname_regexp) || ah.getNameattr().matches(colname_regexp)){
				if( Messenger.debug_mode ) 
					Messenger.printMsg(Messenger.DEBUG, msg + " Found: " + ah);
//				return new ColumnExpressionSetter(ah, ColumnSetMode.BY_TABLE_COLUMN, false, false);
				ColumnExpressionSetter setter = new ColumnExpressionSetter(ah,ColumnSetMode.BY_TABLE_COLUMN);
				setter.completeMessage("col " + ah.getNameorg() + " detected by name");
				return setter;

			}
		}
		if( Messenger.debug_mode ) 
			Messenger.printMsg(Messenger.DEBUG, msg + " Not found" );
		return notSetColumnSetter();
	}
	/**
	 * Search the first entry attribute handler with a description matching colname_regexp
	 * @param ucd_regexp
	 * @return
	 * @throws Exception 
	 */
	protected ColumnExpressionSetter searchColumnsByDescription(String colname_regexp) throws Exception {
		String msg = "";
		if( Messenger.debug_mode ) 
			msg = "Search column by DESCRIPTION /" + colname_regexp + "/";
		for( AttributeHandler ah: this.entryAttributeHandler.values()) {
			if( ah.getComment().matches(colname_regexp) ){
				if( Messenger.debug_mode ) 
					Messenger.printMsg(Messenger.DEBUG, msg + " Found: " + ah);
//				return new ColumnExpressionSetter(ah, ColumnSetMode.BY_TABLE_COLUMN, false, false);
				ColumnExpressionSetter setter = new ColumnExpressionSetter(ah,ColumnSetMode.BY_TABLE_COLUMN);
				setter.completeMessage("col " + ah.getNameorg() + " detected by description");
				return setter;

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
	private ColumnExpressionSetter notSetColumnSetter(){
		ColumnExpressionSetter retour = new ColumnExpressionSetter();
		//retour.completeMessage("Nothing found");
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
