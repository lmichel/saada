package saadadb.dataloader.testprov;

import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.dataloader.Loader;
import saadadb.exceptions.FatalException;

public class LoadSpecgtrum {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			Database.init("ObscoreP");

			Loader l =new Loader(new String[]{
					"-classifier=EPICSpectrum" 
					,"-category=spectrum" 
					,"-collection=XMM"
					,"-filename=/Users/laurentmichel/Desktop/DossierBoston/data_sample/EPIC Spectra/P0105070101M2S002SRSPEC0002.FIT.gz"
					,"-repository=no"
					,"-spcmapping=only" 
					,"-spccolumn='0.2 12'" 
					,"-spcunit=keV"
					, "ObscoreP"});
			l.load();
			Database.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			Database.close();
		}
	}

}
