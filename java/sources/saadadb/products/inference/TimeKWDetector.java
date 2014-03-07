package saadadb.products.inference;

import java.util.Map;

import saadadb.meta.AttributeHandler;

public class TimeKWDetector extends KWDetector {

	public TimeKWDetector(Map<String, AttributeHandler> tableAttributeHandler) {
		super(tableAttributeHandler);
		// TODO Auto-generated constructor stub
	}
	public TimeKWDetector(Map<String, AttributeHandler> tableAttributeHandler, Map<String, AttributeHandler> entryAttributeHandler) {
		super(tableAttributeHandler, entryAttributeHandler);
	}

	public AttributeHandler getTminName(){
		return null;
	}
	public AttributeHandler getTmaxName(){
		return null;
	}
	public AttributeHandler getExposureName(){
		return null;
	}

}
