package ajaxservlet.formator;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.json.simple.JSONArray;

import saadadb.exceptions.FatalException;
import saadadb.meta.AttributeHandler;
import saadadb.meta.MetaClass;
import saadadb.query.result.OidsaadaResultSet;

public class AttributeHandlerDisplayFilter implements DisplayFilter {
	private ArrayList<String> datatable_columns;

	public AttributeHandlerDisplayFilter() {
		datatable_columns = new ArrayList<String>();
		datatable_columns.add("nameorg");
		datatable_columns.add("nameattr");
		datatable_columns.add("type");
		datatable_columns.add("unit");
		datatable_columns.add("ucd");
		datatable_columns.add("queriable");
		datatable_columns.add("comment");
	}

	public void setOId(long oidsaada) throws FatalException {
	}

	public void setMetaClass(MetaClass mc) {
	}

	public Set<String> getDisplayedColumns() {
		LinkedHashSet<String> retour = new LinkedHashSet<String>();
		retour.addAll(datatable_columns);
		return retour;
	}

	public Set<AttributeHandler> getQueriableColumns() throws FatalException {
		return new LinkedHashSet<AttributeHandler>();
	}

	public boolean valid(AttributeHandler ah) {
		return false;
	}

	public List<String> getRow(Object obj, int rank) throws Exception {
		List<String> retour = new ArrayList<String>();
		AttributeHandler ah = (AttributeHandler) obj;
		if( ah != null ) {
			retour .add(ah.getNameorg());
			retour .add(ah.getNameattr());
			retour .add(ah.getType());
			retour .add(ah.getUnit());
			retour .add(ah.getUcd());
			retour .add(String.valueOf(ah.isQueriable()));
			retour .add(ah.getComment());
		}
		return retour;
	}

	public JSONArray getClassKWTable() throws Exception {
		return null;
	}

	public JSONArray getCollectionKWTable() throws Exception {
		return null;
	}

	public String getTitle() {
		return "Attribute Handler";
	}

	public List<String> getLinks() {
		return null;
	}

	public void addUCDColumn(AttributeHandler ah) {
		// TODO Auto-generated method stub
		
	}

	public void setResultSet(OidsaadaResultSet resultSet) {
		// TODO Auto-generated method stub
		
	}
	
	public String getJSONString() {
		return null;
	}

}
