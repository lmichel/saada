package junit;

import java.util.LinkedHashMap;

import junit.framework.TestCase;
import saadadb.collection.Category;
import saadadb.query.constbuilders.SaadaQLConstraint;
import saadadb.query.constbuilders.UCDField;
import saadadb.query.merger.Merger;

/**
 * @author laurentmichel
 * * @version $Id$

 */
public class QueryDMTester extends TestCase {

	public QueryDMTester(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testGetSQL() {
		Merger merger;
		UCDField q1;
		LinkedHashMap<String, SaadaQLConstraint>	builders;
		try {
			q1 = new UCDField("my.utype", "[]", "(12, 34)", "km/s") ;
			builders = new LinkedHashMap<String, SaadaQLConstraint>();
			builders.put("column1", q1);
			merger = new Merger(Category.ENTRY, builders, false);
			merger.addCollection("MaCollection");
			System.out.println(merger.getSQL());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	protected void tearDown() throws Exception {
		super.tearDown();
	}

}

