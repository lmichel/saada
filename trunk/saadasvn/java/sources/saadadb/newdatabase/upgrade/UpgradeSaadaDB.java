package saadadb.newdatabase.upgrade;

import java.io.File;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

import saadadb.database.SaadaDBConnector;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.util.Messenger;
import saadadb.util.Version;

public class UpgradeSaadaDB {
	public final String rootDir;
	private String saadadbToUpgrade;
	private String tarTarget;
	private String repositoryToUpgrade;
	private SaadaDBConnector connector;



	/**
	 * @param rootDir
	 */
	UpgradeSaadaDB(String rootDir, SaadaDBConnector connector, String tarTarget) {
		this.rootDir = rootDir;
		this.connector = connector;
		if( tarTarget != null && tarTarget.length() > 0 )
			this.tarTarget = tarTarget;
		else 
			this.tarTarget = "";
	}

	/**
	 * @param saadadbToUpgrade
	 */
	private void upgradeDistrib() throws Exception{
		this.saadadbToUpgrade = connector.getRoot_dir();;
		this.repositoryToUpgrade = connector.getRepository();

		Project project = new Project();
		project.init();
		ProjectHelper helper = ProjectHelper.getProjectHelper();
		org.apache.tools.ant.DefaultLogger log = new org.apache.tools.ant.DefaultLogger();
		log.setErrorPrintStream(System.err);
		log.setOutputPrintStream(System.out);
		log.setMessageOutputLevel(Project.MSG_INFO);
		project.addBuildListener(log);
		helper.parse(project, new File(rootDir + File.separator + "bin" + File.separator + "build.xml"));

		project.setProperty("saadadbhome", this.connector.getRoot_dir());
		Messenger.printMsg(Messenger.TRACE, "SaadaDB located at " + this.saadadbToUpgrade);
		project.setProperty("repository", this.connector.getRepository());
		Messenger.printMsg(Messenger.TRACE, "Repository  located at " + this.repositoryToUpgrade);
		if( tarTarget != null && tarTarget.length() > 0 ) {
			project.setProperty("tarname", tarTarget);			
			Messenger.printMsg(Messenger.TRACE, "Make a backup in " + tarTarget);
			project.executeTarget("upgrade.tar");
		}
		
		Messenger.printMsg(Messenger.TRACE, "Upgrade distribution");
		project.executeTarget("upgrade.saadadb");

	}

	/**
	 * @throws Exception
	 */
	private void upgradeWebApps() throws Exception{
		Messenger.printMsg(Messenger.TRACE, "Redeplay Web interfae of the SaadaDB located at " + this.connector.getRoot_dir());
		Project project = new Project();
		project = new Project();
		project.init();
		ProjectHelper helper = ProjectHelper.getProjectHelper();
		org.apache.tools.ant.DefaultLogger log = new org.apache.tools.ant.DefaultLogger();
		log.setErrorPrintStream(System.err);
		log.setOutputPrintStream(System.out);
		log.setMessageOutputLevel(Project.MSG_INFO);
		project.addBuildListener(log);
		helper.parse(project, new File(this.saadadbToUpgrade + File.separator + "bin" + File.separator + "build.xml"));
		project.executeTarget("xmode.set");
		project.executeTarget("tomcat.deploy");
		Messenger.printMsg(Messenger.TRACE, "Web application upgraded");
	}
	
	/**
	 * @throws FatalException 
	 * 
	 */
	protected  void upgrade() throws FatalException {
		try {
			Messenger.printMsg(Messenger.TRACE, "Upgrade to version " + Version.getVersion());
			this.upgradeDistrib();
			this.upgradeWebApps() ;
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			FatalException.throwNewException(SaadaException.INTERNAL_ERROR, e);
		}

	}
}
