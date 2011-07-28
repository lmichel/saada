package saadadb.kdtree;

import java.util.TreeMap;

import saadadb.exceptions.QueryException;


/**
 * USAGE: FisrtNearestNeighbour nn = new FisrtNearestNeighbour();
 * ....
 * nn.init(collection, classes[], category);
 * nearest = nn.getCounterparts(target, -1, 0.2);
 * @author michel
 *
 * @param <E>
 */
public class SigmaFirstNearestNeighbour extends SigmaKDTree {



	/**
	 * Return the first neighbour at a distance <= dist_max
	 * The result is encapsulated in a TreeMap in order to have the same interface id KNearestNeighbour
	 * @param target
	 * @param k
	 * @param dist_max
	 * @return
	 * @throws QueryException 
	 */
	public  final  TreeMap<Double,HasKCoo> getCounterparts(final HasKCoo target,final int k, double dist_sigma_max) throws QueryException{
		TreeMap<Double,HasKCoo> retour = new TreeMap<Double,HasKCoo>();
		/*
		 * Convert the distance in sigma to sphere unit
		 */
		double dist_sunit = 2*Math.sin(dist_sigma_max/2);
		HasKCoo voisin = nn(target,dist_sunit);
		if( voisin != null ) {
			retour.put(voisin.getDistance(), voisin);
		}
		return retour;
	} 

}

