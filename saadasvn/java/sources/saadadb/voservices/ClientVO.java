package saadadb.voservices;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import saadadb.util.Messenger;

/** * @version $Id$

 * This is the Client part of the Saada VO service.
 * It allows you to do VOQL queries on a Saada database,
 * and to get a VOTable file or URL as a result.
 */
public class ClientVO {
    
    
    public static final int M_URL = 1, M_FILE = 2;
    
    
    public ClientVO (String param, int method) {
	PrintWriter votable =  null;
	try {
	    
	    VOSaadaServiceService locator = new VOSaadaServiceServiceLocator();
	    // We get a proxy
	    VOSaadaService myv = locator.getVOSaada();
	    String adqlXMLString = getXMLFromFile(param);
	    //System.out.println("Adql String Input : " + adqlXMLString);
	    String result = "";
	    if (method == M_FILE) {
		// first method
		result = myv.serviceFile(adqlXMLString);
		System.out.println("================== VOtable result received from service : ======================\n");
	    } else {
		// second method
		result = myv.serviceURL(adqlXMLString);
		System.out.println("================== VOtable URL received from service : ======================\n");
	    }
	    System.out.println(result);
	    
	} catch (java.rmi.RemoteException se) {
	    Messenger.printMsg(Messenger.ERROR, "Saada Service error : " + se.getMessage());
	    /*Throwable cause = se.getCause();
	      if (cause != null) {
	      Messenger.printMsg(Messenger.ERROR, "Cause : ");
	      causMessenger.printStackTrace(e);
	      } else {
	      sMessenger.printStackTrace(e);
	      }*/
	} catch (Exception e) {
	    Messenger.printMsg(Messenger.ERROR, "Client Error : " + e.getMessage());
	    //Messenger.printStackTrace(e);
	}
    }
    
    /**
     * Main method
     * @param args arguments : VOTable ADQL query [ + output format options]
     */
    public static void main(String [] args) throws Exception {
	if (args.length == 0 || args.length > 2) {
	    System.out.println("Usage: java ClientSaada [option] <ADQL XML filename>");
	    System.out.println("Type 'java ClientSaada -help' for more information.");
	    System.exit(0);
	}
	for(int i = 0; i < args.length; ++i) {
	    if (args[i].equals("-help")) {
		System.out.println("Usage: java ClientSaada [option] <ADQL XML filename>");
		System.out.println(" Options : ");
		System.out.println(" -url : returns a VOTable String containing a URL to the real VOTable result");
		System.out.println(" -file : returns directly the VOTable result as a String (default value)");
		System.exit(0);
	    }
	}
	int m = M_URL;
	String fileName = null;
	for(int i = 0; i < args.length; ++i) {
	    if (args[i].equals("-url")) {
		m = M_URL;
	    } else if (args[i].equals("-file")) {
		m = M_FILE;
	    } else {
		fileName = args[i];
	    }
	}
	if (fileName == null) {
	    System.out.println("Usage: java ClientSaada [option] <ADQL XML filename>");
	    System.out.println("Type 'java ClientSaada -help' for more information.");
	    System.exit(0);
	}
	new ClientVO(fileName, m);
    }
    
    public static String getXMLFromFile(String nameFile) throws IOException {
	FileReader in = new FileReader(nameFile);
	BufferedReader reader = new BufferedReader(in);
	String str="",stringXML="";
	while ((str = reader.readLine()) != null) {
	    if (str.indexOf("#")<0)
		stringXML = stringXML + str;
	}
	return stringXML;
    }

}
