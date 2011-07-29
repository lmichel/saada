package ajaxservlet.formator;

import saadadb.collection.*;
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
	private SaadaInstance saadai;
	private int cat;
	
	public SpecialFieldFormatter (SaadaInstance si) {
		saadai = si;
		cat = si.getCategory();
	}
	
	public String getDLLink() {
		try {
			if (cat != Category.ENTRY) {
				return DefaultPreviews.getDLLink(saadai.getOid());
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
			return ("<span>" + pos + " <a title=\"Open Simbad Tooltip\" href='javascript:void(0);' onclick='resultPaneView.overPosition(\""+pos+"\");'>(s)</a></span>");
		}
		return null;
	}
	
	public String getPosWithError() {
		if ((cat == Category.ENTRY) || (cat == Category.IMAGE) || (cat == Category.SPECTRUM)) {
			String pos = DefaultFormats.getHMSCoord(((Position) saadai).getPos_ra_csa(), ((Position) saadai).getPos_dec_csa());
			return ("<span>" + pos + " <a title=\"Open Simbad Tooltip\" href='javascript:void(0);' onclick='resultPaneView.overPosition(\""+pos+"\");'>(s)</a> [&#177; "+ DefaultFormats.getString(((Position) saadai).getError_maj_csa()) +"]</span>");
	}
	return null;
}
	
	public String getSize() {
		if (cat == Category.IMAGE) {
			return (DefaultFormats.getString(((ImageSaada) saadai).size_alpha_csa) + " x " + DefaultFormats.getString(((ImageSaada) saadai).size_delta_csa));
		}
		return null;
	}
	
	public String getAladinSAMP(){
		if ((cat == Category.ENTRY) || (cat == Category.IMAGE) || (cat == Category.SPECTRUM)) {
			return ("<a href='javascript:void(0);' class=aladinsmall onclick='sampView.fireSendImage(\"" + saadai.getOid() + "\");'></a>");
		} else {
			return null;
		}
	}
	
	public String getSpecSAMP() {
		if (cat == Category.SPECTRUM) {
			return ("<a href='javascript:void(0);' class=vospecsmall onclick='sampView.fireSendSpectra(\"" + saadai.getOid() + "\");'></a>");
		}
		return null;
	}
	
	public String getTopcatSAMP() {
		if (cat != Category.FLATFILE) {
			// TODO add method
			return ("-");
		}
		return null;
	}
	
	public String getSimbad() {
		if ((cat == Category.ENTRY) || (cat == Category.IMAGE) || (cat == Category.SPECTRUM)) {
			return ("<a href='javascript:void(0);' class=simbad onclick='resultPaneView.fireShowSimbad(\"" + DefaultFormats.getHMSCoord(((Position)saadai).getPos_ra_csa(), ((Position)saadai).getPos_dec_csa()) + "\");'></a>");
		}
		return null;
	}
	
	public String getVizier() {
		if ((cat == Category.ENTRY) || (cat == Category.IMAGE) || (cat == Category.SPECTRUM)) {
			// TODO add method
		}
		return null;
	}

}
