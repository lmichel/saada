package saadadb.dataloader.testprov;

import java.io.File;
import java.util.Map;

import saadadb.collection.Category;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.dataloader.mapping.ProductMapping;
import saadadb.products.ColumnSetter;
import saadadb.products.DataFile;
import saadadb.products.ExtensionSetter;
import saadadb.products.FitsDataFile;
import saadadb.products.Image2DBuilder;
import saadadb.products.MiscBuilder;
import saadadb.products.ProductBuilder;
import saadadb.products.SpectrumBuilder;
import saadadb.products.TableBuilder;
import saadadb.products.VOTableDataFile;
import saadadb.util.RegExp;

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
			DataFile df;
			if (ap.getFilename().matches(RegExp.VOTABLE_FILE)) {
				df = new VOTableDataFile(ap.getFilename());
			} else if (ap.getFilename().matches(RegExp.FITS_FILE)) {
				df = new FitsDataFile(ap.getFilename());
			} else {
				throw new Exception("Ni VOTABLE NI FITS");
			}
			switch (Category.getCategory(ap.getCategory())) {
			case Category.TABLE:
				product = new TableBuilder(df,
						new ProductMapping("mapping", ap));
				break;
			case Category.MISC:
				product = new MiscBuilder(df, new ProductMapping("mapping", ap));
				break;
			case Category.SPECTRUM:
				product = new SpectrumBuilder(df, new ProductMapping("mapping",
						ap));
				break;
			case Category.IMAGE:
				product = new Image2DBuilder(df, new ProductMapping("mapping",
						ap));
				break;
			}
			Map<String, ColumnSetter> r = product.getReport();
			System.out.println("======== " + ap.getFilename());
			System.out.println("      -- Loaded extensions");
			for (ExtensionSetter es : product.getReportOnLoadedExtension()) {
				System.out.println(es);
			}
			System.out.println("      -- Field values");
			for (java.util.Map.Entry<String, ColumnSetter> e : r.entrySet()) {
				System.out.print(String.format("%20s", e.getKey()) + "     ");
				ColumnSetter ah = e.getValue();
				System.out.print(ah.getMode() + " " + ah.message);
				if (!ah.notSet())
					System.out.print(" storedValue=" + ah.storedValue);
				System.out.println("");

			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			Database.close();
		}
	}

}
