package saadadb.cache;


/**
 * <p>Title: SAADA </p>
 * <p>Description: Automatic Archival System For Astronomical Data -
    This is framework of a PhD funded by the CNES and by the Region Alsace.</p>
 * <p>Director of research: L.Michel and C. Motch.</p>
 * <p>Copyright: Copyright (c) 2002-2005</p>
 * <p>Company: Observatoire Astronomique Strasbourg-CNES</p>
 * @version SAADA 1.0
 * @author: NGUYEN Ngoc Hoan
 * E-Mail: nguyen@saadadb.u-strasbg.fr</p>
 */

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.WeakHashMap;

import saadadb.collection.SaadaInstance;
import saadadb.collection.SaadaOID;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.generationclass.SaadaClassReloader;
import saadadb.util.Messenger;

/**
 * @author michel
 *
 */
public class CacheManager {
	/*
	 * Map of the objects no longer used but not yet garbaged
	 */
    private HashMap<Long, SoftValue> soft_ref_map=new HashMap<Long, SoftValue>();
	/*
	 * Map of objects maintained in cache even if not used by the application
	 */   
    public  LinkedList<SaadaInstance> hook_table=new LinkedList<SaadaInstance> (); 
    /*
     * Access queue to the objects still not garbage
     */
    private ReferenceQueue queue = new ReferenceQueue();
    private  int Max_size_cache = 1000;

    /**
    * Constructor CacheManager
    */
    public  CacheManager(){}
    /**
    * Init CacheObject
    * @param size   number object limit of cache
    * @param db     Database
    */
    public void getCache(int size){
    	Max_size_cache=size;
    }
    
    private static class SoftValue extends SoftReference {
        private final Long key; // always make data member final
        /** Did you know that an outer class can access private data
         members and methods of an inner class?  I didn't know that!
         I thought it was only the inner class who could access the
         outer class's private information.  An outer class can also
         access private members of an inner class inside its inner
         class. */
        private SoftValue(Long key, SaadaInstance obj, ReferenceQueue q) {
    	super(obj, q);
    	this.key = key;
        }
      }
    /**
     * @param obj
     * @throws SaadaException 
     */
    public void getObjectBusiness(SaadaInstance obj) throws FatalException{
     		obj = Database.getObjectBusiness(obj);
    		if (obj != null) {
    			this.add(obj);
    		}
     }

    /**
     * return SaadaInstance
     * @param oid identifier of object
     * @exception CacheManagerException
     */
    public synchronized SaadaInstance getObject(long oid) throws FatalException{
    	SaadaInstance retour;
    	try{
     		SoftValue sr = soft_ref_map.get(new Long(oid));
     		/*
     		 * In some cases, very difficult to identify, we get a SoftValue but with a SaadaInstance NULL.
     		 * That could be due to an GC action during this code. But why synchronized has no effect??
     		 */
     		if (sr != null && (retour = (SaadaInstance)(sr.get())) != null ) {
    			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Object <" + oid +
    					"> SoftReferenced (level " +
    					hook_table.size() + "/" +  this.Max_size_cache + ")");
     			return retour;
    		}
    		else {
    			return  this.loadObject(oid);
     		}
    	}catch (Exception e) {			
    		Messenger.printStackTrace(e);
    		FatalException.throwNewException(SaadaException.METADATA_ERROR, e);
    	}
    	return null;
    }
 
	/**
	 * @param oid
	 * @return
	 * @throws SaadaException
	 */
	private SaadaInstance loadObject(long oid) throws Exception {
		SaadaInstance obj = null;
		try {
			String _nameclass = "";
			int class_id = SaadaOID.getClassNum(oid);
			/*
			 * Class_id = 1 is reserved for FLatfile which have no
			 * business classes
			 */
			if( class_id == 1 ) {
				_nameclass = "FLATFILEUserColl";
			}
			else {
				_nameclass = Database.cachemeta.getClass(SaadaOID.getClassNum(oid)).getName();
			}
			if (_nameclass.equals("")) {
				Messenger.printMsg(Messenger.ERROR, "No class name for Oid " + oid + " in table");
				return null;
			}
			_nameclass = _nameclass.trim();
			obj = (SaadaInstance)  SaadaClassReloader.forGeneratedName( _nameclass).newInstance();
			obj.init(oid);
			this.add(obj);
		} catch (Exception ex) {
			Messenger.printStackTrace(ex);
			FatalException.throwNewException(SaadaException.METADATA_ERROR, ex);
			return null;
		}
		return obj;
	}
   /**
     * Insert object into Cache
     * @param obj  object will insert inito cache
     * @exception CacheManagerException
     */
	private void add(SaadaInstance obj) {
		if (obj!=null){
			removeNull();
			/*
			 * New soft reference on the Saada Instance
			 */
			soft_ref_map.put((new Long(obj.getOid())), new SoftValue(new Long(obj.oidsaada), obj, queue));
			/*
			 * Bind the Saada Instance with the cache
			 */
			hook_table.add(obj); // references temporaires
		}
	}

    /**
     * @return
     */
    public String getStatus() {
    	return "SoftReference Map: " + soft_ref_map.size() + " bind objects: " + hook_table.size();
    }
    
    /**
     * remove the 20% oler bind object and cleanup the soft reference map
     */
    synchronized private void  removeNull() {
    	if( hook_table.size() > this.Max_size_cache ) {
    		/*
    		 * remove the 20% older object in hook_table
    		 */
    		int to_be_removed = hook_table.size() / 5;
    		for( int i=0 ; i<to_be_removed ; i++ ) {
    			hook_table.remove();
    		}
    		System.gc();
     		int queued = 0;
    		SoftValue sr;
    		while( (sr = (SoftValue)queue.poll()) != null ) {
    			queued++;
    			soft_ref_map.remove(sr.key);
    		}
    		if( queued != 0 ) {
    			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, (queued + " soft references removed: " + this.getStatus()));
    		}
    	}
    }
    

    /**
     * @param args
     */
    public static void main(String[] args) {
    	
     	long oid = 309237645313L;
    	Messenger.debug_mode = false;
    	Database.init(args[0]);
    	CacheManager cm = new CacheManager();
       	Messenger.debug_mode = true;
    	cm.getCache(100);
    	SaadaInstance obj;
    	int bcl=1000, plage=10;
    	for( int r=0 ; r<bcl ; r++ ) {
    		oid = 309237645313L + (long)r;
    		for( int i=0 ; i<plage ; i++ ) {
        		try {
    				obj = cm.getObject(oid);
    			} catch (Exception e) {
    				Messenger.printStackTrace(e);
    			}
        		oid = oid+1;
        		if( i == (plage-1) ) {
        			System.out.println((r*(i+1)*plage) + " " + i + " " + cm.getStatus());
        		}
    		}
    	}
     }

    public static void testWeakHashMap() {
        int size = 1000;
        // La taille peut être choisie via la ligne de commande :
         long[] keys = new long[size];
        WeakHashMap<Long, long[]> whm = new WeakHashMap<Long, long[]>();
        for(int i = 0; i < size; i++) {
          Long k = new Long(Long.toString(i));
          if(i % 3 == 0)
            keys[i] = k; // Save as "real" references
          whm.put(k, new long[10000]);
          System.out.println(whm.size() + " ");
       }
        System.exit(1);
      }
}
  
