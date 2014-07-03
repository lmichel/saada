package saadadb.dataloader.testprov;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;

public class ShakePosition extends ParamShaker{
	
	static {
		TEMPLATE = "{"
	
		+ "\"parameters\": [ \n"
		+ "	\"-category=misc\" , \n"
		+ "	\"-collection=XMM\", \n"
		+ "	\"-filename=obscore\", \n"
		+ "	\"-repository=no\", \n"
		+ "	\"-posmapping=first\" , \n"
		+ "	\"-spcunit=keV\", \n"
		+ "	\"-timemapping=only\" 	,	 \n"
		+ "	\"-tmin=11 03 2013\" 	,	 \n"
		+ "	\"-tmax=12 03 2013\"	 \n"
		+ "], \n"
		+ "\"fields\": { \n"
		+ "    \"header\": [\n"
		+ "             [\"RA\"        , \"double\", \"deg\"   , \"\"              , \"23.67\"], \n"
		+ "				[\"DEC\"       , \"double\", \"deg\"   , \"\"              , \"-56.9\"], \n"
		+ "				[\"eMin\"      , \"double\", \"KeV\"   , \"em.wl;stat.min\", \"1.\"], \n"
		+ "				[\"eMax\"      , \"double\", \"KeV\"   , \"em.wl;stat.max\", \"2.\"], \n"
		+ "				[\"obsStart\"  , \"\"      , \"\"      , \"\"              , \"2014-02-12\"], \n"
		+ "				[\"obsEnd\"    , \"\"      , \"\"      , \"\"              , \"2014-02-13\"], \n"
		+ "				[\"collection\", \"\"      , \"string\", \"\"              , \"3XMM\"], \n"
		+ "				[\"target\"    , \"\"      , \"string\", \"\"              , \"M33\"], \n"
		+ "				[\"instrume\"  , \"\"      , \"string\", \"\"              , \"MOS1\"], \n"
		+ "				[\"facility\"  , \"\"      , \"string\", \"\"              , \"XMM\"] \n"
		+ "		]\n"
		+ "		,\n"
		+ "    \"columns\": []\n"
		+ "    }\n"
		+ "}\n";
	}

	ShakePosition() throws Exception{
		super();
		this.paramsOfInterest = new HashSet<String>();
		this.paramsOfInterest.add("s_ra");
		this.paramsOfInterest.add("s_dec");
	}

	/**
	 * Priority first, good mapped parameters
	 * @throws Exception 
	 */
	protected void runFirstWithGoodMParams() throws Exception{
		super.runFirstWithGoodMParams();	
		this.process();
	}
	/**
	 * Priority first, good inferred parameters
	 * @throws Exception 
	 */
	protected void runFirstWithGoodIParams() throws Exception{
		super.runFirstWithGoodIParams();	
		this.process();
	}
	/**
	 * Priority first, wrong mapped parameters
	 * @throws Exception 
	 */
	protected void runFirstWithWrongMParams() throws Exception{
		super.runFirstWithWrongMParams();
		this.process();
	}
	/**
	 * Priority first, wrong inferred parameters
	 * @throws Exception 
	 */
	protected void runFirstWithWrongIParams() throws Exception{
		super.runFirstWithWrongIParams();
		this.process();
	}
	/**
	 * Priority first, partially wrong mapped parameters
	 * @throws Exception 
	 */
	protected void runFirstWithPWrongMParams() throws Exception{
		super.runFirstWithPWrongMParams();
		this.process();
	}
	/**
	 * Priority first, partially wrong inferred parameters
	 * @throws Exception 
	 */
	protected void runFirstWithPWrongIParams() throws Exception{
		super.runFirstWithPWrongIParams();
		this.process();
	}
	/**
	 * Priority Last, good mapped parameters
	 * @throws Exception 
	 */
	protected void runLastWithGoodMParams() throws Exception{
		super.runLastWithGoodMParams();		
		this.process();
	}
	/**
	 * Priority Last, good inferred parameters
	 * @throws Exception 
	 */
	protected void runLastWithGoodIParams() throws Exception{
		super.runLastWithGoodIParams();	
		this.process();
	}
	/**
	 * Priority Last, wrong mapped parameters
	 * @throws Exception 
	 */
	protected void runLastWithWrongMParams() throws Exception{
		super.runLastWithWrongMParams();
		this.process();
	}
	/**
	 * Priority Last, wrong inferred parameters
	 * @throws Exception 
	 */
	protected void runLastWithWrongIParams() throws Exception{
		super.runLastWithWrongIParams();
		this.process();
	}
	/**
	 * Priority Last, partially wrong mapped parameters
	 * @throws Exception 
	 */
	protected void runLastWithPWrongMParams() throws Exception{
		super.runLastWithPWrongMParams();
		this.process();
	}
	/**
	 * Priority Last, partially wrong inferred parameters
	 * @throws Exception 
	 */
	protected void runLastWithPWrongIParams() throws Exception{
		super.runLastWithPWrongIParams();
		this.process();
	}
	/**
	 * @param args
	 * @throws FatalException 
	 */
	public static void main(String[] args) throws Exception {
		ArgsParser ap = new ArgsParser(args);
		Database.init(ap.getDBName());
		ShakePosition sp = new ShakePosition();
		sp.processAll();
		sp.showReport();
		Database.close();
	}

}
