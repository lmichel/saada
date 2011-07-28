package saadadb.kdtree;

import saadadb.exceptions.QueryException;


public interface HasKCoo {
	/**
	 * @return the number of coordinates (= the dimension) of the point
	 */
	public int dim();
	/**
	 * @param i
	 * @return the i^th coordinate of the point (i start at 0)
	 */
	public double coo(int i);
	
	public void setDistance(double distance);
	public double getDistance();
	public long getOidsaada();
	public double getError() throws QueryException;
	
}
