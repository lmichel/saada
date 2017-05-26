package saadadb.util;

import java.io.File;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;

/**
 * Excecute an ant task from the Saadadb build .xml
 * @author michel
 * @version $Id$
 *
 */
public class AntTarget {
	private final String target;
	private final String description;
	private String propSummary =  "";
	private final Project project;
	private final ProjectHelper helper;
	private final String buildFile;

	/**
	 * @param target : ant targe name
	 * @param description: task description (printed out by the messeneger)
	 */
	public AntTarget(String target, String description) {
		this.target = target;
		this.description = description;
		this.project= new Project();
		this.project.init();
		this.helper = ProjectHelper.getProjectHelper();		
		this.buildFile = Database.getRoot_dir() + File.separatorChar + "bin" + File.separator + "build.xml";
		this.helper.parse(this.project, new File(this.buildFile));
		this.initLogger();
	}

	/**
	 * Set of the Ant logger
	 * May be parametrized in the future
	 */
	private void initLogger(){
		org.apache.tools.ant.DefaultLogger log = new org.apache.tools.ant.DefaultLogger();
		log.setErrorPrintStream(System.err);
		log.setOutputPrintStream(System.out);
		log.setMessageOutputLevel(Project.MSG_INFO);
		this.project.addBuildListener(log);
	}


	/**
	 * Add a user property the task
	 * @param name
	 * @param value
	 * @throws QueryException 
	 */
	public void setProperty(String name,  String value) throws QueryException {
		if( name == null || value == null || name.length() == 0 ) {
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "Properties can not be null");
		}
		this.propSummary += name+ "=" + value + " ";
		this.project.setProperty(name,  value);
	}

	/**
	 * Return 
	 * @return
	 */
	public String getPropSummary() {
		return ((this.propSummary.length() > 0)?("[" + this.propSummary + "]"): "[no user parameter]");
	}
	/**
	 * Execute the task 
	 * @throws FatalException 
	 * @param reloadMeta : reload the cache meta after the task is complete
	 * @throws Exception
	 */
	public void execute(boolean reloadMeta) throws Exception {
		Messenger.printMsg(Messenger.TRACE, "Run ant task " + this.target + " in " + this.buildFile);
		this.project.executeTarget(this.target);
		Messenger.printMsg(Messenger.TRACE, "task " + this.description + this.getPropSummary() + ":  done");
		if( reloadMeta)	Database.getCachemeta().reload(true);
	}

	public static void main(String[] args) throws Exception{
		ArgsParser ap = new ArgsParser(args);
		Database.init(ap.getDBName());
		AntTarget at = new AntTarget("task.dummy", "Remove relation MiscToMisc");
		at.setProperty("param", "MiscToMisc");
		at.execute(true);
		Database.close();
	}

}
