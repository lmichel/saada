CREATE  FUNCTION corner01_ra(ra DOUBLE ,de DOUBLE ,size_ra DOUBLE ,size_de DOUBLE ) RETURNS DOUBLE  DETERMINISTIC
BEGIN
DECLARE cdec DOUBLE DEFAULT corner01_dec(de, size_de);
DECLARE d DOUBLE DEFAULT 0;
	-- bottom border pass on the pole: take the top border
	IF cdec > 89.99 OR cdec < -89.99 THEN
		IF size_de > 0.015 THEN 
			RETURN corner00_ra(ra, de, size_ra, size_de);
		-- Don't know what to do here with a squashed box
		ELSE 
			RETURN ra; 
		END IF;
	ELSE
		SET  d = (ra - (size_ra/2)/ABS(COS(RADIANS(cdec))));
  		IF d < 0 THEN   
    		RETURN (360. + d);
  		ELSE
    		RETURN d;
  		END IF;
  	END IF;
END;
