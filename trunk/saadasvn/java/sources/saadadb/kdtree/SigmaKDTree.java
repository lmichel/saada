package saadadb.kdtree;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeMap;

import saadadb.command.ManageRelation;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.sqltable.SQLQuery;
import saadadb.util.Messenger;

public class SigmaKDTree extends KDTree<HasKCoo> {
	private double err_max = 0;
	
	/* (non-Javadoc)
	 * @see saadadb.kdtree.KDTree#init(java.lang.String, java.lang.String[], int)
	 * * @version $Id$
/
	@Override
	public void init(String collection, String[] classes, int category ) throws SaadaException{
		super.init(collection, classes, category);
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Look at error max on primary collection");
		
		for( HasKCoo hkc: points) {
			if( hkc.getError()  > err_max) {
				err_max = hkc.getError();
			}
		}
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Error max = " + err_max + " deg");
	}
	/**
	 * @param col_table
	 * @param class_select
	 * @return
	 * @throws Exception
	 */
	@Override
	protected ArrayList<HasKCoo> getKCooList(String col_table, String class_select) throws Exception{
		String query = "SELECT pos_x_csa, pos_y_csa, pos_z_csa, error_maj_csa, oidsaada \nFROM "
			+ col_table
			+ " \nWHERE pos_x_csa is not null AND pos_y_csa is not null AND pos_z_csa is not null AND error_maj_csa is not null " + class_select;
		
		SQLQuery squery = new SQLQuery();
		ResultSet rs = squery.run(query);
		ArrayList<HasKCoo> kcooas = new ArrayList<HasKCoo>();
		while( rs.next() ) {
			kcooas.add(new SigmaCounterPart(rs));
		}
		squery.close();
		return kcooas;
	}

	/**
	 * Returns the nearest elements from the target that contains the table "kdtree"
	 * WARNING: - the table kdtree must have been sorted by the "create" function!!
	 *          - the dim of target must be the same as the dim of the kdtree!
	 *          --> If not you will obtain a random result, without error or warning!
	 * @param <E>
	 * @param <T> an Object of your class implementing the HasKCoo interface
	 * @param dist_max: distance max for counterparts (in sphere radius unit)
	 * @param target: the target
	 * @return the nearest elements of the target that contains the table "kdtree"
	 * @throws QueryException 
	 */
	@Override
	public final HasKCoo nn(final HasKCoo target, final double sigma_max) throws QueryException{
		double err_max = Math.sqrt((target.getError()*target.getError())+(this.err_max*this.err_max));
		//System.out.println("@@@@@@@@@@@@@ err_max = "+err_max + " " + this.err_max);
		TreeMap<Double, HasKCoo> kds = super.inKDSphere(target,sigma_max*err_max);
		double dist_sig_min = Double.MAX_VALUE;
		HasKCoo res = null;
		//System.out.println("@@@@@@@@@@@@@@@ Mbr Neigh" + kds.size() + " " + sigma_max);
		int cpt = 0;
		for(Entry<Double,HasKCoo> e:kds.entrySet()){
			HasKCoo src = e.getValue();
			double d_sigma = e.getKey()/Math.sqrt((target.getError()*target.getError())+(src.getError()*src.getError()));
//			System.out.println("target " + target);
//			System.out.println("srfc " + src);
//			System.out.println("@@@@@@@@@@@@@@@ " + (e.getKey()*3600) + " " + d_sigma + " " + dist_sig_min);
//			if( cpt++ > 20 ) System.exit(1);
			if(d_sigma<sigma_max && d_sigma<dist_sig_min){
				dist_sig_min = d_sigma;
				res = src;
			}
		}
		return res;
	}
	
	/**
	 * Returns the k nearest elements from the target that contains the table "kdtree"
	 * sorted according to their distance to the target
	 * WARNING: - the table kdtree must have been sorted by the "create" function!!
	 *          - the dim of target must be the same as the dim of the kdtree!
	 *          --> If not you will obtain a random result, without error or warning!
	 * @param <T> a class implementing the HasKCoo interface
	 * @param target: the target
	 * @param k: the number of nearest neighbours you are looking for
	 * @param dist_max: distance max for counterparts (in sphere radius unit)
	 * @return the k nearest elements from the target that contains the table "kdtree"
	 * sorted according to their distance to the target
	 * @throws QueryException 
	 */
	@Override
	public   TreeMap<Double,HasKCoo> knn(final HasKCoo target,final int k, double sigma_max) throws QueryException{
		double err_max = Math.sqrt((target.getError()*target.getError())+(this.err_max*this.err_max));
		double dist_sig_min = Double.MAX_VALUE;
//		System.out.println("errmax " + err_max + " " + sigma_max);
		TreeMap<Double, HasKCoo> kds = super.inKDSphere(target,2*Math.sin(sigma_max*Math.toRadians(err_max)*0.5));
		
		TreeMap<Double,HasKCoo> res = new TreeMap<Double, HasKCoo>();
		//System.out.println("@@@@@@@@@@@@@@@ Mbr Neigh" + kds.size() + " " + sigma_max);
		int cpt = 0;
		for(Entry<Double,HasKCoo> e:kds.entrySet()){
			HasKCoo src = e.getValue();
			double d_rad = 2*Math.asin(e.getKey()/2);	
			double d_sigma = Math.toDegrees(d_rad)/Math.sqrt((target.getError()*target.getError())+(src.getError()*src.getError()));
//			System.out.println("----------------------------------");
//			System.out.println("target " + target);
//			System.out.println("srfc " + src);
//			System.out.println("@@@@@@@@@@@@@@@ " + Math.toDegrees(2*Math.asin(0.5*e.getKey()))*3600 + " " + d_sigma + " " + dist_sig_min + " " + sigma_max + " " + target.getError() + " " + src.getError());
//			if( cpt++ > 200 ) System.exit(1);
//			//if( (e.getKey()*3600) > 30) System.exit(1);
			if(d_sigma<sigma_max && (d_sigma<dist_sig_min || res.size()<k)){
//				try {
//					Position p1 = (Position) Database.getCache().getObject(target.getOidsaada());
//					Position p2 = (Position) Database.getCache().getObject(src.getOidsaada());
//					Coo c = new Coo(p1.getPos_ra_csa(), p1.getPos_dec_csa());
//					System.out.println("@@@@@ Distance arcmin " + 60*c.distance(new Coo(p2.getPos_ra_csa(), p2.getPos_dec_csa())));
//				} catch (FatalException e1) {
//					e1.printStackTrace();
//				}
				HasKCoo previous = src;
				previous.setDistance(d_sigma);
				while((previous=res.put(d_sigma,previous))!=null){  d_sigma += DELTA_MIN; }
				if(res.size()>k) res.remove(res.lastKey());
				dist_sig_min = res.lastKey();
			}
		}
		return res;
	}
	
	/**
	 * @param rs
	 * @return
	 * @throws SQLException 
	 */
	@Override
	public CounterPart getCounterpart(ResultSet rs) throws SQLException {
		return new SigmaCounterPart(rs);
	}
	
	public static void main(String[] args ) {
		ManageRelation.main(new String[]{"-populate=WFICounterparts",  "-debug",  "BENCH2_0_PSQL"});
	}
}
