package saadadb.vo.tap.tap2;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.QueryException;
import saadadb.sqltable.SQLLargeQuery;
import tap.db.DBConnection;
import tap.db.DBException;
import tap.metadata.TAPColumn;
import tap.metadata.TAPTable;
import tap.metadata.TAPTypes;
import cds.savot.model.TDSet;

public class TapSaadaDBConnection implements DBConnection<ResultSet> {

	private String jobId;
	private TapSqlTransaction transactionMaker;

	public TapSaadaDBConnection(String jobId) {
		this.jobId = jobId;
		transactionMaker = new TapSqlTransaction();
	}

	@Override
	public String getID() {
		return jobId;
	}

	@Override
	public void startTransaction() throws DBException {
		//transactionMaker.beginTransaction();
		System.out
				.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@TabSaadaDBConnection#startTransaction");
	}

	@Override
	public void cancelTransaction() throws DBException {
		System.out.println("The query can't be canceled : Feature not implemented");
	}

	@Override
	public void endTransaction() throws DBException {
		// try {
		// transactionMaker.endTransaction();
		// } catch (SQLException e) {
		//
		// }
		System.out
				.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@TabSaadaDBConnection#endTransaction");

	}

	@SuppressWarnings("finally")
	@Override
	public ResultSet executeQuery(String sqlQuery, adql.query.ADQLQuery adqlQuery)
			throws DBException {
		System.out
				.println("===================================== TabSaadaDBConnection#executeQuery");

		System.out.println("\nADQL:\t" + adqlQuery.toADQL());
		System.out.println("\nSQL:\t" + sqlQuery + "\n");
		System.out
				.println("=======================================================================");
		ResultSet rs;
		try {
			transactionMaker.addQueryToTransaction(sqlQuery, true);
			transactionMaker.commitTransaction();
			rs = transactionMaker.getResultSet();
			return rs;
		} catch (Exception e) {
			try {
				transactionMaker.forceCloseTransaction();
				System.out.println("Force close");
			} finally  {
				throw new DBException("Failed to upload the table or execute the queries to the DB", e);
			}
			
		}
		// ResultSet result;
		// try {
		// //query
		// result = query.run(sqlQuery);
		// return result;
		// } catch (QueryException e1) {
		// e1.printStackTrace();
		// }
	}

	@Override
	public void createSchema(String schemaName) throws DBException {
		System.out.println("TabSaadaDBConnection#createSchema");

	}

	@Override
	public void dropSchema(String schemaName) throws DBException {
		System.out.println("TabSaadaDBConnection#dropSchema");

	}

	@Override
	public void createTable(TAPTable table) throws DBException {
		System.out.println("TabSaadaDBConnection#createTable");
		int NO_SIZE = TAPTypes.NO_SIZE;
		int STAR_SIZE = TAPTypes.STAR_SIZE;
		StringBuffer fmt = new StringBuffer("(");
		Iterator<TAPColumn> it = table.getColumns();
		while (it.hasNext()) {
			TAPColumn col = it.next();
			try {
				//System.out.println("Col datatype : "+col.getDatatype());
				// System.out.println("COL:\t'"+col.getName()+"' datatype\t'"+col.getDatatype()+"' arraysize\t'"+col.getArraySize()+"'");
				System.out.println("COLUMN DBNAME "+col.getDBName()+" COLUMNNAME "+col.getName());
				fmt
						.append("\"")
						.append(col.getDBName())
						.append("\" ")
						.append(
								Database.getWrapper().getDBTypeFromVOTableType(
										col.getDatatype(),
										col.getArraySize(),
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
			String sql = Database.getWrapper().getCreateTempoTable(
					"\"" + table.getName() + "\"",
					fmt.toString());
			transactionMaker.addQueryToTransaction(sql, false);
		} catch (FatalException | QueryException e) {
			throw new DBException("Unable to generate the table creation query for table '"
					+ table.getName() + "'",e);
		}

		// try {
		// String sql = Database.getWrapper().getCreateTempoTable("\""+table.getName()+"\"", fmt.toString());
		// query.runUpdate(sql);
		// System.out.println("QUERY CREATE TABLE EXECUTED");
		// } catch (FatalException | QueryException e) {
		// e.printStackTrace();
		// throw new DBException("Error while creating table " + table.getName()+" :"+e.getMessage());
		// }

	}

	@Override
	public void insertRow(cds.savot.model.SavotTR row, TAPTable table) throws DBException {
		// System.out.println("TabSaadaDBConnection#insertRow");
		TDSet cells = row.getTDs();
		Iterator<TAPColumn> it = table.getColumns();
		TAPColumn col;
		ArrayList<String> columnsName = new ArrayList<String>();
		ArrayList<String> sqlValues = new ArrayList<String>();
		int i = 0;
		// Get the Data to insert
		while (it.hasNext()) {
			col = it.next();
			columnsName.add("\"" + col.getName() + "\"");
			String value = cells.getContent(i);
			if (value == null || value.isEmpty()) {
				sqlValues.add("\"NULL\"");
			} else if (value.equalsIgnoreCase("NaN")) {
				sqlValues.add("\"NaN\"");
			} else {
				sqlValues.add("\"" + value.replace("\0", "") + "\"");
			}
			i++;
		}

		// Execute the Query
		try {
			String sqlInsert = Database.getWrapper().getInsertStatement(
					"\"" + table.getName() + "\"",
					columnsName.toArray(new String[columnsName.size()]),
					sqlValues.toArray(new String[columnsName.size()]));
			// System.out.println("SQL INSERT \t"+sqlInsert);
			transactionMaker.addQueryToTransaction(sqlInsert, false);
			// query.runUpdate(sqlInsert);
		} catch (FatalException | QueryException e) {
			throw new DBException("Unable to generate data insertion query for table '"
					+ table.getName() + "'");
		}
	}

	@Override
	public void dropTable(TAPTable table) throws DBException {
		System.out.println("TabSaadaDBConnection#dropTable");

		try {
			String sql = Database.getWrapper().getDropTempoTable(table.getName());
			transactionMaker.addQueryToTransaction(sql, false);
			transactionMaker.commitTransaction();
			// FIXME Can't close the connection here, there might be more than one table
			transactionMaker.endTransaction();
		} catch (Exception e) {
			// Do something
			throw new DBException("Failed to free the database Connection", e);
		}
		/*
		 * These are temporary table, they will/should be dropped automatically
		 */
		/*original		try {
					String sql = Database.getWrapper().getDropTempoTable(table.getName());
					query.run(sql);
				} catch (FatalException | QueryException e) {
					throw new DBException("Error occured when trying to remove the table '"
							+ table.getName() + "'");
				}
				*/
	}

	@Override
	public void close() throws DBException {
		// if (query != null) {
		// try {
		// query.close();
		// } catch (QueryException e) {
		// e.printStackTrace();
		// }
		// }
		// The Database open/close is handled by saada
		try {
			transactionMaker.endTransaction();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("TabSaadaDBConnection#close");
	}

}