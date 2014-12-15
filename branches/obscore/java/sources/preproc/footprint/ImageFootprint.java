/**
 * 
 */
package preproc.footprint;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsFactory;
import nom.tam.fits.Header;
import nom.tam.util.BufferedDataOutputStream;
import saadadb.dataloader.mapping.ProductMapping;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.products.Image2DBuilder;
import saadadb.products.datafile.FitsDataFile;
import saadadb.util.Messenger;

/**
 * Set to -1 all pixels out a the footprint of the image.
 * The footprint detection is done testing a floating pad of pixels.
 * @author laurentmichel
 *
 */
public class ImageFootprint {
	private final List<String> inputFiles = new ArrayList<String>();
	private final List<String> outFiles = new ArrayList<String>();
	private String inputFile;
	private String outFile;
	private int[][] outPixels;
	private int naxis1 = 0, naxis2 = 0, bitpix=0;
	private Map<String, AttributeHandler> ahs;

	ImageFootprint(String inputFile, String outFile) throws Exception{
		File f = new File(inputFile);
		if( f.isDirectory() ){
			File o = new File(outFile);
			if( !o.isDirectory() ){
				System.out.println("Both in and out must be directories");
			}
			Messenger.printMsg(Messenger.TRACE, "Reading " + inputFile);
			File[] content = f.listFiles();
			Messenger.printMsg(Messenger.TRACE, content.length + " files found ");
			for(File c: content) {
				try {
					this.inputFiles.add(c.getAbsolutePath());
					this.outFiles.add(outFile + File.separator + c.getName().replace(".gz", ""));
				} catch(Exception e){}
			}
		} else {
			this.inputFiles.add(inputFile);
			File o = new File(outFile);
			if( o.isDirectory() ){
				this.outFiles.add(outFile + File.separator + f.getName().replace(".gz", ""));
			} else {
				this.outFiles.add(outFile);
			}

		}
	}

	private boolean isInABlankPad(int line, int col, int width, int height) {
		int xMin = col - width;
		if( xMin < 0 ) xMin =0; 
		int yMin = line - height;
		if( yMin < 0 ) yMin =0; 

		int xMax = col + width;
		if( xMax >= this.naxis1 ) xMax = (this.naxis1 -1); 
		int yMax = line + height;
		if( yMax >= this.naxis2 ) yMax = (this.naxis2 -1); 

		for( int l=yMin ; l<=yMax ; l++) {
			for( int c=xMin ; c<=xMax ; c++) {
				int v = this.outPixels[l][c];
				if( v != 0 && v != -1 ){
					return false;
				}
			}
		}
		return true;
	}

	public void process() throws Exception {
		for(int num=0 ; num<this.inputFiles.size() ; num ++) {
			this.inputFile = this.inputFiles.get(num);
			this.outFile = this.outFiles.get(num);
			Messenger.printMsg(Messenger.TRACE, "Process " + this.inputFile);
			FitsDataFile input = new FitsDataFile(this.inputFile, (ProductMapping)null);
			Image2DBuilder img  = new Image2DBuilder(input,null);
			FitsDataFile inputFits = new FitsDataFile(img);
			this.ahs = img.getProductAttributeHandler();
			for( AttributeHandler ah: ahs.values()) {
				if( ah.getNameorg().equals("NAXIS1") ){
					this.naxis1 = Integer.parseInt(ah.getValue());
				} else if( ah.getNameorg().equals("NAXIS2") ){
					this.naxis2 = Integer.parseInt(ah.getValue());
				}else if( ah.getNameorg().equals("BITPIX") ) {
					this.bitpix = Integer.parseInt(ah.getValue());
				}
			}
			if(this.bitpix==32){
				int[] tmp = (int[])inputFits.getImagePixels();

				this.outPixels = new int[this.naxis1][this.naxis2];
				for( int l=0 ; l<this.naxis1 ; l++ ) {
					for( int col=0 ; col<this.naxis2 ; col++ ) {
						this.outPixels[l][col] = tmp[(l * this.naxis1) + col];
					}
				}
				this.buildTileFile();
			} else {
				FatalException.throwNewException(SaadaException.FILE_FORMAT, "bitpix " + bitpix + " not supported");
			}
		}

	}

    private void buildTileFile() throws Exception{
		for( int l=0 ; l<this.naxis1 ; l++ ) {
			for( int c=0 ; c<this.naxis2 ; c++ ) {
				if( this.isInABlankPad(l, c, 0, 2)) {
					this.outPixels[l][c] = -1;
				} else {
					break;
				}
			}
			for( int c=(this.naxis2 -1 ) ; c>=0 ; c-- ) {
				if( this.isInABlankPad(l, c, 0, 2)) {
					this.outPixels[l][c] = -1;
				} else {
					break;
				}
			}
		}
		for( int l=0 ; l<this.naxis1 ; l++ ) {
			for( int c=0 ; c<this.naxis2 ; c++ ) {
				if( this.isInABlankPad(l, c, 3, 3)) {
					this.outPixels[l][c] = -1;
				} 
			}
		}
		/*
		 * Builds the output file
		 */
		Messenger.printMsg(Messenger.TRACE, "Write output file " + this.outFiles);		
		Fits fNew = new Fits();
		BasicHDU hh = FitsFactory.HDUFactory(this.outPixels);
		Header hdrn = hh.getHeader();
		fNew.addHDU(hh);

		for( AttributeHandler ah: ahs.values() ) {
			String key = ah.getNameorg();
			String type = ah.getType();
			String value = ah.getValue();
			if( key.equals("XTENSION") || key.equals("BITPIX")) continue;
			else if( type == "boolean" ) {
				hdrn.addValue(key, new Boolean(value) , ah.getComment());
			} else if( type == "int" ) {
				hdrn.addValue(key, new Integer(value) , ah.getComment());
			} else if( type == "long" ) {
				hdrn.addValue(key, new Long(value) , ah.getComment());
			} else if( type == "float" ) {
				hdrn.addValue(key, new Float(value) , ah.getComment());
			} else if( type == "double" ) {
				hdrn.addValue(key, new Double(value) , ah.getComment());
			} else  {
				hdrn.addValue(key, value , ah.getComment());
			}			
			hdrn.addValue("BLANK", new Integer(-1) , "Out of Footprint pixels");
		}
		FileOutputStream d1 = new FileOutputStream(this.outFile);
		BufferedOutputStream d2 = new BufferedOutputStream(d1);
		DataOutputStream d3 = new DataOutputStream(d2);
		BufferedDataOutputStream s = new BufferedDataOutputStream(d3);
		fNew.write(s);
	}

	/**
	 * @param args
	 * @throws Exception 
	 * @throws SaadaException 
	 * @throws FatalException 
	 */
	public static void main(String[] args) throws Exception {
		ImageFootprint imageFootprint;
		imageFootprint = new ImageFootprint(
				"/data/Boule/build/out/ROOT/PN/EB2/"
			   ,"/data/Boule/build/out/ROOT/PN/EB2/");
		imageFootprint.process();
	}

}
