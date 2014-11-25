package saadadb.sqltable;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;

import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.database.SaadaDBConnector;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.UTypeHandler;
import saadadb.meta.VOResource;
import saadadb.util.Messenger;

public class Table_Saada_VO_Resources extends SQLTable {

	/**
	 * @param connector : used to know the director where resource files are stored
	 * @throws AbortException
	 */
	public static  void createTable(SaadaDBConnector connector) throws Exception {
		SQLTable.createTable("saada_vo_resources", "pk " + Database.getWrapper().getSerialToken() 
				+ ", resource text, field_group text, nickname text , utype text, ucd text, type text DEFAULT 'char', arraysize " 
				+ Database.getWrapper().getIndexableTextType() + " DEFAULT '*', hidden boolean DEFAULT false, value " 
				+ Database.getWrapper().getIndexableTextType() + " DEFAULT '', expression " 
				+ Database.getWrapper().getIndexableTextType() + " DEFAULT '', description " 
				+ Database.getWrapper().getIndexableTextType() + " DEFAULT ''"
				, "pk"
				, false);
		loadFromConfigFiles(connector);
		//		saveDefaultSSAResource();
		//		saveDefaultSIAResource();
		//		saveDefaultCSResource();
		//		saveEpicSSAResource();
	}

	/**
	 */
	private static void saveDefaultSSAResource() {
		LinkedHashMap<String, UTypeHandler[]>SSA_DM =  new LinkedHashMap<String, UTypeHandler[]>();

		SSA_DM.put("Access", new UTypeHandler[]{
				new UTypeHandler("","Access.Reference"   ,"meta.ref.url"            , "char"  , -1, false, "", "",""),
				new UTypeHandler("","Access.Format"      ,""                        , "char"  , -1, false, "", "","")});
		SSA_DM.put("Spectrum", new UTypeHandler[]{
				new UTypeHandler("","Dataset.DataModel"  , ""                       , "char"  , -1, false, "Spectrum-1.02","",""),
				new UTypeHandler("","Dataset.Length"     , "meta.number"            , "long"  ,  1, false, "x", "","")});
		SSA_DM.put("DataID", new UTypeHandler[]{
				new UTypeHandler("","DataID.Title"       , "meta.title;meta.dataset", "char"  , -1, false, "", "","")});
		SSA_DM.put("Curation", new UTypeHandler[]{
				new UTypeHandler("", "Curation.Publisher", "meta.curation"          , "char"  , -1, false, "SaadaDB Instance","","")});
		SSA_DM.put("Char.SpatialAxis", new UTypeHandler[]{
				new UTypeHandler("", "Char.SpatialAxis.Coverage.Location.Value" , "pos.eq"         , "double",  2, false, "", "",""),
				new UTypeHandler("", "Char.SpatialAxis.Coverage.Bounds.Extent"  , "instr.fov"      , "double",  1, false, "", "","")});
		SSA_DM.put("Char.SpectralAxis", new UTypeHandler[]{
				new UTypeHandler("", "Char.SpectralAxis.Coverage.Location.Value", "instr.bandpass" , "double",  1, false, "", "",""),
				new UTypeHandler("", "Char.SpectralAxis.Coverage.Bounds.Extent" , "instr.bandwidth", "double",  1, false, "", "","")});
		SSA_DM.put("Char.TimeAxis", new UTypeHandler[]{
				new UTypeHandler("", "Char.TimeAxis.Coverage.Location.Value"    , "time.epoch"     , "double",  1, false, "", "","")});

		for( Entry<String, UTypeHandler[]> e: SSA_DM.entrySet() ) {
			for( UTypeHandler uth: e.getValue()) {
				try {
					Table_Saada_VO_Resources.storeField("SSA default", e.getKey(), uth);
				} catch (Exception e1) {
					e1.printStackTrace();
					System.exit(0);
				}
			}
		}
	}

	/**
	 */
	private static void saveDefaultSIAResource() {
		LinkedHashMap<String, UTypeHandler[]>SIA_DM = new LinkedHashMap<String, UTypeHandler[]>();

		SIA_DM.put("Access", new UTypeHandler[]{
				new UTypeHandler("Format"  , "", "VOX:Image_Format"         , "char",-1, false, "", "",""),
				new UTypeHandler("DataLink", "", "VOX:Image_AccessReference", "char",-1, false, "", "","")
		});
		SIA_DM.put("Dataset", new UTypeHandler[]{
				new UTypeHandler("", "Dataset.DataModel", ""           , "char", -1, false, "SIAP 1.0","",""),
				new UTypeHandler("", "Dataset.Length"   , "meta.number", "long",  1, false, "x"       ,"","")
		});
		SIA_DM.put("DataID", new UTypeHandler[]{
				new UTypeHandler("SaadaName","","meta.id","char",-1, false, "", "","")
		});
		SIA_DM.put("Curation", new UTypeHandler[]{
				new UTypeHandler("","Curation.Publisher","meta.curation","char",-1, false, "SaadaDB Instance","","")
		});
		SIA_DM.put("Char.Image", new UTypeHandler[]{
				new UTypeHandler("RA"       ,"","POS_EQ_RA_MAIN" ,"double", 1, false, "", "",""),
				new UTypeHandler("DEC"      ,"","POS_EQ_DEC_MAIN","double", 1, false, "", "",""),
				new UTypeHandler("Naxes"    ,"","VOX:Image_Naxes","int"   , 1, false, "", "",""),
				new UTypeHandler("Naxis"    ,"","VOX:Image_Naxis","int"   , -1, false, "", "",""),
				new UTypeHandler("Scale"    ,"","VOX:Image_Scale","double", -1, false, "", "",""),
				new UTypeHandler("SaadaName","","VOX:Image_Title","char"  ,-1, false, "", "","")
		});

		for( Entry<String, UTypeHandler[]> e: SIA_DM.entrySet() ) {
			for( UTypeHandler uth: e.getValue()) {
				try {
					Table_Saada_VO_Resources.storeField("SIA default", e.getKey(), uth);
				} catch (Exception e1) {
					e1.printStackTrace();
					System.exit(0);
				}
			}
		}
	}

	/**
	 */
	private static void saveDefaultCSResource() {
		LinkedHashMap<String, UTypeHandler[]>CS_DM = new LinkedHashMap<String, UTypeHandler[]>();

		CS_DM.put("DataID", new UTypeHandler[]{   
				new UTypeHandler("unique_id", "", "ID_MAIN"   , "char", -1, false, "", "",""),
				new UTypeHandler("SaadaName", "", "meta.title", "char", -1, false, "", "",""),
				new UTypeHandler("","DataID.Title","meta.title;meta.dataset","char",-1, false, "", "","")
		});
		CS_DM.put("Dataset", new UTypeHandler[]{
				new UTypeHandler("","Dataset.DataModel",""           ,"char",-1 ,false, "SIAP 1.0", "",""),
				new UTypeHandler("","Dataset.Length"   ,"meta.number","long", 1, false, "x","","")
		});
		CS_DM.put("Curation", new UTypeHandler[]{
				new UTypeHandler("","Curation.Publisher", "meta.curation", "char",-1, false, "SaadaDB Instance", "","")
		});
		CS_DM.put("Char.Image", new UTypeHandler[]{
				new UTypeHandler("urlCS"  ,""             ,"VOX:Image_AccessReference","char"  , -1, true , ""       , "Requested by Aladin to handle assoc.",""),
				new UTypeHandler("catalog","Access.Format",""                         ,"char"  , -1, true , ""       , "Requested by Aladin to handle assoc.",""),
				new UTypeHandler("RA"     ,""             ,"POS_EQ_RA_MAIN"           ,"double",  1, false, ""       , "",""),
				new UTypeHandler("DEC"    ,""             ,"POS_EQ_DEC_MAIN"          ,"double",  1, false, ""       , "",""),
		});

		for( Entry<String, UTypeHandler[]> e: CS_DM.entrySet() ) {
			for( UTypeHandler uth: e.getValue()) {
				try {
					Table_Saada_VO_Resources.storeField("Cone Search default", e.getKey(), uth);
				} catch (Exception e1) {
					e1.printStackTrace();
					System.exit(0);
				}
			}
		}
	}



	/**
	 * @throws AbortException 
	/**
	 * @param resources
	 * @param group
	 * @param uth
	 * @throws AbortException
	 */
	public static void storeField(String resources, String group, UTypeHandler uth) throws Exception {
		String query = "INSERT INTO saada_vo_resources VALUES (" + Database.getWrapper().getInsertAutoincrementStatement() +  ", " 
		+ "'"   + resources + "'"
		+ ", '" + group + "'";
		if( uth.getNickname() != null ) {
			query += ", '" +  Database.getWrapper().getEscapeQuote(uth.getNickname()) + "'";
		} else {
			query += ", null";
		}
		
		if( uth.getUtype() != null ) {
			query += ", '" +  Database.getWrapper().getEscapeQuote(uth.getUtype()) + "'";
		} else {
			query += ", null";
		}
		
		if( uth.getUcd() != null ) {
			query += ", '" +  Database.getWrapper().getEscapeQuote(uth.getUcd()) + "'";
		} else {
			query += ", null";
		}
		
		if( uth.getType() != null ) {
			query += ", '" +  Database.getWrapper().getEscapeQuote(uth.getType()) + "'";
		} else {
			query += ", null";
		}
		
		if( uth.getArraysize() == -1 ) {
			query += ", '*'";
		} else {
			query += ", '" +  uth.getArraysize() + "'";
		} 		
		query += ", " +  Database.getWrapper().getBooleanAsString(uth.isHidden());		
		if( uth.getValue() != null ) {
			query += ", '" +  Database.getWrapper().getEscapeQuote(uth.getValue()) + "'";
		} else {
			query += ", ''";

		}
		if(uth.getExpression() != null) {
			query += ", '" +  Database.getWrapper().getEscapeQuote(uth.getExpression()) + "'";
		} else {
			query += ", ''";
		}

		if( uth.getComment() != null ) {
			query += ", '" +  Database.getWrapper().getEscapeQuote(uth.getComment()) + "'";
		} else {
			query += ", ''";

		}
		SQLTable.addQueryToTransaction(query + ")", "saada_vo_resources");
	}

	/**
	 * @param connector 
	 * @throws Exception 
	 * 
	 */
	public static void loadFromConfigFiles(SaadaDBConnector connector) throws Exception {
		File confdir = new File(connector.getRoot_dir() + Database.getSepar() + "config");
		if( confdir.isDirectory() == false ) {
			FatalException.throwNewException(SaadaException.MISSING_FILE,  "<" + confdir + "> is not a directory. Your SaadaDB install is corrupted");
		}
		else {
			String[] files = confdir.list();
			String base = confdir.getAbsolutePath() + Database.getSepar();
			for( String file: files ) {
				File cf = new File(base + file);
				if( file.matches("vodm\\..*\\.xml") && cf.isFile() ) {
					Messenger.printMsg(Messenger.TRACE, "Load DM file " + file);
					loadFromConfigFile(cf.getAbsolutePath());
				}
			}
		}
	}

	/**
	 * @param resource
	 * @throws AbortException
	 */
	public static void removeResource(String resource) throws AbortException {
		Messenger.printMsg(Messenger.TRACE, "Remove resource " + resource);
		SQLTable.addQueryToTransaction("DELETE FROM saada_vo_resources WHERE resource = '" + resource + "'", "saada_vo_resources");
	}
	/**
	 * @param file_path
	 * @throws Exception
	 */
	public static void loadFromConfigFile(String file_path ) throws Exception {
		VOResource vor = new VOResource(file_path);
		Table_Saada_VO_Resources.removeResource(vor.getName());
		Messenger.printMsg(Messenger.TRACE, "Load resource file " + file_path);
		for( Entry<String, Set<UTypeHandler>> e: vor.getGroups().entrySet() ) {
			for( UTypeHandler uth: e.getValue()) {
				Table_Saada_VO_Resources.storeField(vor.getName(), e.getKey(), uth);
			}
		}	
	}


	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main_org(String[] args) throws Exception {
		ArgsParser ap = new ArgsParser(args);
		Database.init(ap.getDBName());
		Database.setAdminMode(ap.getPassword());
		Messenger.printMsg(Messenger.TRACE, "Reload al data models");
		SQLTable.addQueryToTransaction("delete from saada_vo_resources");
		saveDefaultSSAResource();
		saveDefaultSIAResource();
		saveDefaultCSResource();
		loadFromConfigFiles(Database.getConnector());
	}

	public static void main(String[] args) throws Exception {
		ArgsParser ap = new ArgsParser(args);
		Database.init(ap.getDBName());
		Database.setAdminMode(ap.getPassword());
		String filename = ap.getFilename();
		SQLTable.beginTransaction();
		loadFromConfigFile(filename);		
		SQLTable.commitTransaction();
		Database.close();

	}
}
