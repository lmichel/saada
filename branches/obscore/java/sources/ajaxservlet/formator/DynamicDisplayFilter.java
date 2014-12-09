package ajaxservlet.formator;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;

import saadadb.collection.Category;
import saadadb.collection.SaadaOID;
import saadadb.collection.obscoremin.EntrySaada;
import saadadb.collection.obscoremin.SaadaInstance;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.meta.AttributeHandler;
import saadadb.meta.MetaClass;
import saadadb.meta.MetaCollection;
import saadadb.query.result.OidsaadaResultSet;
import saadadb.util.ChangeKey;
import saadadb.util.SaadaConstant;


/**
 * Used to handle to apply a JSON filter (StoredFilter) to a specific Saada object instance
 * 
 * @author Clémentine Frère
 *  * @version $Id$

 * contact : frere.clementine[at]gmail.com
 *
 */

public class DynamicDisplayFilter implements DisplayFilter {
	private static final long serialVersionUID = 1L;
	protected long oidsaada = SaadaConstant.LONG;
	protected final LinkedHashSet<String> columns_specialf = new LinkedHashSet<String>();
	protected final LinkedHashSet<String> columns_natcol = new LinkedHashSet<String>();
	protected final LinkedHashSet<String> columns_constcol = new LinkedHashSet<String>();
	protected final LinkedHashSet<AttributeHandler> columns_ucds = new LinkedHashSet<AttributeHandler>();
	protected final LinkedHashSet<String> columns_rel = new LinkedHashSet<String>();
	protected MetaClass mc = null;
	protected OidsaadaResultSet resultSet;
	private StoredFilter sf;
	protected boolean anyCollAtt = false;
	protected long sessionId; // used to lock relationship indexes

	/**
	 * creates a DisplayFilter from a StoredFilter
	 * and the collection the filter is applied to
	 * 
	 * @param sf  :Stored filter
	 * @param coll: Collection on which the filter must be applied
	 * @throws FatalException 
	 */
	public DynamicDisplayFilter(StoredFilter sf, String coll) throws FatalException {
		this.sf = sf;

		ArrayList<String> tmp_array = sf.getSpecialField();
		for (String val : tmp_array) {
			columns_specialf.add(val);
		}

		tmp_array = sf.getCollection_show();

		if (tmp_array.size() > 0) {
			for (String val : tmp_array) {
				if( val.equals(FilterKeys.ANY_COLL_Att) ) {
					anyCollAtt = true;
				}
			}
			if( !anyCollAtt ) {
				for (String val : tmp_array) {
					if (!val.startsWith("_")) columns_natcol.add(val);
				}
			} else {
				for( String ah: MetaCollection.getAttribute_handlers(Category.getCategory(sf.getCategory())).keySet() ) {
					columns_natcol.add(ah);
				}
			}
		}

		/*
		 * reste à initialiser UCDs
		 */
		tmp_array = sf.getRelationship_show();
		if (tmp_array.size() > 0) {
			if (tmp_array.get(0).compareTo(FilterKeys.ANY_RELATION) == 0) {
				String[] tmp_tab = Database.getCachemeta().getRelationNamesStartingFromColl(coll,Category.getCategory(sf.getCategory()));
				for (String val : tmp_tab) {
					columns_rel.add("Rel : "+val);
				}
			} else if (!(tmp_array.get(0).compareTo("") == 0)) {
				for (String val : tmp_array) {
					columns_rel.add("Rel : "+val);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see ajaxservlet.formator.DisplayFilter#addConstrainedColumns(java.util.Set)
	 */
	public void addConstrainedColumns(Set<AttributeHandler> ahs){
		boolean alreadyHere = false;
		for( AttributeHandler ah: ahs){
			String na = ah.getNameattr();
			if( !na.startsWith("_")  ) {

				/*
				 * Avoid duplicate column
				 */
				if( na.equals("namesaada") ) {
					for( String sf: columns_specialf) {
						if( sf.equals("Name") || sf.equals(na)){
							alreadyHere = true;
						}
					}
				}
				if( !alreadyHere) {
					columns_constcol.add(ah.getNameattr());		
				}
			}	
		}
	}

	/* (non-Javadoc)
	 * @see ajaxservlet.formator.DisplayFilter#setSessionId(long)
	 */
	public void setSessionId(long sessionId) {
		this.sessionId = sessionId;
	}


	/**
	 * returns a set of strings containing the titles
	 * of the columns to be displayed by the filter
	 */
	public Set<String> getDisplayedColumns() {
		LinkedHashSet<String> result = new LinkedHashSet<String>();
		for (String val : columns_specialf) {
			result.add(val);
		}
		for (String val : columns_rel) {
			if( !isDirective(val) ) {
				continue;
			}
			result.add(val);
		}
		for (String val : columns_natcol) {
			if( !isDirective(val) ) {
				continue;
			}
			result.add(val);
		}
		result.addAll(this.getConstrainedColumns());
		return result;
	}

	/* (non-Javadoc)
	 * @see ajaxservlet.formator.DisplayFilter#getConstrainedColumns()
	 */
	public Set<String> getConstrainedColumns() {
		LinkedHashSet<String> result = new LinkedHashSet<String>();
		for (AttributeHandler ah : this.columns_ucds) {
			String lbl = "UCD " + ah.getUcd();
			if (ah.getUnit().length() > 0) {
				lbl += "(" + ah.getUnit() + ")";
			}
			result.add(lbl);
		}
		for (String val : columns_constcol) {
			if( !isDirective(val) ) {
				continue;
			}
			result.add(val);
		}
		return result;		
	}
	/**
	 * returns a set of strings containing the columns
	 * to be available in the query attributes list
	 */
	public Set<AttributeHandler> getQueriableColumns() throws FatalException {

		LinkedHashSet<AttributeHandler> result = new LinkedHashSet<AttributeHandler>();
		ArrayList<String> tmp_array = sf.getCollection_query();

		int cat;
		Map<String, AttributeHandler> attributs = null;
		for (String attr : tmp_array) {
			attributs = null;
			cat = Category.getCategory(sf.getCategory());
			attributs = MetaCollection.getAttribute_handlers(cat);
			result.add(attributs.get(attr));
		}

		if (mc != null) {
			for (AttributeHandler ah : mc.getAttributes_handlers().values()) {
				if (!ah.getCollname().startsWith("_"))
					result.add(ah);
			}
		}
		return result;
	}
	public void setOId(long oidsaada) throws FatalException {
		this.oidsaada = oidsaada;
		if (SaadaOID.getCategoryNum(oidsaada) != Category.FLATFILE) {
			mc = Database.getCachemeta().getClass(SaadaOID.getClassNum(oidsaada));
		}
	}

	/**
	 * provides a row of the table, given the object 
	 */
	public List<String> getRow(Object obj, int rank) throws Exception {
		SaadaInstance si = Database.getCache().getObject(oidsaada);
		SpecialFieldFormatter sff = new SpecialFieldFormatter(si);

		List<String> retour = new ArrayList<String>();

		for (String speField : columns_specialf) {
			if ("DL Link".equals(speField)) {
				retour.add(sff.getDLLink(false));

			} else if ("Position".equals(speField)) {
				retour.add(sff.getPos());

			} else if ("Access".equals(speField)) {
				retour.add(sff.getAccess(false));

			} else if ("Position with error".equals(speField)) {
				retour.add(sff.getPosWithError());

			} else if ("Size (deg)".equals(speField)) {
				retour.add(sff.getSize());

			} else if ("Aladin".equals(speField)) {
				retour.add(DefaultPreviews.getAladinSAMP(oidsaada));

			} else if ("Visu".equals(speField)) {
				retour.add(DefaultPreviews.getSpecSAMP(oidsaada));

			} else if ("TopCat".equals(speField)) {
				retour.add(DefaultPreviews.getTopcatSAMP(oidsaada));

			} else if ("Simbad".equals(speField)) {
				retour.add(sff.getSimbad());

			} else if ("VizieR".equals(speField)) {
				retour.add(sff.getVizier());

			} else if ("Name".equals(speField)) {
				retour.add(si.obs_id);

			} else if ("Detail".equals(speField)) {
				retour.add(DefaultPreviews.getDetailLink(oidsaada, null));

			} else if ("Plot".equals(speField)) {
				retour.add(DefaultPreviews.getImageVignette(oidsaada, 64));

			} else if ("Table".equals(speField)) {
				retour.add(si.obs_id);

			} else if ("Header".equals(speField)) {
				retour.add(DefaultPreviews.getHeaderLink(oidsaada));

			} else if ("Entries".equals(speField)) {
				retour.add(DefaultPreviews.getSourcesLink(oidsaada));

			} else if ("Table Header".equals(speField)) {
				retour.add(DefaultPreviews.getHeaderLink(((EntrySaada) si)
						.oidtable));

			} else if ("Error (arcsec)".equals(speField)) {
				double e = ((EntrySaada) si).s_resolution;
				if (e == SaadaConstant.DOUBLE) {
					retour.add(DefaultFormats.getString(e));
				} else {
					retour.add("&plusmn;" + DefaultFormats.getString(3600 * e));
				}

			} else if ("Preview".equals(speField)) {
				retour.add(DefaultPreviews.getFlatfilePreview(oidsaada, 64));

			} else if ( speField.startsWith("Energy Range") ) {
				retour.add(DefaultFormats.getString(si.em_min) + " - " + DefaultFormats.getString(si.em_max) );
			} else if ( speField.startsWith("time range") ) {
				retour.add(DefaultFormats.getDateRangeFromMJD(si.t_min, si.t_max) );
			} else if ( speField.startsWith("Gallery") ) {
				retour.add(Long.toString(oidsaada) );
			}
		}

		for (String rel : columns_rel) {
			if( !isDirective(rel) ) {
				continue;
			}
			/*
			 * filter Json string
			 */
			String rfs[] = rel.split("[ :]");
			rel = rfs[rfs.length - 1].trim();
			if( !Database.getCachemeta().getRelation(rel).isIndexed() ){
				retour.add("<span>No index!!</span>");
				continue;
			}
			long[] cpts = si.getCounterparts(rel, this.sessionId);
			int nbcounter = cpts.length;
			switch (nbcounter) {
			case 0 : 
				retour.add("<span>No link</span>");
				break;
			case 1 :
				long counterpart = cpts[0];
				SaadaInstance instance = Database.getCache().getObject(counterpart);

				int tmpcat = SaadaOID.getCategoryNum(counterpart);
				switch (tmpcat) {
				case (Category.IMAGE) :
					retour.add(DefaultPreviews.getImageVignette(counterpart, 64));
				break;
				case (Category.FLATFILE) :
					retour.add(DefaultPreviews.getFlatfilePreview(counterpart, 64));
				break;
				default :
					//retour.add("<span>"+ instance.namesaada+ " " + (DefaultPreviews.getDetailLink(counterpart, rel)) + "</span>");
					retour.add("<span>" + nbcounter + " links</span> " + DefaultPreviews.getDetailLink(oidsaada, rel));
				}
				break;
			default :
				retour.add("<span>" + nbcounter + " links</span> " + DefaultPreviews.getDetailLink(oidsaada, rel));
				//retour.add("<span>" + nbcounter + " links</span>");
				break;
			}
		}
		for (String natcol : columns_natcol) {
			if( !isDirective(natcol) ) {
				continue;
			}
			si.loadBusinessAttribute();
			String res = DefaultFormats.getString(si.getFieldValue(natcol));
			retour.add(res);
		}

		for (AttributeHandler ah : columns_ucds) {
			si.loadBusinessAttribute();
			retour.add(DefaultFormats.getString(resultSet.getObject(rank, ChangeKey.getUCDNickName(ah.getUcd())))
					+ " <a title=\"Native Attribute: " + DefaultFormats.getString(si.getFieldDescByUCD(ah.getUcd())) + "\" >(?)</a>");
		}

		for (String natcol : columns_constcol) {
			if( !isDirective(natcol) ) {
				continue;
			}
			si.loadBusinessAttribute();
			String res = DefaultFormats.getString(si.getFieldValue(natcol));
			retour.add(res);
		}


		return retour;
	}

	@SuppressWarnings("unchecked")
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

	public JSONArray getCollectionKWTable() throws Exception {
		List<String> retour = new ArrayList<String>();
		SaadaInstance si = Database.getCache().getObject(oidsaada);
		for (String natcol : columns_natcol) {
			retour.add(si.getFieldString(natcol));
		}
		for (String natcol : columns_constcol ){
			retour.add(si.getFieldString(natcol));
		}
		return null;
	}

	public void setMetaClass(MetaClass mc) {
		this.mc = mc;
	}

	//methode obsolète
	public boolean valid(AttributeHandler ah) {
		return false;
	}

	public String getTitle() {
		if (oidsaada != SaadaConstant.LONG) {
			try {
				SaadaInstance si = Database.getCache().getObject(oidsaada);
				String pos = DefaultFormats.getHMSCoord(si.s_ra, si.s_dec);
				
				return SaadaOID.getCategoryName(oidsaada) + "  "
				+ Database.getCache().getObject(oidsaada) .obs_id
				+ " " 
				+ pos;
			} catch (FatalException e) {
				return e.toString();
			}
		} else {
			return "";
		}
	}

	public List<String> getLinks() {
		return new ArrayList<String>();
	}

	public void setResultSet(OidsaadaResultSet resultSet) {
		this.resultSet = resultSet;
	}

	public void addUCDColumn(AttributeHandler ah) {
		if (ah != null)
			this.columns_ucds.add(ah);
	}

	public String getRawJSON() {
		return sf.getRawJSON();
	}

	/**
	 * return true if the quantity is a filter directive (e.g. Any-...) but not the
	 * name of a database quantity
	 * @param quantity
	 * @return
	 */
	public boolean isDirective(String quantity) {
		return (quantity.indexOf('-') == -1);	
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
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
		retour += "       Ucds " ;
		for( AttributeHandler s :columns_ucds ) retour += s.getNameorg() + " " ;
		retour += "\n";
		retour += "  Relations " ;
		for( String s :columns_rel ) retour += s + " " ;
		retour += "\n";
		return retour;
	}
}
