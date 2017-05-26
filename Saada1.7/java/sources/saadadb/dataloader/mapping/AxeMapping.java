package saadadb.dataloader.mapping;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import saadadb.command.ArgsParser;
import saadadb.exceptions.FatalException;

public abstract class AxeMapping {
	protected Map<String, ColumnMapping> columnMapping;
	protected Priority priority;
	protected final String[] attributeNames;
	
	AxeMapping(ArgsParser ap, String[] attributesNames) {
		this.attributeNames = attributesNames;
		this.columnMapping = new LinkedHashMap<String, ColumnMapping>();
		this.priority = Priority.LAST;
	}
	/**
	 * Add a dummy mapping for the columns which are not mapped
	 * @throws FatalException
	 */
	protected void completeColumns() throws FatalException {
		for( String s: attributeNames) {
			if( columnMapping.get(s)  == null ) {
				this.columnMapping.put(s, new ColumnMapping(MappingMode.NOMAPPING, null, null));
				
			}
		}
	}

	public boolean mappingFirst() {
		return (priority == Priority.FIRST);
	}
	public boolean mappingLast() {
		return (priority == Priority.LAST);
	}
	public boolean mappingOnly() {
		return (priority == Priority.ONLY);
	}

	public String toString() {
		String retour = "Priority " + this.priority + "\n";
		for( Entry<String, ColumnMapping> e: this.columnMapping.entrySet() ) {
			retour += "Column " + e.getKey() + " " + e.getValue();
		}
		return retour;
	}

}
