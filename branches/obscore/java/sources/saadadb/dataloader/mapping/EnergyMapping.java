package saadadb.dataloader.mapping;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import saadadb.command.ArgsParser;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.util.Messenger;
import saadadb.util.RegExp;

public class EnergyMapping extends AxisMapping {
	/**
	 * @param ap
	 * @throws SaadaException
	 */
	EnergyMapping(ArgsParser ap, boolean entryMode) throws SaadaException {
		super(ap, new String[]{"dispertion_column", "x_unit_org_csa", "em_min", "em_max"}, entryMode);
		if( ap.getSpectralUnit() != null )  {
			this.columnMapping.put("x_unit_org_csa", new ColumnMapping(MappingMode.VALUE, null,ap.getSpectralUnit(),"x_unit_org_csa"));
		}
		this.mappSpectralRange(ap);
		this.completeColumns();
		this.priority = ap.getSpectralMappingPriority();
	}
	/**
	 * Unit is not set in the dispertion_column because it is handle as a specifi column
	 * @param tabArg
	 * @throws SaadaException
	 */
	private void mappSpectralRange(ArgsParser tabArg) throws SaadaException {
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Set spectral dispersion mapping");

		String sc_col = tabArg.getSpectralColumn();
		if( sc_col != null ) {
			if ( sc_col.startsWith("'")) {

				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Spectral range given as numeric values.");
				sc_col = sc_col.replaceAll("'", ""); 		
				Pattern p = Pattern.compile("(" + RegExp.FITS_INT_VAL + "|" + RegExp.FITS_FLOAT_VAL + ")[,:;\\- ]("
						+ RegExp.FITS_INT_VAL + "|" + RegExp.FITS_FLOAT_VAL + ")");
				Matcher m = p.matcher(sc_col);
				if( m.find() && m.groupCount() == 2 ) {
					this.columnMapping.put("dispertion_column"
							, new ColumnMapping(null, new String[]{m.group(1),m.group(2)}, "dispertion_column")) ;
					this.columnMapping.put("em_min", new ColumnMapping(null, "'" + m.group(1) + "'", "em_min"));
					this.columnMapping.put("em_max", new ColumnMapping(null, "'" + m.group(2) + "'", "em_max"));
				} else {
					FatalException.throwNewException(SaadaException.WRONG_PARAMETER, "The spectral range <" + sc_col + "> requires 2 values");
				}
			} else {
				this.columnMapping.put("dispertion_column", new ColumnMapping(MappingMode.ATTRIBUTE, null, sc_col, "dispertion_column"));
			}
		}
	}


}
