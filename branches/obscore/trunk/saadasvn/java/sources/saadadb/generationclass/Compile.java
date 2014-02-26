package saadadb.generationclass;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

import saadadb.util.Messenger;

/**
 * Compile a java class which both source an bin directories are class_mapping
 * The compilation is done by the ant task located in SAADA_DB_HOME/bin/build.xml
 * Ant target:
 * 	<target name="javaclass.compile">
     	<javac fork="true"  debug="on" 
		       target="${javac.target}" 
		       source="${javac.target}" 
		       srcdir="${SAADA_DB_HOME}/class_mapping" 
			   destdir="${SAADA_DB_HOME}/class_mapping"
			   includes="${class.source}">
            <classpath refid="saadadb.classpath"/>
		</javac>
	</target>
 *
 * @author michel
 * @version $Id$
 *
 */
public class Compile{      
  
    /**
     * Compile the class classToCompile with an output merged to Messenger streams.
     * Compilation log is minimal.
     * The saadadbHome is specified is passed as parameter because this method ca be called 
     * at database creation time
     * @param saadadbHome
     * @param classToCompile
     * @throws Exception
     */
    public static void compileItWithAnt(String saadadbHome, String classToCompile) throws Exception{
    	compileItWithAnt(saadadbHome, classToCompile, Messenger.getOutput(), false);
     }
    /**
     * Compile the class classToCompile with an output logged in a filer located in logDir.
     * Compilation log is talkative. Log file is classToCompile.complog
     * The saadadbHome is specifier is passed as parameter because this method ca be called 
     * at database creation time
     * @param saadadbHome
     * @param classToCompile
     * @param logDir
     * @throws Exception
     */
    public static void compileItWithAnt(String saadadbHome, String classToCompile, String logDir) throws Exception{
    	compileItWithAnt(saadadbHome, classToCompile,new PrintStream(new FileOutputStream(logDir + File.separator + classToCompile + ".complog")), true);
     }
 
    /**
     * Ivoke the ant task javaclass.compile to compile the class classToCompile.
     * Log messaged are pushed into the printStream. Log are detailed if debug is true
     * @param saadadbHome
     * @param classToCompile
     * @param printStream
     * @param debug
     * @throws Exception
     */
    private static void compileItWithAnt(String saadadbHome, String classToCompile, PrintStream printStream, boolean debug) throws Exception{
    	Project p = new Project();
        p.setUserProperty("class.source", classToCompile + ".java");
        p.init();
		ProjectHelper helper = ProjectHelper.getProjectHelper();
		org.apache.tools.ant.DefaultLogger log = new org.apache.tools.ant.DefaultLogger();
		log.setErrorPrintStream(printStream);
		log.setOutputPrintStream(printStream);
		log.setMessageOutputLevel((debug)?Project.MSG_VERBOSE: Project.MSG_INFO);
		p.addBuildListener(log);
		helper.parse(p, new File(saadadbHome + File.separator + "bin" + File.separator  + "build.xml"));
		Messenger.printMsg(Messenger.TRACE, "Compile class " + classToCompile);
		p.executeTarget("javaclass.compile");
    }
}
  
