package saadadb.products.inference;

import java.util.Map;

import saadadb.meta.AttributeHandler;

public class ObservationKWDetector extends KWDetector {

	public ObservationKWDetector(
			Map<String, AttributeHandler> tableAttributeHandler) {
		super(tableAttributeHandler);
	}
	public ObservationKWDetector(Map<String, AttributeHandler> tableAttributeHandler, Map<String, AttributeHandler> entryAttributeHandler) {
		super(tableAttributeHandler, entryAttributeHandler);
	}
	
	public AttributeHandler getCollNameAttribute(){
		return null;
	}
	public AttributeHandler getTargetAttribute(){
		return null;
	}
	public AttributeHandler getFacilityAttribute(){
		return null;
	}
	public AttributeHandler getAttribute(){
		return null;
	}
	public AttributeHandler getInstrumentAttribute(){
		return null;
	}

}
