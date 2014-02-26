package saadadb.collection;

import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.util.SaadaConstant;

/**
 * @author michel
 *
 */
public abstract class Category {
	static public final int ROOT_PRODUCT =0;
	static public final int TABLE =1;
	static public final int ENTRY =2;
	static public final int IMAGE =3;
	static public final int SPECTRUM =4;
	static public final int MISC =5;
	static public final int CUBE =6;
	static public final int FLATFILE =7;
	static public final int UNKNOWN =-1;
	static public final int NB_CAT=8;
	static public final String[] NAMES = {"ROOT_PRODUCT", "TABLE", "ENTRY", "IMAGE", "SPECTRUM", "MISC", "CUBE", "FLATFILE"};

		/**
		 * @param coll_name
		 * @return
		 * @throws SaadaException
		 */
		static public int getCategory(String string_cat) throws FatalException {
			int pos = string_cat.lastIndexOf('_');
			String str = string_cat.substring(pos+1).toLowerCase();
			if( str.endsWith("table") ) {
				return Category.TABLE;
			}
			else if( str.endsWith("entry") ) {
				return Category.ENTRY;
			}
			else if( str.endsWith("image")  || str.endsWith("image3d") ) {
				return Category.IMAGE;
			}
			else if( str.endsWith("spectrum")  ) {
				return Category.SPECTRUM;
			}
			else if( str.endsWith("image3d")   || str.endsWith("cube") ) {
				return Category.CUBE;
			}
			else if( str.endsWith("plot") || str.endsWith("misc") ) {
				return Category.MISC;
			}
			else if( str.endsWith("flatfile")  || str.endsWith("flatfile") ) {
				return Category.FLATFILE;
			}
			else {
				FatalException.throwNewException(SaadaException.WRONG_PARAMETER, "Can't extract any category from the name <" + string_cat + ">");
				return SaadaConstant.INT;
			}			
		}
		
		/**
		 * @param category
		 * @return
		 * @throws SaadaException
		 */
		public static String explain(int category) throws FatalException {
			if( category == TABLE ) {
				return "TABLE";
			}
			else if( category == ENTRY ) {
				return "ENTRY";
			}
			else if( category == IMAGE ) {
				return "IMAGE";
			}
			else if( category == SPECTRUM ) {
				return "SPECTRUM";
			}
			else if( category == CUBE ) {
				return "CUBE";
			}
			else if( category == MISC ) {
				return "MISC";
			}
			else if( category == FLATFILE ) {
				return "FLATFILE";
			}
			else {
				FatalException.throwNewException(SaadaException.WRONG_PARAMETER, "There is no category identify with number  <" + category + ">");				
				return null;
			}
		}
		
		/**
		 * @param category
		 * @return
		 * @throws SaadaException
		 */
		public static String getCollectionExtension(int category) throws FatalException {
			if( category == TABLE ) {
				return "TableSaada";
			}
			else if( category == ENTRY ) {
				return "EntrySaada";
			}
			else if( category == IMAGE ) {
				return "ImageSaada";
			}
			else if( category == SPECTRUM ) {
				return "SpectrumSaada";
			}
			else if( category == CUBE ) {
				return "CubeSaada";
			}
			else if( category == MISC ) {
				return "MiscSaada";
			}
			else if( category == FLATFILE ) {
				return "FlatfileSaada";
			}
			else {
				FatalException.throwNewException(SaadaException.WRONG_PARAMETER, "There is no category identify with number  <" + category + ">");				
				return null;
			}
		}

		/**
		 * @param coll_name
		 * @return
		 * @throws CollectionException
		 */
		public static String explain(String coll_name) throws FatalException {
			int cat = getCategory(coll_name);
			return explain(cat);
		}
		
		/**
		 * @param st
		 * @return
		 */
		public final static String buildRegExp() {
			StringBuffer strBuf = new StringBuffer("(?:");
			for(String key:NAMES){
				strBuf.append("(?:").append(key).append(")|");
			}
			return strBuf.substring(0,strBuf.length()-1)+")";
		}

		/**
		 * @param cat
		 * @return
		 */
		public static boolean isValid(int cat) {
			if( cat < 0 || cat >= NB_CAT)
				return false;
			else 
				return true;
		}
		
		/**
		 * @param cat
		 * @return
		 */
		public static boolean isValid(String cat) {
			for( int i=1 ; i<NB_CAT ; i++ ) {
				if( NAMES[i].equalsIgnoreCase(cat)) {
					return true;
				}
			}
			return true;
		}
		
}
