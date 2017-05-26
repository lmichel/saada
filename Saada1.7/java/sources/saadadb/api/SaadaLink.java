package saadadb.api;

import java.util.Comparator;

import saadadb.query.matchpattern.Qualif;
import saadadb.query.parser.Operator;
import saadadb.util.Messenger;

/**
 * @author michel
 * * @version $Id$

 */
public class SaadaLink extends SaadaDMBrik implements Comparable, Comparator {
	SaadaRelation relation;
	long primary_oid=-1, secondary_oid = -1;
	double[] qualifiers;
	int sort_pos = -1;
  
	/**
	 * Just used for the comparator
	 */
	public SaadaLink(){
		super("");
		this.sort_pos = -1;
		}
	
	/**
	 * Just used for the comparator
	 * @param pos qualifer used for the sort
	 */
	public SaadaLink(int pos){
		super("");
		this.sort_pos = pos;
	}
	/**
	 * @param relation
	 * @param secondary_oid
	 * @param qualifiers
	 */
	SaadaLink(SaadaRelation relation, long primary_oid, long secondary_oid, Double[] qualifiers){
		super("");
		this.relation      = relation;
		this.primary_oid   = primary_oid;		
		this.secondary_oid = secondary_oid;		
		this.qualifiers    = new double[qualifiers.length];
		for( int i=0 ; i<qualifiers.length ; i++ ){
			this.qualifiers[i] = qualifiers[i].doubleValue();
		}
   }
	
	/**
	 * @param relation
	 * @param secondary_oid
	 * @param qualifiers
	 */
	SaadaLink(SaadaRelation relation, long primary_oid, long secondary_oid, double[] qualifiers){
		super("");
		this.relation      = relation;
		this.primary_oid   = primary_oid;		
		this.secondary_oid = secondary_oid;		
		this.qualifiers = qualifiers;
    }
	/**
	 * @return
	 */
	public long getStartingOID() {
		return this.primary_oid;
    }
    
    /**
     * @return
     */
    public long getEndindOID() {
		return this.secondary_oid;
    }
    
    /**
     * @param qualifier
     * @return
     */
    public double getQualifierValue(String qualifier) {
    	String[] qualifiers = this.relation.getQualifiers();
    	for( int i=0 ; i<this.qualifiers.length ; i++ ) {
    		if( qualifier.equals(qualifiers[i]) && this.qualifiers.length > i ) {
    			return this.qualifiers[i];
    		}
    	}
		Messenger.printMsg(Messenger.ERROR, "Can't get value for qualifier " + qualifier + " in relation " + this.relation.getName());
		return -1.;
    }
        
    /**
     * @return
     */
    public SaadaRelation getRelation() {
		return this.relation;
    }
    
    /* (non-Javadoc)
     * @see saadadb.api.Saada_DM_Brik#explains()
     */
    public void explains() {
       	String[] qualifiers = this.relation.getQualifiers();
    	System.out.println("OIDs primary: " + Long.toHexString(this.primary_oid)
    	+ " secondary: " + Long.toHexString(this.secondary_oid));
		System.out.print("Qualifiers " );
    	for( int i=0 ; i<qualifiers.length ; i++ ) {
    		if( this.qualifiers.length > i ) {
    			System.out.print(" " + qualifiers[i] + " = " + this.qualifiers[i]);
    		}
    		else {
       			System.out.print(" " + qualifiers[i] + " = not set");   			
    		}
    	}
    	System.out.println("");
    }

	/**
	 * used to sort counterparts
	 * @param arg0
	 * @return
	 */
	public int compareTo(Object arg0) {
		long poid1 = ((SaadaLink)(arg0)).getEndindOID();
		if (poid1 > this.secondary_oid) {
			return 1;
		}
		else if (poid1 == this.secondary_oid) {
			return 0;
		}
		else {
			return -1;
		}
	}

	/**
	 * used to sort links by qualifier (first -> last)
	 * @param arg0
	 * @param arg1
	 * @return
	 */
	public int compare(Object arg0, Object arg1) {
		SaadaLink l1 = (SaadaLink)arg0;
		SaadaLink l2 = (SaadaLink)arg1;
		int c1 = l1.qualifiers.length;
		if( l2.qualifiers.length < c1 ){
			c1 = l2.qualifiers.length;
		}
		if( this.sort_pos == -1 || this.sort_pos >= c1 ) {
			for( int i=0 ; i<c1 ; i++ ) {
				if( l1.qualifiers[i] > l2.qualifiers[i]) {
					return 1;
				}
				else if( l1.qualifiers[i] < l2.qualifiers[i]) {
					return -1;
				}
			}
			
			return 0;
		}
		else {
			if( l1.qualifiers[this.sort_pos] > l2.qualifiers[this.sort_pos]) {
				return 1;
			}
			else if( l1.qualifiers[this.sort_pos] < l2.qualifiers[this.sort_pos]) {
				return -1;
			}
			return 0;
		}
	}

	public boolean match(String key, Qualif value) {
		int op = value.getOp();
		double val1 = value.getVal1();
		double val2 = value.getVal1();
		int pos = 0;
		for( String q: this.relation.getQualifiers()) {
			if( q.equals(key) ) {
				return Operator.matchValue(this.qualifiers[pos], op, val1, val2);
			}
			pos++;
		}
		return false;
	}
}
  
