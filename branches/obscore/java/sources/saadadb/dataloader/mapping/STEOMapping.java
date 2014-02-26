package saadadb.dataloader.mapping;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import saadadb.collection.Category;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
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
	private Map<Axe, AxeMapping> axeMapping;
	public final int category;
	public final String collection;
	private ArrayList<String> ignoredAttributes;
	
	/**
	 * @param ap
	 * @throws FatalException
	 */
	STEOMapping(ArgsParser ap, boolean entryMode) throws SaadaException {
		this.axeMapping = new LinkedHashMap<Axe, AxeMapping>();
		this.axeMapping.put(Axe.SPACE      , new SpaceMapping(ap, entryMode));
		this.axeMapping.put(Axe.TIME       , new TimeMapping(ap, entryMode));
		this.axeMapping.put(Axe.ENERGY     , new EnergyMapping(ap, entryMode));
		this.axeMapping.put(Axe.OBSERVATION, new ObservationMapping(ap, entryMode));
		this.category = Category.getCategory(ap.getCategory());
		if( Database.getCachemeta().getAtt_extend(this.category).size() > 0 ){
			this.axeMapping.put(Axe.EXTENDEDATT, new ExtendedAttMapping(ap, entryMode));
		} else {
			this.axeMapping = null;
		}
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
	public AxeMapping getAxeMapping(Axe axe) throws FatalException{
		if( axe == Axe.EXTENDEDATT && this.axeMapping.get(Axe.EXTENDEDATT) == null ) {
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
		for(AxeMapping am: this.axeMapping.values() ){
			am.toString().replaceAll("\\n" , "").replaceAll("[, ] " , "");
		}
		return retour.toString();
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("* Ignored att :");
		sb.append("\n");
		for( String ia: ignoredAttributes) sb.append(ia + " ");
		for( Entry<Axe, AxeMapping>e: this.axeMapping.entrySet()){
			sb.append("* Axe " + e.getKey() + "\n" + e.getValue());
		}
		return sb.toString();
	}
}
