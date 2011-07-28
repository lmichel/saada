package saadadb.query.executor;

import saadadb.util.Messenger;
import saadadb.util.TimeSaada;

/**
 * @author michel
 *
 */
public class Report {
	private String exec_report="";
	private String error_report="";
  	private TimeSaada time = new TimeSaada();

	/**
	 * @return Returns the report.
	 */
	public String getExecReport() {
		return exec_report;
	}
	
	/**
	 * @return Returns the report.
	 */
	public String getErrorReport() {
		return error_report;
	}
	
	/**
	 * @return Returns the report.
	 */
	public String getReport() {
		String retour = "";
		if( this.exec_report != null && !this.exec_report.equals("") ) {
			retour += exec_report;
		}
		if( this.error_report != null && !this.error_report.equals("") ) {
			retour += "\nERRORS\n" + error_report;
		}
		return retour;
	}
	/**
	 * @param sentence
	 */
	public void addSentence(String sentence) {
		if( sentence != null ) {
			time = new TimeSaada();		
			this.exec_report += sentence;
			if( !sentence.endsWith("\n") ) {
				this.exec_report += "\n";
			}
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, sentence);
		}
	}
	/**
	 * @param sentence
	 */
	public void addError(String sentence) {
		if( sentence != null ) {
			time = new TimeSaada();		 
			this.error_report += sentence;
			if( !sentence.endsWith("\n") ) {
				this.error_report += "\n";
			}
			Messenger.printMsg(Messenger.ERROR, sentence);
		}
	}

	public void isDone() {
	   time.stop();
		this.addSentence( "Done in " + time.check()+" ms");
	    this.exec_report += "Done in " + time.check()+" ms\n";
		
	}
	
}
