package saadadb.vo.formator;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;

import nom.tam.fits.BinaryTableHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsFactory;
import nom.tam.fits.Header;
import nom.tam.util.BufferedFile;
import saadadb.collection.Category;
import saadadb.collection.SaadaInstance;
import saadadb.database.Database;
import saadadb.database.Repository;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.generationclass.SaadaClassReloader;
import saadadb.meta.UTypeHandler;
import saadadb.query.result.SaadaQLResultSet;
import saadadb.util.Messenger;
import saadadb.util.SaadaConstant;

/**
 * @author michel
 *@version $Id$
 */
public abstract class FITSFormatorList extends VOResultFormator {
	protected Fits fits ;
	protected int result_size;
	protected ArrayList<Object>[] data;
	protected int nb_columns;
	protected ArrayList<UTypeHandler> column_set = new ArrayList<UTypeHandler>();
	//protected int current_line;


	/**
	 * Constructor.
	 * @throws SaadaException 
	 */
	public FITSFormatorList(String voresource_name) throws SaadaException {
		super(voresource_name, "Cone Search default", "Saada Cone Search service", "dal:SimpleQueryResponse", "Cone Search search result on SaadaDB");
		FitsFactory.setUseAsciiTables(false);
	}

	/**
	 * Constructor.
	 * @throws SaadaException 
	 */
	public FITSFormatorList(String voresource_name, String result_filename) throws SaadaException {
		super(voresource_name, "Cone Search default", "Saada Cone Search service", "dal:SimpleQueryResponse", "Cone Search search result on SaadaDB", result_filename);
		FitsFactory.setUseAsciiTables(false);
	}

	/**
	 * @param votable_desc
	 * @param vores_type
	 * @param data_desc
	 * @param result_filename
	 */
	public FITSFormatorList(String votable_desc,String vores_type, String data_desc, String result_filename) {
		super(votable_desc, vores_type, data_desc, result_filename);		
		FitsFactory.setUseAsciiTables(false);
	}
	/**
	 * @param voresource_name
	 * @param default_resource
	 * @param votable_desc
	 * @param vores_type
	 * @param data_desc
	 * @throws SaadaException
	 */
	public FITSFormatorList(String voresource_name, String default_resource, String votable_desc,
			String vores_type, String data_desc) throws SaadaException {
		super(voresource_name, default_resource, votable_desc, vores_type, data_desc);
		FitsFactory.setUseAsciiTables(false);
	}


	/**
	 * @param voresource_name
	 * @param default_resource
	 * @param votable_desc
	 * @param vores_type
	 * @param data_desc
	 * @param result_filename
	 * @throws SaadaException
	 */
	public FITSFormatorList(String voresource_name, String default_resource, String votable_desc,
			String vores_type, String data_desc, String result_filename) throws SaadaException {
		super(voresource_name, default_resource, votable_desc, vores_type, data_desc, result_filename);
		FitsFactory.setUseAsciiTables(false);
	}


	/* (non-Javadoc)
	 * @see saadadb.vo.VOResultFormator#setDefaultReportFilename()
	 */
	public void setDefaultReportFilename() {
		this.result_filename = Database.getRepository() + Repository.getVoreportsPath()
		+  Database.getSepar() 
		+ "SaadaQL_Result" + System.currentTimeMillis()
		+ ".fits";	
	}

	/* (non-Javadoc)
	 * @see saadadb.vo.VOResultFormator#dumpResultFile(long[], java.io.Writer)
	 */
	public void dumpResultFile(OutputStream out) throws Exception {
		BufferedInputStream br = new BufferedInputStream(new FileInputStream(this.result_filename));
		int lg;
		byte[] target = new byte[1024];
		while( (lg = br.read(target)) > 0  ) {
			out.write(target, 0, lg);
		}
		out.flush();
		br.close();
	}

	/*
	 * NOUVEAUUUUUUUUUUUUUUUUUU
	 */	/**
	 * @param oids
	 * @return
	 * @throws Exception
	 */
	public String buildResultFile(long[] oids) throws Exception {
		if( this.limit > 0 && this.limit < oids.length ) {
			this.result_size = this.limit;
		}
		else {
			this.result_size = oids.length;
		}
		File recup = new File(this.result_filename);
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Build temporary results file");
		createResultFile(oids);
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "FITS file stored in <" + recup.getAbsolutePath() + ">");
		BufferedFile bf = new BufferedFile(recup, "rw");
		Messenger.printMsg(Messenger.TRACE, "Write file " + this.result_filename);
		fits.write(bf);
		bf.flush();
		bf.close();
		return recup.getAbsolutePath();
	}
	/* (non-Javadoc)
	 * @see saadadb.vo.VOResultFormator#buildResultErrorFile(java.lang.String)
	 */
	public String buildResultErrorFile(String cause) throws Exception {
		File recup = new File(this.result_filename);
		createResultErrorFile(cause);
		BufferedFile bf = new BufferedFile(recup, "rw");
		Messenger.printMsg(Messenger.TRACE, "Write file " + this.result_filename);
		fits.write(bf);
		bf.flush();
		bf.close();
		return recup.getAbsolutePath();
	}

	/* (non-Javadoc)
	 * @see saadadb.vo.VOResultFormator#buildResultFile(saadadb.query.SQLLikeResultSet)
	 */
	public String buildResultFile(SaadaQLResultSet srs) throws Exception {
		if( this.limit <srs.getSize() ) {
			this.result_size = this.limit;
		}
		else {
			this.result_size = srs.getSize();
		}
		File recup = new File(this.result_filename);
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Build temporary results file");
		createResultFile(srs);
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "FITS file stored in <" + recup.getAbsolutePath() + ">");
		BufferedFile bf = new BufferedFile(recup, "rw");
		Messenger.printMsg(Messenger.TRACE, "Write file " + this.result_filename);
		fits.write(bf);
		bf.flush();
		bf.close();
		return recup.getAbsolutePath();
	}
	/* (non-Javadoc)
	 * @see saadadb.vo.VOResultFormator#createResultFile(long[], int, java.lang.String, java.lang.String, java.lang.String, int)
	 */
	protected void createResultFile(long[] oids) throws Exception{
		FitsFactory.setUseAsciiTables(false);
		this.fits = new Fits();
		Messenger.printMsg(Messenger.TRACE, "Build table map");
		this.initDataArray();
		this.writeData(oids);		
		fits.addHDU(  Fits.makeHDU(this.data));
		Header primHDU = (Header)fits.getHDU(0).getHeader();
		primHDU.addLine("COMMENT 'Generated by Saada'");
		if( this.queryInfos != null ) {
			String query = this.queryInfos.getSaadaqlQuery();
			if( query != null ) {
				primHDU.addLine("COMMENT 'Query:");
				/*
				 * Split the query in multiple lines limited to 64 charatcers
				 */
				String[] phrases = query.split("\n");
				for( int i=0 ; i<phrases.length ; i++ ) {
					if( phrases[i].length() > 64 ) {
						primHDU.addLine("COMMENT '" + phrases[i].substring(0, 63) + "'");					
						int pos = 63;
						while( pos < phrases[i].length() ) {
							primHDU.addLine("COMMENT '" + phrases[i].substring(pos, pos+64) + "'");
							pos += 64;
						}
						primHDU.addLine("COMMENT '" +  phrases[i].substring(pos-64, phrases[i].length()) + "'");					
					}
					else {
						primHDU.addLine("COMMENT '" + phrases[i] + "'");					
					}
				}
			}
		}	
		writeMetaData(oids);	
	}

	/**
	 * @param cause
	 * @throws Exception
	 */
	protected void createResultErrorFile(String cause) throws Exception{
		FitsFactory.setUseAsciiTables(false);
		this.fits = new Fits();
		Messenger.printMsg(Messenger.TRACE, "Build table map");
		fits.addHDU(  Fits.makeHDU(this.data));
		Header primHDU = (Header)fits.getHDU(0).getHeader();
		primHDU.addLine("ERROR '" + cause + "'");
		primHDU.addLine("COMMENT 'Generated by Saada'");
		String query = this.queryInfos.getSaadaqlQuery();
		if( query != null ) {
			primHDU.addLine("COMMENT 'Query:");
			/*
			 * Split the query in multiple lines limited to 64 charatcers
			 */
			String[] phrases = query.split("\n");
			for( int i=0 ; i<phrases.length ; i++ ) {
				if( phrases[i].length() > 64 ) {
					primHDU.addLine("COMMENT '" + phrases[i].substring(0, 63) + "'");					
					int pos_start = 63;
					while( true ) {
						int pos_end = pos_start + 64;
						if( pos_end >= phrases[i].length()) pos_end = phrases[i].length() - 1;
						primHDU.addLine("COMMENT '" + phrases[i].substring(pos_start, pos_end) + "'");
						pos_start = pos_end + 1;
						if( pos_start >= phrases[i].length()) break;
					}
				}
				else {
					primHDU.addLine("COMMENT '" + phrases[i] + "'");					
				}
			}
		}	

	}

	/* (non-Javadoc)
	 * @see saadadb.vo.VOResultFormator#createResultFile(saadadb.query.SQLLikeResultSet)
	 */
	protected void createResultFile(SaadaQLResultSet srs) throws Exception{
		FitsFactory.setUseAsciiTables(false);
		this.fits = new Fits();
		Messenger.printMsg(Messenger.TRACE, "Build table map");
		this.initDataArray();
		this.writeData(srs);		
		fits.addHDU(Fits.makeHDU(this.data));
		Header primHDU = (Header)fits.getHDU(0).getHeader();
		primHDU.addLine("COMMENT 'Generated by Saada'");
		String query = this.queryInfos.getSaadaqlQuery();
		if( query != null ) {
			primHDU.addLine("COMMENT 'Query:");
			/*
			 * Split the query in multiple lines limited to 64 charatcers
			 */
			String[] phrases = query.split("\n");
			for( int i=0 ; i<phrases.length ; i++ ) {
				if( phrases[i].length() > 64 ) {
					primHDU.addLine("COMMENT '" + phrases[i].substring(0, 63) + "'");					
					int pos_start = 63;
					while( true ) {
						int pos_end = pos_start + 64;
						if( pos_end >= phrases[i].length()) pos_end = phrases[i].length() - 1;
						primHDU.addLine("COMMENT '" + phrases[i].substring(pos_start, pos_end) + "'");
						pos_start = pos_end + 1;
						if( pos_start >= phrases[i].length()) break;
					}
				}
				else {
					primHDU.addLine("COMMENT '" + phrases[i] + "'");					
				}
			}
		}	
		writeMetaData(srs);	
	}

	/**
	 * @throws Exception 
	 * 
	 */
	private void initDataArray() throws Exception {
		Messenger.printMsg(Messenger.TRACE, "Build table columns");
		String[] groups = vo_resource.groupNames();
		for( String group_name: groups ) {
			UTypeHandler[] uths = vo_resource.getGroupUtypeHandlers(group_name);
			for( UTypeHandler uth: uths) {
				/*
				 * Add a key word for each param 
				 */
				if(uth.getValue().length() == 0 || "null".equalsIgnoreCase(uth.getValue())) {
					this.column_set.add(uth);
				}
			}
		}		
		nb_columns = this.column_set.size();
		this.data = new ArrayList[nb_columns];
		for( int i=0 ; i<nb_columns ; i++ ) {
			this.data[i] = getValueVector(this.column_set.get(i));
		}

	}	/**
	 * @param oid
	 * @throws Exception
	 */

	@Override
	protected void writeData(long[] oids) throws Exception {
		if( oids != null  ) {
			//current_line = 0;
			for (int i = 0; i < oids.length; ++i) {
				long oid =  oids[i];
				SaadaInstance si = Database.getCache().getObject(oids[i]);
				this.writeDMData(si);
				this.writeHouskeepingData(si);
				//this.writeMappedUtypeData(oid);
				//this.writeAttExtendData(oid);
				this.writeExtReferences(si);
				//current_line++;
				if( this.limit > 0 && i >= this.limit ) {
					Messenger.printMsg(Messenger.TRACE, "result truncated to i");
					break;
				}
			}
		}
	}
	/* (non-Javadoc)
	 * @see saadadb.vo.VOResultFormator#writeData(saadadb.query.SQLLikeResultSet)
	 */
	protected void writeData(SaadaQLResultSet srs) throws Exception {
		SaadaInstance si;
		if( this.queryInfos.getQueryTarget() == QueryInfos.ONE_COLL_ONE_CLASS) {
			si = (SaadaInstance) SaadaClassReloader.forGeneratedName(this.queryInfos.getClassName()).newInstance();
		}
		else {
			si = (SaadaInstance) Class.forName("generated." + Database.getName() + "." + Category.explain(this.queryInfos.getCategory()) + "UserColl").newInstance();			
		}
		if( srs != null  ) {
			//current_line = 0;
			int i=0 ; 
			while(srs.next()) {
				if( i >= this.limit ) {
					break;
				}
				if( !isInMappedDmMode () ) {
					si.init(srs);
					this.writeDMData(si);
				}
				else {
					this.writeDMData(srs.getOid(), srs);
				}
				this.writeHouskeepingData(si);
				//this.writeMappedUtypeData(oid);
				//this.writeAttExtendData(oid);
				this.writeExtReferences(si);
				//current_line++;
				if( this.limit > 0 && i >= this.limit ) {
					Messenger.printMsg(Messenger.TRACE, "result truncated to i");
					break;
				}
				i++;
			}
		}
	}


	/* (non-Javadoc)
	 * @see saadadb.vo.VOResultFormator#writeExtReferences(long, int)
	 */
	protected void writeExtReferences(SaadaInstance si) {
	}

	/* (non-Javadoc)
	 * @see saadadb.vo.VOResultFormator#writeHouskeepingData(long)
	 */
	protected void writeHouskeepingData(SaadaInstance si) throws SaadaException {
	}

	/* (non-Javadoc)
	 * @see saadadb.vo.VOResultFormator#writeNativeValues(saadadb.collection.SaadaInstance)
	 */
	protected  void writeNativeValues(SaadaInstance obj) throws Exception {
		int pos = 0;
		for( UTypeHandler sf: this.column_set) {
			ArrayList<Object> data_column =  this.data[pos];
			String name  = sf.getNickname();
			String type  = sf.getType();
			if( name != null && name.length() > 0 ){
				Object val = null;
				/*
				 * Columns with names starting with ucd_ denote values returned by UCD based queries
				 * The real value must then be retrieved in the business object
				 */
				if( name.startsWith("ucd_")) {
					val = Database.getCache().getObject(obj.getOid()).getFieldValueByUCD(sf.getUcd(), false);
				}
				else {
					val = obj.getFieldValue(name);
				}
				if( name.equals("product_url_csa")) { 
					data_column.add(obj.getDownloadURL(true));
					//((Object[])data_column)[current_line] = obj.getDownloadURL(true);	
				}
				else if( type.equals("int") ) {
					data_column.add(val);
				}
				else if( type.equals("short") ) {
					data_column.add(val);
					//((short[])data_column)[current_line] = (Short)val;
				}
				else if( type.equals("byte") ) {
					data_column.add(val);
					//((byte[])data_column)[current_line] = (Byte)val;
				}
				else if( type.equals("long") ) {
					data_column.add(val);
					//((long[])data_column)[current_line] = (Long)val;
				}
				else if( type.equals("float") ) {
					data_column.add(val);
					//((float[])data_column)[current_line] = (Float)val;
				}
				else if( type.equals("double") ) {
					/*
					 * Saada ignored float right to 1.4.0.2. That causes cast errors 
					 * here when the report is built from a query result (skipping te API)
					 */
					/*
					 * In case of UCD based query, a pseudo column is set typed as double.
					 * This columns can receive any numeric type
					 */
					if( val instanceof Float) {
						data_column.add(val);
						//((double[])data_column)[current_line] = (Float)val;	
					}
					else if( val instanceof Byte) {
						data_column.add(val);
						//((double[])data_column)[current_line] = (Byte)val;	
					}
					else if( val instanceof Short) {
						data_column.add(val);
						//((double[])data_column)[current_line] = (Short)val;	
					}
					else if( val instanceof Integer) {
						data_column.add(val);
						//((double[])data_column)[current_line] = (Integer)val;	
					}
					else if( val instanceof Long) {
						data_column.add(val);
						//((double[])data_column)[current_line] = (Long)val;	
					}
					else {
						data_column.add(val);
						//((double[])data_column)[current_line] = (Double)val;
					}
				}
				else if( type.equals("boolean") ) {
					data_column.add(val);
					//((boolean[])data_column)[current_line] = (Boolean)val;
				}
				else if( type.equals("char") ) {
					if( sf.getArraysize() == 1 ) {
						data_column.add(val);
						//((char[])data_column)[current_line] = (Character)val;					
					}
					else {
						data_column.add(val);
						//((String[])data_column)[current_line] = (String)val;											
					}
				}
				else  {
					data_column.add(val);
					//((Object[])data_column)[current_line] = val;
				}
			}
			pos++;
		}

	}

	/* (non-Javadoc)
	 * @see saadadb.vo.VOResultFormator#writeDMData(long, saadadb.query.SQLLikeResultSet)
	 */
	protected  void writeDMData(long oid, SaadaQLResultSet rs) throws Exception {
		SaadaInstance si = Database.getCache().getObject(oid);
		si.activateDataModel(vo_resource.getName());
		int pos = 0;
		for( UTypeHandler sf: this.column_set) {
			ArrayList<Object> data_column =  this.data[pos];
			String name  = sf.getNickname();
			String type  = sf.getType();
			if( name != null && name.length() > 0 ){
				Object val = si.getFieldValue(sf.getNickname(), rs);
				if( type.equals("int") ) {
					data_column.add(((val == null )?SaadaConstant.INT: (Integer)val));
					//((int[])data_column)[current_line]   = ((val == null )?SaadaConstant.INT: (Integer)val);
				}
				else if( type.equals("short") ) {
					data_column.add(((val == null )?SaadaConstant.SHORT: (Short)val));
					//((short[])data_column)[current_line] = ((val == null )?SaadaConstant.SHORT: (Short)val);
				}
				else if( type.equals("byte") ) {
					data_column.add(((val == null )?SaadaConstant.BYTE: (Byte)val));
					//((byte[])data_column)[current_line]  = ((val == null )?SaadaConstant.BYTE: (Byte)val);
				}
				else if( type.equals("long") ) {
					data_column.add(((val == null )?SaadaConstant.LONG: (Long)val));
					//((long[])data_column)[current_line]  = ((val == null )?SaadaConstant.LONG: (Long)val);
				}
				else if( type.equals("float") ) {
					data_column.add(((val == null )?SaadaConstant.FLOAT: (Float)val));
					//((float[])data_column)[current_line] = ((val == null )?SaadaConstant.FLOAT: (Float)val);
				}
				else if( type.equals("double") ) {
					/*
					 * Saada ignored float right to 1.4.0.2. That causes cast errors 
					 * here when the report is built from a query result (skipping the API)
					 */
					/*
					 * In case of UCD based query, a pseudo column is set typed as double.
					 * This columns can receive any numeric type
					 */
					if( val instanceof Float) {
						data_column.add(((val == null )?SaadaConstant.FLOAT: (Float)val));
						//((double[])data_column)[current_line] = ((val == null )?SaadaConstant.FLOAT: (Float)val);	
					}
					else if( val instanceof Byte) {
						data_column.add(((val == null )?SaadaConstant.BYTE: (Byte)val));
						//((double[])data_column)[current_line] = ((val == null )?SaadaConstant.BYTE: (Byte)val);	
					}
					else if( val instanceof Short) {
						data_column.add(((val == null )?SaadaConstant.SHORT: (Short)val));
						//((double[])data_column)[current_line] = ((val == null )?SaadaConstant.SHORT: (Short)val);	
					}
					else if( val instanceof Integer) {
						data_column.add(((val == null )?SaadaConstant.INT: (Integer)val));
						//((double[])data_column)[current_line] = ((val == null )?SaadaConstant.INT: (Integer)val);	
					}
					else if( val instanceof Long) {
						data_column.add(((val == null )?SaadaConstant.LONG: (Long)val));
						//((double[])data_column)[current_line] = ((val == null )?SaadaConstant.LONG: (Long)val);	
					}
					else if( val instanceof String) {
						data_column.add(((val == null )?SaadaConstant.LONG: Double.parseDouble(val.toString())));
						//((double[])data_column)[current_line] = ((val == null )?SaadaConstant.LONG: Double.parseDouble(val.toString()));	
					}
					else if( val instanceof BigDecimal) {
						data_column.add(((val == null )?SaadaConstant.DOUBLE: ((BigDecimal)val).doubleValue() ));
						//((double[])data_column)[current_line] = ((val == null )?SaadaConstant.DOUBLE: ((BigDecimal)val).doubleValue() );	
					}
					else if( val instanceof Boolean) {
						if( val == null || val.toString().equals("false")) {
							data_column.add(0.0);
							//((double[])data_column)[current_line] = 0.0;
						}
						else {
							data_column.add(1.0);
							//((double[])data_column)[current_line] = 1.0;							
						}
					}
					else {
						if( val != null ) System.out.println(val.getClass());
						data_column.add(((val == null )?SaadaConstant.DOUBLE: new Double(val.toString())));
						//((double[])data_column)[current_line] = ((val == null )?SaadaConstant.DOUBLE: new Double(val.toString()));
					}
				}
				else if( type.equals("boolean") ) {
					data_column.add(((val == null )?false: (Boolean)val));
					//((boolean[])data_column)[current_line] = ((val == null )?false: (Boolean)val);
				}
				else if( type.equals("char") ) {
					if( sf.getArraysize() == 1 ) {
						data_column.add(((val == null )?SaadaConstant.CHAR: (Character)val));
						//((char[])data_column)[current_line] = ((val == null )?SaadaConstant.CHAR: (Character)val);					
					}
					else {
						data_column.add(((val == null )?SaadaConstant.STRING: (String)val));
						//((String[])data_column)[current_line] = ((val == null )?SaadaConstant.STRING: (String)val);											
					}
				}
				else  {
					data_column.add(((val == null )?SaadaConstant.STRING: (String)val));
					//((Object[])data_column)[current_line] = ((val == null )?SaadaConstant.STRING: (String)val);
				}
			}
			pos++;
		}

	}
	/* (non-Javadoc)
	 * @see saadadb.vo.VOResultFormator#writeExtMetaReferences(int)
	 */
	protected  void writeExtMetaReferences(int category) throws QueryException {
	}


	/* (non-Javadoc)
	 * @see saadadb.vo.VOResultFormator#writeHousekeepingFieldAndGroup()
	 */
	protected void writeHousekeepingFieldAndGroup() {
	}

	@Override
	public String buildMetadataFile() throws Exception {
		return null;
	}

	@Override
	protected void createMetadataResultFile() throws Exception {
	}

	@Override
	protected void writeProtocolParamDescription() {
	}

	/* (non-Javadoc)
	 * @see saadadb.vo.VOResultFormator#writeDMFieldsAndGroups(int)
	 */
	protected void writeDMFieldsAndGroups(int length) throws Exception {
		BinaryTableHDU bhdu = (BinaryTableHDU) fits.getHDU(1);
		Messenger.printMsg(Messenger.TRACE, "Build table columns");
		String[] groups = vo_resource.groupNames();
		int num = 0;
		for( String group_name: groups ) {
			UTypeHandler[] uths = vo_resource.getGroupUtypeHandlers(group_name);
			for( UTypeHandler uth: uths) {
				/*
				 * Add a key word for each param 
				 */
				if(uth.getValue().length() != 0 && !"null".equalsIgnoreCase(uth.getValue())) {
					String name="";
					String type = uth.getType();
					/*
					 * AS fields identifier as usually longer than 8, values are split in 2 keywords.
					 * one with the identifier and the one with the value. Both are linked with an incremental number.
					 */
					if( uth.getUtype().length() > 0 ) {
						bhdu.addValue("UTYPE_" + num, uth.getUtype(), "Utype of the next keyword");
						name = "VALUE_" + num;
					}
					else if( uth.getUcd().length() > 0 ) {
						bhdu.addValue("UCD_" + num, uth.getUcd(), "Ucd of the next keyword");
						name = "VALUE_" + num;
					}
					else if( uth.getNickname().length() > 8 ) {
						bhdu.addValue("NAME_" + num, uth.getNickname(), "Data model name of the next keyword");
						name = "VALUE_" + num;
					}
					else {
						name = uth.getNickname();
					}
					num++;
					if( type.equals("float") || type.equals("double") ) {
						bhdu.addValue(name, Double.parseDouble(uth.getValue()), uth.getComment());
					}
					else if( type.equals("int") || type.equals("short") ) {
						bhdu.addValue(name, Integer.parseInt(uth.getValue()), uth.getComment());
					}
					else if( type.equals("boolean") ) {
						bhdu.addValue(name, Boolean.parseBoolean(uth.getValue()), uth.getComment());
					}
					else{
						bhdu.addValue(name, uth.getValue().toString(), uth.getComment());
					}
				}
			}
		}
		for( int i=0 ; i<this.column_set.size() ; i++ ){
			bhdu.setColumnName(i,this.column_set.get(i).getNickname(), this.column_set.get(i).getComment());
		}
	}


	/**
	 * Return a table typed with "type" used to store a FITS table column
	 * @param typeHandler
	 * @return
	 * @throws Exception
	 */
	private ArrayList getValueVector(UTypeHandler typeHandler) throws Exception {
		String type = typeHandler.getType();
		int size = typeHandler.getArraysize();
		if( this.result_size == 0 ) {
			return new ArrayList();
		}
		else {
			 ArrayList retour;
			if( type.equals("int") ) {
				retour = new ArrayList<Integer>();
				//retour = new int[this.result_size];
			}
			else if( type.equals("short") ) {
				retour = new ArrayList<Short>();
				//retour = new short[this.result_size];
			}
			else if( type.equals("long") ) {
				retour = new ArrayList<Long>();
				//retour = new long[this.result_size];
			}
			else if( type.equals("float") ) {
				retour = new ArrayList<Float>();
				//retour = new float[this.result_size];
			}
			else if( type.equals("double") ) {
				retour = new ArrayList<Double>();
				//retour = new double[this.result_size];	
			}
			else if( type.equals("boolean") ) {
				retour = new ArrayList<Boolean>();
				//sretour = new boolean[this.result_size];
			}
			else if( type.equals("char")&& size == 1 ) {
				retour = new ArrayList<Character>();
				//retour = new char[this.result_size];
			}
			else if( type.equals("byte")&& size == 1 ) {
				retour = new ArrayList<Byte>();
				//retour = new byte[this.result_size];
			}
			else  {
				retour = new ArrayList<String>();
				//retour = new String[this.result_size];
			}
			return retour;
		}	
	}

	/**
	 * SOuvenir de debuggage
	 * @throws Exception
	 */
	public static void essai() throws Exception {
		FitsFactory.setUseAsciiTables(false);
		Fits f = new Fits();
		double[] v = new double[45];
		for( int i=0 ; i<v.length ; i++) {
			v[i] = Math.random();
		}
		Object[] data = new Object[1];
		data[0] = v;
		f.addHDU(Fits.makeHDU(data));

		BinaryTableHDU bhdu = (BinaryTableHDU) f.getHDU(1);
		bhdu.setColumnName(0,"doubles",null);
		BufferedFile bf = new BufferedFile("/home/michel/Desktop/cs.fits", "rw");
		f.write(bf);
		bf.flush();
		bf.close();
		System.exit(1);
	}
}
