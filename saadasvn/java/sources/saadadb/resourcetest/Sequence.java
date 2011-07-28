package saadadb.resourcetest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.TreeSet;

import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.UnknownElement;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.selectors.FileSelector;

import saadadb.database.Database;
import saadadb.exceptions.SaadaException;
import saadadb.util.Messenger;

/**
 * This class is very specific to the Saada TestBed.
 * It runs one test on one or all supported DBMS.
 * These test beds are usually run by script from code updated by SVN. This tool allows to run (and debug) one 
 * test without commiting code.
 * USAGE: Sequence [PSQL|MSQL|SQLITE|all] [test#|all] [follow]
 * 
 * @author laurent
 *
 */
public class Sequence {
	static final String baseDir = "/home/michel/saada/deploy/TestBeds/";
	/**
	 * @param args
	 * @throws SaadaException 
	 */
	public static void main(String[] cmdargs) throws Exception {
		/*
		 * Restore the last reponse
		 */
		String repfilename = System.getProperty("java.io.tmpdir") + Database.getSepar() + "sequence.txt";
		File f = new File(repfilename);
		String defaultResp = "";
		if( f.exists() ) {
			BufferedReader br = new BufferedReader( new FileReader(f));
			defaultResp = br.readLine();
			br.close();
		}
		/*
		 * Prompt for the new command parameters (last response by default)
		 */
		System.out.print("Parameters [" + defaultResp + "]: ");
		System.out.flush();
		/*
		 * Stores the response in tmp if diff from the last one
		 */
		InputStreamReader inp = new InputStreamReader(System.in);
		String arg = (new BufferedReader(inp)).readLine();
		if( arg.length() == 0 ) {
			Messenger.printMsg(Messenger.TRACE, "Take last: " + defaultResp);
			arg = defaultResp;
		}
		else {
			BufferedWriter bw = new BufferedWriter( new FileWriter(f));
			bw.write(arg);
			bw.close();
		}
		/*
		 * Analyse parameters
		 */
		String[] args = arg.split(" ", -1);		
		if( (args.length != 2 && args.length != 3 ) || (!args[0].equals("PSQL") && !args[0].equals("MSQL") && !args[0].equals("SQLITE")) ) {
			Messenger.printMsg(Messenger.ERROR, "USAGE: Sequence [PSQL|MSQL|SQLITE|all] [test#|all] [follow]");
			System.exit(1);
		}
		String[] dbs;
		if( args[0].trim().equals("all")) {
			dbs = new String[]{"PSQL" , "MSQL" , "SQLITE"};
		}
		else {
			dbs = new String[]{args[0].trim()};
		}
		boolean follow = false;
		if( args.length == 3 && args[2].startsWith("fol")) {
			follow = true;
		}
		/*
		 * Run the test for all selected DBMS
		 */
		for( String db: dbs ) {
			/*
			 * Look for tne test bed starting with test#
			 */
			File bdir = new File(baseDir + "/beds/");
			TreeSet<String> tsts = new TreeSet<String> ();			
			for( String tb: bdir.list()) tsts.add(tb);
			boolean started = false;
			for( String tb: tsts) {
				if( args[1].trim().equalsIgnoreCase("all") || tb.startsWith(args[1].trim() + "." ) || (follow && started) ) {
					Messenger.printMsg(Messenger.TRACE, "Start test bench <" + tb +  "> on " + db );

					started = true;
					Project p = new Project();
					p.init();
					ProjectHelper helper = ProjectHelper.getProjectHelper();
					DefaultLogger log = new DefaultLogger();
					log.setErrorPrintStream(System.err);
					log.setOutputPrintStream(System.out);
					log.setMessageOutputLevel(Project.MSG_INFO);
					p.addBuildListener(log);
					/*
					 * Set project resources usually defined in shell scripts
					 */
					p.setProperty("testbeddir", baseDir + "/version2_0/dbms_" + db);
					helper.parse(p, new File(baseDir + "/beds/" + tb + "/build.xml")); 
					/*
					 * Change the class path in order to work with Eclipse env
					 */
					for( Object t: p.getReferences().keySet()) {
						if( t.toString().equals("saadadb.classpath") ) {
							Path pth =  new Path(p);
							pth.setPath(p.getProperty("SAADA_DB_HOME") + "/class_mapping:" + System.getProperty("java.class.path"));
							p.getReferences().put(t, pth);
						}
					}
				/*
				 * Run the default (for testbeds)  target
				 */
				p.executeTarget("runtest");
				}
			}
		}
	}
}
