package saadadb.dataloader.mapping;

import java.util.LinkedHashMap;
import java.util.Map;

import saadadb.command.ArgsParser;

public abstract class AxeMapping {
	private String name;
	private Map<String, ColumnMapping> columnMapping;
	private Priority priority;
	protected final String[] attributeNames;
	
	AxeMapping(ArgsParser ap, String[] attributesNames) {
		this.attributeNames = attributesNames;
		this.columnMapping = new LinkedHashMap<String, ColumnMapping>();
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

}
