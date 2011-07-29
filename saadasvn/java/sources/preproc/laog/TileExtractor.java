package preproc.laog;


import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import nom.tam.fits.FitsException;

/*******************************************************************************
 * LAOG project
 *
 * "@(#) $Id: TileExtractor.java,v 1.3 2010/01/05 14:24:16 saada Exp $"
 *
 * History
 * -------
 * $Log: TileExtractor.java,v $
 * Revision 1.3  2010/01/05 14:24:16  saada
 * *** empty log message ***
 *
 * Revision 1.1  2009/11/16 13:03:24  saada
 * Lets;s start with V2
 *
 * Revision 1.2  2009/05/22 15:12:31  saada
 * DM Demo
 *
 * Revision 1.1  2009/05/14 16:28:01  saada
 * pre release misa a jour du SIAP
 *
 ******************************************************************************/
/** * @version $Id$

 * Program extracting 'box' around a star from a 'mega' fits file and save it
 * into a new file. The argument of the programs are:
 * @param inFile 'mega' FITS file name
 * @param ra rigth ascension of the star (in degrees)
 * @param dec declinaison of the star (in degrees)
 * @param boxWidth box width in arcminutes or boxWith whidth un pixel
 * @param outFile output file name
 */
public class TileExtractor {

	/* The program implementation comment can go here. */
	public static void main(String[] args) throws FitsException, IOException {

		if (args.length == 0) {
			logger_.log(Level.SEVERE, "zero argument (ARGUMENT USAGE : [fileNameIn RA DEC DimX DimY FileNameOut])");
			System.exit(-1);
		} else if (args.length > 6) {
			logger_.log(Level.SEVERE, "too argument (ARGUMENT USAGE : [fileNameIn RA DEC DimX DimY FileNameOut])");
			System.exit(-1);
		} else if (args.length < 6) {
			logger_.log(Level.SEVERE, "not enought argument (ARGUMENT USAGE :  [fileNameIn RA DEC DimX DimY FileNameOut])");
			System.exit(-1);
		} else {

			//boolean exist = fileExistsInDirectory(".", args[0]);
			boolean exist = (new File(args[0])).exists();

			if (exist) {
				FistImageExctractor ImgToRedim = new FistImageExctractor(args[0]);

				double[] coords = new double[]{Double.parseDouble(args[1]), Double.parseDouble(args[2])};

				if (isFloat(args[3])) {

					double[] boxDimsUser = new double[]{Double.parseDouble(args[3]), Double.parseDouble(args[4])};
					ImgToRedim.getBox(coords, boxDimsUser, args[5]);

				} else {

					int[] boxDimsUser = new int[]{Integer.parseInt(args[3]), Integer.parseInt(args[4])};
					ImgToRedim.getBox(coords, boxDimsUser, args[5]);
				}

			} else {

				logger_.log(Level.SEVERE, "Wrong file name, or file isn't in this repository");
				System.exit(-1);
			}
		}
	}

	private static final Logger logger_ = Logger.getLogger(TileExtractor.class.getName());

	private static boolean isFloat(String s) {
		return (s.split("\\.").length == 1);
	}

	private static boolean fileExistsInDirectory(String directoryName, String fileName) {

		boolean exists = false;
		File racine = new File(directoryName + File.separator + fileName);
		if (racine.exists()) {
			exists = true;
		}
		return exists;
	}
}

/*___oOo___*/