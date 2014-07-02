package saadadb.dataloader.testprov;

import java.io.FileReader;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ShakePosition extends ParamShaker{
	
	static {
		TEMPLATE = "{"
	
		+ "\"parameters\": [ \n"
		+ "	\"-category=misc\" , \n"
		+ "	\"-collection=XMM\", \n"
		+ "	\"-filename=obscore\", \n"
		+ "	\"-repository=no\", \n"
		+ "	\"-spcmapping=first\" , \n"
		+ "	\"-spcunit=keV\", \n"
		+ "	\"-timemapping=only\" 	,	 \n"
		+ "	\"-tmin=11 03 2013\" 	,	 \n"
		+ "	\"-tmax=12 03 2013\"	 \n"
		+ "], \n"
		+ "\"fields\": { \n"
		+ "    \"header\": [	[\"RA\", \"double\", \"\", \"23.67\"], \n"
		+ "				[\"DEC\", \"double\", \"\", \"-56.9\"], \n"
		+ "				[\"eMin\", \"KeV\", \"em.wl;stat.min\", \"1.\"], \n"
		+ "				[\"eMax\", \"KeV\", \"em.wl;stat.max\", \"2.\"], \n"
		+ "				[\"obsStart\", \"\", \"\", \"2014-02-12\"], \n"
		+ "				[\"obsEnd\", \"\", \"\", \"2014-02-13\"], \n"
		+ "				[\"collection\", \"string\", \"\", \"3XMM\"], \n"
		+ "				[\"target\", \"string\", \"\", \"M33\"], \n"
		+ "				[\"instrume\", \"string\", \"\", \"MOS1\"], \n"
		+ "				[\"facility\", \"string\", \"\", \"XMM\"] \n"
		+ "		]\n"
		+ "		,\n"
		+ "    \"columns\": []\n"
		+ "    }\n"
		+ "}\n";
	}

	ShakePosition() {
		super();
	}
	/**
	 * Priority first, good mapped parameters
	 */
	private void runFirstWithGoodMParams(){
		
	}
	/**
	 * Priority first, good inferred parameters
	 */
	private void runFirstWithGoodIParams(){
		
	}
	/**
	 * Priority first, wrong mapped parameters
	 */
	private void runFirstWithWrongMParams(){
		
	}
	/**
	 * Priority first, wrong inferred parameters
	 */
	private void runFirstWithWrongIParams(){
		
	}
	/**
	 * Priority first, partially wrong mapped parameters
	 */
	private void runFirstWithPWrongMParams(){
		
	}
	/**
	 * Priority first, partially wrong inferred parameters
	 */
	private void runFirstWithPWrongIParams(){
		
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
