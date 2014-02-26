package saadadb.command;

import saadadb.exceptions.SaadaException;

public abstract class  EntityManager extends SaadaProcess {
	protected String name ;
	
	/**
	 * Although all method could be static, we need to make an instance to monitor the progress from 
	 * the GUI
	 */	
	public EntityManager(String name) {
		super(1);
		this.setName(name);
	}
	public EntityManager() {
		super(1);
	}
	public abstract void create(ArgsParser ap) throws SaadaException ;
	public abstract void rename(ArgsParser ap) throws SaadaException ;
	public abstract void empty(ArgsParser ap)  throws SaadaException ;
	public abstract void remove(ArgsParser ap) throws SaadaException ;
	public abstract void populate(ArgsParser ap) throws SaadaException ;
	public abstract void index(ArgsParser ap)   throws SaadaException ;
	public abstract void comment(ArgsParser ap) throws SaadaException ;
	public void setName(String name) {this.name = name;}

}
