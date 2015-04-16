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
public class PositionErrorShaker extends ParamShaker{

	static {
		TEMPLATE = "{"

			+ "\"parameters\": [ \n"
			+ "	\"-category=misc\" , \n"
			+ "	\"-collection=XMM\", \n"
			+ "	\"-filename=obscore\", \n"
			+ "	\"-repository=no\", \n"
			+ "	\"-posmapping=first\" , \n"
			+ "	\"-position=RA,RA\"  \n"
			+ "	\"-poserror='2arcmin'\" \n"
			+ "	\"-sfov='12arcmin'\" \n"
			+ "	\"-sregion='CARRE 1 2 3 4'\" \n"
			+ "	\"-system='FK5'\"  \n"
			+ "], \n"
			+ "\"data\": { \n"
			+ "    \"header\": [\n"
			+ "             [\"RA\"        , \"double\", \"deg\"   , \"\"              , \"10\"], \n"
			+ "				[\"DEC\"       , \"double\", \"deg\"   , \"\"              , \"+20\"] \n"
			+ "             [\"errmaj\"    , \"double\", \"arcsec\"   , \"\"              , \"20\"], \n"
			+ "             [\"fov\"       , \"double\", \"arcmin\"   , \"\"              , \"10\"], \n"
			+ "             [\"region\"    , \"String\", \"\"   , \"\"              , \"12 20 30 40\"], \n"
			+ "		]\n"
			+ "		,\n"
			+ "    \"table\": {}\n"
			+ "    }\n"
			+ "}\n";
	}

	PositionErrorShaker() throws Exception{
		super();
		this.paramsOfInterest = new HashSet<String>();
		this.paramsOfInterest.add("s_resolution");
		this.paramsOfInterest.add("s_region");
		this.paramsOfInterest.add("s_fov");
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
		this.setArgParam("-sfov", "fsdfsdff");
		this.setArgParam("-sregion", "fsdfsdff");
		this.setArgParam("-poserror", "fsdfsdff");
		this.process();
	}
	/* (non-Javadoc)
	 * @see saadadb.dataloader.testprov.ParamShaker#runFirstWithWrongIParams()
	 */
	protected void runFirstWithWrongIParams() throws Exception{
		super.runFirstWithWrongIParams();
		this.setField("errmaj", null, "arcmin", null, "zzzzz");
		this.setField("fov", null, "arcmin", null, "zzzzz");
		this.setField("region", null, "arcmin", null, "zzzzz");
		this.process();
	}
	/* (non-Javadoc)
	 * @see saadadb.dataloader.testprov.ParamShaker#runFirstWithPWrongMParams()
	 */
	protected void runFirstWithPWrongMParams() throws Exception{
		super.runFirstWithPWrongMParams();
		this.setArgParam("-poserror", "12zaeaze");
		this.process();
	}
	/* (non-Javadoc)
	 * @see saadadb.dataloader.testprov.ParamShaker#runFirstWithPWrongIParams()
	 */
	protected void runFirstWithPWrongIParams() throws Exception{
		super.runFirstWithPWrongIParams();
		this.setField("errmaj", null, "arcmin", null, "zzzzz");
		this.setField("fov", null, "arcmin", null, "zzzzz");
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
		this.setArgParam("-poserror", "'fsdfsdff'");
		this.process();
	}
	/* (non-Javadoc)
	 * @see saadadb.dataloader.testprov.ParamShaker#runLastWithWrongIParams()
	 */
	protected void runLastWithWrongIParams() throws Exception{
		super.runLastWithWrongIParams();
		this.removeField("errmaj");
		this.removeField("fov");
		this.removeField("region");
		this.process();
	}
	/* (non-Javadoc)
	 * @see saadadb.dataloader.testprov.ParamShaker#runLastWithPWrongMParams()
	 */
	protected void runLastWithPWrongMParams() throws Exception{
		super.runLastWithPWrongMParams();
		this.setArgParam("-poserror", "fsdfsdff");
		this.process();
	}
	/* (non-Javadoc)
	 * @see saadadb.dataloader.testprov.ParamShaker#runLastWithPWrongIParams()
	 */
	protected void runLastWithPWrongIParams() throws Exception{
		super.runLastWithPWrongIParams();
		this.removeField("errmaj");
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
		PositionErrorShaker sp = new PositionErrorShaker();
		sp.setItemToProcess(ap.getNumber());
		sp.processAll();
		sp.showReport();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Database.close();

	}

}
