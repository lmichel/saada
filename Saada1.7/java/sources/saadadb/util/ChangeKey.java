package saadadb.util;
/**
 * <p>Title: System d'archivage automatique des donnes astrononique</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Observaroire Astronomique Strasbourg</p>
 * @version SAADA 1.0
 * @author: NGUYEN Ngoc Hoan
 * E-Mail: nguyen@saadadb.u-strasbg.fr</p>
 */
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import saadadb.meta.AttributeHandler;

public class ChangeKey {

    public static byte getAsscii(char c){
	return (Character.toString(c).getBytes())[0];
    }
 
    /** * @version $Id$

     * Make the key compatible with SQL and Java. 
     * The changed key is in lower case and applying
     * several time changeKey will no longer modify it
     * @param key
     * @return
     */
    public static String changeKey(String key){
    	String ret = key.replaceAll("[^\\w]+", "_").toLowerCase();
//    	if( ret.startsWith("_") ) {
//    		return ret;
//    	}
//    	else {
//    		return "_" + ret;
//    	}
    		String retour = "_" + ret;
    		/*
    		 * Reserved KW for mysql
    		 */
    		if( "_filename".equalsIgnoreCase(retour)) {
    			return "_file_name";
    		}
    		return retour;
    }

    public static String changeKeyHIERARCH(String key){
        String retour = key.replaceAll(" ","_");
        if( retour.equals(key) == false ) {
        	if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Change HIERARCH key <" + key + "> to <" + retour + ">");
        }
        return retour;
        }

    
   
    public static char getChar(byte code){
	return Byte.toString(code).charAt(0);
    }
    
    /**
     * Attempt to build an SQL compliant nickname from a UCD.
     * Unicity is not sure
     * @param ucd
     * @return
     */
    public static String getUCDNickName(String ucd) {
    	String[] meta_comp = ucd.split("[\\.:;]");
    	String name="";
    	for( int i=0 ; i<meta_comp.length ; i++ ) {
    		if( i == 0 ) {
    			name = meta_comp[0] + "_";
    		}
    		else if( meta_comp[i].length() <= 3 ) {
    			name += meta_comp[i];
    		}
    		else {
    			name += meta_comp[i].substring(0,3);

    		}
    	}
    	return name.replaceAll("-", "_");
    }
	/*
	 * PSQL being not case sensitive, one have to avoid duplicate keys
	 * e.g m1_x = M1_x for psql but not for java or FITS
	 */     
 public static String renameDuplicateKey(LinkedHashMap<String, AttributeHandler> kwtable, String key) {
	 /*
	  * PSQL being not case sensitive, one have to avoid duplicate keys
	  * e.g m1_x = M1_x for psql but not for java or FITS
	  */
	 String modified_key = key.toLowerCase();
	 while( kwtable.get(modified_key) != null ) {
		 Pattern p = Pattern.compile(".*_saada([1-9][0-9]*)");
		 Matcher m = p.matcher(modified_key);
		 if( m.find() ) {
			 int num = Integer.parseInt(m.group(1));
			 modified_key = modified_key.replace("_saada" + m.group(1), "_saada" + (num + 1));
		 }
		 else {
			 modified_key = modified_key + "_saada1";
		 }
	 }
	 if( !key.toLowerCase().equals(modified_key )) {
		 Messenger.printMsg(Messenger.WARNING, "Attribute " + key + " already exist: renamed as " 
				 + modified_key);
	 }
	 return modified_key;
 }
 


}
  
