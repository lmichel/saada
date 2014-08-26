package saadadb.products.inference;

import java.util.Map;

import saadadb.enums.ColumnSetMode;
import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.products.setter.ColumnExpressionSetter;
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
	private Map<String, AttributeHandler> attributesList;
	private ColumnExpressionSetter[] CRPIX;
	private ColumnExpressionSetter[] CRVAL;
	private ColumnExpressionSetter[] CDELT;
	private ColumnExpressionSetter[] CTYPE;
	private ColumnExpressionSetter[] CUNIT;
	private ColumnExpressionSetter[] CD;
	private int[] NAXISi;
	private double[] matrix;
	private int NAXIS;
	boolean kwset_ok = true;
	public String detectionMessage ="";
	private WATInfos watInfos;
	/**
	 * @return
	 * @throws Exception
	 */
	public WCSModel(Map<String, AttributeHandler> attributesList) throws Exception{
		this.attributesList = attributesList;
		AttributeHandler ah;
		if( (ah = this.attributesList.get("_naxis")) != null ) {
			this.NAXIS  = Integer.parseInt(ah.getValue());
			this.initArrays();
		} else {
			this.kwset_ok = false;
			return;
		//	IgnoreException.throwNewException(SaadaException.WCS_ERROR, "No NAXIS keyword");
		}

		this.watInfos = new WATInfos(NAXIS, attributesList);
		for( int axe=0 ; axe<this.NAXIS ; axe++ ) {
			int axe_num = axe+1;
			if( (ah = this.attributesList.get("_naxis" + axe_num)) != null ) {
				this.NAXISi[axe]  = Integer.parseInt(ah.getValue());
			} else {
				kwset_ok = false;
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "No WCS keywords NAXIS" + axe_num);
			}				


			if( (ah = this.attributesList.get("_cunit" + axe_num)) != null || 
					(ah = this.attributesList.get("_tcuni" + axe_num)) != null ) {
				//this.CUNIT[axe] = new ColumnExpressionSetter(ah, ColumnSetMode.BY_WCS);
				this.CUNIT[axe] = new ColumnExpressionSetter(ah);

			} else {
				//kwset_ok = false;
				this.CUNIT[axe] = new ColumnExpressionSetter();
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "No WCS keywords CUNIT" + axe_num + " or TCUNI" + axe_num + " (look in comment later)");
			}

			if( (ah = this.attributesList.get("_ctype" + axe_num)) != null || 
					(ah = this.attributesList.get("_tctyp" +  axe_num)) != null ) {
				//this.CTYPE[axe] = new ColumnExpressionSetter(ah, ColumnSetMode.BY_WCS);
				this.CTYPE[axe] = new ColumnExpressionSetter(ah);
			} else {
				//kwset_ok = false;
				this.CTYPE[axe] = new ColumnExpressionSetter();
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "No WCS keywords CTYPE" + axe_num + " or TCTYP" + axe_num);
			}

			if( (ah = this.attributesList.get("_crval" + axe_num)) != null || 
					(ah = this.attributesList.get("_tcrvl" + axe_num)) != null ) {
				//this.CRVAL[axe] = new ColumnExpressionSetter(ah, ColumnSetMode.BY_WCS);;
				this.CRVAL[axe] = new ColumnExpressionSetter(ah);;
			} else if( this.NAXISi[axe] == 1 ){
				ColumnExpressionSetter cd = new ColumnExpressionSetter();
				cd.setByValue("0",false);
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "axe " + axe_num + " is one pixel width, take  CRVAL = 0");
				this.CRVAL[axe] = cd;
			} else{
				kwset_ok = false;
				this.CRVAL[axe] = new ColumnExpressionSetter();
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "No WCS keywords CRVAL" + axe_num  + " or TCRVL" + axe_num);
			}

			if( (ah = this.attributesList.get("_crpix" + axe_num)) != null ) {
				//this.CRPIX[axe] = new ColumnExpressionSetter(ah, ColumnSetMode.BY_WCS);;
				this.CRPIX[axe] = new ColumnExpressionSetter(ah);
			} else if( this.NAXISi[axe] == 1 ){
				ColumnExpressionSetter cd = new ColumnExpressionSetter();
				cd.setByValue("1",false);
				this.CRPIX[axe] = cd;
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "axe " + axe_num + " is one pixel width, take  CRPIX = 1");
			} else{
				kwset_ok = false;
				this.CRPIX[axe] = new ColumnExpressionSetter();
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "No WCS keywords CRPIX" + axe_num );
			}

			if( (ah = this.attributesList.get("_cdelt" + axe_num)) != null || 
					(ah = this.attributesList.get("_tcdlt" + axe_num)) != null ) {

				//this.CDELT[axe] = new ColumnExpressionSetter(ah, ColumnSetMode.BY_WCS);;
				this.CDELT[axe] = new ColumnExpressionSetter(ah);

			} else if( this.NAXISi[axe] == 1 ){
				ColumnExpressionSetter cd = new ColumnExpressionSetter();
				cd.setByValue("1",false);
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "axe " + axe_num + " is one pixel width, take  CDELT = 1");
				this.CDELT[axe] = cd;
			} else {
				kwset_ok = false;
				this.CDELT[axe] = new ColumnExpressionSetter();
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "No WCS keywords CDELT" + axe_num  + " or TCDLT" + axe_num );
			}

			for( int axe2=0 ; axe2<NAXIS ; axe2++ ) {
				int axe2_num = axe2+1;
				if( (ah = this.attributesList.get("_pc" + axe_num + "_" + axe2_num)) != null || 
						(ah = this.attributesList.get("_cd" + axe_num + "_" + axe2_num)) != null ) {
					//this.CD[(NAXIS*axe) + axe2] = new ColumnExpressionSetter(ah, ColumnSetMode.BY_WCS);
					this.CD[(NAXIS*axe) + axe2] = new ColumnExpressionSetter(ah);


				}else{
					this.CD[(NAXIS*axe) + axe2].setNotSet();
				}
			}	
			/*
			 * Override with those read in WAT
			 */
			for( int axe2=0 ; axe2<NAXIS ; axe2++ ) {
				String u = this.watInfos.getUnit(axe2);
				if( u.length() > 0 ) {
					this.CUNIT[axe2] = new ColumnExpressionSetter();
					this.CUNIT[axe2].setByValue(u, false);
					this.CUNIT[axe2].completeMessage("Read in WAT keywords");
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
	}


	/**
	 * Init arrays of WCS KWs
	 */
	private void initArrays() {

		this.CRPIX   = new ColumnExpressionSetter[NAXIS];
		this.CRVAL   = new ColumnExpressionSetter[NAXIS];
		this.CDELT   = new ColumnExpressionSetter[NAXIS];
		this.CTYPE   = new ColumnExpressionSetter[NAXIS];
		this.CUNIT   = new ColumnExpressionSetter[NAXIS];
		this.CD      = new ColumnExpressionSetter[NAXIS*NAXIS];
		this.NAXISi  = new int[NAXIS];
		this.matrix  = new double[NAXIS*NAXIS];
		for( int i=0 ; i< NAXIS ; i++ ){
			this.CRPIX[i] =  null;
			this.CRVAL[i] =  null;
			this.CDELT[i] =  null;
			this.CUNIT[i] =  null;
			this.CTYPE[i] =  null;
			this.NAXISi[i]  =  SaadaConstant.INT;
		}
		for( int i=0 ; i< (NAXIS*NAXIS) ; i++ ){
			this.CD[i] = new ColumnExpressionSetter();
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
		} else if( WCSModel.hasSetElements(this.CD)){
			if( WCSModel.hasNotSetElements(this.CD))  {
				Messenger.printMsg(Messenger.TRACE, "Not all CDi_j keywords are set");
				return false;
			}					
		} else {
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
				Messenger.printMsg(Messenger.DEBUG, "Take CDELT[i,j] keyword as projection matrix");
			for( int axe=0 ; axe<this.NAXIS ; axe++) {
				if( this.CDELT[axe].notSet()) {
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
				Messenger.printMsg(Messenger.DEBUG, "Take CD[i,j] keywords as projection matrix");
			for( int axe=0 ; axe<this.NAXIS ; axe++) {
				/*
				 * Diagonal elements are required at least
				 */
				if( this.getCD(axe, axe).notSet()) {
					IgnoreException.throwNewException(SaadaException.WCS_ERROR, "CD" + (axe+1) + "_" + (axe+1) + "not set: Can not compute the projection matrix");

				}
				/*
				 * Cross elements are taken as 0 when not defined
				 */
				for( int axe2=0 ; axe2<this.NAXIS ; axe2++) {
					if( this.getCD(axe, axe2).notSet() ) {
						this.setMatrix(axe, axe2, 0);
					} else {
						this.setMatrix(axe, axe2, this.getCD(axe, axe2));						
					}
				}
			}
		} else {
			IgnoreException.throwNewException(SaadaException.WCS_ERROR, "Neither CDELT nor CDi_j keywords defined: Can not compute the projection matrix");
		}
	}

	/**
	 * @param ligne
	 * @param col
	 * @param d
	 * @throws Exception
	 */
	private void setMatrix(int ligne, int col, ColumnExpressionSetter d) throws Exception {
		this.matrix[(this.NAXIS*ligne) + col] = Double.parseDouble(d.getValue());

	}
	/**
	 * @param ligne
	 * @param col
	 * @param v
	 * @throws Exception
	 */
	private void setMatrix(int ligne, int col, double v ) throws Exception {
		this.matrix[(this.NAXIS*ligne) + col] = v;

	}

	/**
	 * @param array
	 * @return
	 * @throws Exception
	 */
	private static boolean hasNotSetElements(ColumnExpressionSetter[] array) throws Exception {
		for( int axe=0 ; axe<array.length ; axe++) {
			if( array[axe].notSet()) {
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
	private static boolean hasSetElements(ColumnExpressionSetter[] array) throws Exception {
		for( int axe=0 ; axe<array.length ; axe++) {
			if( !array[axe].notSet() ) {
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
		return (CRPIX[axe] == null)? SaadaConstant.DOUBLE: Double.parseDouble(CRPIX[axe].getValue());
	}

	/**
	 * @return the cRVAL
	 * @throws Exception
	 */
	public double getCRVAL(int axe) throws Exception {
		return (CRVAL[axe] == null)? SaadaConstant.DOUBLE: Double.parseDouble(CRVAL[axe].getValue());
	}

	/**
	 * @return the cDELT
	 * @throws Exception
	 */
	public double getCDELT(int axe) throws Exception {
		return (CDELT[axe] == null)? SaadaConstant.DOUBLE: Double.parseDouble(CDELT[axe].getValue());
	}

	/**
	 * @return the cTYPE
	 * @throws Exception
	 */
	public String  getCTYPE(int axe) throws Exception {
		return (CTYPE[axe] == null)? null:CTYPE[axe].getValue();
	}

	/**
	 * @return the cUNIT
	 * @throws Exception
	 */
	public String getCUNIT(int axe) throws Exception {
		return (CUNIT[axe] == null)? null:CUNIT[axe].getValue();
	}

	/**
	 * @return the pC
	 * @throws Exception
	 */
	public ColumnExpressionSetter getCD(int i, int j) throws Exception {
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
	public boolean isAxeProjectedToRealWord(int axe) throws Exception {
		if( !this.CRPIX[axe].notSet() && !this.CRVAL[axe].notSet() && this.getMatrix(axe, axe) != SaadaConstant.DOUBLE) {
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
	public void projectAxeToRealWord(int axe) throws Exception {
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Set default projection parameters to axe #" + (axe+1));
		this.CRPIX[axe].setByValue("0", false);
		this.CRVAL[axe].setByValue("0", false);;
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
	public void projectAllAxesToRealWord() throws Exception {
		for( int axe=0 ; axe<this.NAXIS ; axe++) {
			if( !this.isAxeProjectedToRealWord(axe) ) {
				this.projectAxeToRealWord(axe);
			}
		}	
	}

	/**
	 * val = CRVLA + matrix*(pix - CPRIX)
	 * @param pix
	 * @return
	 * @throws Exception
	 */
	public double[] getRealWorldValue(int[] pix) throws Exception {
		if( pix.length != this.NAXIS) {
			IgnoreException.throwNewException(SaadaException.WCS_ERROR, "Attempt to read a pixel vector vith a size different from the pixel map");
			return null;
		}
		double[] retour = new double[this.NAXIS];
		for( int axe=0 ; axe<this.NAXIS ; axe++) {
			retour[axe] = Double.parseDouble(this.CRVAL[axe].getValue());
			for( int axe2=0 ; axe2<this.NAXIS ; axe2++) {
				retour[axe] += this.getMatrix(axe, axe2)*(pix[axe2]  - Double.parseDouble(this.CRPIX[axe2].getValue()));
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
		return getRealWorldValue(pix)[axe];
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
			} else {
				pix[axe2] = 0;
			}
		}
		return getRealWorldValue(pix)[axe];
	}

	/**
	 * Return the number (starting to 0) of the first axe possibly a dispersion axe
	 * @return
	 * @throws Exception
	 */
	public int getDispersionAxe() throws Exception {
		int retour;
		if( (retour = this.watInfos.getDispersionAxe()) != SaadaConstant.INT) {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "WAP keywords: dispersion axe: " + retour);
			this.detectionMessage = "Dispersion taken from WAT keywords";
			return retour;
		} else {
			for( int axe=0  ; axe< this.NAXIS ; axe ++ ) {
				if(isDispersionAxe(axe)) {
					return axe;
				}
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
		return (dispersionInCtype(dispersion_axe_num) || dispersionInCval(dispersion_axe_num));
	}

	/**
	 * Supported dispersion type are given by
	 * http://www.aanda.org/index.php?option=com_article&access=standard&Itemid=129&url=/articles/aa/full/2006/05/aa3818-05/aa3818-05.html
	 * @param dispersion_axe_num
	 * @return
	 */
	private boolean dispersionInCtype(int dispersion_axe_num){
		if( this.CTYPE[dispersion_axe_num].getValue() .equalsIgnoreCase("FREQ") ){// 	Frequency  	Hz
			if( this.CUNIT[dispersion_axe_num].getValue().length() == 0 ) {
				this.CUNIT[dispersion_axe_num].setValue("Hz");
				this.CUNIT[dispersion_axe_num].completeMessage("Take the WCS default unit Hz");
			} else {
				this.CUNIT[dispersion_axe_num].completeMessage("Unit taken from CUNIT keyword");				
			}
			this.CTYPE[dispersion_axe_num].byWcs();
			this.CUNIT[dispersion_axe_num].byWcs();
			return true;
		}
		else if( this.CTYPE[dispersion_axe_num].getValue() .equalsIgnoreCase("ENER") ){//  	Energy 		J
			if( this.CUNIT[dispersion_axe_num].getValue().length() == 0 ) {
				this.CUNIT[dispersion_axe_num].setValue("J");
				this.CUNIT[dispersion_axe_num].completeMessage("Take the WCS default unit J");
			} else {
				this.CUNIT[dispersion_axe_num].completeMessage("Unit taken from CUNIT keyword");				
			}
			this.CTYPE[dispersion_axe_num].byWcs();
			this.CUNIT[dispersion_axe_num].byWcs();
			return true;
		}
		else if( this.CTYPE[dispersion_axe_num].getValue() .equalsIgnoreCase("WAVN ") ){// 	Wavenumber 	m-1
			if( this.CUNIT[dispersion_axe_num].getValue().length() == 0 ) {
				this.CUNIT[dispersion_axe_num].setValue("1/m");
				this.CUNIT[dispersion_axe_num].completeMessage("Take the WCS default unit 1/m");
			} else {
				this.CUNIT[dispersion_axe_num].completeMessage("Unit taken from CUNIT keyword");				
			}
			this.CTYPE[dispersion_axe_num].byWcs();
			this.CUNIT[dispersion_axe_num].byWcs();
			return true;			
		}
		else if( this.CTYPE[dispersion_axe_num].getValue() .equalsIgnoreCase("VRAD") ){//  	Radio velocity 	  m/s-1
			if( this.CUNIT[dispersion_axe_num].getValue().length() == 0 ) {
				this.CUNIT[dispersion_axe_num].setValue("m/s");
				this.CUNIT[dispersion_axe_num].completeMessage("Take the WCS default unit m/s");
			} else {
				this.CUNIT[dispersion_axe_num].completeMessage("Unit taken from CUNIT keyword");				
			}
			this.CTYPE[dispersion_axe_num].byWcs();
			this.CUNIT[dispersion_axe_num].byWcs();
			return true;			
		}
		else if( this.CTYPE[dispersion_axe_num].getValue() .equalsIgnoreCase("WAVE") ){//  	Vacuum wavelength 		m
			if( this.CUNIT[dispersion_axe_num].getValue().length() == 0 ) {
				this.CUNIT[dispersion_axe_num].setValue("m");
				this.CUNIT[dispersion_axe_num].completeMessage("Take the WCS default unit m");
			} else {
				this.CUNIT[dispersion_axe_num].completeMessage("Unit taken from CUNIT keyword");				
			}
			this.CTYPE[dispersion_axe_num].byWcs();
			this.CUNIT[dispersion_axe_num].byWcs();
			return true;			
		}
		else if( this.CTYPE[dispersion_axe_num].getValue() .equalsIgnoreCase("VOPT") ){//  	Optical velocity 	  s-1
			if( this.CUNIT[dispersion_axe_num].getValue().length() == 0 ) {
				this.CUNIT[dispersion_axe_num].setValue("m");
				this.CUNIT[dispersion_axe_num].completeMessage("Take the WCS default unit m");
			} else {
				this.CUNIT[dispersion_axe_num].completeMessage("Unit taken from CUNIT keyword");				
			}
			this.CTYPE[dispersion_axe_num].byWcs();
			this.CUNIT[dispersion_axe_num].byWcs();
			return true;			
		}
		else if( this.CTYPE[dispersion_axe_num].getValue() .equalsIgnoreCase("ZOPT") ){//  	Redshift 	 	-
			if( this.CUNIT[dispersion_axe_num].getValue().length() == 0 ) {
				this.CUNIT[dispersion_axe_num].setValue("m");
				this.CUNIT[dispersion_axe_num].completeMessage("Take the WCS default unit m");
			} else {
				this.CUNIT[dispersion_axe_num].completeMessage("Unit taken from CUNIT keyword");				
			}
			this.CTYPE[dispersion_axe_num].byWcs();
			this.CUNIT[dispersion_axe_num].byWcs();
			return true;			
		}
		else if( this.CTYPE[dispersion_axe_num].getValue() .equalsIgnoreCase("AWAV") ){//  	Air wavelength  	m
			if( this.CUNIT[dispersion_axe_num].getValue().length() == 0 ) {
				this.CUNIT[dispersion_axe_num].setValue("m");
				this.CUNIT[dispersion_axe_num].completeMessage("Take the WCS default unit m");
			} else {
				this.CUNIT[dispersion_axe_num].completeMessage("Unit taken from CUNIT keyword");				
			}
			this.CTYPE[dispersion_axe_num].byWcs();
			this.CUNIT[dispersion_axe_num].byWcs();
			return true;			
		}
		else if( this.CTYPE[dispersion_axe_num].getValue() .equalsIgnoreCase("VELO") ){//  	Apparent radial velocity 	m /s-1
			if( this.CUNIT[dispersion_axe_num].getValue().length() == 0 ) {
				this.CUNIT[dispersion_axe_num].setValue("m");
				this.CUNIT[dispersion_axe_num].completeMessage("Take the WCS default unit m");
			} else {
				this.CUNIT[dispersion_axe_num].completeMessage("Unit taken from CUNIT keyword");				
			}
			this.CTYPE[dispersion_axe_num].byWcs();
			this.CUNIT[dispersion_axe_num].byWcs();
			return true;			
		}
		else if( this.CTYPE[dispersion_axe_num].getValue() .equalsIgnoreCase("BETA") ){//  	Beta factor v/c	-
			if( this.CUNIT[dispersion_axe_num].getValue().length() == 0 ) {
				this.CUNIT[dispersion_axe_num].setValue("m");
				this.CUNIT[dispersion_axe_num].completeMessage("Take the WCS default unit m");
			} else {
				this.CUNIT[dispersion_axe_num].completeMessage("Unit taken from CUNIT keyword");				
			}
			this.CTYPE[dispersion_axe_num].byWcs();
			this.CUNIT[dispersion_axe_num].byWcs();
			return true;			
		}
		/*
		 * Dispersion unit can be written in CTYPE keyword
		 */
		if( this.CTYPE[dispersion_axe_num].getValue().matches(".*(?i)(angstro).*") ) {
			Messenger.printMsg(Messenger.TRACE, "Dispersion unit written in CTYPE keyword CTYPE=\"" + this.CTYPE 
					+ "\". CTYPE=\"WAVE\" and CUNIT=\"Angstrom\" are taken"); 
			this.CUNIT[dispersion_axe_num].setValue("Angstrom");
			this.CTYPE[dispersion_axe_num].setValue("WAVE");
			this.detectionMessage =  "Dispersion on axis #" + dispersion_axe_num + " (CTYPE=" + this.CTYPE[dispersion_axe_num] + ")";
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, this.detectionMessage);
			return true;
		} else if( this.CTYPE[dispersion_axe_num].getValue().matches(".*(?i)((wavelength)|(frequency)|(energy)).*")  ) {
			this.detectionMessage =  "Dispersion on axis #" + dispersion_axe_num + " (CTYPE=" + this.CTYPE[dispersion_axe_num] + ")";
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, this.detectionMessage);
			return true;
		} else {
			this.detectionMessage = "";
			return false;
		}

	}

	/**
	 * @param dispersion_axe_num
	 * @return
	 */
	private boolean dispersionInCval(int dispersion_axe_num){
		String valComment = this.CRVAL[dispersion_axe_num].getComment();
		String deltComment = this.CDELT[dispersion_axe_num].getComment();
		if( valComment.matches(".*(?i)(angstro).*") 
				|| 	deltComment .matches(".*(?i)(angstro).*")) {
			this.CUNIT[dispersion_axe_num].setValue("Angstrom");
			this.CTYPE[dispersion_axe_num].setValue("WAVE");
			this.detectionMessage =  "Dispersion on axis #" + dispersion_axe_num + " (CRVAL=" + valComment + " or CDELT=" + deltComment + ")";
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, this.detectionMessage);
			return true;
		} else {
			this.detectionMessage = "";
			return false;
		}
	}

	/*
	 * Getters for the center of the real world coordinates 
	 */
	public ColumnExpressionSetter[] getGlonlatCenter() {
		return getCenterCoords("GLON", "GLAT");
	}
	public ColumnExpressionSetter[] getElonlatCenter() {
		return getCenterCoords("ELON", "ELAT");
	}
	public ColumnExpressionSetter[] getRadecCenter() {
		return getCenterCoords("RA", "DEC");
	}
	/**
	 * Return the real world of the projection center 
	 * @param ascPrefix prefix CTYP of the ascension axis
	 * @param decPrefix prefix CTYP of the declination axis
	 * @return
	 */
	private ColumnExpressionSetter[] getCenterCoords(String ascPrefix, String decPrefix) {
		ColumnExpressionSetter asc=new ColumnExpressionSetter(), dec=new ColumnExpressionSetter();
		for( int axe=0 ; axe<this.NAXIS ; axe++) {
			if( this.CTYPE[axe].getValue().startsWith(ascPrefix) ) {
				try {
					asc.setByWCS(this.CRVAL[axe].getValue(), false);
					asc.completeMessage("Center asc axe #" + axe);
				} catch (Exception e) {
					asc.completeMessage(e.getMessage());
				}
			}
			if( this.CTYPE[axe].getValue().startsWith(decPrefix) ) {
				try {
					dec.setByWCS(this.CRVAL[axe].getValue(), false);
					dec.completeMessage("Center dec axe #" + axe);
				} catch (Exception e) {
					dec.completeMessage(e.getMessage());
				}
			}
		}		
		return new ColumnExpressionSetter[]{ asc, dec};
	}
	/*
	 * Getters for the resultion
	 */
	public ColumnExpressionSetter getGlonlatResolution() {
		return getResolution("GLON", "GLAT");
	}
	public ColumnExpressionSetter getElonlatResolution() {
		return getResolution("ELON", "ELAT");
	}
	public ColumnExpressionSetter getRadecResolution() {
		return getResolution("RA", "DEC");
	}

	/**
	 * Return resolution computed as range/naxis
	 * The worst resolution is taken 
	 * @param ascPrefix ascPrefix prefix CTYP of the ascension axis
	 * @param decPrefix decPrefix prefix CTYP of the declination axis
	 * @return
	 */
	private ColumnExpressionSetter getResolution(String ascPrefix, String decPrefix) {
		double r1 = SaadaConstant.DOUBLE, r2 = SaadaConstant.DOUBLE;
		ColumnExpressionSetter retour = new ColumnExpressionSetter();
		for( int axe=0 ; axe<this.NAXIS ; axe++) {
			if( this.CTYPE[axe].getValue().startsWith(ascPrefix) ) {
				try {
					r1 = Math.abs((this.getMaxValue( axe) - this.getMinValue( axe))/this.NAXISi[axe]);
				} catch (Exception e) {	}
			}
			if( this.CTYPE[axe].getValue().startsWith(decPrefix) ) {
				try {
					r2 =  Math.abs((this.getMaxValue( axe) - this.getMinValue( axe))/this.NAXISi[axe]);
				} catch (Exception e) {	
					retour.completeMessage(e.getMessage());
				}
			}
		}

		if( r1 != SaadaConstant.DOUBLE && r2 != SaadaConstant.DOUBLE) {
			double r = (r1 > r2)?r1: r2;
			retour.setByWCS(String.valueOf(r), false);
			retour.setUnit("deg");
			retour.completeMessage("Range / NAXIS");
		}
		return retour;
	}

	/*
	 * Getters for the range of the real world coordinates 
	 */
	public ColumnExpressionSetter[] getRaRange() {
		return getPixelRange("RA");
	}
	public ColumnExpressionSetter[] getDecRange() {
		return getPixelRange("DEC");
	}
	public ColumnExpressionSetter[] getGlonRange() {
		return getPixelRange("GLON");
	}
	public ColumnExpressionSetter[] getGlatRange() {
		return getPixelRange("GLAT");
	}
	public ColumnExpressionSetter[] getElonRange() {
		return getPixelRange("ELON");
	}
	public ColumnExpressionSetter[] getElatRange() {
		return getPixelRange("ELAT");
	}
	/**
	 * Return the real world range value of the axis with CTYPE beginning with ctypePrefix
	 * @param ctypePrefix
	 * @return
	 */
	private ColumnExpressionSetter[] getPixelRange(String ctypePrefix){
		ColumnExpressionSetter[] retour = new ColumnExpressionSetter[]{new ColumnExpressionSetter(), new ColumnExpressionSetter()};;
		for( int axe=0 ; axe<this.NAXIS ; axe++) {
			if( this.CTYPE[axe].getValue().startsWith(ctypePrefix) ) {
				try {
					retour[0].setByWCS(String.valueOf(this.getMinValue( axe)), false);
					retour[0].completeMessage("min of WCS axis #" + axe);
					retour[1].setByWCS(String.valueOf(this.getMaxValue( axe)), false);
					retour[1].completeMessage("max of WCS axis #" + axe);
					return retour;
				} catch (Exception e) {	}
			}
		}
		return retour;
	}

}
