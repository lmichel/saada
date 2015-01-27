package saadadb.resourcetest;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import saadadb.util.Messenger;

public class TapTestAsync {

	private enum Phase {
		PENDING, QUEUED, EXECUTING, COMPLETED, ABORTED, ERROR;
	}

	/*
	 * Sync query :
	 * 
	 * tap/
	 * async?
	 * UPLOAD=table_name,path_to_file;table_name2,path_to_file_2
	 * &REQUEST=doQuery or getCapabilities
	 * &LANG=ADQL or PQL
	 * &QUERY=SELECT TOP 1 s_ra FROM VizierData.p0105070101epx000oimage8000 NATURAL JOIN TAP_UPLOAD.taphandlesample
	 */

	/*
	 * 	see:  http://cdsportal.u-strasbg.fr/taptuto/reminder.html
	 * for more informations about the parameters
	 */
	public static void main(String[] args) {

		Messenger.debug_mode = true;

		/*
		 * Parameters
		 */
		String host = "http://localhost:8080/saadaObscore/tap/async";
		String upload = ""; 		// The file(s) to upload; syntax : "file_name,file_path;file_name_2,file_path2"
		String request = "doQuery"; // {doQuery | getCapabilities}
		String lang = "ADQL"; 		// {ADQL | PQL}  only if request = doQuery
		String ADQLquery = "Select * from p0105070101epx000oimage8000"; // only if request = doQuery
		String phase = "RUN"; 		// { 'can be unspecified' defaults to PENDING | RUN | PENDING } Automatically start the job at creation. if  "PENDING" or unspecified, you must send a  POST to the
							  		// host/{jobid}/phase=run to start the job
		String responseFilePath = "/home/hahn/"; //The path were the output files will be saved

		Phase currentPhase = Phase.EXECUTING;
		String jobID = "";
		StringBuffer params = new StringBuffer();
		params.append("REQUEST=").append(request);
		if (request.trim().equalsIgnoreCase("doQuery")) {
			params.append("&LANG=").append(lang);

			if (!upload.trim().isEmpty()) {
				if (upload.endsWith(";"))
					upload = upload.substring(0, upload.length() - 1);
				System.out.println(upload);
				params.append("&UPLOAD=").append(upload);
			}

			if (!ADQLquery.trim().isEmpty()) {
				params.append("&QUERY=").append(ADQLquery);
			}
		}
		params.append("&PHASE=").append(phase);

		/*
		 * Create and Start the Job
		 */

		try {
			URL obj = new URL(host);
			HttpURLConnection connector = (HttpURLConnection) obj.openConnection();
			connector.setRequestMethod("POST");
			connector.setDoOutput(true);

			// Send Post Request
			DataOutputStream output = new DataOutputStream(connector.getOutputStream());
			output.writeBytes(params.toString());
			output.flush();
			output.close();
			Messenger.locateCode("Starting the Job");
			System.out.println("\nSending 'POST' request to URL : " + host);
			System.out.println("Post parameters : " + params.toString());
			System.out.println("Response Code : " + connector.getResponseCode());
			System.out.println("\n");
			InputStreamReader input = new InputStreamReader(connector.getInputStream());
			BufferedReader reader = new BufferedReader(input);
			String inputLine;
			Pattern pattern = Pattern.compile("<uws:jobId><!\\[CDATA\\[(\\w*)\\]\\]></uws:jobId>");
			Matcher matcher;
			while ((inputLine = reader.readLine()) != null) {

				matcher = pattern.matcher(inputLine.trim());
				if (matcher.matches()) {
					jobID = matcher.group(1);
					Messenger.printMsg(Messenger.DEBUG, "JobID=" + jobID);
				}
			}
			/*
			 * Wait for the job to be completed
			 */
			StringBuffer response = new StringBuffer(); // if an error occurs, we can get it in this StringBuffer
			if (!jobID.isEmpty()) {
				Messenger.printMsg(Messenger.DEBUG, "Waiting for the job to be completed");
				do {
					obj = new URL(host + "/" + jobID);
					connector = (HttpURLConnection) obj.openConnection();
					connector.setRequestMethod("GET");
					pattern = Pattern.compile("<uws:phase>(\\w*)</uws:phase>");
					input = new InputStreamReader(connector.getInputStream());
					reader = new BufferedReader(input);
					PrintWriter responseFile = new PrintWriter(responseFilePath
							+ "tapTestAsync.STATUS.xml", "UTF-8");
					while ((inputLine = reader.readLine()) != null) {
						response.append(inputLine);
						matcher = pattern.matcher(inputLine.trim());

						responseFile.println(inputLine);
						if (matcher.matches()) {
							switch (matcher.group(1).trim().toLowerCase()) {
							case "completed":
								currentPhase = Phase.COMPLETED;
								break;
							case "executing":
								currentPhase = Phase.EXECUTING;
								// System.out.println("executing");
								break;
							case "pending":
								currentPhase = Phase.PENDING;
								// System.out.println("pending");
								break;
							case "aborted":
								currentPhase = Phase.ABORTED;
								// System.out.println("aborted");
								break;
							case "error":
								currentPhase = Phase.ERROR;
								// System.out.println("error");
								break;

							}
						}
					}
					responseFile.close();
					Messenger.locateCode("Current Phase is " + currentPhase);
					if (currentPhase == Phase.EXECUTING) {
						Messenger.printMsg(
								Messenger.DEBUG,
								"Job Not finished, going to Sleep for 500ms...");
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {

						}
					}
				} while (!(currentPhase == Phase.COMPLETED || currentPhase == Phase.ABORTED || currentPhase == Phase.ERROR));

				/*
				 * Get the result of the Job
				 */
				if (currentPhase == Phase.COMPLETED) {
					Messenger.printMsg(Messenger.DEBUG, "Job, finished, getting result");
					PrintWriter responseFile = new PrintWriter(responseFilePath
							+ "tapTestAsync.RESULT.xml", "UTF-8");
					obj = new URL(host + "/" + jobID + "/results/result");
					connector = (HttpURLConnection) obj.openConnection();
					connector.setRequestMethod("GET");

					input = new InputStreamReader(connector.getInputStream());
					reader = new BufferedReader(input);
					while ((inputLine = reader.readLine()) != null) {
						responseFile.println(inputLine);
					}
					responseFile.close();
					Messenger.printMsg(Messenger.DEBUG, "Result file saved at '" + responseFilePath
							+ "tapTestAsync.RESULUT.xml'");

				} else if (currentPhase == Phase.ERROR) {
					pattern = Pattern.compile(".*<uws:message>(.*)</uws:message>.*");
					matcher = pattern.matcher(response.toString().trim());
					if (matcher.matches()) {
						Messenger.locateCode(matcher.group(1));
					}

					Messenger
							.printMsg(Messenger.DEBUG, "Please Check the file '" + responseFilePath
									+ "tapTestAsync.STATUS.xml' for the complete output");
				}
				reader.close();
				connector.disconnect();
				reader.close();
				connector.disconnect();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
