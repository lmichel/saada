/**
 * 
 */
package saadadb.products.datafile;

import java.util.List;

import saadadb.meta.AttributeHandler;
import saadadb.vocabulary.enums.DataFileExtensionType;

/**
 * Class used to map the extensions accessible within a data product;
 * This class is file format agnostic: no logic checking the attributes handlers
 * @author michel
 * @version $Id$
 */
public class DataFileExtension {
	public final int resourceNum;
	public final String resourceName;
	public final int tableNum;
	public final String tableName;
	public final DataFileExtensionType type;
	public final List<AttributeHandler> attributeHandlers;
	/**
	 * Direct access to the data (type depends on the file format
	 */
	public final Object resource;


	/**
	 * @param tableNum
	 * @param tableName
	 * @param type
	 * @param resource
	 * @param attributeHandlers
	 */
	public DataFileExtension(int tableNum, String tableName, DataFileExtensionType type, Object resource, List<AttributeHandler> attributeHandlers) {
		this.resourceNum = 0;
		this.resourceName = "No name";
		this.tableNum = tableNum;
		this.tableName = (tableName != null)? tableName: "No name";;
		this.type = type;
		this.attributeHandlers = attributeHandlers;
		this.resource = resource;
	}
	
	/**
	 * Used for VOTables
	 * @param resourceNum
	 * @param tableNum
	 * @param name
	 * @param type
	 * @param attributeHandlersnull
	 */
	public DataFileExtension(int resourceNum, String resourceName, int tableNum, String tableName, DataFileExtensionType type, List<AttributeHandler> attributeHandlers) {
		this.resourceNum = resourceNum;
		this.resourceName = (resourceName != null)? resourceName: "No name";
		this.tableNum = tableNum;
		this.tableName = (tableName != null)? tableName: "No name";;
		this.type = type;
		this.attributeHandlers = attributeHandlers;
		this.resource = null;
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
		return this.resourceNum + ":" + this.resourceName + " " + this.tableNum + ":" + this.tableName + " " + this.type + " " + this.attributeHandlers.size() + " columns";
	}
}