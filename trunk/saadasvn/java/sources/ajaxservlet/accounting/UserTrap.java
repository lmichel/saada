package ajaxservlet.accounting;



import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import saadadb.util.Messenger;

/**
 * @author michel
 * @version $Id$
 */
public class UserTrap {

	public static UserAccount getUserAccount(HttpServletRequest req) throws ServletException {
		try {
			HttpSession session = req.getSession(true);  

			session.setMaxInactiveInterval(-1);
			String session_id = session.getId();
			if (session.isNew()) {
				UserAccount account = new UserAccount(session_id);
				session.setAttribute("account", account);
				return account;
			} 
			else {
				UserAccount account =  (UserAccount) session.getAttribute("account");
				/*
				 * The reason of this situation (a session without account) are not well understood!!
				 */
				if( account == null ) {
					account = new UserAccount(session_id);
					session.setAttribute("account", account);
				}
				return account;	    

			}
		} catch(Exception e) {
			Messenger.printStackTrace(e);
			throw new ServletException(e);

		}
	}
	public static void destroySession(HttpServletRequest request) throws IOException {
		HttpSession session = request.getSession(true);  
		if ( !session.isNew()) {
			UserAccount account =  (UserAccount) session.getAttribute("account");	
			if( account != null ) account.destroySession();
		}
		session.invalidate();		
	}

}
