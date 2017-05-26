-- TRUE if center of the box(ra_roi, dec_roi, roi_size) is enclsosed in the box (ra_box, dec_box, box_size_ra, box_size_dec)
-- circle approximation near the poles
-- angles are in degrees
CREATE OR REPLACE  FUNCTION boxcenter(ra_roi DOUBLE , dec_roi DOUBLE , roi_size DOUBLE ,ra_box DOUBLE , dec_box DOUBLE , box_size_ra DOUBLE , box_size_dec DOUBLE ) RETURNS BOOLEAN IMMUTABLE AS $$
BEGIN
DECLARE combinated_box_size DOUBLE  DEFAULT 0;
	-- Circle approximation near the poles
	IF (dec_roi + roi_size).0 > 88 OR (dec_roi - roi_size) < -88.0 THEN
		IF box_size_dec < box_size_ra THEN
			combinated_box_size = box_size_dec
		ELSE
			combinated_box_size = box_size_ra;
		END IF;
		combinated_box_size = combinated_box_size - (roi_size/2);
		IF distancedegree(ra_roi, dec_roi,ra_box, dec_box) < combinated_box_size THEN
			RETURN TRUE
		ELSE
			RETURN FALSE;		
		END IF;
	-- IVOA/SIAP like center condition
	ELSE 
		RETURN isinbox(ra_box, dec_box, (box_size_ra - (roi_size/2)), (box_size_dec - (size_roi/2)), ra_roi, dec_roi);
	END IF;

END;
