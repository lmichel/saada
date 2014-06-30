/**
 * 
 */
package saadadb.products;

import java.util.List;

import saadadb.enums.DataFileExtensionType;
import saadadb.meta.AttributeHandler;

/**
 * Class used to map the extensions accessible within a data product;
 * This class is file format agnostic: no logic checking the attributes handlers
 * @author michel
 * @version $Id$
 */
public class DataFileExtension {
	public final int num;
	public final String name;
	public final DataFileExtensionType type;
	public final List<AttributeHandler> attributeHandlers;

	/**
	 * @param num
	 * @param name
	 * @param type
	 * @param attributeHandlers
	 */
	public DataFileExtension(int num, String name, DataFileExtensionType type, List<AttributeHandler> attributeHandlers) {
		super();
		this.num = num;
		this.name = name;
		this.type = type;
		this.attributeHandlers = attributeHandlers;
	}
	
	/**
	 * @return
	 */
	public boolean isDataTable() {
		return (this.type == DataFileExtensionType.ASCIITABLE || this.type == DataFileExtensionType.BINTABLE);
	}
	/**
	 * Returns the type  as as String
	 * @return
	 */
	public boolean isImage() {
		return (this.type == DataFileExtensionType.IMAGE || this.type == DataFileExtensionType.TILE_COMPRESSED_IMAGE);
	}
	
	/**
	 * @return
	 */
	public String getSType() {
		return (isImage())? "IMAGE": (isDataTable())? "TABLE": "UNKNOWN";
	}
	
	public String toString() {
		return this.num + " " + this.name + " " + this.type + " " + this.attributeHandlers.size() + " columns";
	}
}
