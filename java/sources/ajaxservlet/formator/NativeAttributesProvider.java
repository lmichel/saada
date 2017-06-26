package ajaxservlet.formator;

import saadadb.collection.Category;
import saadadb.collection.obscoremin.SaadaInstance;
import saadadb.collection.obscoremin.SpectrumSaada;
import saadadb.collection.obscoremin.TableSaada;
import saadadb.collection.obscoremin.WCSSaada;
import saadadb.exceptions.FatalException;
import saadadb.util.Messenger;

/**
 * @author laurentmichel
 * * @version $Id$

 */
public class NativeAttributesProvider {
	private SaadaInstance saadai;
	private int cat;
	
	public NativeAttributesProvider(SaadaInstance si) {
		saadai = si;
		cat = si.getCategory();
	}
	
	public String getNativeAttr(String attr_name) throws FatalException {
		switch (cat) {
		case Category.ENTRY:
			return getEntryNative(attr_name);
		case Category.FLATFILE:
			return getFlatfileNative(attr_name);
		case Category.IMAGE:
			return getImageNative(attr_name);
		case Category.MISC:
			return getMiscNative(attr_name);
		case Category.SPECTRUM:
			return getSpectrumNative(attr_name);
		case Category.TABLE:
			return getTableNative(attr_name);
		default:
			Messenger.printMsg(Messenger.TRACE, "Couldn't find category.");
		}
		return null;
	}
	
	public String getPosNative(String attr_name) {
		if (attr_name.compareTo("s_ra") == 0) {
			return DefaultFormats.getString(saadai.s_ra);
		
		} else if (attr_name.compareTo("sky_pixel_csa") == 0) {
			return DefaultFormats.getString(saadai.healpix_csa);
		
		} else if (attr_name.compareTo("error_maj_csa") == 0) {
			return DefaultFormats.getString(saadai.s_resolution);
		
		} else if (attr_name.compareTo("error_min_csa") == 0) {
			return DefaultFormats.getString(saadai.s_resolution);
		
		}
		
		return null;
	}
	
	public String getSINative(String attr_name) {
		if (attr_name.compareTo("oidsaada") == 0) {
			return DefaultFormats.getString(saadai.oidsaada);
		} else if (attr_name.compareTo("contentsignature") == 0) {
			return DefaultFormats.getString(saadai.contentsignature);
		
		} else if (attr_name.compareTo("obs_id") == 0) {
			return DefaultFormats.getString(saadai.obs_id);
		
		} else if (attr_name.compareTo("date_load") == 0) {
			return DefaultFormats.getString(saadai.getDate_load());
		
		} else if (attr_name.compareTo("access_right") == 0) {
			return DefaultFormats.getString(saadai.access_right);
		}
		
		return null;
	}
	
	public String getWCSNative(String attr_name) throws FatalException {
		WCSSaada wcsInstance = (WCSSaada)saadai;
		if (attr_name.compareTo("crpix1_csa") == 0) {
			return DefaultFormats.getString(wcsInstance.crpix1_csa);
			
		} else if (attr_name.compareTo("crpix2_csa") == 0) {
			return DefaultFormats.getString(wcsInstance.crpix2_csa);
		
		} else if (attr_name.compareTo("ctype1_csa") == 0) {
			return DefaultFormats.getString(wcsInstance.ctype1_csa);
		
		} else if (attr_name.compareTo("ctype2_csa") == 0) {
			return DefaultFormats.getString(wcsInstance.ctype2_csa);
		
		} else if (attr_name.compareTo("cd1_1_csa") == 0) {
			return DefaultFormats.getString(wcsInstance.cd1_1_csa);
		
		} else if (attr_name.compareTo("cd1_2_csa") == 0) {
			return DefaultFormats.getString(wcsInstance.cd1_2_csa);
		
		} else if (attr_name.compareTo("cd2_1_csa") == 0) {
			return DefaultFormats.getString(wcsInstance.cd2_1_csa);
		
		} else if (attr_name.compareTo("cd2_2_csa") == 0) {
			return DefaultFormats.getString(wcsInstance.cd2_2_csa);
		
		} else if (attr_name.compareTo("crota_csa") == 0) {
			return DefaultFormats.getString(wcsInstance.crota_csa);
		
		} else if (attr_name.compareTo("crval1_csa") == 0) {
			return DefaultFormats.getString(wcsInstance.crval1_csa);
		
		} else if (attr_name.compareTo("crval2_csa") == 0) {
			return DefaultFormats.getString(wcsInstance.crval2_csa);
		
		} else if (attr_name.compareTo("product_url_csa") == 0) {
			return DefaultFormats.getString(wcsInstance.getDownloadURL(true));
		}
		
		return null;
	}
	
	public String getEntryNative(String attr_name) {
		String result = this.getPosNative(attr_name);
		if (result != null) return result;
		if (attr_name.compareTo("oidtable") == 0) {
			return DefaultFormats.getString(saadai.getOIdtable());
		}
		return null;
	}
	
	public String getFlatfileNative(String attr_name) throws FatalException {
		String result = this.getSINative(attr_name);
		if (result != null) return result;
		if (attr_name.compareTo("product_url_csa") == 0) {
			return DefaultFormats.getString((saadai).getDownloadURL(true));
		}
		return null;
	}
	
	public String getImageNative(String attr_name) throws FatalException {
		String result = this.getWCSNative(attr_name);
		if (result != null) return result;
		if (attr_name.compareTo("size_alpha_csa") == 0) {
			return DefaultFormats.getString(saadai.getS_fov());
		} else if (attr_name.compareTo("size_delta_csa") == 0) {
			return DefaultFormats.getString(saadai.getS_fov());
		} 
		return null;
	}
	
	public String getMiscNative(String attr_name) throws FatalException {
		String result = this.getSINative(attr_name);
		if (result != null) return result;
		if (attr_name.compareTo("product_url_csa") == 0) {
			return DefaultFormats.getString(saadai.getDownloadURL(true));
		}
		return null;
	}
	
	public String getSpectrumNative(String attr_name) throws FatalException {
		SpectrumSaada spectrumInstance = (SpectrumSaada)saadai;
		String result = this.getWCSNative(attr_name);
		if (result != null) return result;
		if (attr_name.compareTo("x_min_csa") == 0) {
			return DefaultFormats.getString(spectrumInstance.em_min);
		
		} else if (attr_name.compareTo("x_max_csa") == 0) {
			return DefaultFormats.getString(spectrumInstance.em_max);
		
		}
		
		return null;
	}
	
	public String getTableNative(String attr_name) throws FatalException {
		TableSaada tableInstance = (TableSaada)saadai;

		String result = this.getSINative(attr_name);
		if (result != null) return result;
		if (attr_name.compareTo("product_url_csa") == 0) {
			return DefaultFormats.getString(((TableSaada)saadai).getDownloadURL(true));
		} else if (attr_name.compareTo("nb_rows_csa") == 0) {
			return DefaultFormats.getString(((TableSaada)saadai).nb_rows_csa);
		}
		return null;
	}
}
