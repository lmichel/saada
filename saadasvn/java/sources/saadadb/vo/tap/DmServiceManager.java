package saadadb.vo.tap;

import saadadb.command.ArgsParser;
import saadadb.command.EntityManager;
import saadadb.database.Database;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.VOResource;
import saadadb.sqltable.SQLTable;
import saadadb.sqltable.Table_Saada_VO_DMVIew;
import saadadb.util.Messenger;

public class DmServiceManager extends EntityManager {
	private VOResource vor;
	/**
	 * @param name
	 * @throws Exception 
	 */
	public DmServiceManager(String name) throws Exception {
		super(name);
		vor = VOResource.getResource(name);
	}
	public DmServiceManager(VOResource vor) throws Exception {
		super(vor.getName());
		this.vor = vor;
	}
	public DmServiceManager() {
	}

	@Override
	public void create(ArgsParser ap) throws SaadaException {
		Messenger.printMsg(Messenger.TRACE, "Create DM view " + vor.getName());
		String className = ap.getCreate();
		if( className == null ) {
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER,"No class name given (param -create)");
		}
		try {
			Table_Saada_VO_DMVIew.createTable(vor);		
		} catch (Exception e) {
			AbortException.throwNewException(SaadaException.DB_ERROR, e);
		}
	}

	@Override
	public void empty(ArgsParser ap) throws SaadaException {
		Messenger.printMsg(Messenger.TRACE, "Empty DM view " + vor.getName());
		try {
			Table_Saada_VO_DMVIew.emptyTable(vor);
		} catch (Exception e) {
			AbortException.throwNewException(SaadaException.DB_ERROR, e);
		}
	}

	@Override
	public void remove(ArgsParser ap) throws SaadaException {
		Messenger.printMsg(Messenger.TRACE, "Drop DM view " + vor.getName());
		try {
			Table_Saada_VO_DMVIew.dropTable(vor);
		} catch (Exception e) {
			AbortException.throwNewException(SaadaException.DB_ERROR, e);
		}
	}

	@Override
	public void populate(ArgsParser ap) throws SaadaException {
		String className = ap.getPopulate();
		Messenger.printMsg(Messenger.TRACE, "Populate DM view " + vor.getName() + " with class " + className);
		if( className == null ) {
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER,"No class name given (param -populate)");
		}
		if( Database.getCachemeta().getClass(className) == null ){
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER,"Class " + className + " does not exists");
		}
		try {
			if( !SQLTable.tableExist(vor.getName())) {
				Messenger.printMsg(Messenger.TRACE, "Create table " + vor.getName());
				Table_Saada_VO_DMVIew.createTable(vor);
			} else {
				SQLTable.dropTableIndex(vor.getName(), this);
			}
			Messenger.printMsg(Messenger.TRACE, "Remove data from class " + className);
			Table_Saada_VO_DMVIew.removeClass(vor, className)	;
			Messenger.printMsg(Messenger.TRACE, "Copy data from class " + className);
			Table_Saada_VO_DMVIew.addClass(vor, className)	;
			SQLTable.commitTransaction();
			Messenger.printMsg(Messenger.TRACE, "Update sky pixels for class " + className );
			Table_Saada_VO_DMVIew.ComputeSkyPixels(vor, className, "s_ra", "s_dec");

		} catch (Exception e) {
			AbortException.throwNewException(SaadaException.DB_ERROR, e);
		}
	}

	@Override
	public void index(ArgsParser ap) throws SaadaException {
		Messenger.printMsg(Messenger.TRACE, "Index DM view " + vor.getName());
		SQLTable.indexTable(vor.getName(), this);
	}

	@Override
	public void comment(ArgsParser ap) throws SaadaException {
	}

}
