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
import saadadb.util.SaadaConstant;

/**
 * This class process the re-computing of the FoV of collection attributes in one category.
 * Only {@link EntityManager#populate(ArgsParser)} method is implemented 
 * The new FoV is computed from the region. 
 * If it is smaller the that current, it replaces it
 *  
 * 
 * @author michel
 *
 */
public class FoVUpdater extends EntityManager {
	private Set<Long> oids;
	private static Set<String> allowedArgs;
	/*
	 * Command line parameters to be used to redo the mapping
	 */
	private String[] newArgs;
	private boolean writeMode = false;


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

	/*
	 * (non-Javadoc)
	 * 
	 * @see saadadb.command.EntityManager#populate(saadadb.command.ArgsParser)
	 */
	@Override
	public void populate(ArgsParser ap) throws SaadaException {
		try {
			String category = ap.getCategory();
			String collection = ap.getPopulate();
			this.writeMode = ( ap.isNop())? false:true;
			System.out.println(ap.isNop());
			Messenger.printMsg(Messenger.TRACE, "writeMode is " + this.writeMode);

			if( collection != null && !collection.equalsIgnoreCase("all")){
				Messenger.printMsg(Messenger.TRACE, "Collection " + collection + " requested");
				if( category != null ){
					category = category.toUpperCase() ;
					Messenger.printMsg(Messenger.TRACE, "Category " +  category + " requested");
					this.populateCollection(collection, category);
				} else {
					Messenger.printMsg(Messenger.TRACE, "All categories requested");
					for( String cat : Category.NAMES) {
						this.populateCollection(collection, cat);
					}
				}
			} else {
				Messenger.printMsg(Messenger.TRACE, "All collections requested");
				for( String coll : Database.getCachemeta().getCollection_names() ) {
					if( category != null ) {
						Messenger.printMsg(Messenger.TRACE, "Category " +  category + " requested");
						category = category.toUpperCase() ;
						this.populateCollection(coll, category);
					} else {
						Messenger.printMsg(Messenger.TRACE, "All categories requested");
						for( String cat : Category.NAMES) {
							this.populateCollection(coll, cat);
						}
					}
				}
			}
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			AbortException.throwNewException(SaadaException.DB_ERROR, e);
		}
	}

	/**
	 * @param collection
	 * @param category
	 * @throws Exception
	 */
	private void populateCollection(String collection, String category) throws Exception {
		Messenger.printMsg(Messenger.INFO, "Processing " + category + " of collection " + collection);
		String query = "Select " + category + " From * In " + collection;
		this.buildOidList(query);
		if( this.writeMode ){
			SQLTable.beginTransaction();
		}
		int cpt   = 0;
		for( long oid: this.oids) {
			SaadaInstance instance = Database.getCache().getObject(oid);
			double fovOrg = instance.getS_fov();
			String region  = instance.getS_region();
			if( region != SaadaConstant.STRING && region != null) {
				double fovFromregion = this.getFovFromRegion(region);
				if( fovFromregion < fovOrg) {
					if( this.writeMode ){
						if (Messenger.debug_mode)
							Messenger.printMsg(Messenger.DEBUG, instance.obs_id + " " +  oid + " " + fovOrg + " (" + region + ") " + fovFromregion);
						query = "UPDATE " + collection + "_" + category + " SET s_fov = " + fovFromregion+ " WHERE oidsaada = " + oid + "\n";
						SQLTable.addQueryToTransaction(query);
						if( cpt > 0 && (cpt%1000) == 0){
							SQLTable.commitTransaction();
							SQLTable.beginTransaction();
						}
						cpt++;
					} else {
						System.out.println(instance.obs_id + " " +  oid + " " + fovOrg + " (" + region + ") " + fovFromregion);
					}
				}
			}
		}

	}

	/**
	 * @param region
	 * @return
	 * @throws Exception
	 */
	private double getFovFromRegion(String region) throws Exception {
		String[] coords = region.split("\\s+");

		double raMin  = 1000;
		double raMax  = -1000;
		double decMin  = 1000;
		double decMax  = -1000;
		for( int i=2 ; i<coords.length ; i+=2) {
			double ra = Double.parseDouble(coords[i]);
			double dec = Double.parseDouble(coords[i+1]);
			if( ra > raMax )  raMax = ra;
			if( ra < raMin )  raMin = ra;
			if( dec > decMax )  decMax = dec;
			if( dec < decMin )  decMin = dec;
		}
		double sizeRa = Math.abs(raMax - raMin);
		if( sizeRa > 180 ) sizeRa = 360 - sizeRa;
		double sizeDec= Math.abs(decMax - decMin);
		if( sizeDec > 180 ) sizeDec = 360 - sizeDec;
		return Math.sqrt((sizeRa * sizeRa) + (sizeDec + sizeDec));
	}

	/**
	 * Set the OIDs to be processed from the query result
	 * 
	 * @param query
	 * @throws SaadaException
	 */
	private final void buildOidList(String query) throws Exception {
		try {
			Query q = new Query();
			OidsaadaResultSet rs = q.runBasicQuery(query);
			Messenger.printMsg(Messenger.TRACE, "Update projections for OIDs returned by " + query);
			this.oids = new TreeSet<Long>();
			while (rs.next()) {
				this.oids.add(rs.getOId());
			}
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			AbortException.throwNewException(SaadaException.DB_ERROR, e);
		}

	}

}
