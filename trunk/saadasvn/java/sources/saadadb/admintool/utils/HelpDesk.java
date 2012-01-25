package saadadb.admintool.utils;

import java.util.LinkedHashMap;
import java.util.Map;


public class HelpDesk {
	public static final Map<Integer, String[]> map;
	public static final int EXTATT_MISSING = 1;
	static{
		map = new LinkedHashMap<Integer, String[]>();
		map.put(EXTATT_MISSING, new String[] {"No extended attribute."
				                            , "Extended attributes must be set at DB creation time"
				                            , "They can no longer be added after that"
				                            });
	}
	
	/**
	 * @param key
	 * @return
	 */
	public static final String[] get(int key){
		return map.get(key);
	}

}
