package saadadb.vo.request.formator.votable;

import java.util.Map;

import saadadb.collection.SaadaInstance;
import saadadb.exceptions.QueryException;
import saadadb.query.result.SaadaInstanceResultSet;
import saadadb.util.Messenger;
import cds.savot.model.ParamSet;
import cds.savot.model.SavotField;
import cds.savot.model.SavotParam;
import cds.savot.model.SavotTR;
import cds.savot.model.TDSet;

/**
 * @author laurent
 *  * @version $Id$

 */
public class SaadaqlVotableFormator extends VotableFormator {
	
	public SaadaqlVotableFormator() throws QueryException {
		limit = 100000;
		protocolName = "Native Saada";
	}
	
	/* (non-Javadoc)
	 * @see saadadb.vo.request.formator.VOResultFormator#setResultSet(saadadb.query.result.SaadaInstanceResultSet)
	 */
	public void setResultSet(SaadaInstanceResultSet saadaInstanceResultSet) throws QueryException{			
		this.saadaInstanceResultSet = saadaInstanceResultSet;
	}


	/* (non-Javadoc)
	 * @see saadadb.vo.request.formator.VOResultFormator#setProtocolParams(java.util.Map)
	 */
	public void setProtocolParams(Map<String, String> fmtParams) throws Exception{
		this.protocolParams = fmtParams;
		String[] classes = this.protocolParams.get("class").split(",");
		if( classes.length == 1 && !classes[0].equals("*") ) {
			setDataModel("native class" + classes[0] ) ;
		}
		else {
			setDataModel("native" + this.protocolParams.get("category")) ;
		}
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
				SaadaInstance saadaInstance = saadaInstanceResultSet.getInstance();
				i++;
				SavotTR savotTR = new SavotTR();					
				tdSet = new TDSet();
				this.writeRowData(saadaInstance);
				this.writeHouskeepingData(saadaInstance);
				//this.writeMappedUtypeData(oid);
				//this.writeAttExtendData(oid);
				this.writeExtReferences(saadaInstance);
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


	@Override
	protected void writeRowData(SaadaInstance obj) throws Exception {
		for( Object f: dataModelFieldSet.getItems()) {
			SavotField sf = (SavotField)f;
			String id = sf.getId();
			if( id.length() == 0 ) {
				id = sf.getName();
			}
			Object val;
			String colname = sf.getName();
			try {
				/*
				 * Columns with names starting with ucd_ denote values returned by UCD based queries
				 * The real value must then be retrieved in the business object
				 */
				if( colname.startsWith("ucd_")) {
					val = obj.getFieldValueByUCD(sf.getUcd(), false);
				}
				else if( colname.equals("product_url_csa") ) {
					val = obj.getURL(true);
				}
				else {
					val = obj.getFieldValue(colname);
				}
			} catch(Exception e) {
				e.printStackTrace();
				val = "";
			}
			if( val == null ) val = "";
			if( sf.getDataType().equals("char")) {
				addCDataTD(val.toString());
			}
			else {
				addTD(val.toString());						
			}
		}
	}

	@Override
	protected void writeProtocolParamDescription() throws Exception {
		ParamSet paramSet = new ParamSet();
		SavotParam param = new SavotParam();
		param.setDataType("char");
		param.setArraySize("*");
		param.setName("INPUT:query");
		param.setDescription("SaadaQL Query");
		paramSet.addItem(param);

		param = new SavotParam();
		param.setName("INPUT:limit");
		param.setDataType("int");
		param.setUnit("deg");
		param.setDescription("Max size of the result set");

		writer.writeParam(paramSet);		
	}

}
