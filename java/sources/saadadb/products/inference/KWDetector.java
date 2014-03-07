package saadadb.products.inference;

import java.util.Map;

import saadadb.meta.AttributeHandler;

public abstract class KWDetector {
	protected Map<String, AttributeHandler> tableAttributeHandler;
	protected Map<String, AttributeHandler> entryAttributeHandler;

	public KWDetector(Map<String, AttributeHandler> tableAttributeHandler) {
		this.tableAttributeHandler = tableAttributeHandler;
	}
	public KWDetector(Map<String, AttributeHandler> tableAttributeHandler, Map<String, AttributeHandler> entryAttributeHandler) {
		this.tableAttributeHandler = tableAttributeHandler;
		this.entryAttributeHandler = entryAttributeHandler;
	}
	
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
	 * @return
	 */
	protected AttributeHandler searchByUcd(String ucd_regexp) {

		for( AttributeHandler ah: tableAttributeHandler.values()) {
			if( ah.getUcd().matches(ucd_regexp))
				return ah;
		}
		return null;
	}
	/**
	 * @param ucd_regexp
	 * @return
	 */
	protected AttributeHandler searchByName(String colname_regexp) {

		for( AttributeHandler ah: tableAttributeHandler.values()) {
			if( ah.getNameorg().matches(colname_regexp) || ah.getNameattr().matches(colname_regexp))
				return ah;
		}
		return null;
	}


}
