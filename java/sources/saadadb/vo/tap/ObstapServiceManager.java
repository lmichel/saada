package saadadb.vo.tap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import saadadb.admintool.utils.DataTreePath;
import saadadb.collection.Category;
import saadadb.command.ArgsParser;
import saadadb.command.EntityManager;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.meta.MetaCollection;
import saadadb.meta.UTypeHandler;
import saadadb.meta.VOResource;
import saadadb.sqltable.SQLTable;
import saadadb.sqltable.Table_Saada_VO_Capabilities;
import saadadb.sqltable.Table_Tap_Schema_Tables;
import saadadb.util.Merger;
import saadadb.util.Messenger;
import saadadb.util.RegExpMatcher;
import saadadb.vo.registry.Capability;
import saadadb.vocabulary.enums.VoProtocol;

/**
 * Publishing a collection to ObsTAP
 * 1) Add a line to saada_vo_capability:
 *     COLL.CATEGORY OBSTAP '' description
 * 2) Build the obscore view
 * 
 * Building the obscore view
 * 1) Listing all categories referenced in saada_vo_capability
 * 2) Building one query per category
 * 3) Building the view with UNIONs on collections registered in saada_vo_capability
 * @author michel
 *
 * 09/2015: Support of cross references in the column definitions set with the -ukw parameter 
 */
public class ObstapServiceManager extends EntityManager{
	public static final String name = "obscore";

	/**
	 * Drop the capability collection.category and rebuild the ObsTAP view
	 * @param collection
	 * @param category
	 * @throws Exception
	 */
	private void  dropCollectionFromView(String collection, int category) throws Exception{
		List<Capability> lc = new ArrayList<Capability>();
		Table_Saada_VO_Capabilities.loadCapabilities(lc, VoProtocol.ObsTAP);
		DataTreePath dataTreePath = new DataTreePath(collection, Category.explain(category), null);
		for( Capability c: lc){
			if( c.getDataTreePathString().equals(dataTreePath.toString()) ){
				Messenger.printMsg(Messenger.TRACE, "Removing " + c.getDataTreePathString() + " from ObsTAP");
				Table_Saada_VO_Capabilities.removeCapability(c);
				/*
				 * Commit to allow buildView to access newly stored capability (SQLITE)
				 */
				SQLTable.commitTransaction();
				SQLTable.beginTransaction();
				buildView();
				return;
			}
		}
		Messenger.printMsg(Messenger.WARNING, dataTreePath.toString() + " not published in ObsTAP");
	}

	/**
	 * Drop both  ObsTAP view  and capabilities
	 * @throws Exception
	 */
	private void dropObstapCapability()throws Exception {
		Messenger.printMsg(Messenger.TRACE, "Removing the ObsTAP capability");
		Table_Saada_VO_Capabilities.emptyTable(VoProtocol.ObsTAP);
		SQLTable.dropView(name);
		Database.makeCacheVOObsolete();
	}
	/**
	 * Publish collection.category in the ObsTAP service.
	 * The capability is first added to Table_Saada_VO_Capabilities
	 * And then the ObsTAP view is rebuilt
	 * @param collection
	 * @param category
	 * @param argsParser contains the ukw parameters used to set fields with constant values
	 * @throws Exception
	 */
	private void  publishCollection(String collection, int category, ArgsParser argsParser) throws Exception{
		List<Capability> lc = new ArrayList<Capability>();
		Table_Saada_VO_Capabilities.loadCapabilities(lc, VoProtocol.ObsTAP);
		DataTreePath dataTreePath = new DataTreePath(collection, Category.explain(category), null);
		/*
		 * Check whether the capability does not exist
		 */
		for( Capability c: lc){
			if( c.getDataTreePathString().equals(dataTreePath.toString()) ){
				QueryException.throwNewException(SaadaException.WRONG_PARAMETER, dataTreePath.toString() + " already published in ObsTAP: remove it first");
				return;
			}
		}
		Messenger.printMsg(Messenger.TRACE, "Publishing " + dataTreePath + " in ObsTAP");
		/*
		 * Build the new capability
		 */
		Capability capability = new Capability();
		capability.setDataTreePath(dataTreePath);
		capability.setProtocol(VoProtocol.ObsTAP);
		capability.setDescription("Published in ObsTAP");
		/*
		 * The ArgsPArser is stored within the access_url columns which is not used otherwise
		 * That is required to rebuild the ObsTAP view from the capabilities read in the Table_Saada_VO_Capabilities table
		 */
		capability.setAccessURL(argsParser.toString());
		Table_Saada_VO_Capabilities.addCapability(capability);
		/*
		 * Commit to allow buildView to access newly stored capability (SQLITE)
		 */
		SQLTable.commitTransaction();
		SQLTable.beginTransaction();
		/*
		 * Build the SQL view
		 */
		buildView();
	}

	/**
	 * Drop the current ObsTAP  view and build a new one from the capabilities read in  Table_Saada_VO_Capabilities
	 * @throws Exception
	 */
	private static void buildView() throws Exception {
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Building the Obstap view");
		SQLTable.dropView(name);
		SQLTable.addQueryToTransaction(ObstapServiceManager.getViewStatement());
		Database.makeCacheVOObsolete();
	}

	/**
	 * @return the SQL string building the view
	 * @throws Exception
	 */
	private static String getViewStatement()throws Exception {
		List<Capability> lc = new ArrayList<Capability>();
		Table_Saada_VO_Capabilities.loadCapabilities(lc, VoProtocol.ObsTAP);
		String retour = ""; 
		for( Capability c: lc){
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Building the view statement for " + c.getDataTreePathString());
			String category = c.getDataTreePath().getElements()[1];
			if( retour.length() > 0 ) {
				retour += " UNION ";
			}
			/*
			 * Extract the ArgsParser used to build that specific capability 
			 * It is used to reconstruct the view exactly as it was
			 */
			RegExpMatcher regExpMatcher = new RegExpMatcher("ArgsParser\\((.*)\\)", 1 );
			List<String> m = regExpMatcher.getMatches(c.getAccessURL());
			ArgsParser ap;
			if( m.size() == 1){
				ap = new ArgsParser(m.get(0).split("\\s"));
			} else {
				ap = new ArgsParser("");
			}
			/*
			 * Add the SQL statement specific for this collection.category
			 */
			retour += getSelectStatement(Category.getCategory(category), ap) + " FROM " + c.getDataTreePath().getElements()[0] + "_" + c.getDataTreePath().getElements()[1];
		}
		return (retour.length() > 0 ? ("CREATE VIEW " + name + " AS "  + retour) 
				: ("CREATE VIEW " + name + " AS "  + getSelectStatement()));
	}
	/**
	 * returns an SQL statement doing an ObsTAP view for the category according to the ap parameters
	 * @param category 
	 * @param argsParser
	 * @return 
	 * @throws Exception
	 */
	private static String getSelectStatement(int category, ArgsParser argsParser) throws Exception{
		if (Messenger.debug_mode)
			Messenger.locateCode("Process category " + category + " with " + argsParser.toString());
		/*
		 * Vo model fields
		 */
		List<UTypeHandler> uths = VOResource.getResource(name).getUTypeHandlers();
		/*
		 * Field implemented in the category
		 */
		Map<String, AttributeHandler> ahs = MetaCollection.getAttribute_handlers(category);
		/*
		 * SQL fields used to build the view
		 */
		List<String> sqlFields = new ArrayList<String>();

		for(UTypeHandler uth: uths ){
			if(uth.isMandatory()){
				String uhn = uth.getNickname();
				String userVal = null;
				/*
				 * Values given by the user parameters are taken in priority
				 */
				if( (userVal = argsParser.getUserKeyword(uhn)) != null ) {
					userVal = userVal.replaceAll("'",  "");
					
					if( uth.getType().equals("char")){
						/*
						 * Look for other columns references in any String field
						 */
						userVal = Database.getWrapper().getStrcatOpWithVariables(userVal);
						sqlFields.add( userVal + " AS " + uhn);
					} else {
						if( userVal.indexOf("$") > -1){
							QueryException.throwNewException(SaadaException.UNSUPPORTED_OPERATION, "User keywords (" + userVal + ") support inner references ($) only for string fields");
						}
						sqlFields.add( userVal + " AS " + uhn);
					}
				/*
				 * dataproduct_type field matches the Saada category
				 */
				} else if(uhn.equals("dataproduct_type")) {
					sqlFields.add("'" + Category.explain(category).toLowerCase() +  "' AS " + uhn);
				/*
				 * Take the Saada field if it exist
				 */
				} else	if( ahs.keySet().contains(uhn)) {
					sqlFields.add(uhn);		
				} else {
					sqlFields.add("NULL AS " + uhn);
				}
			} 
		}
		return "SELECT " + Merger.getMergedCollection(sqlFields);
	}

	/**
	 * returns an SQL statement doing a dummy ObsTAP view
	 * @return 
	 * @throws Exception
	 */
	private static String getSelectStatement() throws Exception{
		if (Messenger.debug_mode)
			Messenger.locateCode("Process no category  " );
		/*
		 * Vo model fields
		 */
		List<UTypeHandler> uths = VOResource.getResource(name).getUTypeHandlers();
		/*
		 * SQL fields used to build the view
		 */
		List<String> sqlFields = new ArrayList<String>();

		for(UTypeHandler uth: uths ){
			if(uth.isMandatory()){
				String uhn = uth.getNickname();
				sqlFields.add("NULL AS " + uhn);
			} 
		}
		return "SELECT " + Merger.getMergedCollection(sqlFields);
	}

	/* (non-Javadoc)
	 * @see saadadb.command.EntityManager#create(saadadb.command.ArgsParser)
	 */
	@Override
	public void create(ArgsParser ap) throws SaadaException {
		Messenger.printMsg(Messenger.ERROR, "Not implemented for ObsTAP service manager");
	}

	/* (non-Javadoc)
	 * @see saadadb.command.EntityManager#rename(saadadb.command.ArgsParser)
	 */
	@Override
	public void rename(ArgsParser ap) throws SaadaException {
		Messenger.printMsg(Messenger.ERROR, "Not implemented for ObsTAP service manager");
	}

	/* (non-Javadoc)
	 * @see saadadb.command.EntityManager#empty(saadadb.command.ArgsParser)
	 */
	@Override
	public void empty(ArgsParser ap) throws SaadaException {
		remove(ap);
	}

	/* (non-Javadoc)
	 * @see saadadb.command.EntityManager#remove(saadadb.command.ArgsParser)
	 */
	@Override
	public void remove(ArgsParser ap) throws SaadaException {
		String collection = ap.getRemove();
		String category = ap.getCategory();		
		try {
			/*
			 * If collection is obscore the service OBSTAP is removed
			 */
			if( "obscore".equalsIgnoreCase(collection) || "ivoa.obscore".equalsIgnoreCase(collection)){
				Messenger.printMsg(Messenger.TRACE, "Removing Obscore from TAP_SCHEMA");
				Table_Tap_Schema_Tables.dropPublishedSchema("ivoa");
				dropObstapCapability();
			/*
			 * Otherwise only the collection is removed from ObsTAP
			 */
			} else {
				dropCollectionFromView(collection, Category.getCategory(category));
			}
		} catch (Exception e) {
			e.printStackTrace();
			FatalException.throwNewException(SaadaException.DB_ERROR, e);
		}
	}

	/* (non-Javadoc)
	 * @see saadadb.command.EntityManager#populate(saadadb.command.ArgsParser)
	 */
	@Override
	public void populate(ArgsParser ap) throws SaadaException {
		String collection = ap.getPopulate();
		/*
		 * Check parameters
		 */
		if( !Database.getCachemeta().collectionExists(collection) ){
			FatalException.throwNewException(SaadaException.WRONG_PARAMETER, "Collection " + collection + " does not exist");
		}
		/*
		 * Check TAP service
		 */
		if( !Table_Tap_Schema_Tables.doesTapShemaExist()){
			FatalException.throwNewException(SaadaException.DB_ERROR, "TAP service does not exist");
		}
		/*
		 * publish  the resource ivoa.obscore in TAP if not already done
		 */
		try {
			if( !Table_Tap_Schema_Tables.knowsTable("ivoa", name)) {
				Messenger.printMsg(Messenger.TRACE, "Publishing Obscore in TAP_SCHEMA");
				(new TapServiceManager()).populate(new ArgsParser(new String[]{"-populate=" + name}));
			}
		} catch (Exception e) {
			FatalException.throwNewException(SaadaException.DB_ERROR, "No running TAP service");
		}

		String category = null;				
		category = ap.getCategory();	
		if( category == null) {
			FatalException.throwNewException(SaadaException.WRONG_PARAMETER, "There is no valid category specified");
		}
		try {
			publishCollection(collection, Category.getCategory(category), ap);
		} catch (SaadaException e) {
			FatalException.throwNewException(SaadaException.DB_ERROR, e);
		} catch( Exception e2){
			Messenger.printStackTrace(e2);
			FatalException.throwNewException(SaadaException.DB_ERROR, e2);			
		}
	}

	/* (non-Javadoc)
	 * @see saadadb.command.EntityManager#index(saadadb.command.ArgsParser)
	 */
	@Override
	public void index(ArgsParser ap) throws SaadaException {
		Messenger.printMsg(Messenger.ERROR, "Not implemented for ObsTAP service manager");
	}

	/* (non-Javadoc)
	 * @see saadadb.command.EntityManager#comment(saadadb.command.ArgsParser)
	 */
	@Override
	public void comment(ArgsParser ap) throws SaadaException {
		Messenger.printMsg(Messenger.ERROR, "Not implemented for ObsTAP service manager");
	}
}
