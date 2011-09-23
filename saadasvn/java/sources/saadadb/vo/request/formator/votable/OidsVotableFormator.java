/**
 * 
 */
package saadadb.vo.request.formator.votable;

import java.util.ArrayList;

import cds.savot.model.SavotTR;
import cds.savot.model.TDSet;

import saadadb.collection.SaadaInstance;
import saadadb.database.Database;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.query.result.SaadaInstanceResultSet;
import saadadb.util.Messenger;

/**
 * @author laurent
 * @version 07/2011
 */
public class OidsVotableFormator extends SaadaqlVotableFormator {

	public OidsVotableFormator() throws QueryException {
		limit = 100000;
		protocolName = "Native Saada";
	}

	/* (non-Javadoc)
	 * @see saadadb.vo.request.formator.VOResultFormator#setResultSet(saadadb.query.result.SaadaInstanceResultSet)
	 */
	public void setResultSet(SaadaInstanceResultSet saadaInstanceResulmsgtSet) throws QueryException{			
		QueryException.throwNewException(SaadaException.UNSUPPORTED_MODE
				, "OidsVotableFormator does not support SaadaInstanceResultSet processing");
	}


	/* (non-Javadoc)
	 * @see saadadb.vo.request.formator.VOResultFormator#setResultSet(saadadb.query.result.SaadaInstanceResultSet)
	 */
	public void setResultSet(ArrayList<Long> oids) throws QueryException{			
		this.oids = oids;
		this.resultSize = oids.size();
	}

	/* (non-Javadoc)
	 * @see saadadb.vo.request.formator.votable.VOTableFormator#writeData()
	 */
	protected void writeData() throws Exception {
		writer.writeDataBegin();
		writer.writeTableDataBegin();
		int i=0 ;
		for( long oid: oids) {
			if( i >= this.limit ) {
				break;
			}
			SaadaInstance saadaInstance = Database.getCache().getObject(oid);
			i++;
			SavotTR savotTR = new SavotTR();					
			tdSet = new TDSet();
			this.writeRowData(saadaInstance);
			this.writeHouskeepingData(saadaInstance);
			//this.writeMappedUtypeData(oid);
			//this.writeAttExtendData(oid);
			this.writeExtReferences(saadaInstance);
			savotTR.setTDs(tdSet);
			writer.writeTR(savotTR);
			if( this.limit > 0 && i >= this.limit ) {
				Messenger.printMsg(Messenger.TRACE, "result truncated to " + i);
				break;
			}
		}
		writer.writeTableDataEnd();
		writer.writeDataEnd();
	}



}
