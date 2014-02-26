package preproc.csvconverter;

import java.io.IOException;

import saadadb.command.ArgsParser;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.util.Messenger;
import cds.savot.model.SavotTD;
import cds.savot.model.SavotTR;
import cds.savot.model.TDSet;

/**
 * Convert an ASCII file with columns with fixed width into as VOTable
 * ASCII file format:
 * Line beginning with #, / or ; are considered as comment. 
 * Comments cannot be inserted into data.
 * Columns are defined with at least 2 line separated  with | or , (comma)
 * 1st line: column names
 * 2nd line: column types
 * the 3rd line is considered as units if it exists
 * Data types are not checked by the converted
 * Any format error throws a FatalExcption
 * USAGE: FixedRowWidth if=inputfilename [of=outputfilename]
 *        The output file if outputfilename if the second parameter is set
 *        The output is set to stdout if outputfilename = -
 *        The output is set to inputfilename.xml if outputfilename is not set.
 * @author michel
 * @date 21/07/2009
 *
 */
public class FixedRowWidthConverter extends ASCIToVOTableConverter {
	public static final String COMMENT_LINE = "(\\s+)|(\\s*[\\\\#;]+.*)";
	public static final String HEADER_LINE = "([|,][^|,]*)+";
	public static final String COLUMN_DELIMITER = "[|,]";

	/**
	 * @param inputfile
	 * @throws Exception
	 */
	public FixedRowWidthConverter(String inputfile) throws Exception {
		super(inputfile);
	}

	/* (non-Javadoc)
	 * @see preproc.csvconverter.ASCIToVOTableConverter#readHeader()
	 */
	protected void readHeader() throws IOException {
		while( (current_line = reader.readLine()) != null  ) {
			if( current_line.matches(COMMENT_LINE)) {
				description.add(current_line);
			}
			else if( current_line.trim().matches(HEADER_LINE)) {
				header.add(current_line);
			}
			else {
				current_line = current_line.trim();
				break;
			}
		}
	}

	/* (non-Javadoc)
	 * @see preproc.csvconverter.ASCIToVOTableConverter#setColumnDefinition()
	 */
	protected void setColumnDefinition() throws FatalException {
		if( header.size() < 2 ) {
			FatalException.throwNewException(SaadaException.FILE_FORMAT, "No column description found") ;
		}
		/*
		 * The 1st header line is supposed to be the column names
		 */
		String [] col_names = header.get(0).split(COLUMN_DELIMITER);
		for( int i=1 ; i<col_names.length ; i++ ) {
			String c = col_names[i];
			if( c.length() == 0 ) {
				FatalException.throwNewException(SaadaException.FILE_FORMAT, "Column  #" + i + " has no name") ;			
			}
			AttributeHandler ah = new AttributeHandler();
			ah.setNameattr(c.trim());
			ah.setNameorg(c.trim());
			ahs.add(ah);
			col_width.add(c.length());
		}
		nb_cols = ahs.size();
		Messenger.printMsg(Messenger.TRACE, nb_cols + " columns detected");
		if( nb_cols < 1) {
			FatalException.throwNewException(SaadaException.FILE_FORMAT, "No column  found") ;			
		}
		/*
		 * The 2nd header line is supposed to be the column names
		 */
		String [] types = header.get(1).split(COLUMN_DELIMITER);
		if( types.length != col_names.length) {
			FatalException.throwNewException(SaadaException.FILE_FORMAT, "Number of declared columns (" + col_names.length + ") is different from the number of declared types (" + types.length + ")") ;			
		}
		for( int i=1 ; i<types.length ; i++ ) {
			String c = types[i].trim();
			if( c.length() == 0 ) {
				FatalException.throwNewException(SaadaException.FILE_FORMAT, "Column  #" + i + " has no type") ;			
			}
			ahs.get(i-1).setType(this.checkType(c));
		}		

		/*
		 * The 3rd header line is supposed to be the units
		 */
		if( header.size() > 2 ) {
			Messenger.printMsg(Messenger.TRACE, "3rd header row taken as unit");
			String [] units = header.get(2).split(COLUMN_DELIMITER);
			if( units.length != col_names.length) {
				FatalException.throwNewException(SaadaException.FILE_FORMAT, "Number of declared columns (" + col_names.length + ") is different from the number of declared units (" + units.length + ")") ;			
			}
			for( int i=1 ; i<units.length ; i++ ) {
				String c = units[i].trim();
				ahs.get(i-1).setUnit(c);
			}	
		}

	}


	/* (non-Javadoc)
	 * @see preproc.csvconverter.ASCIToVOTableConverter#checkType(java.lang.String)
	 */
	protected String checkType(String type) throws FatalException {
		if( type.equalsIgnoreCase("int") || type.equalsIgnoreCase("integer")) {
			return "int";
		}
		else if( type.equalsIgnoreCase("bool") || type.equalsIgnoreCase("boolean")) {
			return "boolean";
		}
		else if( type.equalsIgnoreCase("short") ) {
			return "short";
		}
		else if( type.equalsIgnoreCase("long") ) {
			return "long";
		}
		else if( type.equalsIgnoreCase("real") || type.equalsIgnoreCase("float")) {
			return "float";
		}
		else if( type.equalsIgnoreCase("double") ) {
			return "double";
		}
		else if( type.equalsIgnoreCase("char") || type.equalsIgnoreCase("string")) {
			return "String";
		}
		else {
			FatalException.throwNewException(SaadaException.FILE_FORMAT, "Unknown type  " + type ) ;	
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see preproc.csvconverter.ASCIToVOTableConverter#writeData()
	 */
	protected  void writeData() throws Exception{
		Messenger.printMsg(Messenger.DEBUG, "Start to write data");
		writeLine();
		int cpt=0;
		while( (current_line = reader.readLine()) != null  ) {
			current_line = current_line.trim();
			if( current_line.length() > 0)
				writeLine();
			cpt++;
			if( (cpt%1000) == 0 ) {
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, cpt + " lines read");
			}
		}
		Messenger.printMsg(Messenger.DEBUG, cpt + " lines read");
	}

	/* (non-Javadoc)
	 * @see preproc.csvconverter.ASCIToVOTableConverter#writeLine()
	 */
	protected void writeLine() throws Exception{
		SavotTR savotTR = new SavotTR();					
		TDSet tdSet = new TDSet();
		int start=0, cpt=0;
		for(AttributeHandler ah: ahs) {
			int l = col_width.get(cpt) + 1;
			String val;
			if( (start+l) >= current_line.length() ) {
				val = current_line.substring(start).trim();		    	
			}
			else {
				val = current_line.substring(start, start+l).trim();
			}
			if( ah.getType().equals("String") ) {
				val = "<![CDATA[ " + val + "]]>";
			}
			start += l;
			SavotTD td = new SavotTD();
			td.setContent(val);
			tdSet.addItem(td);
			cpt++;
		}
		savotTR.setTDs(tdSet);
		writer.writeTR(savotTR);
	}

		

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			if( args.length != 1 && args.length != 2) {
				usage();
			}
			ArgsParser ap = new ArgsParser(args);
			String ifl = ap.getIf();
			if( ifl.length() == 0 ) {
				usage();								
			}
			FixedRowWidthConverter frw = new FixedRowWidthConverter(ifl);				
			frw.convert(ap.getof());
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			System.exit(1);
		}

	}
}
