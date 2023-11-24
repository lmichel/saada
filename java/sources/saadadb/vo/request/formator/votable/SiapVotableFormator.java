package saadadb.vo.request.formator.votable;

import cds.savot.model.OptionSet;
import cds.savot.model.ParamSet;
import cds.savot.model.SavotOption;
import cds.savot.model.SavotParam;
import cds.savot.model.SavotValues;
import saadadb.collection.Category;
import saadadb.collection.obscoremin.ImageSaada;
import saadadb.database.Database;
import saadadb.exceptions.QueryException;
import saadadb.meta.AttributeHandler;
import saadadb.query.result.SaadaInstanceResultSet;

/**
 * @author laurent
 * @version 07/2011
 */
public class SiapVotableFormator extends VotableFormator {


	public SiapVotableFormator() throws Exception {
		this.setDataModel("SIA");
		this.dataModel.addObscoreFields();
		this.dataModel.addExtendedFields(Category.IMAGE);
		limit = 100;
		protocolN = "SIAP";
		protocolV="1.0";
		this.infoMap.put("SERVICE_PROTOCOL", new infoEntry(this.protocolV, this.protocolN));
	}
	/* (non-Javadoc)
	 * @see saadadb.vo.request.formator.Formator#supportResponseInRelation()
	 */
	@Override
	public boolean supportResponseInRelation() {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see saadadb.vo.request.formator.VOResultFormator#setResultSet(saadadb.query.result.SaadaInstanceResultSet)
	 */
	public void setResultSet(SaadaInstanceResultSet saadaInstanceResultSet) throws QueryException{	
		this.saadaInstanceResultSet = saadaInstanceResultSet;
	}

	
	@Override
	protected AttributeHandler getAttr_extended(String name) throws Exception{
		return Database.getCachemeta().getAtt_extend_image(name);
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
			return sp.crval1_csa  + " " + sp.crval2_csa;
		}
		else if( utype.equals("CharacterisationAxis.coverage.bounds.limits.charBox.size2") ) {
			return (sp.cd1_1_csa*sp.naxis1) + " " + (sp.cd2_2_csa*sp.naxis2);
		}
		else if( utype.equals("CharacterisationAxis.coverage.bounds.limits.charBox.value") ) {
			return sp.crval1_csa + " " + sp.crval2_csa;
		}
		else if( utype.equals("SpatialAxis.samplingPrecision.samplingPeriod.PixSize") ) {
			return ((Math.abs(sp.cd1_1_csa) + Math.abs(sp.cd2_2_csa))/2.0) + "";
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
	protected void writeProtocolParamDescription() throws Exception{
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
