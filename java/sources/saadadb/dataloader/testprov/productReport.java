package saadadb.dataloader.testprov;

import java.io.File;

import saadadb.collection.Category;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.dataloader.mapping.ProductMapping;
import saadadb.products.Image2DBuilder;
import saadadb.products.MiscBuilder;
import saadadb.products.ProductBuilder;
import saadadb.products.SpectrumBuilder;
import saadadb.products.TableBuilder;

public class productReport {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			ArgsParser ap = new ArgsParser(args);
			Database.init(ap.getDBName());
			System.out.println(ap);
			ProductBuilder product = null;
			switch( Category.getCategory(ap.getCategory()) ) {
			case Category.TABLE: product = new TableBuilder((new File(ap.getFilename()))
					, new ProductMapping("mapping", ap));
			break;
			case Category.MISC : product = new MiscBuilder((new File(ap.getFilename()))
					, new ProductMapping("mapping", ap));
			break;
			case Category.SPECTRUM: product = new SpectrumBuilder((new File(ap.getFilename()))
					, new ProductMapping("mapping", ap));
			break;
			case Category.IMAGE: product = new Image2DBuilder((new File(ap.getFilename()))
					, new ProductMapping("mapping", ap));
			break;
			}
			product.getReport()
			product.initProductFile();
			product.printReport();
			System.exit(1);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			Database.close();
		}
	}

}
