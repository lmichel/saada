package saadadb.util;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import md5.MD5;
import saadadb.meta.AttributeHandler;
/**
 * <p>Title: System d'archivage automatique des donnes astrononique</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Observaroire Astronomique Strasbourg</p>
 * @author XXXX
 * @version 00000001
 */
public class MD5Key {

	public static String calculMD5Key(String s){
		byte[] out = new byte[16];
		MD5 md = new MD5();
		md.update(s.getBytes());
		md.md5final(out);
		return md.dumpBytes(out);
	}


	public static String  getFmtsignature(Map<String, AttributeHandler> ahs ) {
		/*
		 * Build an ordered map with name/type of each attribute
		 */
		TreeMap<String, String> md5tree = new TreeMap<String, String>();
		String md5Key = "", md5Type = "";
		for( AttributeHandler ah: ahs.values()) {
			md5tree.put(ah.getNameorg(), ah.getType());
		}
		/*
		 * Compute the MD5 signature
		 */
		Iterator it = md5tree.keySet().iterator();
		while( it.hasNext() ) {
			String key = (String)it.next();
			md5Key += key;
			md5Type += md5tree.get(key);
		}
		return   MD5Key.calculMD5Key(md5Key+md5Type);
	}
}