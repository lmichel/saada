package ajaxservlet.formator;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import saadadb.collection.SaadaInstance;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.meta.AttributeHandler;

/**
 * This dynamic display filter adds the management of class level attributes
 * @author michel
 * @version $Id$
 *
 */
public class DynamicClassDisplayFilter extends DynamicDisplayFilter {
	protected final LinkedHashSet<String> columns_natcla = new LinkedHashSet<String>();
	protected boolean anyClassAtt = false;

	public DynamicClassDisplayFilter(StoredFilter sf, String collection, String saadaclass) throws FatalException {
		super(sf, collection);
		if(saadaclass != null && saadaclass.length() > 0 && !saadaclass.equals(FilterKeys.ANY_CLASS) && !saadaclass.equals("*")) {
			mc = Database.getCachemeta().getClass(saadaclass);			
			ArrayList<String>tmp_array = sf.getCollection_show();

			if (tmp_array.size() > 0) {
				for (String val : tmp_array) {
					if( val.equals(FilterKeys.ANY_CLASS_ATT) ) {
						anyClassAtt = true;
					}
				}
				if( !anyClassAtt ) {
					for (String val : tmp_array) {
						if (val.startsWith("_")) columns_natcla.add(val);
					}
				} else {
					for( String ah : mc.getAttribute_names()) {
						columns_natcla.add(ah);
					}
				}
			}
		}

	}


	/* (non-Javadoc)
	 * @see ajaxservlet.formator.DynamicDisplayFilter#getDisplayedColumns()
	 */
	public Set<String> getDisplayedColumns() {
		Set<String> result = super.getDisplayedColumns();
		for (String val : columns_natcla) {
			if( !isDirective(val) ) {
				continue;
			}
			result.add(val);
		}
		return result;
	}

	/**
	 * provides a row of the table, given the object 
	 */
	public List<String> getRow(Object obj, int rank) throws Exception {
		SaadaInstance si = Database.getCache().getObject(oidsaada);
		List<String> retour = super.getRow(obj, rank);

		for (String natcol : columns_natcla) {
			if( !isDirective(natcol) ) {
				continue;
			}
			si.loadBusinessAttribute();
			String res = DefaultFormats.getString(si.getFieldValue(natcol));
			retour.add(res);
		}


		return retour;
	}

	/* (non-Javadoc)
	 * @see ajaxservlet.formator.DisplayFilter#addConstrainedColumns(java.util.Set)
	 */
	public void addConstrainedColumns(Set<AttributeHandler> ahs){
		super.addConstrainedColumns(ahs);
		for( AttributeHandler ah: ahs){
			String na = ah.getNameattr();
			if( na.startsWith("_")  ) {
				columns_natcla.add(ah.getNameattr());			
			}
		}		
	}

	public String toString() {
		String retour;
		retour = "Filter:  oid " + oidsaada + " \n";
		retour += "      Class "  + mc + " \n";;
		retour += "    Special " ;
		for( String s :columns_specialf ) retour += s + " " ;
		retour += "\n";
		retour += "     Native " ;
		for( String s :columns_natcol ) retour += s + " " ;
		retour += "\n";
		retour += " Native Cla " ;
		for( String s :columns_natcla ) retour += s + " " ;
		retour += "\n";
		retour += "       Ucds " ;
		for( AttributeHandler s :columns_ucds ) retour += s.getNameorg() + " " ;
		retour += "\n";
		retour += "  Relations " ;
		for( String s :columns_rel ) retour += s + " " ;
		retour += "\n";
		return retour;
	}

}
