package saadadb.util;

import java.util.HashMap;

import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;



/**
 * Some function helping to deal with Java type at class creation
 * FITS fmt -<> Java type conversion
 * Java type comparison
 *  * @version $Id$

 * @author laurent
 *
 * 12/2013: gestion des types supportes pour les attributs etendus
 */
public class JavaTypeUtility {
	/*
	 * List of supported types
	 */
	public static final int DOUBLE  = 0; 
	public static final int FLOAT   = 1; 
	public static final int BOOLEAN = 2;
	public static final int BYTE    = 3; 
	public static final int CHAR    = 4; 
	public static final int SHORT   = 5; 
	public static final int INT     = 6; 
	public static final int LONG    = 7; 
	public static final int STRING  = 8; 
	public static final int CHARARRAY  = 9; // fully-qualified-class (take String)
	public static final int UNSUPPORTED  = 10; // fully-qualified-class (take String)
	
	public static final String ATTREXTENDTYPES[] = new String[]{"String", "int", "long", "double", "boolean"};
	/*
	 * Sort of Java type by ascendant strength (more specific to more general)
	 */
	private  static HashMap<String, Integer> strengthRank;
	static {
		strengthRank = new HashMap<String, Integer>();
		strengthRank.put("boolean", 1);
		strengthRank.put("char", 2);
		strengthRank.put("byte", 3);
		strengthRank.put("short", 4);
		strengthRank.put("int", 5);
		strengthRank.put("long", 6);
		strengthRank.put("float", 7);
		strengthRank.put("double", 8);
		strengthRank.put("String", 9);
	}

	/**
	 * return true if type is one of the supported types for the exteded attributes
	 * @param type
	 * @return
	 */
	public static boolean isSupportedForExtAtt(String type){
		for( int i=0 ; i<ATTREXTENDTYPES.length ; i++){
			if( type.equals(ATTREXTENDTYPES[i]) ){
				return true;
			}
		}
		return false;		
	}
	/**
	 * Returns true if the java type current_type can express any value (and more) of type new_type
	 * @param current_type
	 * @param new_type
	 * @return
	 * @throws FatalException: unsupported type
	 */
	public static boolean strongerThan(String current_type, String new_type) throws FatalException  {
		if( strengthRank.get(new_type) == null ) {
			FatalException.throwNewException(SaadaException.UNSUPPORTED_TYPE, "Unknown type <" + new_type + ">");
			return false;
		}
		if( strengthRank.get(current_type) > strengthRank.get(new_type) ) {
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * Returns the numerical code of the Java type matching the FITS column format
	 * FITS Array cells are not considered
	 * Bits are considered as Strings
	 * Complex are also considered as Strings (but not supported by Java FITS!!)
	 * The response is noty the same to ASCII or BINARY table. ASCII have less supported types.
	 * @param format
	 * @param asciiTable : true if the considered table is an ASCII table
	 * @return
	 */
	public static int convertFitsFormatToJavaType(String format, boolean asciiTable) {
		if( format.indexOf('A')  >= 0) {
			if( asciiTable ) return   STRING;
			else  return   CHARARRAY;
		}
		else if( format.indexOf('L')  >= 0)	return   BOOLEAN;
		else if( format.indexOf('X')  >= 0)	return   UNSUPPORTED;
		else if( format.indexOf('B')  >= 0)	return   BYTE;
		else if( format.indexOf('I')  >= 0) 	{
			if( asciiTable ) return  INT;
			else  return   SHORT;
		}
		else if( format.indexOf('J')  >= 0)	return   INT;
		else if( format.indexOf('K')  >= 0)	return   LONG;
		else if( format.indexOf('E')  >= 0)	return   FLOAT;
		else if( format.indexOf('F')  >= 0)	return   FLOAT;
		else if( format.indexOf('D')  >= 0)	return   DOUBLE;
		else if( format.indexOf('C')  >= 0)	return   UNSUPPORTED;
		else if( format.indexOf('M')  >= 0)	return   UNSUPPORTED;
		else return   UNSUPPORTED;

	}
	
	/**
	 * Convert a numerical Java type code to the real Java type
	 * @param typeCode
	 * @return
	 */
	public static String convertJavaTypeCodeToName(int typeCode)  {
		switch(typeCode) {
		case DOUBLE   : return "double";
		case FLOAT    : return "float";
		case BOOLEAN  : return "boolean";
		case BYTE     : return "byte";
		case CHAR     : return "char";
		case SHORT    : return "short";
		case INT      : return "int";
		case LONG     : return "long";
		case CHARARRAY:
		case STRING   : 
		default       : return "String";
		}
	}

}
