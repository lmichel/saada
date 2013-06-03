package saadadb.admintool.utils;

import java.util.ArrayList;

import javax.swing.tree.TreePath;

import saadadb.collection.Category;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;


/**
 * @author laurentmichel
 *
 */
public class DataTreePath {
	public final String collection;
	public final String category;
	public final String classe;

	/**
	 * @param collection
	 * @param category
	 * @param classe
	 */
	public DataTreePath(String collection, String category , String classe) {
		this.collection = collection;
		this.category = category;
		this.classe = classe;
	}

	public DataTreePath(TreePath treePath) throws QueryException {
		int count = treePath.getPathCount();
		Object[] pathEle = treePath.getPath();
		if( count ==2 ) {
			this.collection = pathEle[1].toString();;
			this.category = null;;
			this.classe = null;		
		}
		else if( count ==3 ) {
			this.collection = pathEle[1].toString();;
			this.category = pathEle[2].toString();;
			this.classe = null;		
		}
		else if( count == 4 ) {
			this.collection = pathEle[1].toString();;
			this.category = pathEle[2].toString();;
			this.classe = pathEle[3].toString();;		
		}
		else {
			this.collection = null;
			this.category = null;
			this.classe = null;					
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER
					, "Data tree path must point on a category or a class");
		}
	}

	/**
	 * @return
	 */
	public boolean isClassLevel() {
		return (classe != null);
	}

	/**
	 * @return
	 */
	public boolean isCategoryLevel() {
		return (classe == null && category != null);
	}

	/**
	 * @return
	 */
	public boolean isCollectionLevel() {
		return (classe == null && category == null && collection != null);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String retour=collection  ;
		if( category != null) {
			retour += "." + category;
			if( classe != null) {
				retour += "." + classe;
			}
		}
		return retour;
	}
	
	/**
	 * Return the SQL table name storing data pointed by that tree path
	 * @return
	 * @throws FatalException
	 */
	public String getSQLTableName() throws FatalException {
		if( this.isClassLevel()) {
			return this.classe;
		} else if( this.isCategoryLevel() ) {
			return Database.getCachemeta().getCollectionTableName(this.collection, Category.getCategory(this.category));
		} else {
			return null;
		}
	}
	/**
	 * @return
	 */
	public String[] getElements() {
		ArrayList<String> retour = new ArrayList<String>();
		retour.add(collection);
		if( category != null) {
			retour .add(category);
			if( classe != null) {
				retour.add(classe);
			}
		}
		return retour.toArray(new String[0]);
		
	}
}
