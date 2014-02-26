package saadadb.api;

import java.sql.ResultSet;
import java.util.Map;

import saadadb.collection.Category;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.meta.MetaCollection;
import saadadb.sqltable.SQLQuery;
import saadadb.util.Messenger;

/**
 * @author michel
 * * @version $Id: SaadaCollection.java 555 2013-05-25 17:18:55Z laurent.mistahl $

 */
public class SaadaCollection extends SaadaDMBrik {
	private final MetaCollection metacollection;
	
	/** Constructeur de cette Collection de Saada
	 * @param name Nom de cette Collection de Saada
	 */
	public SaadaCollection(String name) throws FatalException {
		super(name);
		metacollection = Database.getCachemeta().getCollection(this.name);
	}

	   /**
     * @return
     */
    public String[] getAttributeNames(int category) {
        Map<String, AttributeHandler> hdls = metacollection.getAttribute_handlers(category);

        if( hdls != null ) {
        	String[] retour = new String[hdls.size()];
        	int i=0;
        	for( AttributeHandler ah: hdls.values()) {
            	retour[i] = ah.getNameattr();
        		i++;
            }
            return retour;
        }
       	Messenger.printMsg(Messenger.ERROR,"Can't get attributes for class  " + this.name);   	
		return null;
        }

	/**
	 * @return
	 */
	public String[] getUserAttributeNames(int category) throws FatalException {
		if (category == Category.IMAGE) {
			return Database.getCachemeta().getAtt_extend_image_names();
		} else if (category ==  Category.TABLE) {
			return Database.getCachemeta().getAtt_extend_table_names();
		} else if (category == Category.ENTRY) {
			return Database.getCachemeta().getAtt_extend_entry_names();
		} else if (category == Category.SPECTRUM ) {
			return Database.getCachemeta().getAtt_extend_spectrum_names();
		} else if (category == Category.MISC) {
			return Database.getCachemeta().getAtt_extend_misc_names();
		} else {
			FatalException.throwNewException(SaadaException.METADATA_ERROR, "Category " + category+ " doesn't exist");
			return null;
		}
	}

	/**
	 * @param att_name
	 * @return
	 */
	public String getUserAttributeType(int category, String att_name) throws FatalException {
		if (category == Category.IMAGE) {
			return Database.getCachemeta().getAtt_extend_image(att_name)
			.getNameattr();
		} else if (category == Category.TABLE) {
			return Database.getCachemeta().getAtt_extend_table(att_name)
			.getNameattr();
		} else if (category == Category.ENTRY) {
			return Database.getCachemeta().getAtt_extend_entry(att_name)
			.getNameattr();
		} else if (category == Category.SPECTRUM) {
			return Database.getCachemeta().getAtt_extend_spectra(att_name)
			.getNameattr();
		} else if (category == Category.MISC) {
			return Database.getCachemeta().getAtt_extend_misc(att_name)
			.getNameattr();
		} else {
			FatalException.throwNewException(SaadaException.METADATA_ERROR, "Category " + category+ " doesn't exist");
			return null;
		}
	}

	/**
	 * @param category
	 * @return
	 */
	public String[] getStartingRelationNames(int category) {
		return Database.getCachemeta().getRelationNamesStartingFromColl(this.name, category);
	}

	/**
	 * @param rel_name
	 * @param category
	 * @return
	 * @throws SaadaException 
	 */
	public SaadaRelation getStartingRelation(int category, String rel_name) throws FatalException {
		String[] rels = this.getStartingRelationNames(category);
		for (int i = 0; i < rels.length; i++) {
			if (rels[i].equals(rel_name) == true) {
				return new SaadaRelation(rel_name);
			}
		}
		FatalException.throwNewException(SaadaException.METADATA_ERROR, "Can't find relation " + rel_name
				+ " for category " + category + " starting from collection "
				+ this.name);
		return null;
	}

	/**
	 * @param category
	 * @return
	 */

	public String[] getEndingRelationNames(int category) {
		return Database.getCachemeta().getRelationNamesEndingOnColl(this.name, category);
	}

	/**
	 * @param rel_name
	 * @param category
	 * @return
	 */

	public SaadaRelation getEndingRelation(int category, String rel_name) throws FatalException{
		String[] rels = this.getEndingRelationNames(category);
		for (int i = 0; i < rels.length; i++) {
			if (rels[i].equals(rel_name) == true) {
				return new SaadaRelation(rel_name);
			}
		}
		FatalException.throwNewException(SaadaException.METADATA_ERROR, "Can't find relation " + rel_name
				+ " for category " + category + " ending to collection "
				+ this.name);
		return null;
	}

	/**
	 * @param category
	 * @return
	 */

	public String[] getClassNames(int category) {
		return Database.getCachemeta().getClassesOfCollection(this.name, category);
	}

	
	/**
	 * @param category
	 * @return
	 */
	public int getNumberOfInstances(int category) {
		ResultSet rs;
		try {
			
			SQLQuery squery = new SQLQuery();
			rs = squery.run("SELECT count(oidsaada) FROM " + this.name + "_" + Category.explain(category));
			while( rs.next() ) {
				squery.close();
				return rs.getInt(1);
			}
			squery.close();
		} catch (Exception e) {
			Messenger.printStackTrace(e);
		}
		return -1;
		
	}

	/* (non-Javadoc)
	 * @see saadadb.api.Saada_DM_Brik#explains()
	 */

	@Override
	public void explains() throws Exception {
	}
}
