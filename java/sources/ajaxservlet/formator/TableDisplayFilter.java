package ajaxservlet.formator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import saadadb.collection.Category;
import saadadb.collection.SaadaOID;
import saadadb.collection.obscoremin.SaadaInstance;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.meta.MetaClass;
import saadadb.meta.MetaCollection;
import saadadb.util.ChangeKey;

/**
 * Model data of category TABLE
 * @author laurent
 * * @version $Id$

 */
public class TableDisplayFilter extends DefaultDisplayFilter implements Serializable{
	private static final long serialVersionUID = 1L;
	private static final Map<String, AttributeHandler> extatt_columns = new  LinkedHashMap<String, AttributeHandler>(Database.getCachemeta().getAtt_extend(Category.TABLE));

	public TableDisplayFilter(String coll) throws FatalException {
		super(coll);
		datatable_columns.add("Access");
		datatable_columns.add("Table");
	}


	@Override
	public Set<String> getDisplayedColumns() {
		LinkedHashSet<String> retour = new LinkedHashSet<String>();
		retour.addAll(datatable_columns);
		for( String ec: extatt_columns.keySet()) {
			if( !ignored_keywords.contains(ec)) {
				retour.add(ec);
			}
		}
		addRelToDisplayColumns(retour);
		addUCDsToDisplayColumns(retour);
		return retour;
	}
	/* (non-Javadoc)
	 * @see ajaxservlet.formator.DisplayFilter#getConstrainedColumns()
	 */
	public Set<String> getConstrainedColumns() {
		return null;
	}

	@Override
	public Set<AttributeHandler> getQueriableColumns() throws FatalException {
		LinkedHashSet<AttributeHandler> retour = new LinkedHashSet<AttributeHandler>();
		for( AttributeHandler ah:  MetaCollection.getAttribute_handlers_table().values() )  {
			if( this.valid(ah) ) {
				retour .add(ah);
			}
		}
		if( mc != null ) {
			for( AttributeHandler ah: mc.getAttributes_handlers().values()) {
				retour .add(ah);
			}
		}
		return retour ;
	}


	@Override
	public List<String> getRow(Object obj, int rank) throws Exception {
		SaadaInstance instance = Database.getCache().getObject(oidsaada);
		if( instance.getCategory() != Category.TABLE) {
			QueryException.throwNewException(SaadaException.METADATA_ERROR
					, "TABLE object expected (not " + Category.explain( instance.getCategory()) +")");
		}
		List<String> retour = new ArrayList<String>();
		for( String s: datatable_columns) {
			if( "Table".equals(s)) {
				retour.add(instance.obs_id);
			}
			else if( "Header".equals(s)) {
				retour.add(DefaultPreviews.getHeaderLink(oidsaada) + " " + instance.obs_id);
			}
			else if( "DL Link".equals(s)) {
				retour.add(DefaultPreviews.getDLLink(oidsaada, false));
			}
			else if( "Entries".equals(s)) {
				retour.add(DefaultPreviews.getSourcesLink(oidsaada));
			}
			else {
				retour.add(s);
			}
		}
		for( String s: extatt_columns.keySet()) {
			Object val = instance.getFieldValue(s);
			if( val == null ) {
				retour.add("Not Set");
			}
			else {
				retour.add(val.toString());
			}
		}
		for( AttributeHandler ah : this.ucd_columns ) {
			retour.add(DefaultFormats.getString(resultSet.getObject(rank, ChangeKey.getUCDNickName(ah.getUcd())))
			+ " (" + DefaultFormats.getString(instance.getFieldDescByUCD(ah.getUcd())) + ")");
		}
		this.getRel(retour);
		return retour;
	}


	@SuppressWarnings("unchecked")
	@Override
	public JSONArray getClassKWTable() throws Exception {
		JSONArray retour = new JSONArray ();
		MetaClass mc = Database.getCachemeta().getClass(SaadaOID.getClassName(oidsaada));
		SaadaInstance si = Database.getCache().getObject(oidsaada);
		for( AttributeHandler ah: mc.getAttributes_handlers().values()) {
			JSONArray list = new JSONArray();
			list.add(ah.getNameorg());
			list.add(si.getFieldString(ah.getNameattr()));
			list.add(ah.getUnit());
			list.add(new Boolean(true));
			list.add(ah.getComment());
			retour.add(list);
		}
		return retour;
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONArray getCollectionKWTable() throws Exception {
		JSONArray retour = new JSONArray ();
		for( AttributeHandler ah:  MetaCollection.getAttribute_handlers_table().values() )  {
			if( this.valid(ah) ) {
				JSONArray list = new JSONArray();
				list.add(ah.getNameorg());
				list.add(Database.getCache().getObject(oidsaada).getFieldString(ah.getNameattr()));
				list.add(ah.getUnit());
				list.add(ah.getComment());
				retour.add(list);
			}
		}
		return retour;
	}


	@Override
	protected void setRelations() {
		setRelations(Category.TABLE);
	}
	
//	public String getRawJSON() {
//		String result = "";
//		result += "{ \"collection\": [\"Any-Collection\"],";
//		result += "\"category\": \"TABLE\",";
//		result += "\"relationship\": {";
//		result += "\"show\": [\"Any-Relation\"],";
//		result += "\"query\": [\"Any-Relation\"]";
//		result += "},";
//		result += "\"ucd.show\": \"false\",";
//		result += "\"ucd.query\": \"false\",";
//		result += "\"specialField\": [\"Access\", \"Table\"],";
//		result += "\"collections\": {";
//		result += "\"show\": [],";
//		result += "\"query\": []}}";
//		
//		return result;
//	}
	@SuppressWarnings("unchecked")
	public String getRawJSON() {
		JSONObject jso  = new JSONObject();
		jso.put("saadaclass", "*");
		JSONArray jsa = new JSONArray();
		jsa.add("Any-Collection");		
		jso.put("collection", jsa);
		jso.put("category", "TABLE");
		JSONObject jsr  = new JSONObject();
		jsa = new JSONArray();
		jsa.add("Any-Relation");	
		jsr.put("show", jsa);
		jsr.put("query", jsa);
		jso.put("relationship", jsr);
		jso.put("ucd.show", "false");
		jso.put("ucd.query", "false");
		jsa = new JSONArray();
		jsa.add("Access");	
		jso.put("specialField", jsa);
		jsr  = new JSONObject();
		jsr.put("query", new JSONArray());
		jsa = new JSONArray();
		jsa.add("namesaada");
		for( String v: Database.getCachemeta().getAtt_extend_table().keySet() ){
			jsa.add(v);
		}
		jsa.add("Any-Class-Att");
		jsr.put("show", jsa);
		
		jso.put("collections", jsr);
		return jso.toJSONString();

	}

//	{
//		 "saadaclass": "Cadel_3dhst", "collection": ["Deep"],"category": "TABLE"
//	,"relationship": {"show": [],"query": ["Any-Relation"]},"ucd.show": "false","ucd
//	.query": "false","specialField": ["Access", "Table"],"collections": {"show": ["X
//	CAT_OBSID","XCAT_INSTRUME","XCAT_OBJECT","namesaada"],"query": [""]}}

}
