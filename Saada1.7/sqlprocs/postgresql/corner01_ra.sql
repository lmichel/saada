CREATE OR REPLACE FUNCTION corner01_ra(ra DOUBLE PRECISION,de DOUBLE PRECISION,size_ra DOUBLE PRECISION,size_de DOUBLE PRECISION) RETURNS DOUBLE PRECISION IMMUTABLE AS $$
DECLARE 
	cdec DOUBLE PRECISION := corner01_dec(de, size_de);
	d DOUBLE PRECISION DEFAULT 0;
BEGIN
	-- bottom border pass on the pole: take the top border
	IF cdec > 89.99 OR cdec < -89.99 THEN
		IF size_de > 0.015 THEN 
			RETURN corner00_ra(ra, de, size_ra, size_de);
		-- Don't know what to do here with a squashed box
		ELSE 
			RETURN ra; 
		END IF;
	ELSE
		d := (ra - (size_ra/2)/ABS(COS(RADIANS(cdec))));
  		IF d < 0 THEN   
    		RETURN (360. + d);
  		ELSE
    		RETURN d;
  		END IF;
  	END IF;
END;
$$ LANGUAGE 'plpgsql';