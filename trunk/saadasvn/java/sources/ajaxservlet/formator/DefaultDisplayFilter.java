package ajaxservlet.formator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.json.simple.JSONArray;

import com.mysql.jdbc.DatabaseMetaData;

import saadadb.collection.Category;
import saadadb.collection.SaadaInstance;
import saadadb.collection.SaadaOID;
import saadadb.collection.SpectrumSaada;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.meta.AttributeHandler;
import saadadb.meta.MetaClass;
import saadadb.meta.MetaCollection;
import saadadb.query.result.OidsaadaResultSet;
import saadadb.util.SaadaConstant;

/**
 * @author laurentmichel
 * * @version $Id$

 */
abstract public class DefaultDisplayFilter implements DisplayFilter,
		Serializable {
	private static final long serialVersionUID = 1L;
	protected final LinkedHashSet<String> datatable_columns = new LinkedHashSet<String>();
	protected long oidsaada = SaadaConstant.LONG;
	protected static final Set<String> ignored_keywords = new LinkedHashSet<String>();
	protected final Set<AttributeHandler> ucd_columns = new LinkedHashSet<AttributeHandler>();
	protected OidsaadaResultSet resultSet;
	protected final LinkedHashSet<String> columns_rel = new LinkedHashSet<String>();

	protected MetaClass mc = null;
	protected MetaCollection metacoll;

	/**
	 * 
	 * @param coll
	 * @throws FatalException
	 */
	DefaultDisplayFilter(String coll) throws FatalException {
		ignored_keywords.add("oidproduct");
		ignored_keywords.add("contentsignature");
		ignored_keywords.add("access_right");
		ignored_keywords.add("loaded");
		ignored_keywords.add("group_oid_csa");
		ignored_keywords.add("nb_rows_csa");
		ignored_keywords.add("pos_x_csa");
		ignored_keywords.add("pos_y_csa");
		ignored_keywords.add("pos_z_csa");
		ignored_keywords.add("md5keysaada");
		ignored_keywords.add("Any-Collection");
		ignored_keywords.add("Any-Relation");
		if (coll != null) {
			metacoll = Database.getCachemeta().getCollection(coll);
			this.setRelations();
		}
	}

	/* (non-Javadoc)
	 * @see ajaxservlet.formator.DisplayFilter#setOId(long)
	 */
	public void setOId(long oidsaada) throws FatalException {
		this.oidsaada = oidsaada;
		if (SaadaOID.getCategoryNum(oidsaada) != Category.FLATFILE) {
			mc = Database.getCachemeta().getClass(
					SaadaOID.getClassNum(oidsaada));
		}
	}

	/* (non-Javadoc)
	 * @see ajaxservlet.formator.DisplayFilter#setMetaClass(saadadb.meta.MetaClass)
	 */
	public void setMetaClass(MetaClass mc) throws FatalException {
		this.mc = mc;
	}
	
	/**
	 * abstract method redefined by all the subclasses
	 * that will specialize it with their category
	 */
	protected abstract void setRelations();
	
	/**
	 * add the relations names to columns_rel
	 * depending on the category
	 * 
	 * @param cat
	 */
	protected void setRelations(int cat) {
	if (metacoll != null) {
		String[] relnames;
		relnames = Database.getCachemeta().getRelationNamesStartingFromColl(metacoll.getName(), cat);
		for (String val : relnames) {
			columns_rel.add("Rel : " + val);
		}
	}
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ajaxservlet.formator.DisplayFilter#setResultSet(saadadb.query.result.
	 * OidsaadaResultSet)
	 */
	public void setResultSet(OidsaadaResultSet resultSet) {
		this.resultSet = resultSet;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ajaxservlet.formator.DisplayFilter#addUCDColumn(saadadb.meta.AttributeHandler
	 * )
	 */
	public void addUCDColumn(AttributeHandler ah) {
		if (ah != null)
			this.ucd_columns.add(ah);
	}

	protected void addUCDsToDisplayColumns(LinkedHashSet<String> retour) {
		for (AttributeHandler ah : this.ucd_columns) {
			String lbl = ah.getUcd();
			if (ah.getUnit().length() > 0) {
				lbl += "(" + ah.getUnit() + ")";
			}
			retour.add(lbl);
		}
	}
	
	protected void addRelToDisplayColumns(LinkedHashSet<String> retour) {
		if (columns_rel.size() > 0) {
			for (String val : columns_rel) {
				retour.add(val);
			}
		}
	}

	abstract public Set<String> getDisplayedColumns();

	abstract public Set<AttributeHandler> getQueriableColumns()
			throws FatalException;

	public boolean valid(AttributeHandler ah) {
		if (ignored_keywords.contains(ah.getNameattr())) {
			return false;
		}
		return true;
	}

	abstract public List<String> getRow(Object obj, int rank) throws Exception;

	public void getRel(List<String> result) {

		try {
			SaadaInstance si = Database.getCache().getObject(oidsaada);

			if (columns_rel.size() > 0) {
				for (String rel : columns_rel) {
					rel = rel.substring(6);
					int nbcounter = si.getCounterparts(rel).length;
					switch (nbcounter) {
					case 0:
						result.add("<span>No link</span>");
						break;
					case 1:

						long counterpart = (si.getCounterparts(rel))[0];
						int tmpcat = SaadaOID.getCategoryNum(counterpart);
						switch (tmpcat) {
						case (Category.IMAGE):
							result.add(DefaultPreviews.getImageVignette(
									counterpart, 64));
							break;
						case (Category.FLATFILE):
							result.add(DefaultPreviews.getFlatfilePreview(
									counterpart, 64));
							break;
						default:
							result.add("<span>"+ Database.getCache().getObject(counterpart).getNameSaada()+ " " + (DefaultPreviews.getDetailLink(counterpart))+ "</span>");
						}
						break;
					default:
						result.add("<span>" + nbcounter + " links</span>");
						break;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	abstract public JSONArray getClassKWTable() throws Exception;

	abstract public JSONArray getCollectionKWTable() throws Exception;

	public String getTitle() {
		if (oidsaada != SaadaConstant.LONG) {
			try {
				return SaadaOID.getCategoryName(oidsaada)
						+ " "
						+ Database.getCache().getObject(oidsaada)
								.getNameSaada();
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
	
	abstract public String getJSONString() ;

}
