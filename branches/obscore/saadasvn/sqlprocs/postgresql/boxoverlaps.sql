-- TRUE if box (ra_roi, dec_roi, roi_size) overlaps with box(ra_box, dec_box, box_size_ra, box_size_dec)
-- circle approximation near the poles
-- angles are in degrees
CREATE OR REPLACE  FUNCTION boxoverlaps(ra_roi DOUBLE PRECISION, dec_roi DOUBLE PRECISION, roi_size DOUBLE PRECISION,ra_box DOUBLE PRECISION, dec_box DOUBLE PRECISION, box_size_ra DOUBLE PRECISION, box_size_dec DOUBLE PRECISION) RETURNS BOOLEAN IMMUTABLE AS $$
DECLARE 
	combinated_box_size DOUBLE PRECISION DEFAULT 0;
	dra DOUBLE PRECISION DEFAULT 0;
BEGIN
	-- Circle approximation near the poles
	IF (dec_roi + roi_size) > 88.0 OR (dec_roi - roi_size) < -88.0 THEN
		IF box_size_dec < box_size_ra THEN
			combinated_box_size := box_size_dec;
		ELSE
			combinated_box_size := box_size_ra;
		END IF;
		combinated_box_size := (combinated_box_size + roi_size)/2;
		IF distancedegree(ra_roi, dec_roi,ra_box, dec_box) < combinated_box_size THEN
			RETURN TRUE;
		ELSE
			RETURN FALSE;		
		END IF;
	-- IVOA/SIAP like overlap condition
	ELSE 
		dra := ABS(ra_roi - ra_box);
		if dra > 180  THEN
		 	dra := 360 - dra;
		END IF;
		IF ABS(dra*COS(RADIANS(dec_roi))) <  (box_size_ra + roi_size)/2 AND ABS(dec_roi - dec_box) <  (box_size_dec + roi_size)/2 THEN
			RETURN TRUE;
		ELSE 
			RETURN FALSE;
		END IF;
	END IF;
END;
$$ LANGUAGE 'plpgsql';
