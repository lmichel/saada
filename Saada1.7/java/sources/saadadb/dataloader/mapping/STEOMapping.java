package saadadb.dataloader.mapping;

import java.util.LinkedHashMap;
import java.util.Map;

import saadadb.collection.Category;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;

/**
 * @author michel
 * @version $Id$
 *
 */
public class STEOMapping {
	private Map<Axe, AxeMapping> axeMapping;
	private STEOMapping entryMapping;
	public final int category;
	public final String collection;
	
	/**
	 * @param ap
	 * @throws FatalException
	 */
	STEOMapping(ArgsParser ap, boolean entryMode) throws FatalException {
		this.axeMapping = new LinkedHashMap<Axe, AxeMapping>();
		this.axeMapping.put(Axe.SPACE, new SpaceMapping(ap));
		this.axeMapping.put(Axe.TIME, new TimeMapping(ap));
		this.axeMapping.put(Axe.ENERGY, new EnergyMapping(ap));
		this.axeMapping.put(Axe.OBSERVATION, new ObservationMapping(ap));
		this.category = Category.getCategory(ap.getCategory());
		if( Database.getCachemeta().getAtt_extend(this.category).size() > 0 ){
			this.axeMapping.put(Axe.EXTENDEDATT, new ExtendedAttMapping(ap));
		} else {
			this.axeMapping = null;
		}
		this.collection = ap.getCollection();
		if( this.category == Category.TABLE){
			this.entryMapping = new STEOMapping(ap, true);
		}
	}
	
	/**
	 * @param axe
	 * @return
	 * @throws FatalException
	 */
	public AxeMapping getAxeMapping(Axe axe) throws FatalException{
		if( axe == Axe.EXTENDEDATT && this.axeMapping.get(Axe.EXTENDEDATT) == null ) {
			FatalException.throwNewException(SaadaException.WRONG_PARAMETER, "No extended attributesin this DB:  no mapping available");
		}
		return this.axeMapping.get(axe);
	}
	
	/**
	 * @return
	 * @throws FatalException
	 */
	public STEOMapping getEntryMapping() throws FatalException {
		if( this.category == Category.TABLE){
			return this.entryMapping;
		} else {
			FatalException.throwNewException(SaadaException.WRONG_PARAMETER, "No entry mapping for " + Category.explain(this.category));
		}
		return null;
	}

}
