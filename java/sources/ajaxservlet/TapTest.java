package ajaxservlet;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import saadadb.vo.tap.tap2.TapSaadaServiceConnection;
import tap.TAPException;
import tap.resource.TAP;
import uws.UWSException;

public class TapTest extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private TAP<?> tap = null;

	@Override
	public void init(ServletConfig cfg) throws ServletException {
		super.init();
		try {
			TapSaadaServiceConnection connection = new TapSaadaServiceConnection();
			tap = new TAP(connection);
			tap.init(cfg);
		} catch (UWSException e) {
			e.printStackTrace();
		} catch (TAPException e) {
			e.printStackTrace();
		}
		// this.tap.setTAPBaseURL("/taptest");
		// Customize the TAP home page:
		// tap.setHomePageURI("index.html");
		catch (Exception e) {
			e.printStackTrace();
		}

		// tap.getUWS().addSerializer(new JSONSerializer());
	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		tap.executeRequest(request, response);
	}
}
