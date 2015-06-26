package ajaxservlet;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ajaxservlet.accounting.QueryContext;
import ajaxservlet.accounting.UserTrap;
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

		TapServiceConnection connection;
		try {
			connection = new TapServiceConnection();
			tap = new TAP(connection);
			tap.init(cfg);
		} catch (Exception e) {
			throw new ServletException("Unable to Init TAP Service: "+e.getMessage(),e);
		}
	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		printAccess(request, false);
		LinkedHashMap<String, String>params=new LinkedHashMap<String, String>();
		params=	(LinkedHashMap<String, String>) getFlatParameterMap(request);

		if(Messenger.debug_mode == true) {
			for(Map.Entry<String, String>e : params.entrySet()) {
				Messenger.locateCode("Key: "+e.getKey()+" | Value "+e.getValue());
			}
		}
		tap.executeRequest(request, response);
	}
}
