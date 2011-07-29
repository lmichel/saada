/**
 * 
 */
package saadadb.vo.request.formator.json;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import saadadb.collection.Category;
import saadadb.collection.SaadaInstance;
import saadadb.collection.SaadaOID;
import saadadb.database.Database;
import saadadb.exceptions.QueryException;
import saadadb.meta.AttributeHandler;
import saadadb.meta.UTypeHandler;
import saadadb.query.result.ADQLResultSet;
import saadadb.util.Messenger;
import adqlParser.SaadaADQLQuery;
import adqlParser.query.ADQLColumn;
import adqlParser.query.ADQLOperand;

/**
 * @author laurent
 * @version $Id$
 */
public class TapAdqlJsonFormator extends JsonFormator {

	private ADQLResultSet adqlResultSet;
	private SaadaADQLQuery adqlQuery;
	public TapAdqlJsonFormator() throws QueryException {
		limit = 10000;
		protocolName = "TAP1.0";
		this.infoMap.put("LANGUAGE", "ADQL");

	}
	public void setAdqlResultSet(ADQLResultSet adqlResultSet){
		this.adqlResultSet = adqlResultSet;
	}

	public void setAdqlQuery(SaadaADQLQuery adqlQuery) {
		this.adqlQuery = adqlQuery;
		this.infoMap.put("QUERY", "<![CDATA["+adqlQuery+"]]>");
	}


	/* (non-Javadoc)
	 * @see saadadb.vo.request.formator.votable.VOTableFormator#writeData()
	 */
	protected void writeData() throws Exception {
		data = new JSONArray();
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
				this.writeRowData(null);
				i++;
			}
		}
	}


	/**
	 * @param obj
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	protected void writeRowData(SaadaInstance obj) throws Exception {
		JSONArray jsonRow = new JSONArray();
		for( int i=0 ; i<columns.size() ; i++ ) {
			String colname = (String) ((JSONObject)(columns.get(i))).get("sTitle");
			// gets its value:
			Object val = adqlResultSet.getObject(colname);
			// replace oid with DL URL for data produc files
			if( colname.equals("oidsaada")) {
				long oid = Long.parseLong(val.toString());
				if( SaadaOID.getCategoryNum(oid) != Category.ENTRY ) {
					val = new String(Database.getUrl_root() + "/getroduct?oid=" + val);
				}
			}
			
			if (val != null)
				jsonRow.add(val.toString());
			else
				jsonRow.add("");
		}
		data.add(jsonRow);
	}

	/* (non-Javadoc)
	 * @see saadadb.vo.request.formator.VOResultFormator#writeProtocolParamDescription()
	 */
	@Override
	protected void writeProtocolParamDescription() throws Exception {
	}

	public void setProtocolParams(Map<String, String> fmtParams) throws Exception{
		this.protocolParams =  new LinkedHashMap<String, String>();
	}

	/* (non-Javadoc)
	 * @see saadadb.vo.request.formator.votable.VOTableFormator#writeDMFieldsAndGroups()
	 */
	@SuppressWarnings("unchecked")
	protected void writeDMFieldsAndGroups() throws Exception{
		// Create one field for each returned column:
		int indCol = 0;
		Iterator<AttributeHandler> it = adqlResultSet.getMeta().iterator();
		columns = new  JSONArray();
		while(it.hasNext()){
			AttributeHandler meta = it.next();
			JSONObject jsonField = new JSONObject();
			if (meta == null && adqlQuery != null){

				if ( adqlQuery != null){
					ADQLOperand col = adqlQuery.getColumn(indCol);
					if (col.getAlias() != null){
						jsonField.put(JSON_COLUMN_TITLE, col.getAlias());
						//						if (col instanceof ADQLColumn)
						//							f.setDescription("Column '"+((ADQLColumn)col).getColumn()+"'");
					}else{
						if (col instanceof ADQLColumn)
							jsonField.put(JSON_COLUMN_TITLE, ((ADQLColumn)col).getColumn());
						else 
							jsonField.put(JSON_COLUMN_TITLE, (col.toString()));
					}
				}
			}else {
				jsonField.put(JSON_COLUMN_TITLE, (new UTypeHandler(meta)).getNickname());
				columns.add(jsonField);
			}
			indCol++;
		}
	}
}
