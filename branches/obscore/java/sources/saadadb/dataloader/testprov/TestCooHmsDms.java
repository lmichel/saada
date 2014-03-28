package saadadb.dataloader.testprov;

import java.text.ParseException;

import cds.astro.Astrocoo;
import cds.astro.Astroframe;
import cds.astro.ICRS;

public class TestCooHmsDms {

	/**
	 * @param args
	 * @throws ParseException 
	 */
	public static void main(String[] args) throws ParseException {
		Astroframe af = new ICRS();
		new Astrocoo(af, "06h30m22.8s +29d38m23s" ) ;

	}

}
