package saadadb.dataloader.mapping;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import saadadb.command.ArgsParser;
import saadadb.exceptions.FatalException;
import saadadb.products.ProductBuilder;
import saadadb.vocabulary.enums.MappingMode;
import saadadb.vocabulary.enums.PriorityMode;

/**
 * Super class of the the classes handling the loader config for one axe (STOE)
 * The list of mapped columns match quantity but not necessarly unique fields
 * The binding between the mapped column and the real column is donw by thye class {@link ProductBuilder}
 * 
 * Each subclass correspond to one characterization axis (Obs, Space, Time, Energy)
 * 
 * @author michel
 * @version $Id$
 */
public abstract class AxisMapping {
	/**
	 * Map of the column mapping handler
	 * key: attributeName
	 * value: columnMapping
	 */
	protected Map<String, ColumnMapping> columnMapping;
	/**
	 * Priority level for the axe
	 * LAST/FIRST/ANY
	 */
	protected PriorityMode priority;
	/**
	 * Array of collection level attributes mapped 
	 */
	protected final String[] attributeNames;
	/**
	 * Look for entry command parameters  if true
	 */
	protected final boolean entryMode ;
	
	/**
	 * @param ap command line parameters
	 * @param attributesNames artray of the n ames of the collection level attributes mapped 
	 */
	AxisMapping(ArgsParser ap, String[] attributesNames, boolean entryMode) {
		this.attributeNames = attributesNames;
		this.entryMode = entryMode;
		this.columnMapping = new LinkedHashMap<String, ColumnMapping>();
		this.priority = PriorityMode.LAST;
	}
	/**
	 * Add a dummy mapping for the columns which are not mapped
	 * @throws FatalException
	 */
	protected void completeColumns() throws FatalException {
		for( String s: attributeNames) {
			if( columnMapping.get(s)  == null ) {
				this.columnMapping.put(s, new ColumnMapping(MappingMode.NOMAPPING, null, null, s));
				
			}
		}
	}
	/*
	 * getter
	 * 
	 */
	public boolean mappingFirst() {
		return (this.priority == PriorityMode.FIRST);
	}
	public boolean mappingLast() {
		return (this.priority == PriorityMode.LAST);
	}
	public boolean mappingOnly() {
		return (this.priority == PriorityMode.ONLY);
	}
	public PriorityMode getPriority() {
		return this.priority;
	}
	public Set<String> getColmunSet() {
		return this.columnMapping.keySet();
	}
	public ColumnMapping getColumnMapping(String colName){
		return this.columnMapping.get(colName);
	}
	public boolean entryMode() {
		return entryMode;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String retour = "Priority " + this.priority + "\n";
		for( Entry<String, ColumnMapping> e: this.columnMapping.entrySet() ) {
			retour += "Column " + e.getKey() + " " + e.getValue();
		}
		return retour;
	}

}
