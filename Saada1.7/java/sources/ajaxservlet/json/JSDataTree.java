package ajaxservlet.json;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import saadadb.collection.Category;
import saadadb.database.Database;
import ajaxservlet.SaadaServlet;

/** * @version $Id$

 * Servlet implementation class JSDataTree
 */
public class JSDataTree extends SaadaServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public JSDataTree() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ServletOutputStream out = response.getOutputStream();
		String[] colls = Database.getCachemeta().getCollection_names();
		printAccess(request, true);
		try {
			response.setContentType("application/json");
			JsonUtils.teePrint(out, "{\"data\" : [");
			boolean first = true;
			for( String coll: colls ) {
				if( !first ) {
					JsonUtils.teePrint(out, "    ,");
				}
				JsonUtils.teePrint(out, "    {  \"attr\"     : { \"id\"   : \"" + coll + "\" },");
				JsonUtils.teePrint(out, "       \"data\"     : { \"title\"   : \"" + coll + "\" },");
				JsonUtils.teePrint(out, "       \"children\" : [");
				boolean cfirst = true;
				for( int cat: new int[] {Category.TABLE, Category.ENTRY,  Category.IMAGE,  Category.SPECTRUM,  Category.MISC,  Category.FLATFILE}) {
					if( Category.FLATFILE == cat && Database.getCachemeta().getCollection(coll).hasFlatFiles())  {
						if( !cfirst ) {
							JsonUtils.teePrint(out, "        ,");
						}
						JsonUtils.teePrint(out, "        {\"attr\"     : { \"id\"   : \"" + coll + "." +  Category.explain(cat) + "\" },");
						JsonUtils.teePrint(out, "         \"data\"     : \"" + Category.explain(cat) + "\",");
						JsonUtils.teePrint(out, "         \"children\" : []}");
						cfirst = false;
					}
					else {
						String[] classes = Database.getCachemeta().getClassesOfCollection(coll, cat);
						if( classes.length > 0 ) {
							if( !cfirst ) {
								JsonUtils.teePrint(out, "        ,");
							}
							JsonUtils.teePrint(out, "        {\"attr\"     : { \"id\"   : \"" + coll + "." + Category.explain(cat) + "\" },");
							JsonUtils.teePrint(out, "         \"data\"     : \"" + Category.explain(cat) + "\",");
							JsonUtils.teePrint(out, "         \"children\" : [");
							boolean clfirst = true;
							for( String classe: classes ) {
								if( !clfirst ) {
									JsonUtils.teePrint(out, "            ,");
								}
								JsonUtils.teePrint(out, "            {\"attr\" : { \"id\"   : \"" + coll + "." +  Category.explain(cat) +  "." + classe + "\" },");
								JsonUtils.teePrint(out, "             \"data\" : \"" + classe + "\"}");
								clfirst = false;
							}
							cfirst = false;
							JsonUtils.teePrint(out, "           ] } ");
						}
					}
				}
				JsonUtils.teePrint(response, " ] }  " );
				first = false;
			}
			JsonUtils.teePrint(out, " ] }");
		} catch (Exception e) {
			reportJsonError(request, response, e);
		}

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}

}
