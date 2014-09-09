package saadadb.products.inference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import saadadb.meta.AttributeHandler;
import saadadb.products.setter.ColumnExpressionSetter;
import saadadb.util.Messenger;

/**

 * @author michel
 * @version $Id$
 */
public class PolarizationKWDetector extends KWDetector {
	public List<String> comments;
	/**
	 * Quantities are bound each to other. they are set together and returned by accessors
	 */
	private ColumnExpressionSetter states=new ColumnExpressionSetter("pol_states");
	
	public PolarizationKWDetector(Map<String, AttributeHandler> tableAttributeHandler, List<String> comments) {
		super(tableAttributeHandler);
		this.comments = (comments == null)? new ArrayList<String>(): comments;
	}
	public PolarizationKWDetector(Map<String, AttributeHandler> tableAttributeHandler
			, Map<String, AttributeHandler> entryAttributeHandler, List<String> comments) {
		super(tableAttributeHandler, entryAttributeHandler);
		this.comments = (comments == null)? new ArrayList<String>(): comments;
	}
	


	/**
	 * @return
	 * @throws Exception 
	 */
	public ColumnExpressionSetter getPolarizationStates() throws Exception{
		if( Messenger.debug_mode ) 
			Messenger.printMsg(Messenger.DEBUG, "Search for the pol states: not implemented yet");
		return states;
	}

}
