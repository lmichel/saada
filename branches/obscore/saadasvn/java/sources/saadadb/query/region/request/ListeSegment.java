package saadadb.query.region.request;

import java.util.ArrayList;
import java.util.Collection;


/**
 * Class can treat the Segment List and make some operations on it
 * @author jremy
 * @version $Id$
 */
public class ListeSegment {

	/**
	 * List of Segment 
	 */
	private ArrayList <Segment> List;

	/**
	 * Constructor
	 * @param array : must be ordered
	 * Initialize the Segment's list by bringing together the pixels which are near
	 * @throws Exception
	 */
	public ListeSegment (long[] array) throws Exception{
		List = new ArrayList<Segment>();
		long debut = Long.MAX_VALUE; //First pixel of a Segment
		long previous = Long.MAX_VALUE; //Last pixel examinated
		boolean depart=true; //Boolean to know if this is the beginning of the loop
		boolean enchainement=false; // Boolean to know if several pixels are following
		for(int i=0;i<array.length;i++){
			if( depart) {
				debut = array[i];
				previous=array[i];
			}
			if (!depart) {
				//if the previous pixel is right before this one
				if( array[i] == (previous + 1)) {
					enchainement=true;
				}
				else {
					//if there is several pixels which are following
					if (enchainement) {
						this.List.add(new Between(debut, previous));
						debut = array[i];
						enchainement=false;
					}
					//if there is only one pixel
					else {
						this.List.add(new Singleton(debut));
						debut = array[i];
					}
				}
				previous=array[i];
			}
			else depart=false;
		}
		//Addition of the last segment
		if (debut==previous) {
			this.List.add(new Singleton(debut));
		}
		else {
			this.List.add(new Between(debut, previous));
		}
	}

	/**
	 * Empty constructor of ListeSegment
	 */
	public ListeSegment() {
		List=new ArrayList<Segment>();
	}

	/**
	 * This method allows to modify the actual list with the array in parameter
	 * It also uses the degradeLong method to make larger segment  
	 * @throws Exception
	 */
	public void setListeDeg (int deg) throws Exception{
		this.setBetweenBit(deg);
		this.degrade();
	}

	/**
	 * This method allows to produce a smaller SQL request by gathering the close pixel
	 * It changes the attribute List of ListeSegment
	 */
	public void degrade () {
		for (int i=0;i<this.List.size()-1;i++) {
			Segment previous = this.List.get(i);
			Segment next = this.List.get(i+1);
			int count=0;
		
			//if the previous segment can merge with the next one (between - segment )
			if (previous.getEnd()+1 >= next.getStart() && previous.getEnd()!=-1) {
				previous.fusion(next);
				this.List.set(i, previous);
				this.List.remove(next);
				i--;
				count++;
			}
			// if two equals singletons are following (singleton - singleton)
			if (previous.getStart() == next.getStart() && previous.getEnd()==-1 && next.getEnd()==-1 && count==0) {
				this.List.remove(next);
				count++;
				i--;	
			}
			//if the previous singleton can merge with the next singleton, we create a Between (singleton - singleton)
			if (previous.getStart()+1 == next.getStart() && previous.getEnd()==-1 && next.getEnd()==-1 && count==0) {
				Between between = new Between(previous.getStart(),next.getStart());
				this.List.remove(next);
				this.List.remove(previous);
				List.add(i,between);
				count++;
				i--;	
			}
			//if the previous singleton can merge with the next between (singleton - between)
			if (previous.getStart()+1 >= next.getStart() && previous.getEnd()==-1 && next.getEnd()!=-1 && count==0) {
				List.remove(next);
				List.remove(previous);
				Between between = new Between(previous.getStart(),next.getEnd());
				List.add(i,between);
				i--;	
			}
		}
	}

	/**
	 * This method returns the list of Segment in String
	 * @return String
	 */
	public String toString() {
		String retour = "";
		for( Segment s: this.List){
			retour += s.toString() + " ";
		}
		return retour;
	}

	/**
	 * This method converts all the segments from the list into a clause "WHERE" from SQL request
	 * @param field : resolution
	 * @return String  : SQL request
	 */
	public String sqlString (String field) {
		String retour = "";
		for( Segment s: this.List){
			//if it's the first segment, the program calls the adequate method
			if (s.toString().equals((this.List.get(0)).toString())) {
				retour += s.toSQLfirst(field);
			}
			else {
				retour += s.toSQL(field) ;
			}
		}
		return retour;
	}

	/**
	 * This method returns the size of the List
	 * @return int : size
	 */
	public int getListSize () {
		return this.List.size();
	}

	/**
	 * This method returns the list of Segment
	 * @return ArrayList<Segment>
	 */
	public ArrayList<Segment> getList() {
		return List;
	}

	/**
	 * This method update all the Between Segment to degrade the resolution
	 * @param deg : int (corresponding to the degrade's degree)
	 */
	public void setBetweenBit(int deg) {
		for (Segment s : List) {
			s.setBit(deg);
		}
	}

	/**
	 * This method update all the Segment (including Singleton) to degrade the resolution 
	 * @param deg : int (corresponding to the degrade's degree)
	 */
	public void setAllBit(int deg) {
		ArrayList<Segment> lfs = new ArrayList<Segment>();
		for (Segment s : List) {
			if (s instanceof Singleton) {
				s.setSingleBit(deg);
			}
			else {
				s.setBit(deg);
			}
			Between b = new Between (s.getStart(),s.getEnd());
			lfs.add(b);
		}
		List.clear();
		for (int j=0;j<lfs.size();j++) {
			List.add(lfs.get(j));
		}
	}

	/**
	 * This method allows to merge the actual list with the parameter list of segment
	 * @param list : ListeSegment
	 */
	public void fusion (ListeSegment list) {
		for (Segment s : this.List) {
			for (int i=0;i<list.getListSize();i++) {
				if (s.equals(list.getList().get(i))) {
					list.getList().remove(i);
				}
			}
		}
		this.List.addAll(list.getList());
	}

	/**
	 * This method allows to know if the list of Segments contains the pixel
	 * @param val : double (value of the Hralpix pixel)
	 * @return boolean : true if the List contains the pixel
	 */
	public boolean contains (long val) {
		boolean ret=false;
		for (Segment s : this.List) {
			if (s.contains(val)) {
				ret=true;
			}
		}
		return ret;
	}

	/**
	 * This method allows to sort the actual List of Segment
	 */
	public void sort () {
		ArrayList<Segment> ret = new ArrayList<Segment>();
		ArrayList<Segment> array = new ArrayList<Segment>(this.List);
		for (int i=0;i<this.List.size();i++) {
			long min=Long.MAX_VALUE;
			int index= Integer.MAX_VALUE;
			Segment s=null;
			for (int j=0;j<array.size();j++) {
				if (array.get(j).getStart()<min) {
					min=array.get(j).getStart();
					index=j;
					s=array.get(j);
				}
			}
			if (!ret.contains(s)) {
				ret.add(s);
			}
			array.remove(index);
		}
		this.setSegmentList(ret);
	}

	/**
	 * This method allows to set the actual segmentList
	 * @param collection : ListeSegment
	 */
	public void setSegmentList (Collection<Segment> collection) {
		this.List=new ArrayList<Segment>(collection);
	}
}




