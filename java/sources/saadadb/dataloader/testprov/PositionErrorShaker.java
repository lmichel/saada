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
			+ "	\"-poserror='2,1'\" \n"
			+ "	\"-poserrorunit='arcmin'\" \n"
			+ "	\"-system='FK5'\"  \n"
			+ "], \n"
			+ "\"fields\": { \n"
			+ "    \"header\": [\n"
			+ "             [\"RA\"        , \"double\", \"deg\"   , \"\"              , \"10\"], \n"
			+ "				[\"DEC\"       , \"double\", \"deg\"   , \"\"              , \"+20\"] \n"
			+ "             [\"errmaj\"       , \"double\", \"arcmin\"   , \"\"              , \"20\"], \n"
			+ "				[\"errmin\"       , \"double\", \"arcmin\"   , \"\"              , \"10\"] \n"
			+ "		]\n"
			+ "		,\n"
			+ "    \"columns\": []\n"
			+ "    }\n"
			+ "}\n";
	}

	PositionErrorShaker() throws Exception{
		super();
		this.paramsOfInterest = new HashSet<String>();
		this.paramsOfInterest.add("error_min_csa");
		this.paramsOfInterest.add("error_maj_csa");
		this.paramsOfInterest.add("error_angle_csa");
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
		this.setArgParam("-position", "a,b");
		this.process();
	}
	/* (non-Javadoc)
	 * @see saadadb.dataloader.testprov.ParamShaker#runFirstWithWrongIParams()
	 */
	protected void runFirstWithWrongIParams() throws Exception{
		super.runFirstWithWrongIParams();
		this.setArgParam("-position", "alpha,delta");
		this.setField("RA", null, null, null, "AZERT");
		this.setField("DEC", null, null, null, "AZERT");
		this.process();
	}
	/* (non-Javadoc)
	 * @see saadadb.dataloader.testprov.ParamShaker#runFirstWithPWrongMParams()
	 */
	protected void runFirstWithPWrongMParams() throws Exception{
		super.runFirstWithPWrongMParams();
	    this.setArgParam("-position", "12,AZERT");
		this.process();
	}
	/* (non-Javadoc)
	 * @see saadadb.dataloader.testprov.ParamShaker#runFirstWithPWrongIParams()
	 */
	protected void runFirstWithPWrongIParams() throws Exception{
		super.runFirstWithPWrongIParams();
		this.setArgParam("-position", "alpha,delta");
		this.setField("RA", null, null, null, "AZERT");
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
		this.setArgParam("-position", "a,b");
		this.process();
	}
	/* (non-Javadoc)
	 * @see saadadb.dataloader.testprov.ParamShaker#runLastWithWrongIParams()
	 */
	protected void runLastWithWrongIParams() throws Exception{
		super.runLastWithWrongIParams();
		this.setField("RA", null, null, null, "AZERT");
		this.setField("DEC", null, null, null, "AZERT");
		this.process();
	}
	/* (non-Javadoc)
	 * @see saadadb.dataloader.testprov.ParamShaker#runLastWithPWrongMParams()
	 */
	protected void runLastWithPWrongMParams() throws Exception{
		super.runLastWithPWrongMParams();
	    this.setArgParam("-position", "12,AZERT");
		this.process();
	}
	/* (non-Javadoc)
	 * @see saadadb.dataloader.testprov.ParamShaker#runLastWithPWrongIParams()
	 */
	protected void runLastWithPWrongIParams() throws Exception{
		super.runLastWithPWrongIParams();
		this.setField("RA", null, null, null, "AZERT");
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
		sp.processAll();
		sp.showReport();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Database.close();

	}

}
