package saadadb.query.matchpattern;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.TreeSet;

import saadadb.meta.MetaClass;

/**
 * Store the stuff needed by the GUI to display counterpart matching a relation pattern
 * @author michel
 * * @version $Id: CounterpartSelector.java 118 2012-01-06 14:33:51Z laurent.mistahl $

 */
public class CounterpartSelector implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String cp_query;
	private LinkedHashMap<String, Qualif> qualif_query;
	
	private  TreeSet<String> metaClassTab = new TreeSet<String>(); 


	/**
	 * @param matchPatternSQLs
	 * @param qt
	 */
	public CounterpartSelector(EPOnePattern epone, Qualif[] qt) {
		if( epone != null ) {
			this.cp_query = epone.getCounterPartsSQL();
			MetaClass[] cls = epone.getMetaClassTab();
			if( cls != null ) {
				for( MetaClass cl: cls) this.metaClassTab.add(cl.getName());
			}
		}
		else {
			this.cp_query = "";		
		}
		this.qualif_query = new LinkedHashMap<String, Qualif>();
		if( qt != null ) {
			for( Qualif q: qt) {
				this.qualif_query.put(q.getName(), q);			
			}
		}
	}


	/**
	 * @return Returns the cp_query.
	 */
	public String getCp_query() {
		return cp_query;
	}


	/**
	 * @return Returns the qualif_query.
	 */
	public LinkedHashMap<String, Qualif> getQualif_query() {
		return qualif_query;
	}

	/**
	 * @return
	 */
	public TreeSet<String> getMetaClassTab() {
		return metaClassTab;
	}

}
