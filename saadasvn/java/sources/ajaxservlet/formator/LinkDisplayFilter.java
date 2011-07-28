package ajaxservlet.formator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.json.simple.JSONArray;

import saadadb.api.SaadaLink;
import saadadb.collection.Category;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.meta.AttributeHandler;
import saadadb.meta.MetaRelation;

public class LinkDisplayFilter extends DefaultDisplayFilter {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private MetaRelation relation;
	private DisplayFilter collformator;
	private ArrayList<String> quals;
	
	public LinkDisplayFilter(String relation) throws FatalException {
		super(null);
		this.relation  = Database.getCachemeta().getRelation(relation);
		switch( this.relation.getSecondary_category() ) {
		case Category.TABLE    : collformator = new TableDisplayFilter(null);break;
		case Category.ENTRY    : collformator = new EntryDisplayFilter(null);break;
		case Category.IMAGE    : collformator = new ImageDisplayFilter(null);break;
		case Category.SPECTRUM : collformator = new SpectrumDisplayFilter(null);break;
		case Category.MISC     : collformator = new MiscDisplayFilter(null);break;
		case Category.FLATFILE : collformator = new FlatfileDisplayFilter(null);break;
		default:
		}
		quals = Database.getCachemeta().getRelation(relation).getQualifier_names();
		for( String q: quals) {
			datatable_columns.add(q);			
		}
		datatable_columns.addAll(collformator.getDisplayedColumns());
	}
	
	@Override
	public Set<String> getDisplayedColumns() {
		return datatable_columns;
	}

	@Override
	public Set<AttributeHandler> getQueriableColumns() throws FatalException {
		return null;
	}

	@Override
	public List<String> getRow(Object obj, int rank) throws Exception {
		SaadaLink sl   = (SaadaLink) obj;
		collformator.setOId(sl.getEndindOID());
		List<String> retour = new ArrayList<String>();
		for( String q: quals) {
			retour.add(DefaultFormats.getString(sl.getQualifierValue(q)));
		}
		retour.addAll(collformator.getRow(null, rank));
		this.getRel(retour);
		return retour;
	}

	@Override
	public JSONArray getClassKWTable() throws Exception {
		return null;
	}

	@Override
	public JSONArray getCollectionKWTable() throws Exception {
		return null;
	}

	@Override
	protected void setRelations() {
		// TODO Auto-generated method stub
		
	}
	
	public String getJSONString() {
		return null;
	}
	

}
