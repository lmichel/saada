/**
 * 
 */
package saadadb.vo.request.formator.votable;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;

import saadadb.collection.Category;
import saadadb.collection.SaadaOID;
import saadadb.collection.obscoremin.SaadaInstance;
import saadadb.database.Database;
import saadadb.exceptions.QueryException;
import saadadb.meta.AttributeHandler;
import saadadb.meta.UTypeHandler;
import saadadb.meta.VOResource;
import saadadb.query.result.ADQLResultSet;
import saadadb.util.Messenger;
import adqlParser.SaadaADQLQuery;
import adqlParser.query.ADQLColumn;
import adqlParser.query.ADQLOperand;
import adqlParser.query.ADQLTable;
import cds.savot.model.FieldSet;
import cds.savot.model.SavotField;
import cds.savot.model.SavotTR;
import cds.savot.model.TDSet;



/**
 * @author laurent
 * @version 07/2011
 */
public class TapAdqlVotableFormator extends VotableFormator {

	private ADQLResultSet adqlResultSet;
	private SaadaADQLQuery adqlQuery;

	public TapAdqlVotableFormator() throws QueryException {
		limit = 10000;
		protocolN = "TAP";
		protocolV ="1.0";
		this.infoMap.put("LANGUAGE", new infoEntry("ADQL", null));

	}
	public void setAdqlResultSet(ADQLResultSet adqlResultSet){
		this.adqlResultSet = adqlResultSet;
	}

	public void setAdqlQuery(SaadaADQLQuery adqlQuery) {
		this.adqlQuery = adqlQuery;
		//TODO this.infoMap.put("QUERY", new infoEntry("<![CDATA["+adqlQuery+"]]>",null));
		this.infoMap.put("QUERY", new infoEntry(null,"<![CDATA["+adqlQuery+"]]>"));
	}


	/* (non-Javadoc)
	 * @see saadadb.vo.request.formator.votable.VOTableFormator#writeData()
	 */
	protected void writeData() throws Exception {
		writer.writeDataBegin();
		writer.writeTableDataBegin();
		if(adqlResultSet != null) {
			int i = 0 ; 
			long start = System.currentTimeMillis() ;
			while(adqlResultSet.next()) {
				if (this.limit > 0 && i >= this.limit){
					Messenger.printMsg(Messenger.TRACE, "result truncated to " + i);
					break;
				}
				/*
				 * SQLite (at least) becomes ery slow with queries returning large result set. 
				 * It is better to set a TO and to return a truncated result than to reach the browser TO
				 * The pb s that the user is not advertised of the situation
				 */
				long delta;
				long TIMEOUT = 30000;
				if(  (delta = (System.currentTimeMillis() - start)) > TIMEOUT) {
					Messenger.printMsg(Messenger.WARNING, "Query stopped on time out (" + (delta/1000) + "\") at " + i + " match ");
					break;				
				}	
				SavotTR savotTR = new SavotTR();					
				tdSet = new TDSet(); 
				writeRowData(null);
				//				this.writeHouskeepingData(si);
				//this.writeMappedUtypeData(oid);
				//this.writeAttExtendData(oid);
				//				this.writeExtReferences(si);
				savotTR.setTDs(tdSet);
				writer.writeTR(savotTR);
				i++;
			}
		}
		writer.writeTableDataEnd();
		writer.writeDataEnd();
	}


	/* (non-Javadoc)
	 * @see saadadb.vo.request.formator.votable.VOTableFormator#writeRowData(saadadb.collection.SaadaInstance)
	 */
	@Override
	protected void writeRowData(SaadaInstance obj) throws Exception {
		for( Object f: dataModelFieldSet.getItems()) {
			SavotField sf = (SavotField)f;

			String colname = sf.getName();
			try {
				// gets its value:
				Object val = adqlResultSet.getObject(colname);
				// replace oid with DL URL for data produc files
				boolean formated = false;
				if( colname.equals("oidsaada")) {
					long oid = Long.parseLong(val.toString());
					if( SaadaOID.getCategoryNum(oid) != Category.ENTRY ) {
						formated = true;
						val = new String(Database.getUrl_root() + "/getproduct?oid=" + val);
					}
				}
				// write the value in a TD element:
				if (val != null){
					if( formated || sf.getDataType().equals("char"))
						addCDataTD(val.toString());
					else
						addTD(val.toString());
				}else
					addTD("");
			}catch(SQLException ex){ 
				addTD("");
			}catch(Exception e) {
				throw e;
			}
		}
	}

	/* (non-Javadoc)
	 * @see saadadb.vo.request.formator.VOResultFormator#writeProtocolParamDescription()
	 */
	@Override
	protected void writeProtocolParamDescription() throws Exception {
	}

	/* (non-Javadoc)
	 * @see saadadb.vo.request.formator.QueryResultFormator#setProtocolParams(java.util.Map)
	 */
	public void setProtocolParams(Map<String, String> fmtParams) throws Exception{
		this.protocolParams = fmtParams;
	}

	/* (non-Javadoc)
	 * @see saadadb.vo.request.formator.votable.VotableFormator#writeExtMetaReferences()
	 */
	protected void writeExtMetaReferences() throws QueryException {}

	/* (non-Javadoc)
	 * @see saadadb.vo.request.formator.votable.VotableFormator#writeHousekeepingFieldAndGroup()
	 */
	protected void writeHousekeepingFieldAndGroup() {

	}


	/* (non-Javadoc)
	 * @see saadadb.vo.request.formator.votable.VOTableFormator#writeDMFieldsAndGroups()
	 */
	protected void writeDMFieldsAndGroups() throws Exception{
		dataModelFieldSet = new FieldSet();
		/*
		 * As TAP does not provide any way to know whether a response is mode compliant, we check if one searched table matches a DM.
		 * In this case, we will load the model and take its UTypehandler matching the name of the selected columns
		 * That way, we get ay mata data of the model with an evident risk of mismatch
		 *  @TODO This procedure should be generalized to all TAP tables and pushed in AdqlResultSet
		 */
		boolean obsceLike=false;
		Iterator<ADQLTable> itt = adqlQuery.getTables();
		while(itt.hasNext() ) {
			if( itt.next().getTable().equalsIgnoreCase("obscore")) {
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "ADQL query on obscore: try apply the model");
				obsceLike=true;
				break;
			}
		}
		if( obsceLike) {
			this.dataModel = VOResource.getResource("ObsCore");
		}		
		// Create one field for each returned column:
		int cpt = 100, indCol = 0;
		Iterator<AttributeHandler> it = adqlResultSet.getMeta().iterator();
		while(it.hasNext()){
			AttributeHandler meta = it.next();
			SavotField f = null;
			if (meta == null && adqlQuery != null){
				f = new SavotField();
				if (adqlQuery != null){
					ADQLOperand col = adqlQuery.getColumn(indCol);
					if (col.getAlias() != null){
						f.setName(col.getAlias());
						if (col instanceof ADQLColumn)
							f.setDescription("Column '"+((ADQLColumn)col).getColumn()+"'");
					}else{
						if (col instanceof ADQLColumn)
							f.setName(((ADQLColumn)col).getColumn());
						else
							f.setName(col.toString());								
					}
					f.setRef(f.getName()+"_"+cpt);
				}else
					f.setName("???");
				f.setDataType("char");
				f.setArraySize("*");
			}else {
				UTypeHandler uth=null;
				if( obsceLike ){
					uth = this.dataModel.getUTypeHandler(meta.getNameorg());
				}
				f = (uth == null)?(new UTypeHandler(meta)).getSavotField(cpt): uth.getSavotField(cpt);
				/*
				 * oidsaada could be replaced with download URLs
				 */
				if(meta.getNameattr().equals("oidsaada")) {
					f.setDataType("char");
					f.setArraySize("*");					
				}
			}

			dataModelFieldSet.addItem(f);
			indCol++;
		}
		writer.writeField(dataModelFieldSet);

	}

}
