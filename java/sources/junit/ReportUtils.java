package junit;

/**
 * @author michel
 *
 * utilities for processing test report for Vizier 
 */
public class ReportUtils {

	/**
	 * Regexp matching report line that can change from a run to another. 
	 */
	public static final String[] excludedLines = {
			"     Saada.*",
            " Processed .*",
            "           date_load .*",
            "          access_url .*",
            "            oidsaada .*",
            "    contentsignature .*"
	};
	
	/**
	 * @param reportContent: report file content
	 * @return filtered file content
	 */
	public static String filterReportContent(String reportContent){
		String retour = reportContent;
		for( String el: excludedLines){
			retour = retour.replaceAll(el, "@@@@@@@@@@@@@@@@@@@@");
		}
		return retour.trim();
	}
	
	/**
	 * @param filename
	 * @param dirname
	 * @return the actual data file location
	 */
	public static String getFilename(String dirname, String filename){
		if( dirname.startsWith("/")){
			return  (dirname + "/" + filename).replaceAll("\\/\\/", "/");
		} else {
			System.err.println("Onmly absolute paths are supported for the moment");
			return null;
		}
	}
	/**
	 * @param filename
	 * @param dirname
	 * @return the actual report file location
	 */
	public static String getReportFilename(String dirname, String filename){
		if( dirname.startsWith("/")){
			return  (dirname + "/report/" + filename + ".txt").replaceAll("\\/\\/", "/");
		} else {
			System.err.println("Onmly absolute paths are supported for the moment");
			return null;
		}
	}

}
