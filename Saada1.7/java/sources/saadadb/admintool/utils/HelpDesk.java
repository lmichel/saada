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
	public static final int MODEL_MAPPER = 15;
	public static final int MAPPING_QVIEW = 16;
	public static final int VOITEM_DESCRIPTION = 17;
	public static final int VOITEM_EDITION = 18;
	public static final int SQL_INDEX = 19;
	public static final int COLL_CREATE = 20;
	public static final int COLL_DROP = 21;
	public static final int COLL_COMMENT = 22;
	public static final int COLL_EMPTY = 23;
	public static final int CATEGORY_EMPTY = 24;
	public static final int CLASS_DROP = 25;
	public static final int CLASS_COMMENT = 26;
	public static final int CLASS_EMPTY = 27;
	public static final int FILTER_CHOOSER = 28;
	public static final int DBINSTALL_NAME = 29;
	public static final int DBINSTALL_DIR = 30;
	public static final int DBINSTALL_REP = 31;
	public static final int WEBINSTALL_DIR = 32;
	public static final int WEBINSTALL_URL = 33;
	public static final int OBSTAP_COMPONENT = 34;
	public static final int DATATABLE_EDITOR = 35;
	public static final int VO_PROTOCOL_FIELDS = 36;
	public static final int VO_PUBLISHED_RESOURCES = 37;
	public static final int EXTATT_EDIT = 38;
	
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
				, "Click on the meta data cell you want to edit."
				, "Utypes, UCds, units or field descriptions can be edited."
				});
		map.put(NODE_NAME, new String[] {
				"Name must only contain numbers"
				, "characters and _ and not start with a number"
				, "Forbidden characers are automatically rejected"
				});
		map.put(RELATION_COLLECTIONS, new String[] {
				"Primary collection is set by clicking " 
				,"in the choosen category node on the data tree."
				, "Secondary collection is set by dropping "
				, "the choosen category node on the text field."
				, "Neither filed can be edited by hand"
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
				, "Red relationships are not indexed"
				});
		map.put(VO_CURATION, new String[] {
				"This page does not publish anything in some registry"
				, "The purpose of the information given here "
				, "is just to provide helpful templates of registry resources"
				, "for data collection hosted by this SaadaDB."
		});
		map.put(MODEL_MAPPER, new String[] {
				"Set DM fields as a computation of native fields"
				, "Select first the class to map by clicking on a class node of the Database map ."
				, "Select the field you want to map and fill the mapping statement."
				, "The mapping statement must be a valid SQL statement mixing class fields and constant values."
				, "Class fields ca be dropped from the Saada attribute tree to the mapping statement"
		});
		map.put(MAPPING_QVIEW, new String[] {
				"Only non null DM columns are displayed"
				, "Both oidsaada and sky_pixel_csa columns are added to the DM in order to enable"
				, "the Saada download facility and to speed up positional queries"
				, "The sky_pixel_csa will be updated from the position columns of the DM."
				, "Modifications carried out on the above queries are not reported to the mapping."
				, "They can be used to test some mapping setup, but new mappings must then "
				, "be reported by hand to the back panel."
		});
		map.put(VOITEM_DESCRIPTION, new String[] {
				"The description can be edited by hand."
				,"It is stored in the saadadb_vo_capability table"
				,"and put in registry marks proposed by Saada"	
		});
		map.put(VOITEM_EDITION, new String[] {
				"Drop a data tree node in this area to add it ot the VO resource"
				,"Uncheck the box to remove it from the list."
				,"Edit its description if the description area."
				,"All changes are save bi clicking on the floppy disk icon."		
		});
		map.put(SQL_INDEX, new String[] {
				"Select a node (class or collection level) "
				,"to display its indexation status"
		});
		map.put(COLL_CREATE, new String[] {
				"Type the name of the new collection (reg. exp: [_a-zA-Z][_a-zA-Z0-9]*)"
				,"Type a description (optional) and run."
		});
		map.put(COLL_DROP, new String[] {
				"Click on the collection to remove on the data tree and run."
		});
		map.put(COLL_EMPTY, new String[] {
				"Click on the collection to empty on the data tree and run."
		});
		map.put(COLL_COMMENT, new String[] {
				"Click on the collection to be commented on the data tree,"
				,"Type a description and run."
		});
		map.put(CATEGORY_EMPTY, new String[] {
				"Click on the collection/category node to empty on the data tree and run."
		});
		map.put(CLASS_DROP, new String[] {
				"Click on the class to remove on the data tree and run."
		});
		map.put(CLASS_EMPTY, new String[] {
				"Click on the class to empty on the data tree and run."
		});
		map.put(CLASS_COMMENT, new String[] {
				"Click on the class to be commented on the data tree,"
				,"Type a description and run."
		});
		map.put(FILTER_CHOOSER, new String[] {
				"Select a node on the data tree either at category or class level"
				,"to display all loader filters (or configuarion) available for the choosen category."
				,"Click on one filter to see its description (formatted as command line parameters)"
		});
		map.put(DBINSTALL_NAME, new String[] {
				"Neither Saada DB name nof JDBC parameters can be changed without dammage"
				," If you need to do it; consoder rebuilding a new DB"
		});
		map.put(DBINSTALL_DIR, new String[] {
				"Changing the installation directory requires some manual setup:"
                , "  - Move the content of the old SaadaDB directory to the new one" 
                , "  - Update saadadb.properties file located in SaadaDBHOME/bin "
                , "  - Start admintool from the new location"
                , "  - Deploy the Web application."
		});
		map.put(DBINSTALL_REP, new String[] {
				"This operation does not move data from the old repository to the new one."
                , "  - Copy the old repository from the old location to the new one" 
                , "  - Deploy the Web application."
		});
		map.put(WEBINSTALL_DIR, new String[] {
				"Change the Tomcat install dir where the Web application is deployed"
                , "  - Works only if a writable directory named webapps does exist." 
                , "  - This action redeploys the Web application"
		});
		map.put(WEBINSTALL_URL, new String[] {
				"Change the database URL"
                , "  - Must be used when the tomcat server runs behind front-end HTTP server." 
                , "  - This action redeploys the Web application."
		});
		map.put(OBSTAP_COMPONENT, new String[] {
				"This is the list of Saada data classes currently mapped"
                , "and published in the Obstap serice."
                , "Removing a class from the ObsCore table does not suppress its mapping."
		});
		map.put(DATATABLE_EDITOR, new String[] {
				"Click on a data node (class or category) to display the related data table."
		});
		map.put(VO_PROTOCOL_FIELDS, new String[] {
				"Click on a protocol to display its data table structure."
		});
		map.put(VO_PUBLISHED_RESOURCES, new String[] {
				"Click on a protocol to display its published resources."
		});
		map.put(EXTATT_EDIT, new String[] {
				"The name of the new attribute must match /[a-zA-Z][_a-zA-Z0-9]*/"
				,"Type a description (optional) and run."
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
