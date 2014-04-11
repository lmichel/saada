package saadadb.products.inference;

import java.util.Map;

import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.products.ColumnSetMode;
import saadadb.products.ColumnSetter;
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
	private ColumnSetter[] CRPIX;
	private ColumnSetter[] CRVAL;
	private ColumnSetter[] CDELT;
	private ColumnSetter[] CTYPE;
	private ColumnSetter[] CUNIT;
	private ColumnSetter[] CD;
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
			IgnoreException.throwNewException(SaadaException.WCS_ERROR, "No NAXIS keyword");
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
				this.CUNIT[axe] = new ColumnSetter(ah, ColumnSetMode.BY_KEYWORD);
			} else {
				//kwset_ok = false;
				this.CUNIT[axe] = new ColumnSetter();
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "No WCS keywords CUNIT" + axe_num + " or TCUNI" + axe_num + " (look in comment later)");
			}

			if( (ah = this.attributesList.get("_ctype" + axe_num)) != null || 
					(ah = this.attributesList.get("_tctyp" +  axe_num)) != null ) {
				this.CTYPE[axe] = new ColumnSetter(ah, ColumnSetMode.BY_KEYWORD);;
			} else {
				//kwset_ok = false;
				this.CTYPE[axe] = new ColumnSetter();
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "No WCS keywords CTYPE" + axe_num + " or TCTYP" + axe_num);
			}

			if( (ah = this.attributesList.get("_crval" + axe_num)) != null || 
					(ah = this.attributesList.get("_tcrvl" + axe_num)) != null ) {
				this.CRVAL[axe] = new ColumnSetter(ah, ColumnSetMode.BY_KEYWORD);;
			}else if( this.NAXISi[axe] == 1 ){
				ColumnSetter cd = new ColumnSetter();
				cd.setByValue("0",false);
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "axe " + axe_num + " is one pixel width, take  CRVAL = 0");
				this.CRVAL[axe] = cd;
			} else{
				kwset_ok = false;
				this.CRVAL[axe] = new ColumnSetter();
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "No WCS keywords CRVAL" + axe_num  + " or TCRVL" + axe_num);
			}

			if( (ah = this.attributesList.get("_crpix" + axe_num)) != null ) {
				this.CRPIX[axe] = new ColumnSetter(ah, ColumnSetMode.BY_KEYWORD);;
			}else if( this.NAXISi[axe] == 1 ){
				ColumnSetter cd = new ColumnSetter();
				cd.setByValue("1",false);
				this.CRPIX[axe] = cd;
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "axe " + axe_num + " is one pixel width, take  CRPIX = 1");
			} else{
				kwset_ok = false;
				this.CRPIX[axe] = new ColumnSetter();
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "No WCS keywords CRPIX" + axe_num );
			}

			if( (ah = this.attributesList.get("_cdelt" + axe_num)) != null || 
					(ah = this.attributesList.get("_tcdlt" + axe_num)) != null ) {
				this.CDELT[axe] = new ColumnSetter(ah, ColumnSetMode.BY_KEYWORD);;
			} else if( this.NAXISi[axe] == 1 ){
				ColumnSetter cd = new ColumnSetter();
				cd.setByValue("1",false);
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "axe " + axe_num + " is one pixel width, take  CDELT = 1");
				this.CDELT[axe] = cd;
			} else {
				kwset_ok = false;
				this.CDELT[axe] = new ColumnSetter();
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "No WCS keywords CDELT" + axe_num  + " or TCDLT" + axe_num );
			}

			for( int axe2=0 ; axe2<NAXIS ; axe2++ ) {
				int axe2_num = axe2+1;
				if( (ah = this.attributesList.get("_pc" + axe_num + "_" + axe2_num)) != null || 
						(ah = this.attributesList.get("_cd" + axe_num + "_" + axe2_num)) != null ) {
					this.CD[(NAXIS*axe) + axe2] = new ColumnSetter(ah, ColumnSetMode.BY_KEYWORD);

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
					this.CUNIT[axe2] = new ColumnSetter();
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

		this.CRPIX   = new ColumnSetter[NAXIS];
		this.CRVAL   = new ColumnSetter[NAXIS];
		this.CDELT   = new ColumnSetter[NAXIS];
		this.CTYPE   = new ColumnSetter[NAXIS];
		this.CUNIT   = new ColumnSetter[NAXIS];
		this.CD      = new ColumnSetter[NAXIS*NAXIS];
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
			this.CD[i] = new ColumnSetter();
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
	private void setMatrix(int ligne, int col, ColumnSetter d) throws Exception {
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
	private static boolean hasNotSetElements(ColumnSetter[] array) throws Exception {
		for( int axe=0 ; axe<array.length ; axe++) {
			System.out.println("@@@@@@@@@@@ " + axe + "  " + array[axe]);
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
	private static boolean hasSetElements(ColumnSetter[] array) throws Exception {
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
	public ColumnSetter getCD(int i, int j) throws Exception {
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
		if( (retour = this.watInfos.getDispersionAxes()) != SaadaConstant.INT) {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "WAP keywords: dispersion axe: " + retour);
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
	 * @param dispersion_axe_num
	 * @return
	 */
	private boolean dispersionInCtype(int dispersion_axe_num){
		/*
		 * Dispersion unit can be written in CTYPE keyword
		 */
		if( this.CTYPE[dispersion_axe_num].getValue().matches(".*(?i)(angstro).*") ) {
			Messenger.printMsg(Messenger.TRACE, "Dispersion unit written in CTYPE keyword CTYPE=\"" + this.CTYPE 
					+ "\". CTYPE=\"WAVE\" and CUNIT=\"Angstrom\" are taken"); 
			this.CUNIT[dispersion_axe_num].setValue("Angstrom");
			this.CTYPE[dispersion_axe_num].setValue("WAVE");
			this.detectionMessage =  "Axe #" + dispersion_axe_num + " can be a dispersion axe (CTYPE=" + this.CTYPE[dispersion_axe_num] + ")";
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, this.detectionMessage);
			return true;
		} else if( this.CTYPE[dispersion_axe_num].getValue().matches(".*(?i)((wavelength)|(frequency)|(energy)).*")  ) {
			this.detectionMessage =  "Axe #" + dispersion_axe_num + " can be a dispersion axe (CTYPE=" + this.CTYPE[dispersion_axe_num] + ")";
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
			this.detectionMessage =  "Axe #" + dispersion_axe_num + " can be a dispersion axe (CRVAL=" + valComment + " or CDELT=" + deltComment + ")";
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, this.detectionMessage);
			return true;
		} else {
			this.detectionMessage = "";
			return false;
		}
	}
	
	/**
	 * @return
	 */
	public double getCenterRa() {
		double retour = SaadaConstant.DOUBLE;
		for( int axe=0 ; axe<this.NAXIS ; axe++) {
			if( this.CTYPE[axe].getValue().startsWith("RA") ) {
				try {
					retour = Double.parseDouble(this.CRVAL[axe].getValue());
					break;
				} catch (Exception e) {
				}
			}
		}
		return retour;

	}
	
	/**
	 * @return
	 */
	public double getCenterDec() {
		double retour = SaadaConstant.DOUBLE;
		for( int axe=0 ; axe<this.NAXIS ; axe++) {
			if( this.CTYPE[axe].getValue().startsWith("DEC") ) {
				try {
					retour = Double.parseDouble(this.CRVAL[axe].getValue());
					break;
				} catch (Exception e) {
				}
			}
		}
		return retour;

	}

}
