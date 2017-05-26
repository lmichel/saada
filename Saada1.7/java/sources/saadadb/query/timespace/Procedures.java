/**
 * 
 */
package saadadb.query.timespace;

/**
 * A set of geometric procedures which are the counterpart of the SQL embedded procedures (ised by PSQL and MySQL)
 * These procedures are used by the JAVA UDP of SQLite
 * The PSQL code is reported on the comment block of each procedure
 * @author michel
 * @version $Id$
 *
 */
public class Procedures {

	/**
	CREATE FUNCTION corner00_ra(ra DOUBLE ,de DOUBLE ,size_ra DOUBLE ,size_de DOUBLE ) RETURNS DOUBLE  DETERMINISTIC
	BEGIN
	DECLARE cdec DOUBLE DEFAULT corner00_dec(de, size_de);
	DECLARE d DOUBLE DEFAULT 0;
		-- bottom border pass on the pole: take the top border
		IF cdec > 89.99 OR cdec < -89.99 THEN
			IF size_de > 0.015 THEN
				RETURN corner01_ra(ra, de, size_ra, size_de);
			-- Don't know what to do here with a squashed box
			ELSE
				RETURN ra;
			END IF;
		ELSE
			SET  d = (ra - (size_ra/2)/ABS(COS(RADIANS(cdec))));
	  		IF d < 0 THEN
	    	RETURN (360. + d);
	  	ELSE
	    	RETURN d;
	 	 END IF;
	  END IF;
	END;
	 *
	 *
	 * @param ra in degrees
	 * @param de in degrees
	 * @param size_ra in degrees
	 * @param size_de in degrees
	 * @return
	 */
	public static final double corner00_ra(double ra, double de, double size_ra, double size_de){
		double cdec = corner00_dec(de, size_de);
		/* bottom border pass on the pole: take the top border */
		if( cdec > 89.99 || cdec < -89.99 ) {
			if( size_de > 0.015 ) {
				return corner01_ra(ra, de, size_ra, size_de);
			}
			/*  Don't know what to do here with a squashed box */
			else {
				return ra;
			}
		} else {
			double d = (ra - (size_ra/2)/Math.abs(Math.cos(Math.toRadians(cdec))));
			if( d < 0 ) {
				return  (360 + d);
			} else {
				return d;
			}
		}
	}
	
	/**
	CREATE  FUNCTION corner00_dec(de DOUBLE ,size_de DOUBLE ) RETURNS DOUBLE  DETERMINISTIC
	BEGIN
	DECLARE d DOUBLE DEFAULT (de - (size_de/2));
	  IF d < -90 THEN
	    RETURN (-180. - d);
	  ELSE
	    RETURN d;
	  END IF;
	END;
	 *
	 *
	 * @param de  in degrees
	 * @param size_de in degrees
	 * @return
	 */
	public static final double corner00_dec(double de, double size_de){
		double d = de - (size_de/2);
		if( d < -90 ) {
			return (-180. - d);
		} else {
			return(d);
		}
	}

	/**
	CREATE  FUNCTION corner01_ra(ra DOUBLE ,de DOUBLE ,size_ra DOUBLE ,size_de DOUBLE ) RETURNS DOUBLE  DETERMINISTIC
	BEGIN
	DECLARE cdec DOUBLE DEFAULT corner01_dec(de, size_de);
	DECLARE d DOUBLE DEFAULT 0;
		-- bottom border pass on the pole: take the top border
		IF cdec > 89.99 OR cdec < -89.99 THEN
			IF size_de > 0.015 THEN
				RETURN corner00_ra(ra, de, size_ra, size_de);
			-- Don't know what to do here with a squashed box
			ELSE
				RETURN ra;
			END IF;
		ELSE
			SET  d = (ra - (size_ra/2)/ABS(COS(RADIANS(cdec))));
	  		IF d < 0 THEN
	    		RETURN (360. + d);
	  		ELSE
	    		RETURN d;
	  		END IF;
	  	END IF;
	END;
	 *
	 *
	 * @param ra in degrees
	 * @param de in degrees
	 * @param size_ra in degrees
	 * @param size_de in degrees
	 * @return
	 */
	public static double corner01_ra(double ra, double de, double size_ra, double size_de){
		double cdec = corner01_dec(de, size_de);
		/* bottom border pass on the pole: take the top border */
		if( cdec > 89.99 || cdec < -89.99 ) {
			if( size_de > 0.015 ) {
				return corner00_ra(ra, de, size_ra, size_de);
			}
			/* Don't know what to do here with a squashed box */
			else {
				return ra;
			}
		} else {
			double d = (ra - (size_ra/2)/Math.abs(Math.cos(Math.toRadians(cdec))));
			if( d < 0 ) {
				return  (360 + d);
			} else {
				return d;
			}
		}
	}
	
	/**
	 CREATE FUNCTION corner01_dec(de  DOUBLE ,size_de DOUBLE ) RETURNS DOUBLE  DETERMINISTIC
	BEGIN
	DECLARE d DOUBLE DEFAULT (de + (size_de/2));
	  IF d > 90 THEN
	    RETURN (180. - d);
	  ELSE
	    RETURN d;
	  END IF;
	END;
	 *
	 *
	 * @param de in degrees
	 * @param size_de in degrees
	 * @return
	 */
	public static final double corner01_dec(double de, double size_de){
		double d = de + (size_de/2);
		if( d > 90 ) {
			return (180. - d);
		}else {
			return(d);
		}
	}

	/**
	CREATE  FUNCTION corner10_ra(ra DOUBLE ,de DOUBLE ,size_ra DOUBLE ,size_de DOUBLE ) RETURNS DOUBLE  DETERMINISTIC
	BEGIN
	DECLARE cdec DOUBLE DEFAULT corner10_dec(de, size_de);
	DECLARE d DOUBLE DEFAULT 0;
		-- bottom border pass on the pole: take the top border
		IF cdec > 89.99 OR cdec < -89.99 THEN
			IF size_de > 0.015 THEN
				RETURN corner11_ra(ra, de, size_ra, size_de);
			-- Don't know what to do here with a squashed box
			ELSE
				RETURN ra;
			END IF;
		ELSE
			SET d = (ra + (size_ra/2)/ABS(COS(RADIANS(cdec))));
	  		IF d > 360 THEN
	    		RETURN (d - 360.);
	  		ELSE
	    		RETURN d;
	  		END IF;
	  	END IF;
	END;
	 *
	 *
	 * @param ra in  degrees
	 * @param de in  degrees
	 * @param size_ra in  degrees
	 * @param size_de in  degrees
	 * @return
	 */
	public static final double corner10_ra(double ra, double de, double size_ra, double size_de){
		double cdec = corner10_dec(de, size_de);
		/* bottom border pass on the pole: take the top border */
		if( cdec > 89.99 || cdec < -89.99 ) {
			if( size_de > 0.015 ) {
				return corner11_ra(ra, de, size_ra, size_de);
			}
			/* Don't know what to do here with a squashed box*/
			else{
				return ra;
			}
		} else {
			double d = (ra + (size_ra/2)/Math.abs(Math.cos(Math.toRadians(cdec))));
			if( d > 360.0 ) {
				return  (d - 360.0);
			} else {
				return d;
			}
		}
	}

	/**
	CREATE FUNCTION corner10_dec(de DOUBLE ,size_de DOUBLE ) RETURNS DOUBLE  DETERMINISTIC
	BEGIN
	DECLARE d DOUBLE DEFAULT (de - (size_de/2));
	  RETURN corner00_dec(de, size_de);
	END;
	 *
	 *
	 * @param de
	 * @param size_de
	 * @return
	 */
	public static final double corner10_dec(double de, double size_de){
		return corner00_dec(de, size_de);
	}

	/**
	CREATE  FUNCTION corner11_ra(ra DOUBLE ,de DOUBLE ,size_ra DOUBLE ,size_de DOUBLE ) RETURNS DOUBLE  DETERMINISTIC
	BEGIN
	DECLARE cdec DOUBLE DEFAULT corner11_dec(de, size_de);
	DECLARE d DOUBLE DEFAULT 0 ;
		-- bottom border pass on the pole: take the top border
		IF cdec > 89.99 OR cdec < -89.99 THEN
			IF size_de > 0.015 THEN
				RETURN corner10_ra(ra, de, size_ra, size_de);
			-- Don't know what to do here with a squashed box
			ELSE
				RETURN ra;
			END IF;
		ELSE
			SET d = (ra + (size_ra/2)/ABS(COS(RADIANS(cdec))));
			IF d > 360 THEN
				RETURN (d - 360.);
			ELSE
				RETURN d;
			END IF;
		END IF;
	END;

	 *
	 *
	 * @param ra
	 * @param de
	 * @param size_ra
	 * @param size_de
	 * @return
	 */
	public static double corner11_ra(double ra, double de, double size_ra, double size_de){
		double cdec = corner11_dec(de, size_de);
		/* bottom border pass on the pole: take the top border */
		if( cdec > 89.99 || cdec < -89.99  ){
			if( size_de > 0.015 ) {
				return corner10_ra(ra, de, size_ra, size_de);
			}
			/** Don't know what to do here with a squashed box */
			else {
				return ra;
			}
		} else {
			double d = (ra + (size_ra/2)/Math.abs(Math.cos(Math.toRadians(cdec))));
			if( d > 360.0 ) {
				return  (d - 360.0);
			} else {
				return d;
			}
		}
	}
	/**
	 CREATE  FUNCTION corner11_dec(de DOUBLE ,size_de DOUBLE ) RETURNS DOUBLE  DETERMINISTIC
	BEGIN
	DECLARE d DOUBLE PRECISION DEFAULT (de - (size_de/2));
	  RETURN corner01_dec(de, size_de);
	END;
	 *
	 *
	 * @param de in degrees
	 * @param size_de in degrees
	 * @return
	 */
	public final static double corner11_dec(double de, double size_de){
		return corner01_dec(de, size_de);
	}

	/**
	 CREATE  FUNCTION distancedegree(ra0 DOUBLE , de0 DOUBLE , ra1 DOUBLE , de1 DOUBLE ) RETURNS DOUBLE  DETERMINISTIC
	BEGIN
	DECLARE rde0 DOUBLE  DEFAULT 0 ;
	DECLARE rde1 DOUBLE  DEFAULT 0 ;
		SET rde0 = radians(de0);
		SET rde1 = radians(de1);
		RETURN degrees(acos((sin(rde0)*sin(rde1)) +
	                     (cos(rde0)*cos(rde1) * cos(radians(ra0)-radians(ra1)))));
	END
	 */
	public final static double distancedegree(double ra0, double de0, double ra1, double de1){
		double  rde0 = Math.toRadians(de0);
		double  rde1 = Math.toRadians(de1);
		return Math.toRadians(Math.acos((Math.sin(rde0)*Math.sin(rde1)) +
				(Math.cos(rde0)*Math.cos(rde1) * Math.cos(Math.toRadians(ra0)-Math.toRadians(ra1)))));
	}
	
	/**
	 CREATE  FUNCTION tileleftborder(ra_center DOUBLE , de_center DOUBLE , width DOUBLE , de DOUBLE ) RETURNS DOUBLE  DETERMINISTIC
	BEGIN
	DECLARE delta DOUBLE  DEFAULT 2*(de - de_center);
	  IF delta < 0 THEN
	    RETURN corner00_ra(ra_center, de_center, width, -delta);
	  ELSE
	    RETURN corner01_ra(ra_center, de_center, width, delta);
	  END IF;
	END
	 *
	 *
	 * @param ra_center in degrees
	 * @param de_center in degrees
	 * @param width in degrees
	 * @param de in degrees
	 * @return
	 */
	public static final double tileleftborder(double ra_center, double de_center, double width, double de){
		double delta = 2*(de - de_center);
		if( delta < 0 ) {
			return corner00_ra(ra_center, de_center, width, -delta);
		} else {
			return corner01_ra(ra_center, de_center, width, delta);
		}
	}
	/**
	 CREATE FUNCTION tilerightborder(ra_center DOUBLE , de_center DOUBLE , width DOUBLE , de DOUBLE ) RETURNS DOUBLE   DETERMINISTIC
	BEGIN
	DECLARE delta DOUBLE  DEFAULT 2*(de - de_center);
	  IF delta < 0 THEN
	    RETURN corner10_ra(ra_center, de_center, width, -delta);
	  ELSE
	    RETURN corner11_ra(ra_center, de_center, width, delta);
	  END IF;
	END
	 *
	 *
	 * @param ra_center in degrees
	 * @param de_center in degrees
	 * @param width in degrees
	 * @param de in degrees
	 * @return
	 */
	public static final double tilerightborder(double ra_center, double de_center, double width, double de){
		double delta = 2*(de - de_center);
		if( delta < 0 ) {
			return corner10_ra(ra_center, de_center, width, -delta);
		}
		else {
			return corner11_ra(ra_center, de_center, width, delta);
		}
	}

	/**
	CREATE  FUNCTION isinbox(ra_box DOUBLE , de_box DOUBLE , size_ra DOUBLE , size_de DOUBLE , ra DOUBLE , de DOUBLE ) RETURNS BOOLEAN DETERMINISTIC
	BEGIN
	DECLARE width DOUBLE  DEFAULT 0;
	DECLARE tleftb DOUBLE  DEFAULT 0;
	DECLARE trightb DOUBLE  DEFAULT 0;
		-- Circle approximation near the poles
		IF de > 88 OR de < -88 THEN
			IF distancedegree(ra_box, debox,ra, de ) < (size_ra + size_de)/2 THEN
				RETURN TRUE;
			ELSE
				RETURN FALSE;
			END IF;
	  	ELSEIF de >  corner01_dec(de_box, size_de) THEN
	     	RETURN FALSE;
	  	ELSEIF de <  corner00_dec(de_box, size_de) THEN
	    	RETURN FALSE;
	  	END IF;
	  	SET tleftb  = tileleftborder(ra_box, de_box, size_ra, de);
	  	SET trightb = tilerightborder(ra_box, de_box, size_ra, de);
	  	SET width = distancedegree(tleftb, de, trightb, de);
	  	IF distancedegree(tleftb, de, ra,de) < width AND distancedegree(trightb, de, ra,de) < width THEN
	    	RETURN TRUE;
	  	ELSE
	    	RETURN FALSE;
	 	END IF;
	END;
	 *
	 */
	/**
	 * @param ra_box in degrees
	 * @param de_box in degrees
	 * @param size_ra in degrees
	 * @param size_de in degrees
	 * @param ra in degrees
	 * @param de in degrees
	 * @return
	 */
	public static final int isinbox(double ra_box, double de_box, double size_ra, double size_de, double ra, double de){
		if( de > 88 || de < -88 ) {
			if( distancedegree(ra_box, de_box,ra, de ) < (size_ra + size_de)/2 ) {
				return 1;
			} else {
				return 0;
			}
		} else if(  de >  corner01_dec(de_box, size_de) ||  de <  corner00_dec(de_box, size_de) ) {
			return 0;
		} else {
			double tleftb  = tileleftborder(ra_box, de_box, size_ra, de);
			double trightb = tilerightborder(ra_box, de_box, size_ra, de);
			double width = distancedegree(tleftb, de, trightb, de);
			if(  distancedegree(tleftb, de, ra,de) < width &&  distancedegree(trightb, de, ra,de) < width ) {
				return 1;
			} else {
				return 0;
			}
		}
	}

	/**
	 * CREATE FUNCTION getmjd (x DOUBLE) RETURNS TEXT DETERMINISTIC
	BEGIN
	DECLARE JGREG  DOUBLE PRECISION DEFAULT (15 + 31*(10+12*1582));
	DECLARE jalpha INTEGER;
	DECLARE ja INTEGER ;
	DECLARE jb INTEGER;
	DECLARE jc INTEGER;
	DECLARE jd INTEGER;
	DECLARE je INTEGER;
	DECLARE year INTEGER;
	DECLARE month INTEGER;
	DECLARE day INTEGER;
	DECLARE hours INTEGER;
	DECLARE min INTEGER;
	DECLARE	julian  DOUBLE PRECISION ;
	DECLARE reste INTEGER  ;
	  SET julian = (x + 2400000.5 + 0.5);
	  SET ja     = FLOOR(julian);
	  SET reste  = FLOOR(((julian  - ja) * 86400.0));
	  IF ja >  JGREG THEN
	    SET jalpha = FLOOR((((ja - 1867216) - 0.25) / 36524.25) );
	    SET ja = ja + 1 + jalpha - FLOOR(jalpha / 4);
	  END IF	;
	  SET jb  = ja + 1524;
	  SET jc  = FLOOR((6680.0 + ((jb - 2439870) - 122.1) / 365.25));
	  SET jd  = FLOOR((365 * jc) + (jc / 4));
	  SET je  = FLOOR(((jb - jd) / 30.6001));
	  SET day = jb - jd - FLOOR((30.6001 * je) );
	  SET month = je - 1;
	  IF month > 12 THEN
	    SET month = month - 12;
	  END IF	;
	  SET year = jc - 4715;
	  IF month > 2  THEN
	    SET year = year-1;
	  END IF;
	  IF year <= 0 THEN
	    SET year = year-1;
	  END IF;
	  SET hours = FLOOR(reste/3600);
	  SET reste = reste - (hours*3600);
	  SET min   = FLOOR(reste/60);
	  SET reste = reste - min*60;
	  RETURN CONCAT(day , '/' , month , '/' , year , ' ' , hours , ':' , min , ':' , reste);
	END
	 */
	public static final String getmjd(double x){
		double JGREG  = (15 + 31*(10+12*1582));
		double julian = (x + 2400000.5 + 0.5);
		double ja     = Math.floor(julian);
		int reste  = (int) Math.floor(((julian  - ja) * 86400));
		if( ja >  JGREG ) {
			double jalpha = Math.floor((((ja - 1867216) - 0.25) / 36524.25) );
			ja = ja + 1 + jalpha - Math.floor(jalpha / 4);
		}
		double jb  = ja + 1524;
		double jc  = Math.floor((6680.0 + ((jb - 2439870) - 122.1) / 365.25));
		double jd  = Math.floor((365 * jc) + (jc / 4));
		double je  = Math.floor(((jb - jd) / 30.6001));
		int day = (int) (jb - jd - Math.floor((30.6001 * je) ));
		int month = (int) (je - 1);
		if( month > 12 ){
			month = month - 12;
		}
		int year = (int) (jc - 4715);
		if( month > 2  ){
			year = year-1;
		}
		if( year <= 0 ) {
			year = year-1;
		}
		int  hours = (int) Math.floor(reste/3600);
		reste = reste - (hours*3600);
		int min   = (int) Math.floor(reste/60);
		reste = reste - min*60;
		return day + "/" + month+ "/" + year+ " " +hours + ":" + min + ":" +  reste;
//		sprintf(retour, "%d/%d/%d %d:%d:%d", day, month,year,hours, min, reste );
//		return retour;
	}

	/**
	 -- TRUE if center of the box(ra_roi, deroi, roi_size) is enclsosed in the box (ra_box, debox, box_size_ra, box_size_dec)
	-- circle approximation near the poles
	-- angles are in degrees
	CREATE  FUNCTION boxcenter(ra_roi DOUBLE , deroi DOUBLE , roi_size DOUBLE ,ra_box DOUBLE , debox DOUBLE , box_size_ra DOUBLE , box_size_dec DOUBLE ) RETURNS BOOLEAN DETERMINISTIC
	BEGIN
	DECLARE combinated_box_size DOUBLE  DEFAULT 0;
		-- Circle approximation near the poles
		IF (deroi + roi_size) > 88.0 OR (deroi - roi_size) < -88.0 THEN
			IF box_size_dec < box_size_ra THEN
				SET combinated_box_size = box_size_dec;
			ELSE
				SET combinated_box_size = box_size_ra;
			END IF;
			SET combinated_box_size = roi_size/2;
			IF distancedegree(ra_roi, deroi,ra_box, debox) < combinated_box_size THEN
				RETURN TRUE;
			ELSE
				RETURN FALSE;
			END IF;
		-- IVOA/SIAP like center condition
		ELSE
			RETURN isinbox(ra_box, debox, roi_size, roi_size, ra_roi, deroi);
		END IF;

	END;
	 *
	 *
	 * @param ra_roi in degrees 
	 * @param deroi in degrees 
	 * @param roi_size in degrees 
	 * @param ra_box in degrees 
	 * @param debox in degrees 
	 * @param box_size_ra in degrees 
	 * @param box_size_dec in degrees 
	 * @return
	 */
	public static final int boxcenter(double ra_roi, double deroi, double roi_size, double ra_box, double de_box, double box_size_ra, double box_size_dec){
		/* Circle approximation near the poles */
		if( (deroi + roi_size) > 88.0 || (deroi - roi_size) < -88.0 ){
			double combinated_box_size;
			if( box_size_dec < box_size_ra ) {
				combinated_box_size = box_size_dec;
			} else {
				combinated_box_size = box_size_ra;
			}
			combinated_box_size = roi_size/2;
			if( distancedegree(ra_roi, deroi,ra_box, de_box) < combinated_box_size ) {
				return 1;
			} else {
				return 0;
			}
		}
		/* IVOA/SIAP like center condition */
		else {
			return isinbox(ra_box, de_box, roi_size, roi_size, ra_roi, deroi);
		}
	}
	
	/**
	 -- TRUE if  the box(ra_roi, deroi, roi_size) is enclsosed in the box (ra_box, debox, box_size_ra, box_size_dec)
	-- circle approximation near the poles
	-- angles are in degrees
	CREATE   FUNCTION boxcovers(ra_roi DOUBLE , deroi DOUBLE , roi_size DOUBLE ,ra_box DOUBLE , debox DOUBLE , box_size_ra DOUBLE , box_size_dec DOUBLE ) RETURNS BOOLEAN DETERMINISTIC
	BEGIN
	DECLARE combinated_box_size DOUBLE  DEFAULT 0;
		-- ROI mut be smaller than the image
		IF roi_size > box_size_ra OR roi_size > box_size_dec THEN
			RETURN FALSE;
		-- Circle approximation near the poles
		ELSEIF (deroi + roi_size) > 88.0 OR (deroi - roi_size) < -88.0 THEN
			IF box_size_dec < box_size_ra THEN
				SET combinated_box_size = box_size_dec;
			ELSE
				SET combinated_box_size = box_size_ra;
			END IF;
			SET combinated_box_size = (combinated_box_size - roi_size)/2;
			IF distancedegree(ra_roi, deroi,ra_box, debox) < combinated_box_size THEN
				RETURN TRUE;
			ELSE
				RETURN FALSE;
			END IF;
		-- IVOA/SIAP like covers condition
		ELSE
			RETURN isinbox(ra_box, debox, (box_size_ra - roi_size), (box_size_dec - roi_size), ra_roi, deroi);
		END IF;

	END;
	 *
	 *
	 * @param ra_roi in degrees
	 * @param deroi in degrees
	 * @param roi_size in degrees
	 * @param ra_box in degrees
	 * @param de_box in degrees
	 * @param box_size_ra in degrees
	 * @param box_size_dec in degrees
	 * @return
	 */
	public static final int boxcovers(double ra_roi, double deroi, double roi_size, double ra_box, double de_box, double box_size_ra, double box_size_dec){
		/* ROI must be smaller than the image */
		if(  roi_size > box_size_ra || roi_size > box_size_dec ) {
			return 0;
		}
		/*- Circle approximation near the poles */
		else if( (deroi + roi_size) > 88.0 || (deroi - roi_size) < -88.0 ) {
			double combinated_box_size;
			if(  box_size_dec < box_size_ra ) {
				combinated_box_size = box_size_dec;
			} else {
				combinated_box_size = box_size_ra;
			}
			combinated_box_size = (combinated_box_size - roi_size)/2;
			if( distancedegree(ra_roi, deroi,ra_box, de_box) < combinated_box_size ) {
				return 1;
			} else {
				return 0;
			}
		}
		/* IVOA/SIAP like covers condition */
		else {
			return isinbox(ra_box, de_box, (box_size_ra - roi_size), (box_size_dec - roi_size), ra_roi, deroi);
		}
	}

	/**
	 * -- TRUE if  the box(ra_roi, deroi, roi_size)  enclsosed  the box (ra_box, debox, box_size_ra, box_size_dec)
	-- circle approximation near the poles
	-- angles are in degrees
	CREATE   FUNCTION boxenclosed(ra_roi DOUBLE , deroi DOUBLE , roi_size DOUBLE ,ra_box DOUBLE , debox DOUBLE , box_size_ra DOUBLE , box_size_dec DOUBLE ) RETURNS BOOLEAN DETERMINISTIC
	BEGIN
	DECLARE combinated_box_size DOUBLE  DEFAULT 0;
		-- ROI mut be larger than the image
		IF roi_size < box_size_ra OR roi_size < box_size_dec THEN
			RETURN FALSE;
		-- Circle approximation near the poles
		ELSEIF (deroi + roi_size) > 88.0 OR (deroi - roi_size) < -88.0 THEN
			IF box_size_dec < box_size_ra THEN
				SET combinated_box_size = box_size_dec;
			ELSE
				SET combinated_box_size = box_size_ra;
			END IF;
			SET combinated_box_size = (roi_size - combinated_box_size)/2;
			IF distancedegree(ra_roi, deroi,ra_box, debox) < combinated_box_size THEN
				RETURN TRUE;
			ELSE
				RETURN FALSE;
			END IF;
		-- IVOA/SIAP like enclosed condition
		ELSE
			RETURN isinbox(ra_box, debox, (roi_size - box_size_ra), (roi_size - box_size_dec), ra_roi, deroi);
		END IF;

	END;
	 *
	 *
	 * @param ra_roi in degrees
	 * @param deroi in degrees
	 * @param roi_size in degrees
	 * @param ra_box in degrees
	 * @param debox in degrees
	 * @param box_size_ra in degrees
	 * @param box_size_dec in degrees
	 * @return
	 */
	public static final int boxenclosed(double ra_roi, double deroi, double roi_size, double ra_box, double debox, double box_size_ra, double box_size_dec){
		/* ROI mut be larger than the image */
		if(  roi_size < box_size_ra || roi_size < box_size_dec ) {
			return 0;
		}
		/* Circle approximation near the poles */
		else if(  (deroi + roi_size) > 88.0 || (deroi - roi_size) < -88.0 ) {
			double combinated_box_size;
			if(  box_size_dec < box_size_ra ) {
				combinated_box_size = box_size_dec;
			} else {
				combinated_box_size = box_size_ra;
			}
			combinated_box_size = (roi_size - combinated_box_size)/2;
			if(  distancedegree(ra_roi, deroi,ra_box, debox) < combinated_box_size ) {
				return 1;
			} else {
				return 0;
			}
		}
		/* IVOA/SIAP like enclosed condition */
		else {
			return isinbox(ra_box, debox, (roi_size - box_size_ra), (roi_size - box_size_dec), ra_roi, deroi);
		}
	}

	/**
	 -- TRUE if box (ra_roi, deroi, roi_size) overlaps with box(ra_box, debox, box_size_ra, box_size_dec)
	-- circle approximation near the poles
	-- angles are in degrees
	CREATE  FUNCTION boxoverlaps(ra_roi DOUBLE , deroi DOUBLE , roi_size DOUBLE ,ra_box DOUBLE , debox DOUBLE , box_size_ra DOUBLE , box_size_dec DOUBLE ) RETURNS BOOLEAN DETERMINISTIC
	BEGIN
	DECLARE combinated_box_size DOUBLE  DEFAULT 0;
	DECLARE dra DOUBLE  DEFAULT 0;
		-- Circle approximation near the poles
		IF (deroi + roi_size) > 88.0 OR (deroi - roi_size) < -88.0 THEN
			IF box_size_dec < box_size_ra THEN
				SET combinated_box_size = box_size_dec;
			ELSE
				SET combinated_box_size = box_size_ra;
			END IF;
			SET combinated_box_size = (combinated_box_size + roi_size)/2;
			IF distancedegree(ra_roi, deroi,ra_box, debox) < combinated_box_size THEN
				RETURN TRUE;
			ELSE
				RETURN FALSE;
			END IF;
		-- IVOA/SIAP like overlap condition
		ELSE
			SET dra = ABS(ra_roi - ra_box);
			if dra > 180  THEN
			 	SET dra = 360 - dra;
			END IF;
			IF ABS(dra*COS(RADIANS(deroi))) <  (box_size_ra + roi_size)/2 AND ABS(deroi - debox) <  (box_size_dec + roi_size)/2 THEN
				RETURN TRUE;
			ELSE
				RETURN FALSE;
			END IF;
		END IF;

	END;
	 *
	 *
	 * @param ra_roi
	 * @param deroi
	 * @param roi_size
	 * @param ra_box
	 * @param debox
	 * @param box_size_ra
	 * @param box_size_dec
	 * @return
	 */
	public static final int boxoverlaps(double ra_roi, double deroi, double roi_size, double ra_box, double debox, double box_size_ra, double box_size_dec){
		/* Circle approximation near the poles */
		if(  (deroi + roi_size) > 88.0 || (deroi - roi_size) < -88.0 ) {
			double combinated_box_size;
			if(  box_size_dec < box_size_ra ) {
				combinated_box_size = box_size_dec;
			}
			else {
				combinated_box_size = box_size_ra;
			}
			combinated_box_size = (combinated_box_size + roi_size)/2;
			if(  distancedegree(ra_roi, deroi,ra_box, debox) < combinated_box_size ) {
				return 1;
			}
			else {
				return 0;
			}
		}
		/* IVOA/SIAP like overlap condition */
		else {
			double dra = Math.abs(ra_roi - ra_box);
			if( dra > 180  ) {
				dra = 360 - dra;
			}
			if(  Math.abs(dra*Math.cos(Math.toRadians(deroi))) <  (box_size_ra + roi_size)/2 && Math.abs(deroi - debox) <  (box_size_dec + roi_size)/2 ) {
				return 1;
			} else {
				return 0;
			}
		}
	}

}
