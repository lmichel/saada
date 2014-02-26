package saadadb.admintool.utils;

import java.util.LinkedHashMap;
import java.util.Map;

public class ToolTipTextDesk 
{
	public static final Map<Integer, String[]> map;
	
	public static final int CREATE_COLLECTION = 1;
	public static final int DATA_LOADER = 2;
	public static final int EXPLORE_DATA = 3;
	public static final int MANAGE_DATA = 4;
	public static final int MANAGE_METADATA = 5;
	public static final int MANAGE_RELATIONS = 6;
	public static final int DB_INSTALL = 7;
	public static final int WEB_INSTALL = 8;
	public static final int VO_PUBLISH = 9;
	
	public static final int COMMENT_COLLECTION = 10;
	public static final int EMPTY_COLLECTION = 11;
	public static final int EMPTY_CATEGORY = 12;
	public static final int DROP_COLLECTION = 13;
	public static final int COMMENT_CLASS = 14;
	public static final int EMPTY_CLASS = 15;
	public static final int DROP_CLASS = 16;
	public static final int SQL_INDEX = 17;
	
	public static final int CREATE_RELATION = 18;
	public static final int COMMENT_RELATION = 19;
	public static final int DROP_RELATION = 20;
	public static final int POPULATE_RELATION = 21;
	public static final int INDEX_RELATION = 22;
	public static final int EMPTY_RELATION = 23;
	
	public static final int SIA_PUBLISH = 24;
	public static final int SSA_PUBLISH = 25;
	public static final int CONESEARCH_PUBLISH = 26;
	public static final int TAP_PUBLISH = 27;
	public static final int OBSCORE_MAPPER = 28;
	public static final int USER_DEFINED_DM = 29;
	public static final int VO_CURATOR = 30;
	public static final int VO_REGISTRY = 31;
	public static final int VO_PROTOCOL_FIELDS = 32;
	public static final int VO_PUBLISHED_RESOURCES = 33;
	public static final int DISPLAY_RELATION = 34;
	public static final int TAG_METADATA = 35;
	public static final int MANAGE_EXTATTR = 36;

	
	static{
		map = new LinkedHashMap<Integer, String[]>();
		
		// RootChoicePanel ToolTipText
		map.put(CREATE_COLLECTION, new String[] {"Create a collection", 
		"A collection contains your data with differents classes and categories."});	
		map.put(DATA_LOADER, new String[] {"Load Data", 
		"You must select either a category (IMAGE, SPECTRUM, ...) or a class in order to load data."});
		map.put(EXPLORE_DATA, new String[] {"Explore Data", 
		"You must select either a category (IMAGE, SPECTRUM, ...) or a class in order to explore data."});	
		map.put(MANAGE_DATA, new String[] {"Manage Data", 
		"You can manage your collections and your classes."});
		map.put(MANAGE_METADATA, new String[] {"Manage Meta Data", 
		"You must select either a category (IMAGE, SPECTRUM, ...) or a class in order to manage meta data."});
		map.put(MANAGE_RELATIONS, new String[] {"Manage Relationships", 
		"You must select either a category (IMAGE, SPECTRUM, ...) or a class in order to manage relationships"});
		map.put(DB_INSTALL, new String[] {"Database Installation", 
		"You can manage the database installation configuration."});
		map.put(WEB_INSTALL, new String[] {"Web Publishing", 
		"You can manage the web publishing configuration."});
		map.put(VO_PUBLISH, new String[] {"VO Publishing", 
		"You can manage your VO and publish data."});
		
		// ManageMetaDataPanel
		map.put(TAG_METADATA, new String[] {"Tag Columns", 
		"Set UCD/Unit/Description to data columns."});
		map.put(MANAGE_EXTATTR, new String[] {"Manage extened attribute", 
		"Add, remove or rename extended attributes of collections"});
		
		// ManageDataPanel
		map.put(COMMENT_COLLECTION, new String[] {"Comment Collection", 
		"You can add a description of the selected collection."});
		map.put(EMPTY_COLLECTION, new String[] {"Empty Collection", 
		"You can empty the selected collection."});
		map.put(EMPTY_CATEGORY, new String[] {"Empty Category", 
		"You can empty the selected category."});
		map.put(DROP_COLLECTION, new String[] {"Remove Collection", 
		"You can remove the selected collection."});
		map.put(COMMENT_CLASS, new String[] {"Comment Class", 
		"You can comment the selected class."});
		map.put(EMPTY_CLASS, new String[] {"Empty Class", 
		"You can empty the selected class."});
		map.put(DROP_CLASS, new String[] {"Remove Class", 
		"You can remove the selected class."});
		map.put(SQL_INDEX, new String[] {"SQL Index", 
		"You can add a SQL Index in the selected category or table."});
		
		// RelationChoicePanel
		map.put(CREATE_RELATION, new String[] {"New Relationship", 
		"Create a new relationship from the selected table or category."});
		map.put(COMMENT_RELATION, new String[] {"Comment Relationship", 
		"Comment a relationship from the selected table or category."});
		map.put(DROP_RELATION, new String[] {"Drop Relationship", 
		"Drop a relationship from the selected table or category."});
		map.put(POPULATE_RELATION, new String[] {"Populate Relationship", 
		"Populate a relationship from the selected table or category."});
		map.put(INDEX_RELATION, new String[] {"Index Relationship", 
		"Create a new index on a relationship."});
		map.put(EMPTY_RELATION, new String[] {"Empty Relationship", 
		"Empty a relationship from the selected table or category."});
		map.put(DISPLAY_RELATION, new String[] {"Display All Relationships",
		"Display all relationships created in the application."});
		
		// VOPublishPanel
		map.put(SIA_PUBLISH, new String[] {"Publish SIA", 
		"You can publish with SIA protocol."});
		map.put(SSA_PUBLISH, new String[] {"Publish SSA", 
		"You can publish with SSA protocol."});
		map.put(CONESEARCH_PUBLISH, new String[] {"Publish SCS", 
		"You can publish with SCS protocol."});
		map.put(TAP_PUBLISH, new String[] {"Publish TAP service", 
		"You can publish with TAP service."});
		map.put(OBSCORE_MAPPER, new String[] {"Publish ObsCore Table", 
		"You can publish with Obscore Table."});
		map.put(USER_DEFINED_DM, new String[] {"Publish User Defined DM", 
		"You can publish with user defined DM."});
		map.put(VO_CURATOR, new String[] {"VO Services Summary", 
		"Summary of your VO Services"});
		map.put(VO_REGISTRY, new String[] {"VO Registry",
		"Summary of your VO Registry"});
		map.put(VO_PROTOCOL_FIELDS, new String[] {"Protocol Fields",
		"Tables structure of implemented protocols"});
		map.put(VO_PUBLISHED_RESOURCES, new String[] {"Published VO Resources",
		"List of published VO Resources"});
		
	}
	
	/**
	 * @param key
	 * @return
	 */
	public static final String[] get(int key){
		return map.get(key);
	}
}
