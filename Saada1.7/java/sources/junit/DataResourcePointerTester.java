package junit;

import java.io.File;
import java.util.List;

import junit.framework.TestCase;
import saadadb.products.DataResourcePointer;

public class DataResourcePointerTester extends TestCase {
	public void testSortedList(){
		try {
			DataResourcePointer dataResourcePointer = new DataResourcePointer("/tmp");
			// load from the older to the newer
			List<File> lf1 = dataResourcePointer.getOrderedDirContent(true);
			for( File f: lf1){
				System.out.println(f.lastModified() +  " " + f);
			}
			// load from the newer to the older
			List<File> lf2 = dataResourcePointer.getOrderedDirContent(false);
			System.out.println("========================================");
			for( File f: lf2){
				System.out.println(f.lastModified() +  " " + f);
			}
		
			// Check teh dates
			for( int i=0 ; i<lf1.size() ; i++) {
				assertTrue(lf1.get(i).lastModified() == lf2.get(lf1.size() - i - 1).lastModified());
			}
		} catch (Exception e) {}
		
	}

}
