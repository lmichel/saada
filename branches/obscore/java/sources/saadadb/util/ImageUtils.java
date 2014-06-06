package saadadb.util;

/*
 * @author: L MICHEL
 * Utilities handling FITS images
 *          
 * 04/2009: methods buildTileFile added
 */

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;

import javax.imageio.ImageIO;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsFactory;
import nom.tam.fits.Header;
import nom.tam.util.BufferedDataOutputStream;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.dataloader.mapping.ProductMapping;
import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.products.FitsDataFile;
import saadadb.products.Image2DBuilder;
import saadadb.products.inference.Coord;
import saadadb.products.inference.Image2DCoordinate;
import cds.astro.Coo;

//import com.sun.image.codec.jpeg.JPEGCodec;
//import com.sun.image.codec.jpeg.JPEGImageEncoder;

/**
 * @author michel
 * @version $Id$
 */
public abstract class ImageUtils {

	/** 
	 * Creates a JPEG file corresponding to the 2D image product with the
	 * calibres in parameter.
	 * @param nameFileOut
	 * @param max_size Size of the largest side of the thumbnail
	 * @throws IgnoreException 
	 */
	public static void createImage(String nameFileOut, FitsDataFile productFile, int max_size) throws IgnoreException {
		try{
			Messenger.printMsg(Messenger.TRACE, "Create vignette");
			int bin_size = 256;
			int[] size;
			int ww, hh, nbit;
			FitsDataFile fp = (FitsDataFile) productFile;
			Object img_pixels= fp.getImagePixels();
			nbit = fp.getBitPIx();
			size = fp.getImageSize();
			ww = size[0];
			hh = size[1];


			BufferedImage img2 = ImageUtils.makeSampledImage(ww, hh, bin_size, nbit, img_pixels);

			/*
			 * Reduced image
			 */
			Image img3;
			if( ww > hh ) {
				img3 = img2.getScaledInstance(max_size, -1, 0);
				/*
				 * make sure that the image has at last pixel height
				 */
				if( img3.getHeight(null) == 0 ) {
					img3 = img2.getScaledInstance(max_size, 1, 0);	
				}
			} else {
				img3 = img2.getScaledInstance(-1, max_size, 0);	
				/*
				 * make sure that the image has at last pixel with
				 */
				if( img3.getWidth(null) == 0 ) {
					img3 = img2.getScaledInstance(1, max_size, 0);	
				}
			}
			int i2w = img3.getWidth(null);
			int i2h = img3.getHeight(null);
			/*
			 * Buffered image to be encoded
			 */
			BufferedImage bi = new BufferedImage(i2w, i2h, BufferedImage.TYPE_BYTE_GRAY);		
			/*
			 * Flip the image horizontally
			 */
			AffineTransform affineTransform = AffineTransform.getScaleInstance(1, -1); 
			affineTransform.translate(0, -bi.getHeight(null));
			AffineTransformOp op = new AffineTransformOp(affineTransform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);			
			bi = op.filter(bi, null);		
			/*
			 * Build the image
			 */
			Graphics2D g2d = bi.createGraphics();
			g2d.drawImage(img3, affineTransform, null); 
			ImageIO.write(bi, "jpeg", new FileOutputStream(nameFileOut));
			Messenger.printMsg(Messenger.TRACE, "Thumbnail (" + i2w + "x" +i2h + ") <" + nameFileOut + "> created");
		} catch(Exception e){
			Messenger.printStackTrace(e);
			IgnoreException.throwNewException(SaadaException.UNSUPPORTED_OPERATION, "Can not generate vignette: " + e.getMessage());
		}
	}

	/**
	 * @param ww
	 * @param hh
	 * @param bin_size
	 * @param nbit
	 * @param img_pixels
	 * @return
	 */
	public static BufferedImage makeSampledImage(int ww, int hh, int bin_size, int nbit, Object img_pixels) {
		/*
		 * Set the sampled image size: avoid to read all pixels
		 */
		int step = 1;
		if( ww >= hh && ww > bin_size ) {
			step = ww/bin_size;
		} else if( ww < hh && hh > bin_size ) {
			step = hh/bin_size;
		}
		int reste_w  = 1;
		if( (ww % step) == 0 ) {
			reste_w = 0;
		}
		int reste_h  = 1;
		if( (hh % step) == 0 ) {
			reste_h = 0;
		}
		int rww = (ww/step) + reste_w;
		int rhh = (hh/step) + reste_h;
		/*
		 * Build the sampled image
		 */
		double[] pixel = new double[rww*rhh];

		if(nbit==32){
			int[] tmp = (int[])img_pixels;
			int pos = 0;
			for( int h=0 ; h<hh ; h+=step ) {
				for( int w=0 ; w<ww ; w+=step ) {
					pixel[pos++] = tmp[(h*ww) + w];						
				}
			}
		} else if(nbit==16){
			if( img_pixels instanceof short[][] ) {
				short[][] tmp = (short[][])img_pixels;
				int pos = 0;
				for( int h=0 ; h<hh ; h+=step ) {
					for( int w=0 ; w<ww ; w+=step ) {
						pixel[pos++] = tmp[w][h];						
					}
				}				
			} else {
				short[] tmp = (short[])img_pixels;
				int pos = 0;
				for( int h=0 ; h<hh ; h+=step ) {
					for( int w=0 ; w<ww ; w+=step ) {
						pixel[pos++] = tmp[(h*ww) + w];						
					}
				}
			}
		} else if(nbit==8){
			byte[] tmp = (byte[])img_pixels;
			int pos = 0;
			for( int h=0 ; h<hh ; h+=step ) {
				for( int w=0 ; w<ww ; w+=step ) {
					pixel[pos++] = tmp[(h*ww) + w];
				}
			}
		} else if(nbit==64){
			long[] tmp = (long[])img_pixels;
			int pos = 0;
			for( int h=0 ; h<hh ; h+=step ) {
				for( int w=0 ; w<ww ; w+=step ) {
					pixel[pos++] = (int)((long)(0xffffffff*tmp[(h*ww) + w])/0xffffffffffffffffL);
				}
			}
		} else if(nbit==-32){
			float[] tmp = (float[])img_pixels;
			int pos = 0;
			/*
			 * Make sure the range is wide enough to get something after a cast to int
			 */
			double min=0, max=0;
			for( int h=0 ; h<hh ; h+=step ) {
				for( int w=0 ; w<ww ; w+=step ) {
					float val =  tmp[(h*ww) + w];
					if( val < min ) {
						min = val;
					}
					else if( val > max ) {
						max = val;
					}
				}
			}
			double scale = 1;
			if( (max - min) < 100 ) {
				scale = 100/(max - min);
			}
			for( int h=0 ; h<hh ; h+=step ) {
				for( int w=0 ; w<ww ; w+=step ) {
					pixel[pos++] = java.lang.Math.round((scale*tmp[(h*ww) + w]));
				}
			}
		} else if(nbit==-64){
			double[] tmp = (double[])img_pixels;
			/*
			 * Make sure the range is wide enough to get something after a cast to in
			 */
			double min=0, max=0;
			for( int h=0 ; h<hh ; h+=step ) {
				for( int w=0 ; w<ww ; w+=step ) {
					double val =  tmp[(h*ww) + w];
					if( val < min ) {
						min = val;
					}
					else if( val > max ) {
						max = val;
					}
				}
			}
			double scale = 1;
			if( (max - min) < 100 ) {
				scale = 100/(max - min);
			}
			int pos = 0;
			for( int h=0 ; h<hh ; h+=step ) {
				for( int w=0 ; w<ww ; w+=step ) {
					pixel[pos++] = (int) java.lang.Math.round(scale*tmp[(h*ww) + w]);
				}
			}
		}
		/*
		 * Autocut (code dervived from Aladin (r) P. Fernique CDS)
		 */
		byte[] pout=null;
		pout = getPix8Bits(pout, pixel, rww, rhh);	
		//pout = imageSampler(pixel, ww, hh);
		/*
		 * Full size image
		 */
		BufferedImage img2 = new BufferedImage(rww, rhh, BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster wr = img2.getRaster();
		wr.setDataElements (0, 0, rww, rhh, pout);
		return img2;
	}

	/** Mise sur 8 bits de l'image
	 * (code dervived from Aladin (r) P. Fernique CDS)
	 * @param pIn le tableau des pixels en entree
	 * @param bitpix la profondeur des pixels (format FITS)
	 * @param width,height Largeur et hauteur de l'image
	 * @param minCut,maxCut valeurs limites
	 * @param autocut true s'il faut appliquer l'algo d'autocut
	 * @param ntest profondeur de la recursivite, si 0, minCut et maxCut ne sont
	 *              pas pris en compte
	 * @return pOut Le tableau des pixels sur 8 bits
	 */
	private static byte[] getPix8Bits(byte[] pOut, double[] pIn, int width, int height) {
		return getPix8Bits(pOut,pIn,width,height,0,0,0);
	}

	private static byte[] getPix8Bits(byte[] pOut, double[] pIn, int width, int height,
			double minCut,double maxCut,int ntest) {
		int i,j,k;
		boolean flagCut=(ntest>0);

		if( pOut==null ) pOut = new byte[width*height];

		double max = 0, max1 =0;
		double min = 0, min1 = 0;
		int MARGE=(int)(width*0.05);
		double c;

		boolean first=true;
		long nmin=0,nmax=0;
		double sum=0;
		int nb=0;
		for( i=MARGE; i<height-MARGE; i++ ) {
			for( j=MARGE; j<width-MARGE; j++ ) {
				c = (double)pIn[i*width+j];
				//	    			On ecarte les valeurs sans signification
				if( Double.isNaN(c) /* ||  c==blank */) continue;

				if( flagCut ) {
					if( c<minCut || c>maxCut ) continue;
				}

				sum += c;
				nb++;

				if( first ) { max=max1=min=min1=c; first=false; }

				if( min>c ) { min=c; nmin=1; }
				else if( max<c ) { max=c; nmax=1; }
				else if( c==min ) nmin++;
				else if( c==max ) nmax++;

				if( c<min1 && c>min || min1==min ) min1=c;
				else if( c>max1 && c<max || max1==max ) max1=c;

				/*
				 if( min>c ) min=c;
				 else if( max<c ) max=c;
				 */
			}


			//	    		Memorisation des valeurs extremes de pixels (pour info dans les Properties)
			if( flagCut ) { }

			if( min1-min>max1-min1 && min1!=Double.MAX_VALUE && min1!=max ) min=min1;
			if( max-max1>max1-min1 && max1!=Double.MIN_VALUE && max1!=min  ) max=max1;
		}
		double mediante = (max-min)/2 + min;

		mediante = (sum/nb);
		boolean methode_bourrin = true;
		if(methode_bourrin ) {
			double alpha = 300/(max - min);
			double beta = -alpha*min;
			alpha = -128/(min - mediante);
			beta  = -alpha*min;
			for( i = 0; i < pOut.length; i++) {
				c = (double)pIn[i];
				double val = ( (alpha*c) + beta);
				if( val >= 255 ) {
					pOut[i] = (byte) 255;
				}
				else {
					pOut[i] = (byte)val;
				}
				//System.out.println(val + " " + pOut[i]);
				pOut[i] = (byte) (0xff - pOut[i]);
			}
			return pOut;
		}

		//			Histogramme
		int nbean = 10000;
		double l = (max-min)/(double)nbean;
		int[] bean = new int[nbean];
		for( i=MARGE; i<height-MARGE; i++ ) {
			for( k=MARGE; k<width-MARGE; k++) {
				c = (double)pIn[i*width+k];

				j = (int)((c-min)/l);
				if( j==bean.length ) j--;
				if( j>=bean.length || j<0 ) continue;
				bean[j]++;
			}
		}

		//	    	Selection du min et du max en fonction du volume de l'information
		//	    	que l'on souhaite garder
		int [] mmBean = getMinMaxBean(bean);
		max1=max; min1=min;
		max1 = mmBean[1]*l+min1;
		//	    	???		min1 += mmBean[0]*l;
		min1 = mmBean[0]*l;

		if( mmBean[0]>mmBean[1]-5 && ntest<10 ) {
			if( min1>min ) min=min1;
			if( max1<max ) max=max1;
			return getPix8Bits(pOut,pIn,width,height,min,max, ntest+1);
		}

		min=min1; max=max1;



		//Simple cut du min et du max, puis extension/reduction sur les 8 bits
		double r = 256./(max - min);
		for( i = 0; i < pOut.length; i++) {
			c = (double)pIn[i];
			pOut[i] = (byte)( c<=min?0x00:c>=max?0xff:(int)( ((c-min)*r) ) & 0xff);
			pOut[i] = (byte) (0xff - pOut[i]);
		}
		return pOut;
	}

	/** Determination pour un tableau de bean[] de l'indice du bean min
	 * et du bean max en fonction d'un pourcentage d'information desire
	 * (code dervived from Aladin (r) P. Fernique CDS)
	 * @param bean les valeurs des beans provenant de l'analyse d'une image
	 * @return mmBean[2] qui contient les indices du bean min et du bean max
	 */
	private static int[] getMinMaxBean(int [] bean) {
		double minLimit=0.003; 	// On laisse 3 pour mille du fond
		double maxLimit=0.999;    // On laisse 1 pour mille des etoiles
		int totInfo;			// Volume de l'information
		int curInfo;			// Volume courant en cours d'analyse
		int [] mmBean = new int[2];	// indice du bean min et du bean max
		int i;

		// Determination du volume de l'information
		for( totInfo=i=0; i<bean.length; i++ ) {
			totInfo+=bean[i];
		}

		// Positionnement des indices des beans min et max respectivement
		// dans mmBean[0] et mmBean[1]
		for( mmBean[0]=mmBean[1]=-1, curInfo=i=0; i<bean.length; i++ ) {
			curInfo+=bean[i];
			double p = (double)curInfo/totInfo;
			if( mmBean[0]==-1 ) {
				if( p>minLimit ) mmBean[0]=i;
			} else if( p>maxLimit ) { mmBean[1]=i; break; }
		}

		// Verification que tout s'est bien passe
		if( mmBean[0]==-1 || mmBean[1]==-1 ) {
			System.err.println("Image autocut problem => no autocut applied");
			mmBean[0]=0;
			mmBean[1]=bean.length-1;
		}
		return mmBean;
	}
	/****************************************/

	/**
	 * @param ra
	 * @param dec
	 * @param box_size: box size in degrees
	 * @param source_file
	 * @param cdh : used to know which extension to load
	 * @param dest_file
	 * @throws Exception 
	 */
	static public void buildTileFile(double ra, double dec, double box_size_ra, double box_size_dec, String source_file, ProductMapping cdh, String dest_file) throws Exception{
		File sf = new File(source_file);
		Image2DBuilder img  = new Image2DBuilder(sf,cdh);
		img.bindDataFile();
		Map<String, AttributeHandler> ahs = img.getProductAttributeHandler();
		Image2DCoordinate i2c = new Image2DCoordinate();
		i2c.setImage2DCoordinate(ahs);
		Messenger.printMsg(Messenger.TRACE, "Extract tile at position " + ra + " " + dec + " from file " + source_file);
		ImageFITSTile ift ;
		ift = new ImageFITSTile(i2c, ra, dec, box_size_ra, box_size_dec);

		/*
		 * Extract pixels
		 */		
		int[] corner = new int[2];
		int[] size = new int[2];
		FitsDataFile fp = ((FitsDataFile)(img.getProducFile()));
		size[1]     = ift.getSizeY();
		size[0]     = ift.getSizeX();
		corner[1]   = ift.getCornerY();
		corner[0]   = ift.getCornerX();
		Object tile = fp.getImagePixels(ift.getCorner(),ift.getSize());

		int nbit = fp.getBitPIx();
		Object pixmap=null;
		if(nbit==32){
			int[] tmp = (int[])tile;
			pixmap = new int[size[0]][size[1]];
			int[][]pm = (int[][]) pixmap;
			for( int l=0 ; l<size[0] ; l++ ) {
				for( int col=0 ; col<size[1] ; col++ ) {
					pm[l][col] = tmp[(l * size[1]) + col];
				}
			}
		}
		else if(nbit==16){
			short[] tmp = (short[])tile;
			pixmap = new short[size[0]][size[1]];
			short[][]pm = (short[][]) pixmap;
			for( int l=0 ; l<size[0] ; l++ ) {
				for( int col=0 ; col<size[1] ; col++ ) {
					pm[l][col] = tmp[(l * size[1]) + col];
				}
			}
		}
		else if(nbit==8){
			byte[] tmp = (byte[])tile;
			pixmap = new byte[size[0]][size[1]];
			byte[][]pm = (byte[][]) pixmap;
			for( int l=0 ; l<size[0] ; l++ ) {
				for( int col=0 ; col<size[1] ; col++ ) {
					pm[l][col] = tmp[(l * size[1]) + col];
				}
			}
		}
		else if(nbit==64){
			long[] tmp = (long[])tile;
			pixmap = new long[size[0]][size[1]];
			long[][]pm = (long[][]) pixmap;
			for( int l=0 ; l<size[0] ; l++ ) {
				for( int col=0 ; col<size[1] ; col++ ) {
					pm[l][col] = tmp[(l * size[1]) + col];
				}
			}
		}
		else if(nbit==-32){
			float[] tmp = (float[])tile;
			pixmap = new float[size[0]][size[1]];
			float[][]pm = (float[][]) pixmap;
			for( int l=0 ; l<size[0] ; l++ ) {
				for( int col=0 ; col<size[1] ; col++ ) {
					pm[l][col] = tmp[(l * size[1]) + col];
				}
			}
		}
		else if(nbit==-64){
			double[] tmp = (double[])tile;
			pixmap = new double[size[0]][size[1]];
			double[][]pm = (double[][]) pixmap;
			for( int l=0 ; l<size[0] ; l++ ) {
				for( int col=0 ; col<size[1] ; col++ ) {
					pm[l][col] = tmp[(l * size[1]) + col];
				}
			}
		}
		/*
		 * Builds the output file
		 */
		Messenger.printMsg(Messenger.TRACE, "Write output file " + dest_file);		
		Fits fNew = new Fits();
		BasicHDU hh = FitsFactory.HDUFactory(pixmap);
		Header hdrn = hh.getHeader();
		fNew.addHDU(hh);

		hdrn.addValue("BITPIX", nbit, "");
		for( AttributeHandler ah: ahs.values() ) {
			String key = ah.getNameorg();
			String type = ah.getType();
			String value = ah.getValue();
			if( key.equals("XTENSION") || key.equals("BITPIX")) continue;
			if( key.equalsIgnoreCase("NAXIS1") ){
				long x = (Integer)size[0];
				hdrn.addValue(key, x, ah.getComment());
			}
			else if( key.equalsIgnoreCase("NAXIS2") ){
				long x = (Integer)size[1];
				hdrn.addValue(key, x, ah.getComment());
			}
			else if( key.equalsIgnoreCase("CRPIX1") ){
				hdrn.addValue(key, Math.abs(corner[0] - ift.getCenterX()) , ah.getComment());
			}
			else if( key.equalsIgnoreCase("CRPIX2") ){
				hdrn.addValue(key, Math.abs(corner[1] - ift.getCenterY()), ah.getComment());
			}
			else if( key.equalsIgnoreCase("CRVAL1") ){
				hdrn.addValue(key, ra, ah.getComment());
			}
			else if( key.equalsIgnoreCase("CRVAL2") ){
				hdrn.addValue(key, dec, ah.getComment());
			}
			else if( type == "boolean" ) {
				hdrn.addValue(key, new Boolean(value) , ah.getComment());
			}
			else if( type == "int" ) {
				hdrn.addValue(key, new Integer(value) , ah.getComment());
			}
			else if( type == "long" ) {
				hdrn.addValue(key, new Long(value) , ah.getComment());
			}
			else if( type == "float" ) {
				hdrn.addValue(key, new Float(value) , ah.getComment());
			}
			else if( type == "double" ) {
				hdrn.addValue(key, new Double(value) , ah.getComment());
			}
			else  {
				hdrn.addValue(key, value , ah.getComment());
			}			
		}
		hdrn.addValue("SRCFILE", sf.getName(), "File the tile were extracted from");
		FileOutputStream d1 = new FileOutputStream(dest_file);
		BufferedOutputStream d2 = new BufferedOutputStream(d1);
		DataOutputStream d3 = new DataOutputStream(d2);
		BufferedDataOutputStream s = new BufferedDataOutputStream(d3);
		fNew.write(s);
	}

	/**
	 * Store the pixel tile matching the search box.
	 * Data are stored following the FITSn order (Y-X)
	 * @author michel
	 *
	 */
	static class ImageFITSTile {
		double center[]   = new double[2];
		int size[]   = new int[2];
		int corner[] = new int[2];
		ImageFITSTile(Image2DCoordinate i2c, double ra, double dec, double box_size_ra, double box_size_dec) throws Exception {
			Coord c_c = new Coord(ra, dec);
			i2c.GetXY(c_c);
			center[0] = (i2c.getYnpix() - c_c.getY());
			center[1] = c_c.getX();
			size[0]   = (int) (Math.abs(box_size_dec/i2c.getIncD()));
			size[1]   = (int) (Math.abs(box_size_ra/i2c.getIncA())) ;
			corner[0] = (int) ((i2c.getYnpix() - c_c.getY()) - (size[0]/2));
			corner[1] = (int) (c_c.getX() - size[1]/2);		
			if( corner[0] < 0 ) {
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "set corner[0] to 0");
				corner[0] = 0;
			}
			if( (corner[0] + size[0]) >  i2c.getYnpix()) {
				size[0] = i2c.getYnpix() -corner[0]  ;
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "set size[0] to " + size[0]);
			}
			if( corner[1] < 0 ) {
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "set corner[1] to 0");
				corner[1] = 0;
			}
			if( (corner[1] + size[1]) >  i2c.getXnpix()) {
				size[1] = i2c.getXnpix() -corner[1]  ;
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "set size[1] to " + size[1]);
			}
		}
		public int[] getSize() {
			return size;
		}
		public int getSizeX() {
			return size[1];
		}
		public int getSizeY() {
			return size[0];
		}
		public int[] getCorner() {
			return corner;
		}
		public int getCornerX() {
			return corner[1];
		}
		public int getCornerY() {
			return corner[0];
		}
		public double[] getCenter() {
			return center;
		}
		public double getCenterX() {
			return center[1];
		}
		public double getCenterY() {
			return center[0];
		}
	}
	public static void main(String[] args) throws Exception {


		ArgsParser ap = new ArgsParser(args);
		Database.init(ap.getDBName());
		Image2DBuilder sp ;

		String img = "/rawdata/XIDResult/wfi_iap/images/AIP_XMM/DPLeo_R-BB_Rc_162_ESO844-852-set_6.fits";
		Coo  c = new Coo(169.082889, 17.9419328);
		buildTileFile(c.getLon(), c.getLat()
				, 1, 1
				, img
				, (new ArgsParser(new String[]{"-collection=WideFieldData"}).getProductMapping())
		, "/home/michel/Desktop/vignette.fits");
		System.exit(1);
		c = new Coo("15:33:17.06 -08:30:20.4");
		//		c = new Coo(0, +90);
		//		buildTileFile(c.getLon(), c.getLat()
		//				, 3./60, 2./60
		// 				, "/home/michel/Desktop/DSS.fits"
		//				, new ConfigurationImage("", new ArgsParser(new String[]{"-collection=ObsCore"}))
		//				, "/home/michel/Desktop/vignette.fits");
		//		System.exit(1);
		//		c = new Coo(233.32105, -08.50569);
		c = new Coo(12.3309909, -52.0671861);
		buildTileFile(c.getLon(), c.getLat()
				, 1./60, 1./60
				, "/rawdata/wfi_iap/images/V/BPM-16274-V-BB_V_89_ESO843-970-set_11.fits"
				,(new ArgsParser(new String[]{"-collection=XID"}).getProductMapping())
		, "/home/michel/Desktop/vignette.fits");
		System.exit(1);
		c = new Coo(12.3309909, 0);
		buildTileFile(c.getLon(), c.getLat()
				, 1./60, 1./60
				, "/rawdata/WFI_IAP/images/V/BPM-16274-V-BB_V_89_ESO843-970-set_11.fits"
				, (new ArgsParser(new String[]{"-collection=XID"}).getProductMapping())
		, "/home/michel/Desktop/vignette.fits");

		//		sp = new Image2D(new File("/home/michel/Desktop/UZLib_V-BB_V_89_ESO843-852-set_4.fits"), new ConfigurationImage("", new ArgsParser(new String[]{"-collection=XID"})));
		//		sp.loadProductFile(new ConfigurationImage("", new ArgsParser(new String[]{"-collection=XID"})));
		//		Messenger.debug_mode = true;
		//		WCSModel wm = new WCSModel(sp.getTableAttributeHandler());
		//		Coo  c = new Coo("15:32:19.41 -08:31:39.1");
		//		System.out.println(c.getLon() + " " +  c.getLat());
		//		double[] retour = wm.getXYValue(new int[] {4726, 4650});
		//		System.out.println(retour[0]/15.0 + " " + retour[1]);
		//		retour = wm.getXYValue(new double[] {4726., 4650.49});
		//		System.out.println(retour[0]/15.0 + " " + retour[1]);
		//		System.out.println(retour[0]/15.0 + " " + (retour[1] - c.getLat())*3600);		
		//		
		//		Image2DCoordinate i2c = new Image2DCoordinate();
		//		i2c.setImage2DCoordinate(sp.getTableAttributeHandler());
		//		Coord coord = new Coord(c.getLon()*15 , c.getLat());
		//		
		//		i2c.GetXY(coord);
		//
		//		System.out.println(coord.getX() + " " + coord.getY());		
		Database.close();

	}
}
