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
import java.sql.SQLException;
import java.util.BitSet;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.query.result.SaadaQLResultSet;
import saadadb.util.Messenger;

/**
 * @author michel
 * * @version $Id$

 */
public class KeyIndex extends KeySet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 
	 */
	public final static int FREE = 0;
	public static final int SELECTED = 1;
	public static final int REJECTED = 2;
	public static final int OVERRIDE = 3;

	private long owner_key;
	private long last_access;

	protected int[] key_tag;
	protected boolean busy;



	/**
	 * @param name
	 * @param p
	 * @param size
	 */
	public KeyIndex(String name, String path, int size) {
		super(size);
		this.key_tag   = new int[size];
		if( name != null ) {
			this.name = name;
		}
	}


	/**
	 * @param name
	 * @param path
	 * @param extension
	 */
	public KeyIndex(String name, String path, String extension) {
		super(name, path, extension);
	}

	/**
	 * @param keys2
	 */
	public KeyIndex(long[] keys2) {
		super(keys2);
		this.key_tag   = new int[size];
	}

	/**
	 * @param keys2
	 * @throws FatalException 
	 * @throws SQLException 
	 */
	public KeyIndex(SaadaQLResultSet srs) throws FatalException  {
		super(srs);
		this.key_tag   = new int[size];
	}

	/**
	 * @param keys2
	 */
	public KeyIndex(Set<Long> keys2) {
		super(keys2);
		this.key_tag   = new int[size];
	}

	/**
	 * @return
	 */
	public boolean take(long owner_key) {
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Take index " + this.name + " " + owner_key);
		if( this.busy  && this.owner_key != owner_key ) {
			Messenger.printMsg(Messenger.DEBUG, "index " + this.name + " already in use by owner " + this.owner_key);
			return false;
		}
		//(new Exception()).printStackTrace();
		this.owner_key = owner_key;
		this.last_access = (new Date()).getTime();
		this.resetTags();
		this.busy = true;
		return true;
	}

	/**
	 * @return
	 */
	public boolean give() {
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Give index " + this.name + " " + owner_key);
		if( ! this.busy ) {
			return false;
		}
		this.busy = false;
		return true;
	}

	public boolean isBusy() {
		return this.busy;
	}
	/**
	 * 
	 */
	public long getOwner_key() {
		return this.owner_key;

	}

	/**
	 * @return
	 */
	public long getLast_access() {
		return this.last_access;
	}


	/**
	 * @return
	 */
	public TreeSet<Long> getSELECTEDKeySet() {
		TreeSet<Long> retour = new TreeSet<Long>();
		for( int i=0 ; i<this.size ; i++ ) {		
			if( this.key_tag[i] == SELECTED ) {
				retour.add(new Long(this.keys[i]));
			}
		}
		return retour;
	}
	/**
	 * @param ind
	 * @return
	 */
	public int getKeyTag(int ind) {
		if( ind >= 0 && ind < this.size ) {
			return key_tag[ind];
		}
		return -1;
	}

	/**
	 * 
	 */
	protected void resetTags() {
		for( int i=0 ; i<this.size ; i++ ) {
			this.key_tag[i] = FREE;
		}

	}

	/**
	 * @param ind
	 */
	public void setTagFREE(int ind) {
		if( ind >= 0 && ind < this.size ) {
			key_tag[ind] = KeyIndex.FREE;
		}
	}

	/**
	 * @param ind
	 */
	public void setTagREJECTED(int ind) {
		if( ind >= 0 && ind < this.size ) {
			key_tag[ind] = KeyIndex.REJECTED;
		}
	}

	/**
	 * @param ind
	 */
	public void setTagSELECTED(int ind) {
		if( ind >= 0 && ind < this.size ) {
			key_tag[ind] = KeyIndex.SELECTED;
		}
	}

	/**
	 * @param ind
	 */
	public void selectKey(long key) {
		int ind=0;
		if( (ind = this.hasDichotoKey(key, false)) >= 0 ) {
			if( ind >= 0 && ind < this.size ) {
				key_tag[ind] = KeyIndex.SELECTED;
			}
		}
	}

	/**
	 * @param ind
	 */
	public void selectKeysInRange(long key1, long key2) {
		int ind1 = this.hasDichotoKeyBefore(key1, false);
		int ind2 = this.hasDichotoKeyAfter(key2, false);
		this.rejectAllKeys();
		/*
		 * Requested range out of key range
		 */
		if( key1 > this.keys[this.size - 1] ||  key2 < this.keys[0]) {
			return;
		}
		else {
			if( key1  < this.keys[0] ) {
				ind1 = -1;
			}
			if( key2  > this.keys[this.size - 1] ) {
				ind2 = this.size ;
			}
			for( int i=(ind1+1) ; i<ind2 ; i++ ) {
				key_tag[i] = KeyIndex.SELECTED;				
			}
		}
	}

	/**
	 * @param ind
	 */
	public void selectKeysInERange(long key1, long key2) {
		int ind1 = this.hasDichotoKeyBefore(key1, false);
		int ind2 = this.hasDichotoKeyAfter(key2, false);
		this.rejectAllKeys();
		/*
		 * Requested range out of key range
		 */
		if( key1 > this.keys[this.size - 1] ||  key2 < this.keys[0]) {
			return;
		}
		else {
			if( key1  < this.keys[0] ) {
				ind1 = 0;
			}
			if( key2  > this.keys[this.size - 1] ) {
				ind2 = this.size - 1;
			}
			for( int i=ind1 ; i<=ind2 ; i++ ) {
				key_tag[i] = KeyIndex.SELECTED;				
			}
		}
	}

	/**
	 * @param ind
	 */
	public void selectKeysOutOfRange(long key1, long key2) {
		int ind1 = this.hasDichotoKeyBefore(key1, false);
		int ind2 = this.hasDichotoKeyAfter(key2, false);
		this.rejectAllKeys();
		/*
		 * Requested range out of key range
		 */
		if( key1 < this.keys[0] &&  key2 > this.keys[this.size - 1]) {
			return;
		}
		else {
			if( key1  > this.keys[this.size - 1] ) {
				ind1 = this.size - 1;
			}
			else if( key1  < this.keys[0] ) {
				ind1 = 0;
			}
			if( key2 <  this.keys[0] ) {
				ind2 = this.size - 1;
			}
			else if( key2  > this.keys[this.size - 1] ) {
				ind2 = this.size - 1;
			}
			for( int i=0 ; i<ind1 ; i++ ) {
				key_tag[i] = KeyIndex.SELECTED;				
			}
			for( int i=(ind2+1) ; i<this.size ; i++ ) {
				key_tag[i] = KeyIndex.SELECTED;				
			}
		}
	}
	/**
	 * @param ind
	 */
	public void selectKeysOutOfERange(long key1, long key2) {
		int ind1 = this.hasDichotoKeyBefore(key1, false);
		int ind2 = this.hasDichotoKeyAfter(key2, false);
		this.rejectAllKeys();
		/*
		 * Requested range out of key range
		 */
		if( key1 < this.keys[0] &&  key2 > this.keys[this.size - 1]) {
			return;
		}
		else {
			if( key1  > this.keys[this.size - 1] ) {
				ind1 = this.size - 1;
			}
			else if( key1  < this.keys[0] ) {
				ind1 = 0;
			}
			if( key2 <  this.keys[0] ) {
				ind2 = this.size - 1;
			}
			else if( key2  > this.keys[this.size - 1] ) {
				ind2 = this.size - 1;
			}
			for( int i=0 ; i<=ind1 ; i++ ) {
				key_tag[i] = KeyIndex.SELECTED;				
			}
			for( int i=ind2 ; i<this.size ; i++ ) {
				key_tag[i] = KeyIndex.SELECTED;				
			}
		}
	}

	/**
	 * @param ind
	 */
	public void selectKeysGreaterThan(long key1) {
		this.selectKeysInRange(key1, 0x7fffffffffffffL);

	}

	/**
	 * @param ind
	 */
	public void selectKeysEGreaterThan(long key1) {
		this.selectKeysInERange(key1, this.keys[this.size-1] + 1);

	}

	/**
	 * @param ind
	 */
	public void selectKeysLowerThan(long key1) {
		this.selectKeysInRange(this.keys[0] - 1,  key1);	
	}

	/**
	 * @param ind
	 */
	public void selectKeysELowerThan(long key1) {
		this.selectKeysInERange(this.keys[0] - 1,  key1);	
	}

	/**
	 * @param ind
	 */
	public void setTagOVERRIDE(int ind) {
		if( ind >= 0 && ind < this.size ) {
			key_tag[ind] = KeyIndex.OVERRIDE;
		}
	}


	public boolean hasKeySELECTED(){
		for(int tag:this.key_tag)if(tag==SELECTED)return true;
		return false;
	}

	/**
	 * @return
	 */
	public long[] getFREEKeys() {
		Vector<Long> vret = new Vector<Long>(100, 11111);
		for( int i=0 ; i<this.size ; i++ ) {
			if( this.key_tag[i] == KeyIndex.FREE ) {
				vret.add(new Long(this.keys[i]));
			}
		}
		long[] retour = new long[vret.size()];
		for( int i=0 ; i<vret.size() ; i++ ) {
			retour[i] = ((Long)(vret.get(i))).longValue();
		}
		return retour;
	}

	/**
	 * @return
	 */
	public long[] getSELECTEDKeys() {
		Vector<Long> vret = new Vector<Long>(100, 11111);
		for( int i=0 ; i<this.size ; i++ ) {
			if( this.key_tag[i] == KeyIndex.SELECTED ) {
				vret.add(new Long(this.keys[i]));
			}
		}
		long[] retour = new long[vret.size()];
		for( int i=0 ; i<vret.size() ; i++ ) {
			retour[i] = ((Long)(vret.get(i))).longValue();
		}
		return retour;
	}

	/**
	 * @return
	 */
	public long[] getREJECTEDKeys() {
		Vector<Long> vret = new Vector<Long>(100, 11111);
		for( int i=0 ; i<this.size ; i++ ) {
			if( this.key_tag[i] == KeyIndex.REJECTED ) {
				vret.add(new Long(this.keys[i]));
			}
		}
		long[] retour = new long[vret.size()];
		for( int i=0 ; i<vret.size() ; i++ ) {
			retour[i] = ((Long)(vret.get(i))).longValue();
		}
		return retour;
	}

	/**
	 * 
	 */
	public void rejectKeys(long[] rkeys) {
		for( int i=0 ; i<rkeys.length ; i++ ) {
			int k=-1;
			if( (k = this.hasDichotoKey(rkeys[i], false)) != -1 ) {
				this.key_tag[k] = KeyIndex.REJECTED;
			}
		}
	}

	/**
	 * 
	 */
	public void selectAllKeys() {
		for( int i=0 ; i<this.size ; i++ ) {
			this.key_tag[i] = KeyIndex.SELECTED;
		}
	}
	/**
	 * 
	 */
	public void rejectAllKeys() {
		for( int i=0 ; i<this.size ; i++ ) {
			this.key_tag[i] = KeyIndex.REJECTED;
		}
	}

	/**
	 * 
	 */
	public void selectKeys(long[] rkeys) {
		this.rejectAllKeys();
		for( int i=0 ; i<rkeys.length ; i++ ) {
			int k=-1;
			if( (k = this.hasDichotoKey(rkeys[i], false)) != -1 ) {
				this.key_tag[k] = KeyIndex.SELECTED;
			}
		}
	}

	/**
	 * @return
	 */
	public final int selectedKeysSize() {
		int nbr = 0;
		for(int i:this.key_tag){
			if(i==SELECTED) nbr++;
		}
		return nbr;
	}

	/**
	 * 
	 */
	public void freeKeys(long[] rkeys) {
		for( int i=0 ; i<rkeys.length ; i++ ) {
			int k=-1;
			if( (k = this.hasKey(rkeys[i], false)) != -1 ) {
				this.key_tag[k] = KeyIndex.FREE;
			}
		}
	}

	/**
	 * Mark selected all keys present in this and being selected in index
	 * @param index
	 */
	public void keysFusion(KeyIndex index) {
		long[] rkeys = index.getSELECTEDKeys();
		int pos;
		int cpt = 0;
		for( int i=0 ; i<rkeys.length ; i++ ) {
			if( (pos = this.hasDichotoKey(rkeys[i], false)) != -1 ) {
				this.setTagOVERRIDE(pos);
				cpt++;
			}
		}
		for( int i=0 ; i<this.size ; i++ ) {
			if( this.key_tag[i] == OVERRIDE ) {
				this.key_tag[i] = SELECTED;
			}
			else {
				this.key_tag[i] = REJECTED;
			}
		}
	}

	/**
	 * Mark selected all keys being selected in both this and index
	 * @param index
	 */
	public void selectedKeysFusion(KeyIndex index) {
		long[] rkeys = index.getSELECTEDKeys();
		int pos;
		int cpt=0;
		for( int i=0 ; i<rkeys.length ; i++ ) {
			if( (pos = this.hasDichotoKey(rkeys[i], true)) != -1 && this.key_tag[pos] == KeyIndex.SELECTED  ) {
				this.setTagOVERRIDE(pos);
				cpt++;
			}
		}
		for( int i=0 ; i<this.size ; i++ ) {
			if( this.key_tag[i] == OVERRIDE ) {
				this.key_tag[i] = SELECTED;
			}
			else {
				this.key_tag[i] = REJECTED;
			}
		}
	}

	/**
	 * Mark rejected all keys being  selected in index
	 * @param index
	 */
	public void unselectedKeysFusion(KeyIndex index) {
		long[] rkeys = index.getSELECTEDKeys();
		int pos;
		for( int i=0 ; i<rkeys.length ; i++ ) {
			if( (pos = this.hasDichotoKey(rkeys[i], true)) != -1  ) {
				this.setTagREJECTED(pos);
			}
			//			else {
			//				System.out.println(this.hasDichotoKey(rkeys[i], false) + " " + index.getKeyTag(i));
			//			}
		}
	}
	/**
	 * @param rindex
	 */
	public void selectedKeysAndCPFusion(KeyIndex rindex) {
		int lg = rindex.getSize(), pos;
		for( int i=0 ; i<lg ; i++ ) {
			if( rindex.getKeyTag(i) == SELECTED  ) {
				long rkey = rindex.getKey(i);
				if( (pos = this.hasDichotoKey(rkey, true)) != -1 ) {
					this.setTagOVERRIDE(pos);
					this.cpTagFusion(pos, rindex.getCPTag(pos));
				}
			}
		}
		for( int i=0 ; i<this.size ; i++ ) {
			if( this.key_tag[i] == OVERRIDE ) {
				this.key_tag[i] = SELECTED;
			}
			else {
				this.key_tag[i] = REJECTED;
			}
		}
	}



	/**
	 * @param key
	 * @return
	 */
	public int hasKey(long key, boolean selected_only) {
		long l=-1;
		int i;
		for(  i=0 ; i<keys.length ; i++ ) {
			if( (l = keys[i]) >= key ) {
				break;
			}
		}
		if( l == key ) {
			if( selected_only ) {
				if( this.key_tag[i] == SELECTED ) {
					return i;						
				}
				else {
					return -1;
				}
			}
			return i;
		}
		else {
			return -1;
		}
	}




	/**
	 * Returns the position of the key equals to "key" or -1
	 * @param key
	 * @param selected_only
	 * @return
	 */
	public int hasDichotoKey(long key, boolean selected_only) {
		int ret = dichotoLook (key, 0, this.size - 1);
		if( ret != -1 && selected_only ) {
			if( this.key_tag[ret] == SELECTED )  {
				return ret;
			}
			else {
				return -1;
			}
		}
		return ret;
	}

	/**
	 * Returns the position of the smallest key higher or equals to "key" 
	 * @param key
	 * @param selected_only
	 * @return
	 */
	public int hasDichotoKeyAfter(long key, boolean selected_only) {
		int ret = dichotoLookAfter(key, 0, this.size - 1);
		if( ret != -1 && selected_only ) {
			if( this.key_tag[ret] == SELECTED )  {
				return ret;
			}
			else {
				return -1;
			}
		}
		return ret;
	}
	/**
	 * Returns the position of the greatest key lower or equals to "key" 
	 * @param key
	 * @param selected_only
	 * @return
	 */
	public int hasDichotoKeyBefore(long key, boolean selected_only) {
		int ret = dichotoLookBefore(key, 0, this.size - 1);
		if( ret != -1 && selected_only ) {
			if( this.key_tag[ret] == SELECTED )  {
				return ret;
			}
			else {
				return -1;
			}
		}
		return ret;
	}

	/*
	 * Abstract method accessing counterparts
	 */
	/**
	 * @param pos
	 * @param rcptags
	 */
	public void cpTagFusion(int pos, BitSet rcptags) {}


	/**
	 * @param pos
	 * @return
	 */
	public long[] getLongCP(int pos) { return null;}

	/**
	 * @param pos
	 * @return
	 */
	public long[] getLongCP(long key) { return null;}

	/**
	 * @param pos
	 * @return
	 */
	public double[] getDoubleCP(int pos) { return null;}

	/**
	 * @param pos
	 * @return
	 */
	public double[] getDoubleCP(long key) { return null;}

	/**
	 * @param pos
	 * @return
	 */
	public BitSet getCPTag(int pos) { return null;}

	/**
	 * @param pos
	 * @return
	 */
	public BitSet getCPTag(long key) { return null;}

	/**
	 * @return
	 */
	public long[] getLongCPOfSelectedKeys() {return null;}

	/**
	 * @return
	 */
	public double[] getDoubleOfSelectedKeys() {return null;}

	/**
	 * @param cps
	 */
	public void addCounterparts(long key, long[] cps) {}

	/* (non-Javadoc)
	 * @see saadadb.relationindex.KeyIndex#addCounterparts(long, long[])
	 */
	public void setCounterparts(long key, long[] cps) {}

	/* (non-Javadoc)
	 * @see saadadb.relationindex.KeyIndex#addCounterparts(long, long[])
	 */
	public void setCounterparts(int  pos, long[] cps) {}

	/**
	 * @param cps
	 */
	public void addCounterparts(long key, double[] cps) {}
	/*
	 * 
	 * Abstract method accessing double counterparts
	 */
	/**
	 * @param cps
	 * @return
	 */
	public int selectCPMatchingList(double[] cps, boolean on_selected_only) {
		return 0;
	}	
	/**
	 * @param counterpart
	 */
	public int selectKeysMatchingCPList(double[] counterpart) {
		return -1;
	}
	/**
	 * @param val
	 */
	public int selectCPEqualsTo(double val, boolean on_selected_only){
		return -1;
	}	
	/**
	 * @param val
	 */
	public int selectCPGreaterThan(double val, boolean on_selected_only){
		return -1;
	}
	/**
	 * @param val
	 */
	public int selectCPEGreaterThan(double val, boolean on_selected_only){
		return -1;
	}
	/**
	 * @param val
	 */
	public int selectCPLowerThan(double val, boolean on_selected_only){return -1;}	
	/**
	 * @param val
	 */
	public int selectCPELowerThan(double val, boolean on_selected_only){return -1;}	
	/**
	 * @param val1
	 * @param v2
	 */
	public int selectCPInRange(double val1, double v2, boolean on_selected_only){
		return -1;
	}	
	/**
	 * @param val1
	 * @param v2
	 */
	public int selectCPInERange(double val1, double v2, boolean on_selected_only){
		return -1;
	}	
	/**
	 * @param val1
	 * @param v2
	 */
	public int selectCPOutOfRange(double val1, double v2, boolean on_selected_only){
		return -1;
	}
	/**
	 * @param val1
	 * @param v2
	 */
	public int selectCPOutOfERange(double val1, double v2, boolean on_selected_only){
		return -1;
	}
	/**
	 * @param val1
	 * @param v2
	 */
	public void selectCPFromAnotherIndex(KeyIndex rindex) {
		for( int i=0 ; i<rindex.getSize() ; i++ ) {
			if( i >= this.getSize() ) {
				Messenger.printMsg(Messenger.WARNING, "selectCPFromAnotherIndex with different size");
				return ;
			}
			this.setCPTag(i, rindex.getCPTag(i));
		}
	}
	/**
	 * @param pos
	 * @param bs
	 */
	protected void setCPTag(int pos, BitSet bs) {}
	/*
	 * 
	 * Abstract method accessing long counterparts
	 */
	/**
	 * @param on_selected_only
	 * @return
	 */
	public int selectAllCP( boolean on_selected_only) {return 0;}

	/**
	 * @param cps
	 * @return
	 */
	public int selectCPMatchingList(long[] cps, boolean on_selected_only) {
		return 0;
	}
	/**
	 * @param counterpart
	 */
	public int selectKeysMatchingCPList(long[] counterpart) {
		return -1;
	}
	/**
	 * @param val
	 */
	public int selectCPEqualsTo(long val, boolean on_selected_only){ return -1;}	
	/**
	 * @param val
	 */
	public int selectCPGreaterThan(long val, boolean on_selected_only){return -1;}
	/**
	 * @param val
	 */
	public int selectCPEGreaterThan(long val, boolean on_selected_only){return -1;}
	/**
	 * @param val
	 */
	public int selectCPLowerThan(long val, boolean on_selected_only){return -1;}	
	/**
	 * @param val
	 */
	public int selectCPELowerThan(long val, boolean on_selected_only){return -1;}	
	/**
	 * @param val1
	 * @param v2
	 */
	public int selectCPInRange(long val1, long v2, boolean on_selected_only){return -1;}
	/**
	 * @param val1
	 * @param v2
	 */
	public int selectCPInERange(long val1, long v2, boolean on_selected_only){return -1;}
	/**
	 * @param val1
	 * @param v2
	 */
	public int selectCPOutOfRange(long val1, long v2, boolean on_selected_only){return -1;}
	/**
	 * @param val1
	 * @param v2
	 */
	public int selectCPOutOfERange(long val1, long v2, boolean on_selected_only){return -1;}

	/*
	 * Methods selecting keys considering the number of selected counterparts
	 */
	/**
	 * @param val
	 * @return
	 */
	public int selectKeysOnCPCardEqualsTo(int val) { return -1;}
	/**
	 * @param val
	 * @return
	 */
	public int selectKeysOnCPCardEGreaterThan(int val) { return -1;}
	/**
	 * @param val
	 * @return
	 */
	public int selectKeysOnCPCardGreaterThan(int val) { return -1;}
	/**
	 * @param val
	 * @return
	 */
	public int selectKeysOnCPCardELowerThan(int val) { return -1;}
	/**
	 * @param val
	 * @return
	 */
	public int selectKeysOnCPCardLowerThan(int val) { return -1;}
	/**
	 * @param v1
	 * @param v2
	 * @return
	 */
	public int selectKeysOnCPCardInRange(int v1, int v2) { return -1;}
	/**
	 * @param v1
	 * @param v2
	 * @return
	 */
	public int selectKeysOnCPCardInERange(int v1, int v2) { return -1;}
	/**
	 * @param v1
	 * @param v2
	 * @return
	 */
	public int selectKeysOnCPCardOutOfRange(int v1, int v2) { return -1;}
	/**
	 * @param v1
	 * @param v2
	 * @return
	 */
	public int selectKeysOnCPCardOutOfERange(int v1, int v2) { return -1;}

	/**
	 * @param filename
	 */
	public void load() throws  FatalException{
		try {
			DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(this.pathname)));
			this.size = in.readInt();
			this.keys  = new long[this.size];
			this.key_tag   = new int[this.size];
			for(int i=0 ; i<this.size ; i++ ) {
				this.keys[i] = in.readLong();				
				this.key_tag[i] = KeyIndex.FREE;				
			}
			in.close();
		} catch( Exception e) {
			FatalException.throwNewException(SaadaException.MISSING_FILE, e);
		}
	}
	/**
	 * @param filename
	 * @return : the number of links (0  in the case of the abstract class)
	 */
	public int save() throws  FatalException{
		try {
			DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(this.pathname)));
			out.writeInt(this.size);
			for(int i=0 ; i<this.size ; i++ ) {
				out.writeLong(this.keys[i]);				
			}
			out.close();
		} catch( Exception e) {
			FatalException.throwNewException(SaadaException.MISSING_FILE, e);
		} 
		return 0;
	}

	/**
	 * 
	 */
	public void scan() {
		scan(-1);
	}

	/**
	 * 
	 */
	public void scan(int lgmax) {
		if( this.busy )
			System.out.println("Owned by " + this.owner_key );
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
			System.out.println("");
			if( lgmax > 0 && i >= lgmax ) {
				System.out.println("truncated..");
				break;

			}
		}
		System.out.println("");
	}

	/**
	 * @param cp_length
	 */
	public void dummyFill(int cp_length){
		keys[0] = 1;
		for( int i=1 ; i<size ; i++ ) {
			keys[i] = keys[i-1] + 4;
		}
	}

	public static void testDicho() {
		for( int size=1 ; size<1000000 ;  ) {
			KeyIndex ki = new KeyIndex("", "", size) ;
			ki.dummyFill(0);
			long v0 = ki.keys[0] - 100;
			long v1 = ki.keys[ki.getSize()-1] + 100;
			int faux=0 , vrai=0;
			System.out.println("*** size = " + size);
			for( long i=v0 ; i<=v1 ; i++) {
				int ret = ki.hasDichotoKey(i, false);
				if( ret == -1 ) {
					faux++;
				}
				else {
					if( ki.keys[ret] != i ) {
						System.out.println("ERREUR a i=" + i + " : " + ki.keys[ret] + " !=  " + i);
						System.exit(1);
					}
					vrai++;
				}	
			}
			long delta = (10*size)/100;
			if( delta == 0 ) {
				delta = 1;
			}
			size += delta;
			System.out.println(" keys found (" + vrai + "/" + faux + ") " );
		}
	}

	public static void testDichoApprox() {
		for( int size=10 ; size<11 ;  ) {
			KeyIndex ki = new KeyIndex("", "", size) ;
			ki.dummyFill(0);
			ki.scan(-1);
			long v0 = ki.keys[0] - 2;
			long v1 = ki.keys[ki.getSize()-1] + 2;
			int faux=0 , vrai=0;
			System.out.println("*** size = " + size);
			for( long i=v0 ; i<=v1 ; i++) {
				int ret = ki.hasDichotoKeyAfter(i, false);
				int ret2 = ki.hasDichotoKeyBefore(i, false);
				if( ret == -1 ) {
					faux++;
				}
				else {
					if( ret != 0 && ki.keys[ret] < i ) {
						System.out.println("ERREUR a i=" + i + " : " + ki.keys[ret] + " !=  " + i);
						System.exit(1);
					}
					if( ret2 != 0 && ki.keys[ret2] > i ) {
						System.out.println("ERREUR a i=" + i + " : " + ki.keys[ret2] + " !=  " + i);
						System.exit(1);
					}
					vrai++;
				}	
				System.out.print("cherche " + i + ": between key[" + ret2 + "] = " + ki.keys[ret2]);
				System.out.println(" and  key[" + ret + "] = " + ki.keys[ret]);
			}
			long delta = (10*size)/100;
			if( delta == 0 ) {
				delta = 1;
			}
			size += delta;
			System.out.println(" keys found (" + vrai + "/" + faux + ") " );
		}
	}
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args){
		try {
			KeyIndex ind;
			//		ind = new Index(10, 2);
			//		ind.scan();
			//		ind.save("/home/michel/Desktop/index.dump");
			//		ind.load("/home/michel/Desktop/index.dump");
			//		ind.scan();
			//		System.exit(0);

			KeyIndex.testDichoApprox();
			System.exit(0);
			long t0 = (new Date()).getTime();
			int size = 100000;
			ind = new KeyIndex("", "", size);
			System.out.println("Creation in " + ((new Date()).getTime() - t0) + " ms");

			t0 = (new Date()).getTime();
			ind.save();
			System.out.println("saved in " + ((new Date()).getTime() - t0) + " ms");


			t0 = (new Date()).getTime();
			ind.load();
			System.out.println("loaded in " + ((new Date()).getTime() - t0) + " ms");

			t0 = (new Date()).getTime();
			int i=0, faux=0 , vrai=0;
			long[] keys = new long[1000];
			for( i=0 ; i<1000 ; i++ ) {
				keys[i] = (long)(Math.random() * size * 1.5);	
				keys[0] = 0;
				keys[999] = size-1; 
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
				in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(new File("/home/michel/Desktop/index.dump"))));
				ind = (KeyIndex) (in.readObject());
				in.close();
				System.out.println("serializable loaded in " + ((new Date()).getTime() - t0) + " ms");
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return Returns the keys.
	 */
	public long[] getKeys() {
		return keys;
	}

	public String toString() {
		String retour = "";
		retour += this.keys.length + " keys\n";
		for( int i=0 ; i< this.keys.length ; i++ ) {
			retour += '#' + i + " " + this.keys[i] + " " + this.key_tag.length + "cparts";
			if( i > 10 ) {
				retour += " truncated\n";
			}
		}
		return retour;
	}
}
