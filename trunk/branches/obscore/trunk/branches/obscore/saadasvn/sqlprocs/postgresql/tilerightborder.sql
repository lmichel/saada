CREATE OR REPLACE FUNCTION tilerightborder(ra_center DOUBLE PRECISION, de_center DOUBLE PRECISION, width DOUBLE PRECISION, de DOUBLE PRECISION) RETURNS DOUBLE PRECISION  IMMUTABLE AS $$
DECLARE 
	delta DOUBLE PRECISION DEFAULT 2*(de - de_center);
BEGIN
  IF delta < 0 THEN
    RETURN corner10_ra(ra_center, de_center, width, -delta);
  ELSE 
    RETURN corner11_ra(ra_center, de_center, width, delta);
  END IF;
END
$$ LANGUAGE 'plpgsql';