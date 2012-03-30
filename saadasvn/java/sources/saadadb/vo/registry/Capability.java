package saadadb.vo.registry;

import saadadb.command.ArgsParser;
import saadadb.command.EntityManager;
import saadadb.database.Database;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;


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
	private String dataTreePath;
	private String protocol;
	private String accessURL;
	private String description;
	
	public static final String TAP = "TAP";
	public static final String SIA = "SIA";
	public static final String SSA = "SSA";
	public static final String ConeSearch = "ConeSearch";
	
	/*
	 * accessors
	 */
	public String getDataTreePath() {
		return dataTreePath;
	}
	public void setDataTreePath(String dataTreePath) throws QueryException {
		if( dataTreePath== null ){
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "NULL data tree path not allowed");
		} else {
			this.dataTreePath = dataTreePath;
		}
	}
	public String getProtocol() {
		return protocol;
	}
	public void setProtocol(String protocol) throws QueryException {
		if( protocol== null ){
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "NULL protocol not allowed");
		} else {
			this.protocol = protocol;
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
	
	public void setAccessURL() throws QueryException {
		if( TAP.equals("this.protocol") ) {
			this.accessURL = Database.getUrl_root() + "/tap?";
		}
		else if( SIA.equals("this.protocol") ) {
			this.accessURL = Database.getUrl_root() + "/tap?";
		}
		
		if( accessURL== null ){
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "NULL accessURL not allowed");
		} else {
			this.accessURL = accessURL;
		}
	}
	
	/*
	@Override
	public void create(ArgsParser ap) throws SaadaException {
		readArgs(ap);

	}
	@Override
	public void empty(ArgsParser ap) throws SaadaException {
		readArgs(ap);

	}
	@Override
	public void remove(ArgsParser ap) throws SaadaException {
		readArgs(ap);

	}
	@Override
	public void populate(ArgsParser ap) throws SaadaException {
		readArgs(ap);

	}
	@Override
	public void index(ArgsParser ap) throws SaadaException {}
	@Override
	public void comment(ArgsParser ap) throws SaadaException {
		readArgs(ap);
	}
	/**
	 * @param ap
	 * @throws SaadaException
	 *
	private void readArgs(ArgsParser ap) throws SaadaException {
		this.setDataTreePath(ap.getCollection());
		this.setAccessURL(ap.getUrlroot());
		this.setProtocol(ap.getProtocol());
		this.setDescription(ap.getComment());
	}
	*/


}
