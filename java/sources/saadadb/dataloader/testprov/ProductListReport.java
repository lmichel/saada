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

public class ProductListReport {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			ArgsParser ap = new ArgsParser(args);
			Database.init(ap.getDBName());
			System.out.println(ap);
			File[] files = (new File(ap.getFilename())).listFiles();
			System.out.println(ap.getFilename() + " " + new File(ap.getFilename()).exists());
			for( File f: files) {
				System.out.println("======== " + f);
				ProductBuilder product = null;
				switch( Category.getCategory(ap.getCategory()) ) {
				case Category.TABLE: product = new TableBuilder((new File(f.getAbsolutePath()))
						, new ProductMapping("mapping", ap));
				break;
				case Category.MISC : product = new MiscBuilder((new File(f.getAbsolutePath()))
						, new ProductMapping("mapping", ap));
				break;
				case Category.SPECTRUM: product = new SpectrumBuilder((new File(f.getAbsolutePath()))
						, new ProductMapping("mapping", ap));
				break;
				case Category.IMAGE: product = new Image2DBuilder((new File(f.getAbsolutePath()))
						, new ProductMapping("mapping", ap));
				break;
				}
				product.initProductFile();
				product.printReport();
				break;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			Database.close();
		}
		System.exit(1);
	}
}
