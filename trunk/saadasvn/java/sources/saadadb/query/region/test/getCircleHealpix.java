package saadadb.query.region.test;



import healpix.core.HealpixIndex;
import healpix.core.base.set.LongRangeSet;
import healpix.tools.SpatialVector;

public class getCircleHealpix {

	public static void main (String [] args) throws Exception{
		int nside = (int) Math.pow(2,Integer.parseInt(args[0]));
		double rayon=(Math.toRadians(Double.parseDouble(args[1])));
		System.out.println("Rayon : "+rayon);
		SpatialVector sv = new SpatialVector(0,0);

		HealpixIndex hpx = new HealpixIndex(nside);

		LongRangeSet lrs= hpx.queryDisc(sv,rayon,false);

		long[] array = lrs.toArray();

		int i=0;
		for (i=0;i<array.length;i++) {
			//System.out.println(array[i]);
		}
		System.out.println(array.length);
	}

	public static long[] execute (String [] args) throws Exception{

		int nside = (int) Math.pow(2,Integer.parseInt(args[0]));
		double ray=(Double.parseDouble(args[1])/60);
		double rayon=(Math.toRadians(ray));
		
		SpatialVector sv = new SpatialVector(0,0);
		System.out.println("Rayon : "+rayon);
		HealpixIndex hpx = new HealpixIndex(nside);

		LongRangeSet lrs= hpx.queryDisc(sv,rayon,true);

		long[] array = lrs.toArray();
		System.out.println("Nombre de pixels etudies : "+array.length);

		return array;
	}
}