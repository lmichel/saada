package saadadb.vo.request.formator.votable;

import java.util.Map;

import saadadb.collection.SaadaInstance;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.query.result.SaadaInstanceResultSet;
import saadadb.util.Messenger;
import saadadb.vo.request.formator.sapmapper.SapFieldMapper;
import cds.savot.model.ParamSet;
import cds.savot.model.SavotField;
import cds.savot.model.SavotParam;
import cds.savot.model.SavotTR;
import cds.savot.model.TDSet;

/**
 * @author laurent
 * @version 07/2011
 * 10/2013: Support of S*AP model
 */
public class SaadaqlVotableFormator extends VotableFormator {
	/**
	 * True if the current model is not "native"
	 */
	private boolean needModel = true;
	/**
	 * Object doing a rough mapping from native fields to S*AP fields @see SapFieldMapper
	 */
	private SapFieldMapper sapFieldMapper = new SapFieldMapper();

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
	/* (non-Javadoc)
	 * @see saadadb.vo.request.formator.QueryResultFormator#setProtocolParams(java.util.Map)
	 */
	public void setProtocolParams(Map<String, String> fmtParams) throws Exception{
		this.protocolParams = fmtParams;
		String model = this.protocolParams.get("model");
		String category = this.protocolParams.get("category");
		/*
		 * Switch on on good S*AP model if a vo/samp model is requested
		 */
		if( "samp".equalsIgnoreCase(model) ||  "samp".equalsIgnoreCase(model) ) {
			if( "ENTRY".equalsIgnoreCase(category)) {
				model = "cs";
			} else if( "IMAGE".equalsIgnoreCase(category)) {
				model = "sia";
			}  else if( "SPECTRUM".equalsIgnoreCase(category)) {
				model = "ssa";
			} else {
				model = "";
			}
		}
		/*
		 * Switch on the requested model
		 */
		if( "sia".equalsIgnoreCase(model)) {
			setDataModel("SIA"); 			
		} else if( "ssa".equalsIgnoreCase(model)) {
			setDataModel("SSA"); 			
		}else if( "cs".equalsIgnoreCase(model)) {
			setDataModel("CS"); 			
		} else{
			this.needModel = false;
			String str;
			if( (str = this.protocolParams.get("class")) != null ) {
				String[] classes = str.split(",");
				if( classes.length == 1 && !classes[0].equals("*") ) {
					setDataModel("native class " + classes[0] ) ;
					return;
				}			
			}
			if ((str = this.protocolParams.get("category")) != null) {
				setDataModel("native " + str) ;
			} else {
				QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "Cannot extract DM from paramters" );
			}
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
				if( needModel )sapFieldMapper.setInstance(saadaInstance);
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
			Object val="";
			String colname = sf.getName();
			boolean valSet = false, cdata = false;
			try {
				/*
				 * Use first the model mapping
				 */
				if( this.needModel ) {
					String ucd = sf.getUcd();
					String utype = sf.getUtype();
					if( ucd.length() > 0 ) {
						sapFieldMapper.getFieldValue(sf.getUcd());
					}
					if( sapFieldMapper.value.isNotSet && utype.length() > 0 ) {
						sapFieldMapper.getFieldValue(sf.getUtype());
					}
					valSet = !sapFieldMapper.value.isNotSet;
					val = sapFieldMapper.value.fieldValue.trim();
					cdata = sapFieldMapper.value.isCdata;
				}
				/*
				 * If the value can not be set within the model of if there is no model mapping
				 * Let's look at native fields
				 */
				if( !valSet) {
					if( sf.getDataType().equals("char") ) cdata= true;
					/*
					 * Columns with names starting with ucd_ denote values returned by UCD based queries
					 * The real value must then be retrieved in the business object
					 */
					if( colname.startsWith("ucd_")) {
						val = obj.getFieldValueByUCD(sf.getUcd(), false);
					} else if( colname.equals("product_url_csa") ) {
						val = obj.getURL(true);
					} else {
						val = obj.getFieldValue(colname);
					}
				}
			} catch(Exception e) {
				val = obj.getFieldValueByUCD(sf.getUcd(), false);
				//val = "";
			}
			if( val == null ) val = "";
			if( cdata) {
				addCDataTD(val.toString());
			} else {
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
