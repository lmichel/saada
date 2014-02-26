package saadadb.kdtree;

import java.sql.ResultSet;
import java.sql.SQLException;

import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.util.SaadaConstant;

/**
 * @author laurentmichel
 * * @version $Id: SigmaCounterPart.java 118 2012-01-06 14:33:51Z laurent.mistahl $

 */
public class SigmaCounterPart extends CounterPart {
	protected double error;
	public SigmaCounterPart(ResultSet rs) throws SQLException{
		super(rs);
		this.error = rs.getDouble("error_maj_csa");
		if( this.error == SaadaConstant.DOUBLE) {
			this.error = 0.0;
		}
	}
	
	public double getError() throws QueryException {
		if( this.error < 0 ) {
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "Can not handle error < 0");
		}
		else if( this.error ==  SaadaConstant.DOUBLE) {
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "Error not set");			
		}
		return this.error;
	}
	
	public String toString(){
		return "coord: [" + this.pos_x_csa + ", " + this.pos_y_csa + ", " + this.pos_z_csa + ", " + this.error + "] oid = " + Long.toHexString(this.oidsaada);
	}
	/* (non-Javadoc)
	 * @see saadadb.kdtree.CounterPart#setDistance(double)
	 */
	public void setDistance(double distance) {
		/*
		 * Distance in sigma
		 */
		this.distance = distance;		
	}

}
