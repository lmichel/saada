package saadadb.query.region.request;

/**
 * Class representing a Segment with only one pixel
 * @author jremy
 * @version $Id$
 */

public class Singleton extends Segment{

	/**
	 * Constructor of the Segment
	 * @param s : unique pixel of the Segment
	 */
	public Singleton(long s) {
		super(s);
	}

	/** 
	 * This method returns the String corresponding to the Segment in hexadecimal
	 * @return String
	 */
	public String toString() {
		//return ("[" + Long.toHexString(this.getStart()) + "]");
		return Long.toString(getStart());
	}


	/** 
	 * This method returns a part of the clause "WHERE" from the SQL request
	 * @param field : corresponding to the selected resolution
	 * @return String
	 */
	public String toSQL(String field) {
		return ( "OR " + field + " = " + this.getStart() + " " );
	}

	/**
	 * This method returns the first part of the clause "WHERE" from the SQL request
	 * @param field : corresponding to the selected resolution
	 * @return String
	 */
	public String toSQLfirst(String field) {
		return (" " + field+ " = " + this.getStart() + " ");
	}

	/**
	 * Method unused in Singleton
	 */
	public void fusion(Segment next) {
		
	}

	/**
	 * This method allow to set the beginning of the segment by the parameter
	 * @param s : Segment
	 */
	public void setStart(Segment s) { 
		this.start=s.start;
	}

	/**
	 * Method unused in Singleton
	 */
	public void setBit(int deg) {
	}

	/**
	 * This method allow to degrade a Singleton by setting these extremities
	 * @param deg : int (corresponding to the degrade's degree)
	 */
	public void setSingleBit(int deg) {
		long masque=((1<<2*deg)-1);
		this.setEnd(masque | this.getStart());
		masque = ~masque;
		this.setStart(masque & this.getStart());
	}

	/**
	 * Method used to compare two segments
	 * @param s : another Segment
	 * @return boolean : true if the segments are equals
	 */
	public boolean equals (Segment s) {
		if (this.getStart()==s.getStart() && this.getEnd()==s.getEnd()) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * This method allow to know if the Segment Between contains the value of the pixel
	 * @param val : double ( value of the Healpix pixel)
	 * @return boolean : true if the Segment contains the pixel
	 */
	public boolean contains (long val) {
		if (this.getStart()==val) {
			return true;
		}
		else {
			return false;
		}
	}
}
