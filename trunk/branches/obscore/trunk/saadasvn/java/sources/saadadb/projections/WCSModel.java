package saadadb.projections;

import java.io.File;
import java.util.LinkedHashMap;

import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.prdconfiguration.ConfigurationSpectrum;
import saadadb.products.Spectrum;
import saadadb.util.Messenger;
import saadadb.util.SaadaConstant;

/** * @version $Id$

 * Class modeling a set of WCS keyword.
 * This class is the first stone for a future clean implementation of projections
 * Axes are referenced from 0 to NAXIS-1
 * @author michel
 *
 */
public class WCSModel {
	private LinkedHashMap<String, AttributeHandler> attributesList;
	private double[] CRPIX;
	private double[] CRVAL;
	private double[] CDELT;
	private String[] CTYPE;
	private String[] CUNIT;
	private double[] CD;
	private int[] NAXISi;
	private double[] matrix;
	private int NAXIS;
	private boolean kwset_ok = true;


	/**
	 * @return
	 * @throws Exception
	 */
	public WCSModel(LinkedHashMap<String, AttributeHandler> attributesList) throws Exception{
		this.attributesList = attributesList;
		AttributeHandler ah;
		try {
			if( (ah = this.attributesList.get("_naxis")) != null ) {
				this.NAXIS  = Integer.parseInt(ah.getValue());
				this.initArrays();
			}
			else {
				IgnoreException.throwNewException(SaadaException.WCS_ERROR, "No NAXIS keyword");
			}

			for( int axe=0 ; axe<this.NAXIS ; axe++ ) {
				int axe_num = axe+1;
				if( (ah = this.attributesList.get("_naxis" + axe_num)) != null ) {
					this.NAXISi[axe]  = Integer.parseInt(ah.getValue());
				}
				else {
					kwset_ok = false;
					if (Messenger.debug_mode)
						Messenger.printMsg(Messenger.DEBUG, "No WCS keywords NAXIS" + axe_num);
				}				


				if( (ah = this.attributesList.get("_cunit" + axe_num)) != null || 
						(ah = this.attributesList.get("_tcuni" + axe_num)) != null ) {
					this.CUNIT[axe] = ah.getValue();
				}
				else {
					kwset_ok = false;
					if (Messenger.debug_mode)
						Messenger.printMsg(Messenger.DEBUG, "No WCS keywords CUNIT" + axe_num + " or TCUNI" + axe_num);
				}

				if( (ah = this.attributesList.get("_ctype" + axe_num)) != null || 
						(ah = this.attributesList.get("_tctyp" +  axe_num)) != null ) {
					this.CTYPE[axe] = ah.getValue();
				}
				else {
					kwset_ok = false;
					if (Messenger.debug_mode)
						Messenger.printMsg(Messenger.DEBUG, "No WCS keywords CTYPE" + axe_num + " or TCTYP" + axe_num);
				}

				if( (ah = this.attributesList.get("_crval" + axe_num)) != null || 
						(ah = this.attributesList.get("_tcrvl" + axe_num)) != null ) {
					this.CRVAL[axe] = Double.parseDouble(ah.getValue());
				}else{
					kwset_ok = false;
					if (Messenger.debug_mode)
						Messenger.printMsg(Messenger.DEBUG, "No WCS keywords CRVAL" + axe_num  + " or TCRVL" + axe_num);
				}

				if( (ah = this.attributesList.get("_crpix" + axe_num)) != null ) {
					this.CRPIX[axe] = Double.parseDouble(ah.getValue());
				}else{
					kwset_ok = false;
					this.CRPIX[axe] = SaadaConstant.DOUBLE;
					if (Messenger.debug_mode)
						Messenger.printMsg(Messenger.DEBUG, "No WCS keywords CRPIX" + axe_num );
				}
				
				if( (ah = this.attributesList.get("_cdelt" + axe_num)) != null || 
						(ah = this.attributesList.get("_tcdlt" + axe_num)) != null ) {
					this.CDELT[axe] = Double.parseDouble(ah.getValue());
				}else{
					kwset_ok = false;
					this.CDELT[axe] = SaadaConstant.DOUBLE;
				}

				for( int axe2=0 ; axe2<NAXIS ; axe2++ ) {
					int axe2_num = axe2+1;
					if( (ah = this.attributesList.get("_pc" + axe_num + "_" + axe2_num)) != null || 
							(ah = this.attributesList.get("_cd" + axe_num + "_" + axe2_num)) != null ) {
						this.CD[(NAXIS*axe) + axe2] = Double.parseDouble(ah.getValue());

					}else{
						this.CD[(NAXIS*axe) + axe2] = SaadaConstant.DOUBLE;
					}
				}
			}

			if( !this.checkMatrix() ) {
				kwset_ok = false;				
			}
			try {
				this.setMatrix();
			}catch (Exception e) {
				Messenger.printMsg(Messenger.WARNING, e.getMessage());
			}
			/*
			 * Exception are caught in case of failure on parseDouble
			 * (NumberFormatException is not trapped by default)
			 */
		}catch (Exception e) {
			IgnoreException.throwNewException(SaadaException.WCS_ERROR, e);
		}
	}

	/**
	 * Init arrays of WCS KWs
	 */
	private void initArrays() {

		this.CRPIX   = new double[NAXIS];
		this.CRVAL   = new double[NAXIS];
		this.CDELT   = new double[NAXIS];
		this.CTYPE   = new String[NAXIS];
		this.CUNIT   = new String[NAXIS];
		this.CD      = new double[NAXIS*NAXIS];
		this.NAXISi  = new int[NAXIS];
		this.matrix  = new double[NAXIS*NAXIS];
		for( int i=0 ; i< NAXIS ; i++ ){
			this.CRPIX[i] =  SaadaConstant.DOUBLE;
			this.CRVAL[i] =  SaadaConstant.DOUBLE;
			this.CDELT[i] =  SaadaConstant.DOUBLE;
			this.CUNIT[i] =  "";
			this.CTYPE[i] =  "";
			this.NAXISi[i]  =  SaadaConstant.INT;
		}
		for( int i=0 ; i< (NAXIS*NAXIS) ; i++ ){
			this.CD[i]    =  SaadaConstant.DOUBLE;
			this.matrix[i] = SaadaConstant.DOUBLE;
		}

	}
	
	/**
	 * @return
	 * @throws Exception
	 */
	private boolean checkMatrix() throws Exception {
		if( WCSModel.hasSetElements(this.CDELT)) {
			if( WCSModel.hasNotSetElements(this.CDELT))  {
				Messenger.printMsg(Messenger.TRACE, "Not all CDELT keywords are set");
				return false;
			}
		}
		else if( WCSModel.hasSetElements(this.CD)){
			if( WCSModel.hasNotSetElements(this.CD))  {
				Messenger.printMsg(Messenger.TRACE, "Not all CDi_j keywords are set");
				return false;
			}					
		}
		else {
			Messenger.printMsg(Messenger.TRACE, "Neither CDELT nor CDi_j keywords are set");					
			return false;
		}
		return true;
	}
	/**
	 * Build the conversion matrix from either CDELT of CD keywords
	 * @throws Exception
	 */
	private void setMatrix() throws Exception {
		/*
		 * If one CDELT is set, we rely on a CDELT mapping
		 */
		if(hasSetElements(this.CDELT) ) {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Take CDELT keyword as projection matrix");
			for( int axe=0 ; axe<this.NAXIS ; axe++) {
				if( this.CDELT[axe] == SaadaConstant.DOUBLE) {
					IgnoreException.throwNewException(SaadaException.WCS_ERROR, "CDLET" + (axe+1) + "not set: Can not compute the projection matrix");
					
				}
				this.setMatrix(axe, axe, this.CDELT[axe]);
				/*
				 * CDELT prof: we assume the matrix to be diagonal
				 */
				for( int axe2=0 ; axe2<this.NAXIS ; axe2++) {
					if( axe != axe2 ) {
						this.setMatrix(axe, axe2, 0);
						this.setMatrix(axe2, axe, 0);
					}
				}
			}
		}
		/*
		 * If one CD is set, we rely on a CD mapping
		 */
		else if(hasSetElements(this.CD) ) {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Take CDi_j keywords as projection matrix");
			for( int axe=0 ; axe<this.NAXIS ; axe++) {
				/*
				 * Diagonal elements are required at least
				 */
				if( this.getCD(axe, axe) == SaadaConstant.DOUBLE) {
					IgnoreException.throwNewException(SaadaException.WCS_ERROR, "CD" + (axe+1) + "_" + (axe+1) + "not set: Can not compute the projection matrix");
					
				}
				/*
				 * Cross elements are taken as 0 when not defined
				 */
				for( int axe2=0 ; axe2<this.NAXIS ; axe2++) {
					if( this.getCD(axe, axe2) == SaadaConstant.DOUBLE) {
						this.setMatrix(axe, axe2, 0);
					}
					else {
						this.setMatrix(axe, axe2, this.getCD(axe, axe2));						
					}
				}
			}
		}
		else {
			IgnoreException.throwNewException(SaadaException.WCS_ERROR, "Neither CDELT nor CDi_j keywords defined: Can not compute the projection matrix");
		}
	}

	/**
	 * @param ligne
	 * @param col
	 * @param d
	 * @throws Exception
	 */
	private void setMatrix(int ligne, int col, double d) throws Exception {
		this.matrix[(this.NAXIS*ligne) + col] = d;
		
	}

	/**
	 * @param array
	 * @return
	 * @throws Exception
	 */
	private static boolean hasNotSetElements(double[] array) throws Exception {
		for( int axe=0 ; axe<array.length ; axe++) {
			if( array[axe] == SaadaConstant.DOUBLE) {
				return true;
			}
		}
		return false;
	}
	/**
	 * @param array
	 * @return
	 * @throws Exception
	 */
	private static boolean hasSetElements(double[] array) throws Exception {
		for( int axe=0 ; axe<array.length ; axe++) {
			if( array[axe] != SaadaConstant.DOUBLE) {
				return true;
			}
		}
		return false;
	}
	/**
	 * @return the cRPIX
	 * @throws Exception
	 */
	public double getCRPIX(int axe) throws Exception {
		return CRPIX[axe];
	}

	/**
	 * @return the cRVAL
	 * @throws Exception
	 */
	public double getCRVAL(int axe) throws Exception {
		return CRVAL[axe];
	}

	/**
	 * @return the cDELT
	 * @throws Exception
	 */
	public double getCDELT(int axe) throws Exception {
		return CDELT[axe];
	}

	/**
	 * @return the cTYPE
	 * @throws Exception
	 */
	public String  getCTYPE(int axe) throws Exception {
		return CTYPE[axe];
	}

	/**
	 * @return the cUNIT
	 * @throws Exception
	 */
	public String getCUNIT(int axe) throws Exception {
		return CUNIT[axe];
	}

	/**
	 * @return the pC
	 * @throws Exception
	 */
	public double getCD(int i, int j) throws Exception {
		return CD[(NAXIS*i) + j];
	}

	/**
	 * @return the nAXISi
	 * @throws Exception
	 */
	public int getNAXISi(int axe) throws Exception {
		return NAXISi[axe];
	}

	/**
	 * @return the matrix
	 * @throws Exception
	 */
	public double getMatrix(int i, int j) throws Exception {
		return matrix[(NAXIS*i) + j];
	}

	/**
	 * @return the nAXIS
	 * @throws Exception
	 */
	public int getNAXIS() {
		return NAXIS;
	}
	
	/**
	 * @return the kwset_ok
	 * @throws Exception
	 */
	public boolean isKwset_ok() {
		return kwset_ok;
	}

	/**
	 * Returns true if all parameter are available to compute a projection following that axe.
	 * @param axe
	 * @return
	 * @throws Exception
	 */
	public boolean isAxeDropedToRealWord(int axe) throws Exception {
		if( this.CRPIX[axe] != SaadaConstant.DOUBLE && this.CRVAL[axe] != SaadaConstant.DOUBLE && this.getMatrix(axe, axe) != SaadaConstant.DOUBLE) {
			return true;
		}
		else {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Axe #" + (axe+1) + " has not all projection parameters");
			return false;
		}
	}
	
	/**
	 * Set default projection parameters (1 to 1) on axe.
	 * @param axe
	 * @throws Exception
	 */
	public void dropAxeToRealWord(int axe) throws Exception {
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Set default projection parameters to axe #" + (axe+1));
		this.CRPIX[axe] = 0;
		this.CRVAL[axe] = 0;
		this.setMatrix(axe, axe, 1);
		for( int axe2=0 ; axe2<this.NAXIS ; axe2++) {
			if( axe2 != axe ) {
				this.setMatrix(axe2, axe, 0);
				this.setMatrix(axe, axe2, 0);
			}
		}
	}
	
	/**
	 * Set the default porjection on all axes not droped to real word
	 * @throws Exception
	 */
	public void dropAllAxesToRealWord() throws Exception {
		for( int axe=0 ; axe<this.NAXIS ; axe++) {
			if( !this.isAxeDropedToRealWord(axe) ) {
				this.dropAxeToRealWord(axe);
			}
		}	
	}
	
	/**
	 * val = CRVLA + matrix*(pix - CPRIX)
	 * @param pix
	 * @return
	 * @throws Exception
	 */
	public double[] getXYValue(int[] pix) throws Exception {
		if( pix.length != this.NAXIS) {
			IgnoreException.throwNewException(SaadaException.WCS_ERROR, "Attempt to read a pixel vector vith a size different from the pixel map");
			return null;
		}
		double[] retour = new double[this.NAXIS];
		for( int axe=0 ; axe<this.NAXIS ; axe++) {
			retour[axe] = this.CRVAL[axe];
			for( int axe2=0 ; axe2<this.NAXIS ; axe2++) {
				retour[axe] += this.getMatrix(axe, axe2)*(pix[axe2]  - this.CRPIX[axe2]);
			}
		}
		return retour;		
	}	
	
	public double[] getXYValue(double[] pix) throws Exception {
		if( pix.length != this.NAXIS) {
			IgnoreException.throwNewException(SaadaException.WCS_ERROR, "Attempt to read a pixel vector vith a size different from the pixel map");
			return null;
		}
		double[] retour = new double[this.NAXIS];
		for( int axe=0 ; axe<this.NAXIS ; axe++) {
			retour[axe] = this.CRVAL[axe];
			for( int axe2=0 ; axe2<this.NAXIS ; axe2++) {
				retour[axe] += this.getMatrix(axe, axe2)*(pix[axe2]  - this.CRPIX[axe2]);
			}
		}
		return retour;		
	}	
	
	/**
	 * Return the min value following axe (all coords to 0)
	 * @param axe
	 * @return
	 * @throws Exception
	 */
	public double getMinValue(int axe) throws Exception {
		int[] pix = new int[this.NAXIS];
		for( int axe2=0 ; axe2<this.NAXIS ; axe2++) {
			pix[axe2] = 0;
		}
		return getXYValue(pix)[axe];
	}
	
	/**
	 * Return the max value following axe (all coords expept axe to 0)
	 * @param axe
	 * @return
	 * @throws Exception
	 */
	public double getMaxValue(int axe) throws Exception {
		int[] pix = new int[this.NAXIS];
		for( int axe2=0 ; axe2<this.NAXIS ; axe2++) {
			if( axe2 == axe ) {
				pix[axe2] = this.NAXISi[axe2];
			}
			else {
				pix[axe2] = 0;
			}
		}
		return getXYValue(pix)[axe];
	}

	/**
	 * Return the number (starting to 0) of the first axe possibly a dispersion axe
	 * @return
	 * @throws Exception
	 */
	public int getDispersionAxe() throws Exception {
		for( int axe=0  ; axe< this.NAXIS ; axe ++ ) {
			if(isDispersionAxe(axe)) {
				return axe;
			}
		}
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "No dispersion axe found");
		return SaadaConstant.INT;
	}
	
	/**
	 * returns true if dispersion_axe_num is possibly a dispersion axe
	 * @param dispersion_axe_num
	 * @return
	 * @throws Exception
	 */
	public boolean isDispersionAxe(int dispersion_axe_num) throws Exception {
		/*
		 * Dispersion unit can be written in CTYPE keyword
		 */
		if( this.CTYPE[dispersion_axe_num].matches(".*(?i)(angstro).*") ) {
			Messenger.printMsg(Messenger.TRACE, "Wrong CTYPE=\"" + this.CTYPE 
					+ "\". CTYPE=\"WAVE\" and CUNIT=\"Angstrom\" are taken"); 
			this.CUNIT[dispersion_axe_num] = "Angstrom";
			this.CTYPE[dispersion_axe_num] = "WAVE";
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Axe #" + dispersion_axe_num + " can be a dispersion axe (CTYPE=" + this.CTYPE[dispersion_axe_num] + ")");
			return true;
		}
		else if( this.CTYPE[dispersion_axe_num].matches(".*(?i)((wavelength)|(frequency)|(energy)).*")  ) {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Axe #" + dispersion_axe_num + " can be a dispersion axe (CTYPE=" + this.CTYPE[dispersion_axe_num] + ")");
			return true;
		}
		else {
			return false;
		}
	}

	
	public static void main(String[] args)  {
		
		try {
			ArgsParser ap = new ArgsParser(args);
			Database.init(ap.getDBName());
			Spectrum sp ;
			sp = new Spectrum(new File("/home/michel/Desktop/MGC29864B.fit"), new ConfigurationSpectrum("", new ArgsParser(new String[]{"-collection=Collection0", "-spcmapping=first", "-spcunit=nm"})));
			sp.loadProductFile(new ConfigurationSpectrum("", new ArgsParser(new String[]{"-collection=Collection0", "-spcmapping=first", "-spcunit=Angstrom"})));
			Messenger.debug_mode = true;
			WCSModel wm = new WCSModel(sp.getTableAttributeHandler());
			wm.dropAllAxesToRealWord();
			double[] retour = wm.getXYValue(new int[] {0, 0});
			for( double v: retour) {
				System.out.print(v + " " );
			}
			System.out.println();
			retour = wm.getXYValue(new int[] {0, wm.getNAXISi(1)});
			for( double v: retour) {
				System.out.print(v + " " );
			}
			System.out.println();
			retour = wm.getXYValue(new int[] {wm.getNAXISi(0), 0});
			for( double v: retour) {
				System.out.print(v + " " );
			}
			System.out.println();
			retour = wm.getXYValue(new int[] {wm.getNAXISi(0), wm.getNAXISi(1)});
			for( double v: retour) {
				System.out.print(v + " " );
			}
			System.out.println();
			
			
		} catch ( Exception e) {
			e.printStackTrace();
		}
		Database.close();
	}

}
