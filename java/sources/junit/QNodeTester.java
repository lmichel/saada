package junit;

import junit.framework.TestCase;
import saadadb.query.merger.QNode;

/**
 * @author michel
 *
 */
public class QNodeTester extends TestCase {


	public void testInsertAlias() {
		
		String SOURCE = "_c1t1c1_sansu > s_ra CLASSAND _c1t1c1_nu > 2000000.0";;
		String[] atts = new String[]{"_c1t1c1_sau", "_c1t1c1_sasu", "_c1t1c1_sanu", "_c1t1c1_sansu", "_c1t1c1_snu", "_c1t1c1_snsu", "_c1t1c1_nu", "_c1t1c1_nsu", "_c1t1c2_sau", "_c1t1c2_sasu", "_c1t1c2_sanu", "_c1t1c2_sansu", "_c1t1c2_snu", "_c1t1c2_snsu", "_c1t1c2_nu", "_c1t1c2_nsu"}; 
		assertEquals("alias._c1t1c1_sansu > s_ra CLASSAND alias._c1t1c1_nu > 2000000.0", QNode.insertAlias(SOURCE, atts, "alias"));
	}

}
