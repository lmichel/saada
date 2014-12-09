package saadadb.admintool.utils;

import java.util.ArrayList;

import javax.swing.tree.TreePath;

import saadadb.collection.Category;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.QueryException;


/**
 * @author laurentmichel
 *
 */
public class DataTreePath {
	public final String collection;
	public final String category;
	public final String classe;
	private boolean isRoot; // To determine if the node is Root Level

	/**
	 * @param collection
	 * @param category
	 * @param classe
	 */
	public DataTreePath(String collection, String category , String classe) {
		this.collection = collection;
		this.category = category;
		this.classe = classe;
		if (this.collection == null && this.category == null && this.classe == null) // Root level
		{
			this.isRoot = true;
		}
		else
		{
			this.isRoot = false;
		}
	}

	public DataTreePath(TreePath treePath) throws QueryException {
		if (treePath!=null)
		{
			int count = treePath.getPathCount();
			Object[] pathEle = treePath.getPath();
			if( count == 2 ) {
				this.collection = pathEle[1].toString();
				this.category = null;;
				this.classe = null;		
				this.isRoot = false;
			}
			else if( count == 3 ) {
				this.collection = pathEle[1].toString();
				this.category = pathEle[2].toString();
				this.classe = null;	
				this.isRoot = false;
			}
			else if( count == 4 ) {
				this.collection = pathEle[1].toString();
				this.category = pathEle[2].toString();
				this.classe = pathEle[3].toString();
				this.isRoot = false;
			}
			else {
				this.collection = null;
				this.category = null;
				this.classe = null;
				if ( count == 1 ) // Root level
				{
					this.isRoot = true;
				}
				else
				{
					this.isRoot = false;
				}
			}
		}
		else
		{
			this.collection = null;
			this.category = null;
			this.classe = null;
			this.isRoot = true;
		}
	}
	
	public boolean isRootLevel()
	{
		return (this.isRoot && this.collection == null && this.category == null && this.classe == null);
	}

	/**
	 * @return
	 */
	public boolean isClassLevel() {
		return (!this.isRoot && classe != null);
	}

	/**
	 * @return
	 */
	public boolean isCategoryLevel() {
		return (!this.isRoot && classe == null && category != null);
	}

	/**
	 * @return
	 */
	public boolean isCollectionLevel() {
		return (this.isRoot==false && classe == null && category == null && collection != null);
	}
	
	public boolean isRootOrCollectionLevel()
	{
		return (this != null && (this.isCollectionLevel() || this.isRootLevel()));
	}
	
	public boolean isCategorieOrClassLevel()
	{
		return (this != null && (this.isCategoryLevel() || this.isClassLevel()));
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String retour;
		if (this.isRoot)
		{
			retour = "Collections";
		}
		else
		{
			retour = collection;
			if (category != null) {
				retour += "." + category;
				if( classe != null) {
					retour += "." + classe;
				}
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
