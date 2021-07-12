package saadadb.products.updaters;

import java.io.File;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import saadadb.collection.Category;
import saadadb.collection.SaadaOID;
import saadadb.collection.obscoremin.SaadaInstance;
import saadadb.command.ArgsParser;
import saadadb.command.EntityManager;
import saadadb.database.Database;
import saadadb.database.Repository;
import saadadb.dataloader.SchemaMapper;
import saadadb.dataloader.mapping.ProductMapping;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.products.Image2DBuilder;
import saadadb.products.MiscBuilder;
import saadadb.products.ProductBuilder;
import saadadb.products.SpectrumBuilder;
import saadadb.products.datafile.DataFile;
import saadadb.products.datafile.DataResourcePointer;
import saadadb.products.reporting.MappingReport;
import saadadb.query.executor.Query;
import saadadb.query.result.OidsaadaResultSet;
import saadadb.sqltable.SQLQuery;
import saadadb.sqltable.SQLTable;
import saadadb.util.Merger;
import saadadb.util.Messenger;

/**
 * This class process the re-computing of the collection attributes of a set of
 * loaded products Cannot be applied to TABLEs Only the
 * {@link EntityManager#populate(ArgsParser)} method is implemented Keywords
 * listed in {@link unSetableAttributes} are not updated The data loader
 * parameters used by default are those stored in the saada_class when the class
 * has been created They can be overidden by the params given to the command and
 * listed on {@link allowedArgs} If the the param -nop is present, a report
 * combining both old and new value is printed out
 * 
 * @author michel
 *
 */
public class ProjectionUpdater extends EntityManager {
	private Set<Long> oids;
	private static Set<String> unSetableAttributes;
	private static Set<String> allowedArgs;
	/*
	 * Command line parameters to be used to redo the mapping
	 */
	private String[] newArgs;
	private boolean writeMode = false;

	static {
		unSetableAttributes = new TreeSet<>();
		unSetableAttributes.add("oidsaada");
		unSetableAttributes.add("repository_location");
		unSetableAttributes.add("date_load");
		unSetableAttributes.add("access_url");

		allowedArgs = new TreeSet<>();
		allowedArgs.add("-obsmapping");
		allowedArgs.add("-name");
		allowedArgs.add("-obsid");
		allowedArgs.add("-ename");
		allowedArgs.add("-obscollection");
		allowedArgs.add("-facility");
		allowedArgs.add("-instrument");
		allowedArgs.add("-target");
		allowedArgs.add("-caliblevel");
		allowedArgs.add("-publisherdid");
		/*
		 * Space Axe
		 */
		allowedArgs.add("-posmapping");
		allowedArgs.add("-system");
		allowedArgs.add("-position");
		allowedArgs.add("-poserror");
		allowedArgs.add("-sresolution");
		allowedArgs.add("-sfov");
		allowedArgs.add("-sregion");
		/*
		 * Energy Axe
		 */
		allowedArgs.add("-spcmapping");
		allowedArgs.add("-spcunit");
		allowedArgs.add("-spccolumn");
		allowedArgs.add("-emin");
		allowedArgs.add("-emax");
		allowedArgs.add("-ebins");
		allowedArgs.add("-emax");
		allowedArgs.add("-spcrespower");
		/*
		 * Time Axe
		 */
		allowedArgs.add("-timemapping");
		allowedArgs.add("-tmin");
		allowedArgs.add("-tmax");
		allowedArgs.add("-exptime");
		allowedArgs.add("-tresol");
		/*
		 * Observable Axe
		 */
		allowedArgs.add("-observablemapping");
		allowedArgs.add("-oucd");
		allowedArgs.add("-ounit");
		allowedArgs.add("-ocalibstatus");
		/*
		 * Polarization axis
		 */
		allowedArgs.add("-polarmapping");
		allowedArgs.add("-polarstates");
	}

	@Override
	public void create(ArgsParser ap) throws SaadaException {
		Messenger.printMsg(Messenger.ERROR, "Not implemented for products");
	}

	@Override
	public void rename(ArgsParser ap) throws SaadaException {
		Messenger.printMsg(Messenger.ERROR, "Not implemented for products");
	}

	@Override
	public void empty(ArgsParser ap) throws SaadaException {
		Messenger.printMsg(Messenger.ERROR, "Not implemented for products");
	}

	@Override
	public void remove(ArgsParser ap) throws SaadaException {
		Messenger.printMsg(Messenger.ERROR, "Not implemented for products");
	}

	@Override
	public void index(ArgsParser ap) throws SaadaException {
		Messenger.printMsg(Messenger.ERROR, "Not implemented for products");
	}

	@Override
	public void comment(ArgsParser ap) throws SaadaException {
		Messenger.printMsg(Messenger.ERROR, "Not implemented for products");
	}

	/**
	 * Extract from the command parameters those which must be used to override
	 * the params initially used to update the projections
	 * 
	 * @param ap
	 */
	private void mergeArgs(ArgsParser ap) {
		String[] args = ap.getArgs();
		List<String> newArgArray = new ArrayList<String>();
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			String argName = arg.split("=")[0];
			/*
			 * Keywords starting with -wcs are unlimited
			 */
			if (allowedArgs.contains(argName) || argName.startsWith("-wcs.")) {
				newArgArray.add(arg);
				if (argName.equals("-ukw") || argName.equals("-eukw")) {
					newArgArray.add(args[++i]);
				}
			}
		}
		this.newArgs = new String[newArgArray.size()];
		for (int i = 0; i < newArgArray.size(); i++) {
			this.newArgs[i] = newArgArray.get(i);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see saadadb.command.EntityManager#populate(saadadb.command.ArgsParser)
	 */
	@Override
	public void populate(ArgsParser ap) throws SaadaException {
		try {
			String query = ap.getPopulate().trim();
			this.writeMode = ( ap.isNop())? false:true;
			this.mergeArgs(ap);
			/*
			 * The populate param can be SaadaQL query....
			 */
			if (query.startsWith("Select")) {
				computeProjections(query);
				/*
				 * Or a list of oids
				 */
			} else {
				String[] soids = query.split("[,;]");
				long oids[] = new long[soids.length];
				for (int j = 0; j < soids.length; j++) {
					oids[j] = Long.parseLong(soids[j]);
				}
				computeProjections(oids);
			}
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			AbortException.throwNewException(SaadaException.DB_ERROR, e);
		}
	}

	/**
	 * Set the OIDs to be processed from the query result
	 * 
	 * @param query
	 * @throws SaadaException
	 */
	public final void computeProjections(String query) throws Exception {
		try {
			Query q = new Query();
			OidsaadaResultSet rs = q.runBasicQuery(query);
			Messenger.printMsg(Messenger.TRACE, "Update projections for OIDs returned by " + query);
			this.oids = new TreeSet<Long>();
			while (rs.next()) {
				this.oids.add(rs.getOId());
			}
			computeProjections();
		} catch (AbortException e) {
			AbortException.throwNewException(SaadaException.DB_ERROR, e);
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			AbortException.throwNewException(SaadaException.DB_ERROR, e);
		}

	}

	/**
	 * Set the OIDs to be processed from the list given as param
	 * 
	 * @param oids
	 * @throws SaadaException
	 */
	public final void computeProjections(long[] oids) throws Exception {
		this.oids = new TreeSet<Long>();
		for (long oid : oids) {
			this.oids.add(oid);

		}
		computeProjections();
	}

	/**
	 * Update the projection for the OIDs stored in the class
	 * @param oids
	 * @throws Exception
	 */
	private final void computeProjections() throws Exception {
		/*
		 * Nothing to do: return
		 */
		if( this.oids == null || this.oids.size() == 0 ) {
			Messenger.printMsg(Messenger.WARNING, "The list of oids to process is empty" );
			return;
		}
		/*
		 * This method can only work if all oids are from the same collection/category.
		 */
		this.checkOidList();
		/*
		 * Nothing to do
		 */
		if( this.oids.size() == 0 ){
			Messenger.printMsg(Messenger.WARNING, "No selected products" );	
			return;
		}

		String category  = null;
		String collection  = null;
		int catNum = 0;
		String classe  = null;
		String newClasse  = null;

		for( long oid: this.oids) {
			category    = SaadaOID.getCategoryName(oid);
			catNum      = SaadaOID.getCategoryNum(oid);
			collection  = SaadaOID.getCollectionName(oid);
			classe      = SaadaOID.getClassName(oid);
		}

		Map<String, AttributeHandler> attMap = Database.cachemeta.getCollectionAttributeHandlers(catNum);
		/*
		 * Not implemented yet
		 */
		if( category.equals("TABLE") ){
			AbortException.throwNewException(SaadaException.DB_ERROR, "Projections cannot be recomputed for TABLEs");
		}
		Repository.getTmpPath();
		/*
		 * Process OIDs one by one
		 */
		String[] lp = null;
		boolean starting = true;
		int cpt = 0;
		for( Long oid: this.oids ) {
			newClasse = SaadaOID.getClassName(oid);
			/*
			 * Processing a new class: take the new loader parameters
			 */
			if( starting || !newClasse.equals(classe)) {
				Messenger.printMsg(Messenger.TRACE, "Processing OIDs from class " + newClasse);
				SQLQuery query = new SQLQuery("SELECT * FROM saada_class WHERE name = '" + classe + "' AND category = '" + category + "' limit 1");
				String params = null;
				ResultSet rs = query.run();
				while( rs.next() ) {
					params = rs.getString("description").trim().replace("ArgsParser(", "");
					Messenger.printMsg(Messenger.TRACE, "Class " + classe + " created with the following parameters " + params);
					if( params.endsWith(")") ) params = params.substring(0, params.length()-1);		
					lp = params.split(" ");
					break;
				}
				query.close();
				classe = newClasse;
				starting = false;
			}
			Messenger.printMsg(Messenger.TRACE, "Processing object saadaoid=" + oid + " (" + collection + "/" + category + "/" + classe + ")");
			SaadaInstance saadaInstance = Database.getCache().getObject(oid);
			String location = saadaInstance.getRepository_location();
			if( !(new File(location).exists())) {
				Messenger.printMsg(Messenger.ERROR, "File " + location + " no longer available");
			} else  if( location.startsWith("http") ){
				Messenger.printMsg(Messenger.WARNING, "Projection can not be recomputed for data product referenced by URLs");
			} else {
				/*
				 * Set the input file location in the loader params 
				 */
				for( int i=0 ; i<lp.length ; i++ ) {
					if( lp[i].trim().startsWith("-filelist") || lp[i].trim().startsWith("-filename")  ) {
						lp[i] = "-filename=" + location;
					}
				}
				ArgsParser ap = new ArgsParser(lp);
				ap.mergeArgParsers(newArgs);
				Messenger.printMsg(Messenger.TRACE, "Parameters used fo the update: " + ap);
				/*
				 * Compute the new projection values
				 */
				ProductBuilder product  = null;
				MappingReport report    = null;
				ProductMapping mapping  = new ProductMapping("mapping", ap);
				DataResourcePointer drp = new DataResourcePointer(ap.getFilename());

				DataFile df = SchemaMapper.getDataFileInstance(drp.getAbsolutePath(), mapping);

				switch( Category.getCategory(category) ) {
				case Category.TABLE: 			
					AbortException.throwNewException(SaadaException.DB_ERROR, "Projections cannot be recomputed for TABLEs");
					break;
				case Category.MISC : product = new MiscBuilder(df, mapping);
				report = new MappingReport(product);
				break;
				case Category.SPECTRUM: product = new SpectrumBuilder(df, mapping);
				report = new MappingReport(product);
				break;
				case Category.IMAGE: product = new Image2DBuilder(df, mapping);
				report = new MappingReport(product);
				break;
				}
				/*
				 * Extract the modified Saada instance
				 */
				product.mapDataFile();
				SaadaInstance newInstance = report.getReportInstance();
				/*
				 * Restore attributes which cannot be changed
				 */
				newInstance.oidsaada = saadaInstance.oidsaada;
				newInstance.setRepository_location(saadaInstance.getRepository_location());
				newInstance.setDate_load(saadaInstance.getDate_load());
				newInstance.setAccess_url(saadaInstance.getAccess_url());
				newInstance.setAccess_format(saadaInstance.getAccess_format());
				product.productIngestor.setContentSignature();
				
				if( !writeMode ){
					System.out.println("==========  " + saadaInstance.obs_id + " ==============");
					for( Entry<String, AttributeHandler> entry: attMap.entrySet()){		
						System.out.println(entry.getKey() + "\t " + newInstance.getFieldValue(entry.getKey())  + " \t (former: " + saadaInstance.getFieldValue(entry.getKey()) + ")");
					}
					System.out.println("========================");
				} else {

					ArrayList<String> fieldStatements = new ArrayList<String>();
					for( Entry<String, AttributeHandler> entry: attMap.entrySet()){		
						String key = entry.getKey();
						AttributeHandler ah = entry.getValue();
						Object o;
						String val = ((o = newInstance.getFieldValue(key)) == null)? "NULL": o.toString();
						fieldStatements.add(key + " = " + Database.getWrapper().getSQLvalue(val,  ah.getType()) + " ");
					}
					String query = "UPDATE " + collection + "_" + category + " SET " + Merger.getMergedCollection(fieldStatements) + " WHERE oidsaada = " + oid + "\n";
					SQLTable.addQueryToTransaction(query);
					if( cpt > 0 && (cpt%1000) == 0){
						SQLTable.commitTransaction();
						SQLTable.beginTransaction();
					}
					cpt++;
					
				}
			}
		}
	}

	/**
	 * Check that all oids are from the same collection/category/class.
	 * 
	 * @throws AbortException
	 */
	private void checkOidList() throws AbortException {
		long mask = 0;
		for (long oid : this.oids) {
			mask = oid >> 48;
		}

		for (long oid : oids) {
			try {
				Database.getCache().getObject(oid);
			} catch (Exception e) {
				AbortException.throwNewException(SaadaException.WRONG_PARAMETER,
						"No object with oid = " + oid + " found");
			}
			if ((oid >> 48) != mask) {
				AbortException.throwNewException(SaadaException.WRONG_PARAMETER,
						"OIDs to be removed are not all from the same collection/category/class");
			}
		}
	}

}