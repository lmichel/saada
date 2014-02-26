CREATE OR REPLACE FUNCTION tilerightborder(ra_center DOUBLE , de_center DOUBLE , width DOUBLE , de DOUBLE ) RETURNS DOUBLE   DETERMINISTIC
BEGIN
DECLARE delta DOUBLE  DEFAULT 2*(de - de_center);
  IF delta < 0 THEN
    RETURN corner10_ra(ra_center, de_center, width, -delta);
  ELSE 
    RETURN corner11_ra(ra_center, de_center, width, delta);
  END IF;
END
