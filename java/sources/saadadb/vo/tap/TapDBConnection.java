package saadadb.vo.tap;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;

import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.util.DBUtils;
import saadadb.util.Messenger;
import saadadb.vocabulary.enums.DBType;
import tap.db.DBConnection;
import tap.db.DBException;
import tap.metadata.TAPColumn;
import tap.metadata.TAPTable;
import tap.metadata.TAPTypes;
import cds.savot.model.SavotTR;
import cds.savot.model.TDSet;


/**
 * 
 * @author hahn
 *
 */
public class TapDBConnection implements DBConnection<ResultSet> {

	private String jobId; // The jobID assigned by the TAP service
	private TapToDBTransaction transactionMaker;

	public TapDBConnection(String jobId) {
		this.jobId = jobId;
		transactionMaker = new TapToDBTransaction();
	}

	/**
	 * @return The job ID
	 */
	@Override
	public String getID() {
		return jobId;
	}

	/**
	 * Gets a connection & initiate a transaction to the DB
	 */
	@Override
	public void startTransaction() throws DBException {
		transactionMaker.beginTransaction();
	}

	/**
	 * Cancel the current transaction
	 * 
	 */
	@Override
	public void cancelTransaction() throws DBException {
		try {
			transactionMaker.rollback();
			Messenger.locateCode("Transaction canceled");
		} catch (Exception e) {
			Messenger.locateCode("Error while canceling transaction :" + e.getMessage());
		}
		// Messenger
		// .printMsg(Messenger.DEBUG, "The query can't be canceled : Feature not implemented");
	}

	/**
	 * Ends he transaction (close the connection) if the tap query has benn executed. do nothing otherwide
	 * @throws DBException
	 */
	@Override
	public void endTransaction() throws DBException {
		try {
			transactionMaker.endTransaction();
		} catch (Exception e) {
			throw new DBException("Could not end transaction", e);
		}
	}

	@SuppressWarnings("finally")
	@Override
	public ResultSet executeQuery(String sqlQuery, adql.query.ADQLQuery adqlQuery)
			throws DBException {
		Messenger.locateCode("===================\nADQL:\t" + adqlQuery.toADQL() + "\nSQL:\t"
				+ sqlQuery
				+ "\n========================================================================\n"
				+ " at");
		ResultSet rs;
		// Try to execute the query
		try {
			transactionMaker.setTapQuery(sqlQuery);
			transactionMaker.commit();
			rs = transactionMaker.getResultset();
			return rs;
			// Try to close the connection to the DB and throws exeception
		} catch (Exception e) {
			try {
				transactionMaker.forceCloseTransaction();
			//	Messenger.locateCode("Connection to DB closed due to errors: " + e.getMessage());
			} finally {
				throw new DBException(
						"Failed to upload the table or execute the queries to the DB: "+e.getMessage(),
						e);
			}
		}
	}

	@Override
	public void createSchema(String schemaName) throws DBException {
		// System.out.println("TabSaadaDBConnection#createSchema");
	}

	@Override
	public void dropSchema(String schemaName) throws DBException {
		// System.out.println("TabSaadaDBConnection#dropSchema");
	}

	/**
	 * Creates a temporary table from the TAPTable
	 * @param TAPTable  table to create in the DB
	 */
	@Override
	public void createTable(TAPTable table) throws DBException {

		int NO_SIZE = TAPTypes.NO_SIZE; // NO_SIZE (-1) & STAR_SIZE (12345) are values used by the TAP service
		// to determine if the arraysize of a field contains a * or nothing
		int STAR_SIZE = TAPTypes.STAR_SIZE;
		StringBuffer fmt = new StringBuffer("(");
		StringBuffer message = new StringBuffer();

		// check if the table we want to upload already exists in the DB
		if (!Database.getCachemeta().isNameAvailable(table.getName(), message)) {
			throw new DBException("table identifier '" + table.getName()
					+ "' is a reserved word: " + message.toString());
		}
		Iterator<TAPColumn> it = table.getColumns();
		while (it.hasNext()) {
			TAPColumn col = it.next();
			try {
				// Build ' "column name" datatype, ' String
				fmt.append("")
				.append((col.getName()))
						.append(" ")
						.append(
								Database.getWrapper().getDBTypeFromVOTableType(
										col.getVotType().datatype,
										col.getVotType().arraysize,
										NO_SIZE,
										STAR_SIZE));
			} catch (FatalException e) {
				throw new DBException("Error while determining dataType for " + table.getName()
						+ "." + col.getName());
			}
			if (it.hasNext())
				fmt.append(", ");
		}
		fmt.append(")");
		try {

			String sql = Database.getWrapper().getCreateTempoTable(table.getName(), fmt.toString());
			// SQLite needs the temporary table to be created explicitly as it checks if the table exists when compiling a prepared
			// statement
			transactionMaker.createTable(sql);
		} catch (Exception e) {
			throw new DBException("Unable to generate the table creation query for table '"
					+ table.getName() + "' : " + e.getMessage(), e);
			// throw new DBException("Unable to generate the table creation query for table '"
			// + table.getName() + "'", e);
		}
	}

	/**
	 * Insert the given rows in the Given TAPTable
	 * @throws  
	 */
	@Override
	public void insertRow(SavotTR row, TAPTable table) throws DBException {

		TDSet cells = row.getTDs();
		Iterator<TAPColumn> it = table.getColumns();
		TAPColumn col;
		// ArrayList<String> columnsName = new ArrayList<String>();
		ArrayList<String> sqlValues = new ArrayList<String>();
		ArrayList<String> columnDatatype = new ArrayList<String>();
		int nb = 0;
		// Get the Data to insert
		// INSERT INTO TABLENAME ( COLNAME1, COLNAME2 ...) VALUES (?, ? ...)
		StringBuffer sql = new StringBuffer();
					sql.append("INSERT INTO ");
			if (DBUtils.getDBType()==DBType.MYSQL) {
				sql.append(DBUtils.getTempoDBName()).append(".");
			}
			sql.append(table.getName()).append(" (");
		
		while (it.hasNext()) {
			if (nb > 0)
				sql.append(" ,");
			col = it.next();
			sql.append("").append((col.getName())).append(" ");
			sqlValues.add(cells.getContent(nb));
			columnDatatype.add(col.getVotType().datatype);
			nb++;
		}
		sql.append(")");
		sql.append("VALUES(");
		for (int i = 0; i < nb; i++) {
			if (i > 0)
				sql.append(" ,");
			sql.append("?");
		}
		sql.append(")");
		/*
		 * Get a PreparedStatement, fill it with values 
		 */
		try {

			PreparedStatement statement = transactionMaker.getPreparedStatement(sql.toString());
			// Fill the prepared statement with value
			for (int i = 1; i < nb + 1; i++) {

				String value = sqlValues.get(i - 1).trim();
				String datatype = columnDatatype.get(i - 1).toLowerCase().trim();
				boolean isNullOrEmpty = false;
				if (value == null || value.isEmpty())
					isNullOrEmpty = true;
				switch (datatype) {
				case "varbinary":
					if (isNullOrEmpty)
						statement.setNull(i, Types.VARBINARY);
					else
						statement.setBytes(i, value.getBytes());
					break;
				case "char":
					statement.setString(i, isNullOrEmpty ? "" : value);
					break;
				case "int":
					if (isNullOrEmpty)
						statement.setNull(i, Types.INTEGER);
					else
						statement.setInt(i, Integer.parseInt(value));
					break;
				case "float":
					if (isNullOrEmpty)
						statement.setNull(i, Types.FLOAT);
					else
						statement
								.setFloat(
										i,
										value.equalsIgnoreCase("NaN") ? Float.NaN : Float
												.parseFloat(value));
					break;
				case "unsignedbyte":
				case "short":
					if (isNullOrEmpty)
						statement.setNull(i, Types.SMALLINT);
					else
						statement.setShort(i, Short.parseShort(value));
					break;
				case "double":
					if (isNullOrEmpty)
						statement.setNull(i, Types.DOUBLE);
					else
						statement.setDouble(
								i,
								value.equalsIgnoreCase("NaN") ? Float.NaN : Double
										.parseDouble(value));
					break;
				case "long":
					if (isNullOrEmpty)
						statement.setNull(i, Types.BIGINT);
					else
						statement.setLong(i, Long.parseLong(value));
					break;
				default:
					statement.setObject(i, value);
				}
			}
			// Add the preparedStatement to the batch
			statement.addBatch();
		} catch (SQLException e1) {
			throw new DBException("Failed to get a PreparedStatement");
		}
	}

	/**
	 * Drops the given table from the DB (if necessary)
	 * @param TAPTable, the table to drop
	 */
	@Override
	public void dropTable(TAPTable table) throws DBException {
		Messenger.locateCode("Drop table : "+table.getName());
		try {
			String sql = Database.getWrapper().getDropTempoTable(table.getName());
			transactionMaker.executeUpdate(sql);
		} catch (Exception e) {
			throw new DBException("Failed to free the database Connection", e);
		}
	}

	/**
	 * Close the transaction if the tapQuery has been executed. do nothing otherwise
	 */
	@Override
	public void close() throws DBException {
		try {
			transactionMaker.endTransaction();
		} catch (Exception e) {
			throw new DBException("Failed to end the transaction", e);
		}
	}
}