CREATE  FUNCTION corner11_ra(ra DOUBLE ,de DOUBLE ,size_ra DOUBLE ,size_de DOUBLE ) RETURNS DOUBLE  DETERMINISTIC
BEGIN
DECLARE cdec DOUBLE DEFAULT corner11_dec(de, size_de);
DECLARE d DOUBLE DEFAULT 0 ;
  	IF cdec = 90 THEN
    	SET d = ra + 90;
  	ELSE
    	SET d = (ra + (size_ra/2)/ABS(COS(RADIANS(cdec))));
	END IF;
  
	IF d > 360 THEN 
		RETURN (d - 360.);
	ELSE 
		RETURN d;
	END IF;
END;
