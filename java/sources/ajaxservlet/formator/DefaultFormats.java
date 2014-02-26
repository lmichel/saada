package ajaxservlet.formator;

import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import javax.servlet.http.HttpServletResponse;

import saadadb.database.Database;
import saadadb.util.Messenger;
import saadadb.util.RegExp;
import saadadb.util.SaadaConstant;
import cds.astro.Astrocoo;

public class DefaultFormats {
	static private DecimalFormat exp =  new DecimalFormat("0.00E00");
	static private DecimalFormat deux = new DecimalFormat("0.0000");
	static private DecimalFormat six = new DecimalFormat("0.000000");
	
    static {
		deux.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.ENGLISH));
		six.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.ENGLISH));
		exp.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.ENGLISH));	
	}
	/** * @version $Id: DefaultFormats.java 480 2012-07-27 14:20:57Z laurent.mistahl $

	 * @param res
	 * @param message
	 */
	public static void errorPage(HttpServletResponse res, String message) {
		try { 
			PrintWriter out;
			res.setContentType("text/html");
			out = res.getWriter();
			out.println("<HTML>");
			out.println("<p>");
			out.println("<B>ERROR: " + message);
			out.println("</p>");
			out.println("Back to the <A HREF=\"home\">Home Page</A>");
			out.println("</BODY>");
			out.println("</HTML>");

		} catch(Exception ee) {
			Messenger.printStackTrace(ee);
		}
	}

	/**
	 * @param obj
	 * @return
	 */
	public static final String getString(Object obj) {
		 if( obj == null ) {
			return "Not Set";
		}
		else if( obj.getClass().getName().equals("java.lang.Float")) {
			return DefaultFormats.getString(((Float)obj).doubleValue());
		}
		else if( obj.getClass().getName().equals("java.lang.Double") ) {
			return DefaultFormats.getString(((Double)obj).doubleValue());
		}
		else if( obj.getClass().getName().equals("java.lang.Long") ) {
			long v = ((Long)obj).longValue();
			if( v == SaadaConstant.LONG ) {
				return "Not Set";
			} else {
				return obj.toString();
			}
		}
		else if( obj.getClass().getName().equals("java.lang.Integer") ) {
			long v = ((Integer)obj).intValue();
			if( v == SaadaConstant.INT || v == -2147483648) {
				return "Not Set";
			} else {
				return obj.toString();
			}
		}
		else if( obj.getClass().getName().equals("java.lang.String") ) {
			String str = obj.toString();
			if( str.matches(RegExp.URL)  ) {
				String vstr = str;
				if( str.length() > 24 ) {
					vstr = "http://...." + str.substring(str.length()-10);
				}
				return "<A target=\"blank\" title=\"" + str + "\" href=\"" + str + "\">" + vstr + "</A>";
			} else if( str.matches(RegExp.BIBCODE)  ) {
				return "<A target=\"blank\" tilte=\"bibcode\" href=\"http://cdsads.u-strasbg.fr/abs/" + str + "\">" + str + "</A>";
			} else {
				return str;
			}
		} else {
			return obj.toString();
		}
	}

	/**
	 * @param val
	 * @return
	 */
	public static final String getString(double val) {
		if( Double.isInfinite(val) || Double.isNaN(val) ) {
			return "Not Set";
		} else if( val == 0 || (val < -1e-2 || val > 1e-2) ) {
			return deux.format(val);
		} else {
			return exp.format(val);
		}
	}
	
	/**
	 * returns a decimal representation of val with a 1e-6 precision
	 * @param val
	 * @return
	 */
	public static final String getDecimalCoordString(double val) {
		if( Double.isInfinite(val) || Double.isNaN(val) ) {
			return "Not Set";
		} else {
			return six.format(val);
		}
	}

	/**
	 * Converts a Julian day to a calendar date
	 * ref :
	 * Numerical Recipes in C, 2nd ed., Cambridge University Press 1992
	 * @param injulian
	 * @return
	 */
	public static final String getModifiedJulianDate(double injulian) {
		int JGREG= 15 + 31*(10+12*1582);
		int jalpha=0,ja,jb,jc,jd,je,year,month,day;
		double julian = injulian + 2400000.5 + 0.5;
		ja = (int) julian;
		int reste = (int)((julian  - (double)ja) * 86400.0);
		if (ja>= JGREG) {    
			jalpha = (int) (((ja - 1867216) - 0.25) / 36524.25);
			ja = ja + 1 + jalpha - jalpha / 4;
		}

		jb = ja + 1524;
		jc = (int) (6680.0 + ((jb - 2439870) - 122.1) / 365.25);
		jd = 365 * jc + jc / 4;

		je = (int) ((jb - jd) / 30.6001);
		day = jb - jd - (int) (30.6001 * je);
		month = je - 1;
		if (month > 12) month = month - 12;
		year = jc - 4715;
		if (month > 2) year--;
		if (year <= 0) year--;
		int hours = reste / 3600;
		reste = reste - hours*3600;
		int min = reste / 60;
		reste = reste - min*60;
		return day + "/" + month + "/" + year + " " + hours + ":" + min + ":" + reste ;
	}	    
	/**
	 * @param ra
	 * @param dec
	 * @return
	 */
	public static final String getHMSCoord(double ra, double dec) {
		if( Double.isInfinite(ra) || Double.isInfinite(dec)) {
			return "Not Set";
		} else {
			Astrocoo coo =new Astrocoo(Database.getAstroframe(), ra, dec);
			//Coo.setDecimals(4);
			coo.setPrecision(6);
			String radec = coo.toString("s:");
			return (radec);
		}
	}
	/**
	 * @param val
	 * @return
	 */
	public static final String getSignedString(double val) {
		String sign;
		if( val < 0 ) {
			sign = "";
		}
		else sign = "+";
		if( Double.isInfinite(val) || Double.isNaN(val) ) {
			return "not set";
		} else if( val == 0 || (val < -1e-2 || val > 1e-2) ) {
			return sign + deux.format(val);
		} else {
			return sign + exp.format(val);
		}
	}

}
