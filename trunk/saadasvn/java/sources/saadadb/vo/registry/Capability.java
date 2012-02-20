package saadadb.vo.registry;

import saadadb.command.ArgsParser;
import saadadb.command.EntityManager;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;


public class Capability extends EntityManager {
	private String dataTreePath;
	private String protocol;
	private String accessURL;
	private String description;
	
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
		// TODO Auto-generated method stub

	}

	/**
	 * @param ap
	 * @throws SaadaException
	 */
	private void readArgs(ArgsParser ap) throws SaadaException {
		this.setDataTreePath(ap.getCollection());
		this.setAccessURL(ap.getUrlroot());
		this.setProtocol(ap.getProtocol());
		this.setDescription(ap.getComment());
	}

}
