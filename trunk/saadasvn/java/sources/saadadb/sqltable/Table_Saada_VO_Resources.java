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
		SQLTable.createTable("saada_vo_resources", "pk " + Database.getWrapper().getSerialToken() + ", resource text, field_group text, nickname text null , utype text, ucd text, type " 
				+ Database.getWrapper().getIndexableTextType() + " DEFAULT 'char', arraysize " 
				+ Database.getWrapper().getIndexableTextType() + " DEFAULT '*', hidden boolean DEFAULT false, value " 
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
				new UTypeHandler("","Access.Reference"   ,"meta.ref.url"            , "char"  , -1, false, "", ""),
				new UTypeHandler("","Access.Format"      ,""                        , "char"  , -1, false, "", "")});
		SSA_DM.put("Spectrum", new UTypeHandler[]{
				new UTypeHandler("","Dataset.DataModel"  , ""                       , "char"  , -1, false, "Spectrum-1.02",""),
				new UTypeHandler("","Dataset.Length"     , "meta.number"            , "long"  ,  1, false, "x", "")});
		SSA_DM.put("DataID", new UTypeHandler[]{
				new UTypeHandler("","DataID.Title"       , "meta.title;meta.dataset", "char"  , -1, false, "", "")});
		SSA_DM.put("Curation", new UTypeHandler[]{
				new UTypeHandler("", "Curation.Publisher", "meta.curation"          , "char"  , -1, false, "SaadaDB Instance","")});
		SSA_DM.put("Char.SpatialAxis", new UTypeHandler[]{
				new UTypeHandler("", "Char.SpatialAxis.Coverage.Location.Value" , "pos.eq"         , "double",  2, false, "", ""),
				new UTypeHandler("", "Char.SpatialAxis.Coverage.Bounds.Extent"  , "instr.fov"      , "double",  1, false, "", "")});
		SSA_DM.put("Char.SpectralAxis", new UTypeHandler[]{
				new UTypeHandler("", "Char.SpectralAxis.Coverage.Location.Value", "instr.bandpass" , "double",  1, false, "", ""),
				new UTypeHandler("", "Char.SpectralAxis.Coverage.Bounds.Extent" , "instr.bandwidth", "double",  1, false, "", "")});
		SSA_DM.put("Char.TimeAxis", new UTypeHandler[]{
				new UTypeHandler("", "Char.TimeAxis.Coverage.Location.Value"    , "time.epoch"     , "double",  1, false, "", "")});

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
				new UTypeHandler("Format"  , "", "VOX:Image_Format"         , "char",-1, false, "", ""),
				new UTypeHandler("DataLink", "", "VOX:Image_AccessReference", "char",-1, false, "", "")
		});
		SIA_DM.put("Dataset", new UTypeHandler[]{
				new UTypeHandler("", "Dataset.DataModel", ""           , "char", -1, false, "SIAP 1.0",""),
				new UTypeHandler("", "Dataset.Length"   , "meta.number", "long",  1, false, "x"       ,"")
		});
		SIA_DM.put("DataID", new UTypeHandler[]{
				new UTypeHandler("SaadaName","","meta.id","char",-1, false, "", "")
		});
		SIA_DM.put("Curation", new UTypeHandler[]{
				new UTypeHandler("","Curation.Publisher","meta.curation","char",-1, false, "SaadaDB Instance","")
		});
		SIA_DM.put("Char.Image", new UTypeHandler[]{
				new UTypeHandler("RA"       ,"","POS_EQ_RA_MAIN" ,"double", 1, false, "", ""),
				new UTypeHandler("DEC"      ,"","POS_EQ_DEC_MAIN","double", 1, false, "", ""),
				new UTypeHandler("Naxes"    ,"","VOX:Image_Naxes","int"   , 1, false, "", ""),
				new UTypeHandler("Naxis"    ,"","VOX:Image_Naxis","int"   , -1, false, "", ""),
				new UTypeHandler("Scale"    ,"","VOX:Image_Scale","double", -1, false, "", ""),
				new UTypeHandler("SaadaName","","VOX:Image_Title","char"  ,-1, false, "", "")
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
				new UTypeHandler("unique_id", "", "ID_MAIN"   , "char", -1, false, "", ""),
				new UTypeHandler("SaadaName", "", "meta.title", "char", -1, false, "", ""),
				new UTypeHandler("","DataID.Title","meta.title;meta.dataset","char",-1, false, "", "")
		});
		CS_DM.put("Dataset", new UTypeHandler[]{
				new UTypeHandler("","Dataset.DataModel",""           ,"char",-1 ,false, "SIAP 1.0", ""),
				new UTypeHandler("","Dataset.Length"   ,"meta.number","long", 1, false, "x","")
		});
		CS_DM.put("Curation", new UTypeHandler[]{
				new UTypeHandler("","Curation.Publisher", "meta.curation", "char",-1, false, "SaadaDB Instance", "")
		});
		CS_DM.put("Char.Image", new UTypeHandler[]{
				new UTypeHandler("urlCS"  ,""             ,"VOX:Image_AccessReference","char"  , -1, true , ""       , "Requested by Aladin to handle assoc."),
				new UTypeHandler("catalog","Access.Format",""                         ,"char"  , -1, true , ""       , "Requested by Aladin to handle assoc."),
				new UTypeHandler("RA"     ,""             ,"POS_EQ_RA_MAIN"           ,"double",  1, false, ""       , ""),
				new UTypeHandler("DEC"    ,""             ,"POS_EQ_DEC_MAIN"          ,"double",  1, false, ""       , ""),
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
	 */
	private static void saveEpicSSAResource() throws AbortException {
		LinkedHashMap<String, UTypeHandler[]>SSA_DM = null;
		
		SSA_DM = new LinkedHashMap<String, UTypeHandler[]>();

		SSA_DM.put("Query", new UTypeHandler[]{new UTypeHandler("","Query.Score","1.0","REC","","float",1),
				new UTypeHandler("","Query.Token","Not available","","","char",-1)});		
		SSA_DM.put("Association", new UTypeHandler[]{new UTypeHandler("","Association.Type","x","OPT","","char",-1),
				new UTypeHandler("","Association.ID","Not available","OPT","","char",-1),
				new UTypeHandler("","Association.Key","x","Not Set","","char",-1)});		
		SSA_DM.put("Access", new UTypeHandler[]{new UTypeHandler("","Access.Reference","","MAN","meta.ref.url","char",-1),
				new UTypeHandler("","Access.Format","","MAN","","char",-1),
				new UTypeHandler("","Access.Size","aaa","REC","","long",1)});		
		SSA_DM.put("Spectrum", new UTypeHandler[]{new UTypeHandler("","Dataset.DataModel","Spectrum-1.02","MAN","","char",-1),
				new UTypeHandler("","Dataset.Type","Spectrum","OPT","","char",-1),
				new UTypeHandler("","Dataset.Length","","MAN","meta.number","long",1),
				new UTypeHandler("","Dataset.Deleted","null","OPT","","",1),
				new UTypeHandler("","Dataset.TimeSI","Not available","OPT","time;arith.zp","double",1),
				new UTypeHandler("","Dataset.SpectralSI","Not available","OPT","","double",1),
				new UTypeHandler("","Dataset.FluxSI","Not available","OPT","","double",1),
				new UTypeHandler("","Dataset.SpectralAxis","CHANNEL","OPT","","char",-1),
				new UTypeHandler("","Dataset.FluxAxis","COUNTS","OPT","","char",-1)});
		SSA_DM.put("DataID", new UTypeHandler[]{new UTypeHandler("","DataID.Title","","MAN","meta.title;meta.dataset","char",-1),
				new UTypeHandler("","DataID.Creator","XMM-Newton SSC Consortium","REC","","char",-1),
				new UTypeHandler("","DataID.Collection","2XMM Catalogue","REC","","char",-1),
				new UTypeHandler("","DataID.DatasetID","2","OPT","meta.id;meta.dataset","char",-1),
				new UTypeHandler("","DataID.CreatorDID","Not available","REC","meta.id","char",-1),
				new UTypeHandler("","DataID.Date","","OPT","time;meta.dataset","char",-1),
				new UTypeHandler("","DataID.Version","","OPT","meta.version;meta.dataset","char",-1),
				new UTypeHandler("","DataID.Instrument","","OPT","meta.id;instr","char",-1),
				new UTypeHandler("","DataID.Bandpass","X-Ray","OPT","instr.bandpass","char",-1),
				new UTypeHandler("","DataID.DataSource","","REC","","char",-1),
				new UTypeHandler("","DataID.CreationType","archival","REC","","char",-1),
				new UTypeHandler("","DataID.Logo","http://xmmssc-www.star.le.ac.uk/Catalogue/2XMM_logo.jpg","","meta.ref.url","char",-1),
				new UTypeHandler("","DataID.Contributor","XMM-Newton SSC Consortium","","","char",-1)});
		SSA_DM.put("Curation", new UTypeHandler[]{new UTypeHandler("","Curation.Publisher","Observatory of Strasbourg SSC Team","MAN","meta.curation","char",-1),
				new UTypeHandler("","Curation.PublisherID","ivo://xcatdb","","meta.ref.url;meta.curation","char",-1),
				new UTypeHandler("","Curation.PublisherDID","ivo://xcatdb/epicssa","REC","meta.ref.url;meta.curation","char",-1),
				new UTypeHandler("","Curation.Date","2007/08/15","OPT","","char",-1),
				new UTypeHandler("","Curation.Version","1","OPT","meta.version;meta.curation","char",-1),
				new UTypeHandler("","Curation.Rights","public","OPT","","char",-1),
				new UTypeHandler("","Curation.Reference","Not available at this stage","REC","meta.bib.bibcode","char",-1),
				new UTypeHandler("","Curation.Contact.Name","Laurent MICHEL","","meta.bib.author;meta.curation","char",-1),
				new UTypeHandler("","Curation.Contact.Email","laurent.michel@astro.u-strasbg.fr","","meta.ref.url;meta.email","char",-1)});
		SSA_DM.put("Target", new UTypeHandler[]{new UTypeHandler("","Target.Name","","OPT","meta.id;src","char",-1),
				new UTypeHandler("","Target.Description","Not available","","meta.note;src","char",-1),
				new UTypeHandler("","Target.Class","X-Ray Source","OPT","src.class","char",-1),
				new UTypeHandler("","Target.Pos","","","pos.eq;src","double", 2),
				new UTypeHandler("","Target.SpectralClass","Not applicable","","src.spType","char",-1),
				new UTypeHandler("","Target.Redshift","Not available","OPT","src.redshift","double",1),
				new UTypeHandler("","Target.VarAmpl","Not available","OPT","src.var.amplitude","float",1)});
		SSA_DM.put("Derived", new UTypeHandler[]{new UTypeHandler("","Derived.SNR","Not available","OPT","stat.snr","float",1),
				new UTypeHandler("","Derived.Redshift.Value","Not available at this stage","","","double",1),
				new UTypeHandler("","Derived.Redshift.StatError","Not available at this stage","","stat.error;src.redshift","float",1),
				new UTypeHandler("","Derived.Redshift.Confidence","Not available at this stage","","","float",1),
				new UTypeHandler("","Derived.VarAmpl","Not available at this stage","","src.var.amplitude;arith.ratio","float",1)});
		SSA_DM.put("CoordSys", new UTypeHandler[]{new UTypeHandler("","CoordSys.ID","FK5(J2000.0)","","","char",-1),
				new UTypeHandler("","CoordSys.SpaceFrame.Name","x","REC","","char",-1),
				new UTypeHandler("","CoordSys.SpaceFrame.Ucd","pos.eq ","","","char",-1),
				new UTypeHandler("","CoordSys.SpaceFrame.RefPos","Not available","","","char",-1),
				new UTypeHandler("","CoordSys.SpaceFrame.Equinox","x","OPT","time.equinox;pos.frame","double",1),
				new UTypeHandler("","CoordSys.TimeFrame.Name","","OPT","time.scale","char",-1),
				new UTypeHandler("","CoordSys.TimeFrame.Ucd","time","","","char",-1),
				new UTypeHandler("","CoordSys.TimeFrame.Zero","","OPT","time;arith.zp","double",1),
				new UTypeHandler("","CoordSys.TimeFrame.RefPos","","","time.scale","char",-1),
				new UTypeHandler("","CoordSys.SpectralFrame.Name","channel","","","char",-1),
				new UTypeHandler("","CoordSys.SpectralFrame.Ucd","em.bin","","","char",-1),
				new UTypeHandler("","CoordSys.SpectralFrame.RefPos","","","","char",-1),
				new UTypeHandler("","CoordSys.SpectralFrame.Redshift","Not applicable","","","double",1),
				new UTypeHandler("","CoordSys.RedshiftFrame.Name","Not applicable","","","char",-1),
				new UTypeHandler("","CoordSys.RedshiftFrame.DopplerDefinition","Not applicable","","","char",-1),
				new UTypeHandler("","CoordSys.RedshiftFrame.RefPos","Not applicable","","","char",-1)});
		SSA_DM.put("Char.SpatialAxis", new UTypeHandler[]{new UTypeHandler("","Char.SpatialAxis.Name","Sky","","","char",-1),
				new UTypeHandler("","Char.SpatialAxis.Ucd","pos.eq ","","","char",-1),
				new UTypeHandler("","Char.SpatialAxis.Unit","pixel","","","char",-1),
				new UTypeHandler("","Char.SpatialAxis.Coverage.Location.Value","","MAN","pos.eq","double",2),
				new UTypeHandler("","Char.SpatialAxis.Coverage.Bounds.Extent","","MAN","instr.fov","double",1),
				new UTypeHandler("","Char.SpatialAxis.Coverage.Support.Area","circle xo yo r where xo yo r are from the above 3 columns.","OPT","","char",-1),
				new UTypeHandler("","Char.SpatialAxis.Coverage.Support.Extent","Source Position with Error Box","","instr.fov","double",1),
				new UTypeHandler("","Char.SpatialAxis.SamplingPrecision.SampleExtent","","","instr.pixel","float",1),
				new UTypeHandler("","Char.SpatialAxis.SamplingPrecision.SamplingPrecisionRefVal.FillFactor","","OPT","stat.fill;pos.eq","float",1),
				new UTypeHandler("","Char.SpatialAxis.Accuracy.StatError","Not applicable","REC","stat.error;pos.eq","double",1),
				new UTypeHandler("","Char.SpatialAxis.Accuracy.SysError","Not applicable","OPT","stat.error.sys;pos.eq","double",1),
				new UTypeHandler("","Char.SpatialAxis.Calibration","Calibrated against optical data","REC","meta.code.qual","char",-1),
				new UTypeHandler("","Char.SpatialAxis.Resolution","Not applicable","REC","pos.angResolution","double",1)});
		SSA_DM.put("Char.SpectralAxis", new UTypeHandler[]{new UTypeHandler("","Char.SpectralAxis.Name","Channel","","","char",-1),
				new UTypeHandler("","Char.SpectralAxis.Ucd","em.bin","REC","","char",-1),
				new UTypeHandler("","Char.SpectralAxis.Unit","ADU (analogue-digital unit)","","","char",-1),
				new UTypeHandler("","Char.SpectralAxis.Coverage.Location.Value","","MAN","instr.bandpass","double",1),
				new UTypeHandler("","Char.SpectralAxis.Coverage.Bounds.Extent","","MAN","instr.bandwidth","double",1),
				new UTypeHandler("","Char.SpectralAxis.Coverage.Bounds.Start","","REC","em;stat.min","double",1),
				new UTypeHandler("","Char.SpectralAxis.Coverage.Bounds.Stop","","REC","em;stat.max","double",1),
				new UTypeHandler("","Char.SpectralAxis.Coverage.Support.Extent","Single value Not applicable","","instr.bandwidth","double",1),
				new UTypeHandler("","Char.SpectralAxis.SamplingPrecision.SampleExtent","Single value Not applicable","","em;spect.binSize","double",1),
				new UTypeHandler("","Char.SpectralAxis.SamplingPrecision.SamplingPrecisionRefVal.FillFactor","1","OPT","stat.fill;em","float",1),
				new UTypeHandler("","Char.SpectralAxis.Accuracy.BinSize","Single value Not available: requires  rmf file","OPT","em;spec.binSize","double",1),
				new UTypeHandler("","Char.SpectralAxis.Accuracy.StatError","In principle Poisson error for each bin but Single value not available at this state since only raw data are given","OPT","stat.error;em","double",1),
				new UTypeHandler("","Char.SpectralAxis.Accuracy.SysError","Usually unknown source error. Not available at this state since only raw data are given","OPT","stat.error.sys;em","double",1),
				new UTypeHandler("","Char.SpectralAxis.Calibration","Energy Calibration","REC","meta.code.qual","char",-1),
				new UTypeHandler("","Char.SpectralAxis.Resolution","Not available at this stage: requires response matrix","REC","spect.resolution;em","double",1),
				new UTypeHandler("","Char.SpectralAxis.ResPower","Not available at this stage: requires response matrix","","spect.resolution","float",1)});
		SSA_DM.put("Char.TimeAxis", new UTypeHandler[]{new UTypeHandler("","Char.TimeAxis.Name","Time","","","char",-1),
				new UTypeHandler("","Char.TimeAxis.Ucd","time","","","char",-1),
				new UTypeHandler("","Char.TimeAxis.Unit","","","","char",-1),
				new UTypeHandler("","Char.TimeAxis.Coverage.Location.Value","","MAN","time.epoch","double",1),
				new UTypeHandler("","Char.TimeAxis.Coverage.Bounds.Extent","","REC","time.duration","double",1),
				new UTypeHandler("","Char.TimeAxis.Coverage.Bounds.Start","","OPT","time.start;obs.exposure","double",1),
				new UTypeHandler("","Char.TimeAxis.Coverage.Bounds.Stop","","OPT","time.stop;obs.exposure","double",1),
				new UTypeHandler("","Char.TimeAxis.Coverage.Support.Extent","","OPT","time.duration;obs.exposure","double",1),
				new UTypeHandler("","Char.TimeAxis.SamplingPrecision.SampleExtent","Not applicable","","time.interval","double",1),
				new UTypeHandler("","Char.TimeAxis.SamplingPrecision.SamplingPrecisionRefVal.FillFactor","","OPT","time;stat.fill;time","float",1),
				new UTypeHandler("","Char.TimeAxis.Accuracy.BinSize","Not applicable:","OPT","time.interval","double",1),
				new UTypeHandler("","Char.TimeAxis.Accuracy.StatError","Not applicable","OPT","stat.error;time","double",1),
				new UTypeHandler("","Char.TimeAxis.Accuracy.SysError","Not applicable: ","OPT","stat.error.sys;time","double",1),
				new UTypeHandler("","Char.TimeAxis.Calibration","Calibrated","OPT","meta.code.qual","char",-1),
				new UTypeHandler("","Char.TimeAxis.Resolution","Not applicable: for timeseries","OPT","time.resolution","double",1)});
		SSA_DM.put("Char.FluxAxis", new UTypeHandler[]{new UTypeHandler("","Char.FluxAxis.Name","Raw photon counts in spectral channel","","","char",-1),
				new UTypeHandler("","Char.FluxAxis.Ucd","phot.count","REC","","char",-1),
				new UTypeHandler("","Char.FluxAxis.Unit","counts","","","char",-1),
				new UTypeHandler("","Char.FluxAxis.Accuracy.StatError","Not available at this stage","OPT","stat.error;phot.flux.density;em","double",1),
				new UTypeHandler("","Char.FluxAxis.Accuracy.SysError","Not available at this stage","OPT","stat.error.sys;phot.flux.density;em","double",1),
				new UTypeHandler("","Char.FluxAxis.Calibration","Calibration is in response files.","REC","","char",-1)});
		SSA_DM.put("Service-defined.MetaData", new UTypeHandler[]{new UTypeHandler("","Background.File","","REC","","char",-1),
				new UTypeHandler("","Response.File","","REC","","char",-1),
				new UTypeHandler("","Ancillary.Respnse.File","","REC","","char",-1),
				new UTypeHandler("","Pipeline.Software.Version","","OPT","","char",-1)});

		for( Entry<String, UTypeHandler[]> e: SSA_DM.entrySet() ) {
			for( UTypeHandler uth: e.getValue()) {
				try {
					Table_Saada_VO_Resources.storeField("SSA EPIC Spectra", e.getKey(), uth);
				} catch (Exception e1) {
					e1.printStackTrace();
					System.exit(1);
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
		}
		else {
			query += ", null";
		}		
		if( uth.getUtype() != null ) {
			query += ", '" +  Database.getWrapper().getEscapeQuote(uth.getUtype()) + "'";
		}
		else {
			query += ", null";
		}
		if( uth.getUcd() != null ) {
			query += ", '" +  Database.getWrapper().getEscapeQuote(uth.getUcd()) + "'";
		}
		else {
			query += ", null";
		}
		if( uth.getType() != null ) {
			query += ", '" +  Database.getWrapper().getEscapeQuote(uth.getType()) + "'";
		}
		else {
			query += ", null";
		}
		if( uth.getArraysize() == -1 ) {
			query += ", '*'";
		}
		else {
			query += ", '" +  uth.getArraysize() + "'";
		} 		
		query += ", " +  Database.getWrapper().getBooleanAsString(uth.isHidden());		
		if( uth.getValue() != null ) {
			query += ", '" +  Database.getWrapper().getEscapeQuote(uth.getValue()) + "'";
		}
		else {
			query += ", ''";
			
		}
		if( uth.getComment() != null ) {
			query += ", '" +  Database.getWrapper().getEscapeQuote(uth.getComment()) + "'";
		}
		else {
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
		SQLTable.addQueryToTransaction("DELETE FROM saada_vo_resources WHERE resource = '" + resource + "'", "saada_vo_resources");
	}
	/**
	 * @param file_path
	 * @throws Exception
	 */
	public static void loadFromConfigFile(String file_path ) throws Exception {
		VOResource vor = new VOResource(file_path);
		Table_Saada_VO_Resources.removeResource(vor.getName());
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
		Database.getConnector().setAdminMode(ap.getPassword());
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
		Database.getConnector().setAdminMode(ap.getPassword());
		String filename = ap.getFilename();
		SQLTable.beginTransaction();
		loadFromConfigFile(filename);		
		SQLTable.commitTransaction();

	}
}
