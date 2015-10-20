package saadadb.util;
/** * @version $Id$

 * <p>Title: SAADA </p>
 * <p>Description: Automatic Archival System For Astronomical Data -
    This is framework of a PhD funded by the CNES and by the Region Alsace.</p>
 * <p>Copyright: Copyright (c) 2002-2006</p>
 * <p>Company: Observatoire Astronomique Strasbourg-CNES</p>
 * @author: MILLAN Patrick
 */

public class ChangeType{

	public static final int nbType = 9;

	//Indices:                                0         1         2        3       4          5       6          7 
	public static final String[] tabType  = {"String", "double", "float", "long", "int"    , "byte", "boolean", "char"     , "short"    };
	public static final String[] tabClass = {"String", "Double", "Float", "Long", "Integer", "Byte", "Boolean", "Character", "Short"};

	//Indices                                         0         1      2      3                   4        5        6          7 
	public static final String[] tabTypeSQL       = {"A"     , "I"  , "J"  , "D"               , "E"    , "F"    , "L"      , "B"       };
	public static final String[] tabSQLFromFits   = {"String", "int", "int", "double"          , "float", "float", "boolean", "byte"    };
	public static final String[] tabSQLFromPSQL   = {""      , "int", "int", "double precision", "float", "float", "boolean", "smallint"};

	/**
	 * @param type
	 * @return
	 */
	public static String getType(String type){
		String test;
		for(int i = 0; i<nbType; i++){
			test = tabType[i];
			if(type.indexOf(test) >= 0){
				return test;
			}
		}
		return "";
	}



	/**
	 * @param typeJava
	 * @return
	 */
	public static String getTypeJavaFromTypeClass(String typeJava){
		String test;
		for(int i = 0; i<nbType; i++){
			test = tabClass[i];
			if(typeJava.equals("class java.lang."+test)){
				return test;
			}
			test = tabType[i];
			if(typeJava.equals(test)){
				return test;
			}
		}
		if(typeJava.equals("class java.util.Date")){
			return "Date";
		}
		return "String";
	}


}

