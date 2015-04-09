package saadadb.vo.tap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import saadadb.collection.Category;
import saadadb.database.Database;
import saadadb.meta.AttributeHandler;
import saadadb.meta.MetaCollection;
import saadadb.meta.UTypeHandler;
import saadadb.meta.VOResource;
import saadadb.sqltable.Table_Saada_VO_Capabilities;
import saadadb.vo.registry.Capability;
import saadadb.vocabulary.enums.VoProtocol;

public class ObscoreProto {

	public static void main(String[] args) throws Exception{
		Database.init("Obscore");
		Collection<Capability> capabilities = new ArrayList<Capability>();
		Table_Saada_VO_Capabilities.loadCapabilities(capabilities, VoProtocol.TAP);
		for( Capability cap: capabilities){
			System.out.println(cap);
		}
		System.out.println("=======================================");
		VOResource vor = VOResource.getResource("Obscore");
		List<UTypeHandler> uths = vor.getUTypeHandlers();
		Set<String> ahnames = new TreeSet<String>();
		for(UTypeHandler uth: uths ){
			if(uth.isMandatory()){
				System.out.println(uth.getNickname() + uth.isMandatory());
				ahnames.add(uth.getNickname());
			} else {
				System.out.println("==========false");
			}
		}
		System.out.println("=======================================");
		Map<String, AttributeHandler> ahs = MetaCollection.getAttribute_handlers(Category.SPECTRUM);
		for( AttributeHandler ah: ahs.values() ){
			if( ahnames.contains( ah.getNameattr())) {
				System.out.println( ah.getNameattr() + " connu");
			} else {
				System .out.println(ah.getNameattr() + " inconnu");
			}
		}
		System.out.println("=======================================");
		for( String uhn: ahnames){
			if( ahs.keySet().contains(uhn)) {
				System.out.println( uhn + " connu");

			}else {
				System .out.println(uhn + " inconnu");
			}

		}
	}

}
