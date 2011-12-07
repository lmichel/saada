package ajaxservlet.formator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;

import saadadb.collection.Category;
import saadadb.collection.EntrySaada;
import saadadb.collection.Position;
import saadadb.collection.SaadaOID;
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
 * Model data of category TABLE
 * @author laurent
 * * @version $Id$

 */
public class EntryDisplayFilter extends DefaultDisplayFilter {
	private static final long serialVersionUID = 1L;
	private static final Map<String, AttributeHandler> extatt_columns = new  LinkedHashMap<String, AttributeHandler>(Database.getCachemeta().getAtt_extend(Category.ENTRY));

	public EntryDisplayFilter(String coll) throws FatalException {
		super(coll);
//		datatable_columns.add("Aladin");
		datatable_columns.add("Access");
		datatable_columns.add("Position");
		datatable_columns.add("Error (arcsec)");
		datatable_columns.add("Name");
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

	@Override
	public Set<AttributeHandler> getQueriableColumns() throws FatalException {
		LinkedHashSet<AttributeHandler> retour = new LinkedHashSet<AttributeHandler>();
		for( AttributeHandler ah:  MetaCollection.getAttribute_handlers_entry().values() )  {
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
		EntrySaada instance = (EntrySaada) Database.getCache().getObject(oidsaada);
		if( instance.getCategory() != Category.ENTRY) {
			QueryException.throwNewException(SaadaException.METADATA_ERROR
					, "ENTRY object expected (not " + Category.explain( instance.getCategory()) +")");
		}

		SpecialFieldFormatter sff = new SpecialFieldFormatter(instance);
		
		List<String> retour = new ArrayList<String>();
		for( String s: datatable_columns) {
			if( "Detail".equals(s)) {
				retour.add(DefaultPreviews.getDetailLink(oidsaada, null));
			}
//			else if( "Aladin".equals(s)) {
//				retour.add("<a href='javascript:void(0);' title='Marks this source on Aladin view' class=aladinsmall onclick='sampView.firePointatSky(" + instance.getPos_ra_csa() + "," + instance.getPos_dec_csa() + ");'></a>");
//			}
			else if( "Table Header".equals(s)) {
				retour.add(DefaultPreviews.getHeaderLink(instance.getOidtable()));
			}
			else if( "Position".equals(s)) {
				retour.add(sff.getPos());
			}
			else if( "Error (arcsec)".equals(s)) {
				double e = instance.getError_maj_csa();
				if( e == SaadaConstant.DOUBLE ) {
					retour.add(DefaultFormats.getString(e));
				}
				else {
					retour.add("&plusmn;" + DefaultFormats.getString(3600 * e));
				}
			}
			else if( "Name".equals(s)) {
				retour.add(instance.namesaada);
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
		for( AttributeHandler ah: mc.getAttributes_handlers().values()) {
			JSONArray list = new JSONArray();
			list.add(ah.getNameorg());
			list.add(Database.getCache().getObject(oidsaada).getFieldString(ah.getNameattr()));
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
		for( AttributeHandler ah:  MetaCollection.getAttribute_handlers_entry().values() )  {
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
		setRelations(Category.ENTRY);
	}
	
	public String getJSONString() {
		String result = "";
		result += "{ \"collection\": [\"Any-Collection\"],";
		result += "\"category\": \"ENTRY\",";
		result += "\"relationship\": {";
		result += "\"show\": [\"Any-Relation\"],";
		result += "\"query\": [\"Any-Relation\"]";
		result += "},";
		result += "\"ucd.show\": \"false\",";
		result += "\"ucd.query\": \"false\",";
		result += "\"specialField\": [\"Access\", \"Position\", \"Error (arcsec)\", \"Name\"],";
		result += "\"collections\": {";
		result += "\"show\": [],";
		result += "\"query\": []}}";
		
		return result;
	}

}
