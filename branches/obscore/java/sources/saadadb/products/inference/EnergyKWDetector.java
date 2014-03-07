package saadadb.products.inference;

import java.util.Map;

import saadadb.meta.AttributeHandler;

public class EnergyKWDetector extends KWDetector {

	public EnergyKWDetector(Map<String, AttributeHandler> tableAttributeHandler) {
		super(tableAttributeHandler);
		// TODO Auto-generated constructor stub
	}
	public EnergyKWDetector(Map<String, AttributeHandler> tableAttributeHandler, Map<String, AttributeHandler> entryAttributeHandler) {
		super(tableAttributeHandler, entryAttributeHandler);
	}
	public AttributeHandler getEminAttribute(){
		return null;
	}
	public AttributeHandler getEmaxAttribute(){
		return null;
	}
	public AttributeHandler getUnitAttribute(){
		return null;
	}
	public AttributeHandler getColumnName(){
		return null;
	}

}
