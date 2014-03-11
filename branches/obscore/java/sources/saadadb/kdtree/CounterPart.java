package saadadb.kdtree;

import java.sql.ResultSet;
import java.sql.SQLException;

import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.util.SaadaConstant;


/** * @version $Id$

 * @author laurentmichel
 *
 */
public class CounterPart implements HasKCoo {
	protected double pos_x_csa;
	protected double pos_y_csa;
	protected double pos_z_csa;
	protected long oidsaada;
	protected double distance;
	
	public CounterPart(ResultSet rs) throws SQLException{
		this.pos_x_csa = rs.getDouble("pos_x_csa");
		this.pos_y_csa = rs.getDouble("pos_y_csa");
		this.pos_z_csa = rs.getDouble("pos_z_csa");
		this.oidsaada =  rs.getLong("oidsaada");
	}
	
	public CounterPart(double[] coo, long oid) throws SQLException{
		this.pos_x_csa = coo[0];
		this.pos_y_csa = coo[1];
		this.pos_z_csa = coo[2];
		this.oidsaada =  oid;
	}
	
	public final double coo(int i) {
		if( i == 0 ) {
			return this.pos_x_csa;
		}
		if( i == 1 ) {
			return this.pos_y_csa;
		}
		if( i == 2 ) {
			return this.pos_z_csa;
		}
		else {
			return SaadaConstant.DOUBLE;
		}
	}

	public final int dim() {
		return 3;
	}
	
	public String toString(){
		return "coord: [" + this.pos_x_csa + ", " + this.pos_y_csa + ", " + this.pos_z_csa + "] oid = " + Long.toHexString(this.oidsaada);
	}

	public void setDistance(double distance) {
		/*
		 * Distance is converted from sphere unit to radians
		 */
		this.distance = 2*Math.asin(distance/2);		
	}

	public double getDistance() {
		return this.distance;
	}

	/**
	 * @return Returns the oidsaada.
	 */
	public long getOidsaada() {
		return oidsaada;
	}

	/**
	 * @return Returns the pos_x_csa.
	 */
	public double getPos_x_csa() {
		return pos_x_csa;
	}

	/**
	 * @return Returns the pos_y_csa.
	 */
	public double getPos_y_csa() {
		return pos_y_csa;
	}

	/**
	 * @return Returns the pos_z_csa.
	 */
	public double getPos_z_csa() {
		return pos_z_csa;
	}

	public double getError() throws QueryException {
		QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "Error not managed by Counterpart (Use SigmaCounterPart)");
		return SaadaConstant.DOUBLE;
	}
}
