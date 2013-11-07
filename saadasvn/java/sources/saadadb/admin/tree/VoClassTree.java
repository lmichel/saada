package saadadb.admin.tree;

import java.awt.Window;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.meta.AttributeHandler;
import saadadb.meta.MetaClass;
import saadadb.meta.MetaCollection;

public class VoClassTree extends VoDataProductTree {
	private MetaClass mc;

	public VoClassTree(Window frame, String  classe) throws Exception {
		super(frame, Database.getCachemeta().getClass(classe).toString());
		this.mc = Database.getCachemeta().getClass(classe);
		buildTree();
	}
	public VoClassTree(Window frame, MetaClass  mc) throws Exception {
		super(frame, mc.toString());
		this.mc = mc;
		buildTree();
	}

	/** * @version $Id$

	 * @param filename
	 * @throws FatalException 
	 */
	private void buildTree() throws FatalException {
		ArrayList<String> flat_array = new ArrayList<String>();
		LinkedHashMap<String, AttributeHandler> prd_map_org;
		prd_map_org = (LinkedHashMap<String, AttributeHandler>) MetaCollection.getAttribute_handlers(mc.getCategory());
		/*
		 * The map returned by the cache is cloned to avoid the alteration of the cache
		 */
		Map<String, AttributeHandler> prd_map = (Map<String, AttributeHandler>) prd_map_org.clone();
		prd_map.putAll(mc.getAttributes_handlers());
		for( String attname: prd_map.keySet()) {
			AttributeHandler ah = prd_map.get(attname);
			/*
			 * Housekeeping filed are ignored
			 */
			if( attname.equals("oidsaada") || attname.equals("oidproduct") || attname.equals("contentsignature") ||
				attname.equals("date_load") || attname.equals("access_right") || attname.equals("loaded") ||
				attname.equals("oidtable") ) {
				continue;
			}
			boolean set = false;
			String leaf;
			leaf = ah.getNameorg();
			if( leaf != null && leaf.length()!= 0 ) {
				set = true;
				flat_array.add(attname  + "<>Org Name:" +leaf);						
			}
			leaf = ah.getType();
			if( leaf != null && leaf.length()!= 0 ) {
				set = true;
				flat_array.add(attname  + "<>Type:" +leaf);						
			}
			leaf = ah.getUnit();
			if( leaf != null && leaf.length()!= 0 ) {
				set = true;
				flat_array.add(attname + "<>Unit:" +leaf);						
			}
			leaf = ah.getComment();
			if( leaf != null && leaf.length()!= 0 ) {
				set = true;
				flat_array.add(attname  + "<>Comment:\"" +leaf+ "\"");						
			}
			leaf = ah.getUcd();
			if( leaf != null && leaf.length()!= 0 ) {
				set = true;
				flat_array.add(attname + "<>UCD:" +leaf);						
			}
			leaf = ah.getUtype();
			if( leaf != null && leaf.length()!= 0 ) {
				set = true;
				flat_array.add(attname  + "<>Utype:" +leaf);						
			}
			leaf = ah.getValue();
			if( leaf != null && leaf.length()!= 0 ) {
				set = true;
				flat_array.add(attname  + "<>Value:" +leaf);						
			}
		}

		flat_types = flat_array.toArray(new String[0]);
	}
}