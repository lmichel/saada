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
import saadadb.dataloader.mapping.ProductMapping;
import saadadb.meta.AttributeHandler;
import saadadb.products.ExtensionSetter;
import saadadb.products.Image2DBuilder;
import saadadb.products.MiscBuilder;
import saadadb.products.ProductBuilder;
import saadadb.products.SpectrumBuilder;
import saadadb.products.TableBuilder;
import saadadb.products.datafile.DataFile;
import saadadb.products.datafile.FitsDataFile;
import saadadb.products.reporting.MappingReport;
import saadadb.products.setter.ColumnSetter;

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
			String log="";
//			String[] sample = {
//			"P0403390101M1S001SRSPEC0017.FIT",
//			"J_ApJ_703_894_PN_IC342_001.fits",
//			"J_ApJ_708_661_SN10297-12042005.fits",
//			"J_MNRAS_371_703_s0001.fits",
//			"moog_t5100g_00f-02a_06.fits",
//			"J_A+A_414_699_hd20766.fits",
//			"J_A+A_524_A86_SDSS0039_1.fit",
//			"j_a+a_507_929_time_serie_blue_78.fit",
//			"EN2_WINDESCRIPTOR_0105574071_20080415T231048_20080907T224903.fits",
//			"J_ApJ_727_125_time_serie_WASP12b_secondary_2008-10-29_Spitzer_IRAC_4.5_microns.fits",
//			"J_ApJ_703_894_p17_12co_2-1.fits",
//			"J_A+A_544_A114_lcmos1.fits"};
			
			Set<File> files = new LinkedHashSet<File>();
			//			for( String s:sample ) {
			int p=0;
			for( File f: allfiles) {
				files.add(f);

			}
				
//			}

			int cpt = 1;
			int MAX = Integer.parseInt(ap.getNumber());
			for( File f: files) {
				if( f.isDirectory() )
					continue;
				if( MAX == -1 || cpt == MAX ) {
					System.out.println(f + " " + f.exists());
					ProductBuilder product = null;
					DataFile df = new FitsDataFile(f.getAbsolutePath());
					switch( Category.getCategory(ap.getCategory()) ) {
					case Category.TABLE: //product = new TableBuilder(df, new ProductMapping("mapping", ap));
					break;
					case Category.MISC : product = new MiscBuilder(df, new ProductMapping("mapping", ap));
					break;
					case Category.SPECTRUM: product = new SpectrumBuilder(df, new ProductMapping("mapping", ap));
					break;
					case Category.IMAGE: product = new Image2DBuilder(df, new ProductMapping("mapping", ap));
					break;
					}
					product.mapDataFile();
					(new MappingReport(product)).writeCompleteReport(f.getParent() + "/report/", ap);
					if( MAX != -1 ) break;
				}
				cpt++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			Database.close();
		}
		System.exit(1);
	}
}
