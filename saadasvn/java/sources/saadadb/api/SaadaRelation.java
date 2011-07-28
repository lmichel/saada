package saadadb.api;

import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Date;
import java.util.Vector;

import saadadb.collection.Category;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.MetaRelation;
import saadadb.relationship.KeyIndex;
import saadadb.sqltable.SQLQuery;
import saadadb.util.Messenger;

/**
 * @author michel
 *
 */
public class SaadaRelation extends SaadaDMBrik {
	MetaRelation metarelation;
	
    /**
     * @param saadadb
     * @param name
     */
	public SaadaRelation(String name){
		super(name);
		metarelation = Database.getCachemeta().getRelation(name);
    }
	
	/**
	 * @return
	 */
	public String getPrimaryCollName() {
		return metarelation.getPrimary_coll();
	}
	
	/**
	 * @return
	 */
	public SaadaCollection getPrimaryColl() {
		try {
			return new SaadaCollection(metarelation.getPrimary_coll());
		} catch (SaadaException e) {
			Messenger.printStackTrace(e);
			return null;
		}
	}
	
	/**
	 * @return
	 */
	public int getPrimaryCategory() {
		return metarelation.getPrimary_category();	
	}
	

	/**
	 * @return
	 */
	public String getSeconadryCollName() {
		return metarelation.getSecondary_coll();
	}
	
	/**
	 * @return
	 */
	public SaadaCollection getSecondaryColl() {
		try {
			return new SaadaCollection(metarelation.getSecondary_coll());
		} catch (SaadaException e) {
			Messenger.printStackTrace(e);
			return null;
		}
	}
	
	/**
	 * @return
	 */
	public int getSecondaryCategory() {
		return metarelation.getSecondary_category();	
	}

        
    /**
     * @return
     */
    public String[] getQualifiers() {
    	return metarelation.getQualifier_names().toArray(new String[0]);
    }
    
    /**
     * @return
     */
    public String getDescription() {
		return metarelation.getDescription();	
   	
    }
 

    /**
     * @param oid
     * @return
     * @throws FatalException 
     */
    public int getNumberOfCounterparts(long oid) throws FatalException {
    	long owner = (new Date()).getTime();
    	KeyIndex ind;
		ind = SaadaDB.getCacheIndex().getCorrIndex(this.name, owner);
    	if( ind == null ) {
    		Messenger.printMsg(Messenger.ERROR, "Could not find correlation index <" + this.name +">");
    		return 0;
    	}
     	long[] cp = ind.getLongCP(oid);
    	if( cp == null ) {
    		SaadaDB.getCacheIndex().freeIndexes(owner);
    		return 0;
    	}
    	else {
    		SaadaDB.getCacheIndex().freeIndexes(owner);
    		return cp.length;
    	}
    }
    /**
     * @param oid
     * @return
     */
    public SaadaLink[] getCounterparts(long oid, boolean sorted) throws Exception{
    	String[]     qnames     = null;
    	SaadaLink[] tbl        = null;
    	Vector <SaadaLink>      retour = new Vector<SaadaLink>(10);
    	boolean found = false;
		SQLQuery squery = new SQLQuery();

    	ResultSet rs = squery.run("select * from " + this.name + " where oidprimary = " + oid + " limit 1000");
    	qnames = Database.getCachemeta().getRelation(this.name).getQualifier_names().toArray(new String[0]);
    	while( rs.next() ) {
    		Vector<Double> qts = new Vector<Double>(qnames.length);
    		found = true;
    		for( int k=0 ; k<qnames.length ; k++ ){
    			qts.add(new Double(rs.getDouble(qnames[k])));
    		}
    		retour.addElement(new SaadaLink(this, oid, rs.getLong(2), (qts.toArray(new Double[0]))));	    			
    	}
    	squery.close();
    	
    	if( found == true ) {
    		tbl = (retour.toArray(new SaadaLink[retour.size()]));
    		if( sorted == true ) {
    			Arrays.sort(tbl, new SaadaLink());
    		}
    		return tbl;
    	}
    	else {
    		return new SaadaLink[0];
    	}    		
    }

    
    /* (non-Javadoc)
     * @see saadadb.api.Saada_DM_Brik#explain()
     */
    @Override
	public void explains() throws SaadaException {
    	System.out.println("Relation : " + this.name);
    	System.out.println("From " + this.getPrimaryColl().getName() + "(" + Category.explain(this.getPrimaryCategory()) 
    			           + ") To " + this.getSecondaryColl().getName() + "(" + Category.explain(this.getSecondaryCategory()) + ")");
    	System.out.print("Qualifers : ");
    	for( int i=0 ; i<this.getQualifiers().length ; i++ ) {
    		System.out.print(this.getQualifiers()[i] + " " );
    	}
    	System.out.println("");
    }
}
  
