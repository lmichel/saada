CREATE  FUNCTION corner00_dec(de DOUBLE ,size_de DOUBLE ) RETURNS DOUBLE  DETERMINISTIC
BEGIN
DECLARE d DOUBLE DEFAULT (de - (size_de/2));
  IF d < -90 THEN 
    RETURN (-180. - d);
  ELSE 
    RETURN d;
  END IF;
END;
