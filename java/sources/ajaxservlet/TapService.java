package ajaxservlet;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import saadadb.util.Messenger;
import saadadb.vo.tap.TapServiceConnection;
import tap.TAPException;
import tap.resource.TAP;
import uws.UWSException;

public class TapService extends SaadaServlet {
	private static final long serialVersionUID = 1L;
	private TAP<?> tap = null;

	@Override
	public void init(ServletConfig cfg) throws ServletException {
		super.init();
		try {
			TapServiceConnection connection = new TapServiceConnection();
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
		printAccess(request, false);
	LinkedHashMap<String, String>params=new LinkedHashMap<String, String>();
		params=	(LinkedHashMap<String, String>) getFlatParameterMap(request);
	
		for(Map.Entry<String, String>e : params.entrySet()) {
			Messenger.locateCode("Key: "+e.getKey()+" | Value "+e.getValue());
		}
		tap.executeRequest(request, response);
	}
}
