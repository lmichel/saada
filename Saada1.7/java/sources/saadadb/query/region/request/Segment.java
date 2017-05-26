package saadadb.query.region.request;

/**
 * Class abstract represting a segment of Healpix's pixel
 * @author jremy
 * @version $Id$
 */
public abstract class Segment {

	/**
	 * Represents the beginning of the segment 
	 */
	protected long start;

	/**
	 * Represents the ending of the segment
	 */
	protected long end;

	/**
	 * Constructor ( Singleton ) 
	 * @param s : long
	 */
	public Segment (long s) {
		this.setStart(s);
		this.setEnd(-1);
	}

	/**
	 * Constructor ( Between ) 
	 * Several pixels
	 * @param s : long
	 * @param e : long
	 */
	public Segment(long s, long e) {
		this.setStart(s);
		this.setEnd(e);
	}

	/**
	 * This method returns the String corresponding to the Segment
	 * @return String
	 */
	public abstract String toString();

	/**
	 * This method returns a part of the clause "WHERE" from the SQL request
	 * @param field : corresponding to the selected resolution
	 * @return String
	 */
	public abstract String toSQL(String field);	

	/**
	 * This method returns the first part of the clause "WHERE" from the SQL request
	 * @param field : corresponding to the selected resolution
	 * @return String
	 */
	public abstract String toSQLfirst(String field);

	/**
	 * Method used to merge two segments
	 * @param next : Segment
	 */
	public abstract void fusion (Segment next);

	/**
	 * Method used in Between
	 * @param deg : int
	 */
	public abstract void setBit (int deg);

	/**
	 * Method used in Singleton
	 * @param deg : int
	 */
	public abstract void setSingleBit (int deg);

	/**
	 * Method used in Between
	 * @param s : Segment
	 */
	public abstract void setStart(Segment s);

	/**
	 * Method used to compare two segments
	 * @param s : another Segment
	 * @return boolean : true if the segments are equals
	 */
	public abstract boolean equals(Segment s);

	/**
	 * Method used to know if a segment contains a pixel
	 * @param val : double (value of the Healpix pixel)
	 * @return boolean : true if the segment contains the pixel
	 */
	public abstract boolean contains (long val);

	/**
	 * This method allows to get the start of the segment
	 * @return the start
	 */
	public long getStart() {
		return start;
	}

	/**
	 * This method allows to set the start of the segment
	 * @param start the start to set
	 */
	public void setStart(long start) {
		this.start = start;
	}

	/**
	 * This method allows to get the end of the segment
	 * @return the end
	 */
	public long getEnd() {
		return end;
	}

	/**
	 * This method allows to set the end of the segment
	 * @param end the end to set
	 */
	public void setEnd(long end) {
		this.end = end;
	}

}


