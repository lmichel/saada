package ajaxservlet;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.util.Messenger;

/**
 * Servlet implementation class updateCache
 */
public class updateCache extends SaadaServlet {
	private static final long serialVersionUID = 1L;
	private static final  long DELAY_MINI = 60;
	private static long LAST_CALL = -1;

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
	/**
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	private  void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		printAccess(request, true);
		long t = (new Date()).getTime()/1000;
		long dt = (t - LAST_CALL);
		if( dt > DELAY_MINI ) {
			Messenger.printMsg(Messenger.TRACE, "Reload cache from servlet");
			try {
				Database.getCachemeta().reload(true);
				LAST_CALL = t;
			} catch (FatalException e) {
				getErrorPage(request, response,e);
			}
		} else {
			Messenger.printMsg(Messenger.WARNING, "Cache already reloaded since " + dt + " sec: must wait a while to redo the operation");
		}
		
	}

}
