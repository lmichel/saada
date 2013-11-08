/**
 * 
 */
package saadadb.database;

import java.sql.Connection;
import java.sql.SQLException;
import org.sqlite.Function;

import saadadb.query.timespace.Procedures;
import saadadb.util.Messenger;

/**
 * Static class added to the current SQLite DB the time space procedures.
 * These procedures are currently used by the query engine.
 * They are embededd as SQL procedures with PSQL or MySQL
 * @author michel
 * @version $Id$
 *
 */
public class SQLiteUDF {

	/**
	 * Load all procedures in one step
	 * @param conn
	 * @throws SQLException
	 */
	public static final void LoadProcedures(Connection conn) throws SQLException {
		/*
		 * REGEXP procedure
		 */
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "loading procedure REGEXP");
		Function.create(conn, "REGEXP", new Function() {
			@Override
			public void xFunc() throws SQLException {
				if( args() != 2 ) {
					throw new SQLException("REGEXP requires 2 String parameters");
				}
				String exp = value_text(0);
				String str = value_text(1);
				result((str.matches(exp))? 1: 0);
			}
		});
		/*
		 * corner00_ra procedure
		 */
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "loading procedure corner00_ra");
		Function.create(conn, "corner00_ra", new Function() {
			@Override
			public void xFunc() throws SQLException {
				if( args() != 4 ) {
					throw new SQLException("corner00_ra requires 4 double parameters in degrees");
				}
				result(Procedures.corner00_ra(value_double(0), value_double(1), value_double(2), value_double(3)));
			}
		});		
		/*
		 * corner00_dec procedure
		 */
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "loading procedure corner00_dec");
		Function.create(conn, "corner00_dec", new Function() {
			@Override
			public void xFunc() throws SQLException {
				if( args() != 2 ) {
					throw new SQLException("corner00_dec requires 2 double parameters in degrees");
				}
				result(Procedures.corner00_dec(value_double(0), value_double(1)));
			}       
		});
		/*
		 * corner01_ra procedure
		 */
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "loading procedure corner01_ra");
		Function.create(conn, "corner01_ra", new Function() {
			@Override
			public void xFunc() throws SQLException {
				if( args() != 4 ) {
					throw new SQLException("corner01_ra requires 4 double parameters in degrees");
				}
				result(Procedures.corner01_ra(value_double(0), value_double(1), value_double(2), value_double(3)));
			}       
		});
		/*
		 * corner01_dec procedure
		 */
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "loading procedure corner01_dec");
		Function.create(conn, "corner01_dec", new Function() {
			@Override
			public void xFunc() throws SQLException {
				if( args() != 2 ) {
					throw new SQLException("corner01_dec requires 2 double parameters in degrees");
				}
				result(Procedures.corner01_dec(value_double(0), value_double(1)));
			}       
		});
		/*
		 * corner10_ra procedure
		 */
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "loading procedure corner10_ra");
		Function.create(conn, "corner10_ra", new Function() {
			@Override
			public void xFunc() throws SQLException {
				if( args() != 4 ) {
					throw new SQLException("corner10_ra requires 4 double parameters in degrees");
				}
				result(Procedures.corner10_ra(value_double(0), value_double(1), value_double(2), value_double(3)));
			}       
		});
		/*
		 * corner10_dec procedure
		 */
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "loading procedure corner10_dec");
		Function.create(conn, "corner10_dec", new Function() {
			@Override
			public void xFunc() throws SQLException {
				if( args() != 2 ) {
					throw new SQLException("corner10_dec requires 2 double parameters in degrees");
				}
				result(Procedures.corner10_dec(value_double(0), value_double(1)));
			}       
		});
		/*
		 * corner11_ra procedure
		 */
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "loading procedure corner11_ra");
		Function.create(conn, "corner11_ra", new Function() {
			@Override
			public void xFunc() throws SQLException {
				if( args() != 4 ) {
					throw new SQLException("corner11_ra requires 4 double parameters in degrees");
				}
				result(Procedures.corner11_ra(value_double(0), value_double(1), value_double(2), value_double(3)));
			}       
		});
		/*
		 * corner11_dec procedure
		 */
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "loading procedure corner11_dec");
		Function.create(conn, "corner11_dec", new Function() {
			@Override
			public void xFunc() throws SQLException {
				if( args() != 2 ) {
					throw new SQLException("corner11_dec requires 2 double parameters in degrees");
				}
				result(Procedures.corner11_dec(value_double(0), value_double(1)));
			}       
		});
		/*
		 * distancedegree procedure
		 */
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "loading procedure distancedegree");
		Function.create(conn, "distancedegree", new Function() {
			@Override
			public void xFunc() throws SQLException {
				if( args() != 4 ) {
					throw new SQLException("distancedegree requires 4 double parameters in degrees");
				}
				result(Procedures.distancedegree(value_double(0), value_double(1), value_double(2), value_double(3)));
			}       
		});
		/*
		 * tileleftborder procedure
		 */
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "loading procedure tileleftborder");
		Function.create(conn, "tileleftborder", new Function() {
			@Override
			public void xFunc() throws SQLException {
				if( args() != 4 ) {
					throw new SQLException("tileleftborder requires 4 double parameters in degrees");
				}
				result(Procedures.tileleftborder(value_double(0), value_double(1), value_double(2), value_double(3)));
			}       
		});
		/*
		 * tileleftborder procedure
		 */
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "loading procedure tilerightborder");
		Function.create(conn, "tilerightborder", new Function() {
			@Override
			public void xFunc() throws SQLException {
				if( args() != 4 ) {
					throw new SQLException("tilerightborder requires 4 double parameters in degrees");
				}
				result(Procedures.tilerightborder(value_double(0), value_double(1), value_double(2), value_double(3)));
			}       
		});
		/*
		 * isinbox procedure
		 */
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "loading procedure isinbox");
		Function.create(conn, "isinbox", new Function() {
			@Override
			public void xFunc() throws SQLException {
				if( args() != 6 ) {
					throw new SQLException("isinbox requires 4 double parameters in degrees");
				}
				result(Procedures.isinbox(value_double(0), value_double(1), value_double(2), value_double(3), value_double(4), value_double(5)));
			}       
		});
		/*
		 * getmjd procedure
		 */
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "loading procedure getmjd");
		Function.create(conn, "getmjd", new Function() {
			@Override
			public void xFunc() throws SQLException {
				if( args() != 1 ) {
					throw new SQLException("getmjd requires 1 double parameters");
				}
				result(Procedures.getmjd(value_double(0)));
			}       
		});
		/*
		 * boxcenter procedure
		 */
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "loading procedure boxcenter");
		Function.create(conn, "boxcenter", new Function() {
			@Override
			public void xFunc() throws SQLException {
				if( args() != 7 ) {
					throw new SQLException("boxcenter requires 7 double parameters in degrees");
				}
				result(Procedures.boxcenter(value_double(0), value_double(1), value_double(2), value_double(3), value_double(4), value_double(5), value_double(6)));
			}       
		});
		/*
		 * boxcovers procedure
		 */
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "loading procedure boxcovers");
		Function.create(conn, "boxcovers", new Function() {
			@Override
			public void xFunc() throws SQLException {
				if( args() != 7 ) {
					throw new SQLException("boxcovers requires 7 double parameters in degrees");
				}
				result(Procedures.boxcovers(value_double(0), value_double(1), value_double(2), value_double(3), value_double(4), value_double(5), value_double(6)));
			}       
		});
		/*
		 * boxenclosed procedure
		 */
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "loading procedure boxenclosed");
		Function.create(conn, "boxenclosed", new Function() {
			@Override
			public void xFunc() throws SQLException {
				if( args() != 7 ) {
					throw new SQLException("boxenclosed requires 7 double parameters in degrees");
				}
				result(Procedures.boxenclosed(value_double(0), value_double(1), value_double(2), value_double(3), value_double(4), value_double(5), value_double(6)));
			}       
		});
		/*
		 * boxoverlaps procedure
		 */
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "loading procedure boxoverlaps");
		Function.create(conn, "boxoverlaps", new Function() {
			@Override
			public void xFunc() throws SQLException {
				if( args() != 7 ) {
					throw new SQLException("boxoverlaps requires 7 double parameters in degrees");
				}
				result(Procedures.boxoverlaps(value_double(0), value_double(1), value_double(2), value_double(3), value_double(4), value_double(5), value_double(6)));
			}       
		});
	}
}
