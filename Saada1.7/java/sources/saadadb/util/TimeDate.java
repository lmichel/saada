/**
 * 
 */
package saadadb.util;

/**
 * @author michel
 * @version $Id$
 *
 */
public class TimeDate {
		/**
		 * @param injulian
		 * @return
		 */
		public static final String getModifiedJulianDate(double injulian) {
			int JGREG= 15 + 31*(10+12*1582);
			int jalpha=0,ja,jb,jc,jd,je,year,month,day;
			double julian = injulian + 2400000.5 + 0.5;
			ja = (int) julian;
			int reste = (int)((julian  - (double)ja) * 86400.0);
			System.out.println(JGREG + " " + julian + " " + reste);
			if (ja>= JGREG) {    
				jalpha = (int) (((ja - 1867216) - 0.25) / 36524.25);
				ja = ja + 1 + jalpha - jalpha / 4;
			}

			jb = ja + 1524;
			jc = (int) (6680.0 + ((jb - 2439870) - 122.1) / 365.25);
			jd = 365 * jc + jc / 4;
			System.out.println(jalpha + " " + ja + " " + jb + " " + jc+ " " + jd);

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

}
