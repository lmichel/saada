package saadadb.vo.formator;

import java.io.File;
import java.util.Date;

import saadadb.collection.ImageSaada;
import saadadb.collection.SaadaInstance;
import saadadb.database.Database;
import saadadb.database.Repository;
import saadadb.dataloader.mapping.ProductMapping;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.util.ImageUtils;
import cds.savot.model.OptionSet;
import cds.savot.model.ParamSet;
import cds.savot.model.SavotField;
import cds.savot.model.SavotOption;
import cds.savot.model.SavotParam;
import cds.savot.model.SavotValues;

/**
 * Get the result of a SIA query on Saada as a list of OIDs, and produces a
 * VOTable XML file containing a list of references to the images found.
 */
public class SiaToVOTableFormator extends VOTableFormator {
	public static final String version = "1.00";


	/**@version $Id: SiaToVOTableFormator.java 118 2012-01-06 14:33:51Z laurent.mistahl $
	 * Constructor.
	 * @throws SaadaException 
	 */
	public SiaToVOTableFormator(String voresource_name) throws SaadaException {
		super(voresource_name, "SIA default", "Saada SIA service", "dal:SimpleQueryResponse", "SIA search result on SaadaDB " + Database.getDbname());
	}

	public SiaToVOTableFormator(String voresource_name, String result_filename) throws SaadaException {
		super(voresource_name, "SIA default", "Saada SIA service", "dal:SimpleQueryResponse", "SIA search result on SaadaDB " + Database.getDbname(), result_filename);
	}


	/* (non-Javadoc)
	 * @see saadadb.vo.VOTableFormator#formatToURL(java.lang.Object[])
	 */
	public String formatToURL(long[] oids) throws SaadaException {
		FatalException.throwNewException(SaadaException.UNSUPPORTED_MODE,
		"Service not available. Use 'formatToString' instead");
		return null;
	}


	protected void writeDMData(SaadaInstance si) throws Exception {
		ImageSaada obj = (ImageSaada)( si) ;
		String download_url;
		String url;
		String targetfile;
		if( this.queryInfos.isCutoutMode() ) {
			ProductMapping cdh = obj.getLoaderConfig();
			targetfile = "tile_" + ((new Date()).getTime()) + ".fits";
			String targetpath = Repository.getVoreportsPath() + Database.getSepar() + targetfile ;
			ImageUtils.buildTileFile(obj.getPos_ra_csa(), obj.getPos_dec_csa(), this.queryInfos.getSizeRA(), this.queryInfos.getSizeDEC(), obj.getRepositoryPath(), cdh, targetpath);
			download_url = Database.getUrl_root() + "/getproduct?report=" + targetfile;
			url = Database.getUrl_root() + "/getproduct?reports=" + targetfile;
		}
		else {
			url = obj.getURL(true);
			download_url = obj.getDownloadURL(true);			
		}
		/*
		 * In native mode we just take attribute values (see superclass)
		 */
		if( this.vo_resource.isNative_mode() ) {
			writeNativeValues(obj);
		}
		/*
		 * If using a DM, only UTYPE and then UCDS are considered
		 */
		else {
			for( Object f: fieldSet_dm.getItems()) {
				String val="";
				boolean cdata;
				SavotField sf = (SavotField)f;
				String ucd = sf.getUcd();
				String utype = sf.getUtype();
				String id = sf.getId();
				cdata = false;
				if( ucd.equals("Target.Pos")) {
					val = obj.getPos_ra_csa() + " " + obj.getPos_dec_csa();
				}
				else if( utype.equals("Char.SpatialAxis.Coverage.Location.Value")) {
					val = obj.getPos_ra_csa() + " " + obj.getPos_dec_csa();
				}
				else if( utype.equals("Access.Reference") || ucd.equalsIgnoreCase("VOX:Image_AccessReference") ) {
					String format = this.queryInfos.params.get("format");
					cdata = true;
					if( "text/html".equals(format)) {
						val = url;
					}
					else {
						val = download_url;
					}
				}
				else if( ucd.equals("VOX:Image_FileSize") ) {
					val = String.valueOf(((new File(obj.getRepositoryPath())).length()));					
				}
				else if( ucd.equals("VOX:STC_CoordRefFrame") ) {
					try {
						val = obj.getFieldString("_radecsys");
					} catch(Exception e) {
						val = "";
					}
				}
				else if( utype.equals("Access.Format") || ucd.equalsIgnoreCase("VOX:Image_Format") ) {
					cdata = true;
					val = obj.getMimeType();
				}
				else if( utype.equals("DataID.Title") || ucd.equalsIgnoreCase("VOX:Image_Title") ) {
					cdata = true;
					val = obj.getNameSaada();
				}
				else if( id.equals("LinktoPixels")) {
					cdata = true;
					val = obj.getURL(true);
				}
				else if( ucd.equalsIgnoreCase("POS_EQ_RA_MAIN") ){
					val = Double.toString(obj.getPos_ra_csa());
				}
				else if( ucd.equalsIgnoreCase("POS_EQ_DEC_MAIN") ){
					val = Double.toString(obj.getPos_dec_csa());
				}
				else if( ucd.equalsIgnoreCase("VOX:Image_Naxes") ){
					val = "2";
				}
				else if( ucd.equalsIgnoreCase("VOX:Image_Naxis") ){
					val = obj.getNaxis1() + " " + obj.getNaxis2();
				}
				else if( ucd.equalsIgnoreCase("VOX:Image_Scale") ){
					val = (obj.getSize_alpha_csa() / obj.getNaxis1()) + " "
							+ (obj.getSize_delta_csa() / obj.getNaxis2());
				}
				else if( ucd.equals("VOX:WCS_CoordProjection") ) {
					val = obj.getCtype1_csa();
					// RA---TAN -> TAN e.g.
					if( val != null && val.length() > 3 ) {
						val = val.substring(val.length() - 3);
					}
				}
				else if( ucd.equals("VOX:WCS_CoordRefValue") ) {
					val = obj.getCrval1_csa() + " " + obj.getCrval2_csa();
				}
				else if( ucd.equals("VOX:WCS_CDMatrix") ) {
					val = obj.getCd1_1_csa() + " " + obj.getCd1_2_csa() + " " + obj.getCd2_1_csa() + " " + obj.getCd2_2_csa();
				}

				/*
				 * Utypes have an higher priority than UCDs: there are checked first
				 */
				else if( utype != null && utype.length() > 0 ){
					AttributeHandler ah  = obj.getFieldByUtype(sf.getUtype(), false);
					if( ah == null ) {
						val = "";					
					}
					else {
						Object v = obj.getFieldValue(ah.getNameattr());
						if( ah.getType().equals("String")) {
							cdata = true;
							val = v.toString();
						}
						else {
							val = v.toString();
						}
					}	
				}
				else if( ucd != null && ucd.length() > 0 ){
					AttributeHandler ah  = obj.getFieldByUCD(sf.getUcd(), false);
					if( ah == null ) {
						val = "";					
					}
					else {
						Object v = obj.getFieldValue(ah.getNameattr());
						if( ah.getType().equals("String")) {
							cdata = true;
							val = v.toString();
						}
						else {
							val = v.toString();
						}
					}
				}	
				if( this.vo_resource.getName().equals("SIA EPIC Charac") && 
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
	private String getXMMData(String utype, ImageSaada sp) throws Exception {

		if( utype.equals("CharacterisationAxis.coverage.location.coord.ScalarCoordinate.Value") ) {
			return sp.getCrval1_csa()  + " " + sp.getCrval2_csa();
		}
		else if( utype.equals("CharacterisationAxis.coverage.bounds.limits.charBox.size2") ) {
			return (sp.getCd1_1_csa()*sp.getNaxis1()) + " " + (sp.getCd2_2_csa()*sp.getNaxis2());
		}
		else if( utype.equals("CharacterisationAxis.coverage.bounds.limits.charBox.value") ) {
			return sp.getCrval1_csa() + " " + sp.getCrval2_csa();
		}
		else if( utype.equals("SpatialAxis.samplingPrecision.samplingPeriod.PixSize") ) {
			return ((Math.abs(sp.getCd1_1_csa()) + Math.abs(sp.getCd2_2_csa()))/2.0) + "";
		}
		else if( utype.equals("TimeAxis.coveraTime.Axis.coverage.location.coord.Time.TimeInstant.ISOTime") ) {
			return sp.getFieldValue("_date_obs").toString();
		}
		else if( utype.equals("TimeAxis.coverage.bounds.limits.TimeInterval.StartTime.ISOTime") ) {
			return sp.getFieldValue("_date_obs").toString();
		}
		else if( utype.equals("TimeAxis.coverage.bounds.limits.TimeInterval.StopTime.ISOTime") ) {
			return sp.getFieldValue("_date_end").toString();
		}
		else {
			return "";
		}
	}
	@Override
	protected void writeProtocolParamDescription() {
		ParamSet paramSet = new ParamSet();
		SavotParam param = new SavotParam();
		param.setName("INPUT:POS");
		param.setDescription("Search position in the form \"ra,dec\" where ra and dec are given in decimal degrees in the ICRS coordinate system");
		paramSet.addItem(param);

		param = new SavotParam();
		param.setName("INPUT:SIZE");
		param
		.setDescription("Size of search regions in the Ra and Dec directions");
		paramSet.addItem(param);

		param = new SavotParam();
		param.setName("INPUT:FORMAT");
		param.setValue("ALL");
		param.setDescription("Requested format of images");
		
		SavotValues formatValues = new SavotValues();
		OptionSet optionSet = new OptionSet();
		SavotOption option = new SavotOption();
		option.setValue("image/fits");
		optionSet.addItem(option);
		option = new SavotOption();
		option.setValue("text/html");
		optionSet.addItem(option);
//		option = new SavotOption();
//		option.setValue("image/jpeg");
//		optionSet.addItem(option);
//		option = new SavotOption();
//		option.setValue("GRAPHIC-ALL");
//		optionSet.addItem(option);
//		option = new SavotOption();
//		option.setValue("GRAPHIC");
//		optionSet.addItem(option);
		option = new SavotOption();
		option.setValue("METADATA");
		optionSet.addItem(option);
		option = new SavotOption();
		option.setValue("ALL");
		optionSet.addItem(option);
		formatValues.setOptions(optionSet);
		param.setValues(formatValues);
		paramSet.addItem(param);

		param = new SavotParam();
		param.setName("INPUT:INTERSECT");
		param
		.setDescription("How matched images should intersect the region of interest");
		param.setValue("ENCLOSED");
		formatValues = new SavotValues();
		optionSet = new OptionSet();
//		option = new SavotOption();
//		option.setValue("CENTER");
//		optionSet.addItem(option);
//		option = new SavotOption();
//		option.setValue("OVERLAPS");
//		optionSet.addItem(option);
//		option = new SavotOption();
//		option.setValue("COVERS");
//		optionSet.addItem(option);
		option = new SavotOption();
		option.setValue("ENCLOSED");
		optionSet.addItem(option);
		formatValues.setOptions(optionSet);
		param.setValues(formatValues);
		paramSet.addItem(param);

		param = new SavotParam();
		param.setName("INPUT:collection");
		param
		.setDescription("Gives the collections and possibly the classes in which we want to search. The format is as follows : collection=coll1,coll2 OR collection=coll1[class1, class2]");
		param.setValue("any");
		paramSet.addItem(param);

		writer.writeParam(paramSet);
		}


}
