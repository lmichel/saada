package saadadb.vo.request.formator.fits;

import java.util.ArrayList;
import java.util.Map.Entry;

import nom.tam.fits.BinaryTableHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsFactory;
import nom.tam.fits.Header;
import nom.tam.util.BufferedFile;
import saadadb.collection.SaadaInstance;
import saadadb.database.Database;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.UTypeHandler;
import saadadb.query.result.SaadaInstanceResultSet;
import saadadb.util.Messenger;
import saadadb.util.SaadaConstant;
import saadadb.vo.request.formator.QueryResultFormator;

/**
 * @author michel
 * @version 07/2011
 */
public abstract class FitsFormator extends QueryResultFormator {
	protected Fits fits ;
	protected int nb_columns;
	protected ArrayList<UTypeHandler> column_set = new ArrayList<UTypeHandler>();
	protected int currentLine;
	protected int realSize;
	protected Object[] data;
	public static final int UNKNOWN_SIZE = SaadaConstant.INT; // mark the result size can not be get (FORWARD only)
	public FitsFormator() {
		this.defaultSuffix = QueryResultFormator.getFormatExtension("fits");
	}

	/* (non-Javadoc)
	 * @see saadadb.vo.request.formator.VOResultFormator#setResultSet(saadadb.query.result.SaadaInstanceResultSet)
	 */
	public void setResultSet(SaadaInstanceResultSet saadaInstanceResultSet) throws QueryException{					
		/*
		 * As we need to know the resultset size to build a FITS table, we have no other choice than copying oids in an array.
		 */
		this.saadaInstanceResultSet = null;;
		oids = new ArrayList<Long>();
		while(saadaInstanceResultSet.next()) {
			oids.add(saadaInstanceResultSet.getOId());
		}
		this.resultSize = oids.size();

	}

	/* (non-Javadoc)
	 * @see saadadb.vo.request.formator.VOResultFormator#buildDataResponse()
	 */
	public void buildDataResponse( ) throws Exception {	
		FitsFactory.setUseAsciiTables(false);
		this.fits = new Fits();
		this.initDataArray();
		this.writeData();	
		this.resizeData();
		this.fits.addHDU(Fits.makeHDU(this.data));
		this.writeProtocolParamDescription();
		this.writeMetaData();	
		this.writeFitsFileOnDisk();
	}

	/* (non-Javadoc)
	 * @see saadadb.vo.request.formator.VOResultFormator#buildDataResponse()
	 */
	public void buildMetaResponse( ) throws Exception {
		FitsFactory.setUseAsciiTables(false);
		this.fits = new Fits();
		this.initDataArray();
		this.fits.addHDU(  Fits.makeHDU(this.data));
		this.writeProtocolParamDescription();
		this.writeMetaData();	
		this.writeFitsFileOnDisk();
	}


	/**
	 * @throws Exception 
	 */
	private void initDataArray() throws Exception {
		Messenger.printMsg(Messenger.TRACE, "Build table columns");
		String[] groups = dataModel.groupNames();
		for( String group_name: groups ) {
			UTypeHandler[] uths = dataModel.getGroupUtypeHandlers(group_name);
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
		this.data = new Object[nb_columns];
		for( int i=0 ; i<nb_columns ; i++ ) {
			this.data[i] = getValueVector(this.column_set.get(i));
		}

	}	
	/**
	 * @throws Exception
	 */
	protected void writeData() throws Exception {
		SaadaInstance si;
		System.out.println("foprtm");
		if( oids != null  ) {
			int i=0 ;
			for (long oid: oids ) {
				if( i >= this.limit ) {
					break;
				}
				i++;
				si = Database.getCache().getObject(oid);
				this.writeRowData(si);
				this.writeHouskeepingData(si);
				//this.writeMappedUtypeData(oid);
				//this.writeAttExtendData(oid);
				this.writeExtReferences(si);
				currentLine++;
				if( this.limit > 0 && i >= this.limit ) {
					Messenger.printMsg(Messenger.TRACE, "result truncated to i");
					break;
				}
				i++;
			}
		}
		this.realSize = currentLine;
	}

	/* (non-Javadoc)
	 * @see saadadb.vo.request.formator.Formator#buildErrorResponse(java.lang.Exception)
	 */
	public void buildErrorResponse(Exception e) throws Exception {
		FitsFactory.setUseAsciiTables(false);
		this.fits = new Fits();
		Messenger.printMsg(Messenger.TRACE, "Build table map");
		fits.addHDU(  Fits.makeHDU(new Object[0]));
		Header primHDU = (Header)fits.getHDU(0).getHeader();
		primHDU.addLine("ERROR '" + e.toString() + "'");
		primHDU.addLine("COMMENT 'Generated by Saada'");
		this.writeProtocolParamDescription();
	}	

	/**
	 * Add to current row data taken on the object si
	 * @param obj
	 * @throws Exception
	 */
	abstract protected void writeRowData(SaadaInstance obj) throws Exception;

	/* (non-Javadoc)
	 * @see saadadb.vo.request.formator.VOResultFormator#writeExtReferences(saadadb.collection.SaadaInstance)
	 */
	protected void writeExtReferences(SaadaInstance si) {
	}

	/* (non-Javadoc)
	 * @see saadadb.vo.request.formator.VOResultFormator#writeExtMetaReferences()
	 */
	protected void writeExtMetaReferences() throws QueryException {
	}
	/* (non-Javadoc)
	 * @see saadadb.vo.request.formator.VOResultFormator#writeHouskeepingData(saadadb.collection.SaadaInstance)
	 */
	protected void writeHouskeepingData(SaadaInstance si) throws SaadaException {
	}


	/* (non-Javadoc)
	 * @see saadadb.vo.request.formator.VOResultFormator#writeHousekeepingFieldAndGroup()
	 */
	protected void writeHousekeepingFieldAndGroup() {
	}

	/* (non-Javadoc)
	 * @see saadadb.vo.request.formator.VOResultFormator#writeProtocolParamDescription()
	 */
	@Override
	protected void writeProtocolParamDescription() throws Exception {
		Header primHDU = (Header)fits.getHDU(0).getHeader();
		primHDU.addLine("COMMENT 'Generated by Saada'");
		for(Entry<String, String> s: this.protocolParams.entrySet() ) {
			String value = s.getValue();
			if( value.length() < 64 ) {
				primHDU.addLine("COMMENT '" + s.getKey() + "' '" + value);					
			}
			else {
				primHDU.addLine("COMMENT '" +  s.getKey() + ":");
				/*
				 * Split the query in multiple lines limited to 64 charatcers
				 */
				String[] phrases = value.split("\n");
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

	}

	/* (non-Javadoc)
	 * @see saadadb.vo.request.formator.VOResultFormator#writeDMFieldsAndGroups()
	 */
	protected void writeDMFieldsAndGroups() throws Exception {
		BinaryTableHDU bhdu = (BinaryTableHDU) fits.getHDU(1);
		Messenger.printMsg(Messenger.TRACE, "Build table columns");
		String[] groups = dataModel.groupNames();
		int num = 0;
		for( String group_name: groups ) {
			UTypeHandler[] uths = dataModel.getGroupUtypeHandlers(group_name);
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
	private Object getValueVector(UTypeHandler typeHandler) throws Exception {
		String type = typeHandler.getType();
		int size = typeHandler.getArraysize();
		int supposedSize;
		if( this.resultSize == 0 ) {
			return new String[0];
		}
		if( this.resultSize == UNKNOWN_SIZE ) {
			supposedSize = this.limit;;
		}
		else {
			supposedSize = this.resultSize;
		}
		Object retour;
		if( type.equals("int") ) {
			retour = new int[supposedSize];
		}
		else if( type.equals("short") ) {
			retour = new short[supposedSize];
		}
		else if( type.equals("long") ) {
			retour = new long[supposedSize];
		}
		else if( type.equals("float") ) {
			retour = new float[supposedSize];
		}
		else if( type.equals("double") ) {
			retour = new double[supposedSize];	
		}
		else if( type.equals("boolean") ) {
			retour = new boolean[supposedSize];
		}
		else if( type.equals("char")&& size == 1 ) {
			retour = new char[supposedSize];
		}
		else if( type.equals("byte")&& size == 1 ) {
			retour = new byte[supposedSize];
		}
		else  {
			retour = new String[supposedSize];
		}
		return retour;
	}	

	/**
	 * Resize the Binary table to the result size if needed.
	 */
	private void resizeData() {
		if( this.resultSize != this.realSize ) {
			Messenger.printMsg(Messenger.TRACE, "Resize binary table from " + java.lang.reflect.Array.getLength(this.data[0]) + " to " + this.realSize);
			for( int i=0 ; i<this.data.length ; i++ ) {
				this.data[i] = resizeArray(this.data[i], this.realSize);
			}
		}


	}

	/**
	 * @param oldArray
	 * @param newSize
	 * @return
	 */
	private static Object resizeArray (Object oldArray, int newSize) {
		int oldSize = java.lang.reflect.Array.getLength(oldArray);
		@SuppressWarnings("rawtypes")
		Class elementType = oldArray.getClass().getComponentType();
		Object newArray = java.lang.reflect.Array.newInstance(
				elementType,newSize);
		int preserveLength = Math.min(oldSize,newSize);
		if (preserveLength > 0)
			System.arraycopy (oldArray,0,newArray,0,preserveLength);
		return newArray; }


	/**
	 * @throws Exception
	 */
	private void writeFitsFileOnDisk() throws Exception {
		BufferedFile bf = new BufferedFile(this.getResponseFilePath(), "rw");
		Messenger.printMsg(Messenger.TRACE, "Write file " + this.getResponseFilePath());
		this.fits.write(bf);
		bf.flush();
		bf.close();
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
