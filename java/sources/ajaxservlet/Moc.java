package ajaxservlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import saadadb.database.Repository;
import saadadb.util.Messenger;

/**
 *  Servlet returning the MOC of image of the given collection
 *  usage: db/moc?collection=collection 
 * @version $Id$
 * Servlet implementation class Download
 */
public class Moc extends SaadaServlet {
	private static final long serialVersionUID = 1L;


	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
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
		printAccess(request, false);
		try {
			String collection = request.getParameter("collection");
			if( collection != null ) {
				downloadProduct(request, response, Repository.getMocCollectionMocPath(collection));
			} else {
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
			}
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}

	}

}
