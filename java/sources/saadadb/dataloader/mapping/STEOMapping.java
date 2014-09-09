package saadadb.dataloader.mapping;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import saadadb.collection.Category;
import saadadb.command.ArgsParser;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.util.Merger;
import saadadb.util.Messenger;

/**
 * @author michel
 * @version $Id$
 *
 */
public class STEOMapping {
	private Map<Axis, AxisMapping> axeMapping;
	public final int category;
	public final String collection;
	private ArrayList<String> ignoredAttributes;
	
	/**
	 * @param ap
	 * @throws FatalException
	 */
	STEOMapping(ArgsParser ap, boolean entryMode) throws SaadaException {
		this.axeMapping = new LinkedHashMap<Axis, AxisMapping>();
		this.axeMapping.put(Axis.SPACE      , new SpaceMapping(ap, entryMode));
		this.axeMapping.put(Axis.TIME       , new TimeMapping(ap, entryMode));
		this.axeMapping.put(Axis.ENERGY     , new EnergyMapping(ap, entryMode));
		this.axeMapping.put(Axis.OBSERVATION, new ObservationMapping(ap, entryMode));
		this.axeMapping.put(Axis.OBSERVABLE , new ObservableMapping(ap, entryMode));
		this.axeMapping.put(Axis.POLARIZATION , new PolarMapping(ap, entryMode));
		this.category = Category.getCategory(ap.getCategory());
		this.axeMapping.put(Axis.EXTENDEDATT, new ExtendedAttMapping(ap, entryMode));
		this.collection = ap.getCollection();
		this.setIgnoredAttributes(ap,entryMode);
	}
	
	/**
	 * @param ap
	 * @param entryMode
	 */
	private void setIgnoredAttributes(ArgsParser ap, boolean entryMode){
		this.ignoredAttributes   = new ArrayList<String>();
		String[]  tabIgnored = ap.getIgnoredAttributes(entryMode);		
		for( int j=0; j<tabIgnored.length; j++) {
			/*
			 * Ignored attribute can contain UNIX wild cards '*' which must be replaced 
			 * with RegExp wildcards '.*'
			 */
			this.ignoredAttributes.add(tabIgnored[j].trim().replaceAll("\\*", ".*"));
		}
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "The ignored attributes are : "+ this.ignoredAttributes);    	
		
	}
	
	/**
	 * @return
	 */
	public List<String> getIgnoredAttributes() {
		return this.ignoredAttributes;
	}
	/**
	 * @param axe
	 * @return
	 * @throws FatalException
	 */
	public AxisMapping getAxisMapping(Axis axe) throws FatalException{
		if( axe == Axis.EXTENDEDATT && this.axeMapping.get(Axis.EXTENDEDATT) == null ) {
			FatalException.throwNewException(SaadaException.WRONG_PARAMETER, "No extended attributes in this DB:  no mapping available");
		}
		return this.axeMapping.get(axe);
	}
	
	/**
	 * @return
	 */
	public String getSignature() {
		StringBuffer retour = new StringBuffer();
		retour.append(this.category);
		retour.append(Merger.getMergedCollection(this.ignoredAttributes).replaceAll("[, ] " , ""));
		for(AxisMapping am: this.axeMapping.values() ){
			am.toString().replaceAll("\\n" , "").replaceAll("[, ] " , "");
		}
		return retour.toString();
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("* Ignored att :");
		sb.append("\n");
		for( String ia: ignoredAttributes) sb.append(ia + " ");
		for( Entry<Axis, AxisMapping>e: this.axeMapping.entrySet()){
			sb.append("* Axe " + e.getKey() + "\n" + e.getValue());
		}
		return sb.toString();
	}
}
