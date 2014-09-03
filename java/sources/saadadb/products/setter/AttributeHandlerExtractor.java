package saadadb.products.setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import saadadb.meta.AttributeHandler;
import saadadb.util.RegExp;

/**
 * Extract a list of Attributes handler from an expression
 * @author pertuy
 * @version $Id$
 */
public class AttributeHandlerExtractor {
	protected List<AttributeHandler> extractAttributes;
	protected String expression;
	protected TreeMap<String,AttributeHandler> orderedAttributeMap;

	public AttributeHandlerExtractor(String expr,Map<String,AttributeHandler> attributesHandler)
	{
		expression=expr;
		orderedAttributeMap = new TreeMap<String,AttributeHandler>();
		/*
		 * We order every Attribute Handler by size(name) >
		 * To do that, we create a tree map having an int as key  
		 */
		if(attributesHandler != null) {
			for(Entry<String,AttributeHandler> e:attributesHandler.entrySet())
			{
				orderedAttributeMap.put(10000-e.getValue().getNameattr().length()+e.getValue().getNameattr(), e.getValue());
			}
		}


	}

	/**
	 * Extract the AH contained in the expression
	 * @return the List of extract AH
	 */
	public List<AttributeHandler> extractAH()
	{
		Pattern keywordPattern = Pattern.compile(RegExp.KEYWORD);
		Matcher matcher= keywordPattern.matcher(expression);
		extractAttributes = new ArrayList<AttributeHandler>();
		String extractedString = "";

		//For each extract value of the expression we look if an AttributeHandler correspond
		while(matcher.find())
		{
			extractedString = matcher.group(1).trim();
			//System.out.println(extractedString);
			for(Entry<String,AttributeHandler> e:orderedAttributeMap.entrySet())
			{
				if((extractedString.equals(e.getValue().getNameattr())) ||(extractedString.equals(e.getValue().getNameorg())))
				{
					//We modify the expression if an old value is in it
					if(extractedString.equals(e.getValue().getNameorg()))
						expression=expression.replace(e.getValue().getNameorg(), e.getValue().getNameattr());
					extractAttributes.add(e.getValue());
				}

			}
		}

		return extractAttributes;
	}

}
