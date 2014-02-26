package saadadb.dataloader.mapping;

import saadadb.command.ArgsParser;
import saadadb.exceptions.SaadaException;

public class EnergyMapping extends AxeMapping {
	/**
	 * @param ap
	 * @throws SaadaException
	 */
	EnergyMapping(ArgsParser ap, boolean entryMode) throws SaadaException {
		super(ap, new String[]{"dispertion_column", "x_unit_org_csa"}, entryMode);
		if( ap.getSpectralColumn() != null )  {
			this.columnMapping.put("dispertion_column", new ColumnMapping(MappingMode.VALUE, null, ap.getSpectralColumn()));
		}
		if( ap.getSpectralUnit() != null )  {
			this.columnMapping.put("x_unit_org_csa", new ColumnMapping(MappingMode.VALUE, null,ap.getSpectralUnit()));
		}
		this.completeColumns();
		
//		sc_col = sc_col.replaceAll("'", ""); 		
//   		Pattern p = Pattern.compile("(" + RegExp.FITS_INT_VAL + "|" + RegExp.FITS_FLOAT_VAL + ")[,:;\\- ]("
//   				                        + RegExp.FITS_INT_VAL + "|" + RegExp.FITS_FLOAT_VAL + ")");
//		Matcher m = p.matcher(sc_col);

	}
	

}
