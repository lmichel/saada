package ajaxservlet.formator;

import saadadb.collection.Category;
import saadadb.collection.obscoremin.EntrySaada;
import saadadb.collection.obscoremin.ImageSaada;
import saadadb.collection.obscoremin.SaadaInstance;
import saadadb.collection.obscoremin.SpectrumSaada;
import saadadb.collection.obscoremin.TableSaada;
import saadadb.collection.obscoremin.WCSSaada;
import saadadb.util.Messenger;

/**
 * @author laurentmichel
 * * @version $Id: NativeAttributesProvider.java 555 2013-05-25 17:18:55Z laurent.mistahl $

 */
public class NativeAttributesProvider {
	private SaadaInstance saadai;
	private int cat;
	
	public NativeAttributesProvider(SaadaInstance si) {
		saadai = si;
		cat = si.getCategory();
	}
	
	public String getNativeAttr(String attr_name) {
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
		} else if (attr_name.compareTo("s_ra") == 0) {
			return DefaultFormats.getString(saadai.s_ra);
		
		} else if (attr_name.compareTo("healpix_csa") == 0) {
			return DefaultFormats.getString(saadai.healpix_csa);
		
		} else if (attr_name.compareTo("error_maj_csa") == 0) {
			return DefaultFormats.getString(saadai.error_maj_csa);
		
		} else if (attr_name.compareTo("error_min_csa") == 0) {
			return DefaultFormats.getString(saadai.error_min_csa);
		
		} else if (attr_name.compareTo("error_angle_csa") == 0) {
			return DefaultFormats.getString(saadai.error_angle_csa);
		}
		
		return null;
	}
	
	public String getSINative(String attr_name) {
		if (attr_name.compareTo("oidsaada") == 0) {
			return DefaultFormats.getString(saadai.oidsaada);
				
		} else if (attr_name.compareTo("contentsignature") == 0) {
			return DefaultFormats.getString(saadai.contentsignature);
		
		} else if (attr_name.compareTo("namesaada") == 0) {
			return DefaultFormats.getString(saadai.obs_id);
		
		} else if (attr_name.compareTo("date_load") == 0) {
			return DefaultFormats.getString(saadai.getDate_load());
		
		} else if (attr_name.compareTo("access_right") == 0) {
			return DefaultFormats.getString(saadai.access_right);
		}
		
		return null;
	}
	
	public String getWCSNative(String attr_name) {
		if (attr_name.compareTo("crpix1_csa") == 0) {
			return DefaultFormats.getString(((WCSSaada)saadai).crpix1_csa);
			
		} else if (attr_name.compareTo("crpix2_csa") == 0) {
			return DefaultFormats.getString(((WCSSaada)saadai).crpix2_csa);
		
		} else if (attr_name.compareTo("ctype1_csa") == 0) {
			return DefaultFormats.getString(((WCSSaada)saadai).ctype1_csa);
		
		} else if (attr_name.compareTo("ctype2_csa") == 0) {
			return DefaultFormats.getString(((WCSSaada)saadai).ctype2_csa);
		
		} else if (attr_name.compareTo("cd1_1_csa") == 0) {
			return DefaultFormats.getString(((WCSSaada)saadai).cd1_1_csa);
		
		} else if (attr_name.compareTo("cd1_2_csa") == 0) {
			return DefaultFormats.getString(((WCSSaada)saadai).cd1_2_csa);
		
		} else if (attr_name.compareTo("cd2_1_csa") == 0) {
			return DefaultFormats.getString(((WCSSaada)saadai).cd2_1_csa);
		
		} else if (attr_name.compareTo("cd2_2_csa") == 0) {
			return DefaultFormats.getString(((WCSSaada)saadai).cd2_2_csa);
		
		} else if (attr_name.compareTo("crota_csa") == 0) {
			return DefaultFormats.getString(((WCSSaada)saadai).crota_csa);
		
		} else if (attr_name.compareTo("crval1_csa") == 0) {
			return DefaultFormats.getString(((WCSSaada)saadai).crval1_csa);
		
		} else if (attr_name.compareTo("crval2_csa") == 0) {
			return DefaultFormats.getString(((WCSSaada)saadai).crval2_csa);
		
		} else if (attr_name.compareTo("product_url_csa") == 0) {
			return DefaultFormats.getString(((WCSSaada)saadai).getAccess_url());
		}
		
		return null;
	}
	
	public String getEntryNative(String attr_name) {
		String result = this.getPosNative(attr_name);
		if (result != null) return result;
		if (attr_name.compareTo("oidtable") == 0) {
			return DefaultFormats.getString(((EntrySaada)saadai).oidtable);
		}
		return null;
	}
	
	public String getFlatfileNative(String attr_name) {
		String result = this.getSINative(attr_name);
		if (result != null) return result;
		if (attr_name.compareTo("access_url") == 0) {
			return DefaultFormats.getString((saadai).getAccess_url());
		}
		return null;
	}
	
	public String getImageNative(String attr_name) {
		String result = this.getWCSNative(attr_name);
		if (result != null) return result;
		if (attr_name.compareTo("s_fov") == 0) {
			return DefaultFormats.getString(((ImageSaada)saadai).s_fov);
		} else if (attr_name.compareTo("size_delta_csa") == 0) {
			return DefaultFormats.getString(((ImageSaada)saadai).s_fov);
		} else if (attr_name.compareTo("naxis1") == 0) {
			return DefaultFormats.getString(((ImageSaada)saadai).naxis1);
		}
		return null;
	}
	
	public String getMiscNative(String attr_name) {
		String result = this.getSINative(attr_name);
		if (result != null) return result;
		if (attr_name.compareTo("access_url") == 0) {
			return DefaultFormats.getString(saadai.getAccess_url());
		}
		return null;
	}
	
	public String getSpectrumNative(String attr_name) {
		String result = this.getWCSNative(attr_name);
		if (result != null) return result;
		if (attr_name.compareTo("e_min") == 0) {
			return DefaultFormats.getString(((SpectrumSaada)saadai).e_min);
		
		} else if (attr_name.compareTo("e_max") == 0) {
			return DefaultFormats.getString(((SpectrumSaada)saadai).e_max);
		
		} else if (attr_name.compareTo("x_type_csa") == 0) {
			return DefaultFormats.getString(((SpectrumSaada)saadai).x_type_csa);
		
		} else if (attr_name.compareTo("x_unit_csa") == 0) {
			return DefaultFormats.getString(((SpectrumSaada)saadai).x_unit_csa);
		
		} else if (attr_name.compareTo("x_naxis_csa") == 0) {
			return DefaultFormats.getString(((SpectrumSaada)saadai).x_naxis_csa);
		
		} else if (attr_name.compareTo("x_colname_csa") == 0) {
			return DefaultFormats.getString(((SpectrumSaada)saadai).x_colname_csa);
		
		} else if (attr_name.compareTo("x_max_org_csa") == 0) {
			return DefaultFormats.getString(((SpectrumSaada)saadai).x_max_org_csa);
		
		} else if (attr_name.compareTo("x_unit_org_csa") == 0) {
			return DefaultFormats.getString(((SpectrumSaada)saadai).x_unit_org_csa);
		}
		return null;
	}
	
	public String getTableNative(String attr_name) {
		String result = this.getSINative(attr_name);
		if (result != null) return result;
		if (attr_name.compareTo("product_url_csa") == 0) {
			return DefaultFormats.getString(saadai.getAccess_url());
		} else if (attr_name.compareTo("nb_rows_csa") == 0) {
			return DefaultFormats.getString(((TableSaada)saadai).nb_rows_csa);
		}
		return null;
	}
}
