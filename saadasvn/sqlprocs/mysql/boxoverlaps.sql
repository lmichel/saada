-- TRUE if box (ra_roi, dec_roi, roi_size) overlaps with box(ra_box, dec_box, box_size_ra, box_size_dec)
-- circle approximation near the poles
-- angles are in degrees
CREATE  FUNCTION boxoverlaps(ra_roi DOUBLE , dec_roi DOUBLE , roi_size DOUBLE ,ra_box DOUBLE , dec_box DOUBLE , box_size_ra DOUBLE , box_size_dec DOUBLE ) RETURNS BOOLEAN DETERMINISTIC
BEGIN
DECLARE combinated_box_size DOUBLE  DEFAULT 0;
DECLARE dra DOUBLE  DEFAULT 0;
	-- Circle approximation near the poles
	IF (dec_roi + roi_size) > 88.0 OR (dec_roi - roi_size) < -88.0 THEN
		IF box_size_dec < box_size_ra THEN
			SET combinated_box_size = box_size_dec;
		ELSE
			SET combinated_box_size = box_size_ra;
		END IF;
		SET combinated_box_size = (combinated_box_size + roi_size)/2;
		IF distancedegree(ra_roi, dec_roi,ra_box, dec_box) < combinated_box_size THEN
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
		IF ABS(dra*COS(RADIANS(dec_roi))) <  (box_size_ra + roi_size)/2 AND ABS(dec_roi - dec_box) <  (box_size_dec + roi_size)/2 THEN
			RETURN TRUE;
		ELSE 
			RETURN FALSE;
		END IF;
	END IF;

END;
