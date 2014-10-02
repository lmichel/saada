package saadadb.util;

import java.io.File;
import java.util.Collection;


/**
 * @author michel
 * * @version $Id$

 * 12/2009: ignore empty items in arrays to merge
 * 01/2014: Add merger on cCollections
 */
public class Merger {
	
	/**
	 * @param array
	 * @return
	 */
	public final static String getMergedArray(String[] array) {
		return getMergedArray(", ", array);
	}
	/**
	 * @param separator
	 * @param array
	 * @return
	 */
	public final static String getMergedArray(String separator, String[] array) {
		if( array == null ) {
			return "";
		}
		else {
			StringBuffer retour = new StringBuffer();
			for( int i=0 ; i<array.length ; i++) {
				if( array[i] == null || array[i].trim().length() == 0 ) {
					continue;
				}
				/*
				 * Don't check//allow  white space here, it'll add a " , " to the query 
				 * e.g SELECT oidsaada,
   						FROM VizierData_IMAGE [.....]
				 */
				if( retour.length() > 0 ) {
					retour.append(separator);
				}
				retour.append(array[i]);
			}

			return retour.toString();
		}
	}
	/**
	 * @param array
	 * @return
	 */
	public final static String getMergedCollection(Collection<?> array) {
		if( array == null ) {
			return "";
		} else {
			String retour ="";
			for( Object o : array) {
				if( o == null || o.toString().trim().length() == 0 ) {
					continue;
				}
				if( retour.length() > 0 ) {
					retour += ", ";
				}
				retour += o.toString();
			}
			return retour;
		}
	}
	
	/**
	 * @param array
	 * @return
	 */
	public final static String getMergedArray(String[] array,  String suffix) {
		if( array == null ) {
			return "";
		}
		else if( suffix == null ) {
			return getMergedArray(array);
		}
		else {
			String retour ="";
			for( int i=0 ; i<array.length ; i++) {
				if( array[i].trim().length() == 0 ) {
					continue;
				}
				if( i > 0 ) {
					retour += ", ";
				}
				retour += array[i] + suffix;
			}
			return retour;
		}
	}
	/**
	 * @param singleEntries
	 * @param suffix
	 * @return
	 */
	public final static String getMergedCollection(Collection<?> singleEntries,  String prefix, String separator) {
		if( singleEntries == null ) {
			return "";
		} else if( separator == null ) {
			return getMergedCollection(singleEntries, prefix, ",");
		} else {
			String retour ="";
			if( prefix == null ) prefix = "";
			int i=0;
			for( Object o : singleEntries) {
				if( o.toString().trim().length() == 0 ) {
					continue;
				}
				if( i > 0 ) {
					retour += separator;
				}
				retour += prefix + " " + o.toString();
				i++;
			}
			return retour;
		}
	}
	
	/**
	 * @param str
	 */
	public final static String quoteString(String str) {
		return "'" + str + "'";
	}

	/**
	 * Do a merge but filter array in order not to have blanks in items
	 * @param array
	 * @return
	 */
	public static String getFilteredAndMergedArray(String[] array) {
		if( array == null ) {
			return "";
		}
		else {
			String retour ="";
			for( int i=0 ; i<array.length ; i++) {
				if( i > 0 ) {
					retour += ", ";
				}
				int pos = array[i].indexOf(" ");
				if( pos > 0 ) {
					retour += array[i].substring(0, pos);					
				}
				else {
					retour += array[i];
				}
			}
			return retour;
		}
	}


}
