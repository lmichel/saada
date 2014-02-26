-- ** Q1 en SaadaQL **
-- Select IMAGE From * In XMMData
-- WhereAttributeSaada { namesaada = 'truc'; };

-- ** Q1 en ADQL **
-- SELECT * FROM XMMData_IMAGE WHERE namesaada = 'truc';

-- ** Q2 en SaadaQL
-- Select SPECTRUM From EPICSpectrum In XMMData
-- WhereAttributeSaada{ pos_ra_csa > 8 and _naxis > 0}

-- ** Q2 en ADQL
-- SELECT oidsaada FROM EPICSpectrum WHERE pos_ra_csa > 8 AND \"_naxis\" > 0;

-- SELECT oidsaada FROM XMMData_SPECTRUM WHERE abs(pos_x_csa) BETWEEN x_min_csa AND x_max_csa AND \"_naxis\" > 0;

-- SELECT oidsaada FROM XMMData_SPECTRUM WHERE x_min_csa IS NULL OR x_max_csa IS NULL;

-- SELECT oidsaada FROM XMMData_SPECTRUM WHERE x_unit_csa LIKE '%m';

SELECT oidsaada, "_s_ra", "_s_dec"
FROM ObsCore
WHERE CONTAINS(POINT('ICRS GEOCENTER', "_s_ra", "_s_dec"), BOX('ICRS GEOCENTER', 1.2, 2.3, 10, 10)) = TRUE;