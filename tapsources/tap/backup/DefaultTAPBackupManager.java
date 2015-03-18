package tap.backup;

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

import org.json.JSONException;
import org.json.JSONObject;

import tap.ExecutionProgression;
import tap.TAPExecutionReport;
import tap.TAPJob;
import uws.UWSException;
import uws.job.UWSJob;
import uws.service.UWS;
import uws.service.backup.DefaultUWSBackupManager;

public class DefaultTAPBackupManager extends DefaultUWSBackupManager {

	public DefaultTAPBackupManager(UWS uws) {
		super(uws);
	}

	public DefaultTAPBackupManager(UWS uws, long frequency) {
		super(uws, frequency);
	}

	public DefaultTAPBackupManager(UWS uws, boolean byUser) throws UWSException {
		super(uws, byUser);
	}

	public DefaultTAPBackupManager(UWS uws, boolean byUser, long frequency) throws UWSException {
		super(uws, byUser, frequency);
	}

	@Override
	protected JSONObject getJSONJob(UWSJob job, String jlName) throws UWSException, JSONException {
		JSONObject json = super.getJSONJob(job, jlName);

		if (job instanceof TAPJob && ((TAPJob)job).getExecReport() != null){
			TAPExecutionReport execReport = ((TAPJob)job).getExecReport();

			JSONObject jsonExecReport = new JSONObject();
			jsonExecReport.put("success", execReport.success);
			jsonExecReport.put("sql", execReport.sqlTranslation);
			jsonExecReport.put("uploadduration", execReport.getUploadDuration());
			jsonExecReport.put("parsingduration", execReport.getParsingDuration());
			jsonExecReport.put("translationduration", execReport.getTranslationDuration());
			jsonExecReport.put("executionduration", execReport.getExecutionDuration());
			jsonExecReport.put("formattingduration", execReport.getFormattingDuration());
			jsonExecReport.put("totalduration", execReport.getTotalDuration());

			JSONObject params = json.getJSONObject(UWSJob.PARAM_PARAMETERS);
			if (params == null)
				params = new JSONObject();
			params.put("tapexecreport", jsonExecReport);

			json.put(UWSJob.PARAM_PARAMETERS, params);
		}

		return json;
	}

	@Override
	protected void restoreOtherJobParams(JSONObject json, UWSJob job) throws UWSException {
		if (job != null && json != null && job instanceof TAPJob){
			TAPJob tapJob = (TAPJob)job;
			Object obj = job.getAdditionalParameterValue("tapexecreport");
			if (obj != null){
				if (obj instanceof JSONObject){
					JSONObject jsonExecReport = (JSONObject)obj;
					TAPExecutionReport execReport = new TAPExecutionReport(job.getJobId(), false, tapJob.getTapParams());
					String[] keys = JSONObject.getNames(jsonExecReport);
					for(String key : keys){
						try{
							if (key.equalsIgnoreCase("success"))
								execReport.success = jsonExecReport.getBoolean(key);
							else if (key.equalsIgnoreCase("sql"))
								execReport.sqlTranslation = jsonExecReport.getString(key);
							else if (key.equalsIgnoreCase("uploadduration"))
								execReport.setDuration(ExecutionProgression.UPLOADING, jsonExecReport.getLong(key));
							else if (key.equalsIgnoreCase("parsingduration"))
								execReport.setDuration(ExecutionProgression.PARSING, jsonExecReport.getLong(key));
							else if (key.equalsIgnoreCase("translationduration"))
								execReport.setDuration(ExecutionProgression.TRANSLATING, jsonExecReport.getLong(key));
							else if (key.equalsIgnoreCase("executionduration"))
								execReport.setDuration(ExecutionProgression.EXECUTING_SQL, jsonExecReport.getLong(key));
							else if (key.equalsIgnoreCase("formattingduration"))
								execReport.setDuration(ExecutionProgression.WRITING_RESULT, jsonExecReport.getLong(key));
							else if (key.equalsIgnoreCase("totalduration"))
								execReport.setTotalDuration(jsonExecReport.getLong(key));
							else
								getLogger().warning("The execution report attribute '"+key+"' of the job \""+job.getJobId()+"\" has been ignored because unknown !");
						}catch(JSONException je){
							getLogger().error("[restoration] Incorrect JSON format for the execution report serialization of the job \""+job.getJobId()+"\" (attribute: \""+key+"\") !", je);
						}
					}
					tapJob.setExecReport(execReport);
				}else if (!(obj instanceof JSONObject))
					getLogger().warning("[restoration] Impossible to restore the execution report of the job \""+job.getJobId()+"\" because the stored object is not a JSONObject !");
			}
		}
	}

}
