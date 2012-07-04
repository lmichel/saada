package ajaxservlet.json;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletOutputStream;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import saadadb.meta.AttributeHandler;
import saadadb.util.Messenger;

/**
 * @author laurentmichel
 * * @version $Id$

 */
public abstract class JsonUtils {
	static boolean STDOUT = Messenger.debug_mode;;
	
	public static String getParam(String param, Object value, String indentation) {
		return indentation + "\"" + param + "\": \"" + value + "\"";
	}
	public static String getParam(String param, Object value) {
		return getParam(param, value, "");
	}
	
	@SuppressWarnings("unchecked")
	public static String getRow(List<String> values) {
		JSONArray list = new JSONArray();
		for( int i=0 ; i<values.size() ; i++ ) {
			list.add(values.get(i));
		}
		return list.toString();
	}
	
	@SuppressWarnings("unchecked")
	public static String getRow(String[] values) {
		JSONArray list = new JSONArray();
		for( int i=0 ; i<values.length ; i++ ) {
			list.add(values[i]);
		}
		return list.toString();
	}
	public static String getErrorMsg(String msg) {
		return "{ \"errormsg\": \"" + msg.replaceAll("\"", "'") + "\"}";
	}
	
	public static void teePrint(ServletOutputStream out, String msg) throws IOException {
//		String[] words = msg.split(",", -1);
//		for( String s: words) {
//			if ( s.length() == 3 ) {
//				System.out.println(((int)(s.charAt(0))) + " " + ((int)(s.charAt(1))) + " " + ((int)(s.charAt(2))));
//			}
//			if( STDOUT ) System.out.println(s.length() + " " + s);
//			out.println(s + "\n");
//
//		}
		if( STDOUT ) Messenger.printMsg(Messenger.DEBUG, msg);
		out.println(msg);
	}
	
	/**
	 * Returns a Json string with all usefull fields of the attribute handler
	 * @param ah
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static JSONObject JsonSerialize(AttributeHandler ah) {
		JSONObject jso = new JSONObject();
		
		jso.put("nameorg", ah.getNameorg());
		jso.put("nameattr", ah.getNameattr());
		jso.put("type", ah.getType());
		jso.put("ucd", ah.getUcd());
		jso.put("utype", ah.getUtype());
		jso.put("unit", ah.getUnit());
		jso.put("comment", ah.getComment());
		
		return jso;
	}

}
