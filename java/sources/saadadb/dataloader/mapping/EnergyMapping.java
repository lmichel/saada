package saadadb.dataloader.mapping;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import saadadb.command.ArgsParser;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.util.Messenger;
import saadadb.vocabulary.RegExp;
import saadadb.vocabulary.enums.MappingMode;

public class EnergyMapping extends AxisMapping {
	/**
	 * @param ap
	 * @throws SaadaException
	 */
	EnergyMapping(ArgsParser ap, boolean entryMode) throws SaadaException {
		super(ap, new String[]{"dispertion_column", "em_unit", "em_min", "em_max", "em_res_power", "em_bins"}, entryMode);
		if( ap.getSpectralUnit() != null )  {
			this.columnMapping.put("em_unit_csa", new ColumnMapping(MappingMode.VALUE, null,ap.getSpectralUnit(),"em_unit_csa"));
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

		String s;
		
		if( (s = tabArg.getSpectralUnit()) != null) {
			this.columnMapping.put("em_unit", new ColumnMapping(null, s, "em_unit"));
		}
		if( (s = tabArg.getSpectralResPower(entryMode)) != null  ){
			this.columnMapping.put("em_res_power", new ColumnMapping(null, s, "em_res_power"));
		} 

		String emin = tabArg.getEmin(entryMode);
		String emax = tabArg.getEmax(entryMode);
		if( emin != null && emax != null ) {
			this.columnMapping.put("em_min", new ColumnMapping(null, emin, "em_min"));
			this.columnMapping.put("em_max", new ColumnMapping(null, emax, "em_max"));
		} else {
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
						this.columnMapping.put("em_min", new ColumnMapping(null, m.group(1), "em_min"));
						this.columnMapping.put("em_max", new ColumnMapping(null, m.group(2), "em_max"));
					} else {
						FatalException.throwNewException(SaadaException.WRONG_PARAMETER, "The spectral range <" + sc_col + "> requires 2 values");
					}
				} else {
					this.columnMapping.put("dispertion_column", new ColumnMapping(MappingMode.KEYWORD, null, sc_col, "dispertion_column"));
				}	
			}
		}
		String ebins = tabArg.getEBins(entryMode);
		if( ebins != null ) {
			this.columnMapping.put("em_bins", new ColumnMapping(null, ebins, "em_bins"));		
		}
	}


}
