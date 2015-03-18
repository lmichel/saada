package uws.service.backup;

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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.json.JSONWriter;
import org.json.Json4Uws;

import uws.UWSException;
import uws.UWSExceptionFactory;
import uws.UWSToolBox;
import uws.job.ErrorSummary;
import uws.job.ErrorType;
import uws.job.JobList;
import uws.job.Result;
import uws.job.UWSJob;
import uws.job.parameters.UWSParameters;
import uws.job.serializer.JSONSerializer;
import uws.job.user.JobOwner;
import uws.service.UWS;
import uws.service.file.UWSFileManager;
import uws.service.log.UWSLog;

/**
 * <p>Default implementation of the interface {@link UWSBackupManager}.</p>
 * 
 * <p>
 * 	With this class, a UWS can be saved and restored easily thanks to {@link #saveAll()} and {@link #restoreAll()}.
 * 	It is saved in JSON and in one or several files in function of the backup mode:
 * </p>
 * <ul>
 * 	<li><u>by user</u>: one file for each user. The file contains all the information about the user and all the jobs he owns.</li>
 * 	<li><u>in one file</u>: one file to describe all users and all the jobs.</li>
 * </ul>
 * 
 * <p>The backup frequency can also be changed and may have 2 special values:</p>
 * <ul>
 * 	<li><u>{@link #AT_USER_ACTION} (=0)</u>: only the jobs of the user which has just created, destroyed, executed or stopped a job are saved. This frequency is possible only if the backup mode is <u>by user</u>.</li>
 * 	<li><u>{@link #MANUAL} (=-1)</u>: you must call yourself the function {@link #saveAll()} to save the UWS.</li>
 * </ul>
 * <p>Another positive value will be considered as the frequency (in milliseconds) of the automatic backup (= {@link #saveAll()}).</p>
 * 
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 06/2012
 */
public class DefaultUWSBackupManager implements UWSBackupManager {

	/** Special frequency to mean that this manager wait a user action (create, update, start, abort, destruction) to save the jobs of this user. */
	public static final long AT_USER_ACTION = 0;
	/** Special frequency to mean that this manager will NOT save automatically the UWS. The function {@link #saveAll()} MUST be explicitly called. */
	public static final long MANUAL = -1;
	/** Default backup frequency. 60000ms = 60s = 1min */
	public static final long DEFAULT_FREQUENCY = 60000;

	/** Date of the last restoration. */
	protected Date lastRestoration = null;
	/** Date of the last backup. */
	protected Date lastBackup = null;

	/** The UWS to restore/save. */
	protected final UWS uws;

	/** Tells whether the backup (and particularly the automatic one) of the associated UWS is enabled or not. */
	protected boolean enabled = true;

	/** Backup mode: one file by user or one file for all jobs and users. */
	protected final boolean byUser;
	/** Backup frequency (in milliseconds). */
	protected long backupFreq = AT_USER_ACTION;

	/** Timer which saves the backup each <i>backupFreq</i> milliseconds. */
	protected Timer timAutoBackup = null;


	/**
	 * Builds a backup manager in the mode "auto": one file for all users and all jobs, and the backup
	 * is done all minutes (see {@link #DEFAULT_FREQUENCY}.
	 * 
	 * @param uws The UWS to save/restore.
	 * 
	 * @see #DefaultBackupManager(UWS, long)
	 */
	public DefaultUWSBackupManager(final UWS uws) {
		this(uws, DEFAULT_FREQUENCY);
	}

	/**
	 * <p>Builds a backup manager in the mode "auto" or "manual": one file for all users and all jobs, and the backup
	 * is done at the given frequency.</p>
	 * 
	 * <p>If the given frequency is 0 or negative (see {@link #MANUAL}), the backup will not be done automatically. You must manually
	 * save the UWS thanks to the function {@link #saveAll()}.</p>
	 * 
	 * <p>If the given frequency is positive, the backup will be done automatically at the given frequency.</p>
	 * 
	 * @param uws 		The UWS to save/restore.
	 * @param frequency	The backup frequency (in ms ; MUST BE positive and different from 0. If negative or 0, the frequency will be automatically set to {@link #DEFAULT_FREQUENCY}).
	 */
	public DefaultUWSBackupManager(final UWS uws, final long frequency) {
		this.uws = uws;
		this.byUser = false;
		this.backupFreq = (frequency <= 0) ? MANUAL : frequency;

		if (backupFreq > 0){
			timAutoBackup = new Timer();
			timAutoBackup.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					saveAll();
				}
			}, backupFreq, backupFreq);
		}
	}

	/**
	 * Builds a backup manager in the given mode: "by user" (one file for each user and the backup is done at each user action)
	 * or not (one file for all users and all jobs and the backup is done all minutes (see {@link #DEFAULT_FREQUENCY})).
	 * 
	 * @param uws 		The UWS to save/restore.
	 * @param byUser	Backup mode.
	 * 
	 * @throws UWSException	If the user identification is disabled (that's to say, if the given UWS has no UserIdentifier) while the parameter <i>byUser</i> is <i>true</i>.
	 * 
	 * @see #DefaultBackupManager(UWS, boolean, long)
	 */
	public DefaultUWSBackupManager(final UWS uws, final boolean byUser) throws UWSException {
		this(uws, byUser, byUser ? AT_USER_ACTION : DEFAULT_FREQUENCY);
	}

	/**
	 * Builds a backup manager in the given mode and with the given frequency.
	 * 
	 * @param uws 		The UWS to save/restore.
	 * @param byUser	Backup mode (<i>true</i> means one file for each user and <i>false</i>, one file for all users and jobs).
	 * @param frequency	Backup frequency ({@link #AT_USER_ACTION}, {@link #MANUAL}, {@link #DEFAULT_FREQUENCY}, or a positive value).
	 * 
	 * @throws UWSException	If the user identification is disabled (that's to say, if the given UWS has no UserIdentifier) while the parameter <i>byUser</i> is <i>true</i>.
	 */
	public DefaultUWSBackupManager(final UWS uws, final boolean byUser, final long frequency) throws UWSException {
		this.uws = uws;
		this.byUser = byUser;
		this.backupFreq = frequency;

		if (byUser && uws.getUserIdentifier() == null)
			throw new UWSException("Impossible to save/restore a UWS by user, if the user identification is disabled (no UserIdentifier is set to the UWS) !");

		if (backupFreq == AT_USER_ACTION && !byUser)
			backupFreq = MANUAL;
		else if (backupFreq > 0){
			timAutoBackup = new Timer();
			timAutoBackup.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					saveAll();
				}
			}, backupFreq, backupFreq);
		}else if (backupFreq < 0)
			backupFreq = MANUAL;
	}

	/**
	 * Tells whether this backup manager is enabled or not.
	 * 
	 * @return <i>true</i> if the backup is enabled, <i>false</i> otherwise.
	 */
	public final boolean isEnabled() {
		return enabled;
	}

	public final void setEnabled(boolean enabled) {
		this.enabled = enabled;
		if (backupFreq > 0){
			if (this.enabled){
				if (timAutoBackup == null){
					timAutoBackup = new Timer();
					timAutoBackup.scheduleAtFixedRate(new TimerTask() {
						@Override
						public void run() {
							saveAll();
						}
					}, backupFreq, backupFreq);
				}
			}else{
				if (timAutoBackup != null){
					timAutoBackup.cancel();
					timAutoBackup = null;
				}
			}
		}
	}

	/**
	 * Gets the backup frequency.
	 * 
	 * @return The backup frequency (in milliseconds).
	 */
	public final long getBackupFreq() {
		return backupFreq;
	}

	/**
	 * <p>Sets the backup frequency.</p>
	 * 
	 * <p>
	 * 	<i><u>note 1:</u> A negative frequency will be interpreted as "manual"..
	 * 	that's to say you will have to call yourself the {@link #saveAll()} method to save the UWS.
	 * </i></p>
	 * <p>
	 * 	<i><u>note 2:</u> Nothing will be done if the given frequency is {@link #AT_USER_ACTION} although the current backup mode is "by user".
	 * </i></p>
	 * 
	 * @param freq The new backup frequency (in milliseconds) ({@link #AT_USER_ACTION}, {@link #MANUAL}, {@link #DEFAULT_FREQUENCY} or any other positive value).
	 */
	public final void setBackupFreq(long freq) {
		if (freq < 0)
			freq = MANUAL;
		else if (freq == AT_USER_ACTION && !byUser)
			return;

		this.backupFreq = freq;
		if (timAutoBackup != null){
			timAutoBackup.cancel();
			timAutoBackup = null;
		}

		if (enabled && backupFreq > 0){
			timAutoBackup = new Timer();
			timAutoBackup.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					saveAll();
				}
			}, 0, backupFreq);
		}
	}

	/**
	 * Gets the date of the last restoration
	 * 
	 * @return The date of the last restoration (MAY BE NULL).
	 */
	public final Date getLastRestoration() {
		return lastRestoration;
	}

	/**
	 * Gets the date of the last backup.
	 * 
	 * @return The date of the last backup (MAY BE NULL).
	 */
	public final Date getLastBackup() {
		return lastBackup;
	}

	/**
	 * Gets the logger of its UWS, or the default one if it is unknown.
	 * 
	 * @return	A logger.
	 * 
	 * @see UWS#getLogger()
	 * @see UWSToolBox#getDefaultLogger()
	 */
	public UWSLog getLogger(){
		if (uws.getLogger() != null)
			return uws.getLogger();
		else
			return UWSToolBox.getDefaultLogger();
	}


	/* ************ */
	/* SAVE METHODS */
	/* ************ */

	public int[] saveAll() {
		if (!enabled)
			return null;

		int nbSavedJobs = 0, nbSavedOwners = 0;
		int nbJobs = 0, nbOwners = 0;

		// List all users of this UWS:
		HashMap<String, JobOwner> users = new HashMap<String, JobOwner>();
		for(JobList jl : uws){
			Iterator<JobOwner> it = jl.getUsers();
			while(it.hasNext()){
				JobOwner owner = it.next();
				users.put(owner.getID(), owner);
			}
		}

		// "byUser" => 1 file par user => call saveOwner(user, true) for each user:
		if (byUser){
			int[] saveReport;
			for(JobOwner user : users.values()){
				nbOwners++;
				saveReport = saveOwner(user, true);
				if (saveReport != null && saveReport.length == 2){
					nbSavedJobs += saveReport[0];
					nbJobs += saveReport[1];
					nbSavedOwners++;
				}
			}
		}// Otherwise: 1 file for all users and all jobs:
		else{
			UWSFileManager fileManager = uws.getFileManager();
			PrintWriter writer = null;
			try{
				// Create a writer toward the backup file:
				writer = new PrintWriter(fileManager.getBackupOutput());
				JSONWriter out = new JSONWriter(writer);

				// JSON structure: { date: ..., users: [...], jobs: [...] }
				out.object();

				// Write the backup date:
				out.key("date").value((new Date()).toString());

				// Write all users:
				out.key("users").array();
				for(JobOwner user : users.values()){
					nbOwners++;
					try{
						out.value(getJSONUser(user));
						nbSavedOwners++;
					}catch(JSONException je){
						getLogger().error("Unexpected JSON error while saving the user '"+user.getID()+"' !", je);
					}
				}
				out.endArray();
				writer.flush();

				// Write all jobs:
				out.key("jobs").array();
				for(JobList jl : uws){
					for(UWSJob job : jl){
						nbJobs++;
						try{
							out.value(getJSONJob(job, jl.getName()));
							nbSavedJobs++;
							writer.flush();
						}catch(UWSException ue){
							getLogger().error("Unexpected UWS error while saving the job '"+job.getJobId()+"' !", ue);
						}catch(JSONException je){
							getLogger().error("Unexpected JSON error while saving the job '"+job.getJobId()+"' !", je);
						}
					}
				}
				out.endArray();

				// End the general structure:
				out.endObject();

			}catch(JSONException je){
				getLogger().error("Unexpected JSON error while saving the whole UWS !", je);
			}catch(IOException ie){
				getLogger().error("Unexpected IO error while saving the whole UWS !", ie);
			}finally{
				// Close the writer:
				if (writer != null)
					writer.close();
			}
		}

		// Build the report and log it:
		int[] report = new int[]{nbSavedJobs, nbJobs, nbSavedOwners, nbOwners};
		getLogger().uwsSaved(uws, report);

		lastBackup = new Date();

		return report;
	}

	public int[] saveOwner(JobOwner user){
		if (!enabled)
			return null;

		return saveOwner(user, false);
	}

	protected int[] saveOwner(JobOwner user, boolean fromSaveAll) {
		if (!enabled)
			return null;

		// DO NOTHING if the "save" order does not come from saveAll():
		if (!fromSaveAll && backupFreq != AT_USER_ACTION)
			return new int[]{-1,-1};

		UWSFileManager fileManager = uws.getFileManager();
		int[] saveReport = new int[]{0, 0};
		PrintWriter writer = null;
		try{
			// Create a writer toward the backup file:
			writer = new PrintWriter(fileManager.getBackupOutput(user));
			JSONWriter out = new JSONWriter(writer);

			// JSON structure: { date: ..., user: {}, jobs: [...] }
			out.object();

			// Write the backup date:
			out.key("date").value(UWSJob.dateFormat.format(new Date()));

			// Write the description of the user:
			out.key("user").value(getJSONUser(user));
			writer.flush();

			// Write all its jobs:
			out.key("jobs").array();
			for(JobList jl : uws){
				Iterator<UWSJob> it = jl.getJobs(user);
				while(it.hasNext()){
					saveReport[1]++;
					try{
						out.value(getJSONJob(it.next(), jl.getName()));
						saveReport[0]++;
						writer.flush();
					}catch(JSONException je){
						getLogger().error("Unexpected JSON error while saving the "+saveReport[1]+"-th job of the job list '"+jl.getName()+"' owned by the user '"+user.getID()+"'  !", je);
					}catch(UWSException ue){
						getLogger().error("Unexpected UWS error while saving the "+saveReport[1]+"-th job of the job list '"+jl.getName()+"' owned by the user '"+user.getID()+"'  !", ue);
					}
				}
			}
			out.endArray();

			// End the general structure:
			out.endObject();

			// Log the "save" report:
			getLogger().ownerJobsSaved(user, saveReport);

			lastBackup = new Date();

			return saveReport;

		}catch(IOException ie){
			getLogger().error("Unexpected IO error while saving the jobs of user '"+user.getID()+"'  !", ie);
		}catch (JSONException je) {
			getLogger().error("Unexpected JSON error while saving the jobs of user '"+user.getID()+"'  !", je);
		}finally{
			// Close the writer:
			if (writer != null)
				writer.close();
		}

		return null;
	}

	/**
	 * <p>Serializes the given user into a JSON object.</p>
	 * 
	 * <pre>
	 * {
	 * 	"id": "...",
	 * 	"pseudo": "...",
	 * 	...
	 * }
	 * </pre>
	 * <p>
	 * 	<i><u>note</u>:
	 * 	the last suspension points mean that other user data may be added into this JSON object.
	 * 	These other user data to save MUST BE given by {@link JobOwner#getDataToSave()}.
	 * </i></p>
	 * 
	 * @param user	The user to save.
	 * 
	 * @return		Its JSON representation.
	 * 
	 * @throws JSONException	If there is an error while building the JSON object.
	 */
	protected JSONObject getJSONUser(final JobOwner user) throws JSONException {
		JSONObject jsonUser = new JSONObject();
		jsonUser.put("id", user.getID());
		jsonUser.put("pseudo", user.getPseudo());
		if (user.getDataToSave() != null){
			Iterator<Map.Entry<String,Object>> itUserData = user.getDataToSave().entrySet().iterator();
			while(itUserData.hasNext()){
				Map.Entry<String, Object> userData = itUserData.next();
				jsonUser.put(userData.getKey(), userData.getValue());
			}
		}
		return jsonUser;
	}

	/**
	 * <p>Serializes the given job into a JSON object.</p>
	 * 
	 * <p>
	 * 	<i><u>note</u>:
	 * 	the structure of the returned JSON object is decided by {@link JSONSerializer#getJson(UWSJob)}.
	 * 	Only one attribute is added: "jobListName".
	 * </i></p>
	 * 
	 * @param job				The job to save.
	 * @param jlName			Name of the jobs list containing the given job.
	 * 
	 * @return					The JSON representation of the given job.
	 * 
	 * @throws UWSException		If there is an error while getting job parameters and serializing them.
	 * @throws JSONException	If there is an error while building the JSON object.
	 */
	protected JSONObject getJSONJob(final UWSJob job, final String jlName) throws UWSException, JSONException {
		JSONObject jsonJob = Json4Uws.getJson(job);
		jsonJob.put(UWSJob.PARAM_OWNER, (job != null && job.getOwner() != null)?job.getOwner().getID():null);
		jsonJob.put("jobListName", jlName);
		return jsonJob;
	}


	/* ******************* */
	/* RESTORATION METHODS */
	/* ******************* */

	public int[] restoreAll() {
		// Removes all current jobs from the UWS before restoring it from files:
		for(JobList jl : uws)
			jl.clear();

		int nbRestoredJobs = 0, nbRestoredUsers = 0;
		int nbJobs = 0, nbUsers = 0;

		boolean userIdentificationEnabled = (uws.getUserIdentifier() != null);

		UWSFileManager fileManager = uws.getFileManager();
		Iterator<InputStream> itInput;

		// Get the list of the input streams (on all the backup files to read):
		if (byUser){
			if (!userIdentificationEnabled){
				getLogger().error("[restoration] Impossible to restore a UWS by user if the user identification is disabled (that's to say, the UWS has no UserIdentifier) !");
				return null;
			}else
				itInput = fileManager.getAllUserBackupInputs();
		}else{
			try{
				itInput = new SingleInputIterator(fileManager.getBackupInput());
			}catch(IOException ioe){
				getLogger().error("[restoration] Restoration of the UWS "+uws.getName()+" failed because an unexpected IO error has occured.", ioe);
				return null;
			}
		}

		// For each backup file...
		while(itInput.hasNext()){
			InputStream inputStream = itInput.next();
			if (inputStream == null)
				continue;

			// Create the JSON reader:
			JSONTokener in = new JSONTokener(new InputStreamReader(inputStream));

			HashMap<String, JobOwner> users = new HashMap<String, JobOwner>();
			String key;
			JSONObject object;

			try{
				// Reads progressively the general structure (which is theoretically a JSON object):
				JSONObjectReader itKeys = new JSONObjectReader(in, getLogger());
				while(itKeys.hasNext()){

					// name of the current attribute:
					key = itKeys.next();
					if (key == null)
						break;

					// key=DATE:
					if (key.equalsIgnoreCase("date"))
						itKeys.getValue();

					// key=USER (note: this key exists only in the backup file of a specified user):
					else if (key.equalsIgnoreCase("user")){
						nbUsers++;
						try{
							// the value is supposed to be a JSON object:
							object = itKeys.getJSONObject();
							if (object == null){
								nbUsers--;
								continue;
							}
							if (userIdentificationEnabled){
								// build the corresponding instance of DefaultJobOwner:
								JobOwner user = getUser(object);
								if (user != null){
									users.put(user.getID(), user);
									nbRestoredUsers++;
								}
							}
						}catch(UWSException ue){
							getLogger().error("[restoration] A job owner can not be restored !", ue);
							//break;	// Because, the key "user" is found ONLY in the backup file of a user. If the user can not be restored, its jobs won't be !
						}

					}// key=USERS (note: this key exists only in the backup file of the whole UWS):
					else if (key.equalsIgnoreCase("users")){
						// the value is supposed to be an array of JSON objects:
						Iterator<JSONObject> it = itKeys.getArrayReader();
						while(it.hasNext()){
							nbUsers++;
							try{
								// get the JSON object corresponding to the current user:
								object = it.next();
								if (object == null){
									nbUsers--;
									continue;
								}
								if (userIdentificationEnabled){
									// build the corresponding instance of DefaultJobOwner:
									JobOwner user = getUser(object);
									if (user != null){
										users.put(user.getID(), user);
										nbRestoredUsers++;
									}
								}
							}catch(UWSException ue){
								getLogger().error("[restoration] The "+nbUsers+"-th user can not be restored !", ue);
							}
						}

					}// JOBS:
					else if (key.equalsIgnoreCase("jobs")){
						// the value is supposed to be an array of JSON objects:
						Iterator<JSONObject> it = itKeys.getArrayReader();
						while(it.hasNext()){
							nbJobs++;
							try{
								// get the JSON object corresponding to the current job:
								object = it.next();
								if (object == null){
									nbJobs--;
									continue;
								}
								// build the corresponding instance of UWSJob:
								if (restoreJob(object, users))
									nbRestoredJobs++;
							}catch(UWSException ue){
								getLogger().error("[restoration] The "+nbJobs+"-th job can not be restored !", ue);
							}
						}

					}// any other key is ignore but with a warning message:
					else
						getLogger().warning("[restoration] Key '"+key+"' ignored because unknown ! The UWS may be not completely restored !");
				}
			}catch(JSONException je){
				getLogger().error("[restoration] Incorrect JSON format for a UWS backup file !", je);
				return null;
			}catch(Exception e){
				getLogger().error("[restoration] Unexpected error while restoring the UWS !", e);
				return null;
			}finally{
				// Close the reader:
				try{
					inputStream.close();
				}catch(IOException ioe){
					getLogger().error("[restoration] Can not close the input stream opened on a user backup file !", ioe);
				}
				// Set the last restoration date:
				lastRestoration = new Date();
			}
		}

		if (!userIdentificationEnabled && nbUsers > 0)
			getLogger().warning("[restoration] "+nbUsers+" job owners have not been restored because the user identification is disabled in this UWS ! => Jobs of these users have not been restored !");


		// Build the restoration report and log it:
		int[] report = new int[]{nbRestoredJobs, nbJobs, nbRestoredUsers, nbUsers};
		getLogger().uwsRestored(uws, report);

		return report;
	}

	/**
	 * Builds the instance of {@link JobOwner} corresponding to the given JSON object.
	 * 
	 * @param json			JSON representation of the user to build.
	 * 
	 * @return				The corresponding instance of {@link JobOwner} or <i>null</i> if the given object is empty.
	 * 
	 * @throws UWSException	If the "id" parameter is missing (a user MUST have an id ; warning: the case sensitivity is enabled only for this attribute).
	 * 
	 * @see JobOwner#restoreData(Map)
	 */
	protected JobOwner getUser(final JSONObject json) throws UWSException {
		if (json == null || json.length() == 0)
			return null;

		// Fetch all user data:
		String ID = null, pseudo = null;
		String[] keys = JSONObject.getNames(json);
		Map<String, Object> userData = new HashMap<String, Object>(keys.length-2);
		for(String key : keys){
			try{
				if (key.equalsIgnoreCase("id"))
					ID = json.getString(key);
				else if (key.equalsIgnoreCase("pseudo"))
					pseudo = json.getString(key);
				else
					userData.put(key, json.getString(key));
			}catch(JSONException je){
				getLogger().error("[restoration] Incorrect JSON format for the serialization of the user "+ID+" !", je);
			}
		}

		// Check that the ID exists:
		if (ID == null || ID.trim().isEmpty())
			throw UWSExceptionFactory.restoreUserImpossible("Missing user ID !");

		return uws.getUserIdentifier().restoreUser(ID, pseudo, userData);
	}

	/**
	 * Builds the job corresponding to the given JSON object and then restore it in the UWS.
	 * 
	 * @param json			The JSON representation of the job to restore.
	 * @param users			The list of all fetched users.
	 * 
	 * @return				<i>true</i> if the corresponding job has been successfully restored, <i>false</i> otherwise.
	 * 
	 * @throws UWSException	If the job ID or the job list name is missing,
	 * 						or if the job list name is incorrect,
	 * 						or if there is an error with "parameters", "error" and "results".
	 */
	protected boolean restoreJob(final JSONObject json, Map<String, JobOwner> users) throws UWSException {
		if (json == null || json.length() == 0)
			return false;

		String jobListName=null, jobId=null, ownerID=null, tmp;
		//Date destruction=null;
		long quote = UWSJob.UNLIMITED_DURATION, /*duration = UWSJob.UNLIMITED_DURATION, */startTime=-1, endTime=-1;
		HashMap<String, Object> inputParams = new HashMap<String, Object>(10);
		//Map<String, Object> params = null;
		ArrayList<Result> results = null;
		ErrorSummary error = null;

		String[] keys = JSONObject.getNames(json);
		for(String key : keys){
			try{
				// key=JOB_LIST_NAME:
				if (key.equalsIgnoreCase("jobListName"))
					jobListName = json.getString(key);

				// key=JOB_ID:
				else if (key.equalsIgnoreCase(UWSJob.PARAM_JOB_ID))
					jobId = json.getString(key);

				// key=PHASE:
				else if (key.equalsIgnoreCase(UWSJob.PARAM_PHASE))
					;

				// key=OWNER:
				else if (key.equalsIgnoreCase(UWSJob.PARAM_OWNER))
					ownerID = json.getString(key);

				// key=RUN_ID:
				else if (key.equalsIgnoreCase(UWSJob.PARAM_RUN_ID)){
					String runId = json.getString(key);
					inputParams.put(UWSJob.PARAM_RUN_ID, runId);

				}// key=QUOTE:
				else if (key.equalsIgnoreCase(UWSJob.PARAM_QUOTE))
					quote = json.getLong(key);

				// key=EXECUTION_DURATION:
				else if (key.equalsIgnoreCase(UWSJob.PARAM_EXECUTION_DURATION)){
					long duration = json.getLong(key);
					inputParams.put(UWSJob.PARAM_EXECUTION_DURATION, duration);

				}// key=DESTRUCTION:
				else if (key.equalsIgnoreCase(UWSJob.PARAM_DESTRUCTION_TIME)){
					try{
						tmp = json.getString(key);
						inputParams.put(UWSJob.PARAM_DESTRUCTION_TIME, UWSJob.dateFormat.parse(tmp));
					}catch(ParseException pe){
						getLogger().error("[restoration] Incorrect date format for the '"+key+"' parameter !", pe);
					}

				}// key=START_TIME:
				else if (key.equalsIgnoreCase(UWSJob.PARAM_START_TIME)){
					tmp = json.getString(key);
					try{
						Date d = UWSJob.dateFormat.parse(tmp);
						startTime = d.getTime();
					}catch(ParseException pe){
						getLogger().error("[restoration] Incorrect date format for the '"+key+"' parameter !", pe);
					}

				}// key=END_TIME:
				else if (key.equalsIgnoreCase(UWSJob.PARAM_END_TIME)){
					tmp = json.getString(key);
					try{
						Date d = UWSJob.dateFormat.parse(tmp);
						endTime = d.getTime();
					}catch(ParseException pe){
						getLogger().error("[restoration] Incorrect date format for the '"+key+"' parameter !", pe);
					}

				}// key=PARAMETERS:
				else if (key.equalsIgnoreCase(UWSJob.PARAM_PARAMETERS))
					inputParams.put(UWSJob.PARAM_PARAMETERS, getParameters(json.getJSONObject(key)));

				// key=RESULTS:
				else if (key.equalsIgnoreCase(UWSJob.PARAM_RESULTS))
					results = getResults(json.getJSONArray(key));

				// key=ERROR:
				else if (key.equalsIgnoreCase(UWSJob.PARAM_ERROR_SUMMARY)){
					error = getError(json.getJSONObject(key));

				}// Ignore any other key but with a warning message:
				else
					getLogger().warning("[restoration] The job attribute '"+key+"' has been ignored because unknown ! A job may be not completely restored !");

			}catch(JSONException je){
				getLogger().error("[restoration] Incorrect JSON format for a job serialization (attribute: \""+key+"\") !", je);
			}
		}

		// The job list name is REQUIRED:
		if (jobListName == null || jobListName.isEmpty())
			getLogger().error("[restoration] Missing job list name ! => Can not restore the job "+jobId+" !");

		// The job list name MUST correspond to an existing job list:
		else if (uws.getJobList(jobListName) == null)
			getLogger().error("[restoration] No job list named "+jobListName+" ! => Can not restore the job "+jobId+" !");

		// The job ID is REQUIRED:
		else if (jobId == null || jobId.isEmpty())
			getLogger().error("[restoration] Missing job ID ! => Can not restore a job !");

		// Otherwise: the job can be created and restored:
		else{
			// Search the job owner:
			JobOwner owner = users.get(ownerID);

			// If the specified user is unknown, display a warning and create the job without owner:
			if (ownerID != null && !ownerID.isEmpty() && owner == null){
				getLogger().error("[restoration] Unknown job owner: "+ownerID+" ! => Can not restore the job "+jobId+" !");
				return false;
			}

			// Build the UWSParameters object:
			UWSParameters uwsParams;
			try{
				uwsParams = uws.getFactory().createUWSParameters(inputParams);
			}catch(UWSException ue){
				getLogger().error("[restoration] Error with at least one of the UWS parameters to restore !", ue);
				return false;
			}

			// Create the job:
			UWSJob job = uws.getFactory().createJob(jobId, owner, uwsParams, quote, startTime, endTime, results, error);

			// Restore other job params if needed:
			restoreOtherJobParams(json, job);

			// Restore it:
			return (uws.getJobList(jobListName).addNewJob(job) != null);
		}
		return false;
	}

	/**
	 * <p>
	 * 	Restores other job parameters, either from the given JSON object or from the parameters map of the given job.
	 * 	The job is supposed to be updated after the call of this function.
	 * </p>
	 * 
	 * <p><i><u>note:</u> By default, this function does nothing ! It is called by {@link #restoreJob(JSONObject, Map)}
	 * just after the default restoration from the given JSON and just before to add the job in its dedicated jobs list.</i></p>
	 * 
	 * @param json				JSON backup of the given job.
	 * @param job				Default restoration of the job.
	 * 
	 * @throws UWSException		If there is an error while restoring other job parameters.
	 * 
	 * @see #restoreJob(JSONObject, Map)
	 */
	protected void restoreOtherJobParams(final JSONObject json, final UWSJob job) throws UWSException {
		;
	}

	/**
	 * Builds the list of parameters corresponding to the given JSON object.
	 * 
	 * @param obj				The JSON representation of a parameters list.
	 * 
	 * @return					The corresponding list of parameters
	 * 							or <i>null</i> if the given object is empty.
	 * 
	 * @throws UWSException
	 */
	protected Map<String, Object> getParameters(final JSONObject obj) throws UWSException {
		if (obj == null || obj.length() == 0)
			return null;

		HashMap<String, Object> params = new HashMap<String, Object>(obj.length());
		String[] names = JSONObject.getNames(obj);
		for(String n : names){
			try{
				params.put(n, obj.get(n));
			}catch(JSONException je){
				getLogger().error("Incorrect JSON format for the serialization of the parameter '"+n+"' !", je);
			}
		}
		return params;
	}

	/**
	 * Builds the list of results corresponding to the given JSON array.
	 * 
	 * @param array				The JSON representation of the results to restore.
	 * 
	 * @return					The corresponding list of results
	 * 							or <i>null</i> if the array is empty.
	 * 
	 * @throws UWSException		If there is an error while restoring one of the result.
	 * 
	 * @see {@link #getResult(JSONObject)}
	 */
	protected ArrayList<Result> getResults(final JSONArray array) throws UWSException {
		if (array == null || array.length() == 0)
			return null;

		ArrayList<Result> results = new ArrayList<Result>(array.length());
		for(int i=0; i<array.length(); i++){
			try{
				Result r = getResult(array.getJSONObject(i));
				if (r != null)
					results.add(r);
			}catch(JSONException je){
				getLogger().error("Incorrect JSON format for the serialization of the "+(i+1)+"-th result !", je);
			}
		}

		return results;
	}

	/**
	 * Builds the result corresponding to the given JSON object.
	 * 
	 * @param obj				The JSON representation of the result to restore.
	 * 
	 * @return					The corresponding result or <i>null</i> if the given object is empty.
	 * 
	 * @throws JSONException	If there is an error while reading the JSON.
	 * @throws UWSException
	 */
	protected Result getResult(final JSONObject obj) throws JSONException, UWSException {
		if (obj == null || obj.length() == 0)
			return null;

		String id=null, type=null, href=null, mime=null;
		boolean redirection = false;
		long size = -1;
		String[] names = JSONObject.getNames(obj);
		for(String n : names){
			if (n.equalsIgnoreCase("id"))
				id = obj.getString(n);
			else if (n.equalsIgnoreCase("type"))
				type = obj.getString(n);
			else if (n.equalsIgnoreCase("href"))
				href = obj.getString(n);
			else if (n.equalsIgnoreCase("mime"))
				mime = obj.getString(n);
			else if (n.equalsIgnoreCase("redirection"))
				redirection = obj.getBoolean(n);
			else if (n.equalsIgnoreCase("size"))
				size = obj.getLong(n);
			else
				getLogger().warning("[restoration] The result parameter '"+n+"' has been ignored because unknown ! A result may be not completely restored !");
		}

		if (id == null){
			getLogger().error("[restoration] Missing result ID ! => A result can not be restored !");
			return null;
		}else{
			Result r = new Result(id, type, href, redirection);
			r.setMimeType(mime);
			r.setSize(size);
			return r;
		}
	}

	/**
	 * Builds the error summary corresponding to the given JSON object.
	 * 
	 * @param obj				The JSON representation of the error summary to restore.
	 * 
	 * @return					The corresponding error summary or <i>null</i> if the given object is empty.
	 * 
	 * @throws UWSException
	 */
	protected ErrorSummary getError(final JSONObject obj) throws UWSException {
		if (obj == null || obj.length() == 0)
			return null;

		String type=null, message=null, details=null;
		String[] names = JSONObject.getNames(obj);
		for(String n : names){
			try{
				if (n.equalsIgnoreCase("type"))
					type = obj.getString(n);
				else if (n.equalsIgnoreCase("detailsRef"))
					details = obj.getString(n);
				else if (n.equalsIgnoreCase("hasDetail"))
					;//hasDetail = obj.getBoolean(n);
				else if (n.equalsIgnoreCase("message"))
					message = obj.getString(n);
				else
					getLogger().warning("[restoration] The error attribute '"+n+"' has been ignored because unknown ! => An error summary may be not completely restored !");
			}catch(JSONException je){
				getLogger().error("Incorrect JSON format for an error serialization !", je);
			}
		}
		if (message != null)
			return new ErrorSummary(message, ErrorType.valueOf(type), details);
		else
			return null;
	}


	/* **************** */
	/* USEFUL ITERATORS */
	/* **************** */

	/**
	 * Lets reading a JSON object from a {@link JSONTokener} (that's to say directly from a file),
	 * as an iterator which returns all the keys. The value of each key can be fetched thanks to
	 * the different available getters (i.e. {@link #getJSONArray()}, {@link #getJSONObject()},
	 * {@link #getValue()}, {@link #getString()}, ...).
	 * 
	 * @author Gr&eacute;gory Mantelet (CDS)
	 * @version 05/2012
	 */
	protected final static class JSONObjectReader implements Iterator<String> {

		private final UWSLog logger;
		private final JSONTokener input;
		private String nextKey = null;
		private boolean valueGot = false;

		private boolean endReached = false;

		public JSONObjectReader(final JSONTokener input, final UWSLog log) throws JSONException {
			this.input = input;
			this.logger = log;

			if (input.nextClean() != '{')
				throw input.syntaxError("A JSONObject text must begin with '{'");

			if (input.nextClean() == '}')
				endReached = true;
			else
				input.back();
		}

		private void readNext() throws JSONException {
			if (nextKey != null){
				nextKey = null;
				if (!prepareNextPair()){
					nextKey = null;
					return;
				}
			}

			char c = input.nextClean();
			switch (c) {
			case 0:
				throw input.syntaxError("A JSONObject text must end with '}'");
			case '}':
				endReached = true;
				return;
			default:
				input.back();
				nextKey = input.nextValue().toString();
			}

			/*
			 * The key is followed by ':'. We will also tolerate '=' or '=>'.
			 */
			c = input.nextClean();
			if (c == '=') {
				if (input.next() != '>') {
					input.back();
				}
			} else if (c != ':') {
				throw input.syntaxError("Expected a ':' after a key");
			}
		}

		private boolean prepareNextPair() throws JSONException {
			if (!valueGot)
				skipValue();

			/*
			 * Pairs are separated by ','. We will also tolerate ';'.
			 */
			switch (input.nextClean()) {
			case ';':
			case ',':
				if (input.nextClean() == '}') {
					endReached = true;
					return false;
				}
				input.back();
				break;
			case '}':
				endReached = true;
				return false;
			default:
				throw input.syntaxError("Expected a ',' or '}'");
			}

			return true;
		}

		private void skipValue() throws JSONException {
			valueGot = true;
			input.nextValue();
		}

		public JSONObject getJSONObject() throws JSONException {
			valueGot = true;
			return new JSONObject(input);
		}

		public JSONArray getJSONArray() throws JSONException {
			valueGot = true;
			return new JSONArray(input);
		}

		public String getString() throws JSONException {
			valueGot = true;
			return input.nextValue().toString();
		}

		public Object getValue() throws JSONException {
			valueGot = true;
			return input.nextValue();
		}

		public JSONArrayReader getArrayReader() throws JSONException {
			valueGot = true;
			return new JSONArrayReader(input, logger);
		}

		public JSONObjectReader getObjectReader() throws JSONException {
			valueGot = true;
			return new JSONObjectReader(input, logger);
		}

		@Override
		public boolean hasNext() {
			return !endReached;
		}

		@Override
		public String next() throws NoSuchElementException {
			if (endReached)
				throw new NoSuchElementException();

			try{
				readNext();
				return nextKey;
			}catch(JSONException je){
				logger.error("Incorrect JSON format in an object !", je);
				endReached = true;
				return null;
			}
		}

		@Override
		public void remove() throws UnsupportedOperationException {
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * Lets reading a JSON array from a {@link JSONTokener} (that's to directly from a file)
	 * as an iterator <b>which returns all items which MUST BE {@link JSONObject}s</b>.
	 * 
	 * @author Gr&eacute;gory Mantelet (CDS)
	 * @version 05/2012
	 */
	protected final static class JSONArrayReader implements Iterator<JSONObject> {

		private final UWSLog logger;
		private final JSONTokener input;
		private final char closeToken;
		private boolean endReached = false;

		private JSONObject nextObj = null;

		public JSONArrayReader(final JSONTokener input, final UWSLog log) throws JSONException {
			this.input = input;
			this.logger = log;

			char c = input.nextClean();
			switch(c){
			case '[':
				closeToken = ']';
				break;
			case '(':
				closeToken = ')';
				break;
			default:
				endReached = true;
				throw input.syntaxError("A JSONArray text must start with '['");
			}

			readNext();
		}

		protected void readNext() throws JSONException {
			nextObj = null;

			while(nextObj == null && !endReached){
				// Read the JSON object:
				char c = input.nextClean();
				if (c != ',' && c != ';' && c != ']' && c != ')'){
					input.back();
					nextObj = (JSONObject)input.nextValue();
					c = input.nextClean();
				}

				// Ensures the next character is allowed (',' or ']'):
				switch (c) {
				case ';':
				case ',':
					if (input.nextClean() == ']') {
						endReached = true;
						return;
					}
					input.back();
					break;
				case ']':
				case ')':
					endReached = true;
					if (closeToken != c) {
						throw input.syntaxError("Expected a '" + new Character(closeToken) + "'");
					}
					return;
				default:
					endReached = true;
					throw input.syntaxError("Expected a ',' or ']'");
				}
			}
		}

		@Override
		public boolean hasNext() {
			return (nextObj != null);
		}

		@Override
		public JSONObject next() throws NoSuchElementException {
			if (nextObj == null && endReached)
				throw new NoSuchElementException();
			JSONObject obj = nextObj;
			try{
				readNext();
			}catch(JSONException je){
				logger.error("Incorrect JSON format in an Array !", je);
				endReached = true;
				nextObj = null;
			}
			return obj;
		}

		@Override
		public void remove() throws UnsupportedOperationException {
			throw new UnsupportedOperationException();
		}

	}

	/**
	 * An iterator of input streams with ONLY ONE input stream.
	 * 
	 * @author Gr&eacute;gory Mantelet (CDS)
	 * @version 05/2012
	 * 
	 * @see DefaultUWSBackupManager#restoreAll()
	 */
	protected final static class SingleInputIterator implements Iterator<InputStream> {
		private InputStream input;
		public SingleInputIterator(final InputStream input){
			this.input = input;
		}
		@Override
		public boolean hasNext() { return (input != null); }
		@Override
		public InputStream next() throws NoSuchElementException {
			if (input == null)
				throw new NoSuchElementException();
			else{
				InputStream in = input;
				input = null;
				return in;
			}
		}
		@Override
		public void remove() throws UnsupportedOperationException {
			throw new UnsupportedOperationException();
		}
	}

}
