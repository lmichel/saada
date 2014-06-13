package saadadb.products.ppknowledge;

import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.util.Messenger;
import java.lang.reflect.Type;
import static java.lang.System.out;


/**
 * @author michel
 * @version $Id$
 */
@SuppressWarnings("rawtypes")
public class KnowledgeBase {

	public static final Map<PipelineKey, Class> parserMap;
	private static final Set<String> pipelineIdentifiers ;

	static {
		pipelineIdentifiers = new HashSet<String>();
		pipelineIdentifiers.add("ORIGIN");

		parserMap = new LinkedHashMap<PipelineKey, Class>();
		parserMap.put(new PipelineKey("ORIGIN", ".*Grenoble.*"), Grenoble.class);
	}
	/**
	 * @param attributesHandlers
	 * @return
	 */
	@SuppressWarnings({ "unchecked" })
	public static final PipelineParser getParser(Map<String, AttributeHandler> attributesHandlers) throws SaadaException{
		for( AttributeHandler ah: attributesHandlers.values()) {
			for( Entry<PipelineKey, Class> pke: parserMap.entrySet()){
				PipelineKey pk = pke.getKey();
				if( pk.keyword.equalsIgnoreCase(ah.getNameorg()) && ah.getValue().matches(pk.regexp) ) {
					try {
						Messenger.printMsg(Messenger.TRACE, "Product identified within the knowledge base as a " + pke.getValue().getName());
						return (PipelineParser) pke.getValue().getDeclaredConstructor(Map.class).newInstance(attributesHandlers);
					} catch (Exception e) { 
						FatalException.throwNewException(SaadaException.INTERNAL_ERROR, e);
					}
				}
			}
		}
		return null;
	}
	/**
	 * @param attributesHandlers
	 * @param entryAttributeHandlers
	 * @return
	 */
	public static final PipelineParser getParser(Map<String, AttributeHandler> attributesHandlers, Map<String, AttributeHandler> entryAttributeHandlers){
		return null;
	}

}
