package saadadb.relationship;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.util.Messenger;

/**
 * @author michel
 *
 */
public class DoubleCPIndex extends KeyIndex {
	protected BitSet[] cp_tag;
	protected double[][] counterparts;


	/**
	 * @param size
	 * @param cp_length
	 */
	public DoubleCPIndex(String name, String filename, int size) {
		super(name, filename, size);
		this.cp_tag   = new BitSet[size];
		this.counterparts = new double[size][];
		for( int i=0 ; i<this.size ; i++ ) {
			this.counterparts[i] = new double[0];
			this.cp_tag[i] = new BitSet(0);
		}
	}
	/**
	 * @param name
	 * @param path
	 * @param extension
	 */
	public DoubleCPIndex(String name, String path, String extension) {
		super(name, path, extension);
	}

	/**
	 * @param size
	 * @param cp_length
	 */
	public DoubleCPIndex(long[] keys2) {
		super(keys2);
		this.cp_tag   = new BitSet[size];
		this.counterparts = new double[size][];
		for( int i=0 ; i<this.size ; i++ ) {
			this.counterparts[i] = new double[0];
			this.cp_tag[i] = new BitSet(0);
		}
	}

	/* (non-Javadoc)
	 * @see KeyIndex#getLongCP(int)
	 */
	@Override
	public double[] getDoubleCP(int pos) { 
		if( pos < 0 || pos >= this.size ) {
			return null;	
		}
		else {
			return this.counterparts[pos];
		}
	}

	/* (non-Javadoc)
	 * @see KeyIndex#getLongCP(long)
	 */
	@Override
	public double[] getDoubleCP(long key) { 
		int pos;
		if( (pos = this.hasDichotoKey(key, false)) == -1 ) {
			return null;
			}
		else {
			return this.counterparts[pos];
		}
	}

	/* (non-Javadoc)
	 * @see saadadb.relationindex.KeyIndex#addCounterparts(long, long[])
	 */
	@Override
	public void addCounterparts(long key, double[] cps) {
		int pos = 0;
		if( (pos = this.hasDichotoKey(key, false)) != -1 ) {
			double[] newcp = new double[this.counterparts[pos].length + cps.length];
			System.arraycopy(this.counterparts[pos], 0, newcp, 0, this.counterparts[pos].length);
			System.arraycopy(cps, 0, newcp, this.counterparts[pos].length, cps.length);
			this.counterparts[pos]  = newcp;
		}
	}
	/**
	 * @return
	 */
	public double[] getDoubleCPOfSelectedKeys() {
		int taille_retour = 0;
		double[] retour;
		/*
		 * Scaning twice the table could be more efficient tha doing a lot of
		 * array extensions including data copies
		 */
		for( int i=0 ; i<this.size ; i++ ) {
			if( this.key_tag[i] == KeyIndex.SELECTED ) {
				taille_retour += this.counterparts[i].length;
			}
		}
		retour = new double[taille_retour];
		int pos = 0;
		for( int i=0 ; i<this.size ; i++ ) {
			if( this.key_tag[i] == KeyIndex.SELECTED ) {
				double[] cp = this.counterparts[i];
				System.arraycopy(cp, 0, retour, pos, cp.length);
				pos +=  cp.length;
			}
		}
		return retour;
	}

	/* (non-Javadoc)
	 * @see saadadb.relationindex.KeyIndex#buildIndex(java.util.HashMap)
	 */
	@Override
	public void buildIndex(HashMap<Long,ArrayList<Object>> sourcemap){
		super.buildIndex(sourcemap);
		this.cp_tag   = new BitSet[this.size];
		this.counterparts = new double[this.size][];
		for( int i=0 ; i<this.size ; i++ ) {
			List<Object> l = sourcemap.get(this.keys[i]);
			Iterator<Object> it = l.iterator();
			this.cp_tag[i] = new BitSet(l.size());
			this.counterparts[i] = new double[l.size()];
			int j=0;
			while( it.hasNext()) {
				this.counterparts[i][j] = (Double)(it.next());
				j++;
			}
			/*
			 * Save memory while index is growing
			 */
			sourcemap.remove(this.keys[i]);
		}
	}
	/**
	 * 
	 */
	@Override
	protected void resetTags() {
		super.resetTags();
		for( int i=0 ; i<this.size ; i++ ) {
			this.cp_tag[i].clear();
		}		
	}
	
	/* (non-Javadoc)
	 * @see KeyIndex#getCPTag(int)
	 */
	@Override
	public BitSet getCPTag(int pos) { 
		if( pos < 0 || pos >= this.size ) {
			return null;
		}
		else {
			return this.cp_tag[pos];
		}
	}
	/* (non-Javadoc)
	 * @see KeyIndex#getCPTag(int)
	 */
	@Override
	public BitSet getCPTag(long key) { 
		int pos;
		if( (pos =  this.hasDichotoKey(key, false)) == -1 ) {
			return null;
		}
		else {
			return this.cp_tag[pos];
		}
	}
	
	/* (non-Javadoc)
	 * @see KeyIndex#cpTagFusion(int, java.util.BitSet)
	 */
	@Override
	public void cpTagFusion(int pos, BitSet rcptags) {
		if( pos >= 0 && pos < this.size ) {
			this.cp_tag[pos].and(rcptags);		
		}
	}
	

	/**
	 * 
	 * @param counterpart
	 */
	@Override
	public int selectKeysMatchingCPList(long[] counterpart) {		
		int ret = 0;
		for( int i=0 ; i<this.size ; i++ ) {
			boolean found = false;
			double[] cp = this.counterparts[i];
			for( int j=0 ; j<cp.length ; j++ ) {
				for( int k=0 ; k<counterpart.length ; k++ ) {
					if( counterpart[k] == cp[j] ) {
						this.key_tag[i] = KeyIndex.SELECTED;
						found = true;
						ret++;
						break;
					}
				}
				if( found ) {
					break;
				}
			}
			if( !found ) {
				this.key_tag[i] = KeyIndex.REJECTED;				
			}
		}
		return  ret;
	}

	/**
	 * @param counterpart
	 */
	public int rejectKeysMatchingCPList(long[] counterpart) {
		int ret = 0;
		for( int i=0 ; i<this.size ; i++ ) {
			boolean found = false;
			double[] cp = this.counterparts[i];
			for( int j=0 ; j<cp.length ; j++ ) {
				for( int k=0 ; k<counterpart.length ; k++ ) {
					if( counterpart[k] == cp[j] ) {
						this.key_tag[i] = KeyIndex.REJECTED;
						found = true;
						break;
					}
				}
				if( found ) {
					break;
				}
			}
		}
		return  ret;
	}

	/**
	 * 
	 */
	@Override
	public void scan() {
		for( int i=0 ; i<size ; i++ ) {
			System.out.print("[" + i + "] " + Long.toHexString(this.keys[i]) + "\t");
			if( this.key_tag[i] == SELECTED ) {
				System.out.print("SELECTED");
			}
			else if( this.key_tag[i] == REJECTED ) {
				System.out.print("REJECTED");
			}
			else {
				System.out.print("FREE");
			}
			System.out.print("\t" + this.cp_tag[i]);
			System.out.print("\t");
			double[] cp  = this.counterparts[i];
			for( int j=0 ; j<cp.length ; j++ ) {
				System.out.print(cp[j] + " " );
			}
			System.out.println("");
		}
		System.out.println("");
	}


	/* (non-Javadoc)
	 * @see KeyIndex#selectCPMatchingList(long[], boolean)
	 */
	@Override
	public int selectCPMatchingList(long[] cps, boolean on_selected_only) {
		HashSet<Long> ts = new HashSet<Long>();
		int ret = 0;
		for( int i=0 ; i<cps.length ; i++ ) {
			ts.add(new Long(cps[i]));
		}
		for( int i=0 ; i<this.size; i++ ) {
			for( int j=0 ; j<this.counterparts[i].length ; j++ ) {
				if( on_selected_only && this.key_tag[i] != KeyIndex.SELECTED) {
					continue;
				}
				if( ts.contains(this.counterparts[i][j])  ){
					this.cp_tag[i].set(j);
					ret++;
				}
			}
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see saadadb.relationindex.KeyIndex#setCPTag(int, java.util.BitSet)
	 */
	@Override
	protected void setCPTag(int pos, BitSet bs) {
		BitSet lbs = this.cp_tag[pos];
		if( lbs.length() == bs.length() ) {
			this.cp_tag[pos] = bs;
		}
		else {
			for( int i=0 ; i<bs.length() ; i++ ) {
				if( i >= lbs.length()) {
					return;
				}
				lbs.set(i, bs.get(i));
			}
		}
	}
	

	/* (non-Javadoc)
	 * @see saadadb.relationindex.KeyIndex#selectAllCP(boolean)
	 */
	@Override
	public int selectAllCP( boolean on_selected_only) {
		int ret = 0;
		for( int i=0 ; i<this.size; i++ ) {
			if( on_selected_only && this.key_tag[i] != KeyIndex.SELECTED) {
				continue;
			}
			this.cp_tag[i].set(0, this.counterparts[i].length);
			ret += this.counterparts[i].length;
		}
		
		
		return ret;
		
	}
	
	/* (non-Javadoc)
	 * @see KeyIndex#selectCPEqualsTo(long, boolean)
	 */
	@Override
	public int selectCPEqualsTo(double val, boolean on_selected_only){
		int ret = 0;
		for( int i=0 ; i<this.size; i++ ) {
			if( on_selected_only && this.key_tag[i] != KeyIndex.SELECTED) {
				continue;
			}
			for( int j=0 ; j<this.counterparts[i].length ; j++ ) {
				if( this.counterparts[i][j] == val  ){
					this.cp_tag[i].set(j);
					ret++;
				}
			}
		}
		return ret;
	}
	
	/* (non-Javadoc)
	 * @see KeyIndex#selectCPGreaterThan(long, boolean)
	 */
	@Override
	public int selectCPGreaterThan(double val, boolean on_selected_only){
		int ret = 0;
		for( int i=0 ; i<this.size; i++ ) {
			if( on_selected_only && this.key_tag[i] != KeyIndex.SELECTED) {
				continue;
			}
			for( int j=0 ; j<this.counterparts[i].length ; j++ ) {
				if( this.counterparts[i][j] > val  ){
					this.cp_tag[i].set(j);
					ret++;
				}
			}
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see KeyIndex#selectCPGreaterThan(long, boolean)
	 */
	@Override
	public int selectCPEGreaterThan(double val, boolean on_selected_only){
		int ret = 0;
		for( int i=0 ; i<this.size; i++ ) {
			if( on_selected_only && this.key_tag[i] != KeyIndex.SELECTED) {
				continue;
			}
			for( int j=0 ; j<this.counterparts[i].length ; j++ ) {
				if( this.counterparts[i][j] >= val  ){
					this.cp_tag[i].set(j);
					ret++;
				}
			}
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see KeyIndex#selectCPLowerThan(long, boolean)
	 */
	@Override
	public int selectCPLowerThan(double val, boolean on_selected_only){
		int ret = 0;
		for( int i=0 ; i<this.size; i++ ) {
			if( on_selected_only && this.key_tag[i] != KeyIndex.SELECTED) {
				continue;
			}
			for( int j=0 ; j<this.counterparts[i].length ; j++ ) {
				if( this.counterparts[i][j] < val  ){
					this.cp_tag[i].set(j);
					ret++;
				}
			}
		}
		return ret;
	}	
	/* (non-Javadoc)
	 * @see KeyIndex#selectCPLowerThan(long, boolean)
	 */
	@Override
	public int selectCPELowerThan(double val, boolean on_selected_only){
		int ret = 0;
		for( int i=0 ; i<this.size; i++ ) {
			if( on_selected_only && this.key_tag[i] != KeyIndex.SELECTED) {
				continue;
			}
			for( int j=0 ; j<this.counterparts[i].length ; j++ ) {
				if( this.counterparts[i][j] <= val  ){
					this.cp_tag[i].set(j);
					ret++;
				}
			}
		}
		return ret;
	}	
	

	/* (non-Javadoc)
	 * @see KeyIndex#selectCPInRange(long, long, boolean)
	 */
	@Override
	public int selectCPInRange(double v1, double v2, boolean on_selected_only){
		int ret = 0;
		for( int i=0 ; i<this.size; i++ ) {
			if( on_selected_only && this.key_tag[i] != KeyIndex.SELECTED) {
				continue;
			}
			for( int j=0 ; j<this.counterparts[i].length ; j++ ) {
				if( this.counterparts[i][j] > v1 &&  this.counterparts[i][j] < v2 ){
					this.cp_tag[i].set(j);
					ret++;
				}
			}
		}
		return ret;
	}	
	/* (non-Javadoc)
	 * @see KeyIndex#selectCPInRange(long, long, boolean)
	 */
	@Override
	public int selectCPInERange(double v1, double v2, boolean on_selected_only){
		int ret = 0;
		for( int i=0 ; i<this.size; i++ ) {
			if( on_selected_only && this.key_tag[i] != KeyIndex.SELECTED) {
				continue;
			}
			for( int j=0 ; j<this.counterparts[i].length ; j++ ) {
				if( this.counterparts[i][j] >= v1 &&  this.counterparts[i][j] <= v2 ){
					this.cp_tag[i].set(j);
					ret++;
				}
			}
		}
		return ret;
	}	
	
	/* (non-Javadoc)
	 * @see KeyIndex#selectCPOutOfRange(long, long, boolean)
	 */
	@Override
	public int selectCPOutOfRange(double v1, double v2, boolean on_selected_only){
		int ret = 0;
		for( int i=0 ; i<this.size; i++ ) {
			if( on_selected_only && this.key_tag[i] != KeyIndex.SELECTED) {
				continue;
			}
			for( int j=0 ; j<this.counterparts[i].length ; j++ ) {
				if( this.counterparts[i][j] < v1 ||  this.counterparts[i][j] > v2 ){
					this.cp_tag[i].set(j);
					ret++;
				}
			}
		}
		return ret;
	}	
	/* (non-Javadoc)
	 * @see KeyIndex#selectCPOutOfRange(long, long, boolean)
	 */
	@Override
	public int selectCPOutOfERange(double v1, double v2, boolean on_selected_only){
		int ret = 0;
		for( int i=0 ; i<this.size; i++ ) {
			if( on_selected_only && this.key_tag[i] != KeyIndex.SELECTED) {
				continue;
			}
			for( int j=0 ; j<this.counterparts[i].length ; j++ ) {
				if( this.counterparts[i][j] <= v1 ||  this.counterparts[i][j] >= v2 ){
					this.cp_tag[i].set(j);
					ret++;
				}
			}
		}
		return ret;
	}	
	
	/*
	 * Methods selecting keys considering the number of selected counterparts
	 */
	/* (non-Javadoc)
	 * @see KeyIndex#selectKeysOnCPCardEqualsTo()
	 */
	@Override
	public int selectKeysOnCPCardEqualsTo(int val) { 
		int ret = 0;
		for( int i=0 ; i<this.size; i++ ) {
			BitSet cp = this.cp_tag[i];
			int nb_sel = 0;
			for( int j=0 ; j<cp.length() ; j++ ) {
				if( cp.get(j) ) {
					nb_sel++;
				}
			}
			if( nb_sel == val ) {
				ret ++;
				this.key_tag[i] = SELECTED;
			}
			else {
				this.key_tag[i] = REJECTED;			
			}
		}
		return ret;
	}
		
	
	/* (non-Javadoc)
	 * @see KeyIndex#selectKeysOnCPCardGreaterThan()
	 */
	@Override
	public int selectKeysOnCPCardGreaterThan(int val) { 
		int ret = 0;
		for( int i=0 ; i<this.size; i++ ) {
			BitSet cp = this.cp_tag[i];
			int nb_sel = 0;
			for( int j=0 ; j<cp.length() ; j++ ) {
				if( cp.get(j) ) {
					nb_sel++;
				}
			}
			if( nb_sel > val ) {
				ret ++;
				this.key_tag[i] = SELECTED;
			}
			else {
				this.key_tag[i] = REJECTED;			
			}
		}
		return ret;
	}
	/* (non-Javadoc)
	 * @see KeyIndex#selectKeysOnCPCardGreaterThan()
	 */
	@Override
	public int selectKeysOnCPCardEGreaterThan(int val) { 
		int ret = 0;
		for( int i=0 ; i<this.size; i++ ) {
			BitSet cp = this.cp_tag[i];
			int nb_sel = 0;
			for( int j=0 ; j<cp.length() ; j++ ) {
				if( cp.get(j) ) {
					nb_sel++;
				}
			}
			if( nb_sel >= val ) {
				ret ++;
				this.key_tag[i] = SELECTED;
			}
			else {
				this.key_tag[i] = REJECTED;			
			}
		}
		return ret;
	}
	
	/* (non-Javadoc)
	 * @see KeyIndex#selectKeysOnCPCardLowerThan()
	 */
	@Override
	public int selectKeysOnCPCardLowerThan(int val){ 
		int ret = 0;
		for( int i=0 ; i<this.size; i++ ) {
			BitSet cp = this.cp_tag[i];
			int nb_sel = 0;
			for( int j=0 ; j<cp.length() ; j++ ) {
				if( cp.get(j) ) {
					nb_sel++;
				}
			}
			if( nb_sel < val ) {
				ret ++;
				this.key_tag[i] = SELECTED;
			}
			else {
				this.key_tag[i] = REJECTED;			
			}
		}
		return ret;
	}
	
	/* (non-Javadoc)
	 * @see KeyIndex#selectKeysOnCPCardLowerThan()
	 */
	@Override
	public int selectKeysOnCPCardELowerThan(int val){ 
		int ret = 0;
		for( int i=0 ; i<this.size; i++ ) {
			BitSet cp = this.cp_tag[i];
			int nb_sel = 0;
			for( int j=0 ; j<cp.length() ; j++ ) {
				if( cp.get(j) ) {
					nb_sel++;
				}
			}
			if( nb_sel <= val ) {
				ret ++;
				this.key_tag[i] = SELECTED;
			}
			else {
				this.key_tag[i] = REJECTED;			
			}
		}
		return ret;
	}
	/* (non-Javadoc)
	 * @see KeyIndex#selectKeysOnCPCardInRange()
	 */
	@Override
	public int selectKeysOnCPCardInRange(int v1, int v2) { 
		int ret = 0;
		for( int i=0 ; i<this.size; i++ ) {
			BitSet cp = this.cp_tag[i];
			int nb_sel = 0;
			for( int j=0 ; j<cp.length() ; j++ ) {
				if( cp.get(j) ) {
					nb_sel++;
				}
			}
			if( nb_sel > v1 && nb_sel < v2 ) {
				ret ++;
				this.key_tag[i] = SELECTED;
			}
			else {
				this.key_tag[i] = REJECTED;			
			}
		}
		return ret;
	}
	/* (non-Javadoc)
	 * @see KeyIndex#selectKeysOnCPCardInRange()
	 */
	@Override
	public int selectKeysOnCPCardInERange(int v1, int v2) { 
		int ret = 0;
		for( int i=0 ; i<this.size; i++ ) {
			BitSet cp = this.cp_tag[i];
			int nb_sel = 0;
			for( int j=0 ; j<cp.length() ; j++ ) {
				if( cp.get(j) ) {
					nb_sel++;
				}
			}
			if( nb_sel >= v1 && nb_sel <= v2 ) {
				ret ++;
				this.key_tag[i] = SELECTED;
			}
			else {
				this.key_tag[i] = REJECTED;			
			}
		}
		return ret;
	}
	/* (non-Javadoc)
	 * @see KeyIndex#selectKeysOnCPCardOutOfRange()
	 */
	@Override
	public int selectKeysOnCPCardOutOfRange(int v1, int v2) { 
		int ret = 0;
		for( int i=0 ; i<this.size; i++ ) {
			BitSet cp = this.cp_tag[i];
			int nb_sel = 0;
			for( int j=0 ; j<cp.length() ; j++ ) {
				if( cp.get(j) ) {
					nb_sel++;
				}
			}
			if( nb_sel <v1 || nb_sel > v2 ) {
				ret ++;
				this.key_tag[i] = SELECTED;
			}
			else {
				this.key_tag[i] = REJECTED;			
			}
		}
		return ret;
	}
	
	/* (non-Javadoc)
	 * @see KeyIndex#selectKeysOnCPCardOutOfRange()
	 */
	@Override
	public int selectKeysOnCPCardOutOfERange(int v1, int v2) { 
		int ret = 0;
		for( int i=0 ; i<this.size; i++ ) {
			BitSet cp = this.cp_tag[i];
			int nb_sel = 0;
			for( int j=0 ; j<cp.length() ; j++ ) {
				if( cp.get(j) ) {
					nb_sel++;
				}
			}
			if( nb_sel <=v1 || nb_sel >= v2 ) {
				ret ++;
				this.key_tag[i] = SELECTED;
			}
			else {
				this.key_tag[i] = REJECTED;			
			}
		}
		return ret;
	}
	
	/**
	 * @param filename
	 */
	@Override
	public void load() throws  FatalException{
		try {
			long t0 = (new Date()).getTime();
			DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(this.pathname)));
		    this.size = in.readInt();
			this.keys  = new long[this.size];
			this.key_tag   = new int[this.size];
			this.cp_tag   = new BitSet[this.size];
			this.counterparts = new double[this.size][];
			for(int i=0 ; i<this.size ; i++ ) {
				this.keys[i] = in.readLong();				
				this.key_tag[i] = KeyIndex.FREE;				
			}
			for(int i=0 ; i<this.size ; i++ ) {
				int cp_size = in.readInt();
				this.counterparts[i] = new double[cp_size];
				this.cp_tag[i] = new BitSet(cp_size);
				for(int j=0 ; j<this.counterparts[i].length ; j++ ) {
					this.counterparts[i][j] = in.readDouble();
				}				
			}
		    in.close();
		} catch( Exception e) {
			FatalException.throwNewException(SaadaException.MISSING_FILE, e);
		}
	}
	
	/**
	 * @param filename
	 */
	@Override
	public void save() throws  FatalException{
		try {
			long t0 = (new Date()).getTime();
			DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(pathname)));
			out.writeInt(this.size);
			for(int i=0 ; i<this.size ; i++ ) {
				out.writeLong(this.keys[i]);				
			}
			for(int i=0 ; i<this.size ; i++ ) {
				out.writeInt(this.counterparts[i].length);		
				for(int j=0 ; j<this.counterparts[i].length ; j++ ) {
					out.writeDouble(this.counterparts[i][j]);	
				}
			}
			out.close();
			Messenger.printMsg(Messenger.DEBUG, "Index " + name + " saved in " + ((new Date()).getTime() - t0) + "ms");
		} catch( Exception e) {
			FatalException.throwNewException(SaadaException.MISSING_FILE, e);
		}
	}

	/**
	 * @param cp_length
	 */
	@Override
	public void dummyFill(int cp_length) {
		super.dummyFill(cp_length);
		for( int i=0 ; i<size ; i++ ) {
			int cp_size = (int)(Math.random() * 2 * cp_length);
			if( cp_size == 0 ) {
				cp_size = 1;
			}
			counterparts[i] = new double[cp_size];
			cp_tag[i] = new BitSet(cp_size);
			for( int j=0 ; j<cp_size  ; j++ ) {
				//counterparts[i][j] = new Long( ((long)(i+1) << 32) + j);	
				counterparts[i][j]	= 	(Math.random() * cp_length);	
			}
		}
	
	}
	
	
	/**
	 * 
	 */
	public static void testCPSelection() {
		KeyIndex ind;
		
		ind = new DoubleCPIndex("", "", 4);
		ind.dummyFill(3);
		ind.scan();
		ind.resetTags();
		System.out.println("*** equals to 1");
		ind.selectCPEqualsTo(1, false);
		ind.scan();
		ind.resetTags();
		System.out.println("*** > 1");
		ind.selectCPGreaterThan(1, false);
		ind.scan();
		ind.resetTags();
		System.out.println("*** < 1");
		ind.selectCPLowerThan(1, false);
		ind.scan();
		ind.resetTags();
		System.out.println("*** in 1,2");
		ind.selectCPInRange(1,2, false);
		ind.scan();
		ind.resetTags();
		System.out.println("*** out of 1,2");
		ind.selectCPOutOfRange(1,2, false);
		ind.scan();
	}
	
	/**
	 * 
	 */
	public static void testCPSelectionOnSelected() {
		KeyIndex ind;
		
		ind = new DoubleCPIndex("", "", 4);
		ind.dummyFill(3);
		ind.scan();
		ind.resetTags();
		System.out.println("*** equals to 1");
		ind.selectKeysMatchingCPList(new long[] {2, 3});
		ind.selectCPEqualsTo(1, true);
		ind.scan();
		ind.resetTags();
		System.out.println("*** > 1");
		ind.selectKeysMatchingCPList(new long[] {2, 3});
		ind.selectCPGreaterThan(1, true);
		ind.scan();
		ind.resetTags();
		System.out.println("*** < 1");
		ind.selectKeysMatchingCPList(new long[] {2, 3});
		ind.selectCPLowerThan(1, true);
		ind.scan();
		ind.resetTags();
		System.out.println("*** in 1,2");
		ind.selectKeysMatchingCPList(new long[] {2, 3});
		ind.selectCPInRange(1,2, true);
		ind.scan();
		ind.resetTags();
		System.out.println("*** out of 1,2");
		ind.selectKeysMatchingCPList(new long[] {2, 3});
		ind.selectCPOutOfRange(1,2, true);
		ind.scan();
	}
	
	/**
	 * 
	 */
	public static void testKeyAndCPSelection() {
		KeyIndex ind;
		
		ind = new DoubleCPIndex("", "", 4);
		ind.dummyFill(3);
		ind.resetTags();

		System.out.println("*** select with CP matching (1,2)");
		ind.resetTags();
		ind.selectKeysMatchingCPList(new long[] {1,2});
		ind.scan();
		
		System.out.println("*** select with CP matching (2,3)");
		ind.resetTags();
		ind.selectKeysMatchingCPList(new long[] {2, 3});
		ind.scan();
		
		System.out.println("*** cardinality equals to 1");
		ind.resetTags();
		ind.selectCPOutOfRange(1,2, false);
		ind.selectKeysOnCPCardEqualsTo(1);
		ind.scan();
		
		System.out.println("*** cardinality > 1");
		ind.resetTags();
		ind.selectCPOutOfRange(1,2, false);
		ind.selectKeysOnCPCardGreaterThan(1);
		ind.scan();
		
		System.out.println("*** cardinality < 1");
		ind.resetTags();
		ind.selectCPOutOfRange(1,2, false);
		ind.selectKeysOnCPCardLowerThan(1);
		ind.scan();
		
		System.out.println("*** cardinality in 1,2");
		ind.resetTags();
		ind.selectCPOutOfRange(1,2, false);
		ind.selectKeysOnCPCardInRange(1,2);
		ind.scan();
		
		System.out.println("*** cardinality out of 1,2");
		ind.resetTags();
		ind.selectCPOutOfRange(1,2, false);
		ind.selectKeysOnCPCardOutOfRange(1,2);
		ind.scan();
	}
	
	public static void testFusion() {
		KeyIndex ind1, ind2;
		ind1 = new DoubleCPIndex("", "", 4);
		ind1.dummyFill(3);
		ind1.resetTags();
		ind1.selectCPMatchingList(new long[] {1,2}, false);
		ind1.selectKeysMatchingCPList(new long[] {1,2});
		System.out.println("*** ind1");
		ind1.scan();
		
		ind2 = new DoubleCPIndex("", "", 4);
		ind2.dummyFill(3);
		ind2.resetTags();
		ind2.selectCPMatchingList(new long[] {2,3}, false);
		ind2.selectKeysMatchingCPList(new long[] {2,3});
		System.out.println("*** ind2");
		ind2.scan();
		
		System.out.println("*** ind1 fusionn�");
		ind1.selectedKeysAndCPFusion(ind2);
		ind1.scan();
	}
	
	/**
	 * 
	 */
	@Override
	public void scan(int lgmax) {
		if( this.busy )
		System.out.println("Owned by " + this.getOwner_key() );
		for( int i=0 ; i<size ; i++ ) {
			System.out.print("[" + i + "] " + Long.toHexString(this.keys[i]) + "\t");
			if( this.key_tag[i] == SELECTED ) {
				System.out.print("SELECTED");
			}
			else if( this.key_tag[i] == REJECTED ) {
				System.out.print("REJECTED");
			}
			else {
				System.out.print("FREE");
			}
			System.out.println(" " + cp_tag[i] + " (" + cp_tag[i].length() + ")");
			if( lgmax > 0 && i >= lgmax ) {
				System.out.println("truncated..");
				break;

			}
		}
		System.out.println("");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)  {
		try {
		KeyIndex ind;
		long t0 = (new Date()).getTime();
		int size = 100000;
		ind = new DoubleCPIndex("", "", 4);
		
		System.out.println("*********** testCPselection");
		DoubleCPIndex.testCPSelection();
		
		System.out.println("*********** testKeyAndCPselection");
		DoubleCPIndex.testKeyAndCPSelection();
		
		System.out.println("*********** testCPSelectionOnSelected");
		DoubleCPIndex.testCPSelectionOnSelected();
		
		System.out.println("*********** test fusion");
		DoubleCPIndex.testFusion();
		System.exit(0);
		
		
		System.out.println("Creation in " + ((new Date()).getTime() - t0) + " ms");

		t0 = (new Date()).getTime();
		ind.save();
		System.out.println("Saved in " + ((new Date()).getTime() - t0) + " ms");
		
		t0 = (new Date()).getTime();
		ind.load();
		System.out.println("Loaded in " + ((new Date()).getTime() - t0) + " ms");
		
		t0 = (new Date()).getTime();
		int i=0, faux=0 , vrai=0;
		long[] keys = new long[1000];
		for( i=0 ; i<1000 ; i++ ) {
			keys[i] = (long)(Math.random() * size * 1.5);
		}
		for( i=0 ; i<1000 ; i++ ) {
		
			int ret = ind.hasKey(keys[i], false);
			if( ret == -1 ) {
				faux++;
			}
			else {
				vrai++;
			}
		}
		System.out.println(i + " keys found (" + vrai + "/" + faux + ") in " + ((new Date()).getTime() - t0) + " ms");
		t0 = (new Date()).getTime();
		faux=0;
		vrai=0;
		for( i=0 ; i<1000 ; i++ ) {
			
			int ret = ind.hasDichotoKey(keys[i], false);
			if( ret == -1 ) {
				faux++;
			}
			else {
				vrai++;
			}
		}
		System.out.println(i + " keys found (" + vrai + "/" + faux + ") in " + ((new Date()).getTime() - t0) + " ms");
		//ind.scan();
		
		t0 = (new Date()).getTime();
		int ret = ind.selectCPMatchingList(new long[] {1,2,3,4,5,6,7,8,9}, false);
		System.out.println(ret + " counterpars tagged  in " + ((new Date()).getTime() - t0) + " ms");
		System.exit(1);
		
		

		
		
		t0 = (new Date()).getTime();
		int free = (ind.getFREEKeys()).length;
		System.out.println(free + " free keys found  in " + ((new Date()).getTime() - t0) + " ms");
		
		long[] rkeys = new long[1000];
		for( i=0 ; i<rkeys.length ; i++ ) {
			rkeys[i] = 2*i;
		}
		t0 = (new Date()).getTime();
		ind.selectKeys(rkeys);
		ind.getSELECTEDKeys();
		System.out.println(rkeys.length + " keys selected  in " + ((new Date()).getTime() - t0) + " ms");
		
		
		t0 = (new Date()).getTime();
		i=0;
		faux=0 ;
		vrai=0;
		for( i=0 ; i<1000 ; i++ ) {
			long key = (long)(Math.random() * size * 1.5);
			ret = ind.hasKey(key, false);
			if( ret == -1 ) {
				faux++;
			}
			else {
				vrai++;
			}
		}
		System.out.println(i + " keys found (" + vrai + "/" + faux + ") in " + ((new Date()).getTime() - t0) + " ms");
		
		t0 = (new Date()).getTime();
		i=0;
		faux=0 ;
		vrai=0;
		long[] rcp = new long[1000];
		for( i=0 ; i<1000 ; i++ ) {
			rcp[i] = ((long)(Math.random() * size * 1.5) << 32) + 1;
		}
		ind.selectKeysMatchingCPList(rcp);
		i = ind.getSELECTEDKeys().length;
		System.out.println(i + " keys found  in " + ((new Date()).getTime() - t0) + " ms");
		
		t0 = (new Date()).getTime();
	    ObjectOutputStream out;
		try {
			out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(new File("/home/michel/Desktop/index.dump"))));
		    out.writeObject(ind);
			out.close();
		} catch (FileNotFoundException e) {
			Messenger.printStackTrace(e);
		} catch (IOException e) {
			Messenger.printStackTrace(e);
		}
		System.out.println("serializable saved in " + ((new Date()).getTime() - t0) + " ms");

		t0 = (new Date()).getTime();
	    ObjectInputStream in;
		try {
			in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(new File("/home/michel/Desktop/index.dump"))));
		    ind = (KeyIndex) (in.readObject());
		    in.close();
		} catch (FileNotFoundException e) {
			Messenger.printStackTrace(e);
		} catch (IOException e) {
			Messenger.printStackTrace(e);
		} catch (ClassNotFoundException e) {
			Messenger.printStackTrace(e);
		}
		System.out.println("serializable loaded in " + ((new Date()).getTime() - t0) + " ms");
		} catch(Exception e) {
			
		}
	
	}

}
