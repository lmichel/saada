package preproc.csvconverter;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.util.Messenger;
import cds.astro.Astroframe;
import cds.savot.model.CoosysSet;
import cds.savot.model.FieldSet;
import cds.savot.model.ParamSet;
import cds.savot.model.SavotCoosys;
import cds.savot.model.SavotField;
import cds.savot.model.SavotParam;
import cds.savot.model.SavotResource;
import cds.savot.model.SavotTable;
import cds.savot.model.SavotVOTable;
import cds.savot.writer.SavotWriter;

/**
 * Convert an ASCII file with columns with fixed width into as VOTable
 * ASCII file format is handled by subclasses
 * Any format error throws a FatalExcption
 * USAGE: subclassname if=inputfilename [of=outputfilename]
 *        The output file if outputfilename if the second parameter is set
 *        The output is set to stdout if outputfilename = -
 *        The output is set to inputfilename.xml if outputfilename is not set.
 * @author michel
 * @date 21/07/2009
 *
 */
public abstract class ASCIToVOTableConverter {
	protected String inputfile;
	protected BufferedReader reader;
	protected String current_line;
	protected ArrayList<String> description  = new ArrayList<String>();
	protected ArrayList<String> header       = new ArrayList<String>();
	protected ArrayList<AttributeHandler>ahs = new ArrayList<AttributeHandler>();
	protected ArrayList<Integer>col_width    = new ArrayList<Integer>();
	protected int nb_cols=-1;
	protected SavotWriter writer = new SavotWriter();	
	protected Astroframe astroframe = null;;
	
	/**
	 * @param inputfile
	 * @throws Exception
	 */
	public ASCIToVOTableConverter(String inputfile) throws Exception {
		this.inputfile = inputfile;
		File ifl = new File(inputfile);
		if( !ifl.exists() || !ifl.isFile()) {
			FatalException.throwNewException(SaadaException.FILE_ACCESS, inputfile + " is does not exist or is not a file");
		}
		reader = new BufferedReader(new FileReader(ifl));
	}
	
	/**
	 * Extract the ASCII file header
	 * @throws IOException
	 */
	protected abstract void readHeader() throws IOException ;
	
	/**
	 * Extract the column definitions from the ASCII file header
	 * @throws FatalException
	 */
	protected abstract void setColumnDefinition() throws FatalException;

	/**
	 * @throws Exception
	 */
	protected  abstract void writeData() throws Exception;

	/**
	 * Transform the current line into a VOTable row
	 * @throws Exception
	 */
	protected abstract void writeLine() throws Exception;

	/**
	 * Build the VOtable (encapsulate SAVOT)
	 * @throws Exception
	 */
	protected void createVOTable() throws Exception{

		SavotVOTable votable = new SavotVOTable();
		votable.setXmlnsxsi("http://www.w3.org/2001/XMLSchema-instance");
		votable.setXsinoschema("http://www.ivoa.net/xml/VOTable-1.1.xsd");
		writer.writeDocumentHead(votable);
		if( astroframe != null ) {
		CoosysSet coosysSet = new CoosysSet();
		SavotCoosys savotCoosys = new SavotCoosys();
		savotCoosys.setId(astroframe.toString());
		savotCoosys.setSystem(astroframe.toString());
		coosysSet.addItem(savotCoosys);
		writer.writeCoosys(coosysSet);
		}

		SavotResource resource = new SavotResource();
		writer.writeResourceBegin(resource);
		writer.writeDescription("Generated from file " + this.inputfile + " by " + this.getClass().getName());
		SavotTable table = new SavotTable();
		table.setName("Data");
		writer.writeTableBegin(table);
		if( description.size() > 0 ) {
			ParamSet paramSet = new ParamSet();
			for( int cpt=0 ; cpt<description.size() ; cpt++ ) {
				SavotParam param = new SavotParam();
				param.setName("Description_" + cpt);
				param.setValue(description.get(cpt).substring(1).trim());
				paramSet.addItem(param);
			}
			writer.writeParam(paramSet);
		}
		FieldSet fs = new FieldSet() ;
		for(AttributeHandler ah: ahs) {
			SavotField field = new SavotField();
			field.setName(ah.getNameorg()); 
			if( "String".equals(ah.getType())) {
				field.setDataType("char");
				field.setArraySize("*");
			}
			else {
				field.setDataType(ah.getType());
			}
			field.setUnit(ah.getUnit());
			if( ah.getUcd() != null && ah.getUcd().length() > 0) {
				field.setUcd(ah.getUcd());
			}
			fs.addItem(field);
		}
		writer.writeField(fs);
		writer.writeDataBegin();
		writer.writeTableDataBegin();
		this.writeData();
		writer.writeTableDataEnd();
		writer.writeDataEnd();
		writer.writeTableEnd();
		writer.writeResourceEnd();
		writer.writeDocumentEnd();

	}
	
	/**
	 * Do the conversion into filename
	 * If filename = "-" stdout is taken as output
	 * If filename is empty, inputfile.xml is taken as output file
	 * @param filename
	 * @throws Exception
	 */
	public void convert(String filename ) throws Exception{
		String output=filename;
		if( filename == null || filename.equals("")) {
			output = inputfile + ".xml";
			Messenger.printMsg(Messenger.TRACE, "Output in file " + output);
			this.writer.initStream(new BufferedOutputStream(new FileOutputStream(new File(output))));			
		}
		else if( filename.equals("-")) {
			Messenger.printMsg(Messenger.TRACE, "Output on stdout");
			Messenger.debug_mode = false;
			this.writer.initStream(System.out);
			output = "stdout";
		}
		else {
			File f = new File(filename);
			if( f.exists() && f.isDirectory() ) {
				output = filename + Database.getSepar() + (new File(inputfile)).getName() + ".xml";				
			}
			Messenger.printMsg(Messenger.TRACE, "Output in file " + output);
			this.writer.initStream(new BufferedOutputStream(new FileOutputStream(new File(output))));	
		}
		this.readHeader();
		this.setColumnDefinition();
		this.createVOTable();
		Messenger.printMsg(Messenger.TRACE, "Conversion done in " + output);
		
	}
	
	/**
	 * Convert a type read into an ASCII file into a type VOTable compliant
	 * @param type
	 * @return
	 * @throws FatalException
	 */
	protected abstract String checkType(String type) throws FatalException ;
	
	/**
	 * 
	 */
	public static void usage() {
		Messenger.printMsg(Messenger.ERROR, "USAGE: XXXConverter if=inputfilename [of=outputfilename]" );
		Messenger.printMsg(Messenger.ERROR, "       Convert inputfilename into a VOTable" );
		Messenger.printMsg(Messenger.ERROR, "       The output file if outputfilename if the second parameter is set" );
		Messenger.printMsg(Messenger.ERROR, "       The output is set to stdout if outputfilename = -" );
		Messenger.printMsg(Messenger.ERROR, "       The output is set to inputfilename.xml if outputfilename is not set." );
		System.exit(1);
	}
	
	/**
	 * Print out data read into the header (debug purpose)
	 */
	public void printHeader() {
		for(String s: description) {
			System.out.println("DESC  : " + s);
		}
		for(String s: header) {
			System.out.println("HEADER: " + s);
		}
		int i=0 ; 
		for(AttributeHandler ah : ahs) {
			System.out.println("AH    : " + ah.getNameorg() + " " + ah.getType()+ " " + ah.getUnit() + " " + ah.getUcd()+ " " + ah.getComment());
			i++;
		}
	}

}
