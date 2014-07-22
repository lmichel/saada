package saadadb.enums;

/**
 * Allowed types FITS extension
 * BINTABLE or ASCIITABLE cab also be used for VOTables
 * @author michel
 * @version $Id$
 */
public enum DataFileExtensionType {
	BASIC,
	TILE_COMPRESSED_IMAGE,
	BINTABLE,
	ASCIITABLE,
	TABLE_COLUMNS,
	IMAGE,
	UNSUPPORTED
}
