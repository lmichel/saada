-- TRUE if  the box(ra_roi, dec_roi, roi_size) is enclsosed in the box (ra_box, dec_box, box_size_ra, box_size_dec)
-- circle approximation near the poles
-- angles are in degrees
CREATE OR REPLACE  FUNCTION boxcovers(ra_roi DOUBLE PRECISION, dec_roi DOUBLE PRECISION, roi_size DOUBLE PRECISION,ra_box DOUBLE PRECISION, dec_box DOUBLE PRECISION, box_size_ra DOUBLE PRECISION, box_size_dec DOUBLE PRECISION) RETURNS BOOLEAN IMMUTABLE AS $$
DECLARE 
	combinated_box_size DOUBLE PRECISION DEFAULT 0;
BEGIN
	-- ROI mut be smaller than the image
	IF roi_size > box_size_ra OR roi_size > box_size_dec THEN
		RETURN FALSE;
	-- Circle approximation near the poles
	ELSEIF (dec_roi + roi_size) > 88.0 OR (dec_roi - roi_size) < -88.0 THEN
		IF box_size_dec < box_size_ra THEN
			combinated_box_size := box_size_dec;
		ELSE
			combinated_box_size := box_size_ra;
		END IF;
		combinated_box_size := (combinated_box_size - roi_size)/2;
		IF distancedegree(ra_roi, dec_roi,ra_box, dec_box) < combinated_box_size THEN
			RETURN TRUE;
		ELSE
			RETURN FALSE;		
		END IF;
	-- IVOA/SIAP like covers condition
	ELSE 
		RETURN isinbox(ra_box, dec_box, (box_size_ra - roi_size), (box_size_dec - roi_size), ra_roi, dec_roi);
	END IF;
END;
$$ LANGUAGE 'plpgsql';