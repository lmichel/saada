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
	}
	
	/**
	 * @param key
	 * @return
	 */
	public static final String[] get(int key){
		return map.get(key);
	}

}
