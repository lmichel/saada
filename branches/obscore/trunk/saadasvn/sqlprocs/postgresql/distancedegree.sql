CREATE  OR REPLACE FUNCTION distancedegree(ra0 DOUBLE PRECISION, de0 DOUBLE PRECISION, ra1 DOUBLE PRECISION, de1 DOUBLE PRECISION) RETURNS DOUBLE PRECISION IMMUTABLE AS $$
DECLARE 
	rde0 DOUBLE PRECISION DEFAULT 0 ;
	rde1 DOUBLE PRECISION DEFAULT 0 ;
BEGIN
	rde0 = radians(de0);
	rde1 = radians(de1);
	RETURN degrees(acos((sin(rde0)*sin(rde1)) + 
                     (cos(rde0)*cos(rde1) * cos(radians(ra0)-radians(ra1)))));
END;
$$ LANGUAGE 'plpgsql';

