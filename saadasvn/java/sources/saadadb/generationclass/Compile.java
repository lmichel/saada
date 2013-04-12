package saadadb.generationclass;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

import saadadb.util.Messenger;

public class Compile{      
  
    public static void compileItWithAnt(String rootDir, String classToCompile) throws Exception{
    	compileItWithAnt(rootDir, classToCompile, Messenger.getOutput(), false);
     }
    public static void compileItWithAnt(String rootDir, String classToCompile, String logDir) throws Exception{
    	compileItWithAnt(rootDir, classToCompile,new PrintStream(new FileOutputStream(logDir + File.separator + classToCompile + ".complog")), true);
     }
 
    public static void compileItWithAnt(String rootDir, String classToCompile, PrintStream printStream, boolean debug) throws Exception{
    	Project p = new Project();
        p.setUserProperty("class.source", classToCompile + ".java");
        p.init();
		ProjectHelper helper = ProjectHelper.getProjectHelper();
		org.apache.tools.ant.DefaultLogger log = new org.apache.tools.ant.DefaultLogger();
		log.setErrorPrintStream(printStream);
		log.setOutputPrintStream(printStream);
		log.setMessageOutputLevel((debug)?Project.MSG_VERBOSE: Project.MSG_INFO);
		p.addBuildListener(log);
		helper.parse(p, new File(rootDir + File.separator + "bin" + File.separator  + "build.xml"));
		Messenger.printMsg(Messenger.TRACE, "Compile class " + classToCompile);
		p.executeTarget("javaclass.compile");
    }
   
//    public static void main(String[] args) throws Exception {
//    	Database.init("ThreeXMM");
//    		Compile.compileItWithAnt(Database.getRoot_dir(), "CUBEUserColl.java");
//    		Compile.compileItWithAnt(Database.getRoot_dir(), "CUBEUserColl.java", "/tmp/comp.log");
//    	}
//
//	
//    public static void compileIt(File sourcefile) throws Exception{
//    	compileIt(sourcefile,Database.getLogDir() );
//    }
//   /** * @version $Id$
//
//     * @param sourcefile
//     * @param classdir
//     * @param log_dir
//     * @throws Exception
//     */
//    public static void compileIt(File sourcefile, String log_dir) throws Exception{
//    	String classpath = "-classpath";
//    	String logfile = log_dir + separ +  sourcefile.getName() + ".comp.log";
//    		PrintWriter out = new PrintWriter( new FileWriter(logfile ) );
//    		String classname = sourcefile.getName();
//    		String classdir =  sourcefile.getParent();
//    		if( classdir.length() > 20 ) 
//    			if( Messenger.debug_mode ) {
//    				if( classdir.length() > 20 )  {
//    					Messenger.printMsg(Messenger.DEBUG, "Compiling: " + classname + " ( -d ..." + classdir.substring(classdir.length() - 20) + ") ");
//    				}
//    				else {
//    	   				Messenger.printMsg(Messenger.DEBUG, "Compiling: " + classname + " ( -d " + classdir + ") "); 					
//    				}
//    			}
//     		
//    		int status =  Main.compile(new String[]{classpath, System.getProperty("java.class.path"), "-d", classdir, "-target", RunProperties.JVM_TARGET, sourcefile.getAbsolutePath()} ,out);
//    		out.close();
//    		if( status == 0 ){
//    			Messenger.printMsg(Messenger.TRACE, " Compilation of "+classname+".java met no problems"); 
//    		}else{
//    			BufferedReader lf;
//    			lf = new BufferedReader(new FileReader(logfile));
//    			String str;
//    			while( (str = lf.readLine()) != null ) {
//    				Messenger.printMsg(Messenger.ERROR, str.toString());  			
//    			}
//    			lf.close();
//    			FatalException.throwNewException(SaadaException.INTERNAL_ERROR, "compilation failed");
//    		}
//     }
}
  
