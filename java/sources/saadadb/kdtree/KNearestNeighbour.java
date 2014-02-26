package saadadb.kdtree;

import java.util.TreeMap;

import saadadb.exceptions.QueryException;

/** * @version $Id: KNearestNeighbour.java 118 2012-01-06 14:33:51Z laurent.mistahl $

 * USAGE: KNearestNeighbour nn = new KNearestNeighbour();
 * ....
 * nn.init(collection, classes[], category);
 * nearest = nn.getCounterparts(target, 5, 0.2);
 * @author michel
 *
 * @param <E>
 */
public class KNearestNeighbour<E extends HasKCoo> extends KDTree {

	public KNearestNeighbour() {
		super();
	}

	/* (non-Javadoc)
	 * @see saadadb.kdtree.KDTree#getCounterparts(saadadb.kdtree.HasKCoo, int, double)
	 */
	public  final  TreeMap<Double,E> getCounterparts(final HasKCoo target,final int k, double dist_ang_max) throws QueryException{
		/*
		 * Convert the distance from radians to sphere unit
		 */
		double dist_sunit = 2*Math.sin(dist_ang_max/2);
		return knn(target, k, dist_sunit);
	}

}
