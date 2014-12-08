package saadadb.vocabulary.enums;

/**
 * Allowed types FITS extension
 * BINTABLE or ASCIITABLE cab also be used for VOTables
 * @author michel
 * @version $Id$
 */
public enum DataFileExtensionType {
	/**
	 * Simple header
	 */
	BASIC,
	/**
	 * simple image
	 */
	IMAGE,
	/**
	 * compressed image
	 */
	TILE_COMPRESSED_IMAGE,
	/**
	 * Binary table
	 */
	BINTABLE,
	/**
	 * ascii table
	 */
	ASCIITABLE,
	/**
	 * Columns description taken as KWs
	 */
	TABLE_COLUMNS,
	/**
	 * Anything else
	 */
	UNSUPPORTED
}
