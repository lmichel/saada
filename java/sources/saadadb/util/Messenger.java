package saadadb.util;
import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;


import saadadb.admintool.AdminTool;
import saadadb.admintool.dialogs.AbortExceptionDialog;
import saadadb.admintool.dialogs.FatalExceptionDialog;
import saadadb.admintool.dialogs.IgnoreExceptionDialog;
import saadadb.admintool.dialogs.QueryExceptionDialog;
import saadadb.admintool.dialogs.StartDialog;
import saadadb.command.ArgsParser;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;


public class Messenger implements Serializable{
	/**
	 * * @version $Id$

	 */
	private static final long serialVersionUID = 1L;
	public static final int DEBUG    = -1 ;
	public static final int TRACE    = 0 ;
	public static final int WARNING  = 1 ;
	public static final int ERROR    = 2 ;
	public static final int INFO     = 3 ;
	public static final int REPORT   = 4 ;
	public static final int NOTIFY   = 5 ;
	public static final int SILENT   = 6 ;

	private static final int COMMAND_LINE = 0;
	private static final int GRAPHIC = 1;
	private static final int SERVLET = 2;

	public static boolean ALWAYS_IGNORE = false;

	public static final int CONTINUE = 0;
	public static final int ABORT = 2;

	public static boolean debug_mode=false;
	public static boolean silent_mode=false;
	protected static PrintStream output;
	protected static String logfile;
	/*
	 * GUI interaction
	 */
	protected static JTextArea    gui_area_output  = null;
	protected static JLabel       gui_label_output = null;
	protected static JProgressBar progress_bar;
	protected static boolean pause = false;
	protected static boolean abort = false;
	private static int mode = COMMAND_LINE;
	private static JFrame frame = null;
	private static StartDialog splash;
	private static AdminTool adminTool;

	/**
	 * @param log_file
	 */
	public static void init(String log_file) {   

		if( log_file == null || log_file.equals("") ) {
			Messenger.useSystemOutput();
		}
		else {
			File f = new File(log_file);
			try {  
				f.createNewFile();
				if( f.canWrite() == false ) {
					Messenger.useSystemOutput();
					Messenger.printMsg(Messenger.ERROR, "Can not redirect output in file " + f.getAbsolutePath());
				}
				else {
					// on commence par changer l'encodage  
					PrintStream ps = new PrintStream(new FileOutputStream(log_file) ,true,"ISO-8859-1"); 
					Messenger.output = ps;
					Messenger.logfile = log_file;
				}
			}
			catch (Exception e) {
				Messenger.printStackTrace(e);
				System.exit(1);
			}
		}
	}    

	/**
	 * 
	 */
	private static void useSystemOutput() {
		try {  
			// on commence par changer l'encodage  
			PrintStream ps = new PrintStream(System.out,true,"ISO-8859-1"); 
			System.setOut(ps); 
		}
		catch (Exception e) {
			Messenger.printStackTrace(e);
			System.exit(1);
		}
		Messenger.output = System.out;
	}
	public static PrintStream getOutput() {
		return output;
	}
	
	/**
	 * @return
	 */
	public static boolean isDebugOn(){
		return debug_mode;
	}
	/**
	 * 
	 */
	public static void switchDebugOn() {
		Messenger.printMsg(Messenger.TRACE, "Debug mode on");
		Messenger.debug_mode = true;
	}
	
	public static void switchDebugOff() {
		Messenger.printMsg(Messenger.TRACE, "Debug mode off");
		Messenger.debug_mode = false;
	}
	
	/**
	 * Return the debug mode as an Messenger {@link ArgsParser} parameter
	 * Used to transmit he debug mode to task spawn by the {@link AdminTool}
	 * @return
	 */
	public static String getDebugParam() {
		return  "-debug=" + Messenger.debug_mode;
	}
	
	/**
	 * @param gui_output The gui_output to set.
	 */
	public static void setGui_area_output(JTextArea gui_output) {
		if( gui_output != null && Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "switch output to a textarea");
		else if( gui_output == null && Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "unplug textarea output");
		Messenger.gui_area_output = gui_output;
	}

	/**
	 * @param gui_output The gui_output to set.
	 */
	public static void setGui_label_output(JLabel gui_output) {
		if( gui_output != null && Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "switch output to a label");
		else if( gui_output == null && Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "unplug label output");
		Messenger.gui_label_output = gui_output;
	}

	/**
	 * @param progress_monitor The progress_monitor to set.
	 */
	public static void setProgress_bar(JProgressBar progress_bar) {
		if( progress_bar != null && Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Connect to progress monitor");
		else if( progress_bar == null && Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Disconnect from progress monitor");
		Messenger.progress_bar = progress_bar;
	}

	/**
	 * Set to null all reference on GUI objects
	 */
	public static void unplugGui() {
		if( Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "unplug gui output");
		Messenger.gui_label_output = null;
		Messenger.gui_area_output = null;
		Messenger.progress_bar = null;
	}


	/**
	 * Display the splash window and redirect outputs to its console frame  
	 * @throws MalformedURLException
	 */
	public static void showSplash() throws MalformedURLException {
		if( splash == null ) {
			splash = (new StartDialog());
			Messenger.setGui_area_output(splash.getConsole());
		}
	}


	/**
	 * Hide splash window and break the graphical gui connection
	 */
	public static void hideSplash() {
		if( splash != null ) {
			splash.setVisible(false);
			Messenger.unplugGui();
			splash = null;
		}

	}
	/**
	 * In this mode {@link FatalException} or {@link AbortException} do not exit 
	 * Only if Jframe not null
	 * @param frame
	 */
	public static void setGraphicMode(JFrame frame) {
		if( frame == null ) {
			Messenger.mode = Messenger.COMMAND_LINE;
			Messenger.frame = null;
		}
		else {
			Messenger.mode = Messenger.GRAPHIC;
			Messenger.frame = frame;			
		}
	}

	/**
	 * In this mode {@link FatalException} or {@link AbortException} do not exit 
	 * @param adminTool
	 */
	public static void setGraphicMode(AdminTool adminTool) {
		if( adminTool == null ) {
			Messenger.mode = Messenger.COMMAND_LINE;
			Messenger.adminTool = null;
		}
		else {
			Messenger.mode = Messenger.GRAPHIC;
			Messenger.adminTool = adminTool;		
		}
	}
	public static void diskAccess() {
		if(Messenger.adminTool != null)
			Messenger.adminTool.diskAccess();
	}
	public static void procAccess() {
		if(Messenger.adminTool != null)
			Messenger.adminTool.procAccess();
	}
	public  static  void dbAccess() {
		if(Messenger.adminTool != null)
			Messenger.adminTool.dbAccess();
	}
	public static void noMoreAccess() {
		if(Messenger.adminTool != null)
			Messenger.adminTool.noMoreAccess();
	}

	/**
	 * In this mode {@link FatalException} or {@link AbortException} do not exit 
	 */
	public static void setServletMode() {
		Messenger.mode = Messenger.SERVLET;	
	}
	/**
	 * In this mode {@link FatalException} or {@link AbortException} do exit 
	 */
	public static void setCommandLineMode() {
		Messenger.mode = Messenger.COMMAND_LINE;	
	}

	/**
	 * Print out the top of the stack without the 2 first lines (getStackTrace and this.printStackTop)
	 */
	public static void printStackTop() {
	      StackTraceElement[] elements = Thread.currentThread().getStackTrace();
	      for(int i=2; i<elements.length; i++) {
	          System.out.println(elements[i]);
	          if( i>5)break;
	      }
		}

	/**
	 * Print out a stdout message with 7 top lines of the stack trace
	 * @param msg
	 */
	public static void printStackTop(String additionnalMessage) {
		System.out.println(additionnalMessage);
	      StackTraceElement[] elements = Thread.currentThread().getStackTrace();
	      for(int i=2; i<elements.length; i++) {
	          System.out.println(elements[i]);
	          if( i>7)break;
	      }
	      System.out.println("");
		}
	/**
	 * Print out a debug message with location in the code
	 * @param msg
	 */
	public static void printLocatedMsg(String msg) {
		String message = msg;
	      StackTraceElement[] elements = Thread.currentThread().getStackTrace();
	      for(int i=2; i<elements.length; ) {
	          message += " " + elements[i] + "";
	          break;
	      }
	      printMsg(DEBUG, message);
		}

	/**
	 * @param level
	 * @param msg
	 */
	public static void printMsg(int level, String msg){
		String string="";
		Date date = new Date();
		String sdate = DateFormat.getDateInstance(DateFormat.SHORT,Locale.FRANCE).format(date);

		sdate += date.toString().substring(10,19);
		if( Messenger.output == null ) {
			Messenger.output = System.out;
		}
		switch( level ) {
		case -1:  string = "[" + sdate + "]   DEBUG: ";
		break;
		case 0:  string = "[" + sdate + "]   TRACE: ";
		break;
		case 1:  string = "[" + sdate + "] WARNING: ";
		break;
		case 2:  string = "[" + sdate + "]   ERROR: ";
		break;
		case 3:  string = "[" + sdate + "]    INFO: ";
		break;
		case 4:  string = "[" + sdate + "]  REPORT: \n";
		break;
		default: string = "[" + sdate + "] LEVEL " + level + ": ";
		}
		/*
		 * Print out the message on various media
		 */
		if( (Messenger.silent_mode && level == ERROR) || 
				(Messenger.silent_mode == false && (Messenger.debug_mode == true || 
						(Messenger.debug_mode == false && level != -1))) ){
			if( level != -1 ) {
				Messenger.printMsgInGui(msg);
			}
			Messenger.output.println(string + msg);
			Messenger.output.flush();
		}
	}

	/**
	 * @param msg
	 */
	private static void printMsgInGui(String msg) {
		if( Messenger.gui_area_output != null ) {
			Messenger.gui_area_output.append(msg + "\n");
			try {
				Rectangle r = Messenger.gui_area_output.modelToView(Messenger.gui_area_output.getDocument().getLength());
				if( r != null ) {
					Messenger.gui_area_output.scrollRectToVisible(r);
				}
				/*
				 * Do not make trouble for a simple display issue
				 * e.g. java.lang.ClassCastException: sun.java2d.NullSurfaceData cannot be cast to sun.java2d.d3d.D3DSurfaceData 
				 */
			} catch (Exception e) {
			}
		}
		else if( Messenger.gui_label_output != null ) {
			Messenger.gui_label_output.setText(msg);
		}
	}

	public static void setProgress(int val) {
		if( Messenger.progress_bar != null && val < Messenger.progress_bar.getMaximum()) {
			Messenger.progress_bar.setValue(val);
		}
	}

	public static void incrementeProgress() {
		if( Messenger.progress_bar != null) {
			SwingUtilities.invokeLater(new Runnable() {

				public void run() {
					int val = Messenger.progress_bar.getValue() + 1;
					Messenger.progress_bar.setValue(val);
				}

			});
		}
	}
	public static void setMaxProgress(int val) {
		if( Messenger.progress_bar != null ) {
			Messenger.progress_bar.setMaximum(val);
		}
	}
	public synchronized static void requestPause()   {
		Messenger.printMsg(Messenger.TRACE, "Pause requested: Effective at the next check point");
		Messenger.pause = true;
	}
	public static boolean pauseRequested() {
		return Messenger.pause;
	}

	public synchronized static void requestAbort()   {
		Messenger.printMsg(Messenger.TRACE, "Abort requested: Effective at the next check point");
		Messenger.abort = true;
	}

	public static boolean abortRequested() {
		return Messenger.abort;
	}

	public synchronized static void requestResume() {
		Messenger.printMsg(Messenger.TRACE, "Resume requested");
		Messenger.pause = false;
	}

	public static void resetUserRequests() {
		Messenger.abort = false;
		Messenger.pause = false;
	}

	/**
	 * @param string
	 */
	public static void openLog(String string) {
		try {
			output = new PrintStream(new File(string));
		} catch (IOException e) {
			Messenger.printStackTrace(e);
			output = System.out;
		}	
	}

	/**
	 * 
	 */
	public static void closeLog() {
		if( output != null ) {
			output.close();
		}
		output = System.out;
	}

	/**
	 * @param e
	 */
	public static void printStackTrace(Exception e) {
		printMsg(ERROR, e.toString());
		if( output != null ) {
			e.printStackTrace(output);
		}
		else {
			e.printStackTrace();
		}
	}

	/**
	 * @param ie
	 * @return
	 */
	public static void trapQueryException(QueryException e) {		
		if( Messenger.mode == Messenger.COMMAND_LINE ) {
			Messenger.printMsg(Messenger.ERROR, e.toString());
			Messenger.printMsg(Messenger.ERROR, "Program exits on fatal error.");
			System.exit(1);
		}
		else if( Messenger.mode == Messenger.GRAPHIC ) {
			Messenger.printMsg(Messenger.ERROR, e.toString());
			new QueryExceptionDialog(Messenger.frame, e);
		}
	}

	/**
	 * @param ie
	 * @return
	 */
	public static void trapAbortException(AbortException e) {		
		if( Messenger.mode == Messenger.COMMAND_LINE ) {
			Messenger.printMsg(Messenger.ERROR, e.toString());
			Messenger.printMsg(Messenger.ERROR, "Program exits on fatal error.");
			System.exit(1);
		}
		else if( Messenger.mode == Messenger.GRAPHIC ) {
			Messenger.printMsg(Messenger.ERROR, e.toString());
			new AbortExceptionDialog(Messenger.frame, e);
		}
	}
	/**
	 * @param ie
	 * @return
	 */
	public static void trapFatalException(SaadaException e) {		
		if( Messenger.mode == Messenger.COMMAND_LINE ) {
			Messenger.printMsg(Messenger.ERROR, e.toString());
			Messenger.printMsg(Messenger.ERROR, "Program exits on fatal error.");
			System.exit(1);
		}
		else if( Messenger.mode == Messenger.GRAPHIC ) {
			Messenger.printMsg(Messenger.ERROR, e.toString());
			new FatalExceptionDialog(Messenger.frame, e);
		}
	}


	/**
	 * @param ie
	 * @return
	 */
	public static int trapIgnoreException(IgnoreException e) {
		if( IgnoreException.isIgnored(e)) {
			return Messenger.CONTINUE;
		}
		else {
			return Messenger.promptUser(e);
		}
	}
	

	/**
	 * 
	 */
	private static int promptUser(IgnoreException ie) {
		if( Messenger.mode == Messenger.COMMAND_LINE ) {
			InputStreamReader sr = new InputStreamReader(System.in);
			BufferedReader br    = new BufferedReader(sr);
			try {
				System.out.println(ie);
				if( Messenger.ALWAYS_IGNORE ) {
					Messenger.printMsg(Messenger.TRACE, "Ignored: continue mode is on");
					return Messenger.CONTINUE;
				}
				else {
					String reply = "";
					while( !reply.matches("[c|i|a]")) {
						System.out.print("Continue, Always Ignore or Abort (c|i|a) ");
						reply  = br.readLine();
					}
					if( reply.equalsIgnoreCase("c") ) {
						return Messenger.CONTINUE;
					}
					else if( reply.equalsIgnoreCase("i") ) {
						IgnoreException.mustIgnore(ie);
						return Messenger.CONTINUE;
					}
					else {
						return Messenger.ABORT;
					}
				}
			} catch (IOException e) {
				Messenger.printStackTrace(e);
				System.exit(1);
			}
		}
		else if( mode == GRAPHIC ) {
			IgnoreExceptionDialog ied = new IgnoreExceptionDialog(Messenger.frame, ie);
			return ied.getAction();

		}
		return Messenger.ABORT;
	}
}

