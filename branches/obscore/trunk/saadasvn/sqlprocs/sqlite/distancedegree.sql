CREATE  OR REPLACE FUNCTION distancedegree(ra0 DOUBLE , de0 DOUBLE , ra1 DOUBLE , de1 DOUBLE ) RETURNS DOUBLE  IMMUTABLE AS $$
BEGIN
DECLARE rde0 DOUBLE  DEFAULT 0 ;
DECLARE rde1 DOUBLE  DEFAULT 0 ;
	rde0 = radians(de0);
	rde1 = radians(de1);
	RETURN degrees(acos((sin(rde0)*sin(rde1)) + 
                     (cos(rde0)*cos(rde1) * cos(radians(ra0)-radians(ra1)))));
END

CREATE OR REPLACE FUNCTION distancedegree(ra0 DOUBLE , de0 DOUBLE , ra1 DOUBLE , de1 DOUBLE ) RETURNS DOUBLE  IMMUTABLE AS $$
BEGIN
		RETURN degrees(
				2*asin(
                sqrt(
                   ( sin(radians((de0-de1)/2))
                    *sin(radians((de0-de1))  )
                  +( sin(radians((ra0 - ra1)/2))
                    *sin(radians((ra0 - ra1)/2))
                    *cos(radians( de0))
                    *cos(radians( de1)))
                )
             )
             ));
END
