package saadadb.vo.request.formator.json;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import saadadb.collection.SaadaInstance;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.vo.request.formator.QueryResultFormator;


/**
 * @author laurent
 *  * @version $Id$

 */
public abstract class JsonFormator extends QueryResultFormator {
	private PrintWriter writer;
	protected JSONObject retour ;
	protected JSONArray  columns ;
	protected JSONArray  data ;
	protected ArrayList<String> dataModelFieldSet;

	public static final String JSON_COLUMN_TITLE = "sTitle";
	public static final String JSON_COLUMN_FIELD = "aoColumns";
	public static final String JSON_DATA_FIELD   = "aaData";

	public JsonFormator() {
		this.defaultSuffix = QueryResultFormator.getFormatExtension("json");
	}

	/* (non-Javadoc)
	 * @see saadadb.vo.request.formator.Formator#buildMetaResponse()
	 */
	public void buildMetaResponse() throws Exception {
	}

	@SuppressWarnings("unchecked")
	public void buildErrorResponse(Exception e) throws Exception {
		this.retour = new JSONObject();
		this.retour.put("errormsg", e.getMessage());
		this.writer = new PrintWriter(new File(this.getResponseFilePath()));
		this.writer.println(this.retour.toJSONString());
		this.writer.close();
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public void buildDataResponse() throws Exception {
		this.retour = new JSONObject();
		this.writeDMFieldsAndGroups();
		this.retour.put(JSON_COLUMN_FIELD, columns);
		this.writeData();
		this.retour.put(JSON_DATA_FIELD, data);  

		this.writer = new PrintWriter(new File(this.getResponseFilePath()));
		this.retour.writeJSONString(this.writer);
		this.writer.close();
	}

		
	/**
	 * Writes the data bloc in this.data
	 * @throws Exception
	 */
	abstract protected void writeData() throws Exception;


	@Override
	protected void writeHouskeepingData(SaadaInstance obj)
			throws SaadaException {
	}

	@Override
	protected void writeExtReferences(SaadaInstance obj) {
	}

	@Override
	protected void writeProtocolParamDescription() throws Exception {
	}

	@Override
	protected void writeExtMetaReferences() throws QueryException {
	}

	@Override
	protected void writeHousekeepingFieldAndGroup() {
	}


}
