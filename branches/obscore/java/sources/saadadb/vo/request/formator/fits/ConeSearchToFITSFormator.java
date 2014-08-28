package saadadb.vo.request.formator.fits;

import java.util.Map;

import saadadb.collection.Category;
import saadadb.collection.obscoremin.EntrySaada;
import saadadb.collection.obscoremin.SaadaInstance;
import saadadb.database.Database;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.meta.UTypeHandler;
import saadadb.query.result.SaadaInstanceResultSet;
import saadadb.util.Messenger;

/**
 * @author laurent
 * @version 07/2011
 */
public class ConeSearchToFITSFormator extends FitsFormator {

	/**
	 * Constructor.
	 * @throws SaadaException 
	 */
	public ConeSearchToFITSFormator() throws QueryException {
		setDataModel("CS");
		limit = 100000;
		protocolName = "CS1.0";
	}


	/* (non-Javadoc)
	 * @see saadadb.vo.request.formator.VOResultFormator#setResultSet(saadadb.query.result.SaadaInstanceResultSet)
	 */
	public void setResultSet(SaadaInstanceResultSet saadaInstanceResultSet) throws QueryException{			
		this.saadaInstanceResultSet = saadaInstanceResultSet;
		this.resultSize = UNKNOWN_SIZE;
	}

	/* (non-Javadoc)
	 * @see saadadb.vo.request.formator.VOResultFormator#includeRelationInResponse(java.lang.String)
	 */
	public void includeRelationInResponse(String relationName) throws QueryException {
		if( this.supportResponseInRelation() ) {

		}
		else {
			QueryException.throwNewException(SaadaException.UNSUPPORTED_MODE, this.getClass() + " can not include linked data in its response");
		}
	}	
	/* (non-Javadoc)
	 * @see saadadb.vo.request.formator.VOResultFormator#supportResponseInRelation()
	 */
	public boolean supportResponseInRelation() {
		return false;
	}
	/* (non-Javadoc)
	 * @see saadadb.vo.request.formator.VOResultFormator#setProtocolParams(java.util.Map)
	 */
	public void setProtocolParams(Map<String, String> fmtParams) throws Exception{
		this.protocolParams = fmtParams;
		String rel, coll, cat;
		if( (coll = fmtParams.get("collection")) == null ) {
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "No collection  in formator parameters");
		}
		if( (cat = fmtParams.get("category")) == null ) {
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "No category in formator parameters");
		}
		/*
		 * Check relations to be include. They must exist in the collection/category
		 */
		if( (rel = fmtParams.get("relations")) != null ) {
			if( ! this.supportResponseInRelation() ) {
				QueryException.throwNewException(SaadaException.WRONG_PARAMETER, this.getClass() + " Does not support relations in relations");				
			}
			// Extract relation list
			String[] rls = rel.split("(,|;|:| )");
			boolean all = false;
			// Look for the any flag
			for( String r: rls) {
				if( r.toLowerCase().startsWith("any-") ) {
					all = true;
					break;
				}
			}
			// Get all candidate relations
			String[] drls = Database.getCachemeta().getRelationNamesStartingFromColl(coll, Category.getCategory(cat));
			// Take all if flag "any" is set
			if( all ) {
				for( String dr: drls ) {
					if (Messenger.debug_mode)
						Messenger.printMsg(Messenger.DEBUG, "Add relation " + dr + " in response");
					this.relationsToInclude.add(dr);							
					this.hasExtensions = true;
				}
			}
			// Match existing relations with requested relations
			else {
				for( String r: rls ) {
					boolean found = false;
					for( String dr: drls ) {
						if( dr.equals(r)) {
							found = true;
							break;
						}
					}
					if( !found ) {
						QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "Relation " + r + " does not existe in " + coll + "_" + cat);					
					} else {
						if (Messenger.debug_mode)
							Messenger.printMsg(Messenger.DEBUG, "Add relation " + r + " in response");
						this.relationsToInclude.add(r);
						this.hasExtensions = true;
					}
				}

			}
		}
	}


	/**
	 * @param oid
	 * @throws Exception
	 */
	protected void writeRowData(SaadaInstance si) throws Exception {
		EntrySaada obj = (EntrySaada)( si) ;
		int pos = 0;
		for( UTypeHandler sf: this.column_set) {
			Object data_column =  this.data[pos];
			String ucd   = sf.getUcd();
			String utype = sf.getUtype();
			String name  = sf.getNickname(); 

			if( ucd.equals("Target.Pos")) {
				((String[])data_column)[currentLine] = obj.s_ra + " " + obj.s_dec;
			}
			else if( utype.equals("Char.SpatialAxis.Coverage.Location.Value")) {
				((String[])data_column)[currentLine] = obj.s_ra + " " + obj.s_dec;
			}
			else if( ucd.equals("meta.ref.url")) {
				((String[])data_column)[currentLine] = Database.getUrl_root() + "/getinstance?oid=" + obj.oidsaada;
			}
			else if( utype.equals("Access.Format")) {
				((String[])data_column)[currentLine] = "catalog";
			}
			else if( ucd.equalsIgnoreCase("meta.title;meta.dataset") ) {
				((String[])data_column)[currentLine] = obj.obs_id.replaceAll("#", "");
			}
			else if( name.equals("LinktoPixels")) {
				((String[])data_column)[currentLine] = obj.getURL(true);
			}
			else if( ucd.equalsIgnoreCase("pos.eq.ra;meta.main") ){
				((double[])data_column)[currentLine] = (double)obj.s_ra;
			}
			else if( ucd.equalsIgnoreCase("pos.eq.dec;meta.mainN") ){
				((double[])data_column)[currentLine] = (double)obj.s_dec;
			}
			else if( ucd.equalsIgnoreCase("ID_MAIN") ){
				/*
				 * ID_MAIN is declared as String in the DM file
				 */
				((String[])data_column)[currentLine] = String.valueOf(obj.oidsaada);
			}
			else if( ucd.equalsIgnoreCase("meta.title") ){
				((String[])data_column)[currentLine] = obj.obs_id.replaceAll("#", "");
			}
			/*
			 * Utypes have an higher priority than UCDs: there are checked first
			 */
			else if( utype != null && utype.length() > 0 ){
				AttributeHandler ah  = obj.getFieldByUtype(sf.getUtype(), false);
				if( ah == null ) {
					((Object[])data_column)[currentLine] = "";					
				}
				else {
					Object val = obj.getFieldValue(ah.getNameattr());
					if( ah.getType().equals("String")) {
						((Object[])data_column)[currentLine] = val;
					}
					else {
						((Object[])data_column)[currentLine] = val;						
					}
				}	
			}
			else if( ucd != null && ucd.length() > 0 ){
				AttributeHandler ah  = obj.getFieldByUCD(sf.getUcd(), false);
				if( ah == null ) {
					((Object[])data_column)[currentLine] = "";					
				}
				else {
					Object val = obj.getFieldValue(ah.getNameattr());
					if( ah.getType().equals("String")) {
						((Object[])data_column)[currentLine] = val;
					}
					else {
						((Object[])data_column)[currentLine] = val;						
					}
				}
			}
			if( sf.getType().equals("String") && ((String[])data_column)[currentLine].length() == 0 ) {
				((Object[])data_column)[currentLine] = "Not Set";
			}
			pos++;
		}		
	}
	
	/* (non-Javadoc)
	 * @see saadadb.vo.request.formator.fits.FitsFormator#writeData()
	 */
	@Override
	protected void writeData() throws Exception {
		SaadaInstance si;
		int i=0 ;
		while( saadaInstanceResultSet.next() ) {
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


}
