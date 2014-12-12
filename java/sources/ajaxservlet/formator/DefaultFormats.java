package ajaxservlet.formator;

import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import javax.servlet.http.HttpServletResponse;

import saadadb.database.Database;
import saadadb.util.Messenger;
import saadadb.util.SaadaConstant;
import saadadb.vocabulary.RegExp;
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
	/** * @version $Id$

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
	 * @param inMjd
	 * @return
	 */
	public static final String getDateFromMJD(double inMjd) {
		int JGREG= 15 + 31*(10+12*1582);
		int jalpha=0,ja,jb,jc,jd,je,year,month,day;
		double julian = inMjd + 2400000.5 + 0.5;
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
	 * @param inMjd1
	 * @param inMjd2
	 * @return
	 */
	public static final String getDateRangeFromMJD(double inMjd1, double inMjd2) {
		int JGREG= 15 + 31*(10+12*1582);
		int jalpha=0,ja,jb,jc,jd,je;
		int year1,month1,day1;
		double julian; 
		String date1, date2;
		String time1, time2;
		if( Double.isNaN(inMjd1) || inMjd1 == SaadaConstant.DOUBLE ) {
			date1 = "NotSet";
			time1 = "";
		} else {
			julian = inMjd1 + 2400000.5 + 0.5;
			ja = (int) julian;
			int reste1 = (int)((julian  - (double)ja) * 86400.0);
			if (ja>= JGREG) {    
				jalpha = (int) (((ja - 1867216) - 0.25) / 36524.25);
				ja = ja + 1 + jalpha - jalpha / 4;
			}

			jb = ja + 1524;
			jc = (int) (6680.0 + ((jb - 2439870) - 122.1) / 365.25);
			jd = 365 * jc + jc / 4;

			je = (int) ((jb - jd) / 30.6001);
			day1 = jb - jd - (int) (30.6001 * je);
			month1 = je - 1;
			if (month1 > 12) month1 = month1 - 12;
			year1 = jc - 4715;
			if (month1 > 2) year1--;
			if (year1 <= 0) year1--;
			int hours1 = reste1 / 3600;
			reste1 = reste1 - hours1*3600;
			int min1 = reste1 / 60;
			reste1 = reste1 - min1*60;
			date1 = day1 + "/" + month1 + "/" + year1;
			time1 = hours1 + ":" + min1 + ":" + reste1;
		}
		if( Double.isNaN(inMjd2) || inMjd2 == SaadaConstant.DOUBLE ) {
			date2 = "NotSet";
			time2 = "";
		} else {

			int year2,month2,day2;
			julian = inMjd2 + 2400000.5 + 0.5;
			ja = (int) julian;
			int reste2 = (int)((julian  - (double)ja) * 86400.0);
			if (ja>= JGREG) {    
				jalpha = (int) (((ja - 1867216) - 0.25) / 36524.25);
				ja = ja + 1 + jalpha - jalpha / 4;
			}

			jb = ja + 1524;
			jc = (int) (6680.0 + ((jb - 2439870) - 122.1) / 365.25);
			jd = 365 * jc + jc / 4;

			je = (int) ((jb - jd) / 30.6001);
			day2 = jb - jd - (int) (30.6001 * je);
			month2 = je - 1;
			if (month2 > 12) month2 = month2 - 12;
			year2 = jc - 4715;
			if (month2 > 2) year2--;
			if (year2 <= 0) year2--;
			int hours2 = reste2 / 3600;
			reste2 = reste2 - hours2*3600;
			int min2 = reste2 / 60;
			reste2 = reste2 - min2*60;
			date2 = day2 + "/" + month2 + "/" + year2;
			time2 = hours2 + ":" + min2 + ":" + reste2;
		}
		if( date1.equals(date2)){
			if( "NotSet".equals(date1)) {
				return "NotSet";
			}
			return  date1 + " " + time1 + " to " +time2;
		} else {
			if( "NotSet".equals(date1)) {
				return "Up to " + date2 + " " + time2;				
			} else if( "NotSet".equals(date2)) {
				return "Rrom " + date2 + " " + time2;				
			} else {
				return date1 + " " + time1 + " to " + date2 + " " + time2;
			}
		}
	}	  

	/**
	 * @param ra
	 * @param dec
	 * @return
	 */
	public static final String getHMSCoord(double ra, double dec) {
		if( Double.isInfinite(ra) || Double.isNaN(ra) || Double.isNaN(dec)|| SaadaConstant.DOUBLE == ra || SaadaConstant.DOUBLE == dec) {
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
			return SaadaConstant.NOTSET;
		} else if( val == 0 || (val < -1e-2 || val > 1e-2) ) {
			return sign + deux.format(val);
		} else {
			return sign + exp.format(val);
		}
	}

}
