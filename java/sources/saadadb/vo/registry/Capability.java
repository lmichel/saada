package saadadb.vo.registry;

import saadadb.admintool.utils.DataTreePath;
import saadadb.database.Database;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.vocabulary.enums.VoProtocol;


/**
 * Class modeling one vo CAPABILITy.
 * A capability is defined by a data tree path (coll-cat-class), a protocol, a baseurl and an description
 * Capabilities are mapped in the table saadadb_vo_capability
 * 
 * @author michel
 * @version $Id$
 *
 */
public class Capability  {
	private DataTreePath dataTreePath;
	private VoProtocol protocol;
	private String accessURL;
	private String description;
		
	/*
	 * accessors
	 */
	public String getDataTreePathString() {
		return dataTreePath.toString();
	}
	public DataTreePath getDataTreePath() {
		return dataTreePath;
	}
	public void setDataTreePath(DataTreePath dataTreePath) throws QueryException {
		if( dataTreePath== null ){
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "NULL data tree path not allowed");
		} else {
			this.dataTreePath = dataTreePath;
		}
	}
	public void setDataTreePath(String dataTreePath) throws QueryException {
		String[] f = dataTreePath.split("\\.");
		if( f.length < 2 ) {
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "Badly formed datatreepath: " + dataTreePath);			
		}
		String col = f[0];
		String cat = f[1];
		String cla = ( f.length > 2 )? f[2]: null;
		this.dataTreePath = new DataTreePath(col, cat, cla);
	}
	public VoProtocol getProtocol() {
		return protocol;
	}
	public void setProtocol(VoProtocol protocol) throws QueryException {
		if( protocol== null ){
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "NULL protocol not allowed");
		} else {
			this.protocol = protocol;
		}
	}
	/**
	 * @param protocol
	 * @throws QueryException
	 */
	public void setProtocol(String protocol) throws QueryException {
		if( protocol== null ){
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "NULL protocol not allowed");
		} else if( protocol.equals(VoProtocol.SIA.toString())){
			this.protocol = VoProtocol.SIA;
		} else if( protocol.equals(VoProtocol.SSA.toString())){
			this.protocol = VoProtocol.SSA;
		} else if( protocol.equals(VoProtocol.ConeSearch.toString())){
			this.protocol = VoProtocol.ConeSearch;
		} else if( protocol.equals(VoProtocol.TAP.toString())){
			this.protocol = VoProtocol.TAP;
		} else if( protocol.equals(VoProtocol.ObsTAP.toString())){
			this.protocol = VoProtocol.ObsTAP;
		} else {
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "Unkwon protocol: " + protocol);
		}
	}
	public String getAccessURL() {
		return accessURL;
	}
	public void setAccessURL(String accessURL) throws QueryException {
		if( accessURL== null ){
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "NULL accessURL not allowed");
		} else {
			this.accessURL = accessURL;
		}
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		if( description== null || "null".equalsIgnoreCase(description)){
			this.description = "";
		} else {
			this.description = description;
		}
	}
	
	/**
	 * @throws QueryException
	 */
	public void setAccessURL() throws QueryException {
		switch (this.protocol) {
		case TAP:
			this.accessURL = Database.getUrl_root() + "/tap?";
			break;
		case SSA:
			this.accessURL = Database.getUrl_root() + "/ssa?";
			break;
		case SIA:
			this.accessURL = Database.getUrl_root() + "/sia?";
			break;
		case ConeSearch:
			this.accessURL = Database.getUrl_root() + "/sia?";
			break;
		default:
			break;
		}
	}
	
	public String toString(){
		return this.protocol + " " + this.dataTreePath + " " + this.accessURL + " " + this.description;
	}
	
}
