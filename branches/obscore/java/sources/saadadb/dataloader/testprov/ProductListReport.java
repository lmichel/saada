package saadadb.dataloader.testprov;

import java.io.File;
import java.util.Map;

import saadadb.collection.Category;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.dataloader.mapping.ProductMapping;
import saadadb.products.ColumnSetter;
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
			int cpt = 1;
			int MAX = 10;
			for( File f: files) {
				if( cpt == MAX ) {
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
					Map<String, ColumnSetter> r = product.getReport();
					System.out.println("======== " + f);	
					for( java.util.Map.Entry<String, ColumnSetter> e:r.entrySet()){
						System.out.print(String.format("%20s",e.getKey()) + "     ");
						ColumnSetter ah = e.getValue();
						System.out.print(ah.getMode() + " " + ah.message);
						if( !ah.notSet() ) 
							System.out.print(" storedValue=" + ah.storedValue);
						System.out.println("");

					}
				}
				if( cpt > MAX ) break;
				cpt++;
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
