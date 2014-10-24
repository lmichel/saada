package saadadb.resourcetest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.TableHDU;
import saadadb.database.Database;
import saadadb.util.Messenger;
import saadadb.vocabulary.RegExp;

public class EPICSpectraBiner {

	private String mask;
	private int nb_bins;
	private double mean;
	private String dirname;

	public EPICSpectraBiner(String dirname, String mask, int nb_bins, double mean) {
		this.dirname = dirname;
		this.mask = mask;
		this.nb_bins = nb_bins;
		this.mean = mean;
	}

	public void scanDir(PrintStream printStream) throws FitsException, IOException {
		File dir = new File(dirname);
		Messenger.printMsg(Messenger.TRACE, "Scan directory file <" + dirname + ">");		
		printStream.println("#Filename mask    : " + mask);
		printStream.println("#Directory Scanned: " + dirname);
		printStream.println("#Number of Bins   : " + nb_bins);
		printStream.println("#Mean value       : " + mean );
		for( String file: dir.list()) {
			String filepath = dir + Database.getSepar() + file;
			if( (new File(filepath)).isFile() && file.matches(RegExp.FITS_FILE) &&  file.matches(mask) ) {
				Messenger.printMsg(Messenger.TRACE, "Reading file <" + file + ">");
				SpectrumBiner sb = new SpectrumBiner(dirname, file, "COUNTS", nb_bins, mean);
				sb.getBins();
				sb.printRow(printStream);
			}
		}	
	}

	class SpectrumBiner {
		private int nb_bins;
		private double mean;
		private Fits fits;
		private String count_key;
		private double[] native_counts;
		private double[] bined_counts;
		private String filename;
		
		public SpectrumBiner(String filedir, String filename, String count_key, int nb_bins, double mean) throws FitsException {
			this.nb_bins = nb_bins;
			this.mean = mean;
			this.fits = new Fits(filedir + Database.getSepar() + filename);
			this.count_key = count_key;
			this.filename = filename;
		}
		
		public void getBins() throws FitsException, IOException {
			this.native_counts = new double[2];
			TableHDU tableEnumeration = (TableHDU)this.fits.getHDU(1);
			Object o =  tableEnumeration.getColumn(count_key);
			String column_class = o.getClass().getName();
			if( column_class.matches("\\[[\\w]+") ) {
				if( column_class.equals("[F") ) {
					float too[] = (float[])(o);	
					native_counts = new double[too.length];				
					for( int i=0 ; i<too.length ; i++ ) {
						native_counts[i] = (double)(too[i]);				
					}
				}
				else if( column_class.equals("[D") ) {
					native_counts = (double[])(o);					
				}
				else if( column_class.equals("[I") ) {
					int too[] = (int[])(o);					
					native_counts = new double[too.length];				
					for( int i=0 ; i<too.length ; i++ ) {
						native_counts[i] = (double)(too[i]);				
					}
				}
				else if( column_class.equals("[S") ) {
					short too[] = (short[])(o);					
					native_counts = new double[too.length];				
					for( int i=0 ; i<too.length ; i++ ) {
						native_counts[i] = (double)(too[i]);				
					}
				}
				else if( column_class.equals("[java.lang.String") ) {
					String too[] = (String[])(o);					
					native_counts = new double[too.length];				
					for( int i=0 ; i<too.length ; i++ ) {
						native_counts[i] = Double.parseDouble(too[i]);				
					}
				}
				this.doBins();
			}
		}
		
		private void doBins() {
			int bin_size = native_counts.length/this.nb_bins;
			this.bined_counts = new double[this.nb_bins];
			for( int i=0 ; i<this.nb_bins ; i++ ) {
				double sum = 0;
				for( int v=0 ; v<bin_size ;v++ ) {
					sum += this.native_counts[(i*bin_size) + v];
				}
				bined_counts[i] += sum/bin_size;	
			}
			double sum = 0;			
			for( int i=0 ; i<this.nb_bins ; i++ ) {
				sum += bined_counts[i];
			}
			double correct = this.mean / (sum/this.nb_bins);
			for( int i=0 ; i<this.nb_bins ; i++ ) {
				bined_counts[i] = correct * bined_counts[i];
			}
			sum = 0;
			for( int i=0 ; i<this.nb_bins ; i++ ) {
				sum += bined_counts[i];
			}
		}
		
		public void printRow(PrintStream printStream) {
			String row = filename + ", 0";
			for( int i=0 ; i<this.nb_bins ; i++ ) {
				row += "," + bined_counts[i];
			} 
			printStream.println(row);
		}
	}
	public static void main(String args[]) throws Exception {
		EPICSpectraBiner esb = new EPICSpectraBiner("/home/michel/Desktop", "P\\d{10}PN\\w{4}SRSPEC\\d{4}\\..*", 4096/32, 1.0);
		PrintStream ps = new PrintStream(new FileOutputStream("/home/michel/Desktop/PN.csv") ,true,"ISO-8859-1");
		esb.scanDir(ps);
		ps.close();
	}
}
