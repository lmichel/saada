package ajaxservlet.formator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;

import saadadb.collection.Category;
import saadadb.collection.FlatfileSaada;
import saadadb.collection.SaadaInstance;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.meta.MetaCollection;
import saadadb.util.ChangeKey;
import saadadb.util.SaadaConstant;

/**
 * @author laurentmichel
 * * @version $Id: FlatfileDisplayFilter.java 650 2013-07-08 11:37:30Z laurent.mistahl $

 */
public class FlatfileDisplayFilter extends DefaultDisplayFilter {
	private static final long serialVersionUID = 1L;
	private static final Map<String, AttributeHandler> extatt_columns 
	= new  LinkedHashMap<String, AttributeHandler>(Database.getCachemeta().getAtt_extend(Category.FLATFILE));

	public FlatfileDisplayFilter(String coll) throws FatalException {
		super(coll);
		datatable_columns.add("Preview");
		datatable_columns.add("DL Link");
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
	/* (non-Javadoc)
	 * @see ajaxservlet.formator.DisplayFilter#getConstrainedColumns()
	 */
	public Set<String> getConstrainedColumns() {
		return null;
	}

	@Override
	public Set<AttributeHandler> getQueriableColumns() throws FatalException {
		LinkedHashSet<AttributeHandler> retour = new LinkedHashSet<AttributeHandler>();
		for( AttributeHandler ah:  MetaCollection.getAttribute_handlers_flatfile().values() )  {
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
		FlatfileSaada instance = (FlatfileSaada) Database.getCache().getObject(oidsaada);
		if( instance.getCategory() != Category.FLATFILE) {
			QueryException.throwNewException(SaadaException.METADATA_ERROR
					, "ENTRY object expected (not " + Category.explain( instance.getCategory()) +")");
		}
		List<String> retour = new ArrayList<String>();
		for( String s: datatable_columns) {
			if( "DL Link".equals(s)) {
				retour.add(DefaultPreviews.getDLLink(oidsaada, false));
			}
			else if( "Name".equals(s)) {
				retour.add(instance.namesaada);
			}
			else if( "Preview".equals(s)) {
				retour.add(DefaultPreviews.getFlatfilePreview(oidsaada, 64));
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

	@Override
	public JSONArray getClassKWTable() throws Exception {
		return new JSONArray();
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONArray getCollectionKWTable() throws Exception {
		JSONArray retour = new JSONArray ();
		SaadaInstance si = Database.getCache().getObject(oidsaada);
		for( AttributeHandler ah:  MetaCollection.getAttribute_handlers_flatfile().values() )  {
			if( this.valid(ah) ) {
				JSONArray list = new JSONArray();
				list.add(ah.getNameorg());
				list.add(si.getFieldString(ah.getNameattr()));
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
		FlatfileSaada instance;
		List<String> retour = new ArrayList<String>();

		try {
			if( oidsaada != SaadaConstant.LONG) {
				instance = (FlatfileSaada) Database.getCache().getObject(oidsaada);
				retour.add("<a class=download href='" + instance.getDownloadURL(true) + "'></A>");
			}
		} catch (FatalException e) {}
		return retour;
	}

	@Override
	protected void setRelations() {
		setRelations(Category.FLATFILE);
	}
	
	public String getRawJSON() {
		String result = "";
		result += "{ \"collection\": [\"Any-Collection\"],";
		result += "\"category\": \"FLATFILE\",";
		result += "\"relationship\": {";
		result += "\"show\": [\"Any-Relation\"],";
		result += "\"query\": [\"Any-Relation\"]";
		result += "},";
		result += "\"ucd.show\": \"false\",";
		result += "\"ucd.query\": \"false\",";
		result += "\"specialField\": [\"Preview\", \"Access\", \"Name\"],";
		result += "\"collections\": {";
		result += "\"show\": [],";
		result += "\"query\": []}}";
		
		return result;
	}

}
