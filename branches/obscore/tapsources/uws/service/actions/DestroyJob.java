package uws.service.actions;

/*
 * This file is part of UWSLibrary.
 * 
 * UWSLibrary is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * UWSLibrary is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with UWSLibrary.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright 2012 - UDS/Centre de Données astronomiques de Strasbourg (CDS)
 */

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import uws.UWSException;
import uws.job.JobList;
import uws.job.UWSJob;
import uws.job.user.JobOwner;
import uws.service.UWSService;
import uws.service.UWSUrl;

/**
 * <p>The "Destroy Job" action of a UWS.</p>
 * 
 * <p><i><u>Note:</u> The corresponding name is {@link UWSAction#DESTROY_JOB}.</i></p>
 * 
 * <p>This action destroys the job specified in the UWS URL.
 * The response of this action is a redirection to the jobs list.</p>
 * 
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 05/2012
 */
public class DestroyJob extends UWSAction {
	private static final long serialVersionUID = 1L;

	public DestroyJob(UWSService u) {
		super(u);
	}

	/**
	 * @see UWSAction#DESTROY_JOB
	 * @see uws.service.actions.UWSAction#getName()
	 */
	@Override
	public String getName() {
		return DESTROY_JOB;
	}

	@Override
	public String getDescription() {
		return "Lets stopping the specified job, removing it from its jobs list and destroying all its associated resources. (URL: {baseUWS_URL}/{jobListName}/{job-id}, Method: HTTP-DELETE, No parameter) or (URL: {baseUWS_URL}/{jobListName}/{job-id}, Method: HTTP-POST, Parameter: ACTION=DELETE)";
	}

	/**
	 * Checks whether:
	 * <ul>
	 * 	<li>a job list name is specified in the given UWS URL <i>(<u>note:</u> the existence of the jobs list is not checked)</i>,</li>
	 * 	<li>a job ID is given in the UWS URL <i>(<u>note:</u> the existence of the job is not checked)</i>,</li>
	 * 	<li>the HTTP method is HTTP-DELETE...</li>
	 * 	<li>...<b>or</b> the HTTP method is HTTP-POST <b>and</b> there is the parameter {@link UWSJob#PARAM_ACTION PARAM_ACTION} (=ACTION) with the value {@link UWSJob#ACTION_DELETE ACTION_DELETE} (=DELETE).</li>
	 * </ul>
	 * 
	 * @see uws.service.actions.UWSAction#match(uws.service.UWSUrl, java.lang.String, javax.servlet.http.HttpServletRequest)
	 */
	@Override
	public boolean match(UWSUrl urlInterpreter, JobOwner user, HttpServletRequest request) throws UWSException {
		return (urlInterpreter.hasJobList()
				&& urlInterpreter.hasJob()
				&& (request.getMethod().equalsIgnoreCase("delete") || (request.getMethod().equalsIgnoreCase("post")
						&& request.getParameter(UWSJob.PARAM_ACTION) != null
						&& request.getParameter(UWSJob.PARAM_ACTION).equalsIgnoreCase(UWSJob.ACTION_DELETE))
				));
	}

	/**
	 * Gets the specified jobs list <i>(throw an error if not found)</i>,
	 * gets the specified job <i>(throw an error if not found)</i>,
	 * destroys the job and makes a redirection to the jobs list.
	 * 
	 * @see #getJobsList(UWSUrl)
	 * @see #getJob(UWSUrl, JobList)
	 * @see JobList#destroyJob(String,JobOwner)
	 * @see UWSService#redirect(String, HttpServletRequest, JobOwner, String, HttpServletResponse)
	 * 
	 * @see uws.service.actions.UWSAction#apply(uws.service.UWSUrl, java.lang.String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public boolean apply(UWSUrl urlInterpreter, JobOwner user, HttpServletRequest request, HttpServletResponse response) throws UWSException, IOException {
		// Get the jobs list:
		JobList jobsList = getJobsList(urlInterpreter);

		// Destroy the job:
		boolean destroyed = jobsList.destroyJob(urlInterpreter.getJobId(), user);

		// Make a redirection to the jobs list:
		uws.redirect(urlInterpreter.listJobs(jobsList.getName()).getRequestURL(), request, user, getName(), response);

		return destroyed;
	}

}
