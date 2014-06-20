package saadadb.dataloader.testprov;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import saadadb.collection.Category;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.dataloader.mapping.ProductMapping;
import saadadb.products.ColumnSetter;
import saadadb.products.ExtensionSetter;
import saadadb.products.FitsDataFile;
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
			File[] allfiles = (new File(ap.getFilename())).listFiles();
			String[] sample = {
			"J_ApJ_703_894_PN_IC342_001.fits",
			"J_ApJ_708_661_SN10297-12042005.fits",
			"J_MNRAS_371_703_s0001.fits",
			"moog_t5100g_00f-02a_06.fits",
			"J_A+A_414_699_hd20766.fits",
			"J_A+A_524_A86_SDSS0039_1.fit",
			"j_a+a_507_929_time_serie_blue_78.fit",
			"EN2_WINDESCRIPTOR_0105574071_20080415T231048_20080907T224903.fits",
			"J_ApJ_727_125_time_serie_WASP12b_secondary_2008-10-29_Spitzer_IRAC_4.5_microns.fits",
			"J_ApJ_703_894_p17_12co_2-1.fits",
			"J_A+A_544_A114_lcmos1.fits"};
			
			Set<File> files = new LinkedHashSet<File>();
			for( String s:sample ) {
				for( File f: allfiles) {
					if( f.getName().equals(s)) {
						files.add(f);
						break;
					}
				}
			}

			System.out.println(ap.getFilename() + " " + new File(ap.getFilename()).exists());
			int cpt = 1;
			int MAX = 1;
			for( File f: files) {
				if( cpt == MAX ) {
					ProductBuilder product = null;
					switch( Category.getCategory(ap.getCategory()) ) {
					case Category.TABLE: product = new TableBuilder((new FitsDataFile(f.getAbsolutePath()))
							, new ProductMapping("mapping", ap));
					break;
					case Category.MISC : product = new MiscBuilder((new FitsDataFile(f.getAbsolutePath()))
							, new ProductMapping("mapping", ap));
					break;
					case Category.SPECTRUM: product = new SpectrumBuilder((new FitsDataFile(f.getAbsolutePath()))
							, new ProductMapping("mapping", ap));
					break;
					case Category.IMAGE: product = new Image2DBuilder((new FitsDataFile(f.getAbsolutePath()))
							, new ProductMapping("mapping", ap));
					break;
					}
				//	product.initProductFile();
					Map<String, ColumnSetter> r = product.getReport();
					System.out.println("======== " + f);	
					System.out.println("      -- Loaded extensions");	
					for( ExtensionSetter es: product.getReportOnLoadedExtension()) {
						System.out.println(es);
					}
					System.out.println("      -- Field values");	
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
