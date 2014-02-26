CREATE  OR REPLACE  FUNCTION isinbox(ra_box DOUBLE , de_box DOUBLE , size_ra DOUBLE , size_de DOUBLE , ra DOUBLE , de DOUBLE ) RETURNS BOOLEAN DETERMINISTIC
BEGIN
DECLARE width DOUBLE  DEFAULT 0;
DECLARE tleftb DOUBLE  DEFAULT 0;
DECLARE trightb DOUBLE  DEFAULT 0;
  IF de >  corner01_dec(de_box, size_de) THEN
    RETURN FALSE;
  ELSEIF de <  corner00_dec(de_box, size_de) THEN
    RETURN FALSE;
  END IF;
  tleftb  = tileleftborder(ra_box, de_box, size_ra, de);
  trightb = tilerightborder(ra_box, de_box, size_ra, de);
  width = distancedegree(tleftb, de, trightb, de);
  IF distancedegree(tleftb, de, ra,de) < width AND distancedegree(trightb, de, ra,de) < width THEN
    RETURN TRUE;
  ELSE
    RETURN FALSE;
  END IF;
END;
