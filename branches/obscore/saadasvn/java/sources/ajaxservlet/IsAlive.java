package ajaxservlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import saadadb.database.Database;
import saadadb.sqltable.SQLQuery;


/**
 * Check if the service is working
 * @author michel
 * @version $Id$
 *
 */
public class IsAlive extends SaadaServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		process(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		process(request, response);
	}
	protected void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			response.setContentType("text/plain");
			for( String c: Database.getCachemeta().getCollection_names() );
			SQLQuery q = new SQLQuery();
			q.run("SELECT * FROM saadadb");
			q.close();
			response.getWriter().print("OK");
		} catch(Exception e) {
			response.getWriter().print(e.getMessage());
		}
	}

}
