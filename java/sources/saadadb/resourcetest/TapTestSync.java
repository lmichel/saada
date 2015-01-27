package saadadb.resourcetest;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import saadadb.util.Messenger;

public class TapTestSync {

	/*
	 * Sync query :
	 * 
	 * tap/
	 * sync?
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
		Messenger.debug_mode=true;
		/*
		 * Parameters
		 */
		String host = "http://localhost:8080/saadaObscore/tap/sync";
		String upload = "";
		String request = "doQuery";
		String lang = "ADQL"; // only if request = doQuery
		String ADQLquery = "Select * from p0105070101epx000oimage800"; // only if request = doQuery

		String responseFilePath = "/home/hahn/";

		StringBuffer params = new StringBuffer();

		params.append("REQUEST=").append(request);
		
		if (request.trim().equalsIgnoreCase("doQuery")) {
			params.append("&LANG=").append(lang);

			if (!upload.trim().isEmpty()) {
				params.append("&UPLOAD=").append(upload);
			}

			if (!ADQLquery.trim().isEmpty()) {
				params.append("&QUERY=").append(ADQLquery);
			}
		}
		HttpURLConnection connector;
		try {
			URL obj = new URL(host);
			connector = (HttpURLConnection) obj.openConnection();
			connector.setRequestMethod("POST");
			connector.setDoOutput(true);

			// Send Post Request
			DataOutputStream output = new DataOutputStream(connector.getOutputStream());
			output.writeBytes(params.toString());
			output.flush();
			output.close();

			System.out.println("\nSending 'POST' request to URL : " + host);
			System.out.println("Post parameters : " + params.toString());
			System.out.println("Response Code : " + connector.getResponseCode());
			System.out.println("\n");
			if(connector.getResponseCode() == 200) {
			InputStreamReader input = new InputStreamReader(connector.getInputStream());
			BufferedReader reader = new BufferedReader(input);
			PrintWriter responseFile = new PrintWriter(
					responseFilePath + "tapTestSync.RESULT.xml",
					"UTF-8");
			String inputLine;
			while ((inputLine = reader.readLine()) != null) {
				System.out.println(inputLine);
				responseFile.println(inputLine);
			}
			reader.close();
			responseFile.close();
			connector.disconnect();

			reader.close();
			connector.disconnect();
			
			Messenger.printMsg(Messenger.DEBUG, "Result file saved at '" + responseFilePath
					+ "tapTestSync.RESULUT.xml'");
			}else {
				Messenger.printMsg(Messenger.DEBUG, "An ERROR "+connector.getResponseCode()+" occured");
				PrintWriter responseFile = new PrintWriter(
						responseFilePath + "tapTestSync.RESULT.xml",
						"UTF-8");
			BufferedReader error = new BufferedReader(new InputStreamReader(connector.getErrorStream()));
		       String inputLine;
				while((inputLine = error.readLine()) != null) {
					System.out.println(inputLine);
					responseFile.println(inputLine);
				}
				Messenger.printMsg(Messenger.DEBUG, "Result file saved at '" + responseFilePath
						+ "tapTestSync.RESULUT.xml'");
				responseFile.close();
				error.close();
			}
		} catch (IOException e) {
		
			e.printStackTrace();
		}

	}
}
