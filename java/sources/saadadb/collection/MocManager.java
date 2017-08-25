package saadadb.collection;

import cds.moc.HealpixMoc;
import hecds.region.request.Region;
import saadadb.collection.obscoremin.SaadaInstance;
import saadadb.command.ArgsParser;
import saadadb.command.EntityManager;
import saadadb.database.Database;
import saadadb.database.Repository;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.query.executor.Query;
import saadadb.query.result.SaadaQLResultSet;
import saadadb.util.Messenger;
import saadadb.util.SaadaConstant;

/**
 * Processing of the MOC management command
 * MOC only supported for images and at collection level
 * 
 * @author michel
 *
 */
public class MocManager extends EntityManager {

	/**
	 * 
	 */
	public MocManager() {
		super();
	}
	/**
	 * @param name
	 */
	public MocManager(String name) {
		super(name);
	}

	@Override
	public void create(ArgsParser ap) throws SaadaException {

		String collection = ap.getCreate();
		if( !Database.getCachemeta().collectionExists(collection) ) {
			FatalException.throwNewException(SaadaException.WRONG_PARAMETER, " Collection " + collection + " Does not exist");
		} else {
			try {
				RepositoryManager.checkMocDirectory(collection);
				Messenger.printMsg(Messenger.TRACE, "Building the MOC for collection " + collection + " can take a piece of time)");
				Query query = new Query();
				HealpixMoc globalMoc = new HealpixMoc();
				HealpixMoc m;
				/*
				 * Moc are built from the s_region of all images of the collection
				 */
				SaadaQLResultSet srs = query.runQuery("Select IMAGE From * " + " In " + collection);
				int i=0 ; 
				while( srs.next()) {
					long oid = srs.getOid();
					SaadaInstance instance = Database.getCache().getObject(oid);
					String stcregion = instance.getS_region();
					if( stcregion == null || stcregion.equals(SaadaConstant.STRING)) {
						continue;
					}

					m = ( new Region(stcregion, true)).getMoc(16);
					globalMoc.add(m);
					i++;
				}
				Messenger.printMsg(Messenger.TRACE, i + " images put into the MOC");

				String outputDir = Repository.getMocCollectionMocPath(collection);
				Messenger.printMsg(Messenger.TRACE, "Wrting MOC in " + outputDir);
				globalMoc.write(outputDir);
				srs.close();	
			} catch (SaadaException e) {
				FatalException.throwNewException(SaadaException.DB_ERROR, e);		
			} catch (Exception e) {
				Messenger.printStackTrace();
				FatalException.throwNewException(SaadaException.DB_ERROR, e);		
			}
		}
	}

	@Override
	public void remove(ArgsParser ap) throws SaadaException {
		String collection = ap.getRemove();
		if( !Database.getCachemeta().collectionExists(collection) ) {
			FatalException.throwNewException(SaadaException.WRONG_PARAMETER, " Collection " + collection + " Does not exist");
		} else {
			try {
				Messenger.printMsg(Messenger.TRACE, "Removing the MOC for collection " + collection);
				Repository.sweepMocDir(collection);
			} catch (SaadaException e) {
				FatalException.throwNewException(SaadaException.DB_ERROR, e);		
			} catch (Exception e) {
				Messenger.printStackTrace();
				FatalException.throwNewException(SaadaException.DB_ERROR, e);		
			}
		}

	}
	@Override
	public void rename(ArgsParser ap) throws SaadaException {
		Messenger.printMsg(Messenger.ERROR, "Not implemented for MOC");
	}
	@Override
	public void empty(ArgsParser ap) throws SaadaException {
		Messenger.printMsg(Messenger.ERROR, "Not implemented for MOC");
	}
	@Override
	public void populate(ArgsParser ap) throws SaadaException {
		Messenger.printMsg(Messenger.ERROR, "Not implemented for MOC");
	}
	@Override
	public void index(ArgsParser ap) throws SaadaException {
		Messenger.printMsg(Messenger.ERROR, "Not implemented for MOC");
	}
	@Override
	public void comment(ArgsParser ap) throws SaadaException {
		Messenger.printMsg(Messenger.ERROR, "Not implemented for MOC");
	}

}



