package saadadb.vo.tap;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import saadadb.database.Database;
import saadadb.exceptions.SaadaException;

public class TapProperties {
	/*
	 * Hard Coded values used in case of the properties file cannot be loaded.
	 */
	public static final String 		DEFAULT_PROVIDER_ID = "Saada";
	public static final String 		DEFAULT_PROVIDER_DESCRIPTION = "Saada's TAP Service";
	public static final String 		DEFAULT_SERVICE_OUT_LIMIT = "10000";
	public static final String 		DEFAULT_SERVICE_OUT_LIMIT_MAX = "100000";
	public static final String 		DEFAULT_SERVICE_EXECUTION_LIMIT = "7200";
	public static final String 		DEFAULT_SERVICE_EXECUTION_LIMIT_MAX = "86400";
	public static final String 		DEFAULT_SERVICE_RETENTION_LIMIT = "7200";
	public static final String 		DEFAULT_SERVICE_RETENTION_LIMIT_MAX = "86400";
	public static final String 		DEFAULT_SERVICE_UPLOAD_LIMIT_ROWS = "10000";
	public static final String 		DEFAULT_SERVICE_UPLOAD_LIMIT_SIZE = "50000";

	private static Properties properties;
	private static ArrayList<String> columnFilters;
/**
 * Load the properties file voconf.tap.properties and return a Properties instance
 * @return a Properties
 * @throws Exception
 */
	public static Properties getProperties() throws Exception {
		if (properties == null) {
			FileInputStream in;
			properties = new Properties();
			try {
				in = new FileInputStream(Database.getRoot_dir() + "/config/voconf.tap.properties");
				properties.load(in);
			} catch (IOException | IllegalArgumentException e) {
				SaadaException.throwNewException(SaadaException.FILE_ACCESS, e);
			}
		}
		return properties;
	}

	/**
	 * Creates an ArrayList of String from the properties. It contains all columns names that won't be available in the Metadata
	 * @return An List of 	all columns name that won't be put in the Metadata
	 */
	public static ArrayList<String> getColumnFilter() {
		if (columnFilters == null) {
			columnFilters = new ArrayList<String>();
			String rawFilter = properties.getProperty("filter.column");
			if (rawFilter != null) {
				String[] key = rawFilter.split(";");
				for (String current : key) {
					columnFilters.add(current.trim().toLowerCase());
				}
			}
		}
		return columnFilters;
	}
}
