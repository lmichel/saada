package saadadb.kdtree;

import java.util.TreeMap;

import saadadb.exceptions.QueryException;

/**
 * USAGE: FisrtNearestNeighbour nn = new FisrtNearestNeighbour();
 * ....
 * nn.init(collection, classes[], category);
 * nearest = nn.getCounterparts(target, -1, 0.2);
 * @author michel
 * * @version $Id: FirstNearestNeighbour.java 118 2012-01-06 14:33:51Z laurent.mistahl $

 * @param <E>
 */
public class FirstNearestNeighbour<E extends HasKCoo> extends KDTree {

	public FirstNearestNeighbour() {
		super();
	}

	/**
	 * Return the first neighbour at a distance <= dist_max
	 * The result is encapsulated in a TreeMap in order to have the same interface id KNearestNeighbour
	 * @param target
	 * @param k
	 * @param dist_max
	 * @return
	 * @throws QueryException 
	 */
	public  final  TreeMap<Double,E> getCounterparts(final HasKCoo target,final int k, double dist_ang_max) throws QueryException{
		TreeMap<Double,E> retour = new TreeMap<Double,E>();
		/*
		 * Convert the distance from radians to sphere unit
		 */
		double dist_sunit = 2*Math.sin(dist_ang_max/2);
		E voisin = (E) nn(target,dist_sunit);
		if( voisin != null ) {
			retour.put(voisin.getDistance(), voisin);
		}
		return retour;
	}

}
