CREATE FUNCTION getmjd (x DOUBLE) RETURNS TEXT DETERMINISTIC
BEGIN
DECLARE JGREG  DOUBLE PRECISION DEFAULT (15 + 31*(10+12*1582));
DECLARE jalpha INTEGER;
DECLARE ja INTEGER ;
DECLARE jb INTEGER;
DECLARE jc INTEGER;
DECLARE jd INTEGER;
DECLARE je INTEGER;
DECLARE year INTEGER;
DECLARE month INTEGER;
DECLARE day INTEGER;
DECLARE hours INTEGER;
DECLARE min INTEGER;
DECLARE	julian  DOUBLE PRECISION ;
DECLARE reste INTEGER  ;
  SET julian = (x + 2400000.5 + 0.5);
  SET ja     = FLOOR(julian);
  SET reste  = FLOOR(((julian  - ja) * 86400.0));
  IF ja >  JGREG THEN
    SET jalpha = FLOOR((((ja - 1867216) - 0.25) / 36524.25) );
    SET ja = ja + 1 + jalpha - FLOOR(jalpha / 4);
  END IF	;
  SET jb  = ja + 1524;
  SET jc  = FLOOR((6680.0 + ((jb - 2439870) - 122.1) / 365.25));
  SET jd  = FLOOR((365 * jc) + (jc / 4));
  SET je  = FLOOR(((jb - jd) / 30.6001));
  SET day = jb - jd - FLOOR((30.6001 * je) );
  SET month = je - 1;
  IF month > 12 THEN
    SET month = month - 12;
  END IF	; 
  SET year = jc - 4715;
  IF month > 2  THEN
    SET year = year-1;
  END IF;	 
  IF year <= 0 THEN
    SET year = year-1;
  END IF;
  SET hours = FLOOR(reste/3600);
  SET reste = reste - (hours*3600);
  SET min   = FLOOR(reste/60);
  SET reste = reste - min*60;
  RETURN CONCAT(day , '/' , month , '/' , year , ' ' , hours , ':' , min , ':' , reste);
END
