package saadadb.dataloader.testprov;

import java.io.File;

import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.dataloader.Loader;
import saadadb.dataloader.mapping.ProductMapping;
import saadadb.products.Image2DBuilder;
import saadadb.products.ProductBuilder;
import saadadb.util.Messenger;

public class LoadImage {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			Database.init("Obscore");

			ArgsParser ap = new ArgsParser(new String[]{
					"-classifier=EPICSpectrum" 
					,"-category=image" 
					,"-collection=XMM"
					,"-filename=/home/michel/Desktop/ADASS2008/data_sample/imagesXMM/P0105070101EPX000OIMAGE8000.FIT"
					,"-repository=no"
					,"-spcmapping=first" 
					//,"-spccolumn='0.2 12'" 
					,"-spcunit=keV"
					,"-timemapping=only" 		
					,"-tmin=11 03 2013" 		
					,"-tmax=12 03 2013" 		
					, "Obscore"});

			ProductBuilder product = new Image2DBuilder(new File("/home/michel/Desktop/ADASS2008/data_sample/imagesXMM/P0105070101EPX000OIMAGE8000.FIT")
			, new ProductMapping("mapping", ap));
			product.initProductFile();
			product.printReport();
			Database.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			Database.close();
		}
	}

}
