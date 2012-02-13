package ajaxservlet.formator;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;

import saadadb.collection.Category;
import saadadb.collection.EntrySaada;
import saadadb.collection.SaadaInstance;
import saadadb.collection.SaadaOID;
import saadadb.collection.SpectrumSaada;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.meta.AttributeHandler;
import saadadb.meta.MetaClass;
import saadadb.meta.MetaCollection;
import saadadb.query.result.OidsaadaResultSet;
import saadadb.util.ChangeKey;
import saadadb.util.SaadaConstant;


/**
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
	protected final LinkedHashSet<AttributeHandler> columns_ucds = new LinkedHashSet<AttributeHandler>();
	protected final LinkedHashSet<String> columns_rel = new LinkedHashSet<String>();
	protected MetaClass mc = null;
	protected OidsaadaResultSet resultSet;


	private StoredFilter sf;

	
	/**
	 * creates a DisplayFilter from a StoredFilter
	 * and the collection the filter is applied to
	 * 
	 * @param sf
	 * @param coll
	 */
	public DynamicDisplayFilter(StoredFilter sf, String coll) {
		try {
			this.sf = sf;

			ArrayList<String> tmp_array = sf.getSpecialField();
			for (String val : tmp_array) {
				columns_specialf.add(val);
			}

			tmp_array = sf.getCollection_show();
			
			if (tmp_array.size() > 0) {
				for (String val : tmp_array) {
					if (!val.startsWith("_")) columns_natcol.add(val);
				}
			}

			/*
			 * reste à initialiser UCDs
			 */
			
			tmp_array = sf.getRelationship_show();
			if (tmp_array.size() > 0) {
				if (tmp_array.get(0).compareTo("Any-Relation") == 0) {
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
		} catch (FatalException e) {
			e.printStackTrace();
		}
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
		for (String val : columns_natcol) {
			result.add(val);
		}
		for (String val : columns_rel) {
			result.add(val);
		}
		for (AttributeHandler ah : this.columns_ucds) {
			String lbl = "UCD " + ah.getUcd();
			if (ah.getUnit().length() > 0) {
				lbl += "(" + ah.getUnit() + ")";
			}
			result.add(lbl);
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
				retour.add(sff.getDLLink());

			} else if ("Position".equals(speField)) {
				retour.add(sff.getPos());

			} else if ("Access".equals(speField)) {
				retour.add(sff.getAccess());

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
				retour.add(si.namesaada);

			} else if ("Detail".equals(speField)) {
				retour.add(DefaultPreviews.getDetailLink(oidsaada, null));

			} else if ("Plot".equals(speField)) {
				retour.add(DefaultPreviews.getImageVignette(oidsaada, 64));

			} else if ("Table".equals(speField)) {
				retour.add(si.namesaada);

			} else if ("Header".equals(speField)) {
				retour.add(DefaultPreviews.getHeaderLink(oidsaada));

			} else if ("Entries".equals(speField)) {
				retour.add(DefaultPreviews.getSourcesLink(oidsaada));

			} else if ("Table Header".equals(speField)) {
				retour.add(DefaultPreviews.getHeaderLink(((EntrySaada) si)
						.getOidtable()));

			} else if ("Error (arcsec)".equals(speField)) {
				double e = ((EntrySaada) si).getError_maj_csa();
				if (e == SaadaConstant.DOUBLE) {
					retour.add(DefaultFormats.getString(e));
				} else {
					retour.add("&plusmn;" + DefaultFormats.getString(3600 * e));
				}
				
			} else if ("Preview".equals(speField)) {
				retour.add(DefaultPreviews.getFlatfilePreview(oidsaada, 64));
				
			} else if ( speField.startsWith("Range") ) {
				retour.add(DefaultFormats.getString(((SpectrumSaada)si).x_min_csa) + " - " + DefaultFormats.getString(((SpectrumSaada)si).x_max_csa) );
			}
		}

		for (String natcol : columns_natcol) {
			si.loadBusinessAttribute();
			String res = DefaultFormats.getString(si.getFieldValue(natcol));
			retour.add(res);
		}
		
		for (AttributeHandler ah : columns_ucds) {
			si.loadBusinessAttribute();
			retour.add(DefaultFormats.getString(resultSet.getObject(rank, ChangeKey.getUCDNickName(ah.getUcd())))
					+ " <a title=\"Native Attribute: " + DefaultFormats.getString(si.getFieldDescByUCD(ah.getUcd())) + "\" href='javascript:void(0);'>(na)</a>");
			}
		
		for (String rel : columns_rel) {
			rel = rel.substring(6);
			int nbcounter = si.getCounterparts(rel).length;
			switch (nbcounter) {
			case 0 : 
				retour.add("<span>No link</span>");
				break;
			case 1 :
				
				long counterpart = (si.getCounterparts(rel))[0];
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
					//retour.add("<span>"+ instance.getNameSaada() + " " + (DefaultPreviews.getDetailLink(counterpart, rel)) + "</span>");
					retour.add("<span>" + nbcounter + " links</span> " + DefaultPreviews.getDetailLink(oidsaada, rel));
				}
				break;
			default :
				retour.add("<span>" + nbcounter + " links</span> " + DefaultPreviews.getDetailLink(oidsaada, rel));
				//retour.add("<span>" + nbcounter + " links</span>");
				break;
			}
		}
		
		return retour;
	}

	@SuppressWarnings("unchecked")
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

	public JSONArray getCollectionKWTable() throws Exception {
		List<String> retour = new ArrayList<String>();
		SaadaInstance si = Database.getCache().getObject(oidsaada);
		for (String natcol : columns_natcol) {
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
				return SaadaOID.getCategoryName(oidsaada) + " "
						+ Database.getCache().getObject(oidsaada) .getNameSaada();
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
	
	public String getJSONString() {
		return null;
	}
}
