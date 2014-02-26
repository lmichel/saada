package saadadb.query.region.request;

/**
 * Class representing a segment with two extremities pixels
 * @author jremy
 * @version $Id$
 */
public class Between extends Segment{

	/**
	 * Constructor of the Segment
	 * @param s : start
	 * @param e : end 
	 */
	public Between(long s, long e) {
		super(s);
		this.setEnd(e);
	}

	/** 
	 * This method returns the String corresponding to the Segment in hexadecimal
	 * @return String 
	 */
	public String toString() {
		return ( "[" + Long.toHexString(this.getStart()) + "-" +  Long.toHexString(this.getEnd())+ "]");
	}


	/** 
	 * This method returns a part of the clause "WHERE" from the SQL request
	 * @param field : corresponding to the selected resolution
	 * @return String
	 */
	public String toSQL(String field) {

		return ("OR "+field+ " BETWEEN " + this.getStart() + " AND " + this.getEnd() +" ");
	}
	/**
	 * This method returns the first part of the clause "WHERE" from the SQL request
	 * @param field : corresponding to the selected resolution
	 * @return String
	 */
	public String toSQLfirst(String field) {

		return (" "+field+ " BETWEEN " + this.getStart() + " AND " + this.getEnd() +" ");
	}

	/**
	 * This method allows to merge two segments
	 * @param next : Segment
	 */
	public void fusion (Segment next) {
		long replace=Long.MAX_VALUE;
		if (this.getEnd()>next.getStart()) {
			if (next.getEnd()>this.getEnd()) {
				replace=next.getEnd();
			}
			else {
				replace=this.getEnd();
			}
		}
		else {
			if (next instanceof Between) {
				replace=next.getEnd();
			}
			else {
				replace=next.getStart();
			}
		}
		this.setEnd(replace);
	}


	/**
	 * This method allows to set the beginning and the ending of the segment to degrade the resolution
	 * @param deg : int (corresponding to the degrade's degree)
	 */
	public void setBit(int deg) {
		this.setStart(this.degradeLongMin(deg));
		this.setEnd(this.degradeLongMax(deg));
	}


	/**
	 * This method allows to set the beginning of the segment by the parameter
	 * @param s : Segment
	 */
	public void setStart(Segment s) { 
		this.start=s.start;
	}

	/**
	 * This method returns the pixel from the lower resolution than currently
	 * @param deg : corresponding to the number of degradation ( proportional to the resolution )
	 * @return long
	 */
	public long degradeLongMax (int deg) {
		long masque=((1<<2*deg)-1);
		return masque | this.getEnd();
	}

	/**
	 * This method returns the pixel from the lower resolution than currently
	 * @param deg : corresponding to the number of degradation ( proportional to the resolution )
	 * @return long
	 */
	public long degradeLongMin (int deg) {
		long masque=((1<<2*deg)-1);
		masque = ~masque;
		return masque & this.getStart();
	}

	/**
	 * Method unused in Between
	 */
	public void setSingleBit(int deg) {
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
	 * This method allows to know if the Segment Between contains the value of the pixel
	 * @param val : double ( value of the Healpix pixel)
	 * @return boolean : true if the Segment contains the pixel
	 */
	public boolean contains (long val) {
		if (this.getStart() <= val && this.getEnd() >= val) {
			return true;
		}
		else {
			return false;
		}
	}
}
