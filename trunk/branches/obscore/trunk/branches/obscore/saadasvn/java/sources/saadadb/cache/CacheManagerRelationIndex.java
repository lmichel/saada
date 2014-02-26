package saadadb.cache;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.MetaRelation;
import saadadb.relationship.DoubleCPIndex;
import saadadb.relationship.KeyIndex;
import saadadb.relationship.LongCPIndex;
import saadadb.util.Messenger;

/**
 * @author michel
 *
 */
public class CacheManagerRelationIndex {
	private HashMap<String, KeyIndex> index;
	private int size;
	private String path_index;
	/**
	 * 
	 */
	public CacheManagerRelationIndex(int size, String path_index) {
		if( size <= 0 ) {
			size = 5;	
		}
		else {
			this.size = size;
		}
		index = new HashMap<String, KeyIndex>();
		this.path_index = path_index;
	}

	/**
	 * @param relation
	 * @param owner_key
	 * @return
	 */
	public KeyIndex getCorrIndex(String relation, long owner_key) throws  FatalException{
		return getIndex(relation, "corr", owner_key);
	}
	/**
	 * @param relation
	 * @param owner_key
	 * @throws FatalException
	 */
	public void freeCorrIndex(String relation, long owner_key) throws  FatalException{
		freeIndex(relation, "corr", owner_key);
	}

	/**
	 * @param relation
	 * @param owner_key
	 * @return
	 */
	public KeyIndex getCardIndex(String relation, long owner_key) throws  FatalException{
		return getIndex(relation, "card", owner_key);
	}
	/**
	 * @param relation
	 * @param owner_key
	 * @throws FatalException
	 */
	public void freeCardIndex(String relation, long owner_key) throws  FatalException{
		freeIndex(relation, "card", owner_key);
	}
	/**
	 * @param relation
	 * @param qualifier
	 * @param owner_key
	 * @return
	 */
	public KeyIndex getQualIndex(String relation, String qualifier, long owner_key) throws  FatalException{
		return getIndex(relation, "qual." + qualifier, owner_key);
	}
	/**
	 * @param relation
	 * @param owner_key
	 * @throws FatalException
	 */
	public void freeQualIndex(String relation, long owner_key) throws  FatalException{
		freeIndex(relation, "qual", owner_key);
	}
	/**
	 * @param relation
	 * @param Qualifier
	 * @param owner_key
	 * @return
	 */
	public KeyIndex getCorrClassIndex(String relation, String classe, long owner_key) throws  FatalException{
		return getIndex(relation, "corrclass." + classe, owner_key);
	}
	/**
	 * @param relation
	 * @param classe
	 * @param owner_key
	 * @throws FatalException
	 */
	public void freeCorrClassIndex(String relation, String classe, long owner_key) throws  FatalException{
		freeIndex(relation,  "corrclass." + classe, owner_key);
	}
	/**
	 * @param relation
	 * @param qualifier
	 * @param owner_key
	 * @return
	 */
	public KeyIndex getCorrClassQualIndex(String relation, String classe, String qualifier, long owner_key) throws  FatalException{
		return getIndex(relation, "classqual." + classe + "." + qualifier, owner_key);
	}
	/**
	 * @param relation
	 * @param classe
	 * @param qualifier
	 * @param owner_key
	 * @throws FatalException
	 */
	public void freeCorrClassQualIndex(String relation, String classe, String qualifier, long owner_key) throws  FatalException{
		freeIndex(relation,   "classqual." + classe + "." + qualifier, owner_key);
	}

	/**
	 * @param name_index
	 * @return
	 */
	synchronized private KeyIndex getIndex(String relation, String extension, long owner_key) throws  FatalException{
		KeyIndex ri=null;
		String name_index = relation + "." + extension + "." + ".sri";
		/*
		 * The index is already in the cache
		 */
		if( (ri = (KeyIndex)(index.get(name_index))) != null){
			/*
			 * if index busy: waits forever it is freed
			 */
			boolean noticed = false;
			int cpt=0;
			while( ri.take(owner_key) == false ) {
				if( !noticed ) {
					Messenger.printMsg(Messenger.WARNING, "Index " + name_index + " busy: enter a waiting pool");
					noticed = true;
				}
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					FatalException.throwNewException(SaadaException.INTERNAL_ERROR, "While waiting on index " + name_index + " : " + e.getMessage());
				}
				if( (cpt++) > 40 ) {
					FatalException.throwNewException(SaadaException.INTERNAL_ERROR, "The cache of relationship indexes seems to be locked: contact the administator");
				}
			}
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "return index <" + name_index + ">");
			return ri;
			/*
			 * else load it first
			 */
		} else {
			if( index.size() >= size ) {
				if( !removeOlderIndex() ) {
					Messenger.printMsg(Messenger.WARNING, "Cache full and no free index!!");
					return null;
				}
			}
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Add index <" + name_index + ">");
			ri = this.loadIndex(relation, extension);    	
			ri.take(owner_key);
			index.put(name_index, ri);
			return ri;
		}		
	}

	/**
	 * Free the given index owned by owner_key
	 * @param relation
	 * @param extension
	 * @param owner_key
	 * @return
	 * @throws FatalException
	 */
	synchronized public void freeIndex(String relation, String extension, long owner_key) throws  FatalException{
		KeyIndex ri=null;
		String name_index = relation + "." + extension + "." + ".sri";
		if( (ri = (KeyIndex)(index.get(name_index))) == null ) {
			FatalException.throwNewException(SaadaException.INTERNAL_ERROR
					, "Owner " + owner_key + " attempt to free index " + name_index + " which is not in the cache");
		} else if( ri.getOwner_key() == owner_key ){
			ri.give();
		} else {
			FatalException.throwNewException(SaadaException.INTERNAL_ERROR
					, "Owner " + owner_key + " attempt to free index " + name_index + " which has been taken by owner " + ri.getOwner_key());
		}	
	}
	/**
	 * @param owner_key
	 */
	synchronized public int freeIndexes(long owner_key) {
		int ret = 0 ;
		for( KeyIndex ki: this.index.values() ) {
			if( ki.getOwner_key() == owner_key ) {
				Messenger.printMsg(Messenger.DEBUG, "Free index <" + ki.getName() + ">");
				ret++;
				ki.give();
			}
		}
		return ret;
	}
	/**
	 * 
	 */
	private boolean removeOlderIndex() {
		Set k = index.keySet();
		long older = 0;
		String key="";
		for( Iterator i = k.iterator() ; i.hasNext();) {
			String me = (String) i.next();
			KeyIndex ri = index.get(me);
			if( !ri.isBusy() ) {
				if( older == 0 || ri.getLast_access() < older ) {
					older = ri.getLast_access();
					key = me;
				}
			}
		}
		if( key.equals("") ) {
			return false;
		}
		else {
			Messenger.printMsg(Messenger.DEBUG, "remove index <" + key + ">");
			this.index.remove(key);
			Database.gc();
			return true;
		}
	}	

	/**
	 * Remove all free indexes: Must be call to save memory
	 */
	public void flush() {
		int cpt = 0;
		int cpt_max = this.size;
		/*
		 * Cpt to avoid a forever loop in a faulty situation
		 */
		while( this.removeOlderIndex() && cpt < cpt_max ) cpt++;
	}
	/**
	 * @param relation
	 * @param extension
	 * @return
	 */
	public KeyIndex loadIndex(String relation, String extension) throws  FatalException{
		KeyIndex ki;

		if( extension.startsWith("qual.")  || extension.startsWith("classqual.")  ) {
			ki = new DoubleCPIndex(relation, this.path_index, extension);
		}
		else {
			ki = new LongCPIndex(relation, this.path_index, extension);
		}
		long t0 = (new Date()).getTime();
		ki.load();
		Messenger.printMsg(Messenger.DEBUG, "Index <" + ki.getName() + "> loaded in " + ((new Date()).getTime() - t0) + "ms");
		return ki;

	}

	/**
	 * @return
	 */
	public HashMap getIndex() {
		return index;
	}

	/**
	 * @return
	 */
	public int getSize() {
		return size;
	}

	/**
	 * @return
	 */
	public int getCurrentSize() {
		return index.size();
	}

	public void scan() {
		Iterator it = this.index.keySet().iterator();
		System.out.println("------------------------");
		while( it.hasNext()) {
			KeyIndex ki = this.index.get(it.next());
			System.out.println(ki.getName() + " " + ki.isBusy() + " " + ki.getOwner_key());
		}
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) throws  Exception{
		testCache();
		Database.close();
	}

	public static void testCache() throws  Exception{
		CacheManagerRelationIndex cm = new CacheManagerRelationIndex(5, 
				Database.getRoot_dir() + Database.getSepar()+ "indexation" + Database.getSepar()
		);
		String[] relations = Database.getCachemeta().getRelation_names();

		while( true ) {
			for( int r=0 ; r<relations.length ; r++ ) {
				MetaRelation mr = Database.getCachemeta().getRelation(relations[r]);
				String[] quals = mr.getQualifier_names().toArray(new String[0]);
				String[] cs =  Database.getCachemeta().getClassesOfCollection(mr.getSecondary_coll(), mr.getSecondary_category());
				KeyIndex ki;
				System.out.println("*** Relation " + relations[r]);

				for(int cpt=0 ; cpt<2 ; cpt++ ) {
					long owner_key = (new Date()).getTime();

					if( (ki = cm.getCorrIndex(relations[r], owner_key)) == null ) {
						System.out.println("getCorrIndex failed");
						System.exit(1);
					}
					ki.give();
					if( (ki = cm.getCardIndex(relations[r], owner_key)) == null ) {
						System.out.println("getCardIndex failed");
						System.exit(1);
					}
					ki.give();
					for( int i=0 ; i<quals.length; i++ ) {
						if( (ki = cm.getQualIndex(relations[r], quals[i], owner_key)) == null ) {
							System.out.println("getQualIndex failed");
							System.exit(1);
						}
						ki.give();

					}
					for( int i=0 ; i<cs.length; i++ ) {
						if( (ki = cm.getCorrClassIndex(relations[r], cs[i], owner_key)) == null ) {
							System.out.println("getCorrClassIndex failed");
							System.exit(1);
						}
						ki.give();
						for( int j=0 ; j<quals.length; j++ ) {
							if( (ki = cm.getCorrClassQualIndex(relations[r], cs[i], quals[j], owner_key)) == null ) {
								System.out.println("getCorrClassQualIndex failed");
								System.exit(1);
							}
							ki.give();

						}
					}
				}
			}
		}
	}	

}
