package saadadb.collection.obscoremin;

import java.io.File;
import java.io.IOException;

import saadadb.collection.Category;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.util.SaadaConstant;

public class ImageSaada extends WCSSaada {
	/*
	 * Public fields are persistent
	 */
	/** Image width in degree */
	public double size_alpha_csa = SaadaConstant.DOUBLE;
	/** Image height in degree */
	public double size_delta_csa = SaadaConstant.DOUBLE;
	
    public int naxis1=SaadaConstant.INT;
    public int naxis2=SaadaConstant.INT;
		
	/* (non-Javadoc)
	 * @see saadadb.collection.obscoremin.SaadaInstance#setVignetteFile()
	 */
	@Override
	public void setVignetteFile() throws FatalException, IOException, SaadaException{		
		this.vignetteFile = new VignetteFile(Database.getRepository() 
				+ File.separator + this.getCollection().getName() 
				+ File.separator + Category.explain(this.getCategory()) 
				+ File.separator + "JPEG" 
				, new File(this.getRepository_location()).getName() );
	}

}

