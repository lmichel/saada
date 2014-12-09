package saadadb.vo.request.formator.votable;

import java.io.IOException;

import saadadb.collection.obscoremin.SaadaInstance;
import saadadb.database.Database;
import saadadb.exceptions.QueryException;
import saadadb.query.result.SaadaInstanceResultSet;
import saadadb.util.Messenger;
import cds.savot.model.OptionSet;
import cds.savot.model.ParamSet;
import cds.savot.model.SavotOption;
import cds.savot.model.SavotParam;
import cds.savot.model.SavotTR;
import cds.savot.model.SavotValues;
import cds.savot.model.TDSet;

/**
 * @author laurent
 * @version 07/2011
 */
public class ConeSearchVotableFormator extends VotableFormator{

	public ConeSearchVotableFormator() throws QueryException {
		setDataModel("CS");
		limit = 10000;
		protocolN= "CS";
		protocolV ="1.0";
		this.infoMap.put("SERVICE_PROTOCOL", new infoEntry(this.protocolV, this.protocolN));
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
	 * @see saadadb.vo.request.formator.votable.VOTableFormator#writeData()
	 */
	protected void writeData() throws Exception {
		writer.writeDataBegin();
		writer.writeTableDataBegin();
		
		if( saadaInstanceResultSet != null  ) {
			int i=0 ;
			while( saadaInstanceResultSet.next() ) {
				if( i >= this.limit ) {
					break;
				}
				i++;
				SaadaInstance si = saadaInstanceResultSet.getInstance();
				SavotTR savotTR = new SavotTR();					
				tdSet = new TDSet();
				this.writeRowData(si);
				this.writeHouskeepingData(si);
				//this.writeMappedUtypeData(oid);
				//this.writeAttExtendData(oid);
				this.writeExtReferences(si);
				savotTR.setTDs(tdSet);
				writer.writeTR(savotTR);
				if( this.limit > 0 && i >= this.limit ) {
					Messenger.printMsg(Messenger.TRACE, "result truncated to " + i);
					break;
				}
			}
		}
		writer.writeTableDataEnd();
		writer.writeDataEnd();
	}


	/* (non-Javadoc)
	 * @see saadadb.vo.request.formator.votable.VOTableFormator#writeDMData(saadadb.collection.SaadaInstance)
	 */
/*	protected void writeRowData(SaadaInstance si) throws Exception {
		EntrySaada obj = (EntrySaada)( si) ;
		for( Object f: dataModelFieldSet.getItems()) {
			SavotField sf = (SavotField)f;
			String ucd   = sf.getUcd();
			String utype = sf.getUtype();
			String id = sf.getId();
			if( ucd.equals("Target.Pos")) {
				addTD(obj.s_ra + " " + obj.s_dec);
			}
			else if( utype.equals("Char.SpatialAxis.Coverage.Location.Value")) {
				addTD(obj.s_ra + " " + obj.s_dec);
			}
			else if( ucd.equals("VOX:Image_AccessReference")) {
				addCDataTD(Database.getUrl_root() + "/getinstance?oid=" + obj.oidsaada);
			}
			else if( utype.equals("Access.Format")) {
				addCDataTD("catalog");
			}
			else if( utype.equals("DataID.Title") || ucd.equalsIgnoreCase("VOX:Image_Title") ) {
				addCDataTD(obj.obs_id.replaceAll("#", ""));
			}
			else if( id.equals("LinktoPixels")) {
				addCDataTD(obj.getURL(true));
			}
			else if( ucd.equalsIgnoreCase("POS_EQ_RA_MAIN") || ucd.equalsIgnoreCase("pos.eq.ra;meta.main") ){
				addTD(Double.toString(obj.s_ra));
			}
			else if( ucd.equalsIgnoreCase("POS_EQ_DEC_MAIN") || ucd.equalsIgnoreCase("pos.eq.dec;meta.main")){
				addTD(Double.toString(obj.s_dec));
			}
			else if( ucd.equalsIgnoreCase("ID_MAIN") ){
				addTD(Long.toString(obj.oidsaada));
			}
			else if( ucd.equalsIgnoreCase("meta.title") ){
				addTD(obj.obs_id);
			}
			
			 // Utypes have an higher priority than UCDs: there are checked first
			 
			else if( utype != null && utype.length() > 0 ){
				AttributeHandler ah  = obj.getFieldByUtype(sf.getUtype(), false);
				if( ah == null ) {
					addTD("");					
				}
				else {
					Object val = obj.getFieldValue(ah.getNameattr());
					if( ah.getType().equals("String")) {
						addCDataTD(val.toString());
					}
					else {
						addTD(val.toString());						
					}
				}	
			}
			else if( ucd != null && ucd.length() > 0 ){
				AttributeHandler ah  = obj.getFieldByUCD(sf.getUcd(), false);
				if( ah == null ) {
					addTD("");					
				}
				else {
					Object val = obj.getFieldValue(ah.getNameattr());
					if( ah.getType().equals("String")) {
						addCDataTD(val.toString());
					}
					else {
						addTD(val.toString());						
					}
				}
			}
		}
	}
*/
	@Override
	protected void writeProtocolParamDescription() throws IOException {
		ParamSet paramSet = new ParamSet();
		SavotParam param = new SavotParam();
		param.setName("INPUT:RA");
		param.setDescription("Search position right ascension in the ICRS coordinate system");
		paramSet.addItem(param);
		param = new SavotParam();
		param.setName("INPUT:DEC");
		param.setDescription("Search position right ascension in the ICRS coordinate system");
		paramSet.addItem(param);

		param = new SavotParam();
		param.setName("INPUT:SR");
		param.setDescription("Size of search regions in the Ra and Dec directions");
		paramSet.addItem(param);

		param = new SavotParam();
		param.setName("INPUT:FORMAT");
		param.setValue("ALL");
		param.setDescription("Requested format of results");
		SavotValues formatValues = new SavotValues();
		OptionSet optionSet = new OptionSet();
		SavotOption option = new SavotOption();
		option.setValue("fits");
		optionSet.addItem(option);
		option = new SavotOption();
		option.setValue("votable");
		optionSet.addItem(option);
		option = new SavotOption();
		option.setValue("METADATA");
		optionSet.addItem(option);
		formatValues.setOptions(optionSet);
		param.setValues(formatValues);
		paramSet.addItem(param);

		param = new SavotParam();
		param.setName("INPUT:collection");
		param
		.setDescription("Gives the collection and possibly the class to be searched. The format is as follow : collection=coll1 OR collection=coll1[class1]");
		param.setValue("any");
		SavotValues svs = new SavotValues();
		OptionSet os = new OptionSet();
		option = new SavotOption(); option.setValue("any");
		os.addItem(option);
		for( String coll : Database.getCachemeta().getCollection_names() ) {
			option = new SavotOption(); 
			option.setValue(coll);
			os.addItem(option);
		}
		svs.setOptions(os);
		param.setValues(svs);
		paramSet.addItem(param);

		writer.writeParam(paramSet);
	}


}
