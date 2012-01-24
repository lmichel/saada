package utils;

/**
 * @author laurentmichel
 *
 */
public class TreePath {
	private final String collection;
	private final String category;
	private final String classe;
	
	/**
	 * @param collection
	 * @param category
	 * @param classe
	 */
	public TreePath(String collection, String category , String classe) {
		this.collection = collection;
		this.category = category;
		this.classe = classe;
	}

	/**
	 * @return
	 */
	public boolean isClassLevel() {
		return (classe != null);
	}
	
	public String toString() {
		String retour=collection + "." + category ;
		if( classe != null) {
			retour += "." + classe;
		}
		return retour;
	}

}
