package saadadb.generationclass;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.LinkedHashMap;
import java.util.Vector;

import saadadb.database.Database;
import saadadb.database.Repository;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.VOResource;
import saadadb.util.Messenger;
import cds.savot.model.ParamSet;
import cds.savot.model.SavotParam;
import cds.savot.model.SavotResource;
import cds.savot.model.SavotTD;
import cds.savot.model.SavotTR;
import cds.savot.model.SavotTable;
import cds.savot.model.SavotVOTable;
import cds.savot.model.TDSet;
import cds.savot.pull.SavotPullEngine;
import cds.savot.pull.SavotPullParser;

/**
 * Add DM accessors to a class
 * @author michel
 * @version $Id: DMImplementer.java 915 2014-01-29 16:59:00Z laurent.mistahl $
 *
 */
public class DMImplementer {
	private String[] imports = new String[]{"java.util.LinkedHashMap", "saadadb.meta.DMInterface"
			, "saadadb.query.result.SaadaQLResultSet", "java.sql.SQLException", "saadadb.exceptions.FatalException"};
	private VOResource vor;
	private String classname;

	public DMImplementer(String mappingfilepath) throws Exception {
		this.vor = null;
		this.classname = "";
		loadMappingFile(mappingfilepath);
	}
	
	/** @param mappingfilepath
	 * @throws Exception
	 */
	public void loadMappingFile(String mappingfilepath) throws Exception {
		Messenger.printMsg(Messenger.TRACE, "Loading DM mapping file <" + mappingfilepath + ">");
		LinkedHashMap<String, String> retour = new LinkedHashMap<String, String>();
		SavotPullParser parser;
		/*
		 * Fullpath case
		 */
		if( mappingfilepath.indexOf(Database.getSepar()) >= 0 ) {
			parser = new SavotPullParser(mappingfilepath, SavotPullEngine.ROWREAD);				
		}
		/*
		 * filename case
		 */
		else {
			parser = new SavotPullParser(Database.getConnector().getRoot_dir() + Database.getSepar() + "config" + Database.getSepar() + mappingfilepath, SavotPullEngine.ROWREAD);	
		}
		/*
		 * Requested to init Savot
		 */
		SavotTR currentTR = parser.getNextTR();  
		SavotVOTable   voTable = parser.getVOTable();
		SavotResource currentResource = (SavotResource) (voTable.getResources().getItemAt(0));
		SavotTable stable = (SavotTable) currentResource.getTables().getItemAt(0);
		ParamSet ps = stable.getParams();
		for( int i=0 ; i<ps.getItemCount() ; i++ ) {
			SavotParam sp = (SavotParam) ps.getItemAt(i);
			if( sp.getName().equals("dm")  ) {
				this.vor = Database.getCachemeta().getVOResource(sp.getValue());
			}
			else if( sp.getName().equals("classname")  ) {
				this.classname = sp.getValue();
			}
		}
		if( vor == null || this.classname.equals("") ) {
			FatalException.throwNewException(SaadaException.FILE_FORMAT, "DM table <" + mappingfilepath + "> param <dm> or <classname> do not exist or has no value");
			return ;
		}
		int cpt = 1;
		do {  
			if( currentTR == null ) {
				Messenger.printMsg(Messenger.WARNING, "DM table <" + mappingfilepath + "> has no data");
				break;
			}
			TDSet tds = currentTR.getTDSet();
			@SuppressWarnings("rawtypes")
			Vector tdv = tds.getItems();
			if( tdv.size() != 2 ) {
				FatalException.throwNewException(SaadaException.FILE_FORMAT, "DM file <" + mappingfilepath + "> badly formated at <TR> #" + cpt); 
				return ;
			}
			retour.put( ((SavotTD)(tdv.get(0))).getContent().toString()
					  , ((SavotTD)(tdv.get(1))).getContent().toString());
			cpt++;
		}while( (currentTR = parser.getNextTR()) != null );
		this.vor.storeClassMapping(retour);
	}
	
	/**
	 * @param classpath
	 * @throws Exception
	 */
	public void putDMInJavaClass(String classpath)throws Exception{
		Messenger.printMsg(Messenger.TRACE, "Insert inner DM in class " + this.classname);
		String java_filename    = classpath + Database.getSepar() + classname + ".java";
		String java_bu_filename = classpath + Database.getSepar() + classname + ".java.org";
		(new File(java_filename)).renameTo(new File(java_bu_filename));
		BufferedReader br = new BufferedReader(new FileReader(java_bu_filename));
		FileWriter     fw = new FileWriter(java_filename);
		String boeuf;
		boolean import_done= false;			
		
		boolean into_former_interface = false;

		while((boeuf = br.readLine()) != null ) {
			if( !import_done && boeuf.trim().startsWith("import") ) {
				for ( int i=0 ; i<imports.length ; i++ ) {
					if( imports[i] != null && boeuf.indexOf(imports[i]) > 0 ) {
						imports[i] = null;
						break;
					}
				}
				fw.write(boeuf + "\n");
				continue;
			}
			/*
			 * Skip the former interface for our DM
			 */
			if( !into_former_interface && boeuf.trim().matches("\\s*\\/\\/\\s*DM <" + vor.getName() + "> begins.*") ) {
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Remove former mapping of DM <" + vor.getName() + "> detected in Java file.");
				into_former_interface = true;
			}
			if( boeuf.trim().startsWith("public class " + this.classname) ) {
				insertImports(fw);
				import_done = true;
				if( !into_former_interface ) {
					fw.write(boeuf+ "\n");
				}
				fw.write(vor.getDMInterfaceCode() + "\n");

			}
			else if( !into_former_interface ) {
				fw.write(boeuf+ "\n");				
			}
			if( into_former_interface && boeuf.trim().matches("\\s*\\/\\/\\s*DM <" + vor.getName() + "> ends.*") ) {
				into_former_interface = false;
			}
		}
		br.close();
		fw.close();
		Compile.compileItWithAnt(Database.getRoot_dir(), classname, (Repository.LOGS_PATH));  	
		}
	
	/**
	 * @param fw
	 * @throws Exception
	 */
	private void  insertImports(FileWriter fw) throws Exception{
		boolean commented = false;
		for ( int i=0 ; i<imports.length ; i++ ) {
			if( imports[i] != null ) { 
				if( !commented ) {
					fw.write("/*\n");
					fw.write(" * Import(s) added with implentatiopn of the DM " + vor.getName()  +"\n");
					fw.write(" */\n");
					commented = true;
				}
				fw.write("import " + imports[i] + ";\n");
			}
		}
	}

	public static void main(String[] args) {
		Messenger.debug_mode = false;
		Database.init("DEVBENCH1_5_1");
		try {
			new DMImplementer("/home/michel/saada/deploy/TestBench1_5_1/saadadbs/DEVBENCH1_5_1/config/dmmap._ssa_default.UCDTester1_UCDTagged2Entry.xml").putDMInJavaClass(Database.getClassLocation());
		} catch (Exception e) {
			e.printStackTrace();
		}
		Database.close();
	}
}
