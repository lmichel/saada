package ajaxservlet.json;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;

import saadadb.database.Database;
import saadadb.util.Version;
import ajaxservlet.SaadaServlet;

/**
 * Servlet implementation class GetVersion
 */
public class GetVersion extends SaadaServlet {
	private static final long serialVersionUID = 1L;


	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@SuppressWarnings("unchecked")
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			JSONObject retour = new JSONObject();
			retour.put("dbname", Database.getDbname());
			retour.put("dbms", Database.getWrapper().getDBMS());
			retour.put("version", Database.version());
			retour.put("date", Version.date);
			JsonUtils.teePrint(response, retour.toJSONString());
		} catch(Exception e){
			reportJsonError(request, response, e);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
