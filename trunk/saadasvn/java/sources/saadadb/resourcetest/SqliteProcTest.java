package saadadb.resourcetest;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.sqlite.Function;

public class SqliteProcTest {
    static Connection    conn;
    static Statement     stat;
    static int    val        = 0;
	public static void main(String[] args) throws Exception{
       Class.forName("org.sqlite.JDBC");
       conn = DriverManager.getConnection("jdbc:sqlite:");
       stat = conn.createStatement();
       Function.create(conn, "REGEXP", new Function() {
           @Override
           public void xFunc() throws SQLException {
        	   SqliteProcTest.val = 4;
        	   if( args() != 2 ) {
        		   throw new SQLException("REGEXP requires 2 String parameters");
        	   }

        	   String exp = value_text(0);
        	   String str = value_text(1);
        	   result((str.matches(exp))? 1: 0);
           }
       });
       ResultSet rs = stat.executeQuery("select REGEXP('.*b.*', 'abs');")  ; 
       
       System.out.println(rs.getObject(1));
       stat.executeQuery("select REGEXP('.*b.*', 'asss');")  ; 
       System.out.println(rs.getObject(1));
       stat.executeQuery("select REGEXP('.*b.*');")  ; 
       System.out.println(rs.getObject(1));
      stat.close();
       conn.close();
 
       
	}

}
