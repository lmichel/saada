package junit;

import java.util.LinkedHashMap;

import junit.framework.TestCase;
import saadadb.collection.Category;
import saadadb.database.Database;
import saadadb.query.constbuilders.DMField;
import saadadb.query.constbuilders.NativeSQLConstraint;
import saadadb.query.constbuilders.SaadaQLConstraint;
import saadadb.query.constbuilders.UCDField;
import saadadb.query.merger.Merger;

/**
 * @author michel
 *
 * example of running query:

SELECT oidsaada, column1, column2 FROM(
    SELECT oidsaada, Collection0_image.namesaada as column2, Aldebaran010._origin as column1 
    FROM Collection0_image
    INNER JOIN Aldebaran010 USING (oidsaada) 
    WHERE Aldebaran010._origin = 'qqq'
) as t1
*/
public class MergerTest extends TestCase {
	public void testGetSQL() {
		Database.init("BENCH2_0_MSQL");
		Merger merger;
		SaadaQLConstraint q1;
		SaadaQLConstraint q2;
		SaadaQLConstraint q3;
		LinkedHashMap<String, SaadaQLConstraint>	builders;
		try {
			q1 = new NativeSQLConstraint("isinbox(s_ra, s_dec,size,size, 23.462541666666667,30.66016666666666)", new String[]{"s_ra", "s_dec"}) ;    
			q1 = new NativeSQLConstraint("oidsaada = 0", new String[]{"oidsaada"}) ;    
			q1 = new UCDField("num.unit",  ">" , "2000", "km/s");
			builders = new LinkedHashMap<String, SaadaQLConstraint>();
			builders.put("num_unit", q1);
			merger = new Merger(Category.ENTRY, builders, false);
			merger.addCollection("UCDTester1");
			
			System.out.println(merger.getSQL());
			System.exit(1);
			
			q1 = new NativeSQLConstraint("oidsaada = 0", new String[]{"oidsaada"}) ;    
			builders = new LinkedHashMap<String, SaadaQLConstraint>();
			builders.put("native", q1);
			merger = new Merger(Category.ENTRY, builders, false);
			merger.addCollection("UCDTester1");
			assertEquals(" (SELECT oidsaada\n FROM UCDTester1_entry\n WHERE UCDTester1_entry.oidsaada = 0)"
					, merger.getSQL().replaceAll(" +", " "));
			
			q1 = new NativeSQLConstraint("oidsaada = 0", new String[]{"oidsaada"}) ;    
			builders = new LinkedHashMap<String, SaadaQLConstraint>();
			builders.put("native", q1);
			merger = new Merger(Category.ENTRY, builders, false);
			merger.addCollection("UCDTester1");
			merger.addCollection("UCDTester2");
			assertEquals("SELECT oidsaada FROM (\n (SELECT oidsaada\n FROM UCDTester1_entry\n WHERE UCDTester1_entry.oidsaada = 0)\n ) AS tcoll1\nUNION (\n (SELECT oidsaada\n FROM UCDTester2_entry\n WHERE UCDTester2_entry.oidsaada = 0)\n ) AS tcoll2\n)"
             , merger.getSQL().replaceAll(" +", " "));
			
			System.exit(1);

			q1 = new NativeSQLConstraint("_c1t1c1_sansu > s_ra", new String[]{"s_ra", "_c1t1c1_sansu"}) ;    
			q2 = new UCDField("num.unit",  ">" , "2000", "km/s");

			
			builders = new LinkedHashMap<String, SaadaQLConstraint>();
			builders.put("native", q1);
//			builders.put("num_unit", q2);
			merger = new Merger(Category.ENTRY, builders, false);
			merger.addCollection("UCDTester1");
			//merger.addClass("UCDTester1", "UCDTester1_UCDTagged1Entry");
			System.out.println(merger.getSQL());
			System.exit(1);
			
			q1 = new DMField(null, "my.utype", "[]", "(12, 34)", "km/s") ;
			builders = new LinkedHashMap<String, SaadaQLConstraint>();
			builders.put("my_utype", q1);
			merger = new Merger(Category.ENTRY, builders, false);
			merger.addCollection("MaCollection");
			System.out.println(merger.getSQL());
	
			//			DevelopConstraintBuilder q1 = new DevelopConstraintBuilder("_qqq", "> 5") ;
//			DevelopConstraintBuilder q2 = new DevelopConstraintBuilder("aaaa", " = 'copll'") ;
//			LinkedHashMap<String, SQLConstraintBuilder>	builders = new LinkedHashMap<String, SQLConstraintBuilder>();
//			builders.put("column1", q1);
//			builders.put("column2", q2);
//			merger = new Merger(Category.ENTRY, builders);
//			merger.addCollection("MaCollection");
//			merger.addClass("MaCollection", "UneClasse");
//			merger.addClass("MaCollection", "AutreClasse");
//			System.out.println(merger.getSQL());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
