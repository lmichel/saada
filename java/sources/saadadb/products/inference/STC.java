/**
 * 
 */
package saadadb.products.inference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import saadadb.database.Database;
import cds.astro.Astroframe;

/**
 * This class parses the input string to model an STC region.
 * The String must like [SHAPE] [FRAME] [Vertex]
 *              or like [SHAPE] [Vertex]
 *              or like [Vertex]
 * If the frame is not set, the default one is take.
 * If it is not recognized, the parsing remain in failure
 * 
 * Forced to Polygon right now
 * @author michel
 *
 */
public class STC {
	/**
	 * Input String, kept for messaging
	 */
	private String stcString;
	/**
	 * List of coordinates (suposed to be even)
	 */
	private List<Double> coords = null;
	/**
	 * Region frame, the default is taken if not in the STC string
	 */
	private Astroframe astroFrame;
	/**
	 * Region shape, forced to Polygon
	 */
	private String type;
	/**
	 * used to feed loggers
	 */
	public String message ="";
	
	/**
	 * @param stcString
	 */
	public STC(String stcString) {
		super();
		this.stcString = stcString;
		this.extractComponents();
	}

	/**
	 * Parses the input String and populate the internal model
	 * In case of failure the coord list remains null
	 */
	private void extractComponents(){
		@SuppressWarnings("rawtypes")
		List<String> lComps = new ArrayList(Arrays.asList(this.stcString.split("\\s")));
		String first = 	lComps.get(0);
		/*
		 * First element not a number: must be the region shape (Polygon...)
		 * TODO Vocabulary check, forced to Polygon meanwhile
		 */
		if( !first.matches("-?\\d+(\\.\\d+)?") ) {
			this.type = "Polygon";
			lComps.remove(0);
		} else {
			this.type = "Polygon";
		}
		/*
		 * Second element not a number: must be the frame
		 * If no frame: take the current one
		 * If frame not recognized: failed
		 * TODO Vocabulary check
		 */
		first = 	lComps.get(0);
		if( !first.matches("-?\\d+(\\.\\d+)?") ) {
			CooSysResolver cooSysResolver;
			try {
				cooSysResolver = new CooSysResolver(first);
				this.astroFrame = cooSysResolver.getCooSys();
				if( this.astroFrame == null ){
					this.message = first + " cannot be interpreted as an astroframe";
					return;
				}
				lComps.remove(0);
			} catch (Exception e) {
				this.message = e.getMessage();
				return;			
			}		
		} else {
			this.astroFrame = Database.getAstroframe();
		}
		/*
		 * Other elements must be vertex coordinates
		 */
		int size = lComps.size();
		if( (size  % 2) != 0  ){
			this.message = "Number of coordinates (" +  size + ") must be even";
			return;
		} else if( size <  4  ){
			this.message = "Number of coordinates (" +  size + ") mst be greater or equals to 4";
			return;
		} else {
			this.coords = new ArrayList<Double>();
			try {
				for( int i=0 ; i<size ; i++ ) {
					this.coords.add(Double.parseDouble(lComps.get(i)));
				}
			} catch(NumberFormatException nfe) {
				this.message = "Coordinates must be numerics";
			}
		}

	}

	/**
	 * @return the coordinate list or null
	 */
	public List<Double> getCoords() {
		return coords;
	}

	/**
	 * @return the astroframe list or null
	 */
	public Astroframe getAstroFrame() {
		return (isValid())? astroFrame: null;
	}

	/**
	 * @return the region shape list or null
	 */
	public String getType() {
		return (isValid())? type: null;
	}
	
	/**
	 * @return true if the parsing succeeded
	 */
	public boolean isValid(){
		return (this.coords == null)? false: true;
	}

}
