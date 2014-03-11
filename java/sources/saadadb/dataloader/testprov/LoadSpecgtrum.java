package saadadb.dataloader.testprov;

import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.dataloader.Loader;
import saadadb.exceptions.FatalException;
import saadadb.util.Messenger;

public class LoadSpecgtrum {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			Database.init("Obscore");
//Messenger.debug_mode = true;
Loader l =new Loader(new String[]{
		"-classifier=EPICSpectrum" 
		,"-category=misc" 
		,"-collection=XMM"
		,"-filename=/home/michel/Desktop/ADASS2008/data_sample/EPIC Spectra/P0205010201PNS003SRSPEC0003.FIT"
		,"-repository=no"
		,"-spcmapping=first" 
		//,"-spccolumn='0.2 12'" 
		,"-spcunit=keV"
		,"-timemapping=only" 		
		,"-tmin=11 03 2013" 		
		,"-tmax=12 03 2013" 		
		, "Obscore"});
l.load();
System.out.println("==========================================");
 l =new Loader(new String[]{
		"-classifier=EPICSpectrum" 
		,"-category=misc" 
		,"-collection=XMM"
		,"-filename=/home/michel/Desktop/ADASS2008/data_sample/EPIC Spectra/P0205010201PNS003SRSPEC0003.FIT"
		,"-repository=no"
		,"-spcmapping=only" 
		,"-spccolumn=CHANNEL" 
		,"-spcunit=keV"
		, "Obscore"});
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
