package saadadb.command;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import saadadb.collection.Category;
import saadadb.collection.RepositoryManager;
import saadadb.collection.SaadaOID;
import saadadb.collection.obscoremin.SaadaInstance;
import saadadb.database.Database;
import saadadb.database.Repository;
import saadadb.dataloader.SchemaMapper;
import saadadb.dataloader.mapping.ProductMapping;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.products.Image2DBuilder;
import saadadb.products.MiscBuilder;
import saadadb.products.ProductBuilder;
import saadadb.products.SpectrumBuilder;
import saadadb.products.TableBuilder;
import saadadb.products.datafile.DataFile;
import saadadb.products.datafile.DataResourcePointer;
import saadadb.products.reporting.MappingReport;
import saadadb.products.reporting.TableMappingReport;
import saadadb.products.setter.ColumnSetter;
import saadadb.query.executor.Query;
import saadadb.query.result.OidsaadaResultSet;
import saadadb.relationship.RelationManager;
import saadadb.sqltable.SQLQuery;
import saadadb.sqltable.SQLTable;
import saadadb.util.Messenger;

public class ProjectComputer extends EntityManager{
	private ArrayList<Long> oids;

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

	@Override
	public void populate(ArgsParser ap) throws SaadaException {
		try {
			String query = ap.getPopulate().trim();
			if( query.startsWith("Select")) {
				computeProjections(query);
			} else {
				String[] soids =  query.split("[,;]");
				long oids[] = new long[soids.length];
				for( int j=0 ; j<soids.length ; j++ ) {
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
	 * Remove all data matching the query
	 * @param query
	 * @throws SaadaException
	 */
	public final void computeProjections(String query) throws Exception {
		try {
			Query q = new Query();
			OidsaadaResultSet rs = q.runBasicQuery(query);
			Messenger.printMsg(Messenger.TRACE, "Remove data matching the query " +query);
			this.oids = new ArrayList<Long>();
			while(rs.next()) {
				this.oids.add(rs.getOId());
			}
			computeProjections();			
		} catch(AbortException e ) {
			AbortException.throwNewException(SaadaException.DB_ERROR, e);
		} catch(Exception e ) {
			Messenger.printStackTrace(e);
			AbortException.throwNewException(SaadaException.DB_ERROR, e);
		}

	}

	/**
	 * @param oids
	 * @throws SaadaException
	 */
	public final void computeProjections(long[] oids) throws Exception {
		this.oids = new ArrayList<Long>();
		for( long oid: oids) {
			this.oids.add(oid);

		}
		computeProjections();				
	}

	/**
	 * @param oids
	 * @throws Exception
	 */
	private final void computeProjections() throws Exception {
		/*
		 * Nothing to do: return
		 */
		if( this.oids == null || this.oids.size() == 0 ) {
			Messenger.printMsg(Messenger.WARNING, "The list of oids to remove is empty" );
			return;
		}
		/*
		 * This method can only work if all oids are from the same collection/category/class.
		 */
		this.checkOidList();

		if( this.oids.size() == 0 ){
			Messenger.printMsg(Messenger.WARNING, "No selected products" );	
		}
		String classe  = SaadaOID.getClassName(this.oids.get(0));
		String category  = SaadaOID.getCategoryName(this.oids.get(0));
		if( category.equals("TABLE") ){
			AbortException.throwNewException(SaadaException.DB_ERROR, "Projections cannot be recomputed for TABLEs");
		}
		Repository.getTmpPath();
		for( Long oid: this.oids ) {
			System.out.println("@@@@@@@@@@@ " + oid);
			SaadaInstance si = Database.getCache().getObject(oid);
			System.out.println("@@@@@@@@@@@ " + classe + " " + category);

			SQLQuery query = new SQLQuery("SELECT * FROM saada_class WHERE name = '" + classe + "' AND category = '" + category + "'");
			ResultSet rs = query.run();
			while( rs.next() ) {
				String params = rs.getString("description").trim().replace("ArgsParser(", "");
				if( params.endsWith(")") ) params = params.substring(0, params.length()-1);
				String[] lp = params.split(" ");
				String location = si.getRepository_location();
				if( location.startsWith("httpXXXXXXX") ){
					//Messenger.printMsg(Messenger.WARNING, "Projection can not be recomputed for data product referenced by URLs");
				} else {
					for( int i=0 ; i<lp.length ; i++ ) {
						if( lp[i].trim().startsWith("-filelist") ||lp[i].trim().startsWith("-filename")  ) {
							lp[i] = "-filename=" + location;
						}
					}
					ArgsParser ap = new ArgsParser(lp);
					System.out.println("@@@@@@@@@@@ " + ap);
					ProductBuilder product = null;
					MappingReport report = null;
					ProductMapping mapping = new ProductMapping("mapping", ap);
					DataResourcePointer drp = new DataResourcePointer(ap.getFilename());

					DataFile df = SchemaMapper.getDataFileInstance(drp.getAbsolutePath(), mapping);

					switch( Category.getCategory(category) ) {
					case Category.TABLE: 			
					AbortException.throwNewException(SaadaException.DB_ERROR, "Projections cannot be recomputed for TABLEs");
				    break;
					case Category.MISC : product = new MiscBuilder(df, mapping);
					report = new MappingReport(product);
					break;
					case Category.SPECTRUM: product = new SpectrumBuilder(df, new ProductMapping("mapping", ap));
					report = new MappingReport(product);
					break;
					case Category.IMAGE: product = new Image2DBuilder(df, new ProductMapping("mapping", ap));
					report = new MappingReport(product);
					break;
					}
					product.mapDataFile();
					Map<String, ColumnSetter> r = report.getReport();
					for( Entry<String, ColumnSetter> e:r.entrySet()){
						ColumnSetter ah = e.getValue();
						System.out.println("@@@@@@@@@@@ " + e.getKey() +"@@@@@@@@@@@ " + ah.storedValue +  " @@@@@@@@@@@ " + ah.isNotSet());
						
//
//						fw.write(String.format("%20s",e.getKey()) + "     ");;
//						ColumnSetter ah = e.getValue();
//						fw.write(ah.getFullMappingReport() +  " ");
//						if( !ah.isNotSet() ) {
//							fw.write("storedValue=" + ah.storedValue+" \n");
//						} else {
//							fw.write("\n");
//						}
					}
				}
			}
			query.close();
		}


	}
	
	/**
	 * Check that all oids are from the same collection/category/class.
	 * @throws AbortException
	 */
	private void checkOidList() throws AbortException {
		for( long oid: oids) {
			try {
				Database.getCache().getObject(oid);
			} catch (Exception e) {
				AbortException.throwNewException(SaadaException.WRONG_PARAMETER, "No object with oid = " + oid + " found");
			}
			if( (oid >> 32) != (this.oids.get(0) >> 32) ) {
				AbortException.throwNewException(SaadaException.WRONG_PARAMETER, "OIDs to be removed are not all from the same collection/category/class");
			}
		}
	}


}
