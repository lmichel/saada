package ajaxservlet.formator;

import java.io.File;

import saadadb.collection.FlatfileSaada;
import saadadb.collection.SaadaInstance;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;

abstract public class DefaultPreviews {
	public static final String[] disp_formats = new String[]{"gif", "jpeg", "jpg", "png", "tiff", "bmp", "GIF", "JPEG", "JPG", "PNG", "TIFF", "BMP"};

	/**
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
		return "<a class=HDU href='javascript:void(0)' onclick='resultPaneView.fireShowRecord(\"" + oid + "\");'></A>";
	}
	public static final String getDetailLink(long oid) throws SaadaException {
		return "<a class=detail href='javascript:void(0)' onclick='resultPaneView.fireShowRecord(\"" + oid + "\");'></A>";
	}
	public static final String getSourcesLink(long oid) throws SaadaException {
		return "<a class=sources href='javascript:void(0)' onclick='resultPaneView.fireShowSources(\"" + oid + "\");'></A>";
	}
	public static final String getDLLink(long oid) throws SaadaException {
		SaadaInstance si =  Database.getCache().getObject(oid);
		File f = new File(si.getRepositoryPath());
		if( !f.exists()) {
			return "Not Found";
		}
		else {
			long size = f.length();
			String unit = "b";
			if( size > 1000000000) {
				size /= 1000000000;
				unit = "Gb";				
			}
			else if( size > 1000000) {
				size /= 1000000;
				unit = "Mb";				
			}
			else if( size > 1000) {
				size /= 1000;
				unit = "Kb";				
			}
			return "<a target=blank class=download href='" + si.getDownloadURL(true) + "'></A><br><span>" + size + unit + "</span>";
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
					+ "<IMG class=vignette  SRC='"  + vproduct + "'"  + " HEIGHT=" + size  +" ALIGN=left></A>";						
				}
			}
			/*
			 * Look for a file with the same name but enable to  be displayed by the browser
			 */
			for(String app: disp_formats) {
				if( (new File(sp + "." + app)).exists() ) {							
					String vproduct = "getproduct?" 
						+ "oid=" + oid 
						+ "&ext=" + app;
					return "<A border=0 TITLE='Show real size' href='javascript:void(0);' onclick='resultPaneView.fireShowPreview(\"" 
					+ vproduct + "\", \"" 
					+ fi.getNameSaada()+ "\");'>" 
					+ "<IMG class=vignette  SRC='"  + vproduct + "'"  + " HEIGHT=" + size  +" ALIGN=left></A>";	
				}
			}
		} catch (SaadaException e) {}
		return "no preview";
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
