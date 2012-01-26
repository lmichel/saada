package saadadb.admintool.utils;

import javax.swing.tree.TreePath;

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
}
