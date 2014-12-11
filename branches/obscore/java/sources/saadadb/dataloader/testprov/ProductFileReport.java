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
import saadadb.dataloader.SchemaMapper;
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
import saadadb.products.reporting.TableMappingReport;
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

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			Database.close();
		}
		System.exit(1);
	}
}

