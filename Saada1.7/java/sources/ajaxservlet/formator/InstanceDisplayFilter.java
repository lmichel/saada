package ajaxservlet.formator;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.json.simple.JSONArray;

import saadadb.collection.Category;
import saadadb.collection.SaadaInstance;
import saadadb.collection.SaadaOID;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.meta.AttributeHandler;
import saadadb.meta.MetaClass;
import saadadb.meta.MetaCollection;

public class InstanceDisplayFilter extends DefaultDisplayFilter {
	/**
	 * * @version $Id$

	 */
	private static final long serialVersionUID = 1L;
	private DisplayFilter collformator;


	public InstanceDisplayFilter(String coll) throws FatalException {
		super(coll);
		datatable_columns.add("Keyword");
		datatable_columns.add("Value");
		datatable_columns.add("Unit");
		datatable_columns.add("Comment");
	}

	@Override
	public void setOId(long oidsaada) throws FatalException {
		super.setOId(oidsaada);		
		this.setMetaClass(mc);
		if( collformator != null  ) {
			collformator.setOId(oidsaada);
		}

	}

	public void setMetaClass(MetaClass mc) throws FatalException {
		switch( SaadaOID.getCategoryNum(oidsaada)) {
		case Category.TABLE    : collformator = new TableDisplayFilter(mc.getCollection_name());break;
		case Category.ENTRY    : collformator = new EntryDisplayFilter(mc.getCollection_name());break;
		case Category.IMAGE    : collformator = new ImageDisplayFilter(mc.getCollection_name());break;
		case Category.SPECTRUM : collformator = new SpectrumDisplayFilter(mc.getCollection_name());break;
		case Category.MISC     : collformator = new MiscDisplayFilter(mc.getCollection_name());break;
		// mc is  null for FLATFILES
		case Category.FLATFILE : collformator = new FlatfileDisplayFilter(SaadaOID.getCollectionName(oidsaada));break;
		default:
		}
		this.mc = mc;
	}

	@Override
	public Set<String> getVisibleColumns() {
		LinkedHashSet<String> retour = new LinkedHashSet<String>();
		for( String ec: datatable_columns) {
			if( !ignored_keywords.contains(ec)) {
				retour.add(ec);
			}
		}
		addRelToDisplayColumns(retour);
		return retour;
	}
	/* (non-Javadoc)
	 * @see ajaxservlet.formator.DisplayFilter#getConstrainedColumns()
	 */
	public Set<String> getConstrainedColumns() {
		return null;
	}

	@Override
	public Set<AttributeHandler> getQueriableColumns() {
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
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONArray getClassKWTable() throws Exception {
		JSONArray retour = new JSONArray ();
		if( SaadaOID.getCategoryNum(oidsaada) != Category.FLATFILE ) {
			MetaClass mc = Database.getCachemeta().getClass(SaadaOID.getClassName(oidsaada));
			SaadaInstance si = Database.getCache().getObject(oidsaada);
			for( AttributeHandler ah: mc.getAttributes_handlers().values()) {
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

	@SuppressWarnings("unchecked")
	@Override
	public JSONArray getCollectionKWTable() throws Exception {
		JSONArray retour = new JSONArray ();
		SaadaInstance si = Database.getCache().getObject(oidsaada);
		for( AttributeHandler ah:   MetaCollection.getAttribute_handlers(SaadaOID.getCategoryNum(oidsaada)).values() )  {
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

	@Override
	public boolean valid(AttributeHandler ah) {
		if( collformator != null )
			return collformator.valid(ah);
		else
			return true;
	}

	public List<String> getLinks() throws Exception {
		if( collformator != null ) {
			return collformator.getLinks();
		}
		else {
			return new ArrayList<String>();
		}
	}

	@Override
	protected void setRelations() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see ajaxservlet.formator.DisplayFilter#getDisplayJSONFilter()
	 */
	public String getVisibleJSONDisplayFilter() {
		return null;
	}
	/* (non-Javadoc)
	 * @see ajaxservlet.formator.DisplayFilter#getDisplayableJSONFilter()
	 */
	public String getJSONDisplayFilter() {
		return null;
	}
}
