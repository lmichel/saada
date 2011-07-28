package saadadb.kdtree;

import java.util.TreeMap;

import saadadb.exceptions.QueryException;

public class SigmaKNearestNeighbour extends SigmaKDTree{


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
		/*
		 * Convert the distance in sigma to sphere unit
		 */
		return knn(target, k, dist_sigma_max);
	} 

}

