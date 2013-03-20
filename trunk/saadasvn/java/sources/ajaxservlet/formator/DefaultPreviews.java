package ajaxservlet.formator;

import java.io.File;

import ajaxservlet.SaadaServlet;

import saadadb.collection.EntrySaada;
import saadadb.collection.FlatfileSaada;
import saadadb.collection.Position;
import saadadb.collection.SaadaInstance;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;

abstract public class DefaultPreviews {
	public static final String[] disp_formats = new String[]{"gif", "jpeg", "jpg", "png", "tiff", "bmp", "GIF", "JPEG", "JPG", "PNG", "TIFF", "BMP"};

	/** * @version $Id$

	 * @param oid
	 * @param size
	 * @return
	 * @throws FatalException
	 */
	public static final String getImageVignette(long oid, int size) throws FatalException {
		String url =  "getvignette?oid=" + oid;
		return "<A border=0 TITLE='Show real size' href='javascript:void(0);' onclick='resultPaneView.fireShowVignette(\"" 
		+ oid + "\", \"" 
		+ Database.getCache().getObject(oid).getNameSaada()+ "\");'>" 
		+ "<IMG class=vignette  SRC='"  + url + "'"  + " HEIGHT=" + size  +" ALIGN=left></A>";	
	}

	/**
	 * Alll links pointing on something else that am AJAX call must be open in a new windows
	 * @param oid
	 * @return
	 * @throws SaadaException
	 */
	public static final String getHeaderLink(long oid) throws SaadaException {
		EntrySaada si =  (EntrySaada) Database.getCache().getObject(oid);
		return "<a title='Get the table header' class=dl_header href='javascript:void(0)' onclick='resultPaneView.fireShowRecord(\"" + si.getOidtable() + "\");'></A>";
	}
	public static final String  getDetailLink(long oid, String panelToOpen) throws SaadaException {
		String panarg = (panelToOpen == null )? "null": "\"" + panelToOpen + "\"";
		return "<a title='Get the detail of that data' class=dl_detail href='javascript:void(0)' onclick='resultPaneView.fireShowRecord(\"" + oid + "\", " + panarg + ");'></A>";
	}
	public static final String getSourcesLink(long oid) throws SaadaException {
		return "<a title='Get all catalogue sources' class=dl_sources href='javascript:void(0)' onclick='resultPaneView.fireShowSources(\"" + oid + "\");'></A>";
	}
	public static final String getInfoLink(long oid) throws SaadaException {
		SaadaInstance si =  Database.getCache().getObject(oid);
		if( SaadaServlet.secureDownlad ) {
			return "<a title='Get info about the product file' class=dl_info href='javascript:void(0)' onclick='resultPaneView.fireGetProductInfo(\"" + si.getDownloadURL(true) + "\");'></A>";
		} else {
			return "<a title='Get info about the product file' class=dl_info href='javascript:void(0)' onclick='resultPaneView.fireGetProductInfo(\"" + si.getDownloadURL(true) + "\");'></A>";
		}
	}
	public static final String getCartLink(long oid) throws SaadaException {
		SaadaInstance si =  Database.getCache().getObject(oid);
		if( SaadaServlet.secureDownlad ) {
		    return "<a class=dl_securecart title=\"Add the product file to the  cart\" href=\"#\" onclick='cartView.fireAddUrl($(this),\"" + si.getNameSaada() + "\", \"" + oid + "\");'></A>";
		} else {
			return "<a class=dl_cart title=\"Add the product file to the  cart\" href=\"#\" onclick='cartView.fireAddUrl($(this),\"" + si.getNameSaada() + "\", \"" + oid + "\");'></A>";
		}
	}
	public static final String getDLLink(long oid) throws SaadaException {
		SaadaInstance si =  Database.getCache().getObject(oid);
		if( SaadaServlet.secureDownlad ) {
			return "<a title='Download the product file (restricted)' target='_blank' class=dl_securedownload  href='javascript:void(0);' onclick='window.open(\"" + si.getDownloadURL(true) + "\", \"preview\");'></A>";

		} else {
			return "<a title='Download the product file' target='_blank' class=dl_download  href='javascript:void(0);' onclick='Modalinfo.iframePanel(\"" + si.getDownloadURL(true) + "\", \"preview\");'></A>";
		}
	}

	/**
	 * @param oid
	 * @param size
	 * @return
	 * @throws FatalException
	 */
	public static final String getFlatfilePreview(long oid, int size) throws FatalException {
		FlatfileSaada fi = (FlatfileSaada) Database.getCache().getObject(oid);
		String sp;
		try {
			/*
			 * Check first if the flatfile can be displayed by the browser
			 */
			sp = fi.getRepositoryPath();
			for(String app: disp_formats) {
				if( sp.endsWith("." + app)) {
					String vproduct = "getproduct?" 
						+ "oid=" + oid ;
					return "<A border=0 TITLE='Show real size' href='javascript:void(0);' onclick='resultPaneView.fireShowPreview(\"" 
					+ vproduct + "\", \"" 
					+ fi.getNameSaada()+ "\");'>" 
					+ "<IMG class=vignette  SRC='"  + vproduct + "'"  + " HEIGHT=" + size  +" ALIGN=top></A>";						
				}
			}
			/*
			 * Look for a file with the same name but enable to  be displayed by the browser
			 */
			String vproduct = "No preview";
			/*
			 * Filename is appended to the URLto help browsers to use their own viewer instead
			 * of downloading or waiting the beginning of the transfer (FTTP header reception) to 
			 * propose what to do with the file
			 */
			String extParam = "&filename=" + sp.substring(sp.lastIndexOf(File.separator) + 1);
			for(String app: disp_formats) {
				if( (new File(sp + "." + app)).exists() ) {							
					vproduct = "<IMG class=vignette  SRC='"  +"getproduct?" 
					+ "oid=" + oid 
					+ "&ext=" + app + "'"  + " HEIGHT=" + size  +" ALIGN=top>";
					break;
				}
			}
			return "<A border=0 target=blank TITLE='Show real size' href='javascript:void(0);' onclick='Modalinfo.iframePanel(\"getproduct?oid=" 
			+ oid + extParam + "\");'>" 
			+ vproduct + "</A>";	

		} catch (SaadaException e) {}
		return "no preview";
	}

	public static String getSpecSAMP(long oid) {
		return ("<a  title='Send a spectra to SAMP' href='javascript:void(0);' class=dl_samp onclick='webSampView.fireSendOid(\"" 
				+ oid + "\");'></a>");
	}

	public static String getTopcatSAMP(long oid) throws FatalException {
		if( SaadaServlet.secureDownlad ) {
			return ("<a  title='Send a VOTable to SAMP (restricted)' href='javascript:void(0);' class=dl_securesamp onclick='sampView.fireSendOid(\"" 
					+  oid + "\");'></a>");
		} else {
			return ("<a  title='Send a VOTable to SAMP' href='javascript:void(0);' class=dl_samp onclick='webSampView.fireSendOid(\"" 
					+  oid + "\");'></a>");
		}
	}

	public static String getAladinSAMP(long oid){
		return ("<a title='Send an image to SAMP' href='javascript:void(0);' class=dl_samp onclick='WebSamp_mVc.fireSendOid(\"" 
				+ oid + "\");'></a>");
	}
	
	public static String getSkyAtSAMP(long oid) throws FatalException{
		Position si =  (Position)(Database.getCache().getObject(oid));
		return ("<a title='Send a position to SAMP' href='javascript:void(0);' class=dl_samp onclick='WebSamp_mVc.fireSendSkyat(" 
				+ si.getPos_ra_csa() + ", " + si.getPos_dec_csa() + ");'></a>");
	}

	/**
	 * @param e
	 * @return
	 */
	public static final String getErrorDiv(Exception e) {
		return  "<span class=\"page_message\"><B>Internal Error: </B>"
		+ e 
		+ "</div>\n";
	}
	public static final String getErrorDiv(String msg) {
		return  "<span class=\"page_message\"><B>Internal Error: </B>"
		+ msg 
		+ "</div>\n";
	}

}
