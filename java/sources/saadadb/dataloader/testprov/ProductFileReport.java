package saadadb.dataloader.testprov;

import java.io.File;

import saadadb.collection.Category;
import saadadb.collection.ClassManager;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.dataloader.Loader;
import saadadb.dataloader.SchemaMapper;
import saadadb.dataloader.mapping.ProductMapping;
import saadadb.products.Image2DBuilder;
import saadadb.products.MiscBuilder;
import saadadb.products.ProductBuilder;
import saadadb.products.SpectrumBuilder;
import saadadb.products.TableBuilder;
import saadadb.products.datafile.DataFile;
import saadadb.products.datafile.DataResourcePointer;
import saadadb.products.reporting.MappingReport;
import saadadb.products.reporting.TableMappingReport;
import saadadb.sqltable.SQLTable;

public class ProductFileReport {

	private static void loadProduct(String[] args) throws Exception {	
		ArgsParser ap = new ArgsParser(args);
		String classe =  ap.getClassName();
		if( Database.getCachemeta().classExists(classe)) {
			Database.setAdminMode(null);
			SQLTable.beginTransaction();
			new ClassManager(classe).remove(ap);
			SQLTable.commitTransaction();
			Database.getCachemeta().reload(true);
		}

		new Loader(args).load();

	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			ArgsParser ap = new ArgsParser(args);
			Database.init(ap.getDBName());
			System.out.println(ap);
			ProductMapping mapping = new ProductMapping("mapping", ap);
			DataResourcePointer drp = new DataResourcePointer(ap.getFilename());

			ProductBuilder product = null;
			DataFile df = SchemaMapper.getDataFileInstance(drp.getAbsolutePath(), mapping);

			MappingReport report = null;
			switch( Category.getCategory(ap.getCategory()) ) {
			case Category.TABLE: product = new TableBuilder(df, mapping);
			report = new TableMappingReport((TableBuilder) product);
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
			report.writeCompleteReport(df.getParent() + File.separator + "report" + File.separator, ap);

			loadProduct(args);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			Database.close();
		}
		System.exit(1);
	}
}

