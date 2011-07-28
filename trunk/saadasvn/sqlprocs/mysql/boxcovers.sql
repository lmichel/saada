-- TRUE if  the box(ra_roi, dec_roi, roi_size) is enclsosed in the box (ra_box, dec_box, box_size_ra, box_size_dec)
-- circle approximation near the poles
-- angles are in degrees
CREATE   FUNCTION boxcovers(ra_roi DOUBLE , dec_roi DOUBLE , roi_size DOUBLE ,ra_box DOUBLE , dec_box DOUBLE , box_size_ra DOUBLE , box_size_dec DOUBLE ) RETURNS BOOLEAN DETERMINISTIC
BEGIN
DECLARE combinated_box_size DOUBLE  DEFAULT 0;
	-- ROI mut be smaller than the image
	IF roi_size > box_size_ra OR roi_size > box_size_dec THEN
		RETURN FALSE;
	-- Circle approximation near the poles
	ELSEIF (dec_roi + roi_size) > 88.0 OR (dec_roi - roi_size) < -88.0 THEN
		IF box_size_dec < box_size_ra THEN
			SET combinated_box_size = box_size_dec;
		ELSE
			SET combinated_box_size = box_size_ra;
		END IF;
		SET combinated_box_size = (combinated_box_size - roi_size)/2;
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
