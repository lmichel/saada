package saadadb.kdtree;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import saadadb.collection.Category;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.MetaClass;
import saadadb.sqltable.SQLQuery;
import saadadb.util.Messenger;

/** * @version $Id$

 * The KDTree class contains methods to:
 *  - create a KDTree (create)
 *  - check if a table is a KDTree or not (check)
 *  - perform a nearest neighbour (nn) query on a KDTree
 *  - perform a k nearest neighbours (knn) query on a KDTree
 *  - retrieve all points in a k dimension sphere around a points (inKDSphere)
 * 
 * Although this KDTree has been built to use as few memory as possible,
 * performances are far from being poor (for a Java code using the Arrays.sort function)
 * and are possibly better or as good as other algorihtms (using the same sorting method).
 * We have choose to use a stack instead of recursivity to have more fun ;o)
 * 
 * The principle:
 *  - the KDTree is not other than a table sorted in a particular way (the pivot choosen is the median point)
 *  - the create function don't create anything: it only sort the argument table!
 *  - this sorted table is a KDTree: magic!
 *  - the other functions (nn,knn,inKDSphere) assume the table you give in argument is a KDTree,
 *   i.e. has been sorted by the create function
 * 
 * To be aware of:
 *   In knn and inKDSphere functions, two returned elements can't be exactly at the same distance
 *   (because of the convenient use of a java Map).
 *   If two ellements are found at the same distance, one of them has its distance 
 *   incremented by DELTA_MIN (default value:1e10-15).
 * 
 * If you are looking for principles of KDTrees, you can read the following document:
 * www.autonlab.org/papers/kdtree.ps
 * 
 * @author F.-X. Pineau
 * @param <E>
 * @since 31/03/2008
 *
 */
public class KDTree<E extends HasKCoo> {
	public static final double DELTA_MIN = 0.00000000001 ;
	
	
	protected  E [] points;
	protected int dim;


	public int getPointSize() {
		if( this.points != null ) {
			return this.points.length;
		}
		return 0;
	}
	
	/**
	 * @param points
	 * @param dim
	 */
	public KDTree(E [] points, int dim){
		this.points = points;
		this.dim = dim;
		this.create();
	}
	/**
	 * @param points
	 * @param dim
	 */
	public KDTree(){
	}

	/**
	 * Build the KDTree from all rows of the collection/category
	 * category must be ENTRY, SPECTRUM or IMAGE
	 * @param collection
	 * @param category
	 * @throws SQLException
	 * @throws SaadaException 
	 */
	public KDTree(String collection, int category) throws SQLException, SaadaException{
		init(collection, new String[0],category);
	}
	
	/**
	 * @param collection
	 * @param classes
	 * @throws SQLException
	 * @throws SaadaException 
	 */
	public KDTree(String collection, String[] classes, int category ) throws SQLException, SaadaException{
		init(collection, classes,category);
	}
	/**
	 * @param collection
	 * @param classes
	 * @throws SaadaException 
	 */
	public void init(String collection, String[] classes, int category ) throws SaadaException{
		String class_select = "";
		if( classes.length > 0 && !classes[0].equals("*" )) {
			for( String classe: classes ) {
				MetaClass mc;
				try {
					mc = Database.getCachemeta().getClass(classe);
				} catch (SaadaException e) {
					throw new IllegalArgumentException("Class <" + classe + "> Does not exist");
				}
				if( !mc.getCollection_name().equals(collection) ) {
					throw new IllegalArgumentException("Class <" + classe + "> not from collection <" + collection + ">");
				}
				if( mc.getCategory() != category) {
					throw new IllegalArgumentException("Class <" + classe + "> not from category <" + Category.explain(category) + ">");
				}
				if( class_select.length() > 0 ) {
					class_select += " OR ";
				}
				class_select += " ((oidsaada>>32) & 65535) = " + mc.getId();
			}
			class_select = " AND (" + class_select + ") ";
		}
		String scat = null;
		try {
			if( !Database.getCachemeta().collectionExists(collection) 
					|| (category != Category.IMAGE && category != Category.SPECTRUM && category != Category.ENTRY)  ) {
				throw new IllegalArgumentException("Bad collection (not exist) or category (IMAGE, SPECTRUM or IMAGE)");
			}
			scat = collection + "_" + Category.explain(category).toLowerCase();
		} catch (SaadaException e) {
			throw new IllegalArgumentException("Bad collection or category");
		}
		try {
//			String query = "SELECT pos_x_csa, pos_y_csa, pos_z_csa, oidsaada \nFROM "
//				+ scat
//				+ " \nWHERE pos_x_csa is not null AND pos_y_csa is not null AND pos_z_csa is not null " + class_select;
//			
//			ResultSet rs = squery.run(query);
//			ArrayList<HasKCoo> kcooas = new ArrayList<HasKCoo>();
//			while( rs.next() ) {
//				kcooas.add(new CounterPart(rs));
//			}
			ArrayList<HasKCoo> kcooas = getKCooList(scat, class_select);
			this.points =  (E[]) kcooas.toArray(new HasKCoo[0]);
			this.dim = 3;
			this.create();
		} catch(Exception e) {
			Messenger.printStackTrace(e);
			FatalException.throwNewException(SaadaException.DB_ERROR, e);
		}
	}
	
	/**
	 * Them job done here can be do differently in some sub classes
	 * @param col_table
	 * @param class_select
	 * @return
	 * @throws Exception
	 */
	protected ArrayList<HasKCoo> getKCooList(String col_table, String class_select) throws Exception{
		String query = "SELECT pos_x_csa, pos_y_csa, pos_z_csa, oidsaada \nFROM "
			+ col_table
			+ " \nWHERE pos_x_csa is not null AND pos_y_csa is not null AND pos_z_csa is not null " + class_select;
		
		SQLQuery squery = new SQLQuery();
		ResultSet rs = squery.run(query);
		ArrayList<HasKCoo> kcooas = new ArrayList<HasKCoo>();
		while( rs.next() ) {
			kcooas.add(new CounterPart(rs));
		}
		squery.close();
		return kcooas;
	}
	/**
	 * Sort the points table following a KDTree scheme:
	 *  - sort the whole table according to the first dimension
	 *  - choose a pivot (the median point)
	 *  - sort the 2 sub tables (at the left and the right of the pivot) according to the second dimension
	 *  - ...
	 *  If the number of point is peer, the larger sub table is the left sub table
	 *  If the dimension of the table is less than or equal to 5, it is useless
	 *  to sort sub tables (dimension of sub tables <= 2)
	 *  If the dimension of the table is less than or equal to 6, it is useless
	 *  to sort the right sub table (dim<=2)
	 * @param points: a table of objet implementing the HasKCoo interface
	 * @param dim: the dimension of the points (= the K of KDTree)
	 */
	public final void create(){
		if(points==null || points.length<3) return;
		final KCooComparatorManager kCooMan = new KCooComparatorManager(dim);
		final Deque<SubArrayToSort> stack = new ArrayDeque<SubArrayToSort>();
		stack.push(new SubArrayToSort(0,points.length,0));
		do{
			final SubArrayToSort ts = stack.pop();
			Arrays.sort(points,ts.lo,ts.hi,kCooMan.getComparator(ts.iCoo));
			final int length = ts.hi-ts.lo;
			if(length>5){
				final int pivot = (ts.hi+ts.lo)/2;
				if((++ts.iCoo)==dim) ts.iCoo=0;
				if(length>6) stack.push(new SubArrayToSort(pivot+1,ts.hi,ts.iCoo));
				stack.push(ts.set(ts.lo,pivot,ts.iCoo));
			}
		}while(!stack.isEmpty());
	}

	/**
	 * Check if the points tables is sorted following this KDTree scheme 
	 * @param points: a table of objet implementing the HasKCoo interface
	 * @param dim: the dimension of the points (= the K of KDTree)
	 * @return true if "points" is a KDTree (= is sorted following this KDTree scheme)
	 */
	public final boolean check(){
		if(points==null || points.length<3) return true;
		final Deque<SubArrayToSort> stack = new ArrayDeque<SubArrayToSort>();
		stack.push(new SubArrayToSort(0,points.length,0));
		do{
			final SubArrayToSort ts = stack.pop();
			final int pivot = (ts.hi+ts.lo)/2;
			final double val = points[pivot].coo(ts.iCoo);
			for(int i=ts.lo;i<pivot;i++)   if(points[i].coo(ts.iCoo)>val) return false;
			for(int i=pivot+1;i<ts.hi;i++) if(points[i].coo(ts.iCoo)<val) return false;
			final int length = ts.hi-ts.lo;
			if(length>5){
				if((++ts.iCoo)==dim) ts.iCoo=0;
				if(length>6) stack.push(new SubArrayToSort(pivot+1,ts.hi,ts.iCoo));
				stack.push(ts.set(ts.lo,pivot,ts.iCoo));
			}
		}while(!stack.isEmpty());
		return true; 
	}

	
	/**
	 * Returns the nearest elements from the target that contains the table "kdtree"
	 * WARNING: - the table kdtree must have been sorted by the "create" function!!
	 *          - the dim of target must be the same as the dim of the kdtree!
	 *          --> If not you will obtain a random result, without error or warning!
	 * @param <T> an Object of your class implementing the HasKCoo interface
	 * @param target: the target
	 * @return the nearest elements of the target that contains the table "kdtree"
	 */
	public final  E nn(final HasKCoo target){
		double d_min = Double.MAX_VALUE;
		E nn = null;

		final Deque<Box> stack = new ArrayDeque<Box>();
		pullFor(target,new Box(0,points.length,dim,0),stack);
		do{
			final Box b = stack.pop();
			final int ipivot = (b.hi+b.lo)/2;
			final E pivot = points[ipivot];
			final double d_pivot = euclidian(target,pivot);
			if(d_pivot<d_min){
				d_min = d_pivot;
				nn = pivot;
				nn.setDistance(d_pivot);
			}
			if((b.hi-b.lo)>2){
				Box nb = b.nextBox(target,pivot,ipivot,false);
				if(nb.isIntersectedBy(target,d_min)){
					pullFor(target,nb,stack);
				}
			}
		}while(!stack.isEmpty());
		return nn;
	}
	/**
	 * Returns the nearest elements from the target that contains the table "kdtree"
	 * WARNING: - the table kdtree must have been sorted by the "create" function!!
	 *          - the dim of target must be the same as the dim of the kdtree!
	 *          --> If not you will obtain a random result, without error or warning!
	 * @param <T> an Object of your class implementing the HasKCoo interface
	 * @param dist_max: distance max for counterparts (in sphere radius unit)
	 * @param target: the target
	 * @return the nearest elements of the target that contains the table "kdtree"
	 * @throws QueryException 
	 */
	public  E nn(final HasKCoo target, final double dist_max) throws QueryException{
		double d_min = dist_max;
		E nn = null;

		final Deque<Box> stack = new ArrayDeque<Box>();
		pullFor(target,new Box(0,points.length,dim,0),stack);
		do{
			final Box b = stack.pop();
			final int ipivot = (b.hi+b.lo)/2;
			final E pivot = points[ipivot];
			final double d_pivot = euclidian(target,pivot);
			if(d_pivot<d_min){
				d_min = d_pivot;
				nn = pivot;
				nn.setDistance(d_pivot);
			}
			if((b.hi-b.lo)>2){
				Box nb = b.nextBox(target,pivot,ipivot,false);
				if(nb.isIntersectedBy(target,d_min)){
					pullFor(target,nb,stack);
				}
			}
		}while(!stack.isEmpty());
		return nn;
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
	 * @return the k nearest elements from the target that contains the table "kdtree"
	 * sorted according to their distance to the target
	 */
	public  final  TreeMap<Double,E> knn(final HasKCoo target,final int k){
		double d_min = Double.MAX_VALUE;
		TreeMap<Double,E> knn = new TreeMap<Double,E>();

		if( k >= points.length ) {
			throw new IllegalArgumentException("Number of searched neighbours (" + k + ") can not excess KDTree size (" + points.length + ")");
		}
		final Deque<Box> stack = new ArrayDeque<Box>();
		pullFor(target,new Box(0,points.length,dim,0),stack);
		do{
			final Box b = stack.pop();
			final int ipivot = (b.hi+b.lo)/2;
			final E pivot = points[ipivot];
			final double d_pivot = euclidian(target,pivot);
			if(d_pivot<d_min){
				E previous = pivot;
				double d = d_pivot;
				previous.setDistance(d);
				while((previous=knn.put(d,previous))!=null){  d += DELTA_MIN; }
				if(knn.size()>=k){// At maximum size should be = k+1 so we can save a test by replacing the while by a if(size>k) or size==k+1
					while(knn.size()>k){ knn.remove(knn.lastKey()); } // In the case knn.size()==k, don't remove!
					d_min = knn.lastKey();
				}
			}
			if((b.hi-b.lo)>2){
				Box nb = b.nextBox(target,pivot,ipivot,false);
				if(nb.isIntersectedBy(target,d_min)){
					pullFor(target,nb,stack);
				}
			}
		}while(!stack.isEmpty());
		return knn;
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
	public   TreeMap<Double,E> knn(final HasKCoo target,final int k, double dist_max) throws QueryException{
		double d_min = dist_max;
		TreeMap<Double,E> knn = new TreeMap<Double,E>();

//	Fixed in pullFor method		
//		if( k >= points.length ) {
//			throw new IllegalArgumentException("Number of searched neighbours (" + k + ") can not excess KDTree size (" + points.length + ")");
//		}
		final Deque<Box> stack = new ArrayDeque<Box>();
		pullFor(target,new Box(0,points.length,dim,0),stack);
		do{
			final Box b = stack.pop();
			final int ipivot = (b.hi+b.lo)/2;
			final E pivot = points[ipivot];
			final double d_pivot = euclidian(target,pivot);
			if(d_pivot<d_min){
				E previous = pivot;
				double d = d_pivot;
				previous.setDistance(d);
				while((previous=knn.put(d,previous))!=null){  d += DELTA_MIN; }
				if(knn.size()>=k){// At maximum size should be = k+1 so we can save a test by replacing the while by a if(size>k) or size==k+1
					while(knn.size()>k){ knn.remove(knn.lastKey()); } // In the case knn.size()==k, don't remove!
					d_min = knn.lastKey();
				}
			}
			if((b.hi-b.lo)>2){
				Box nb = b.nextBox(target,pivot,ipivot,false);
				if(nb.isIntersectedBy(target,d_min)){
					pullFor(target,nb,stack);
				}
			}
		}while(!stack.isEmpty());
		return knn;
	}

	/**
	 * Returns all the elements inside a sphere of radius "d_max" and center "target"
	 * that contains the table "kdtree" sorted according to their distance to the target
	 * WARNING: - the table kdtree must have been sorted by the "create" function!!
	 *          - the dim of target must be the same as the dim of the kdtree!
	 *          --> If not you will obtain a random result, without error or warning!
	 * @param <T> a class implementing the HasKCoo interface
	 * @param target: the target
	 * @param d_max: the radius of the "dim" dimensions sphere we are looking in
	 * @return all the elements inside a sphere of radius "d_max" and center "target"
	 * that contains the table "kdtree" sorted according to their distance to the target
	 */
	public final  TreeMap<Double,E> inKDSphere(final HasKCoo target,double d_max){
		TreeMap<Double,E> knn = new TreeMap<Double,E>();

		final Deque<Box> stack = new ArrayDeque<Box>();
		pullFor(target,new Box(0,points.length,dim,0),stack);
		do{
			final Box b = stack.pop();
			final int ipivot = (b.hi+b.lo)/2;
			final E pivot = points[ipivot];
			final double d_pivot = euclidian(target,pivot);
			if(d_pivot<d_max){
				E previous = pivot;
				double d = d_pivot;
				previous.setDistance(d);
				while((previous=knn.put(d,previous))!=null){ d += DELTA_MIN; }
			}
			if((b.hi-b.lo)>2){
				Box nb = b.nextBox(target,pivot,ipivot,false);
				if(nb.isIntersectedBy(target,d_max)){
					pullFor(target,nb,stack);
				}
			}
		}while(!stack.isEmpty());
		return knn;
	}


	
	/**
	 * Come-down the sub tree represented by the Box box to put sub boxes in the stack
	 * @param target
	 * @param box
	 * @param stack
	 */
	protected final void pullFor(HasKCoo target,Box box,Deque<Box> stack){
		stack.push(box);
		int length = 0;
		do{
			final Box b = stack.getFirst();
			length = b.hi-b.lo;
			if(length>2){
				final int ipivot = (b.hi+b.lo)/2;
				stack.push(b.nextBox(target,points[ipivot],ipivot,true));
			}else if(length==2){
				stack.push(b.nextBox(points[b.lo],b.lo+1,true));
				//stack.push(b.nextBox(points[b.hi - 1],b.hi - 2,false));
			}
		}while(length>2);
	}

	/**
	 * @param p1
	 * @param p2
	 * @return
	 */
	protected final double euclidian(final HasKCoo p1,final HasKCoo p2){
		double res = 0;
		for(int i=0;i<dim;i++){
			double temp = p2.coo(i)-p1.coo(i);
			res += temp*temp;
		}
		return Math.sqrt(res);
	}

	/**
	 * @param p1
	 * @param p2
	 * @return
	 */
	private  final double euclidian(final HasKCoo p1,double[] p2){
		double res = 0;
		if( p2.length != dim ) {
			throw new IllegalArgumentException("bad size for target " + p2.length  + " ( " + dim + " expected)");
		}
		for(int i=0;i<dim;i++){
			double temp = p2[i]-p1.coo(i);
			res += temp*temp;
		}
		return Math.sqrt(res);
	}
	
	/**
	 * @author pineau
	 *
	 */
	private static final class SubArrayToSort {
		private int lo;
		private int hi;
		private int iCoo;
		private SubArrayToSort(int lo,int hi,int iCoo){
			this.set(lo,hi,iCoo);
		}
		private SubArrayToSort set(int lo,int hi,int iCoo){
			this.lo = lo;
			this.hi = hi;
			this.iCoo = iCoo;
			return this;
		}
	}

	/**
	 * Inner class
	 * @author pineau
	 *
	 */
	final class Box {
		int lo;
		int hi;
		private final int dim;
		private int coo;
		private final double[] lo_c;
		private final double[] hi_c;

		Box(int lo,int hi,int dim,int coo){
			this(lo,hi,dim,coo,new double[dim],new double[dim]);
			Arrays.fill(this.lo_c,-Double.MAX_VALUE);
			Arrays.fill(this.hi_c,Double.MAX_VALUE);
		}
		private Box(int lo,int hi,int dim,int coo,double[] lo_c,double[] hi_c){
			this.lo = lo;
			this.hi = hi;
			this.dim = dim;
			this.coo = coo;
			this.lo_c = lo_c;
			this.hi_c = hi_c;
		}
		// Juste pour DEBUG
		@Override
		public final String toString(){
			StringBuffer strBuff = new StringBuffer();
			strBuff.append("lo:").append(this.lo).append(" hi:").append(this.hi).append(" coo:").append(this.coo);
			strBuff.append("\n -min: ");
			for(double d:this.lo_c) strBuff.append(d).append("\t");
			strBuff.append("\n -max: ");
			for(double d:this.hi_c) strBuff.append(d).append("\t");
			return strBuff.toString();
		}

		/**
		 * Returns the next box where the pivot is in
		 * @param target
		 * @param pivot
		 * @param ipivot
		 * @return
		 */
		final Box nextBox(HasKCoo target,HasKCoo pivot,int ipivot,boolean in){ //		 "in" means get the box where the pivot is "in" or is not "in"
			int coo_next = this.coo;
			if((++coo_next)==dim) coo_next=0;
			if( target.coo(this.coo) < pivot.coo(this.coo) == in){ // xnor
				double[] hi_c_next = this.hi_c.clone();
				hi_c_next[this.coo] = pivot.coo(this.coo);
				return new Box(this.lo,ipivot,this.dim,coo_next,this.lo_c,hi_c_next);
			}else{
				double[] lo_c_next = this.lo_c.clone();
				lo_c_next[this.coo] = pivot.coo(this.coo);
				return new Box(ipivot+1,this.hi,this.dim,coo_next,lo_c_next,this.hi_c);
			}
		}
		/**
		 * Returns the next left box or the next right box
		 * @param pivot
		 * @param ipivot
		 * @param left
		 * @return
		 */
		private final Box nextBox(HasKCoo pivot,int ipivot,boolean left){
			int coo_next = this.coo;
			if((++coo_next)==dim) coo_next=0;
			if(left){
				double[] hi_c_next = this.hi_c.clone();
				hi_c_next[this.coo] = pivot.coo(this.coo);
				return new Box(this.lo,ipivot,this.dim,coo_next,this.lo_c,hi_c_next);
			}else{
				double[] lo_c_next = this.lo_c.clone();
				lo_c_next[this.coo] = pivot.coo(this.coo);
				return new Box(ipivot+1,this.hi,this.dim,coo_next,lo_c_next,this.hi_c);
			}
		}

		/**
		 * Returns true if the box is instersected by the sphere of dimension "dim",
		 * radius "radius", having for center the "target" point
		 * @param target
		 * @param dim
		 * @param radius
		 * @return
		 */
		final boolean isIntersectedBy(HasKCoo target,final double radius){
			final double[] n_coo = new double[dim];
			for(int i=0;i<dim;i++){
				double centeri = target.coo(i);
				n_coo[i] = (centeri<this.lo_c[i])?this.lo_c[i]:(centeri>this.hi_c[i])?this.hi_c[i]:centeri;
			}
			return (KDTree.this.euclidian(target, n_coo) <= radius);
		}
	}	


	/**
	 * This method must never be invoked
	 * @param target
	 * @param k
	 * @param dist_max
	 * @return
	 * @throws QueryException 
	 */
	public  TreeMap<Double,E> getCounterparts(final HasKCoo target,final int k, double dist_max) throws QueryException{
		return null;
	}


	public static void main(String[] args) throws Exception{
		ArgsParser ap = new ArgsParser(args);
		Database.init(ap.getDBName());

		SQLQuery squery = new SQLQuery();
		ResultSet rs = squery.run("select pos_x_csa, pos_y_csa, pos_z_csa, oidsaada from SelectedXGPS_spectrum where pos_x_csa is not null");
		List<HasKCoo> lhkc = new ArrayList<HasKCoo>();
		while( rs.next() ) {
			lhkc.add(new CounterPart(rs));
		}
		squery.close();

		
		KDTree<HasKCoo> kdtree = new KDTree<HasKCoo> ("SelectedXGPS", Category.ENTRY);

		for(HasKCoo t:lhkc){		
			HasKCoo res = kdtree.nn(t);
			System.out.println("NN of "+t.toString()+" found  ms. Res: "+res.toString()+" d="+res.getDistance());
		}
		
		for(HasKCoo t:lhkc){		
			TreeMap<Double, HasKCoo> res = kdtree.knn(t, 5, 0.2);
			System.out.println("Target "+t.toString());
			for( Entry<Double, HasKCoo> voisin: res.entrySet()) {
				System.out.println(" "+voisin.getValue().toString()+" at  "+voisin.getKey() + " (" + voisin.getValue().getDistance() + ")");
			}
		}

	}

	/**
	 * @param rs
	 * @return
	 * @throws SQLException 
	 */
	public CounterPart getCounterpart(ResultSet rs) throws SQLException {
		return new CounterPart(rs);
	}

}