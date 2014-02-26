package saadadb.dataloader.mapping;

/**
 * The way to classify heterogenous products
 * @author michel
 * @version $Id$
 *
 */
public enum ClassifierMode {
	/**
	 * All in one class
	 */
	CLASS_FUSION,
	/**
	 * All ith the same format in one class
	 */
	CLASSIFIER,
	/**
	 * Nothing specified
	 */
	NOCLASSIFICATION
}
