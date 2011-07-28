package saadadb.query.executor;



/**
 * @author michel
 *
 */
public class Query_Report {
	protected Report report ;
	
	/**
	 * @param report
	 */
	public Query_Report(Report report) {
		if( report == null ) {
			this.setReport(new Report());
		}
		else {
			this.setReport(report);
		}
	}
	/**
	 * @return Returns the report.
	 */
	public String getExecReport() {
		return this.report.getExecReport();
	}
	
	/**
	 * @return Returns the report.
	 */
	public String getErrorReport() {
		return this.report.getErrorReport();
	}
	
	/**
	 * @return Returns the report.
	 */
	public Report getReport() {
		return this.report;
	}
	/**
	 * @param sentence
	 */
	public void addSentence(String sentence) {
		this.report.addSentence(sentence);
	}
	/**
	 * @param sentence
	 */
	public void addError(String sentence) {
      	this.report.addError(sentence);
	}
	
	/**
	 * 
	 */
	public void isDone() {
		this.report.isDone();
	}
	public void setReport(Report report) {
		this.report = report;
	}
}
