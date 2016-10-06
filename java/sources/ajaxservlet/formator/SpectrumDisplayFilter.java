package ajaxservlet.formator;

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
import saadadb.collection.obscoremin.SpectrumSaada;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.meta.MetaClass;
import saadadb.meta.MetaCollection;
import saadadb.util.ChangeKey;
import saadadb.util.SaadaConstant;


/**
 * @author laurentmichel
 * * @version $Id$

 */
public class SpectrumDisplayFilter extends DefaultDisplayFilter {
	private static final long serialVersionUID = 1L;
	private static final Map<String, AttributeHandler> extatt_columns 
	= new  LinkedHashMap<String, AttributeHandler>(Database.getCachemeta().getAtt_extend(Category.SPECTRUM));

	public SpectrumDisplayFilter(String coll) throws FatalException {
		super(coll);
		datatable_columns.add("Access");
/*		datatable_columns.add("DL Link");
		datatable_columns.add("Visu");
		datatable_columns.add("Detail");
*/		datatable_columns.add("Position");
		datatable_columns.add("Name");
		datatable_columns.add("Range (" + Database.getSpect_unit() + ")");
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
		for( AttributeHandler ah:  MetaCollection.getAttribute_handlers_spectrum().values() )  {
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
		SpectrumSaada instance = (SpectrumSaada) Database.getCache().getObject(oidsaada);
		if( instance.getCategory() != Category.SPECTRUM) {
			QueryException.throwNewException(SaadaException.METADATA_ERROR
					, "ENTRY object expected (not " + Category.explain( instance.getCategory()) +")");
		}

		SpecialFieldFormatter sff = new SpecialFieldFormatter(instance);
		
		List<String> retour = new ArrayList<String>();
		for( String s: datatable_columns) {
			if( "Visu".equals(s)) {
				retour.add("<a  class=dl_ivoa onclick=WebSamp_mVc.fireSendVoreport(\"" + instance.oidsaada + "\");'></a>");
			}
			else if( "Access".equals(s)) {
				retour.add(DefaultPreviews.getDetailLink(oidsaada, "ClassLevel")
						+ DefaultPreviews.getInfoLink(oidsaada)
						+ DefaultPreviews.getDLLink(oidsaada, false)
						+ DefaultPreviews.getCartLink(oidsaada)
						+ DefaultPreviews.getSpecSAMP(oidsaada));
			}
			else if( "Detail".equals(s)) {
				retour.add(DefaultPreviews.getDetailLink(oidsaada, "ClassLevel"));
			}
			else if( "DL Link".equals(s)) {
				retour.add(DefaultPreviews.getDLLink(oidsaada, false));
			}
			else if( "Position".equals(s)) {
				retour.add(sff.getPos());
			}
			else if( s.startsWith("Range") ) {
				retour.add(DefaultFormats.getString(instance.em_min) + " - " + DefaultFormats.getString(instance.em_max) );
			}
			else if( "Name".equals(s)) {
				retour.add(instance.obs_id);
			}
		}
		for( String s: extatt_columns.keySet()) {
			Object val = instance.getFieldValue(s);
			if( val == null ) {
				retour.add("Not Set");
			}
			else {
				retour.add(DefaultFormats.getString(val));
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
		for( AttributeHandler ah:  MetaCollection.getAttribute_handlers_spectrum().values() )  {
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

	/* (non-Javadoc)
	 * @see ajaxservlet.formator.DefaultDisplayFilter#getLinks()
	 */
	public List<String> getLinks() {
		SpectrumSaada instance;
		List<String> retour = new ArrayList<String>();

		try {
			if( oidsaada != SaadaConstant.LONG) {
				instance = (SpectrumSaada) Database.getCache().getObject(oidsaada);
				SpecialFieldFormatter sfm = new SpecialFieldFormatter(instance);
				retour.add("Position " + DefaultFormats.getHMSCoord(instance.s_ra, instance.s_dec) );
				retour.add("Range " + DefaultFormats.getString(instance.em_min) + " - " + DefaultFormats.getString(instance.em_max) + " " + Database.getSpect_unit());
				retour.addAll(super.getLinks());
			}
		} catch (Exception e) {}
		return retour;

	}

	@Override
	protected void setRelations() {
		setRelations(Category.SPECTRUM);
	}
	
	@SuppressWarnings("unchecked")
	public String getRawJSON() {
		JSONObject jso  = new JSONObject();
		jso.put("saadaclass", "*");
		JSONArray jsa = new JSONArray();
		jsa.add("Any-Collection");		
		jso.put("collection", jsa);
		jso.put("category", "SPECTRUM");
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
		jsa.add("Position");	
		jsa.add("Range (" + Database.getSpect_unit() + ")");	
		jso.put("specialField", jsa);
		jsr  = new JSONObject();
		jsr.put("query", new JSONArray());
		jsa = new JSONArray();
		jsa.add("namesaada");
		for( String v: Database.getCachemeta().getAtt_extend_spectra().keySet() ){
			jsa.add(v);
		}
		jsa.add("Any-Class-Att");
		jsr.put("show", jsa);
		
		jso.put("collections", jsr);
		return jso.toJSONString();

	}

//	public String getRawJSON() {
//		String result = "";
//		result += "{ \"collection\": [\"Any-Collection\"],";
//		result += "\"category\": \"SPECTRUM\",";
//		result += "\"relationship\": {";
//		result += "\"show\": [\"Any-Relation\"],";
//		result += "\"query\": [\"Any-Relation\"]";
//		result += "},";
//		result += "\"ucd.show\": \"false\",";
//		result += "\"ucd.query\": \"false\",";
//		result += "\"specialField\": [\"Access\", \"Position\", \"Name\", \"Range (" + Database.getSpect_unit() + ")\"],";
//		result += "\"collections\": {";
//		result += "\"show\": [],";
//		result += "\"query\": []}}";
//		
//		return result;
//	}

}
