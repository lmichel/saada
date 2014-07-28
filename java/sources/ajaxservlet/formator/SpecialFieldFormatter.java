package ajaxservlet.formator;

import saadadb.collection.Category;
import saadadb.collection.obscoremin.ImageSaada;
import saadadb.collection.obscoremin.SaadaInstance;
import saadadb.exceptions.SaadaException;

/**
 * class producing the datatable content depending on 
 * the class calling it
 *  * @version $Id$

 * @author Clémentine Frère
 * contact : frere.clementine@gmail.com
 *
 */
public class SpecialFieldFormatter {
	protected SaadaInstance saadai;
	protected int cat;

	public SpecialFieldFormatter (SaadaInstance si) {
		saadai = si;
		cat = si.getCategory();
	}

	public String getDLLink(boolean dlWithRelations) {
		try {
			if (cat != Category.ENTRY) {
				return DefaultPreviews.getDLLink(saadai.oidsaada,dlWithRelations);
			} else {
				return null;
			}
		} catch (SaadaException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String getPos() {
		if ((cat == Category.ENTRY) || (cat == Category.IMAGE) || (cat == Category.SPECTRUM)) {
			String pos = DefaultFormats.getHMSCoord(saadai.s_ra, saadai.s_dec);
			return ("<span>" + pos + " <a title=\"Open Simbad Tooltip\" onclick='resultPaneView.overPosition(\""+pos+"\");'>(s)</a></span>");
		}
		return null;
	}

	public String getPosWithError() {
		if ((cat == Category.ENTRY) || (cat == Category.IMAGE) || (cat == Category.SPECTRUM)) {
			String pos = DefaultFormats.getHMSCoord(saadai.s_ra, saadai.s_dec);
			return ("<span>" + pos + " <a title=\"Open Simbad Tooltip\"  onclick='resultPaneView.overPosition(\""+pos+"\");'>(s)</a> [&#177; "+ DefaultFormats.getString(saadai.s_resolution) +"]</span>");
		}
		return null;
	}

	public String getSize() {
		if (cat == Category.IMAGE) {
			return (DefaultFormats.getString(((ImageSaada) saadai).s_fov) + " x " + DefaultFormats.getString(((ImageSaada) saadai).s_fov));
		}
		return null;
	}




	public String getSimbad() {
		if ((cat == Category.ENTRY) || (cat == Category.IMAGE) || (cat == Category.SPECTRUM)) {
			return ("<a class=simbad onclick='resultPaneView.fireShowSimbad(\"" + DefaultFormats.getHMSCoord(saadai.s_ra, saadai.s_dec) + "\");'></a>");
		}
		return null;
	}

	public String getVizier() {
		if ((cat == Category.ENTRY) || (cat == Category.IMAGE) || (cat == Category.SPECTRUM)) {
			// TODO add method
		}
		return null;
	}

	public String getAccess(boolean dlWithRelations) throws SaadaException {
		long oid = saadai.oidsaada;
		switch( cat ) {
		case Category.SPECTRUM:
			return DefaultPreviews.getDetailLink(oid, null)
			+ DefaultPreviews.getInfoLink(oid)
			+ DefaultPreviews.getDLLink(oid, dlWithRelations)
			+ DefaultPreviews.getCartLink(oid)
			+ DefaultPreviews.getSpecSAMP(oid);
		case Category.FLATFILE:
			return  DefaultPreviews.getInfoLink(oid)
			+ DefaultPreviews.getDLLink(oid, dlWithRelations)
			+ DefaultPreviews.getCartLink(oid);
		case Category.TABLE:
			return DefaultPreviews.getDetailLink(oid, null)
			+ DefaultPreviews.getSourcesLink(oid)
			+ DefaultPreviews.getInfoLink(oid)
			+ DefaultPreviews.getDLLink(oid, dlWithRelations)
			+ DefaultPreviews.getCartLink(oid)
			+ DefaultPreviews.getTopcatSAMP(oid);
		case Category.ENTRY:
			return DefaultPreviews.getDetailLink(oid, null)
			+ DefaultPreviews.getHeaderLink(oid)
			+ DefaultPreviews.getSkyAtSAMP(oid);
		default: 			
			return DefaultPreviews.getDetailLink(oid, null)
			+ DefaultPreviews.getInfoLink(oid)
			+ DefaultPreviews.getDLLink(oid, dlWithRelations)
			+ DefaultPreviews.getCartLink(oid)
			+  DefaultPreviews.getAladinSAMP(oid);

		}
	}
	public String getAccessForDetail() throws SaadaException {
		long oid = saadai.oidsaada;
		switch( cat ) {
		case Category.SPECTRUM:
			return DefaultPreviews.getInfoLink(oid)
			+ DefaultPreviews.getDLLink(oid, false)
			+ DefaultPreviews.getCartLink(oid)
			+ DefaultPreviews.getSpecSAMP(oid);
		case Category.FLATFILE:
			return  DefaultPreviews.getInfoLink(oid)
			+ DefaultPreviews.getDLLink(oid, false)
			+ DefaultPreviews.getCartLink(oid);
		case Category.TABLE:
			return  DefaultPreviews.getInfoLink(oid)
			+ DefaultPreviews.getDLLink(oid, false)
			+ DefaultPreviews.getCartLink(oid)
			+ DefaultPreviews.getTopcatSAMP(oid);
		case Category.ENTRY:
			return DefaultPreviews.getHeaderLink(oid)
			+ DefaultPreviews.getSkyAtSAMP(oid);
		default: 			
			return DefaultPreviews.getInfoLink(oid)
			+ DefaultPreviews.getDLLink(oid, false)
			+ DefaultPreviews.getCartLink(oid)
			+  DefaultPreviews.getAladinSAMP(oid);

		}
	}

}
