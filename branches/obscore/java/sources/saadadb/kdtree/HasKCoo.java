package saadadb.kdtree;

import saadadb.exceptions.QueryException;


public interface HasKCoo {
	/** * @version $Id: HasKCoo.java 118 2012-01-06 14:33:51Z laurent.mistahl $

	 * @return the number of coordinates (= the dimension) of the point
	 */
	public int dim();
	/** * @version $Id: HasKCoo.java 118 2012-01-06 14:33:51Z laurent.mistahl $

	 * @param i
	 * @return the i^th coordinate of the point (i start at 0)
	 */
	public double coo(int i);
	
	public void setDistance(double distance);
	public double getDistance();
	public long getOidsaada();
	public double getError() throws QueryException;
	
}
