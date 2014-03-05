package saadadb.collection.obscoremin;

import java.io.File;
import java.io.IOException;

import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.util.RegExp;
import saadadb.util.SaadaConstant;

public class TimeSerieSaada extends WCSSaada {
	public double size_alpha_csa=SaadaConstant.DOUBLE;
	public double size_delta_csa=SaadaConstant.DOUBLE;
	public String shape_csa=SaadaConstant.STRING;
    public int naxis1=SaadaConstant.INT, naxis2=SaadaConstant.INT;
		
	public TimeSerieSaada() {
		super();
	}
	
    /**
     * @return Returns the naxis1.
     */
    public int getNaxis1() {
            return naxis1;
    }

    /**
     * @param naxis1 The naxis1 to set.
     */
    public void setNaxis1(int naxis1) {
            this.naxis1 = naxis1;
    }

    /**
     * @return Returns the naxis2.
     */
    public int getNaxis2() {
            return naxis2;
    }

    /**
     * @param naxis2 The naxis2 to set.
     */
    public void setNaxis2(int naxis2) {
            this.naxis2 = naxis2;
    }

    public void setSize_alpha_csa(double size)
	{
		this.size_alpha_csa=size;
		
	}
	public double getSize_alpha_csa()
	{
		return this.size_alpha_csa;
	}
	
	public void setSize_delta_csa(double size)
	{
		this.size_delta_csa=size;
	}
	
	public double getSize_delta_csa()
	{
		return this.size_delta_csa;
	}
	
	public void setShape_csa(String shape)
	{
		this.shape_csa=shape;
	}
	
	public String getShape_csa()
	{
		return this.shape_csa;
	}
	
	/**
	 * @param filename
	 * @return
	 */
	public String getMimeType(String filename) {
		if( filename == null ) {
			return "";
		}
		else if( filename.matches(RegExp.FITS_FILE)) {
			return "image/fits";						
		}
		else if( filename.matches(RegExp.VOTABLE_FILE)) {
			return "application/x-votable+xml";						
		}
		else {
			return "text/html";												
		}
	}	

	/* (non-Javadoc)
	 * @see saadadb.collection.SaadaInstance#setVignetteFile()
	 */
	public void setVignetteFile() throws FatalException, IOException, SaadaException{
		this.vignetteFile = new VignetteFile(Database.getRepository() 
				+ File.separator + this.getCollection().getName() 
				+ File.separator + "IMAGE" + File.separator + "JPEG" 
				, new File(this.getRepositoryPath()).getName() );
	}
	
	public void calculCoordWCS()
	{
		
		/*
		 Point2D[] pixel=new Point2D[2];
		 pixel[0] =new Point2D(0.0,0.0);
		 
		 pixel[1] =new Point2D(this.getSize_alpha_csa(),this.getSize_delta_csa());
		 Point2D[] point=this.getPointWCS(pixel);
		 ra1=point[0].getX();dec1=point[0].getY();
		 ra2=point[1].getX();dec2=point[1].getY();
		 */
	}
	
		

}

