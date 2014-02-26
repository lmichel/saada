CREATE OR REPLACE FUNCTION getmjd (x DOUBLE) RETURNS TEXT DETERMINISTIC
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
  julian = (x + 2400000.5 + 0.5);
  ja     = FLOOR(julian);
  reste  = FLOOR(((julian  - ja) * 86400.0));
  IF ja >  JGREG THEN
    jalpha = FLOOR((((ja - 1867216) - 0.25) / 36524.25) );
    ja = ja + 1 + jalpha - FLOOR(jalpha / 4);
  END IF	;
  jb  = ja + 1524;
  jc  = FLOOR((6680.0 + ((jb - 2439870) - 122.1) / 365.25));
  jd  = FLOOR((365 * jc) + (jc / 4));
  je  = FLOOR(((jb - jd) / 30.6001));
  day = jb - jd - FLOOR((30.6001 * je) );
  month = je - 1;
  IF month > 12 THEN
    month = month - 12;
  END IF	; 
  year = jc - 4715;
  IF month > 2  THEN
    year = year-1;
  END IF;	 
  IF year <= 0 THEN
    year = year-1;
  END IF;
  hours = FLOOR(reste/3600);
  reste = reste - (hours*3600);
  min   = FLOOR(reste/60);
  reste = reste - min*60;
  RETURN CONCAT(day , '/' , month , '/' , year , ' ' , hours , ':' , min , ':' , reste);
END
