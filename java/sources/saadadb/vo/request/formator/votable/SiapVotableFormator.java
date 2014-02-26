package saadadb.vo.request.formator.votable;

import java.io.File;
import java.util.Date;

import saadadb.collection.ImageSaada;
import saadadb.collection.SaadaInstance;
import saadadb.database.Database;
import saadadb.database.Repository;
import saadadb.dataloader.mapping.ProductMapping;
import saadadb.exceptions.QueryException;
import saadadb.meta.AttributeHandler;
import saadadb.prdconfiguration.ConfigurationDefaultHandler;
import saadadb.query.result.SaadaInstanceResultSet;
import saadadb.util.ImageUtils;
import cds.savot.model.OptionSet;
import cds.savot.model.ParamSet;
import cds.savot.model.SavotField;
import cds.savot.model.SavotOption;
import cds.savot.model.SavotParam;
import cds.savot.model.SavotValues;

/**
 * @author laurent
 * @version 07/2011
 */
public class SiapVotableFormator extends VotableFormator {


	public SiapVotableFormator() throws QueryException {
		setDataModel("SIA");
		limit = 100;
		protocolName = "SIAP1.0";
	}
	/* (non-Javadoc)
	 * @see saadadb.vo.request.formator.Formator#supportResponseInRelation()
	 */
	public boolean supportResponseInRelation() {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see saadadb.vo.request.formator.VOResultFormator#setResultSet(saadadb.query.result.SaadaInstanceResultSet)
	 */
	public void setResultSet(SaadaInstanceResultSet saadaInstanceResultSet) throws QueryException{	
		this.saadaInstanceResultSet = saadaInstanceResultSet;
	}


	

	/* (non-Javadoc)
	 * @see saadadb.vo.request.formator.votable.VOTableFormator#writeDMData(saadadb.collection.SaadaInstance)
	 */
	protected void writeRowData(SaadaInstance si) throws Exception {
		ImageSaada obj = (ImageSaada)( si) ;
		String download_url;
		String url;
		String targetfile;
		if( this.getProtocolParam("mode").equalsIgnoreCase("cutout") ) {
			ProductMapping cdh = obj.getLoaderConfig();
			targetfile = "tile_" + ((new Date()).getTime()) + ".fits";
			String targetpath = Repository.getVoreportsPath() + Database.getSepar() + targetfile ;
			ImageUtils.buildTileFile(obj.getPos_ra_csa(), obj.getPos_dec_csa()
					, Double.parseDouble(this.getProtocolParam("size_ra"))
					, Double.parseDouble(this.getProtocolParam("size_dec"))
					, obj.getRepositoryPath(), cdh, targetpath);
			download_url = Database.getUrl_root() + "/getproduct?report=" + targetfile;
			url = Database.getUrl_root() + "/getproduct?reports=" + targetfile;
		} else {
			url = obj.getURL(true);
			download_url = obj.getDownloadURL(true);			
		}
		for( Object f: dataModelFieldSet.getItems()) {
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
				String format = this.getProtocolParam("format");
				cdata = true;
				if( "text/html".equals(format)) {
					val = url;
				} else {
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
				} else {
					Object v = obj.getFieldValue(ah.getNameattr());
					if( ah.getType().equals("String")) {
						cdata = true;
						val = v.toString();
					} else {
						val = v.toString();
					}
				}	
			} else if( ucd != null && ucd.length() > 0 ){
				AttributeHandler ah  = obj.getFieldByUCD(sf.getUcd(), false);
				if( ah == null ) {
					val = "";					
				} else {
					Object v = obj.getFieldValue(ah.getNameattr());
					if( ah.getType().equals("String")) {
						cdata = true;
						val = v.toString();
					} else {
						val = v.toString();
					}
				}
			}	
			if( cdata ) {
				addCDataTD(val);
			} else {
				addTD(val);
			}
		}
	}

	/**
	 * @param utype
	 * @param sp
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unused")
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
		option = new SavotOption();
		option.setValue("CENTER");
		optionSet.addItem(option);
		option = new SavotOption();
		option.setValue("OVERLAPS");
		optionSet.addItem(option);
		option = new SavotOption();
		option.setValue("COVERS");
		optionSet.addItem(option);
		option = new SavotOption();
		option.setValue("ENCLOSED");
		optionSet.addItem(option);
		formatValues.setOptions(optionSet);
		param.setValues(formatValues);
		paramSet.addItem(param);

		param = new SavotParam();
		param.setName("INPUT:collection");
		param
		.setDescription("Gives the collection and possibly the class to be searched. The format is as follow : collection=coll1 OR collection=coll1[class1]");
		param.setValue("any");
		paramSet.addItem(param);

		writer.writeParam(paramSet);
	}

}
