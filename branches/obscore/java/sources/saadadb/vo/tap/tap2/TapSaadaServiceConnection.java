package saadadb.vo.tap.tap2;

import java.io.File;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import saadadb.database.Database;
import tap.ServiceConnection;
import tap.TAPFactory;
import tap.file.LocalTAPFileManager;
import tap.file.TAPFileManager;
import tap.formatter.OutputFormat;
import tap.formatter.ResultSet2JsonFormatter;
import tap.formatter.ResultSet2TextFormatter;
import tap.formatter.ResultSet2VotableFormatter;
import tap.log.TAPLog;
import tap.metadata.TAPMetadata;
import uws.UWSException;
import uws.job.user.DefaultJobOwner;
import uws.job.user.JobOwner;
import uws.service.UWSUrl;
import uws.service.UserIdentifier;

public class TapSaadaServiceConnection implements ServiceConnection<ResultSet> {
	private final UserIdentifier identifier;
	private final SaadaTapFactory tapFactory;
	private final TAPFileManager fileManager;
	private final SaadaTapLog logger;
	private final ArrayList<String> coordSys;
	private final ArrayList<OutputFormat<ResultSet>> formats;
	private final Properties properties;
	private boolean metaDataAvailable = true;

	public TapSaadaServiceConnection() throws Exception {
		// Create a way to identify users (by IP address)

		identifier = new UserIdentifier() {

			private static final long serialVersionUID = 3672135070597868792L;

			@Override
			public JobOwner extractUserId(UWSUrl urlInterpreter, HttpServletRequest request)
					throws UWSException {
				return new DefaultJobOwner(request.getRemoteAddr());
			}

			@Override
			public JobOwner restoreUser(String userId, String userName, Map<String, Object> params)
					throws UWSException {
				JobOwner user = new DefaultJobOwner(userId, userName);
				user.restoreData(params);
				return user;
			}

		};
		properties = SaadaTapProperties.getProperties();
		tapFactory = new SaadaTapFactory(this);
		fileManager = new LocalTAPFileManager(new File("/home/hahn/tapfile"));
		logger = new SaadaTapLog();
		
		//Output Formats
		formats = new ArrayList<OutputFormat<ResultSet>>();
		formats.add(new ResultSet2VotableFormatter(this));
		formats.add(new ResultSet2JsonFormatter(this));
		formats.add(new ResultSet2TextFormatter(this));
		/*
		 * The coordinate systems allowed
		 */

		coordSys = new ArrayList<String>();
		coordSys.add(Database.getAstroframe().name); //The default coordinate system of the DB
//		coordSys.add("ICRS");
//		coordSys.add("FK4");
//		coordSys.add("GALACTIC");
//		coordSys.add("ECLIPTIC"); 
//		coordSys.add("GAL");
//		coordSys.add("ECL");
		// ICRS, FK4 FK5 GALACTIC ECLIPTIC GAL ECL
	}

	/**
	 * Get the provider name
	 */
	@Override
	public String getProviderName() {
		return properties.getProperty("provider.id", SaadaTapProperties.DEFAULT_PROVIDER_ID);
	}

	/**
	 * Get the provider description
	 */
	@Override
	public String getProviderDescription() {

		return properties.getProperty(
				"provider.description",
				SaadaTapProperties.DEFAULT_PROVIDER_DESCRIPTION);
	}

	/**
	 * @return true if the tap service is available, false otherwise
	 */
	@Override
	public boolean isAvailable() {
		return metaDataAvailable;
	}

	/**
	 * Checks the availability of the service
	 * @return a human readable String telling is the service is available or not
	 */
	@Override
	public String getAvailability() {
		if (isAvailable()) {
			return "service is online";
		} else {
			return "service is offline";
		}
	}

	/**
	 * get the retention period of a Job's result
	 * @return an int[2]{defaultLimit, hardLimit}
	 */
	@Override
	public int[] getRetentionPeriod() {
		int limit = Integer.parseInt(properties.getProperty(
				"service.retention.limit",
				SaadaTapProperties.DEFAULT_SERVICE_RETENTION_LIMIT));
		int limitMax = Integer.parseInt(properties.getProperty(
				"service.retention.limit.max",
				SaadaTapProperties.DEFAULT_SERVICE_RETENTION_LIMIT_MAX));
		return new int[] { limit, limitMax };
	}

	/**
	 * Get the max duration time of a Job
	 *  @return an int[2]{defaultLimit, hardLimit}
	 */
	@Override
	public int[] getExecutionDuration() {
		int limit = Integer.parseInt(properties.getProperty(
				"service.execution.limit",
				SaadaTapProperties.DEFAULT_SERVICE_EXECUTION_LIMIT));
		int limitMax = Integer.parseInt(properties.getProperty(
				"service.execution.limit.max",
				SaadaTapProperties.DEFAULT_SERVICE_EXECUTION_LIMIT_MAX));
		return new int[] { limit, limitMax };
	}

	/**
	 * Get the max number of rows the service can provide
	 *  @return an int[2]{defaultLimit, hardLimit}
	 */
	@Override
	public int[] getOutputLimit() {
		int limit = Integer.parseInt(properties.getProperty(
				"service.out.limit",
				SaadaTapProperties.DEFAULT_SERVICE_OUT_LIMIT));
		int limitMax = Integer.parseInt(properties.getProperty(
				"service.out.limit.max",
				SaadaTapProperties.DEFAULT_SERVICE_OUT_LIMIT_MAX));
		return new int[] { limit, limitMax };
	}

	/**
	 * Get the unit of the output limit. It can be either rows or size
	 */
	@Override
	public tap.ServiceConnection.LimitUnit[] getOutputLimitType() {
		return new LimitUnit[] { LimitUnit.rows, LimitUnit.rows };
	}

	/**
	 * Get the UserIdentifier
	 */
	@Override
	public UserIdentifier getUserIdentifier() {
		return identifier;
	}

	/**
	 * Checks if upload is enabled
	 * @return true if upload is enabled, false otherwise
	 */
	@Override
	public boolean uploadEnabled() {
		return true;
	}

	/**
	 * get the number of rows (or size, depending of the chosen unit) a user can upload to the server
	 */
	@Override
	public int[] getUploadLimit() {
		int limit = Integer.parseInt(properties.getProperty(
				"service.upload.limit.rows",
				SaadaTapProperties.DEFAULT_SERVICE_UPLOAD_LIMIT_ROWS));
		return new int[] { limit, limit };
	}

	/**
	 * Get the unit of the upload limit. It can be either rows or data size (bytes)
	 */
	@Override
	public tap.ServiceConnection.LimitUnit[] getUploadLimitType() {
		return new LimitUnit[] { LimitUnit.rows, LimitUnit.rows };
	}

	/**
	 * get the max upload size (bytes) a user can upload to the service
	 */
	@Override
	public int getMaxUploadSize() {
		int limit = Integer.parseInt(properties.getProperty(
				"service.upload.limit.size",
				SaadaTapProperties.DEFAULT_SERVICE_UPLOAD_LIMIT_SIZE));
		return limit;
	}

	/**
	 * Read the Database to get all available TapMetadata (Schemas, tables, columns)
	 * @retun TapMetadata a representation of all metadata available to the tap service
	 */
	@Override
	public TAPMetadata getTAPMetadata() {
		try {
			TAPMetadata meta = new TAPMetadata();
			meta = new SaadaTapMeta().getTAPMetadata();
			if (!meta.isEmpty())
				metaDataAvailable = true;
			return meta;
		} catch (Exception e) {
			metaDataAvailable = false;
			return null;
		}
	}

	/**
	 * Return the available coordinates systems
	 */
	@Override
	public Collection<String> getCoordinateSystems() {
		return coordSys;
	}

	/**
	 * return the logger
	 */
	@Override
	public TAPLog getLogger() {
		return logger;
	}
/**
 * return the TapFactory
 */
	@Override
	public TAPFactory<ResultSet> getFactory() {
		return tapFactory;
	}
/**
 * Return the file manager
 */
	@Override
	public TAPFileManager getFileManager() {

		return fileManager;
	}

	@Override
	public Iterator<OutputFormat<ResultSet>> getOutputFormats() {

		return formats.iterator();
	}

	@Override
	public OutputFormat<ResultSet> getOutputFormat(String mimeOrAlias) {
		for (OutputFormat<ResultSet> f : formats) {
			if ((f.getMimeType() != null && f.getMimeType().equalsIgnoreCase(mimeOrAlias))
					|| (f.getShortMimeType() != null && f.getShortMimeType().equalsIgnoreCase(
							mimeOrAlias)))
				return f;
		}
		return null;
	}

}