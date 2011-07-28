package ajaxservlet.json;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class GetJsonArray
 */
public class GetJsonArray extends HttpServlet {
	private static final long serialVersionUID = 1L;


	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String file = request.getParameter("file");
		response.setContentType("application/json");
		ServletOutputStream out = response.getOutputStream();   


		if( file == null ) {
			file = (String) request.getAttribute("file");
		}
		if( file != null ) {
			System.out.println(file);
			InputStream is = getServletConfig().getServletContext().getResourceAsStream("jsonsample/" + file + ".json") ;
			try {
				if( is == null ) {
					JsonUtils.teePrint(out,JsonUtils.getErrorMsg("Can't read file " + file));
					return;
				}

				Scanner scanner = new Scanner(is);
				while (scanner.hasNextLine()){
					String line = scanner.nextLine();
					JsonUtils.teePrint(out,line);
				}
			}
			catch(Exception e) {
				JsonUtils.teePrint(out,JsonUtils.getErrorMsg(e.toString()));
			}
			finally{
				try {is.close();out.close();} catch (Exception e) {}
			}
		}
		else {
			int nbrec = 10;
			int echo = Integer.parseInt(request.getParameter("sEcho"));
			JsonUtils.teePrint(out,"{");
			JsonUtils.teePrint(out,JsonUtils.getParam("sEcho", new Integer(echo) , "    ") + ",");
			JsonUtils.teePrint(out,JsonUtils.getParam("iTotalRecords", 100, "    ") + ",");
			JsonUtils.teePrint(out,JsonUtils.getParam("iTotalDisplayRecords", 100,  "    ") + ",");

			JsonUtils.teePrint(out,"\"aaData\": [");
			for( int i=0 ; i<nbrec ; i++ ) {
				//String comma = (i != (nbrec-1))? ",": "";
				//JsonUtils.teePrint(out,JsonUtils.getRow(new Integer[]{i}, "      " ) + comma);
			}
			JsonUtils.teePrint(out,"     ]");
			JsonUtils.teePrint(out,"}");
		}
	      out.close();

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
