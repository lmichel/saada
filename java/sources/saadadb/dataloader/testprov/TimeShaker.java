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
			+ "	\"-timemapping=first\" 	,	 \n"
			+ "	\"-tmin='11 03 2013'\" 	,	 \n"
			//+ "	\"-tmax='11 04 2013'\"	 \n"
			+ "	\"-tmax=date-obs\"	 \n"
		//	+ "	\"-exptime='1700'\"	 \n"
			+ "	\"-oucd=pow(RA)+pow('10')+eMin\"	 \n"
			+ "], \n"
			+ "\"data\": { \n"
			+ "    \"header\": [\n"
			+ "             [\"RA\"        , \"double\", \"deg\"   , \"\"              , \"10\"], \n"
			+ "				[\"DEC\"       , \"double\", \"deg\"   , \"\"              , \"-56.9\"], \n"
			+ "				[\"eMin\"      , \"double\", \"KeV\"   , \"em.wl;stat.min\", \"1.\"], \n"
			+ "				[\"eMax\"      , \"double\", \"KeV\"   , \"em.wl;stat.max\", \"2.\"], \n"
			+ "				[\"date-obs\"  , \"\"      , \"\"      , \"\"              , \"2013-11-04\"], \n"
			+ "				[\"obsStart\"  , \"\"      , \"\"      , \"\"              , \"2013-11-04\"], \n"
			+ "				[\"obsEnd\"    , \"\"      , \"\"      , \"\"              , \"2013-11-05\"], \n"
			+ "				[\"exptime\"    , \"\"      , \"\"      , \"\"              , \"1000\"], \n"
			+ "				[\"collection\", \"\"      , \"string\", \"\"              , \"3XMM\"], \n"
			+ "				[\"target\"    , \"\"      , \"string\", \"\"              , \"M33\"], \n"
			+ "				[\"instrume\"  , \"\"      , \"string\", \"\"              , \"MOS1\"], \n"
			+ "				[\"facility\"  , \"\"      , \"string\", \"\"              , \"XMM\"] \n"
			+ "		]\n"
			+ "		,\n"
			+ "    \"table\": {}\n"
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
		this.setArgParam("-tmin", "a,b");
		this.setArgParam("-tmax", "c");
		this.setArgParam("-exptime", "'doudo");
		this.process();
	}
	/* (non-Javadoc)
	 * @see saadadb.dataloader.testprov.ParamShaker#runFirstWithWrongIParams()
	 */
	protected void runFirstWithWrongIParams() throws Exception{
		super.runFirstWithWrongIParams();
		this.setArgParam("-tmin", "'11 03 2013'");
		this.setField("obsStart", null, null, null, "");
		this.setField("obsEnd", null, null, null, "");
		this.process();
	}
	/* (non-Javadoc)
	 * @see saadadb.dataloader.testprov.ParamShaker#runFirstWithPWrongMParams()
	 */
	protected void runFirstWithPWrongMParams() throws Exception{
		super.runFirstWithPWrongMParams();
		this.setArgParam("-tmin", "a,b");
		this.process();
	}
	/* (non-Javadoc)
	 * @see saadadb.dataloader.testprov.ParamShaker#runFirstWithPWrongIParams()
	 */
	protected void runFirstWithPWrongIParams() throws Exception{
		super.runFirstWithPWrongIParams();
		this.setField("obsStart", null, null, null, "");
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
		this.setArgParam("-tmin", "a,b");
		this.setArgParam("-tmax", "c");
		this.setArgParam("-exptime", "'doudo");
		this.process();
	}
	/* (non-Javadoc)
	 * @see saadadb.dataloader.testprov.ParamShaker#runLastWithWrongIParams()
	 */
	protected void runLastWithWrongIParams() throws Exception{
		super.runLastWithWrongIParams();
		this.setField("obsStart", null, null, null, "sssss");
		this.setField("obsEnd", null, null, null, "");
		this.process();
	}
	/* (non-Javadoc)
	 * @see saadadb.dataloader.testprov.ParamShaker#runLastWithPWrongMParams()
	 */
	protected void runLastWithPWrongMParams() throws Exception{
		super.runLastWithPWrongMParams();
		this.setArgParam("-tmin", "a,b");
		this.process();
	}
	/* (non-Javadoc)
	 * @see saadadb.dataloader.testprov.ParamShaker#runLastWithPWrongIParams()
	 */
	protected void runLastWithPWrongIParams() throws Exception{
		super.runLastWithPWrongIParams();
		this.setField("obsStart", null, null, null, "sssss");
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
		sp.setItemToProcess(ap.getNumber());
		sp.processAll();
		sp.showReport();
		} catch(Exception e){
			e.printStackTrace();
		}
		Database.close();
	}

}
