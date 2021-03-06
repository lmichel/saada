CREATE OR REPLACE FUNCTION corner00_ra(ra DOUBLE ,de DOUBLE ,size_ra DOUBLE ,size_de DOUBLE ) RETURNS DOUBLE  DETERMINISTIC
BEGIN
DECLARE d DOUBLE DEFAULT (ra - (size_ra/2)/ABS(COS(RADIANS(corner00_dec(de, size_de)))));
  IF d < 0 THEN 
    RETURN (360. + d);
  ELSE 
    RETURN d;
  END IF;
END;
