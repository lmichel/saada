package saadadb.query.region.triangule.utils;

/**
 * Class util
 * @author jremy
 * @version $Id$
 *
 */
public class Util {
	
	/**
	 * attribute private static final EQUALGAP
	 */
	private final static double EQUALGAP=0.00000001;
	
	/**
	 * This method allows to compare two double approximatively
	 * @param a : double 
	 * @param b : double
	 * @return boolean : true if there are equal
	 */
	public static boolean compare (double a, double b) {
		double value=Math.abs(a-b);
		if (value<EQUALGAP) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * This method allow to know if the first parameter is superior than the second
	 * @param a : double
	 * @param b : double
	 * @return boolean : true if a is superior
	 */
	public static boolean isSuperior (double a, double b) {
		double value=a-b;
		if (value-EQUALGAP>0) {
			return true;
		}
		else {
			return false;
		}
	}

}
