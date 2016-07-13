/**
 * 
 */
package saadadb.vo;

/**
 * Stores service parameters read in a property file
 * @author michel
 *
 */
public class VoProperties {
	public static final int TAP_retentionPeriod;
	public static final int TAP_executionDuration ;
	public static final int TAP_outputLimit ;
	public static final int TAP_hardLimit ;
	/**
	 */
	static {
		int trp = 3600;
		int ted = 3600;
		int tol = 10000;
		int thl = 10000;
		TAP_retentionPeriod = trp;
		TAP_executionDuration = ted;
		TAP_outputLimit = tol;
		TAP_hardLimit = thl;
	}
}
