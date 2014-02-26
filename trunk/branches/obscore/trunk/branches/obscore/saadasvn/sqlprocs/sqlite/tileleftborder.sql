CREATE OR REPLACE FUNCTION tileleftborder(ra_center DOUBLE , de_center DOUBLE , width DOUBLE , de DOUBLE ) RETURNS DOUBLE  DETERMINISTIC
BEGIN
DECLARE delta DOUBLE  DEFAULT 2*(de - de_center);
  IF delta < 0 THEN
    RETURN corner00_ra(ra_center, de_center, width, -delta);
  ELSE 
    RETURN corner01_ra(ra_center, de_center, width, delta);
  END IF;
END
