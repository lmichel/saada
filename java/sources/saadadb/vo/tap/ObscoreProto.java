package saadadb.vo.tap;

import java.util.ArrayList;
import java.util.Collection;

import saadadb.database.Database;
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
	}

}
