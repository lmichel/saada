CREATE  OR REPLACE  FUNCTION isinbox(ra_box DOUBLE PRECISION, de_box DOUBLE PRECISION, size_ra DOUBLE PRECISION, size_de DOUBLE PRECISION, ra DOUBLE PRECISION, de DOUBLE PRECISION) RETURNS BOOLEAN IMMUTABLE AS $$
DECLARE 
	width DOUBLE PRECISION DEFAULT 0;
	tleftb DOUBLE PRECISION DEFAULT 0;
	trightb DOUBLE PRECISION DEFAULT 0;
BEGIN
	-- Circle approximation near the poles
	IF de > 88 OR de < -88 THEN
		IF distancedegree(ra_box, de_box,ra, de ) < (size_ra + size_de)/2 THEN
			RETURN TRUE;
		ELSE
			RETURN FALSE;		
		END IF;
  	ELSEIF de >  corner01_dec(de_box, size_de) THEN
     	RETURN FALSE;
  	ELSEIF de <  corner00_dec(de_box, size_de) THEN
    	RETURN FALSE;
  	END IF;
  	tleftb  := tileleftborder(ra_box, de_box, size_ra, de);
  	trightb := tilerightborder(ra_box, de_box, size_ra, de);
  	width := distancedegree(tleftb, de, trightb, de);
  	IF distancedegree(tleftb, de, ra,de) < width AND distancedegree(trightb, de, ra,de) < width THEN
    	RETURN TRUE;
  	ELSE
    	RETURN FALSE;
 	END IF;
END;
$$ LANGUAGE 'plpgsql';