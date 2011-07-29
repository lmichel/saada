package saadadb.resourcetest;

import java.io.File;

import saadadb.database.Database;
import saadadb.dataloader.Loader;
import saadadb.util.Messenger;

/**
 * @author laurentmichel
 * * @version $Id$

 */
public class ReadLinkedData {

	public static void main(String[] args) throws Exception {
		Messenger.debug_mode = false;
		 File f = new File("/rawdata/2xmmdr3/EpicSumSrcList/P0000110101EPX000OBSMLI0000.FIT.gz");
		 System.out.println(f.getAbsolutePath());
		 System.out.println(f.getCanonicalPath());
		 System.out.println(f.exists());
		 System.out.println((new File(f.getCanonicalPath())).getCanonicalPath());
		 Database.init("XCatDR3");
		 Loader ld = new Loader(new String[]{"-collection=EPIC"
				 , "-filename=/rawdata/2xmmdr3/EpicSumSrcList/P0000110101EPX000OBSMLI0000.FIT.gz"
				 , "-category=table"
				 , "XCatDR3"
				 });
		 ld.load();
	}
}
 