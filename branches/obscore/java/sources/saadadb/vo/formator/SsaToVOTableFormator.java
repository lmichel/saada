package saadadb.vo.formator;

import java.io.File;
import java.util.Map;

import saadadb.collection.SaadaInstance;
import saadadb.collection.SpectrumSaada;
import saadadb.database.Database;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.products.inference.SpectralCoordinate;
import cds.savot.model.ParamSet;
import cds.savot.model.SavotField;
import cds.savot.model.SavotParam;

/**@version $Id$
 * Get the result of a SSA query on Saada as a list of OIDs, and produces a
 * VOTable XML file containing a list of references to the spectra found.
 */
public class SsaToVOTableFormator extends VOTableFormator {
	
//http://xcatdb/xidresult/ssa?&pos=sdfg&size=0.7&band=1/2[m]&collection=[SpectroscopicSample]

	/**
	 * Constructor.
	 * @throws SaadaException 
	 */
	public SsaToVOTableFormator(String voresource_name) throws SaadaException {
		super(voresource_name, "SSA default", "Saada SSA service", "dal:SimpleQueryResponse", "SSA search result on SaadaDB " + Database.getDbname());
		version = "1.03";
		}
	
	public SsaToVOTableFormator(String voresource_name, Map<String, String[]> cgi_params) throws SaadaException {
		super(voresource_name, "SSA default", "Saada SSA service", "dal:SimpleQueryResponse", "SSA search result on SaadaDB " + Database.getDbname());
		this.setCGIParams(cgi_params);
		version = "1.03";
	}

	/**
	 * @param voresource_name
	 * @param result_filename
	 * @throws SaadaException
	 */
	public SsaToVOTableFormator(String voresource_name, String result_filename) throws SaadaException {
		super(voresource_name, "SSA default", "Saada SSA service", "dal:SimpleQueryResponse", "SSA search result on SaadaDB " + Database.getDbname(), result_filename);
		version = "1.03";
	}


	/* (non-Javadoc)
	 * @see saadadb.vo.VOResultFormator#writeProtocolParamDescription()
	 */
	protected void writeProtocolParamDescription() {
		ParamSet paramSet = new ParamSet();
		SavotParam param = new SavotParam();
		param.setDataType("char");
		param.setArraySize("*");
		param.setName("INPUT:POS");
		param.setDescription("The center of the region of interest." 
				+ "The coordinate values are specified in list format (comma separated) in decimal degrees "
				+ "with no embedded white space followed by an optional coord. systems such as GALACTIC;"
				+ "default is ICRS.");
		param.setValue(this.getCGIParam("pos"));
		paramSet.addItem(param);
		

		param = new SavotParam();
		param.setName("INPUT:SIZE");
		param.setDataType("double");
		param.setUnit("deg");
		param.setDescription("The diameter of the circular region of interest in decimal degrees."
				+ "A special case is SIZE=0. It will cause a search in the service defined "
				+ "default sized region of 0.1 degrees resulting in a patch of 0.01*pi sq.deg.");
		param.setValue(this.getCGIParam("size"));
		paramSet.addItem(param);

		param = new SavotParam();
		param.setName("INPUT:BAND");
		param.setDataType("char");
		param.setArraySize("*");
		param.setDescription("");
		param.setValue(this.getCGIParam("band"));
		paramSet.addItem(param);
//
//		svs = new SavotValues();
//		OptionSet os = new OptionSet();
//		SavotOption option = new SavotOption(); option.setValue("eV");os.addItem(option);
//		option = new SavotOption(); option.setValue("KeV");os.addItem(option);
//		option = new SavotOption(); option.setValue("MeV");os.addItem(option);
//		option = new SavotOption(); option.setValue("GeV");os.addItem(option);
//		option = new SavotOption(); option.setValue("TeV");os.addItem(option);
//		option = new SavotOption(); option.setValue("Angstroem");os.addItem(option);
//		option = new SavotOption(); option.setValue("nm");os.addItem(option);
//		option = new SavotOption(); option.setValue("um");os.addItem(option);
//		option = new SavotOption(); option.setValue("mm");os.addItem(option);
//		option = new SavotOption(); option.setValue("cm");os.addItem(option);
//		option = new SavotOption(); option.setValue("m");os.addItem(option);
//		option = new SavotOption(); option.setValue("km");os.addItem(option);
//		option = new SavotOption(); option.setValue("Hz");os.addItem(option);
//		option = new SavotOption(); option.setValue("KHz");os.addItem(option);
//		option = new SavotOption(); option.setValue("MHz");os.addItem(option);
//		option = new SavotOption(); option.setValue("GHz");os.addItem(option);
//		svs.setOptions(os);
//		param.setValues(svs);


		param = new SavotParam();
		param.setName("INPUT:FORMAT");
		param.setValue("COMPLIANT");
		param.setDataType("char");
		param.setArraySize("*");
		param.setDescription("Requested format of spectra");
		paramSet.addItem(param);


		param = new SavotParam();
		param.setName("INPUT:collection");
		param.setDataType("char");
		param.setArraySize("*");
		param.setDescription("Gives the collections and possibly the classes in which we want to search. The format is as follows : collection=coll1,coll2 OR collection=coll1[class1, class2]");
		param.setValue(this.getCGIParam("collection"));
		paramSet.addItem(param);

		//aaaaa
//		String[] groups = vo_resource.groupNames();
//		for( String group_name: groups ) {
//			UTypeHandler[] uths = vo_resource.getGroupUtypeHandlers(group_name);
//			for( UTypeHandler uth: uths) {		
//				System.out.println("@@@@ " + uth.getNickname() + " " + uth.getValue());
//				paramSet.addItem(uth.getSavotParam(uth.getValue(), "OUTPUT:"));			
//			}
//		}
		
		

//		param = new SavotParam();
//		param.setId("LinktoPixels");
//		param.setName("OUTPUT:LinktoPixels");
//		param.setDataType("char");
//		param.setArraySize("*");
//		param.setUcd("meta.ref.url");
//		paramSet.addItem(param);

		writer.writeParam(paramSet);		
	}

	/* (non-Javadoc)
	 * @see saadadb.vo.VOTableFormator#writeDMData(long)
	 */
	protected void writeDMData(SaadaInstance si) throws Exception {
		SpectrumSaada obj = (SpectrumSaada)( si) ;
		/*
		 * In native mode we just take attribute values (see superclass)
		 */
		if( this.vo_resource.isNative_mode() ) {
			writeNativeValues(obj);
		}
		/*
		 * If using a DM, only UTYPE and then UCDS ar considered
		 */
		else {
			for( Object f: fieldSet_dm.getItems()) {
				SavotField sf = (SavotField)f;
				String utype = sf.getUtype();
				String id = sf.getId();
				String val;
				boolean cdata = false;
				//			System.out.println("Utype " + utype );
				if( utype.equals("Target.Pos")) {
					val = obj.getPos_ra_csa() + " " + obj.getPos_dec_csa();
				}
				else if( utype.equals("ssa:Char.SpatialAxis.Coverage.Location.Value")) {
					val = obj.getPos_ra_csa() + " " + obj.getPos_dec_csa();
					/*
					 * Just to be overridden for XMM
					 */
					val = "";
				}
				else if( utype.equals("ssa:Char.SpectralAxis.Coverage.Location.Value")) {
					try {
						/*
						 * Conversion must be first done because it could be non linear (e.g. Kev -> m)
						 * Hence converting the mean is no equivalent to the mean of converted values
						 */
						double v1 = SpectralCoordinate.convertSaada(Database.getSpect_unit(), "m", obj.getX_min_csa());
						double v2 = SpectralCoordinate.convertSaada(Database.getSpect_unit(), "m", obj.getX_max_csa());
						val = (Double.toString((v1  + v2)/2));
					} catch(Exception e) {
						e.printStackTrace();
						val = "";
					}
				}
				else if( utype.equals("ssa:Char.SpectralAxis.Coverage.Bounds.Extent")) {
					try {
						double v1 = SpectralCoordinate.convertSaada(Database.getSpect_unit(), "m", obj.getX_min_csa());
						double v2 = SpectralCoordinate.convertSaada(Database.getSpect_unit(), "m", obj.getX_max_csa());
						double v = v2 - v1;	
						if( v < 0 ) v *= -1.;
						val = (Double.toString(v));
					} catch(Exception e) {
						e.printStackTrace();
						val = "";
					}
				}
				else if( utype.equals("Access.Size")) {
					val = Long.toString(obj.getFileSize()/1024) ;
				}
				else if( utype.equals("Access.Reference")) {
					val = obj.getDownloadURL(true) ;
					cdata = true;
				}
				else if( utype.equals("Access.Format")) {
					val = obj.getMimeType();
					cdata = true;
				}
				else if( utype.equals("DataID.Title")) {
					val = obj.getNameSaada();
					cdata = true;
				}
				else if( id.equals("LinktoPixels")) {
					val = obj.getURL(true);
					cdata = true;
				}
				else {
					AttributeHandler ah  = obj.getFieldByUtype(sf.getUtype(), false);
					if( ah == null ) {
						val = "";					
					}
					else {
						Object v = obj.getFieldValue(ah.getNameattr());
						val = (v.toString());						
					}
				}
				if( this.vo_resource.getName().equals("SSA EPIC Spectra") && 
					(val.length() == 0 || val.equals("Infinity") ||  val.equals("NaN")) ){
					val = getXMMData(utype, obj);
				}
				if( cdata ) {
					addCDataTD(val);
				}
				else {
					addTD(val);
				}
			}
		}
	}


	/**
	 * @param utype
	 * @param sp
	 * @return
	 * @throws Exception
	 */
	private String getXMMData(String utype, SpectrumSaada sp) throws Exception {
		if( utype.equals("Target.Name")) {
			return sp.getNameSaada();
		}
		else if( utype.equals("Curation.PublisherDID") || utype.equals("DataID.DatasetID") ) {
			return "ivo://xcatdb/2xmmi/epicssa#" + (new File(sp.getProduct_url_csa())).getName();
		}
		else if( utype.equals("CoordSys.SpectralFrame.RefPos") ) {
			return sp.getFieldValue("_tlmin1").toString();
		}
		else if( utype.equals("CoordSys.TimeFrame.Name") ) {
			return sp.getFieldValue("SSA_TIMESYS").toString();
		}
		else if( utype.equals("CoordSys.TimeFrame.RefPos") ) {
			return sp.getFieldValue("SSA_TIMEREF").toString();
		}
		else if( utype.equals("CoordSys.TimeFrame.Zero") ) {
			return sp.getFieldValue("SSA_MJDREF").toString();
		}
		else if( utype.equals("Char.SpatialAxis.Coverage.Location.Value") ) {
			return sp.getFieldValue("SSA_SPAT_LOC").toString();
		}
		else if( utype.equals("Char.SpatialAxis.Coverage.Bounds.Extent") ) {
			double v1 = 2.0*Double.parseDouble(sp.getFieldValue("SSA_SPAT_EXT").toString());
			return "" + v1;
		}
		else if( utype.equals("Char.SpatialAxis.Coverage.Support.Area") ) {
			double v1 = Double.parseDouble(sp.getFieldValue("SSA_SPAT_EXT").toString());
			return "Circle with (center, radius) = ("  + sp.getFieldValue("SSA_SPAT_LOC").toString() + " " + v1 + ")";
		}
		else if( utype.equals("Char.SpatialAxis.SamplingPrecision.SampleExtent") ) {
			return sp.getFieldValue("SSA_SPAT_SAMPEXT").toString();
		}
		else if( utype.equals("Char.SpectralAxis.Coverage.Location.Value") ) {
			double v1 = Double.parseDouble(sp.getFieldValueByUtype("Char.SpectralAxis.Coverage.Bounds.Start", true).toString());
			double v2 = Double.parseDouble(sp.getFieldValueByUtype("Char.SpectralAxis.Coverage.Bounds.Stop", true).toString());
			return Double.toString((v1 + v2)/2);
		}
		else if( utype.equals("Char.SpectralAxis.Coverage.Bounds.Extent") ) {
			double v1 = Double.parseDouble(sp.getFieldValueByUtype("Char.SpectralAxis.Coverage.Bounds.Start", true).toString());
			double v2 = Double.parseDouble(sp.getFieldValueByUtype("Char.SpectralAxis.Coverage.Bounds.Stop", true).toString());
			return Double.toString(v2 - v1 + 1);
		}
		else if( utype.equals("Char.TimeAxis.Unit") ) {
			return sp.getFieldValue("SSA_TIMEUNIT").toString();
		}
		else if( utype.equals("Char.TimeAxis.Coverage.Location.Value") ) {
			double v1 = Double.parseDouble(sp.getFieldValue("SSA_MJDREF").toString());
			double v2 = Double.parseDouble(sp.getFieldValue("SSA_TIMESTART").toString());
			double v3 = Double.parseDouble(sp.getFieldValue("SSA_TIMESTOP").toString());
			return Double.toString(v1 + (0.5*(v2 + v3)/86400));
		}
		else if( utype.equals("Char.TimeAxis.Coverage.Bounds.Extent") ) {
			return sp.getFieldValue("SSA_DURATION").toString();
		}
		else if( utype.equals("Char.TimeAxis.Coverage.Bounds.Start") ) {
			double v1 = Double.parseDouble(sp.getFieldValue("SSA_MJDREF").toString());
			double v2 = Double.parseDouble(sp.getFieldValue("SSA_TIMESTART").toString());
			return Double.toString(v1 + ((v2)/86400));
		}
		else if( utype.equals("Char.TimeAxis.Coverage.Bounds.Stop") ) {
			double v1 = Double.parseDouble(sp.getFieldValue("SSA_MJDREF").toString());
			double v2 = Double.parseDouble(sp.getFieldValue("SSA_TIMESTOP").toString());
			return Double.toString(v1 + ((v2)/86400));
		}
		else if( utype.equals("Char.TimeAxis.Coverage.Support.Extent") ) {
			return sp.getFieldValue("_exposure").toString();
		}
		else if( utype.equals("Char.TimeAxis.SamplingPrecision.SamplingPrecisionRefVal.FillFactor") ) {
			double v = Double.parseDouble(sp.getFieldValue("_exposure").toString());
			return Double.toString(v/Double.parseDouble(sp.getFieldValue("SSA_DURATION").toString()));
		}
		else if( utype.equals("Background.File") ) {
			return sp.getFieldValue("_backfile").toString();
		}
		else if( utype.equals("Response.File") ) {
			return sp.getFieldValue("_respfile").toString();
		}
		else if( utype.equals("Ancillary.Response.File") ) {
			return sp.getFieldValue("_ancrfile").toString();
		}
		else if( utype.equals("Pipeline.Software.Version") ) {
			return sp.getFieldValue("_ppsvers").toString();
		}
		/*
		 * Return a ZIP ball with the calibration files
		 */
		else if( utype.equals("Allfiles.Zipball") ){
			return "<![CDATA[ " + Database.getUrl_root() + "/getproduct?reports=" + sp.getProduct_url_csa().replace(".gz", "").replace(".FIT", ".zip")  + "]]>";
		}
		else {
			return null;
		}
	}


}
