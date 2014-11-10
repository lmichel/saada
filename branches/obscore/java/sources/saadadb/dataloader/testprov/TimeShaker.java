package saadadb.dataloader.testprov;

import java.util.HashSet;

import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;

/**
 * @author michel
 * @version $Id$
 */
/**
 * @author michel
 * @version $Id$
 */
public class TimeShaker extends ParamShaker{

	static {
		TEMPLATE = "{"

			+ "\"parameters\": [ \n"
			+ "	\"-category=misc\" , \n"
			+ "	\"-collection=XMM\", \n"
			+ "	\"-filename=obscore\", \n"
			+ "	\"-repository=no\", \n"
			+ "	\"-posmapping=first\" , \n"
			+ "	\"-position=RA,DEC\" , \n"
			+ "	\"-spcunit=keV\", \n"
			+ "	\"-timemapping=only\" 	,	 \n"
			+ "	\"-tmin='11 03 2013'\" 	,	 \n"
			+ "	\"-tmax='11 04 2013'\"	 \n"
			+ "	\"-oucd=pow(RA)+pow('10')+eMin\"	 \n"
			+ "], \n"
			+ "\"fields\": { \n"
			+ "    \"header\": [\n"
			+ "             [\"RA\"        , \"double\", \"deg\"   , \"\"              , \"10\"], \n"
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

	TimeShaker() throws Exception{
		super();
		this.paramsOfInterest = new HashSet<String>();
		this.paramsOfInterest.add("t_min");
		this.paramsOfInterest.add("t_max");
		this.paramsOfInterest.add("t_exptime");
	}

	/* (non-Javadoc)
	 * @see saadadb.dataloader.testprov.ParamShaker#runFirstWithGoodMParams()
	 */
	protected void runFirstWithGoodMParams() throws Exception{
		super.runFirstWithGoodMParams();	
		this.process();
	}

	/* (non-Javadoc)
	 * @see saadadb.dataloader.testprov.ParamShaker#runFirstWithGoodIParams()
	 */
	protected void runFirstWithGoodIParams() throws Exception{
		super.runFirstWithGoodIParams();	
		this.process();
	}
	/* (non-Javadoc)
	 * @see saadadb.dataloader.testprov.ParamShaker#runFirstWithWrongMParams()
	 */
	protected void runFirstWithWrongMParams() throws Exception{
		super.runFirstWithWrongMParams();
		this.setArgParam("-position", "a,b");
		this.process();
	}
	/* (non-Javadoc)
	 * @see saadadb.dataloader.testprov.ParamShaker#runFirstWithWrongIParams()
	 */
	protected void runFirstWithWrongIParams() throws Exception{
		super.runFirstWithWrongIParams();
		this.setArgParam("-position", "RA,DEC");
		this.setField("RA", null, null, null, "");
		this.setField("DEC", null, null, null, "");
		this.process();
	}
	/* (non-Javadoc)
	 * @see saadadb.dataloader.testprov.ParamShaker#runFirstWithPWrongMParams()
	 */
	protected void runFirstWithPWrongMParams() throws Exception{
		super.runFirstWithPWrongMParams();
		this.process();
	}
	/* (non-Javadoc)
	 * @see saadadb.dataloader.testprov.ParamShaker#runFirstWithPWrongIParams()
	 */
	protected void runFirstWithPWrongIParams() throws Exception{
		super.runFirstWithPWrongIParams();
		this.process();
	}
	/* (non-Javadoc)
	 * @see saadadb.dataloader.testprov.ParamShaker#runLastWithGoodMParams()
	 */
	protected void runLastWithGoodMParams() throws Exception{
		super.runLastWithGoodMParams();		
		this.process();
	}
	/* (non-Javadoc)
	 * @see saadadb.dataloader.testprov.ParamShaker#runLastWithGoodIParams()
	 */
	protected void runLastWithGoodIParams() throws Exception{
		super.runLastWithGoodIParams();	
		this.process();
	}
	/* (non-Javadoc)
	 * @see saadadb.dataloader.testprov.ParamShaker#runLastWithWrongMParams()
	 */
	protected void runLastWithWrongMParams() throws Exception{
		super.runLastWithWrongMParams();
		this.process();
	}
	/* (non-Javadoc)
	 * @see saadadb.dataloader.testprov.ParamShaker#runLastWithWrongIParams()
	 */
	protected void runLastWithWrongIParams() throws Exception{
		super.runLastWithWrongIParams();
		this.process();
	}
	/* (non-Javadoc)
	 * @see saadadb.dataloader.testprov.ParamShaker#runLastWithPWrongMParams()
	 */
	protected void runLastWithPWrongMParams() throws Exception{
		super.runLastWithPWrongMParams();
		this.process();
	}
	/* (non-Javadoc)
	 * @see saadadb.dataloader.testprov.ParamShaker#runLastWithPWrongIParams()
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
		try {
		ArgsParser ap = new ArgsParser(args);
		Database.init(ap.getDBName());
		TimeShaker sp = new TimeShaker();
		sp.processAll();
		sp.showReport();
		} catch(Exception e){
			e.printStackTrace();
		}
		Database.close();
	}

}