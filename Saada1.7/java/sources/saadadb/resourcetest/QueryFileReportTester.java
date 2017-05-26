package saadadb.resourcetest;

import java.io.BufferedReader;
import java.io.FileReader;

import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.vo.QueryFileReport;

public class QueryFileReportTester {

	/**
	 * @param args
	 * @throws FatalException 
	 */
	public static void main(String[] args) throws Exception {
		ArgsParser ap = new ArgsParser(args);
		Database.init(ap.getDBName());
		String query = ap.getQuery();
		String protocol = ap.getProtocol();
		int protoc = QueryFileReport.AUTO;
		if( "sia".equalsIgnoreCase(protocol)) {
			protoc = QueryFileReport.SIA;
		}
		else if( "ssa".equalsIgnoreCase(protocol)) {
			protoc = QueryFileReport.SSA;
		}
		else if( "cs".equalsIgnoreCase(protocol) || "cone search".equalsIgnoreCase(protocol) || "conesearch".equalsIgnoreCase(protocol)) {
			protoc = QueryFileReport.CS;
		}
		else if( "auto".equalsIgnoreCase(protocol)) {
			protoc = QueryFileReport.AUTO;
		}
		else if( "noprotocol".equalsIgnoreCase(protocol)) {
			protoc = QueryFileReport.NO_PROTOCOL;
		}

		QueryFileReport qfr = new QueryFileReport(protoc, "native class", query, "votable");
		qfr.getQueryReport("/tmp/qfr.xml", 1);
		BufferedReader bfr = new BufferedReader(new FileReader("/tmp/qfr.xml"));
		String str;
		while( (str = bfr.readLine() ) != null ) {
			System.out.println(str);
		}
		Database.close();
	}

}
