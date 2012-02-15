package saadadb.admintool.utils;

import java.util.LinkedHashMap;
import java.util.Map;


public class HelpDesk {
	public static final Map<Integer, String[]> map;
	public static final int EXTATT_MISSING = 1;
	public static final int COO_SYS_MAPPING = 2;
	public static final int DISPERSION_MAPPING = 3;
	public static final int POSITION_MAPPING = 4;
	public static final int POSERROR_MAPPING = 5;
	public static final int ENTRY_MAPPING = 6;
	public static final int EXTENSION_MAPPING = 7;
	public static final int CLASS_MAPPING = 8;
	public static final int METADATA_EDITOR = 9;
	public static final int NODE_NAME = 10;
	public static final int RELATION_COLLECTIONS = 11;
	public static final int RELATION_QUALIFIER = 12;
	public static final int RELATION_SELECTOR = 13;
	public static final int VO_CURATION = 14;
	static{
		map = new LinkedHashMap<Integer, String[]>();
		map.put(EXTATT_MISSING, new String[] {
				  "No extended attribute."
				, "Extended attributes must be set at DB creation time"
				, "They can no longer be added after that"
				});	
		map.put(COO_SYS_MAPPING, new String[] {
				  "Give a quoted object name or position or keywords"
				, "with the following format RA[,DEC]"
				, "Keywords can (must) be dropped from the Data Sample window"
				});
		map.put(DISPERSION_MAPPING, new String[] {
				"Give a quoted range or the keyword representing"
				, "the dispersion column (in case of table store)."
				, "Keywords can (must) be dropped from the Data Sample window"
				});
		map.put(POSITION_MAPPING, new String[] {
				"Give a quoted object name or position or keywords"
				, "with the following format RA[,DEC]"
				, "Keywords can (must) be dropped from the Data Sample window"
				});
		map.put(POSERROR_MAPPING, new String[] {
				"Give quoted constant values or keywords"
				, "with the following format ERA[,EDEC[,EANGLE]]"
				, "Keywords can (must) be dropped from the Data Sample window"
				});
		map.put(ENTRY_MAPPING, new String[] {
				"The following parameters are related to the table entries"
				, "Requested Keywords must be searched in the table columns"
				});
		map.put(EXTENSION_MAPPING, new String[] {
				"Drop an extension from the Data Sample window" 
				, "or put a number prefixed with a #"
				, "Keywords of the first extension are loaded by default"
				});
		map.put(CLASS_MAPPING, new String[] {
				"The class name must only contain letters, numbers or undescores."
				, "It can not starts with a number"
				});
		map.put(METADATA_EDITOR, new String[] {
				"Click on a data node (class or category) to display the related meta data."
				, "Right click on the meta data table to edit Utypes, UCds, units or field descriptions."
				});
		map.put(NODE_NAME, new String[] {
				"Name must only contain numbers"
				, "characters and _ and not start with a number"
				, "Forbidden characers are automatically rejected"
				});
		map.put(RELATION_COLLECTIONS, new String[] {
				"Primary collection is set by clicking " 
				,"in the choosen category node on the data tree."
				, "It can not be edited by hand."
				, "Secondary collection is set by dropping "
				, "the choosen category node on the text field."
				});
		map.put(RELATION_QUALIFIER, new String[] {
				"Qualifier names must only contain numbers"
				, ", characters and _ and not start with a number"
				, "You can add as many qualifiers as you want."
				, "The value of any qualifier named 'distance' "
				, "will be automatically computed if possible"
				});
		map.put(RELATION_SELECTOR, new String[] {
				"Cick on a data node to display all relationships touching it"
				, "Select the relationship of interest in the list"
				});
		map.put(VO_CURATION, new String[] {
				"This page does not publish anything in some registry"
				, "The purpose of the information given here "
				, "is just to provide helpful templates of registry resources"
				, "for data collection hosted by this SaadaDB."
		});
	}
	
	/**
	 * @param key
	 * @return
	 */
	public static final String[] get(int key){
		return map.get(key);
	}

}
