package saadadb.vo.request.formator.fits;

import java.util.Map;

import saadadb.collection.SaadaInstance;
import saadadb.exceptions.QueryException;
import saadadb.meta.UTypeHandler;
import saadadb.query.result.SaadaInstanceResultSet;
import saadadb.util.Messenger;

/**
 * @author laurent
 *  * @version $Id$

 */
public class SaadaqlFitsFormator extends FitsFormator {

	public SaadaqlFitsFormator() throws QueryException {
		limit = 100000;
		protocolName = "Native Saada";
	}

	/* (non-Javadoc)
	 * @see saadadb.vo.request.formator.VOResultFormator#setResultSet(saadadb.query.result.SaadaInstanceResultSet)
	 */
	public void setResultSet(SaadaInstanceResultSet saadaInstanceResultSet) throws QueryException{			
		this.saadaInstanceResultSet = saadaInstanceResultSet;
		this.resultSize = UNKNOWN_SIZE;
	}


	/* (non-Javadoc)
	 * @see saadadb.vo.request.formator.VOResultFormator#setProtocolParams(java.util.Map)
	 */
	public void setProtocolParams(Map<String, String> fmtParams) throws Exception{
		this.protocolParams = fmtParams;
		String[] classes = this.protocolParams.get("class").split(",");
		if( classes.length == 1 && !classes[0].equals("*") ) {
		}
		else {
			setDataModel("native" + this.protocolParams.get("category")) ;
		}
	}


	/* (non-Javadoc)
	 * @see saadadb.vo.request.formator.fits.FitsFormator#writeData()
	 */
	@Override
	protected void writeData() throws Exception {
		SaadaInstance si;
		int i=0 ;
		System.out.println("couco");
		while( saadaInstanceResultSet.next() ) {
			System.out.println("couco");
			if( i >= this.limit ) {
				break;
			}
			i++;
			si = saadaInstanceResultSet.getInstance();
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
		this.realSize = currentLine;

	}

	@Override
	protected void writeRowData(SaadaInstance obj) throws Exception {
		int pos = 0;
		System.out.println("@@@ " );
		for( UTypeHandler sf: this.column_set) {
			Object data_column =  this.data[pos];
			String name  = sf.getNickname();
			String type  = sf.getType();
			System.out.println("@@@ " + name);
			if( name != null && name.length() > 0 ){
				Object val = null;
				/*
				 * Columns with names starting with ucd_ denote values returned by UCD based queries
				 * The real value must then be retrieved in the business object
				 */
				if( name.startsWith("ucd_")) {
					val = obj.getFieldValueByUCD(sf.getUcd(), false);
				}
				else {
					val = obj.getFieldValue(name);
				}
				System.out.println(val);
				if( name.equals("product_url_csa")) { 
					((Object[])data_column)[currentLine] = obj.getDownloadURL(true);	
				}
				else if( type.equals("int") ) {
					((int[])data_column)[currentLine] = (Integer)val;
				}
				else if( type.equals("short") ) {
					((short[])data_column)[currentLine] = (Short)val;
				}
				else if( type.equals("byte") ) {
					((byte[])data_column)[currentLine] = (Byte)val;
				}
				else if( type.equals("long") ) {
					((long[])data_column)[currentLine] = (Long)val;
				}
				else if( type.equals("float") ) {
					((float[])data_column)[currentLine] = (Float)val;
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
						((double[])data_column)[currentLine] = (Float)val;	
					}
					else if( val instanceof Byte) {
						((double[])data_column)[currentLine] = (Byte)val;	
					}
					else if( val instanceof Short) {
						((double[])data_column)[currentLine] = (Short)val;	
					}
					else if( val instanceof Integer) {
						((double[])data_column)[currentLine] = (Integer)val;	
					}
					else if( val instanceof Long) {
						((double[])data_column)[currentLine] = (Long)val;	
					}
					else {
						((double[])data_column)[currentLine] = (Double)val;
					}
				}
				else if( type.equals("boolean") ) {
					((boolean[])data_column)[currentLine] = (Boolean)val;
				}
				else if( type.equals("char") ) {
					if( sf.getArraysize() == 1 ) {
						((char[])data_column)[currentLine] = (Character)val;					
					}
					else {
						((String[])data_column)[currentLine] = (String)val;											
					}
				}
				else  {
					((Object[])data_column)[currentLine] = val;
				}
			}
			pos++;
		}
	}

}
