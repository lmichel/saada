package saadadb.vo.formator;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;

import saadadb.api.SaadaLink;
import saadadb.collection.Category;
import saadadb.collection.ImageSaada;
import saadadb.collection.Position;
import saadadb.collection.SaadaInstance;
import saadadb.database.Database;
import saadadb.database.Repository;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.generationclass.SaadaClassReloader;
import saadadb.meta.MetaRelation;
import saadadb.meta.UTypeHandler;
import saadadb.query.result.SaadaQLResultSet;
import saadadb.util.Messenger;
import saadadb.util.SaadaConstant;
import cds.savot.model.CoosysSet;
import cds.savot.model.FieldRefSet;
import cds.savot.model.FieldSet;
import cds.savot.model.GroupSet;
import cds.savot.model.InfoSet;
import cds.savot.model.LinkSet;
import cds.savot.model.ParamSet;
import cds.savot.model.SavotCoosys;
import cds.savot.model.SavotField;
import cds.savot.model.SavotFieldRef;
import cds.savot.model.SavotGroup;
import cds.savot.model.SavotInfo;
import cds.savot.model.SavotLink;
import cds.savot.model.SavotParam;
import cds.savot.model.SavotResource;
import cds.savot.model.SavotTD;
import cds.savot.model.SavotTR;
import cds.savot.model.SavotTable;
import cds.savot.model.SavotVOTable;
import cds.savot.model.TDSet;
import cds.savot.writer.SavotWriter;

/**
 * Ancestor for any parser which transforms a list of SaadaQL OIDs into a File
 * or a String. We use the SAVOT 'cds.savot.writer.jar' and
 * 'cds.savot.model.jar' packages, developped by Andr� Schaaff for the Centre de
 * Donnees astronomiques de Strasbourg URL :
 * http://cdsweb.u-strasbg.fr/devcorner.gml
 */
/**
 * @author laurentmichel
 *@version $Id$
 */
/**
 * @author laurentmichel
 *
 */
public abstract class VOTableFormator extends VOResultFormator {
	
	protected SavotWriter writer;	
	protected SavotTable table;
	
	protected FieldSet fieldSet_dm;
	protected FieldSet fieldSet_housekeeping;
	
	protected TDSet tdSet;
	public static  String version ;
	
	
	/**
	 * Constructor.
	 * @throws SaadaException 
	 */
	public VOTableFormator(String voresource_name, String default_resource, String votable_desc,String vores_type, String data_desc) throws SaadaException {
		super(voresource_name, default_resource, votable_desc, vores_type, data_desc);
	}

	/**
	 * @param voresource_name
	 * @param default_resource
	 * @param votable_desc
	 * @param vores_type
	 * @param data_desc
	 * @param result_filename
	 * @throws SaadaException
	 */
	public VOTableFormator(String voresource_name, String default_resource, String votable_desc,String vores_type, String data_desc, String result_filename) throws SaadaException {
		super(voresource_name, default_resource, votable_desc, vores_type, data_desc, result_filename);
	}
	
	/**
	 * @param votable_desc
	 * @param vores_type
	 * @param data_desc
	 */
	public VOTableFormator(String votable_desc,String vores_type, String data_desc) {
		super(votable_desc, vores_type, data_desc);		
	}
	/**
	 * @param votable_desc
	 * @param vores_type
	 * @param data_desc
	 * @param result_filename
	 */
	public VOTableFormator(String votable_desc,String vores_type, String data_desc, String result_filename) {
		super(votable_desc, vores_type, data_desc, result_filename);		
	}


	/* (non-Javadoc)
	 * @see saadadb.vo.VOResultFormator#setDefaultReportFilename()
	 */
	public void setDefaultReportFilename() {
		this.result_filename = Repository.getVoreportsPath()
		+  Database.getSepar() 
		+ "SaadaQL_Result" + System.currentTimeMillis()
		+ ".xml";	
	}

    /* (non-Javadoc)
     * @see saadadb.vo.VOResultFormator#dumpResultFile(long[], java.io.Writer)
     */
    public void dumpResultFile(OutputStream out) throws Exception {
    	BufferedInputStream br = new BufferedInputStream(new FileInputStream(this.result_filename));
       	int lg;
    	byte[] target = new byte[1024];
    	while( (lg = br.read(target)) > 0  ) {
    		out.write(target, 0, lg);
     	}
    	out.flush();
    	br.close();
    }
    

	/**
	 * @param file
	 * @param oids
	 * @param description
	 * @param limit
	 * @throws Exception 
	 */
	protected void writeVOTableFile(File file, long[] oids, String description, int limit) throws Exception{
		writer = new SavotWriter();
		writer.initStream(file.getPath());
		createResultFile(oids);
		Messenger.printMsg(Messenger.TRACE, "Write file " + file.getAbsolutePath());
	}


	
	/**
	 * @param oids
	 * @return
	 * @throws Exception
	 */
	public String buildResultFile(long[] oids) throws Exception {
		File recup = new File(this.result_filename);
		initWriter(recup);
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Build temporary results file");
		createResultFile(oids);
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Votable stored in <" + recup.getAbsolutePath() + ">");
		return recup.getAbsolutePath();
	}
	/**
	 * @param oids
	 * @return
	 * @throws Exception
	 */
	public String buildResultFile(SaadaQLResultSet rs) throws Exception {
		File recup = new File(this.result_filename);
		initWriter(recup);
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Build temporary results file");
		createResultFile(rs);
		Messenger.printMsg(Messenger.TRACE, "Votable stored in <" + recup.getAbsolutePath() + ">");
		return recup.getAbsolutePath();
	}
	
	/* (non-Javadoc)
	 * @see saadadb.vo.VOResultFormator#buildResultErrorFile(java.lang.String)
	 */
	public String buildResultErrorFile(String cause) throws Exception{
		File recup = new File(this.result_filename);
		initWriter(recup);
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Build temporary results file");
		createResultErrorFile(cause);
		Messenger.printMsg(Messenger.TRACE, "Votable stored in <" + recup.getAbsolutePath() + ">");
		return recup.getAbsolutePath();
		
	}
	/**
	 * @param oids
	 * @return
	 * @throws Exception 
	 * @throws Exception
	 */
	public String buildMetadataFile() throws Exception  {
		File recup = new File(this.result_filename);
		initWriter(recup);
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Build temporary results file");
		createMetadataResultFile();
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Votbale stored in <" + recup.getAbsolutePath() + ">");
		return recup.getAbsolutePath();
	}

	/**
	 * Call this method when an error occured before producing the VOTable
	 * @param msg the error message to display in the VOTable output
	 * @return a String representation of the VOTable output containing the error message
	 */
	public void formatError(String msg) throws SaadaException {
		File recup = new File(this.result_filename);
		String result = null;
		writer = new SavotWriter();
		writer.initStream(recup.getPath());

		writeBeginingVOTable();
		ParamSet paramSet = new ParamSet();
		SavotParam param = new SavotParam();
		param.setValue(msg);
		param.setName("Error");
		paramSet.addItem(param);
		writer.writeParam(paramSet);
		writer.writeResourceEnd();
		writer.writeDocumentEnd();
	}

    /**
	 * @param oids
	 * @param category
	 * @param votable_desc
	 * @param restype
	 * @param data_desc
	 * @throws Exception
	 */
	protected void createResultFile(long[] oids) throws Exception{
		if( this.queryInfos != null ) {
			this.allowsExtensions(this.queryInfos.getCategory()) ;
		}
		if( limit <= 0 ) {
			this.limit = SaadaConstant.INT;
		}
		writeBeginingVOTable();
		
		InfoSet infoSet = new InfoSet();
		SavotInfo info = new SavotInfo();
		info.setValue("OK");
		info.setName("QUERY_STATUS");
		infoSet.addItem(info);
		writer.writeInfo(infoSet);
		
		table = new SavotTable();
		table.setName("Results");
		table.setDescription(data_desc);
		if( this.queryInfos != null ) {
			writer.writeComment("Query parameters:\n " + this.queryInfos.toString());
		}
		else {
			writer.writeComment("Counterpart selection");
		}
		
		writer.writeTableBegin(table);
		writeMetaData(oids);
		writeData(oids);
		writer.writeTableEnd();
		writer.writeResourceEnd();
		/*
		 * Add tables with linked data
		 */
		this.writeExtensions(oids, this.queryInfos.getCategory());
		writer.writeDocumentEnd();
	}
	
	/**
	 * @param cause
	 * @throws Exception
	 */
	protected void createResultErrorFile(String cause) throws Exception{
		if( limit <= 0 ) {
			this.limit = SaadaConstant.INT;
		}
		writeBeginingVOTable();
		
		InfoSet infoSet = new InfoSet();
		SavotInfo info = new SavotInfo();
		info.setValue("ERROR");
		info.setName("QUERY_STATUS");
		info.setContent("<![CDATA[" + cause + "]]>");
		infoSet.addItem(info);
		writer.writeInfo(infoSet);
		
		ParamSet paramSet = new ParamSet();
		SavotParam sp = new SavotParam();
		sp.setId("Error");
		sp.setName("ERROR");
		sp.setValue(protectSpecialCharacters(cause));
		paramSet.addItem(sp);	
		writer.writeParam(paramSet);
		writer.writeResourceEnd();
		writer.writeDocumentEnd();	
	}


	/* (non-Javadoc)
	 * @see saadadb.vo.formator.VOResultFormator#createResultFile(saadadb.query.result.SaadaQLResultSet)
	 */
	protected void createResultFile(SaadaQLResultSet rs) throws Exception{
		this.allowsExtensions(this.queryInfos.getCategory()) ;
		if( limit <= 0 ) {
			this.limit = SaadaConstant.INT;
		}
		writeBeginingVOTable();
		
		InfoSet infoSet = new InfoSet();
		SavotInfo info = new SavotInfo();
		info.setValue("OK");
		info.setName("QUERY_STATUS");
		infoSet.addItem(info);
		writer.writeInfo(infoSet);
		
		table = new SavotTable();
		table.setName("Results");
		table.setDescription(data_desc);
		if( this.queryInfos != null ) {
			writer.writeComment("Query parameters:\n " + this.queryInfos.toString());
		}
		else {
			writer.writeComment("Counterpart selection");
		}
		
		writer.writeTableBegin(table);
		writeMetaData(rs);
		writeData(rs);
		writer.writeTableEnd();
		writer.writeResourceEnd();
		/*
		 * Add tables with linked data
		 */
		this.writeExtensions(rs, this.queryInfos.getCategory());
		writer.writeDocumentEnd();
	}
	
	
	/* (non-Javadoc)
	 * @see saadadb.vo.VOResultFormator#createMetadataResultFile(long[], int, java.lang.String, java.lang.String, java.lang.String, int)
	 */
	protected void createMetadataResultFile() throws Exception{
		this.allowsExtensions(this.queryInfos.getCategory());
		if( limit <= 0 ) {
			this.limit = SaadaConstant.INT;
		}
		writeBeginingVOTable();
		
		InfoSet infoSet = new InfoSet();
		SavotInfo info = new SavotInfo();
		info.setValue("OK");
		info.setName("QUERY_STATUS");
		infoSet.addItem(info);
		
		info = new SavotInfo();
		info.setValue(version);
		info.setName("SERVICE_PROTOCOL");
		info.setContent("SSAP");
		infoSet.addItem(info);
		
		writer.writeInfo(infoSet);
		if( this.queryInfos != null ) {
			writer.writeComment("Query parameters:\n " + this.queryInfos.toString());
		}
		else {
			writer.writeComment("Counterpart selection");
		}
		
		writeProtocolParamDescription();
		
		table = new SavotTable();
		table.setId("OutputFields");
		table.setDescription(data_desc);
		
		writer.writeTableBegin(table);
		writeMetaData((long[])null);
		writeData((long[])null);
		writer.writeTableEnd();
		writer.writeResourceEnd();
		writer.writeDocumentEnd();
	}
	
	/**
	 * @param description
	 * @param resType
	 */
	protected void writeBeginingVOTable() {
		SavotVOTable votable = new SavotVOTable();
		votable.setXmlnsxsi("http://www.w3.org/2001/XMLSchema-instance");
		votable.setXmlns("http://www.ivoa.net/xml/VOTable/v1.1");
		writer.writeDocumentHead(votable);
		
		CoosysSet coosysSet = new CoosysSet();
		SavotCoosys savotCoosys = new SavotCoosys();
		savotCoosys.setId(Database.getAstroframe().toString());
		if( Database.getCoord_equi() != 0 )
			savotCoosys.setEquinox(Double.toString(Database.getCoord_equi()));
		savotCoosys.setSystem(Database.getCoord_sys());
		coosysSet.addItem(savotCoosys);
		writer.writeCoosys(coosysSet);
		SavotResource resource = new SavotResource();
		if (this.hasExtensions == true && this.vores_type != null && this.vores_type.equals("") == false)
			resource.setUtype(this.vores_type);
		writer.writeResourceBegin(resource);
		writer.writeDescription(this.votable_desc);
	}
	
	

	/* (non-Javadoc)
	 * @see saadadb.vo.VOResultFormator#writeData(long[], int)
	 */
	protected void writeData(long[] oids) throws Exception {
		writer.writeDataBegin();
		writer.writeTableDataBegin();
		if( oids != null  ) {
			for (int i = 0; i < oids.length; ++i) {
				if( i >= this.limit ) {
					break;
				}
				SaadaInstance si = Database.getCache().getObject(oids[i]);
				SavotTR savotTR = new SavotTR();					
				tdSet = new TDSet();
				this.writeDMData(si);
				this.writeHouskeepingData(si);
				//this.writeMappedUtypeData(oid);
				//this.writeAttExtendData(oid);
				this.writeExtReferences(si);
				savotTR.setTDs(tdSet);
				writer.writeTR(savotTR);
				if( this.limit > 0 && i >= this.limit ) {
					Messenger.printMsg(Messenger.TRACE, "result truncated to i");
					break;
				}
			}
		}
		writer.writeTableDataEnd();
		writer.writeDataEnd();
	}
	
	/* (non-Javadoc)
	 * @see saadadb.vo.VOResultFormator#writeData(saadadb.query.SQLLikeResultSet)
	 */
	protected void writeData(SaadaQLResultSet rs) throws Exception {
		SaadaInstance si;
		if( this.queryInfos.getQueryTarget() == QueryInfos.ONE_COLL_ONE_CLASS) {
			si = (SaadaInstance)  SaadaClassReloader.forGeneratedName(this.queryInfos.getClassName()).newInstance();
		}
		else {
			si = (SaadaInstance) Class.forName("generated." + Database.getName() + "." + Category.explain(this.queryInfos.getCategory()) + "UserColl").newInstance();			
		}
		writer.writeDataBegin();
		writer.writeTableDataBegin();
		if( rs != null  ) {
			int i=0 ; 
			while(rs.next()) {
				if( i >= this.limit ) {
					break;
				}
				SavotTR savotTR = new SavotTR();					
				tdSet = new TDSet(); 
				if( !isInMappedDmMode () ) {
					si.init(rs);
					this.writeDMData(si);
				}
				else {
					long oid = rs.getOid();
					si.setOid(oid);
					si.setNameSaada(Database.getCache().getObject(oid).getNameSaada());
					this.writeDMData(oid, rs);
				}
				this.writeHouskeepingData(si);
				//this.writeMappedUtypeData(oid);
				//this.writeAttExtendData(oid);
				this.writeExtReferences(si);
				savotTR.setTDs(tdSet);
				writer.writeTR(savotTR);
				if( this.limit > 0 && i >= this.limit ) {
					Messenger.printMsg(Messenger.TRACE, "result truncated to i");
					break;
				}
				i++;
			}
		}
		writer.writeTableDataEnd();
		writer.writeDataEnd();
	}

	/**
	 * Write into the VOTable, native DM field values of obj
	 * @param obj
	 * @throws Exception
	 */
	protected void writeNativeValues(SaadaInstance obj) throws Exception {
		for( Object f: fieldSet_dm.getItems()) {
			SavotField sf = (SavotField)f;
			String name  = sf.getName();
			if( name != null && name.length() > 0 ){
				String val = obj.getFieldString(name);
				if( val == null ) {
					val = "";
				}
				if( sf.getDataType().equals("char") && (sf.getArraySize().length() > 0 && !sf.getArraySize().equals("1")) ){
					if( name.equals("product_url_csa")) { 
						addCDataTD(obj.getDownloadURL(true));							
					}
					else {	
						addCDataTD(val.toString());
					}
				}
				else {
					addTD(val.toString());						
				}
			}
		}
	}

	
	abstract protected void writeDMData(SaadaInstance obj) throws Exception;
	
	/* (non-Javadoc)
	 * @see saadadb.vo.VOResultFormator#writeDMData(long, saadadb.query.SQLLikeResultSet)
	 */
	protected void writeDMData(long oid, SaadaQLResultSet rs) throws Exception {
		SaadaInstance si = Database.getCache().getObject(oid);
		si.activateDataModel(vo_resource.getName());
		for( Object f: fieldSet_dm.getItems()) {
			SavotField sf = (SavotField)f;
			String id = sf.getId();
			if( id.length() == 0 ) {
				id = sf.getName();
			}
			String ucd = sf.getUcd();
			String utype = sf.getUtype();
			Object val=null;;
			boolean cdata = false;
			/*
			 * This case is due to the demo IVOA 05/2009 where we have to add SIAP parameter into the DM in order to run Aladin.
			 * In the future, the DM will reach collection attribute. The SIAP field will then be processed as any other fields.
			 * That can not be done yet because collection fields can not be used by the WhereDM clause of the query engine (joins not supported)
			 */
			if( si instanceof ImageSaada) {
				ImageSaada img = (ImageSaada)si;
				if( ucd.equals("Target.Pos")) {
					val = img.getPos_ra_csa() + " " + img.getPos_dec_csa();
				}
				else if( utype.equals("Char.SpatialAxis.Coverage.Location.Value")) {
					val = img.getPos_ra_csa() + " " + img.getPos_dec_csa();
				}
				else if( utype.equals("Access.Format") || ucd.equalsIgnoreCase("VOX:Image_Format") ) {
					cdata = true;
					val = img.getMimeType();
				}
				else if( utype.equals("DataID.Title") || ucd.equalsIgnoreCase("VOX:Image_Title") ) {
					cdata = true;
					val = img.getNameSaada();
				}
				else if( id.equals("DataLink") || utype.equals("Access.Reference") || ucd.equalsIgnoreCase("VOX:Image_AccessReference")) {
					cdata = true;
					val = img.getDownloadURL(true);
				}
				else if( ucd.equalsIgnoreCase("POS_EQ_RA_MAIN") ){
					val = Double.toString(img.getPos_ra_csa());
				}
				else if( ucd.equalsIgnoreCase("POS_EQ_DEC_MAIN") ){
					val = Double.toString(img.getPos_dec_csa());
				}
				else if( ucd.equalsIgnoreCase("VOX:Image_Naxes") ){
					val = "2";
				}
				else if( ucd.equalsIgnoreCase("VOX:Image_Naxis") ){
					val = img.getNaxis1() + " " + img.getNaxis2();
				}
				else if( ucd.equalsIgnoreCase("VOX:Image_Scale") ){
					val = (img.getSize_alpha_csa() / img.getNaxis1()) + " "
							+ (img.getSize_delta_csa() / img.getNaxis2());
				}
			}
			if( val == null ) {
				val = si.getFieldValue(sf.getName(), rs);
			}
			if( val == null ) {
				val = "";
			}
			
			if( sf.getDataType().equals("char") || cdata) {
				addCDataTD(val.toString());
			}
			else {
				addTD(val.toString());						
			}
		}

	}		



	
	/**
	 * 
	 */
	protected void writeHousekeepingFieldAndGroup() {
		fieldSet_housekeeping = new FieldSet();		
		/*
		 * Param used by Aladin to order the response tree
		 *
		 * <PARAM name="SORTORDER" datatype="char" arraysize="*" value="TonNomDeChampPourLesSurvey SpectralAxis.coverage.Location" />
		 */
		ParamSet paramSet = new ParamSet();
		SavotParam param = new SavotParam();
		param.setName("SORTORDER");
		param.setDataType("char");
		param.setArraySize("*");
		/*
		 * Param specifying order criteria for Aladin
		 * take the utype SpectralAxis.coverage.location.coord.ScalarCoordinate.Value
		 * as second criterium.
		 */
		param.setValue("SaadaOID SpatialAxis_covloc SpectralAxiscovloccoorScalVal");
		paramSet.addItem(param);
		writer.writeParam(paramSet);
		writeSaadaLinksMetaReferences("SaadaOID", fieldSet_housekeeping);
		writer.writeField(fieldSet_housekeeping);
	}
	
	/**
	 * @param oid
	 * @throws SaadaException
	 */
	protected void writeHouskeepingData(SaadaInstance obj) throws SaadaException {
		addTD(Long.toString(obj.getOid()));
		addCDataTD(obj.getNameSaada().replaceAll("#", ""));
		
	}
	
	/**
	 * Create fields used by Aladin to put an URL within the data pane.
	 * @param writer
	 * @param category
	 */
	protected void writeSaadaLinksMetaReferences(String key_field, FieldSet fieldset) {
		SavotField field = new SavotField();
//		field.setId("LinktoPixels");
//		field.setDataType("char");
//		field.setArraySize("*");
//		field.setUcd("meta.ref.url");
//		fieldset.addItem(field);
		
		field = new SavotField(); 
		field.setId(key_field);
		field.setName(key_field);
		field.setRef(key_field);
		field.setType("hidden");
		field.setDataType("char"); field.setArraySize("*");
		field.setUtype("utype.not.set");
		fieldset.addItem(field);
		
		SavotLink link = new SavotLink();
		LinkSet links = new LinkSet();
		link.setHref(Database.getUrl_root() + "/getinstance?oid=${" + key_field + "}");
		links.addItem(link);
		field = new SavotField();
		field.setName("Saada Anchor");
		field.setUtype("utype.not.set");
		field.setDataType("char");
		field.setArraySize("*");
		field.setLinks(links);
		fieldset.addItem(field);
	}
	
	/**
	 * @param length
	 */
	protected void writeDMFieldsAndGroups(int length) {
		
		GroupSet groupSet = new GroupSet();		
		fieldSet_dm = new FieldSet();
		/*
		 * Create one field for each DM attribute
		 */
		String[] groups = vo_resource.groupNames();
		int cpt = 100;
		for( String group_name: groups ) {
			UTypeHandler[] uths = vo_resource.getGroupUtypeHandlers(group_name);
			for( UTypeHandler uth: uths) {
				if( uth.getValue().length() == 0 || "null".equalsIgnoreCase(uth.getValue())) {
					fieldSet_dm.addItem(uth.getSavotField(cpt++));
				}
			}
		}
		writer.writeField(fieldSet_dm);
		/*
		 * Arrange fields and DM params in groups 
		 */
		for( String group_name: groups ) {
			SavotField sf;
			SavotGroup group  = new SavotGroup();
			ParamSet paramSet = new ParamSet();
			FieldRefSet fieldRefSet = new FieldRefSet();
			group.setName(group_name);
			paramSet = new ParamSet();
			UTypeHandler[] uths = vo_resource.getGroupUtypeHandlers(group_name);
			for( UTypeHandler uth: uths) {
				if(uth.getValue().length() == 0 || "null".equalsIgnoreCase(uth.getValue())) {
					for( Object f: fieldSet_dm.getItems()) {
						sf = (SavotField)f;
						if( ("".equals(uth.getUtype())    ||  sf.getUtype().equals(uth.getUtype())) &&
								("".equals(uth.getUcd())      || sf.getUcd().equals(uth.getUcd()))	    &&
								("".equals(uth.getNickname()) || sf.getName().equals(uth.getNickname()))) {
							SavotFieldRef fieldRef = new SavotFieldRef();
							fieldRef.setRef(sf.getRef());
							fieldRefSet.addItem(fieldRef);
						}
					}
				}
				else if( uth.getUtype().equals("Dataset.Length") ) {
					paramSet.addItem(uth.getSavotParam(Integer.toString(length), ""));						
				}
				else if( uth.getUtype().equals("Dataset.Size") /* not a param: defined par file ||  uth.getUtype().equals("Access.Size") */) {
					paramSet.addItem(uth.getSavotParam(Integer.toString(length), ""));						
				}
				else if( uth.getUtype().equals("CoordSys.SpaceFrame.Name") ) {
					paramSet.addItem(uth.getSavotParam(Database.getCoord_sys(), ""));						
				}
				else if( uth.getUtype().equals("CoordSys.SpaceFrame.Equinox") ) {
					paramSet.addItem(uth.getSavotParam(Database.getCoord_equi(), ""));						
				}
				else {
					paramSet.addItem(uth.getSavotParam(uth.getValue(), ""));
				}
			}
			if( paramSet.getItemCount() > 0 ) {
				group.setParams(paramSet);				
			}
			if( fieldRefSet.getItemCount() > 0 ) {
				group.setFieldsRef(fieldRefSet);				
			}
			groupSet.addItem(group);
		}
		writer.writeGroup(groupSet);
	}
	
	/**
	 * Write fields used to reference associated tables
	 * @param writer
	 * @param category
	 * @throws QueryException 
	 */
	protected void writeExtMetaReferences(int category) throws QueryException {
		if (this.hasExtensions == true) {
			try {
				String relations[] = this.getRelationNames();
				for (int j = 0; j < relations.length; j++) {
					String ref_id = relations[j].toString() + "_OID";
					/*
					 * Field: Unique ID identifying the association instance
					 */
					FieldSet fieldSet = new FieldSet();
					SavotField field = new SavotField();
					field.setRef(relations[j].toString());
					field.setId(ref_id);
					/*
					 * Association.ID is the utype recommened by SSA as association key
					 * Bu use Observation/Identifier while Aladin requires it
					 * field.setUtype("Association.ID");
					 */					
					field.setUtype("Observation/Identifier");
					field.setType("hidden");
					field.setRef(relations[j]);
					fieldSet.addItem(field);
					writer.writeField(fieldSet);
					GroupSet gs = new GroupSet();
					SavotGroup sg = new SavotGroup();
					sg.setDescription("Association through the Saada relationship: " + relations[j]);
					/*
					 * ref to the fieldField: Unique ID identifying the association instance
					 */
					FieldRefSet frs = new FieldRefSet() ;
					SavotFieldRef fieldRef = new SavotFieldRef();
					fieldRef.setRef(ref_id);
					frs.addItem(fieldRef);
					sg.setFieldsRef(frs);
					/*
					 * Param : Type of association
					 */
					ParamSet ps = new ParamSet();
					SavotParam sp = new SavotParam();
					sp.setUtype("Association.Type");
					sp.setValue(Category.explain(Database.getCachemeta().getRelation(relations[j]).getSecondary_category()));
					ps.addItem(sp);
					sg.setParams(ps);
					/*
					 * Param Unique key different for each element of association
					 */
					sp = new SavotParam();
					sp.setUtype("Association.Key");
					sp.setValue(ref_id);
					ps.addItem(sp);
					sg.setParams(ps);
					gs.addItem(sg);
					
					writer.writeGroup(gs);
				}
			} catch (Exception e) {
				Messenger.printStackTrace(e);
				QueryException.throwNewException(SaadaException.METADATA_ERROR, "Getting relations for "
						+ this.queryInfos.getInputSaadaTable() + " ) " + e.getMessage());
			}
		}
	}
	/**
	 * Append to the main tbale the field value used as associations
	 * @param oid
	 * @param category
	 */
	protected void writeExtReferences(SaadaInstance si) {
		if (this.hasExtensions == true) {
			String relations[] = this.getRelationNames();
			for (int j = 0; j < relations.length; j++) {
				addTD(Long.toString(si.getOid()));
			}
		}
	}
	
	/**
	 * Appends to the VOTable all extensions matching Saada relationships
	 * @param oids
	 * @param category
	 * @throws SaadaException
	 */
	protected void writeExtensions(long[] oids, int category)
	throws SaadaException {
		if (this.hasExtensions == true) {
			try {
				SavotResource res = new SavotResource();
				res.setName("Extensions");
				res.setType("");
				writer.writeResourceBegin(res);
				String[] relations = getRelationNames();
				for (int i = 0; i < relations.length; i++) {
					SavotTable ext_table = new SavotTable();
					ext_table.setName(relations[i]);
					ext_table.setId(relations[i]);
					ext_table.setDescription("Saada counterparts of "
							+ this.queryInfos.getInputSaadaTable()
							+ " in relation " + relations[i]);
					writer.writeTableBegin(ext_table);
					this.writeExtMetaData(relations[i]);
					this.writeExtData(relations[i], oids);
					writer.writeTableEnd();
				}
				writer.writeResourceEnd();
			} catch (Exception e) {
				Messenger.printStackTrace(e);
				QueryException.throwNewException(SaadaException.METADATA_ERROR, "Getting relation list for "
						+ this.queryInfos.getInputSaadaTable() + ": " + e.getMessage());
			}
		}
	}
	

	/**
	 * @param rs
	 * @param category
	 * @throws SaadaException
	 */
	protected void writeExtensions(SaadaQLResultSet rs, int category)
	throws SaadaException {
		if (this.hasExtensions == true) {
			try {
				SavotResource res = new SavotResource();
				res.setName("Extensions");
				res.setType("");
				writer.writeResourceBegin(res);
				String[] relations = getRelationNames();
				for (int i = 0; i < relations.length; i++) {
					SavotTable ext_table = new SavotTable();
					ext_table.setName(relations[i]);
					ext_table.setId(relations[i]);
					ext_table.setDescription("Saada counterparts of "
							+ this.queryInfos.getInputSaadaTable()
							+ " in relation " + relations[i]);
					writer.writeTableBegin(ext_table);
					this.writeExtMetaData(relations[i]);
					this.writeExtData(relations[i], rs);
					writer.writeTableEnd();
				}
				writer.writeResourceEnd();
			} catch (Exception e) {
				Messenger.printStackTrace(e);
				QueryException.throwNewException(SaadaException.METADATA_ERROR, "Getting relation list for "
						+ this.queryInfos.getInputSaadaTable() + ": " + e.getMessage());
			}
		}
	}
	/**
	 * Write field for the extension table matching the Saada relation
	 * @param relation
	 * @throws SaadaException 
	 */
	protected void writeExtMetaData(String relation) throws SaadaException {
		MetaRelation mr = Database.getCachemeta().getRelation(relation);
		if ( mr  == null) {
			Messenger.printMsg(Messenger.ERROR,
					"Can't found meta data for relation " + relation);
		} else {
			FieldSet fieldSet = new FieldSet();
			SavotField field;
			
			field = new SavotField();
			field.setId("Access_For");
			field.setUtype("Access.Format");
			field.setDataType("char");
			field.setArraySize("*");
			field.setType("hidden");
			fieldSet.addItem(field);
			
			field = new SavotField();
			field.setId("Access_Ref");
			field.setUtype("Access.Reference");
			field.setDataType("char");
			field.setArraySize("*");
			field.setType("hidden");
			fieldSet.addItem(field);
			
			field = new SavotField();
			field.setId("namesaada");
			field.setDataType("char");
			field.setArraySize("*");
			field.setUcd("meta.title");
			fieldSet.addItem(field);
			
			/*
			 * Entry are not listed individulally in the table but there is
			 * just one CS reference for all sources
			 */
			if( mr.getSecondary_category() != Category.ENTRY) {
				writeSaadaLinksMetaReferences(relation + "_OID", fieldSet);
				if( mr.getSecondary_category() == Category.SPECTRUM || mr.getSecondary_category() == Category.IMAGE) {
					
					field = new SavotField();
					field.setName("Char_SpaCovLocVal");
					field.setUtype("Char.SpatialAxis.Coverage.Location.Value");
					field.setDataType("char");
					field.setArraySize("*");
					fieldSet.addItem(field);				
				}
				String[] qn = mr.getQualifier_names().toArray(new String[0]);
				for (int i = 0; i < qn.length; i++) {
					field = new SavotField();
					field.setId(qn[i]);
					field.setName("relation_qua_table_"
							+ qn[i]);
					field.setDataType("double");
					fieldSet.addItem(field);
				}
			}
			else {
				field = new SavotField(); 
				field.setId(relation + "_OID");
				field.setRef(relation + "_OID");
				field.setType("hidden");
				field.setDataType("char"); field.setArraySize("*");
				fieldSet.addItem(field);				
			}
			writer.writeField(fieldSet);
			if( mr.getSecondary_category() != Category.ENTRY) {
				writer.writeGroup(this.writeSaadaReferencesGroup(mr));
			}
		}
	}
	
	/**
	 * Returns a SavotGroup with all Savot params referecing the relation
	 * @param mr
	 * @return
	 * @throws SaadaException
	 */
	private GroupSet writeSaadaReferencesGroup(MetaRelation relation) throws SaadaException {
		GroupSet gs = new GroupSet();
		ParamSet ps = new ParamSet();
		SavotGroup sg = new SavotGroup();
		gs.addItem(sg);
		sg.setName("Saada References");
		sg.setParams(ps);
		SavotParam sp = new SavotParam();
		sp.setName("Saada Collection");
		sp.setValue(relation.getSecondary_coll());
		sp.setDescription("Saada collection where data can be retreived");
		ps.addItem(sp);
		
		sp = new SavotParam();
		sp.setName("Saada Category");
		sp.setValue(Category.explain(relation.getSecondary_category()));
		sp.setDescription("Saada product category where data can be retreived");
		ps.addItem(sp);
		
		sp = new SavotParam();
		sp.setName("Saada Relationship");
		sp.setValue(relation.getName());
		sp.setDescription("Saada relationship pointing to these data");
		ps.addItem(sp);
		
		return gs;
	}
	

	/**
	 * Write table data for the extension matching the relation
	 * @param relation
	 * @param oids
	 * @throws Exception
	 */
	protected void writeExtData(String relation, long[] oids)
	throws Exception {
		MetaRelation mr = Database.getCachemeta().getRelation(relation);
		String[] qn = mr.getQualifier_names().toArray(new String[0]);
		if ( mr == null) {
			Messenger.printMsg(Messenger.ERROR,
					"Can't found meta data for relation " + relation);
		} else {
			writer.writeDataBegin();
			writer.writeTableDataBegin();
			for (int i = 0; i < oids.length; i++) {
				long oid = ((Long) oids[i]).longValue();
				SaadaInstance obj = (SaadaInstance) Database.getCache().getObject(oid);
				SaadaLink[] links = obj.getStartingLinks(relation);
				if (links.length == 0) {
					continue;
				}
				int sec_cat = mr.getSecondary_category();
				
				for (int k = 0; k < links.length; k++) {
					SaadaInstance counterpart = Database.getCache().getObject(links[k].getEndindOID());
					SavotTR savotTR = new SavotTR();
					tdSet = new TDSet();
					// Access.format
					if(  sec_cat == Category.ENTRY ) {
						addCDataTD("text/xml");							
					}
					else {
						addCDataTD(counterpart.getMimeType());	
					}
					// Access.reference
					if(  sec_cat == Category.ENTRY ) {
						addCDataTD(Database.getUrl_root() + "/conesearch?relation="
								 + relation + "&primoid=" + oids[i] );
					}
					else {
						addCDataTD(counterpart.getDownloadURL(true));
					}
					/*
					 * Entry are not listed individulally in the table but there is
					 * just one CS reference for all sources
					 */
					if( sec_cat == Category.ENTRY ) {
						// Saada name (Savot doesn't support # in dataset, even in a CDATA)
						addCDataTD("Source Counterparts");
						savotTR.setTDs(tdSet);
						addTD(Long.toString(oid));
						writer.writeTR(savotTR);
						break;
					}
					// Saada name (Savot doesn't support # in dataset, even in a CDATA)
					addCDataTD(counterpart.getNameSaada().replace("#", "_").trim());
					// Links to pixels
					addCDataTD(obj.getURL(true) );
					// OID used as key for the Aladin link
					addTD(Long.toString(oid));
					// Links for Aladin
					addTD("SaadaDB Anchor");
					// qualifiers
					if( mr.getSecondary_category() == Category.ENTRY || mr.getSecondary_category() == Category.SPECTRUM || mr.getSecondary_category() == Category.IMAGE) {
						addTD(((Position)obj).getPos_ra_csa() + " " + ((Position)obj).getPos_dec_csa());						
					}
					for (int j = 0; j < qn.length; j++) {
						addTD(Double.toString(links[k].getQualifierValue(qn[j])));
					}
					savotTR.setTDs(tdSet);
					writer.writeTR(savotTR);
				}
			}
			writer.writeTableDataEnd();
			writer.writeDataEnd();
		}
		
	}
	
	/**
	 * @param relation
	 * @param rs
	 * @throws Exception
	 */
	protected void writeExtData(String relation, SaadaQLResultSet rs)
	throws Exception {
		MetaRelation mr = Database.getCachemeta().getRelation(relation);
		String[] qn = mr.getQualifier_names().toArray(new String[0]);
		if ( mr == null) {
			Messenger.printMsg(Messenger.ERROR,
					"Can't found meta data for relation " + relation);
		} else {
			writer.writeDataBegin();
			writer.writeTableDataBegin();
			while( rs.next()) {
				long oid =rs.getOid();
				SaadaInstance obj = (SaadaInstance) Database.getCache().getObject(oid);
				SaadaLink[] links = obj.getStartingLinks(relation);
				if (links.length == 0) {
					continue;
				}
				int sec_cat = mr.getSecondary_category();
				
				for (int k = 0; k < links.length; k++) {
					SaadaInstance counterpart = Database.getCache().getObject(links[k].getEndindOID());
					SavotTR savotTR = new SavotTR();
					tdSet = new TDSet();
					// Access.format
					if(  sec_cat == Category.ENTRY ) {
						addCDataTD("text/xml");							
					}
					else {
						addCDataTD(counterpart.getMimeType());	
					}
					// Access.reference
					if(  sec_cat == Category.ENTRY ) {
						addCDataTD(Database.getUrl_root() + "/conesearch?relation="
								 + relation + "&primoid=" + oid );
					}
					else {
						addCDataTD(counterpart.getDownloadURL(true));
					}
					/*
					 * Entry are not listed individulally in the table but there is
					 * just one CS reference for all sources
					 */
					if( sec_cat == Category.ENTRY ) {
						// Saada name (Savot doesn't support # in dataset, even in a CDATA)
						addCDataTD("Source Counterparts");
						savotTR.setTDs(tdSet);
						addTD(Long.toString(oid));
						writer.writeTR(savotTR);
						break;
					}
					// Saada name (Savot doesn't support # in dataset, even in a CDATA)
					addCDataTD(counterpart.getNameSaada().replace("#", "_").trim());
					// Links to pixels
					addCDataTD(obj.getURL(true) );
					// OID used as key for the Aladin link
					addTD(Long.toString(oid));
					// Links for Aladin
					addTD("SaadaDB Anchor");
					// qualifiers
					if( mr.getSecondary_category() == Category.ENTRY || mr.getSecondary_category() == Category.SPECTRUM || mr.getSecondary_category() == Category.IMAGE) {
						addTD(((Position)obj).getPos_ra_csa() + " " + ((Position)obj).getPos_dec_csa());						
					}
					for (int j = 0; j < qn.length; j++) {
						addTD(Double.toString(links[k].getQualifierValue(qn[j])));
					}
					savotTR.setTDs(tdSet);
					writer.writeTR(savotTR);
				}
			}
			writer.writeTableDataEnd();
			writer.writeDataEnd();
		}
		
	}
	/**
	 * @param content
	 */
	protected void addTD(String content) {
		SavotTD td = new SavotTD();
		if( content != null )
			td.setContent(content);
		else {
			td.setContent("");
		}
		tdSet.addItem(td);
	}
	
	/**
	 * @param content
	 */
	protected void addCDataTD(String content) {
		SavotTD td = new SavotTD();
		/*
		 * content prepended with a ' ' to help Aladin parser to get not lost
		 */
		td.setContent("<![CDATA[" + content + "]]>");
		tdSet.addItem(td);
	}
	
	/**
	 * @param val
	 */
	protected void addValTD(Object val) {	
		if( val != null ) {
			String sval = val.toString();
			if( !(sval.equals("Infinity") || sval.equals("NaN") || sval.equals("2147483647")) ) {	
				addCDataTD(sval.replaceAll("#", ""));
			}
			else {
				addTD("");
			}
		}
		else {
			addTD("");
		}
	}
	

	/**
	 * @param file
	 */
	protected void initWriter(File file) {
		writer = new SavotWriter();
		writer.enableAttributeEntities(false);
		writer.enableElementEntities(false);
		writer.initStream(file.getPath());
	}

	/**
	 * @param msg
	 */
	protected void writeProtocolError(String msg) {
		InfoSet infoSet = new InfoSet();
		SavotInfo info = new SavotInfo();
		info.setContent("<![CDATA[" + msg + "]]>");
		info.setValue("ERROR");
		info.setName("QUERY_STATUS");
		infoSet.addItem(info);
		writer.writeInfo(infoSet);
		writer.writeResourceEnd();
		writer.writeDocumentEnd();
	}
	
	/**
	 * courtesy of http://stackoverflow.com/questions/439298/best-way-to-encode-text-data-for-xml-in-java
	 * @param msg
	 * @return
	 */
	public static String protectSpecialCharacters(String msg) {
	    if (msg == null) {
	        return null;
	    }
	    boolean anyCharactersProtected = false;

	    StringBuffer stringBuffer = new StringBuffer();
	    for (int i = 0; i < msg.length(); i++) {
	        char ch = msg.charAt(i);

	        boolean controlCharacter = ch < 32;
	        boolean unicodeButNotAscii = ch > 126;
	        boolean characterWithSpecialMeaningInXML = ch == '"' || ch == '<' || ch == '&' || ch == '>';

	        if (characterWithSpecialMeaningInXML || unicodeButNotAscii || controlCharacter) {
	            stringBuffer.append("&#" + (int) ch + ";");
	            anyCharactersProtected = true;
	        } else {
	            stringBuffer.append(ch);
	        }
	    }
	    if (anyCharactersProtected == false) {
	        return msg;
	    }

	    return stringBuffer.toString();
	}


}