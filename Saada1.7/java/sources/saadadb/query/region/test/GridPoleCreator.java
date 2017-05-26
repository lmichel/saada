package saadadb.query.region.test;

import saadadb.query.region.test.gridcreator.FillTablePix;
import saadadb.query.region.test.gridcreator.GridBuilder;
import saadadb.query.region.test.gridcreator.GridBuilderGalact;
import saadadb.query.region.test.gridcreator.UpdateHPXTMPSQL;



/**
 * Class GridPoleCreator
 * This class is only made to fill table at the pole
 * Can only empty the table, fill it from the dec parameter
 * And update the pixels thanks to another temp table
 * 
 * args[0] => Database Name
 * args[1] => Collection Name
 * args[2] => VOTable Name
 * args[3] => Dec_initial 
 * @author jremy
 * @version $Id$
 *
 */
public class GridPoleCreator {
	public static void main (String [] args) throws Exception{
		String nomBDD=args[0];
		String nomCOL=args[1];
		String nomVOT=args[2];		
		double dec_init=Double.parseDouble(args[3]);


		new GridBuilder("empty",nomBDD,nomCOL);
		GridBuilderGalact.execute(nomBDD,nomCOL,nomVOT,dec_init);
		FillTablePix.execute(nomCOL,nomBDD);
		UpdateHPXTMPSQL.execute(nomCOL,nomBDD);
	}
}
