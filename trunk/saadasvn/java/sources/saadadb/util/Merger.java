package saadadb.util;


/**
 * @author michel
 * * @version $Id$

 * 12/2009: ignore empty items in arrays to merge
 */
public class Merger {
	
	/**
	 * @param array
	 * @return
	 */
	public final static String getMergedArray(String[] array) {
		if( array == null ) {
			return "";
		}
		else {
			String retour ="";
			for( int i=0 ; i<array.length ; i++) {
				if( array[i] == null || array[i].trim().length() == 0 ) {
					continue;
				}
				if( retour.length() > 0 ) {
					retour += ", ";
				}
				retour += array[i];
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
