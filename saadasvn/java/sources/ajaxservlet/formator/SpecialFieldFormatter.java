package ajaxservlet.formator;

import saadadb.collection.Category;
import saadadb.collection.ImageSaada;
import saadadb.collection.Position;
import saadadb.collection.SaadaInstance;
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
				return DefaultPreviews.getDLLink(saadai.getOid(),dlWithRelations);
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
			String pos = DefaultFormats.getHMSCoord(((Position) saadai).getPos_ra_csa(), ((Position) saadai).getPos_dec_csa());
			return ("<span>" + pos + " <a title=\"Open Simbad Tooltip\" onclick='resultPaneView.overPosition(\""+pos+"\");'>(s)</a></span>");
			//return ("<span>" + pos + "</span>");
		}
		return null;
	}

	public String getPosWithError() {
		if ((cat == Category.ENTRY) || (cat == Category.IMAGE) || (cat == Category.SPECTRUM)) {
			String pos = DefaultFormats.getHMSCoord(((Position) saadai).getPos_ra_csa(), ((Position) saadai).getPos_dec_csa());
			return ("<span>" + pos + " <a title=\"Open Simbad Tooltip\"  onclick='resultPaneView.overPosition(\""+pos+"\");'>(s)</a> [&#177; "+ DefaultFormats.getString(((Position) saadai).getError_maj_csa()) +"]</span>");
			//return ("<span>" + pos + "[&#177; "+ DefaultFormats.getString(((Position) saadai).getError_maj_csa()) +"]</span>");
		}
		return null;
	}

	public String getSize() {
		if (cat == Category.IMAGE) {
			return (DefaultFormats.getString(((ImageSaada) saadai).size_alpha_csa) + " x " + DefaultFormats.getString(((ImageSaada) saadai).size_delta_csa));
		}
		return null;
	}




	public String getSimbad() {
		if ((cat == Category.ENTRY) || (cat == Category.IMAGE) || (cat == Category.SPECTRUM)) {
			return ("<a class=simbad onclick='resultPaneView.fireShowSimbad(\"" + DefaultFormats.getHMSCoord(((Position)saadai).getPos_ra_csa(), ((Position)saadai).getPos_dec_csa()) + "\");'></a>");
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
		long oid = saadai.getOid();
		switch( cat ) {
		case Category.SPECTRUM:
			return DefaultPreviews.getDetailLink(oid, "ClassLevel")
			+ DefaultPreviews.getInfoLink(oid)
			+ DefaultPreviews.getAladinLiteLink(oid)
			+ DefaultPreviews.getDLLink(oid, dlWithRelations)
			+ DefaultPreviews.getCartLink(oid)
			+ DefaultPreviews.getSpecSAMP(oid);
		case Category.FLATFILE:
			return  DefaultPreviews.getInfoLink(oid)
			+ DefaultPreviews.getDLLink(oid, dlWithRelations)
			+ DefaultPreviews.getCartLink(oid);
		case Category.TABLE:
			return DefaultPreviews.getDetailLink(oid, "ClassLevel")
			+ DefaultPreviews.getSourcesLink(oid)
			+ DefaultPreviews.getInfoLink(oid)
			+ DefaultPreviews.getDLLink(oid, dlWithRelations)
			+ DefaultPreviews.getCartLink(oid)
			+ DefaultPreviews.getTopcatSAMP(oid);
		case Category.ENTRY:
			return DefaultPreviews.getDetailLink(oid, "ClassLevel")
			+ DefaultPreviews.getHeaderLink(oid)
			+ DefaultPreviews.getAladinLiteLink(oid);
		default: 			
			return DefaultPreviews.getDetailLink(oid, "ClassLevel")
			+ DefaultPreviews.getInfoLink(oid)
			+ DefaultPreviews.getDLLink(oid, dlWithRelations)
			+ DefaultPreviews.getCartLink(oid)
			+  DefaultPreviews.getAladinSAMP(oid);

		}
	}
	public String getAccessForDetail() throws SaadaException {
		long oid = saadai.getOid();
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
