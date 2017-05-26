package saadadb.relationship;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import saadadb.database.Database;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.query.parser.UnitHandler;
import saadadb.util.Messenger;
import cds.astro.Unit;

/**
 * @author laurentmichel
 * * @version $Id$

 */
public final class RelationUCDsManager {

	/*class
	ucd
	attribute
	alias*/

	private final Map<String,Set<AttributeHandler>> classAttr = new HashMap<String,Set<AttributeHandler>>();
	private final List<CorrQueryTranslator.UcdCond> ucdCondList;

	public RelationUCDsManager(List<CorrQueryTranslator.UcdCond> ucdCondList,String collName,int category) throws SaadaException{
		this.ucdCondList = ucdCondList;
		for(String className:Database.getCachemeta().getClassesOfCollection(collName, category)){
			for(AttributeHandler ah:Database.getCachemeta().getClassAttributes(className)){
				for(CorrQueryTranslator.UcdCond ucdc:ucdCondList){
					if(ah.isQueriable() && ah.getUcd().trim().equals(ucdc.getUcd())){
						if(ucdc.getUnit().equals("none")){
							put(this.classAttr,className,ah);
						}else {
							try {
								Unit query = new Unit(ucdc.getUnit());
								Unit base = new Unit(ah.getUnit());
								if(base.isCompatibleWith(query)){
									put(this.classAttr,className,ah);
								}
							} catch (ParseException e){
								Messenger.printMsg(Messenger.WARNING,"Unit \""+ucdc.getUnit()+"\" of the query and unit \""+ah.getUnit()+"\" of the classe \""+ah.getClassname()+"\" are incompatibles! The attribute \""+ah.getNameattr()+"\" is ignored!");
							}
						}
					}
				}
			}
		}	
	}
	private static final void put(Map<String,Set<AttributeHandler>> map,String className,AttributeHandler ah){
		if(map.containsKey(className)){
			map.get(className).add(ah);
		}else{
			Set<AttributeHandler> alAH = new HashSet<AttributeHandler>();
			alAH.add(ah);
			map.put(className,alAH);
		}
	}



	public final List<String> classNamesHavingAllUCDs(String[] listClass){
		List<String> res = new ArrayList<String>();
		if(listClass!=null && listClass.length>0){
			if(this.classAttr.isEmpty()) return res;
			if(listClass.length==1 && listClass[0].equals("*")){
				for(Map.Entry<String,Set<AttributeHandler>> me:this.classAttr.entrySet()){
					if(hasAllUCDs(this.ucdCondList,me.getValue())) res.add(me.getKey());
				}
			}else{
				for(Map.Entry<String,Set<AttributeHandler>> me:this.classAttr.entrySet()){
					if(isIn(me.getKey(),listClass) && hasAllUCDs(this.ucdCondList,me.getValue())) res.add(me.getKey());
				}
			}
		}
		return res;
	}
	private static final boolean hasAllUCDs(List<CorrQueryTranslator.UcdCond> ucdCondList,Set<AttributeHandler> set){
		for(CorrQueryTranslator.UcdCond ucdc:ucdCondList) if(!hasUCD(ucdc.getUcd(),set))return false;
		return true;
	}
	private static final boolean isIn(String s,String[] tab){
		for(String str:tab)if(str.equals(s))return true;
		return false;
	}
	private static final boolean hasUCD(String ucd,Set<AttributeHandler> set){
		for(AttributeHandler ah:set) if(ucd.equals(ah.getUcd())) return true;
		return false;
	}

	/*public final String[] getAttributeList(String className,List<RelationCreator.UcdCond> ucdList){
		Set<String> ss = new LinkedHashSet<String>();
		for(AttributeHandler ah:this.classAttr.get(className)){
			for(RelationCreator.UcdCond ucdc:ucdList){
				if(ah.getUcd().equals(ucdc.getUcd())) ss.add(ah.getNameattr());
			}
		}
		return (String[])ss.toArray(new String[0]);
	}
	public final String[] getAttributeListWithFunction(String className) throws SaadaException{
		Set<String> ss = new LinkedHashSet<String>();
		for(AttributeHandler ah:this.classAttr.get(className)){
			ss.add(UCDsUnitManager.getConvFunction(this.mapUcdUcdc.get(ah.getUcd()).getUnit(),ah.getUnit(),ah.getNameattr()));
		}
		return (String[])ss.toArray(new String[0]);
	}*/
	
	public final Set<String> getAliases(){
		final Set<String> ss = new LinkedHashSet<String>();
		for(CorrQueryTranslator.UcdCond ucdc:this.ucdCondList)	ss.add(ucdc.getSubstitute());
		return ss;
	}
	
	public final Set<String> getAttributeListWithFunctionAndAlias(String className) throws SaadaException{
		final Set<String> ss = new LinkedHashSet<String>();
		for(CorrQueryTranslator.UcdCond ucdc:this.ucdCondList){
			AttributeHandler ah= getFirstCompatibleWith(className,ucdc);
			ss.add(UnitHandler.getConvFunction(ucdc.getUnit(),ah.getUnit(),ah.getNameattr())+" as "+ucdc.getSubstitute());
		}
		return ss;
	}
	
	private final AttributeHandler getFirstCompatibleWith(String className,CorrQueryTranslator.UcdCond ucdc){
		for(AttributeHandler ah:this.classAttr.get(className)){
			if(ah.getUcd().equals(ucdc.getUcd())) return ah;
		}
		return null;
	}
	

}
