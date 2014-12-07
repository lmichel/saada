package saadadb.dataloader.testprov;

import java.io.File;
import java.io.PrintWriter;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import saadadb.collection.Category;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.database.Repository;
import saadadb.dataloader.mapping.ProductMapping;
import saadadb.meta.AttributeHandler;
import saadadb.products.ExtensionSetter;
import saadadb.products.Image2DBuilder;
import saadadb.products.MiscBuilder;
import saadadb.products.ProductBuilder;
import saadadb.products.SpectrumBuilder;
import saadadb.products.TableBuilder;
import saadadb.products.datafile.DataFile;
import saadadb.products.datafile.DataResourcePointer;
import saadadb.products.datafile.FitsDataFile;
import saadadb.products.reporting.MappingReport;
import saadadb.products.setter.ColumnSetter;

public class ProductFileReport {

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
			DataFile df = new FitsDataFile(drp.getAbsolutePath());
			switch( Category.getCategory(ap.getCategory()) ) {
			case Category.TABLE: //product = new TableBuilder(df, new ProductMapping("mapping", ap));
				break;
			case Category.MISC : product = new MiscBuilder(df, mapping);
			break;
			case Category.SPECTRUM: product = new SpectrumBuilder(df, new ProductMapping("mapping", ap));
			break;
			case Category.IMAGE: product = new Image2DBuilder(df, new ProductMapping("mapping", ap));
			break;
			}
			product.mapDataFile();
			(new MappingReport(product)).writeCompleteReport(Repository.getTmpPath(), ap);


		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			Database.close();
		}
		System.exit(1);
	}
}

