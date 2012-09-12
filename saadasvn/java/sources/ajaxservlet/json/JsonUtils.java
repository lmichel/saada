package ajaxservlet.json;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

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
		if( STDOUT ) Messenger.printMsg(Messenger.DEBUG, msg);
		out.println(msg);
	}
	/**
	 * Force the MIME type to JSON: avoid FF "badly formed" errors
	 * @param response
	 * @param msg
	 * @throws IOException
	 */
	public static void teePrint(HttpServletResponse response, String msg) throws IOException {
		response.setContentType("application/json");
		teePrint(response.getOutputStream(),msg);
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
