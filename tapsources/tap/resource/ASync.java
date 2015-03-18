package tap.resource;

/*
 * This file is part of TAPLibrary.
 * 
 * TAPLibrary is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * TAPLibrary is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with TAPLibrary.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright 2012 - UDS/Centre de Données astronomiques de Strasbourg (CDS)
 */

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import tap.ServiceConnection;
import tap.TAPException;
import uws.UWSException;
import uws.job.JobList;
import uws.service.UWSService;
import uws.service.backup.UWSBackupManager;

public class ASync implements TAPResource {

	public static final String RESOURCE_NAME = "async";

	@SuppressWarnings("unchecked")
	protected final ServiceConnection service;
	protected final UWSService uws;

	@SuppressWarnings("unchecked")
	public ASync(ServiceConnection service) throws UWSException, TAPException {
		this.service = service;

		uws = service.getFactory().createUWS();

		if (uws.getUserIdentifier() == null)
			uws.setUserIdentifier(service.getUserIdentifier());

		if (uws.getJobList(getName()) == null)
			uws.addJobList(new JobList(getName()));

		if (uws.getBackupManager() == null)
			uws.setBackupManager(service.getFactory().createUWSBackupManager(uws));

		UWSBackupManager backupManager = uws.getBackupManager();
		if (backupManager != null){
			backupManager.setEnabled(false);
			int[] report = uws.getBackupManager().restoreAll();
			String errorMsg = null;
			if (report == null || report.length == 0)
				errorMsg = "GRAVE error while the restoration of the asynchronous jobs !";
			else if (report.length < 4)
				errorMsg = "Incorrect restoration report format ! => Impossible to know the restoration status !";
			else if (report[0] != report[1])
				errorMsg = "FAILED restoration of the asynchronous jobs: "+report[0]+" on "+report[1]+" restored !";
			else
				backupManager.setEnabled(true);

			if (errorMsg != null){
				errorMsg += " => Backup disabled.";
				service.getLogger().error(errorMsg);
				throw new UWSException(UWSException.INTERNAL_SERVER_ERROR, errorMsg);
			}
		}
	}

	@Override
	public String getName() { return RESOURCE_NAME; }

	@Override
	public void setTAPBaseURL(String baseURL) { ; }

	public final UWSService getUWS(){
		return uws;
	}

	@Override
	public void init(ServletConfig config) throws ServletException { ; }

	@Override
	public void destroy() { ; }

	@Override
	public boolean executeResource(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, TAPException, UWSException {
		return uws.executeRequest(request, response);
	}

}
